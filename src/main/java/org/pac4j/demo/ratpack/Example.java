package org.pac4j.demo.ratpack;

import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import ratpack.guice.Guice;
import ratpack.pac4j.RatpackPac4j;
import ratpack.session.SessionModule;
import ratpack.test.embed.EmbeddedApp;

import static org.junit.Assert.assertEquals;

public class Example {
    public static void main(String... args) throws Exception {
        EmbeddedApp.of(s -> s
            .registry(Guice.registry(b -> b.module(SessionModule.class)))
            .handlers(c -> c
                .all(RatpackPac4j.authenticator(new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())))
                .prefix("require-authz", a -> a
                    .all(RatpackPac4j.security(IndirectBasicAuthClient.class, (ctx, profile) -> { return "user".equals(profile.getId()); }))
                    .get(ctx -> ctx.render("Hello " + ctx.get(UserProfile.class).getId()))
                )
                .get("logout", ctx -> RatpackPac4j.logout(ctx).then(() -> ctx.redirect("/")))
                .get(ctx -> ctx.render("no auth required"))
            )
        ).test(httpClient -> {
            httpClient.requestSpec(r -> r.redirects(1));
            assertEquals("no auth required", httpClient.getText());
            assertEquals(401, httpClient.get("require-authz").getStatusCode());
            assertEquals(403, httpClient.requestSpec(r -> r.basicAuth("u", "u")).get("require-authz").getStatusCode());
            httpClient.get("logout");
            assertEquals("Hello user", httpClient.requestSpec(r -> r.basicAuth("user", "user")).getText("require-authz"));
        });
    }
}
