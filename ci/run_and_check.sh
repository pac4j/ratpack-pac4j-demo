#!/bin/bash

# Script to launch ratpack-pac4j-demo and verify it works
# Usage: ./run_and_check.sh

set -e  # Stop script on error

PORT=8080

echo "🚀 Starting ratpack-pac4j-demo..."

# Get script directory and go to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "📁 Working in: $(pwd)"

# Clean and compile project
echo "📦 Compiling Ratpack project..."
mvn clean package -q

# Ensure target directory exists
mkdir -p target

# Start Ratpack application in background using mvn exec:java with Java options
echo "🌐 Starting Ratpack application..."
MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED" mvn exec:java > target/app.log 2>&1 &
APP_PID=$!

# Wait for server to start (maximum 90 seconds)
echo "⏳ Waiting for server startup..."
for i in {1..90}; do
    # Check if the application is responding
    HTTP_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT 2>/dev/null || echo "000")
    if [ "$HTTP_RESPONSE" = "200" ] || [ "$HTTP_RESPONSE" = "301" ] || [ "$HTTP_RESPONSE" = "302" ]; then
        echo "✅ Server started successfully!"
        break
    fi
    
    # Also check logs for successful startup message
    if [ -f target/app.log ] && grep -q "Ratpack started" target/app.log; then
        echo "✅ Server startup detected in logs!"
        sleep 2  # Give it a moment more to be fully ready
        break
    fi
    
    if [ $i -eq 90 ]; then
        echo "❌ Timeout: Server did not start within 90 seconds"
        echo "📋 Server logs:"
        cat target/app.log || true
        kill $APP_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# Verify application responds correctly
