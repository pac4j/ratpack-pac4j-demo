package org.leleuj;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.pac4j.internal.Pac4jSessionKeys;
import ratpack.session.Session;

public class LogoutHandler implements Handler {

    @Override
    public void handle(final Context context) {
        context.get(Session.class).getData().then(sessionData -> {
            sessionData.remove(Pac4jSessionKeys.USER_PROFILE_SESSION_KEY);
            context.redirect("index.html");
        });
    }
}
