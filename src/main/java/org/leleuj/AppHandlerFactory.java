package org.leleuj;

import org.pac4j.core.client.Clients;
import org.pac4j.http.client.FormClient;
import ratpack.guice.Guice;
import ratpack.handling.ChainAction;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;

public class AppHandlerFactory implements HandlerFactory {

    private final Clients clients;
    private final AuthenticatedAuthorizer authenticatedAuthorizer;
    private final Handler protectedIndexHandler;

    public AppHandlerFactory(final Clients clients) {
        this.clients = clients;
        this.authenticatedAuthorizer = new AuthenticatedAuthorizer();
        this.protectedIndexHandler = Handlers.path("index.html", new ProtectedIndexHandler(this.clients));
    }

    @Override
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        return Guice.handler(launchConfig, new Bindings(), new ChainAction() {
            @Override
            protected void execute() throws Exception {
                handler("", new DefaultRedirectHandler());

                prefix("facebook", new AuthenticatedPageChain("FacebookClient"));
                prefix("twitter", new AuthenticatedPageChain("TwitterClient"));
                prefix("form", new AuthenticatedPageChain("FormClient"));
                prefix("basicauth", new AuthenticatedPageChain("BasicAuthClient"));
                prefix("cas", new AuthenticatedPageChain("CasClient"));
                prefix("saml2", new AuthenticatedPageChain("Saml2Client"));

                handler("theForm.html", new FormHandler(clients.findClient(FormClient.class)));
                handler("logout.html", new LogoutHandler());
                handler("index.html", new IndexHandler(clients));
                handler("callback", new Pac4jCallbackHandler(clients));
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
            handler(new Pac4jAuthenticationHandler(clients, clientName, authenticatedAuthorizer));
            handler(protectedIndexHandler);
        }
    }
}
