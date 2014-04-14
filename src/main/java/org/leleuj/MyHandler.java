package org.leleuj;

import org.pac4j.oauth.profile.facebook.FacebookProfile;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public class MyHandler implements Handler {
    @Override
    public void handle(final Context context) {
        context.render("Authenticated as " + context.getRequest().get(FacebookProfile.class).getDisplayName());
    }
}
