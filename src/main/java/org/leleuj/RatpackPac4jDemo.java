package org.leleuj;

import java.io.File;

import org.pac4j.oauth.client.FacebookClient;

import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {

    public static void main(String[] args) throws Exception {
        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("root")).port(8080)
                .build(new MyHandlerFactory());
        RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
