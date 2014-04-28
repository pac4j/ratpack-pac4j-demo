package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.error.ClientErrorHandler;
import ratpack.handling.Context;

public class AppClientErrorHandler implements ClientErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AppClientErrorHandler.class);
    
    @Override
    public void error(final Context context, final int statusCode) throws Exception {
        if (statusCode == 404) {
            context.render(groovyTemplate("error404.html"));
        } else if (statusCode == 401) {
            context.render(groovyTemplate("error401.html"));
        } else if (statusCode == 403) {
            context.render(groovyTemplate("error403.html"));
        } else {
            logger.error("Unexpected: {}", statusCode);
        }
    }
}
