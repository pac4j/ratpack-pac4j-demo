package org.pac4j.demo.ratpack;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.http.profile.UsernameProfileCreator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.pac4j.RatpackPac4j;
import ratpack.server.RatpackServer;
import ratpack.session.SessionModule;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static ratpack.groovy.Groovy.groovyTemplate;
import static ratpack.handling.Handlers.redirect;

public class RatpackPac4jDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatpackPac4jDemo.class);

    public static void main(final String[] args) throws Exception {


        RatpackServer.start(server -> server
                .serverConfig(c -> c.baseDir(new File("src/main").getAbsoluteFile()).port(8080))
                .registry(Guice.registry(b -> b
                        .bindInstance(ServerErrorHandler.class, (ctx, error) ->
                                ctx.render(groovyTemplate("error500.html"))
                        )
                        .bindInstance(ClientErrorHandler.class, (ctx, statusCode) -> {
                            ctx.getResponse().status(statusCode);
                            if (statusCode == 404) {
                                ctx.render(groovyTemplate("error404.html"));
                            } else if (statusCode == 401) {
                                ctx.render(groovyTemplate("error401.html"));
                            } else if (statusCode == 403) {
                                ctx.render(groovyTemplate("error403.html"));
                            } else {
                                LOGGER.error("Unexpected: {}", statusCode);
                            }
                        })
                        .module(TextTemplateModule.class)
                        .module(SessionModule.class)
                ))
                .handlers(chain -> {
                    final Saml2Client saml2Client = new Saml2Client();
                    saml2Client.setKeystorePath("resource:samlKeystore.jks");
                    saml2Client.setKeystorePassword("pac4j-demo-passwd");
                    saml2Client.setPrivateKeyPassword("pac4j-demo-passwd");
                    saml2Client.setIdpMetadataPath("resource:testshib-providers.xml");

                    final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
                    final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA", "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");

                    // HTTP
                    final FormClient formClient = new FormClient("/loginForm.html", new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());
                    final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());

                    // CAS
                    final CasClient casClient = new CasClient();
                    // casClient.setGateway(true);
                    casClient.setCasLoginUrl("http://localhost:8888/cas/login");

                    chain
                        .path(redirect(301, "index.html"))
                        .all(RatpackPac4j.authenticator(formClient, formClient, saml2Client, facebookClient, twitterClient, basicAuthClient, casClient))
                        .prefix("facebook", auth(FacebookClient.class))
                        .prefix("twitter", auth(TwitterClient.class))
                        .prefix("form", auth(FormClient.class))
                        .prefix("basicauth", auth(BasicAuthClient.class))
                        .prefix("cas", auth(CasClient.class))
                        .prefix("saml2", auth(Saml2Client.class))
                        .path("loginForm.html", ctx ->
                            ctx.render(groovyTemplate(
                                singletonMap("callbackUrl", formClient.getCallbackUrl()),
                                "loginForm.html"
                            ))
                        )
                        .path("logout.html", ctx ->
                                RatpackPac4j.logout(ctx).then(() -> ctx.redirect("index.html"))
                        )
                        .path("index.html", ctx -> {
                            LOGGER.debug("Retrieving user profile...");
                            RatpackPac4j.userProfile(ctx)
                                .left(RatpackPac4j.webContext(ctx))
                                .then(pair -> {
                                    final WebContext webContext = pair.left;
                                    final Optional<UserProfile> profile = pair.right;

                                    final Map<String, Object> model = Maps.newHashMap();
                                    profile.ifPresent(p -> model.put("profile", p));
                                    ctx.render(groovyTemplate(model, "index.html"));
                                });
                        });
                })
        );
    }

    private static Action<Chain> auth(Class<? extends Client<?, ?>> clientClass) {
        return chain -> chain
            .all(RatpackPac4j.requireAuth(clientClass))
            .path("index.html", ctx ->
                    ctx.render(groovyTemplate(
                        singletonMap("profile", ctx.get(UserProfile.class)),
                        "protectedIndex.html"
                    ))
            );
    }
}
