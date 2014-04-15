package org.leleuj;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;

import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.pac4j.internal.Pac4jAuthenticationHandler;
import ratpack.pac4j.internal.Pac4jCallbackHandler;

public class AppHandlerFactory implements HandlerFactory {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA", "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        final AuthenticateAllAuthorizer authenticateAllAuthorizer = new AuthenticateAllAuthorizer();
        final ForbiddenOnFailureAuthorizer forbiddenOnFailureAuthorizer = new ForbiddenOnFailureAuthorizer();
        final ProtectedIndexHandler protectedIndexHandler = new ProtectedIndexHandler();
        final NotFoundHandler notFoundHandler = new NotFoundHandler();
        return Guice.handler(launchConfig, new ModuleBootstrap(), new Action<Chain>() {
            @Override
            public void execute(final Chain chain) throws Exception {
                chain.handler("", new DefaultRedirectHandler())
                .prefix("facebook", new Action<Chain>() {
                    @Override
                    public void execute(final Chain chain) {
                        chain.handler(new Pac4jAuthenticationHandler(facebookClient, authenticateAllAuthorizer, "callbackFB"))
                        .handler("index.html", protectedIndexHandler);
                    }
                })
                .prefix("twitter", new Action<Chain>() {
                    @Override
                    public void execute(final Chain chain) {
                        chain.handler(new Pac4jAuthenticationHandler(twitterClient, authenticateAllAuthorizer, "callbackTW"))
                        .handler("index.html", protectedIndexHandler);
                    }
                })
                .handler("logout.html", new LogoutHandler())
                .handler("index.html", new IndexHandler())
                .handler("callbackFB", new Pac4jCallbackHandler(facebookClient, forbiddenOnFailureAuthorizer))
                .handler("callbackTW", new Pac4jCallbackHandler(twitterClient, forbiddenOnFailureAuthorizer))
                .register(ServerErrorHandler.class, new AppServerErrorHandler(), new Action<Chain>() {
                    @Override
                    public void execute(final Chain chain) throws Exception {
                        chain.handler(notFoundHandler);
                    }
                });
            }
        });
    }
}
