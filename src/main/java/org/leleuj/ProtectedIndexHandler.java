package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import ratpack.handling.Context;

public class ProtectedIndexHandler extends IndexHandler {

    protected void render(final Context context, final Object profile) {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROFILE, profile);
        context.render(groovyTemplate(model, "protectedIndex.html"));
    }
}
