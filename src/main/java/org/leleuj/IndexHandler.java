package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Request;
import ratpack.pac4j.internal.Pac4jSessionKeys;
import ratpack.pac4j.internal.RatpackWebContext;
import ratpack.session.Session;
import ratpack.session.SessionData;

public class IndexHandler implements Handler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final static String PROFILE = "profile";

    @Override
    public void handle(final Context context) {
        logger.debug("Retrieving user profile...");
        context.get(Session.class).getData()
            .then(sessionData -> {
                Optional<UserProfile> userProfile = sessionData.get(Pac4jSessionKeys.USER_PROFILE_SESSION_KEY);
                if (userProfile.isPresent()) {
                    render(context, sessionData, userProfile.get());
                } else {
                    render(context, sessionData, "");
                }
            });
    }

    protected void render(final Context context, final SessionData sessionData, final Object profile) {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROFILE, profile);
        final Request request = context.getRequest();
        final Clients clients = request.get(Clients.class);
        final WebContext webContext = new RatpackWebContext(context, sessionData);

        final FacebookClient fbclient = (FacebookClient) clients.findClient(FacebookClient.class);
        final String fbUrl = fbclient.getRedirectionUrl(webContext);
        logger.debug("fbUrl: {}", fbUrl);
        model.put("facebookUrl", fbUrl);

        final TwitterClient twClient = (TwitterClient) clients.findClient(TwitterClient.class);
        final String twUrl = twClient.getRedirectionUrl(webContext);
        logger.debug("twUrl: {}", twUrl);
        model.put("twitterUrl", twUrl);

        final FormClient fmClient = (FormClient) clients.findClient(FormClient.class);
        final String fmUrl = fmClient.getRedirectionUrl(webContext);
        logger.debug("fmUrl: {}", fmUrl);
        model.put("formUrl", fmUrl);

        final BasicAuthClient baClient = (BasicAuthClient) clients.findClient(BasicAuthClient.class);
        final String baUrl = baClient.getRedirectionUrl(webContext);
        logger.debug("baUrl: {}", baUrl);
        model.put("baUrl", baUrl);

        final CasClient casClient = (CasClient) clients.findClient(CasClient.class);
        final String casUrl = casClient.getRedirectionUrl(webContext);
        logger.debug("casUrl: {}", casUrl);
        model.put("casUrl", casUrl);

        final Saml2Client samlClient = (Saml2Client) clients.findClient(Saml2Client.class);
        final String samlUrl = samlClient.getRedirectionUrl(webContext);
        logger.debug("samlUrl: {}", samlUrl);
        model.put("samlUrl", samlUrl);
    }
}
