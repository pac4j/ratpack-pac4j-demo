package org.leleuj;

import org.pac4j.core.profile.UserProfile;

import ratpack.handling.Context;
import ratpack.pac4j.AbstractAuthorizer;

public class AnonymousAuthorizer extends AbstractAuthorizer<UserProfile> {

    @Override
    public boolean isAuthenticationRequired(final Context context) {
        return false;
    }
}
