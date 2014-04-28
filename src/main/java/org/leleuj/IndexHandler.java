package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.session.store.SessionStorage;

public class IndexHandler extends AllClients implements Handler {
    
    protected Object getProfile(final Context context) {
        final SessionStorage sessionStorage = context.getRequest().get(SessionStorage.class);
        final UserProfile profile = (UserProfile) sessionStorage.get("ratpack.pac4j-user-profile");
        if (profile != null) {
            return profile;
        } else {
            return "";
        }
        /*try {
            return context.getRequest().get(UserProfile.class);
        } catch (final NotInRegistryException e) {
            return "";
        }*/
    }
    
    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", getProfile(context));
        final WebContext webContext = new RatpackWebContext(context);
        model.put("facebookUrl", getFacebookClient().getRedirectionUrl(webContext, false));
        model.put("twitterUrl", getTwitterClient().getRedirectionUrl(webContext, false));
        context.render(groovyTemplate(model, "index.html"));
    }
}
