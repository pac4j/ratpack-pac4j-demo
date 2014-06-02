package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import ratpack.handling.Context;

public class ProtectedIndexHandler extends IndexHandler {

    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", getProfile(context));
        context.render(groovyTemplate(model, "protectedIndex.html"));
    }
}
