package org.leleuj;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.http.profile.UsernameProfileCreator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.pac4j.RatpackPac4j;

public class AppHandlerFactory implements Action<Chain> {

    private final Handler protectedIndexHandler;

    public AppHandlerFactory() {
        this.protectedIndexHandler = new ProtectedIndexHandler();
    }

    public void execute(Chain chain) throws Exception {
        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath("resource:samlKeystore.jks");
        saml2Client.setKeystorePassword("pac4j-demo-passwd");
        saml2Client.setPrivateKeyPassword("pac4j-demo-passwd");
        saml2Client.setIdpMetadataPath("resource:testshib-providers.xml");

        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        // HTTP
        final FormClient formClient = new FormClient(RatpackPac4jDemo.URL + "/theForm.html", new SimpleTestUsernamePasswordAuthenticator(),
                new UsernameProfileCreator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());

        // CAS
        final CasClient casClient = new CasClient();
        // casClient.setGateway(true);
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");

        chain
            .all(RatpackPac4j.callback("callback", formClient, formClient, saml2Client, facebookClient, twitterClient, basicAuthClient, casClient))
            .path("", new DefaultRedirectHandler())
            .prefix("facebook", new AuthenticatedPageChain(FacebookClient.class))
            .prefix("twitter", new AuthenticatedPageChain(TwitterClient.class))
            .prefix("form", new AuthenticatedPageChain(FormClient.class))
            .prefix("basicauth", new AuthenticatedPageChain(BasicAuthClient.class))
            .prefix("cas", new AuthenticatedPageChain(CasClient.class))
            .prefix("saml2", new AuthenticatedPageChain(Saml2Client.class))
            .path("theForm.html", new FormHandler(formClient))
            .path("logout.html", new LogoutHandler())
            .path("index.html", new IndexHandler()
            //.path("callback", RatpackPac4j.callback(formClient, formClient, saml2Client, facebookClient, twitterClient, basicAuthClient, casClient)
        );
    }

    private class AuthenticatedPageChain implements Action<Chain> {

        private final Class<? extends Client<?, ?>> clientClass;

        public AuthenticatedPageChain(Class<? extends Client<?, ?>> clientClass) {
            this.clientClass = clientClass;
        }

        @Override
        public void execute(Chain chain) throws Exception {
            chain
                .all(RatpackPac4j.auth(clientClass))
                .path("index.html", protectedIndexHandler);
        }
    }
}
