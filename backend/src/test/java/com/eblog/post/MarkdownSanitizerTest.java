package com.eblog.post;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MarkdownSanitizerTest {
  private final MarkdownRenderer renderer = new MarkdownRenderer();

  @Test
  void removesScriptTags() {
    String html = renderer.renderToHtml("Hello <script>alert(1)</script>");
    assertFalse(html.toLowerCase().contains("<script"));
  }

  @Test
  void stripsEventHandlersFromRawHtml() {
    String html = renderer.renderToHtml("<img src=\"x\" onerror=\"alert(1)\" />");
    assertFalse(html.toLowerCase().contains("onerror"));
  }

  @Test
  void blocksJavascriptLinks() {
    String html = renderer.renderToHtml("[x](javascript:alert(1))");
    assertFalse(html.toLowerCase().contains("javascript:"));
  }

  @Test
  void removesUnsafeTagsAndAttributes() {
    String html = renderer.renderToHtml("<iframe src=\"https://evil.example\"></iframe><div onload=\"alert(1)\">x</div><span style=\"color:red\">y</span>");
    String lower = html.toLowerCase();
    assertFalse(lower.contains("<iframe"));
    assertFalse(lower.contains("onload"));
    assertFalse(lower.contains("style="));
  }

  @Test
  void addsUgcRelPolicyToLinks() {
    String html = renderer.renderToHtml("[safe](https://example.com)");
    assertTrue(html.contains("rel=\"nofollow ugc noopener noreferrer\""));
  }
}
