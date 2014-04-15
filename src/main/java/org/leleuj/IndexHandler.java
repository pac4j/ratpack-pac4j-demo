package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.core.profile.UserProfile;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.session.store.SessionStorage;

public class IndexHandler implements Handler {

    protected Object getProfile(final Context context) {
        final SessionStorage sessionStorage = context.getRequest().get(SessionStorage.class);
        UserProfile profile = (UserProfile) sessionStorage.get("ratpack.pac4j-user-profile");
        if (profile != null) {
            return profile;
        } else {
            return "";
        }
    }

    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", getProfile(context));
        context.render(groovyTemplate(model, "index.html"));
    }
}
