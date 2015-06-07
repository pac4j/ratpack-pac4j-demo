package org.leleuj;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
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
import ratpack.groovy.Groovy;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.pac4j.RatpackPac4j;
import ratpack.pac4j.internal.Pac4jSessionKeys;
import ratpack.pac4j.internal.RatpackWebContext;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import ratpack.session.Session;
import ratpack.session.SessionModule;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RatpackPac4jDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatpackPac4jDemo.class);

    public static void main(final String[] args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(ServerConfig
                        .baseDir(new File("src/main"))
                        .port(8080)
                )
                .registry(Guice.registry(b -> b
                        .bind(ServerErrorHandler.class, AppServerErrorHandler.class)
                        .bind(ClientErrorHandler.class, AppClientErrorHandler.class)
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
                    final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                        "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
                    // HTTP
                    final FormClient formClient = new FormClient("/theForm.html", new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());
                    final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());

                    // CAS
                    final CasClient casClient = new CasClient();
                    // casClient.setGateway(true);
                    casClient.setCasLoginUrl("http://localhost:8888/cas/login");

                    chain
                        .all(RatpackPac4j.callback("callback", formClient, formClient, saml2Client, facebookClient, twitterClient, basicAuthClient, casClient))
                        .path(Handlers.redirect(301, "index.html"))
                        .all(auth("facebook", FacebookClient.class))
                        .all(auth("twitter", TwitterClient.class))
                        .all(auth("form", FormClient.class))
                        .all(auth("basicauth", BasicAuthClient.class))
                        .all(auth("cas", CasClient.class))
                        .all(auth("saml2", Saml2Client.class))
                        .path("theForm.html", context -> {
                            context.render(
                                Groovy.groovyTemplate(
                                    Collections.singletonMap("callbackUrl", formClient.getCallbackUrl()),
                                    "theForm.html"
                                )
                            );
                        })
                        .path("logout.html", context -> {
                            context.get(Session.class).getData().then(sessionData -> {
                                sessionData.remove(Pac4jSessionKeys.USER_PROFILE_SESSION_KEY);
                                context.redirect("index.html");
                            });
                        })
                        .path("index.html", context -> {
                            LOGGER.debug("Retrieving user profile...");
                            context.get(Session.class)
                                .getData()
                                .then(sessionData -> {
                                    Optional<UserProfile> profile = sessionData.get(Pac4jSessionKeys.USER_PROFILE_SESSION_KEY);
                                    final Map<String, Object> model = Maps.newHashMap();

                                    profile.ifPresent(p -> model.put("profile", p));

                                    final Clients clients = context.get(Clients.class);
                                    final WebContext webContext = new RatpackWebContext(context, sessionData);

                                    final FacebookClient fbclient = clients.findClient(FacebookClient.class);
                                    final String fbUrl = fbclient.getRedirectionUrl(webContext);
                                    LOGGER.debug("fbUrl: {}", fbUrl);
                                    model.put("facebookUrl", fbUrl);

                                    final TwitterClient twClient = clients.findClient(TwitterClient.class);
                                    final String twUrl = twClient.getRedirectionUrl(webContext);
                                    LOGGER.debug("twUrl: {}", twUrl);
                                    model.put("twitterUrl", twUrl);

                                    final FormClient fmClient = clients.findClient(FormClient.class);
                                    final String fmUrl = fmClient.getRedirectionUrl(webContext);
                                    LOGGER.debug("fmUrl: {}", fmUrl);
                                    model.put("formUrl", fmUrl);

                                    final BasicAuthClient baClient = clients.findClient(BasicAuthClient.class);
                                    final String baUrl = baClient.getRedirectionUrl(webContext);
                                    LOGGER.debug("baUrl: {}", baUrl);
                                    model.put("baUrl", baUrl);

                                    final CasClient casClient1 = clients.findClient(CasClient.class);
                                    final String casUrl = casClient1.getRedirectionUrl(webContext);
                                    LOGGER.debug("casUrl: {}", casUrl);
                                    model.put("casUrl", casUrl);

                                    final Saml2Client samlClient = clients.findClient(Saml2Client.class);
                                    final String samlUrl = samlClient.getRedirectionUrl(webContext);
                                    LOGGER.debug("samlUrl: {}", samlUrl);

                                    context.render(Groovy.groovyTemplate(model, "index.html"));
                                });
                        });
                })
        );
    }

    private static Handler auth(String prefix, Class<? extends Client<?, ?>> clientClass) {
        return Handlers.prefix(prefix, Handlers.chain(
            RatpackPac4j.auth(clientClass),
            Handlers.path("index.html", ctx ->
                    ctx.render(Groovy.groovyTemplate(
                        Collections.singletonMap("profile", ctx.get(UserProfile.class)),
                        "protectedIndex.html"
                    ))
            )
        ));
    }
}
