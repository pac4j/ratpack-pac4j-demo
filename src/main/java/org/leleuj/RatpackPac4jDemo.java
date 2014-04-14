package org.leleuj;

import java.io.File;

import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {
    
    public static void main(final String[] args) throws Exception {
        final LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("root")).port(8080)
            .build(new MyHandlerFactory());
        final RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
