package org.leleuj;

import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;

public class MyHandlerFactory implements HandlerFactory {
    
    @Override
    public Handler create(final LaunchConfig launchConfig) throws Exception {
        return Guice.handler(launchConfig, new ModuleBootstrap(), new Action<Chain>() {
            @Override
            public void execute(final Chain chain) {
                chain.handler(new MyHandler());
            }
        });
    }
}
