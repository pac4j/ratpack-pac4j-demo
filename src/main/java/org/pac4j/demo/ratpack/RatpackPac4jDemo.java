package org.pac4j.demo.ratpack;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
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

    private static final String JWT_SALT = "12345678901234567890123456789012";

    public static void main(final String[] args) throws Exception {


        RatpackServer.start(server -> server
                .serverConfig(c -> c.baseDir(new File("src/main").getAbsoluteFile()).port(8080))
                .registry(Guice.registry(b -> b
                        .bindInstance(ServerErrorHandler.class, (ctx, error) -> {
                                LOGGER.error("Unexpected error", error);
                                ctx.render(groovyTemplate("error500.html"));
                            }
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
                    final OidcClient oidcClient = new OidcClient();
                    oidcClient.setClientID("343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com");
                    oidcClient.setSecret("uR3D8ej1kIRPbqAFaxIE3HWh");
                    oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
                    oidcClient.setUseNonce(true);
                    //oidcClient.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
                    oidcClient.addCustomParam("prompt", "consent");

                    final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                        "pac4j-demo-passwd",
                        "pac4j-demo-passwd",
                        "resource:testshib-providers.xml");
                    cfg.setMaximumAuthenticationLifetime(3600);
                    cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
                    cfg.setServiceProviderMetadataPath("sp-metadata.xml");
                    final SAML2Client saml2Client = new SAML2Client(cfg);

                    final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
                    final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA", "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");

                    // HTTP
                    final FormClient formClient = new FormClient("/loginForm.html", new SimpleTestUsernamePasswordAuthenticator());
                    final IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

                    // CAS
                    final CasClient casClient = new CasClient("https://casserverpac4j.herokuapp.com/login");

                    // direct clients
                    final ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(JWT_SALT));
                    parameterClient.setSupportGetRequest(true);
                    parameterClient.setSupportPostRequest(false);

                    // basic auth
                    final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

                    chain
                        .path(redirect(301, "index.html"))
                        .all(RatpackPac4j.authenticator("callback", formClient, saml2Client, facebookClient, twitterClient, basicAuthClient, casClient, oidcClient, parameterClient, directBasicAuthClient))
                        .prefix("facebook", auth(FacebookClient.class))
                        .prefix("facebookadmin", auth(FacebookClient.class, new RequireAnyRoleAuthorizer<UserProfile>("ROLE_ADMIN")))
                        .prefix("facebookcustom", auth(FacebookClient.class, (ctx, profile) -> {
                            if (profile == null) {
                                return false;
                            }
                            return StringUtils.startsWith(profile.getId(), "jle");
                        }))
                        .prefix("twitter", auth(TwitterClient.class))
                        .prefix("form", auth(FormClient.class))
                        .prefix("basicauth", auth(IndirectBasicAuthClient.class))
                        .prefix("cas", auth(CasClient.class))
                        .prefix("saml2", auth(SAML2Client.class))
                        .prefix("oidc", auth(OidcClient.class))
                        .prefix("dba", auth(DirectBasicAuthClient.class))
                        .prefix("rest-jwt", auth(ParameterClient.class))
                        .path("jwt.html", ctx -> {
                            final Map<String, Object> model = Maps.newHashMap();
                            RatpackPac4j.userProfile(ctx)
                                .route(Optional::isPresent, p -> {
                                    final JwtGenerator generator = new JwtGenerator(JWT_SALT);
                                    final String token = generator.generate(p.get());
                                    model.put("token", token);
                                    ctx.render(groovyTemplate(model, "jwt.html"));
                                })
                                .then(p -> {
                                    ctx.render(groovyTemplate(model, "jwt.html"));
                                });
                            }
                        )
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
                            final Map<String, Object> model = Maps.newHashMap();
                            RatpackPac4j.userProfile(ctx)
                                .route(Optional::isPresent, p -> {
                                    model.put("profile", p);
                                    ctx.render(groovyTemplate(model, "index.html"));
                                })
                                .then(p -> {
                                    ctx.render(groovyTemplate(model, "index.html"));
                                });
                        });
                })
        );
    }

    private static <C extends Credentials, U extends UserProfile> Action<Chain> auth(Class<? extends Client<C, U>> clientClass) {
        return chain -> chain
            .all(RatpackPac4j.requireAuth(clientClass))
            .path("index.html", ctx ->
                ctx.render(groovyTemplate(
                    singletonMap("profile", ctx.get(UserProfile.class)),
                    "protectedIndex.html"
                ))
            );
    }

    private static <C extends Credentials, U extends UserProfile> Action<Chain> auth(Class<? extends Client<C, U>> clientClass, Authorizer<? super U>... authorizers) {
        return chain -> chain
            .all(RatpackPac4j.requireAuth(clientClass, authorizers))
            .path("index.html", ctx ->
                ctx.render(groovyTemplate(
                    singletonMap("profile", ctx.get(UserProfile.class)),
                    "protectedIndex.html"
                ))
            );
    }
}
