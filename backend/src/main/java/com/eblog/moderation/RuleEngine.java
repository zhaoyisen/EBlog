package com.eblog.moderation;

import com.eblog.moderation.enums.ModerationStatus;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RuleEngine {

  @Value("${MODERATION_SENSITIVE_WORDS:spam,adult,gambling,xxx,porn}")
  private String sensitiveWordsConfig = "spam,adult,gambling,xxx,porn";

  @Value("${MODERATION_MAX_EXTERNAL_LINKS:10}")
  private int maxExternalLinks = 10;

  private Set<String> sensitiveWords = new HashSet<>();
  private Pattern httpLinkPattern = Pattern.compile("https?://[\\w\\.-]+", Pattern.CASE_INSENSITIVE);

  public void init() {
    sensitiveWords.clear();
    if (sensitiveWordsConfig != null && !sensitiveWordsConfig.trim().isEmpty()) {
      String[] words = sensitiveWordsConfig.split(",");
      for (String word : words) {
        String w = word.trim().toLowerCase();
        if (!w.isEmpty()) {
          sensitiveWords.add(w);
        }
      }
    }
  }

  public void setMaxExternalLinks(int maxExternalLinks) {
    this.maxExternalLinks = maxExternalLinks;
  }

  public void setSensitiveWordsConfig(String sensitiveWordsConfig) {
    this.sensitiveWordsConfig = sensitiveWordsConfig;
  }

  public RuleResult evaluate(String title, String content) {
    ModerationStatus status = ModerationStatus.APPROVED;
    String reason = null;
    String ruleHit = null;

    String titleLower = title != null ? title.toLowerCase() : "";
    String contentLower = content != null ? content.toLowerCase() : "";
    String fullText = titleLower + " " + contentLower;

    for (String word : sensitiveWords) {
      if (fullText.contains(word)) {
        status = ModerationStatus.REJECTED;
        reason = "Content contains sensitive word: " + word;
        ruleHit = "SENSITIVE_WORD_" + word.toUpperCase();
        return new RuleResult(status, reason, ruleHit);
      }
    }

    Matcher titleMatcher = httpLinkPattern.matcher(title != null ? title : "");
    Matcher contentMatcher = httpLinkPattern.matcher(content != null ? content : "");
    int titleLinks = 0;
    int contentLinks = 0;
    while (titleMatcher.find()) {
      titleLinks++;
    }
    while (contentMatcher.find()) {
      contentLinks++;
    }
    int totalLinks = titleLinks + contentLinks;
    if (totalLinks > maxExternalLinks) {
      status = ModerationStatus.NEEDS_REVIEW;
      reason = "Too many external links: " + totalLinks;
      ruleHit = "LINK_COUNT_THRESHOLD";
    }

    return new RuleResult(status, reason, ruleHit);
  }

  public static class RuleResult {
    private final ModerationStatus status;
    private final String reason;
    private final String ruleHit;

    public RuleResult(ModerationStatus status, String reason, String ruleHit) {
      this.status = status;
      this.reason = reason;
      this.ruleHit = ruleHit;
    }

    public ModerationStatus getStatus() {
      return status;
    }

    public String getReason() {
      return reason;
    }

    public String getRuleHit() {
      return ruleHit;
    }
  }
}
