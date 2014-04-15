package org.leleuj;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public class DefaultRedirectHandler implements Handler {
    @Override
    public void handle(final Context context) {
        context.redirect("index.html");
    }
}
