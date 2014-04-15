package org.leleuj;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.session.store.SessionStorage;

public class LogoutHandler implements Handler {

    @Override
    public void handle(final Context context) {
        final SessionStorage sessionStorage = context.getRequest().get(SessionStorage.class);
        sessionStorage.remove("ratpack.pac4j-user-profile");
        context.redirect("index.html");
    }
}
