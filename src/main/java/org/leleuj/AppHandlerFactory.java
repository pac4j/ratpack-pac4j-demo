package org.leleuj;

import org.pac4j.cas.client.CasClient;
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
import ratpack.handling.Handlers;
import ratpack.pac4j.Pac4jCallbackHandlerBuilder;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jClientsHandler;

public class AppHandlerFactory implements Action<Chain> {

    private final AuthenticatedAuthorizer authenticatedAuthorizer;
    private final Handler protectedIndexHandler;

    public AppHandlerFactory() {
        this.authenticatedAuthorizer = new AuthenticatedAuthorizer();
        this.protectedIndexHandler = Handlers.path("index.html", new ProtectedIndexHandler());
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
            .handler(new Pac4jClientsHandler("callback", formClient, formClient, saml2Client, facebookClient, twitterClient,
                    basicAuthClient, casClient))
            .handler("", new DefaultRedirectHandler())

            .prefix("facebook", new AuthenticatedPageChain("FacebookClient"))
            .prefix("twitter", new AuthenticatedPageChain("TwitterClient"))
            .prefix("form", new AuthenticatedPageChain("FormClient"))
            .prefix("basicauth", new AuthenticatedPageChain("BasicAuthClient"))
            .prefix("cas", new AuthenticatedPageChain("CasClient"))
            .prefix("saml2", new AuthenticatedPageChain("Saml2Client"))

            .handler("theForm.html", new FormHandler(formClient))
            .handler("logout.html", new LogoutHandler())
            .handler("index.html", new IndexHandler())
            .handler("callback", new Pac4jCallbackHandlerBuilder().build()
        );
    }

    private class AuthenticatedPageChain implements Action<Chain> {

        private final String clientName;

        public AuthenticatedPageChain(String clientName) {
            this.clientName = clientName;
        }

        @Override
        public void execute(Chain chain) throws Exception {
            chain
                .handler(new Pac4jAuthenticationHandler(clientName, authenticatedAuthorizer))
                .handler(protectedIndexHandler);
        }
    }
}
