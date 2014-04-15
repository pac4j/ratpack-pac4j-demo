package org.leleuj;

import java.io.File;
import java.net.URI;

import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {

    public static void main(final String[] args) throws Exception {
        final LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("src/main")).port(8080)
                .publicAddress(new URI("http://localhost:8080")).build(new AppHandlerFactory());
        final RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