echo "🔍 Verifying HTTP response..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "301" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "✅ Application responds with HTTP $HTTP_CODE"
    echo "🌐 Application accessible at: http://localhost:$PORT"

    # Default flags
    CAS_AUTH_PASSED=false
    
    # Test CAS authentication
    echo "🔗 Testing CAS authentication..."
    
    # Get the CAS URL from the homepage
    CAS_URL="http://localhost:$PORT/cas/index.html"
    echo "📍 Following CAS link: $CAS_URL"
    
    # Follow redirections and capture final URL and response
    CAS_RESPONSE=$(curl -s -L -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" "$CAS_URL")
    CAS_HTTP_CODE=$(echo "$CAS_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
    CAS_FINAL_URL=$(echo "$CAS_RESPONSE" | grep "FINAL_URL:" | cut -d: -f2-)
    CAS_CONTENT=$(echo "$CAS_RESPONSE" | sed '/^FINAL_URL:/d' | sed '/^HTTP_CODE:/d')
    
    echo "🌐 CAS Final URL: $CAS_FINAL_URL"
    echo "📄 CAS HTTP Code: $CAS_HTTP_CODE"
    
    # Verify we reached the CAS login page
    if [ "$CAS_HTTP_CODE" = "200" ] && echo "$CAS_CONTENT" | grep -q "Enter Username & Password"; then
        echo "✅ CAS login page test passed!"
        echo "🔐 Successfully redirected to CAS login page"
        
        # Simulate a CAS login using curl WITH cookies and follow redirects
        echo "🧪 Simulating CAS authentication via curl..."
        CAS_COOKIE_JAR="target/cas_cookies.txt"
        CAS_LOGIN_PAGE="target/cas_login.html"
        CAS_AFTER_LOGIN="target/cas_after_login.html"
        FINAL_APP_PAGE="target/final_app.html"

        # 1) Fetch the login page (keep cookies) and capture the execution token
        echo "⬇️  Fetching CAS login page and capturing execution token..."
        curl -s -c "$CAS_COOKIE_JAR" -b "$CAS_COOKIE_JAR" -L "$CAS_FINAL_URL" -o "$CAS_LOGIN_PAGE" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}\n" > target/cas_login_fetch.meta

        EXECUTION=$(grep -Eo 'name=\"execution\"[^>]*value=\"[^\"]+\"' "$CAS_LOGIN_PAGE" | sed -E 's/.*value=\"([^\"]+)\".*/\1/' | head -n1 || true)

        if [ -z "$EXECUTION" ]; then
            echo "❌ Could not extract CAS execution token from login page."
            CAS_AUTH_PASSED=false
        else
            echo "🔑 Found execution token: $EXECUTION"

            # 2) Post credentials to CAS with cookies and follow redirects
            echo "📤 Posting credentials to CAS and following redirects..."
            CAS_POST_RESPONSE=$(curl -s -c "$CAS_COOKIE_JAR" -b "$CAS_COOKIE_JAR" -L -o "$CAS_AFTER_LOGIN" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" \
                --data-urlencode "username=leleuj@gmail.com" \
                --data-urlencode "password=password" \
                --data-urlencode "execution=$EXECUTION" \
                --data-urlencode "_eventId=submit" \
                "$CAS_FINAL_URL")

            CAS_POST_HTTP_CODE=$(echo "$CAS_POST_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
            CAS_POST_FINAL_URL=$(echo "$CAS_POST_RESPONSE" | grep "FINAL_URL:" | cut -d: -f2-)

            echo "🌐 After CAS login final URL: $CAS_POST_FINAL_URL"
            echo "📄 CAS HTTP Code: $CAS_POST_HTTP_CODE"

            # 3) Fetch the final app page with cookies and show content
            echo "📥 Fetching final app page content..."
            FINAL_META=$(curl -s -c "$CAS_COOKIE_JAR" -b "$CAS_COOKIE_JAR" -L -o "$FINAL_APP_PAGE" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" "$CAS_POST_FINAL_URL")
            FINAL_URL=$(echo "$FINAL_META" | grep "FINAL_URL:" | cut -d: -f2-)
            FINAL_APP_CODE=$(echo "$FINAL_META" | grep "HTTP_CODE:" | cut -d: -f2)

            echo "🌐 Final app URL after CAS redirects: $FINAL_URL"
            echo "📄 Final app HTTP Code: $FINAL_APP_CODE"

            if [ "$FINAL_APP_CODE" = "200" ]; then
                echo "✅ Demo reachable after CAS login (HTTP 200)"
                echo "----- Final page content (begin) -----"
                cat "$FINAL_APP_PAGE"
                echo "\n----- Final page content (end) -----"

                # Verify that the expected authenticated email is present in the page
                if grep -q "leleuj@gmail.com" "$FINAL_APP_PAGE"; then
                    echo "✅ Email 'leleuj@gmail.com' found in final page content"
                    CAS_AUTH_PASSED=true
                else
                    echo "❌ Email 'leleuj@gmail.com' NOT found in final page content"
                    CAS_AUTH_PASSED=false
                fi
            else
                echo "❌ Demo not reachable after CAS login (HTTP $FINAL_APP_CODE)"
                CAS_AUTH_PASSED=false
            fi
        fi
        
    else
        echo "❌ CAS login page test failed!"
        echo "🚫 Expected CAS login page but got:"
        echo "   HTTP Code: $CAS_HTTP_CODE"
        echo "   Final URL: $CAS_FINAL_URL"
        CAS_AUTH_PASSED=false
    fi
else
    echo "❌ Initial test failed! HTTP code received: $HTTP_CODE"
    echo "📋 Server logs:"
    cat target/app.log || true
    CAS_AUTH_PASSED=false
fi

# Always stop the server
echo "🛑 Stopping server..."
kill $APP_PID 2>/dev/null || true

# Wait a moment for graceful shutdown
sleep 2

# Force kill if still running
kill -9 $APP_PID 2>/dev/null || true

# Clean up temporary files
rm -f target/cas_cookies.txt target/cas_*.html target/final_*.html target/*_fetch.meta 2>/dev/null || true

if ([ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "301" ] || [ "$HTTP_CODE" = "302" ]) && [ "$CAS_AUTH_PASSED" = "true" ]; then
    echo "🎉 ratpack-pac4j-demo test completed successfully!"
    echo "✅ All tests passed:"
    echo "   - Application responds with HTTP $HTTP_CODE"
    echo "   - CAS authentication works correctly"
    exit 0
else
    echo "💥 ratpack-pac4j-demo test failed!"
    if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "301" ] && [ "$HTTP_CODE" != "302" ]; then
        echo "❌ Application HTTP test failed (code: $HTTP_CODE)"
    fi
    if [ "$CAS_AUTH_PASSED" != "true" ]; then
        echo "❌ CAS authentication test failed"
    fi
    exit 1
fi
