package org.leleuj;

import org.pac4j.cas.client.CasClient;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

import ratpack.guice.Guice;
import ratpack.handling.ChainAction;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;
import ratpack.pac4j.internal.Pac4jClientsHandler;

public class AppHandlerFactory implements HandlerFactory {

    private final AuthenticatedAuthorizer authenticatedAuthorizer;
    private final Handler protectedIndexHandler;

    public AppHandlerFactory() {
        this.authenticatedAuthorizer = new AuthenticatedAuthorizer();
        this.protectedIndexHandler = Handlers.path("index.html", new ProtectedIndexHandler());
    }

    @Override
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        return Guice.handler(launchConfig, new Bindings(), new ChainAction() {
            @Override
            protected void execute() throws Exception {
                final Saml2Client saml2Client = new Saml2Client();
                saml2Client.setKeystorePath("resource:samlKeystore.jks");
                saml2Client.setKeystorePassword("pac4j-demo-passwd");
                saml2Client.setPrivateKeyPassword("pac4j-demo-passwd");
                saml2Client.setIdpMetadataPath("resource:testshib-providers.xml");

                final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
                final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                        "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
                // HTTP
                final FormClient formClient = new FormClient(launchConfig.getPublicAddress().toString() + "/theForm.html", new SimpleTestUsernamePasswordAuthenticator());
                final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

                // CAS
                final CasClient casClient = new CasClient();
                // casClient.setGateway(true);
                casClient.setCasLoginUrl("https://freeuse1.casinthecloud.com/leleujgithub/login");
                
                handler(new Pac4jClientsHandler("callback", formClient, formClient, saml2Client, facebookClient, twitterClient,
                                                basicAuthClient, casClient));
                handler("", new DefaultRedirectHandler());

                prefix("facebook", new AuthenticatedPageChain("FacebookClient"));
                prefix("twitter", new AuthenticatedPageChain("TwitterClient"));
                prefix("form", new AuthenticatedPageChain("FormClient"));
                prefix("basicauth", new AuthenticatedPageChain("BasicAuthClient"));
                prefix("cas", new AuthenticatedPageChain("CasClient"));
                prefix("saml2", new AuthenticatedPageChain("Saml2Client"));

                handler("theForm.html", new FormHandler(formClient));
                handler("logout.html", new LogoutHandler());
                handler("index.html", new IndexHandler());
                handler("callback", new Pac4jCallbackHandler());
            }
        });
    }

    private class AuthenticatedPageChain extends ChainAction {

        private final String clientName;

        public AuthenticatedPageChain(String clientName) {
            this.clientName = clientName;
        }

        @Override
        protected void execute() {
            handler(new Pac4jAuthenticationHandler(clientName, authenticatedAuthorizer));
            handler(protectedIndexHandler);
        }
    }
}
