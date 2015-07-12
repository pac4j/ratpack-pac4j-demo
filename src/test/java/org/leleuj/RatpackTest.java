package org.leleuj;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.After;
import org.junit.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.CloseableApplicationUnderTest;
import ratpack.test.MainClassApplicationUnderTest;
import ratpack.test.http.TestHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class RatpackTest {

    private final CloseableApplicationUnderTest aut = new MainClassApplicationUnderTest(RatpackPac4jDemo.class);
    private final TestHttpClient httpClient = aut.getHttpClient();

    @After
    public void tearDown() throws Exception {
        aut.close();
    }

    @Test
    public void redirectsToIndexHtml() {
        final ReceivedResponse response = httpClient.get();
        assertEquals(200, response.getStatusCode());
        assertThat(response.getBody().getText(), containsString("<h1>index</h1>"));
    }

    @Test
    public void requiresFormAuth() {
        ReceivedResponse response = httpClient.get("form/index.html");
        assertEquals(200, response.getStatusCode());
        assertThat(response.getBody().getText(), containsString("<form action=\"" + aut.getAddress() + "authenticator?client_name=FormClient\" method=\"POST\">"));

        response = httpClient
            .requestSpec(r -> r
                    .body(b -> b.text("username=foo&password=foo"))
                    .headers(h -> h
                            .set(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")
                            .set(HttpHeaderNames.REFERER, aut.getAddress())
                    )
            )
            .params(m -> m.put("client_name", "FormClient"))
            .post("authenticator");

        assertThat(response.getBody().getText(), containsString("attributes: {username=foo}"));
        assertEquals(200, response.getStatusCode());
    }
}
