package org.pac4j.demo.ratpack;

import static java.util.Collections.singletonMap;
import static ratpack.handling.Handlers.redirect;

import java.util.HashMap;
import java.util.Collections;
import com.google.inject.AbstractModule;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.pac4j.RatpackPac4j;
import ratpack.render.Renderer;
import ratpack.server.RatpackServer;
import ratpack.session.SessionModule;
import ratpack.session.SessionTypeFilter;
import static ratpack.handling.Handlers.redirect;

public class RatpackPac4jDemo {

  private static final Logger LOGGER = LoggerFactory.getLogger(RatpackPac4jDemo.class);

  private static final String JWT_SALT = "12345678901234567890123456789012";


  private static Template template(String name, Map<String, ?> model) {
    return new Template(name, model);
  }

  private static Template template(String name) {
    return new Template(name, Map.of());
  }

  public static void main(final String[] args) throws Exception {
    RatpackServer.start(server -> server
        .serverConfig(c -> c.baseDir(new File("src/main").getAbsoluteFile()).port(8080))
        .registry(Guice.registry(b -> b
            .bindInstance(ServerErrorHandler.class, (ctx, error) -> {
                  LOGGER.error("Unexpected error", error);
                  ctx.render(template("error500.html"));
                }
            )
            .multiBind(Renderer.class, TemplateRenderer.class)
            .bindInstance(ClientErrorHandler.class, (ctx, statusCode) -> {
              ctx.getResponse().status(statusCode);
              if (statusCode == 404) {
                ctx.render(template("error404.html"));
              } else if (statusCode == 401) {
                ctx.render(template("error401.html"));
              } else if (statusCode == 403) {
                ctx.render(template("error403.html"));
              } else {
                LOGGER.error("Unexpected: {}", statusCode);
              }
            })
            .module(SessionModule.class)
            .module(new AbstractModule() {
              @Override
              protected void configure() {
                binder().bind(SessionTypeFilter.class).toInstance(SessionTypeFilter.unsafeAllowAll());
              }
            })
        ))
        .handlers(chain -> {
          final OidcConfiguration oidcConfig = new OidcConfiguration();
          oidcConfig.setClientId("343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com");
          oidcConfig.setSecret("uR3D8ej1kIRPbqAFaxIE3HWh");
          oidcConfig.setUseNonce(true);
          oidcConfig.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
          //oidcConfig.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
          oidcConfig.addCustomParam("prompt", "consent");
          final OidcClient oidcClient = new OidcClient(oidcConfig);
          oidcClient.setAuthorizationGenerator((ctx, sessionStore, profile) -> {
            profile.addRole("ROLE_ADMIN");
            return Optional.of(profile);
          });

          final SAML2Configuration cfg = new SAML2Configuration("resource:samlKeystore.jks",
              "pac4j-demo-passwd",
              "pac4j-demo-passwd",
              "resource:metadata-okta.xml");
          cfg.setMaximumAuthenticationLifetime(3600);
          cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
          cfg.setServiceProviderMetadataPath("sp-metadata-ratpack.xml");
          final SAML2Client saml2Client = new SAML2Client(cfg);

          final FacebookClient facebookClient = new FacebookClient("145278422258960",
              "be21409ba8f39b5dae2a7de525484da8");
          final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
              "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");

          // HTTP
          final FormClient formClient = new FormClient("/loginForm.html",
              new SimpleTestUsernamePasswordAuthenticator());
          final IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient(
              new SimpleTestUsernamePasswordAuthenticator());

          // CAS
          final CasClient casClient = new CasClient(new CasConfiguration("https://casserverpac4j.herokuapp.com/login"));

          // direct clients
          final SignatureConfiguration signatureConfiguration = new SecretSignatureConfiguration(JWT_SALT);
          final EncryptionConfiguration encryptionConfiguration = new SecretEncryptionConfiguration(JWT_SALT);
          final JwtAuthenticator jwtAuthenticator = new JwtAuthenticator(Arrays.asList(signatureConfiguration),
              Arrays.asList(encryptionConfiguration));
          final ParameterClient parameterClient = new ParameterClient("token", jwtAuthenticator);
          parameterClient.setSupportGetRequest(true);
          parameterClient.setSupportPostRequest(false);

          // basic auth
          final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(
              new SimpleTestUsernamePasswordAuthenticator());

          chain
              .path(redirect(301, "index.html"))
              .all(RatpackPac4j.authenticator("callback", formClient, saml2Client, facebookClient, twitterClient,
                  basicAuthClient, casClient, oidcClient, parameterClient, directBasicAuthClient))
              .prefix("facebook", auth(FacebookClient.class))
              .prefix("facebookadmin",
                  auth(FacebookClient.class, new RequireAnyRoleAuthorizer("ROLE_ADMIN")))
              .prefix("facebookcustom", auth(FacebookClient.class, new Authorizer() {
                @Override
                public boolean isAuthorized(WebContext context, SessionStore sessionStore,
                    List<UserProfile> profiles) {
                  if (profiles == null || profiles.size() == 0) {
                    return false;
                  }
                  return StringUtils.startsWith(profiles.get(0).getId(), "jle");
                }
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
                    final Map<String, Object> model = new HashMap<>();
                    RatpackPac4j.userProfile(ctx)
                        .route(Optional::isPresent, p -> {
                          final JwtGenerator generator = new JwtGenerator(signatureConfiguration, encryptionConfiguration);
                          final String token = generator.generate(p.get());
                          model.put("token", token);
                          ctx.render(template("jwt.html", model));
                        })
                        .then(p -> {
                          ctx.render(template("jwt.html", model));
                        });
                  }
              )
              .path("loginForm.html", ctx ->
                  ctx.render(template(
                      "loginForm.html",
                      Collections.singletonMap("callbackUrl", formClient.getCallbackUrl() + "?client_name=FormClient")
                  ))
              )
              .path("logout.html", ctx ->
                  RatpackPac4j.logout(ctx).then(() -> ctx.redirect("index.html"))
              )
              .path("index.html", ctx -> {
                LOGGER.debug("Retrieving user profile...");
                final Map<String, Object> model = new HashMap<>();
                RatpackPac4j.userProfile(ctx)
                    .route(Optional::isPresent, p -> {
                      model.put("profile", p);
                      ctx.render(template("index.html", model));
                    })
                    .then(p -> {
                      ctx.render(template("index.html", model));
                    });
              });
        })
    );
  }

  private static <T extends org.pac4j.core.client.Client> Action<Chain> auth(Class<T> clientClass) {
    return chain -> chain
        .all(RatpackPac4j.requireAuth(clientClass))
        .path("index.html", ctx ->
            ctx.render(template(
                "protectedIndex.html",
                Collections.singletonMap("profile", ctx.get(UserProfile.class))
            ))
        );
  }

  private static <T extends org.pac4j.core.client.Client> Action<Chain> auth(Class<T> clientClass, Authorizer... authorizers) {
    return chain -> chain
        .all(RatpackPac4j.requireAuth(clientClass, authorizers))
        .path("index.html", ctx ->
            ctx.render(template(
                "protectedIndex.html",
                Collections.singletonMap("profile", ctx.get(UserProfile.class))
            ))
        );
  }
}
