package org.leleuj;

import org.pac4j.oauth.client.FacebookClient;

import ratpack.func.Action;
import ratpack.guice.ModuleRegistry;
import ratpack.pac4j.Pac4jModule;
import ratpack.session.SessionModule;
import ratpack.session.store.MapSessionsModule;

public class ModuleBootstrap implements Action<ModuleRegistry> {
    
    @Override
    public void execute(final ModuleRegistry modules) throws Exception {
        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        modules.register(new SessionModule());
        modules.register(new MapSessionsModule(10, 5));
        modules.register(new Pac4jModule(facebookClient, new AuthenticateAllAuthorizer()));
    }
}
