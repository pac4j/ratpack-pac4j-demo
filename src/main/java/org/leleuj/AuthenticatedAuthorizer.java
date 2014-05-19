package org.leleuj;

import ratpack.handling.Context;
import ratpack.pac4j.AbstractAuthorizer;

public class AuthenticatedAuthorizer extends AbstractAuthorizer {
    
    @Override
    public boolean isAuthenticationRequired(final Context context) {
        return true;
    }
}
