package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;
import ratpack.handling.Context;
import ratpack.handling.Handler;

public class NotFoundHandler implements Handler {
    @Override
    public void handle(final Context context) {
        context.render(groovyTemplate("error404.html"));
    }
}
