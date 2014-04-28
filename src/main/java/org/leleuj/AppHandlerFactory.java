package org.leleuj;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;

import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;

public class AppHandlerFactory extends AllClients implements HandlerFactory {
    
    public AppHandlerFactory(final FacebookClient facebookClient, final TwitterClient twitterClient) {
        setFacebookClient(facebookClient);
        setTwitterClient(twitterClient);
    }
    
    @Override
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        
        final AuthenticatedAuthorizer authenticatedAuthorizer = new AuthenticatedAuthorizer();
        final AnonymousAuthorizer anonymousAuthorizer = new AnonymousAuthorizer();
        
        final ProtectedIndexHandler protectedIndexHandler = new ProtectedIndexHandler();
        
        return Guice.handler(launchConfig, new ModuleBootstrap(), new Action<Chain>() {
            @Override
            public void execute(final Chain chain) throws Exception {
                chain
                    .handler("", new DefaultRedirectHandler())
                    .prefix("facebook", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) {
                            chain.handler(new Pac4jAuthenticationHandler(getFacebookClient(), authenticatedAuthorizer,
                                                                         "callbackFB")).handler("index.html",
                                                                                                protectedIndexHandler);
                        }
                    })
                    .prefix("twitter", new Action<Chain>() {
                        @Override
                        public void execute(final Chain chain) throws Exception {
                            chain.handler(new Pac4jAuthenticationHandler(getTwitterClient(), authenticatedAuthorizer,
                                                                         "callbackTW")).handler("index.html",
                                                                                                protectedIndexHandler);
                        }
                    }).handler("logout.html", new LogoutHandler()).handler("index.html", new IndexHandler())
                    .handler("callbackFB", new Pac4jCallbackHandler(getFacebookClient(), anonymousAuthorizer))
                    .handler("callbackTW", new Pac4jCallbackHandler(getTwitterClient(), anonymousAuthorizer));
            }
        });
    }
}
