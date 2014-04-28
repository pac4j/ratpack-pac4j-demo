package org.leleuj;

import java.io.File;
import java.net.URI;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;

import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {
    
    public static void main(final String[] args) throws Exception {
        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                                                              "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        
        final HandlerFactory appHandlerFactory = new AppHandlerFactory(facebookClient, twitterClient);
        final LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("src/main")).port(8080)
            .publicAddress(new URI("http://localhost:8080")).build(appHandlerFactory);
        final RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
