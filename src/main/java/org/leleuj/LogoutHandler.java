package org.leleuj;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.pac4j.internal.SessionConstants;
import ratpack.session.store.SessionStorage;

public class LogoutHandler implements Handler {

    @Override
    public void handle(final Context context) {
        final SessionStorage sessionStorage = context.getRequest().get(SessionStorage.class);
        sessionStorage.remove(SessionConstants.USER_PROFILE);
        context.redirect("index.html");
    }
}
