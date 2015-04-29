package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;

public class AppServerErrorHandler implements ServerErrorHandler {
    
    @Override
    public void error(final Context context, final Throwable t) {
        context.render(groovyTemplate("error500.html"));
    }
}
