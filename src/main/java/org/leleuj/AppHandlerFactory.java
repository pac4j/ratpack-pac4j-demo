package org.leleuj;

import org.pac4j.core.client.Clients;
import org.pac4j.http.client.FormClient;
import ratpack.guice.Guice;
import ratpack.handling.ChainAction;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;

public class AppHandlerFactory implements HandlerFactory {

    private final Clients clients;

    public AppHandlerFactory(final Clients clients) {
        this.clients = clients;
    }

    @Override
    public Handler create(final LaunchConfig launchConfig) throws Exception {

        final AuthenticatedAuthorizer authenticatedAuthorizer = new AuthenticatedAuthorizer();
        final ProtectedIndexHandler protectedIndexHandler = new ProtectedIndexHandler(clients);

        return Guice.handler(launchConfig, new Bindings(), new ChainAction() {
            @Override
            protected void execute() throws Exception {
                handler("", new DefaultRedirectHandler());
                prefix("facebook", new ChainAction() {
                    @Override
                    protected void execute() {
                        handler(new Pac4jAuthenticationHandler(clients, "FacebookClient", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                prefix("twitter", new ChainAction() {
                    @Override
                    protected void execute() throws Exception {
                        handler(new Pac4jAuthenticationHandler(clients, "TwitterClient", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                prefix("form", new ChainAction() {
                    @Override
                    protected void execute() throws Exception {
                        handler(new Pac4jAuthenticationHandler(clients, "FormClient", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                prefix("basicauth", new ChainAction() {
                    @Override
                    protected void execute() throws Exception {
                        handler(new Pac4jAuthenticationHandler(clients, "BasicAuthClient", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                prefix("cas", new ChainAction() {
                    @Override
                    protected void execute() throws Exception {
                        handler(new Pac4jAuthenticationHandler(clients, "CasClient", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                prefix("saml2", new ChainAction() {
                    @Override
                    protected void execute() throws Exception {
                        handler(new Pac4jAuthenticationHandler(clients, "Saml2Client", authenticatedAuthorizer));
                        handler("index.html", protectedIndexHandler);
                    }
                });
                handler("theForm.html", new FormHandler(clients.findClient(FormClient.class)));
                handler("logout.html", new LogoutHandler());
                handler("index.html", new IndexHandler(clients));
                handler("callback", new Pac4jCallbackHandler(clients));
            }
        });
    }
}
