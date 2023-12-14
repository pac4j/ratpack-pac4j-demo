package org.pac4j.demo.ratpack;

import com.google.inject.Inject;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Pattern;
import ratpack.file.FileSystemBinding;
import ratpack.handling.Context;
import ratpack.render.Renderer;

public class TemplateRenderer implements Renderer<Template> {

  private static Pattern REGEX = Pattern.compile("\\$\\{model.(\\w+)}");

  private final FileSystemBinding fileSystemBinding;

  @Inject
  TemplateRenderer(FileSystemBinding fileSystemBinding) {
    this.fileSystemBinding = fileSystemBinding;
  }

  @Override
  public Class<Template> getType() {
    return Template.class;
  }

  @Override
  public void render(Context context, Template template) throws Exception {
    var file = context.get(FileSystemBinding.class).file("templates/" + template.id());
    var text = Files.readString(file);
    var templated = REGEX.matcher(text)
        .replaceAll(m -> Optional.ofNullable(template.model().get(m.group(1))).map(Object::toString).orElse("null"));
    context.getResponse().contentType("text/html");
    context.getResponse().send(templated);
  }
}
