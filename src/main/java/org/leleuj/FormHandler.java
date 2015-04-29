package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.http.client.FormClient;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public class FormHandler implements Handler {

    private final FormClient formClient;
    
    public FormHandler(final FormClient formClient) {
        this.formClient = formClient;
    }
    
    @Override
    public void handle(final Context context) {
        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", formClient.getCallbackUrl());
        context.render(groovyTemplate(model, "theForm.html"));
    }
}
