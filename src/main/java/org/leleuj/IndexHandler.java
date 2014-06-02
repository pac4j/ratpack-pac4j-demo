package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

import ratpack.handling.Context;
import ratpack.http.Request;
import ratpack.pac4j.internal.Pac4jProfileHandler;
import ratpack.pac4j.internal.RatpackWebContext;

public class IndexHandler extends Pac4jProfileHandler {
    
    protected Object getProfile(final Context context) {
        final UserProfile profile = getUserProfile(context);
        if (profile != null) {
            return profile;
        } else {
            return "";
        }
    }
    
    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("profile", getProfile(context));
        final Request request = context.getRequest();
        final Clients clients = request.get(Clients.class);
        final WebContext webContext = new RatpackWebContext(context);        
        model.put("facebookUrl", ((FacebookClient) clients.findClient(FacebookClient.class)).getRedirectionUrl(webContext));
        model.put("twitterUrl", ((TwitterClient) clients.findClient(TwitterClient.class)).getRedirectionUrl(webContext));
        model.put("formUrl", ((FormClient) clients.findClient(FormClient.class)).getRedirectionUrl(webContext));
        model.put("baUrl", ((BasicAuthClient) clients.findClient(BasicAuthClient.class)).getRedirectionUrl(webContext));
        model.put("casUrl", ((CasClient) clients.findClient(CasClient.class)).getRedirectionUrl(webContext));
        model.put("samlUrl", ((Saml2Client) clients.findClient(Saml2Client.class)).getRedirectionUrl(webContext));
        context.render(groovyTemplate(model, "index.html"));
    }
}
