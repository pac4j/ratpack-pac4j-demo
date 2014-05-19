package org.leleuj;

import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.groovy.templating.TemplatingModule;
import ratpack.guice.BindingsSpec;
import ratpack.session.SessionModule;
import ratpack.session.store.MapSessionsModule;

public class Bindings implements Action<BindingsSpec> {
    
    @Override
    public void execute(BindingsSpec bindings) throws Exception {
        bindings.add(new SessionModule(), new MapSessionsModule(10, 5), new TemplatingModule());
        bindings.bind(ServerErrorHandler.class, new AppServerErrorHandler());
        bindings.bind(ClientErrorHandler.class, new AppClientErrorHandler());
    }
}
