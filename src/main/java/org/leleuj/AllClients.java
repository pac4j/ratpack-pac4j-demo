package org.leleuj;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;

public class AllClients {
    
    private FacebookClient facebookClient;
    private TwitterClient twitterClient;
    
    public FacebookClient getFacebookClient() {
        return this.facebookClient;
    }
    
    public void setFacebookClient(final FacebookClient facebookClient) {
        this.facebookClient = facebookClient;
    }
    
    public TwitterClient getTwitterClient() {
        return this.twitterClient;
    }
    
    public void setTwitterClient(final TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }
}
