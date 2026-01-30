package com.eblog.post;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRenderer {
  private final Parser parser;
  private final HtmlRenderer renderer;
  private final Safelist safelist;

  public MarkdownRenderer() {
    this.parser = Parser.builder().build();
    this.renderer = HtmlRenderer.builder().escapeHtml(false).build();
    this.safelist = Safelist.relaxed()
        .addProtocols("a", "href", "http", "https", "mailto")
        .addProtocols("img", "src", "http", "https")
        .addEnforcedAttribute("a", "rel", "nofollow ugc noopener noreferrer");
  }

  public String renderToHtml(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return "";
    }
    Node document = parser.parse(markdown);
    String html = renderer.render(document);
    return Jsoup.clean(html, safelist);
  }
}
