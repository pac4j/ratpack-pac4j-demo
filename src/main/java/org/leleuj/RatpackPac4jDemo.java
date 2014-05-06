package org.leleuj;

import java.io.File;
import java.net.URI;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;

public class RatpackPac4jDemo {

    private final static String URL = "http://localhost:8080";

    public static void main(final String[] args) throws Exception {
        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath("resource:samlKeystore.jks");
        saml2Client.setKeystorePassword("pac4j-demo-passwd");
        saml2Client.setPrivateKeyPassword("pac4j-demo-passwd");
        saml2Client.setIdpMetadataPath("resource:testshib-providers.xml");

        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        // HTTP
        final FormClient formClient = new FormClient(URL + "/theForm.html", new SimpleTestUsernamePasswordAuthenticator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        // CAS
        final CasClient casClient = new CasClient();
        // casClient.setGateway(true);
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");

        final Clients clients = new Clients(URL + "/callback", saml2Client, facebookClient, twitterClient, formClient,
                basicAuthClient, casClient);
        final HandlerFactory appHandlerFactory = new AppHandlerFactory(clients);
        final LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("src/main")).port(8080)
                .publicAddress(new URI(URL)).build(appHandlerFactory);
        final RatpackServer server = RatpackServerBuilder.build(launchConfig);
        server.start();
    }
}
