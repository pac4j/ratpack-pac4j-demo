package org.leleuj;

import java.io.File;
import java.net.URI;

import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {

    private final static String URL = "http://localhost:8080";

    public static void main(final String[] args) throws Exception {

        final HandlerFactory appHandlerFactory = new AppHandlerFactory();
        final LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("src/main")).port(8080)
                .publicAddress(new URI(URL)).build(appHandlerFactory);
        final RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
