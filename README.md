## What is this project ? [![Build Status](https://travis-ci.org/pac4j/ratpack-pac4j-demo.png?branch=master)](https://travis-ci.org/pac4j/ratpack-pac4j-demo)

This **ratpack-pac4j-demo** project is a Java web application to test the [ratpack-pac4j library](https://github.com/ratpack/ratpack/tree/master/ratpack-pac4j) with Facebook, Twitter, form authentication, basic auth, CAS...  
The **ratpack-pac4j** library is built to delegate authentication to a provider and be authenticated back in the protected application with a complete user profile retrieved from the provider.

## Quick start & test

To start quickly, build the project and launch the web app with jetty :

    cd ratpack-pac4j-demo
    mvn exec:java

To test, you can call a protected url by clicking on the "Protected by **xxx** : **xxx**/index.jsp" url, which will start the authentication process with the **xxx** provider.  
Or you can click on the "Authenticate with **xxx**" link, to start manually the authentication process with the **xxx** provider.
