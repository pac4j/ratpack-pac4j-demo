package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;

public class MyHandlerFactory implements HandlerFactory {

    @Override
    public Handler create(LaunchConfig launchConfig) throws Exception {
        return new Handler() {
            @Override
            public void handle(Context context) {
                context.render(groovyTemplate("index"));
            }
        };
    }
}
