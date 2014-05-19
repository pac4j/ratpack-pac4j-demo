package org.leleuj;

import ratpack.handling.Context;
import ratpack.pac4j.internal.Pac4jProfileHandler;

public class LogoutHandler extends Pac4jProfileHandler {

    @Override
    public void handle(final Context context) {
        removeUserProfile(context);
        context.redirect("index.html");
    }
}
