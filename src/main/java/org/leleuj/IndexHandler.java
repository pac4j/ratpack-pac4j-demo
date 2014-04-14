package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.core.profile.UserProfile;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public class IndexHandler implements Handler {
    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", context.getRequest().get(UserProfile.class));
        context.render(groovyTemplate(model, "index.html"));
    }
}
