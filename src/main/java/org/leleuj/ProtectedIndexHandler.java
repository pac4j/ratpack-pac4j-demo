package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.core.client.Clients;

import ratpack.handling.Context;

public class ProtectedIndexHandler extends IndexHandler {
    
    public ProtectedIndexHandler(final Clients clients) {
        super(clients);
    }
    
    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", getProfile(context));
        context.render(groovyTemplate(model, "protectedIndex.html"));
    }
}
