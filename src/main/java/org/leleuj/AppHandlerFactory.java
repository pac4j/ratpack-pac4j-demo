package org.leleuj;

import org.pac4j.oauth.client.FacebookClient;

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
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final AuthenticateAllAuthorizer authenticateAllAuthorizer = new AuthenticateAllAuthorizer();
        return Guice.handler(launchConfig, new ModuleBootstrap(), new Action<Chain>() {
            @Override
            public void execute(final Chain chain) throws Exception {
                chain
                    .prefix("facebook",
                            new Pac4jAuthenticationHandler(facebookClient, authenticateAllAuthorizer, "callback"))
                    .handler("index.html", new IndexHandler())
                    .handler("callback", new Pac4jCallbackHandler(facebookClient, authenticateAllAuthorizer))
                    .register(ServerErrorHandler.class, new AppServerErrorHandler(), (Handler) null);
            }
        });
    }
}
