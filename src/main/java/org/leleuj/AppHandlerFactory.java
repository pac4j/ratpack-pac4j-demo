package org.leleuj;

import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.FormClient;

import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;

public class AppHandlerFactory implements HandlerFactory {
    
    private final Clients clients;
    
    public AppHandlerFactory(final Clients clients) {
        this.clients=  clients;
    }
    
    @Override
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        
        final AuthenticatedAuthorizer authenticatedAuthorizer = new AuthenticatedAuthorizer();
        final AnonymousAuthorizer anonymousAuthorizer = new AnonymousAuthorizer();
        
        final ProtectedIndexHandler protectedIndexHandler = new ProtectedIndexHandler(clients);
        
        return Guice.handler(launchConfig, new ModuleBootstrap(), new Action<Chain>() {
            @Override
            public void execute(final Chain chain) throws Exception {
                chain
                    .handler("", new DefaultRedirectHandler())
                    .prefix("facebook", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "FacebookClient", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .prefix("twitter", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "TwitterClient", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .prefix("form", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "FormClient", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .prefix("basicauth", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "BasicAuthClient", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .prefix("cas", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "CasClient", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .prefix("saml2", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler<UserProfile>(clients, "Saml2Client", authenticatedAuthorizer))
                                 .handler("index.html", protectedIndexHandler);
                        }
                    })
                    .handler("theForm.html", new FormHandler((FormClient) clients.findClient(FormClient.class)))
                    .handler("logout.html", new LogoutHandler())
                    .handler("index.html", new IndexHandler(clients))
                    .handler("callback", new Pac4jCallbackHandler<UserProfile>(clients, anonymousAuthorizer));
            }
        });
    }
}
