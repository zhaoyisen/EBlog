package com.eblog.moderation;

import com.eblog.moderation.enums.ModerationStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleEngineTest {

  @Test
  void testApprovedWhenNoSensitiveWordsAndLowLinkCount() {
    RuleEngine engine = new RuleEngine();
    engine.init();

    RuleEngine.RuleResult result = engine.evaluate(
      "Test Title",
      "This is a test post with a single link: https://example.com"
    );

    assertEquals(ModerationStatus.APPROVED, result.getStatus());
  }

  @Test
  void testRejectedWhenContainsSensitiveWord() {
    RuleEngine engine = new RuleEngine();
    engine.init();

    RuleEngine.RuleResult result = engine.evaluate(
      "Spam Title",
      "Content about spam here"
    );

    assertEquals(ModerationStatus.REJECTED, result.getStatus());
    assertEquals("Content contains sensitive word: spam", result.getReason());
  }

  @Test
  void testNeedsReviewWhenTooManyLinks() {
    RuleEngine engine = new RuleEngine();
    engine.setMaxExternalLinks(5);

    String content = String.join("\n", "https://link1.com", "https://link2.com", "https://link3.com",
      "https://link4.com", "https://link5.com", "https://link6.com");

    RuleEngine.RuleResult result = engine.evaluate("Title with many links", content);

    assertEquals(ModerationStatus.NEEDS_REVIEW, result.getStatus());
    assertEquals("Too many external links: 6", result.getReason());
  }

  @Test
  void testApprovedWhenMultipleSensitiveWordsButNotInContent() {
    RuleEngine engine = new RuleEngine();
    engine.setSensitiveWordsConfig("spam,adult,gambling");
    engine.init();

    RuleEngine.RuleResult result = engine.evaluate(
      "Normal Title",
      "Normal content with no sensitive words"
    );

    assertEquals(ModerationStatus.APPROVED, result.getStatus());
  }
}
