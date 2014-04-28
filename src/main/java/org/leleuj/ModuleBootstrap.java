package org.leleuj;

import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.groovy.templating.TemplatingModule;
import ratpack.guice.ModuleRegistry;
import ratpack.session.SessionModule;
import ratpack.session.store.MapSessionsModule;

public class ModuleBootstrap implements Action<ModuleRegistry> {
    
    @Override
    public void execute(final ModuleRegistry modules) throws Exception {
        modules.register(new SessionModule());
        modules.register(new MapSessionsModule(10, 5));
        modules.register(new TemplatingModule());
        modules.bind(ServerErrorHandler.class, new AppServerErrorHandler());
        modules.bind(ClientErrorHandler.class, new AppClientErrorHandler());
    }
}
