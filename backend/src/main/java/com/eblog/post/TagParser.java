package com.eblog.post;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TagParser {
  private TagParser() {}

  public static List<String> parseTags(String tagsCsv) {
    if (tagsCsv == null || tagsCsv.trim().isEmpty()) {
      return java.util.Collections.emptyList();
    }
    String[] parts = tagsCsv.split(",");
    Set<String> tags = new LinkedHashSet<String>();
    for (String raw : parts) {
      String t = normalize(raw);
      if (t != null) {
        tags.add(t);
      }
    }
    return new ArrayList<String>(tags);
  }

  public static String normalize(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.trim();
    if (t.isEmpty()) {
      return null;
    }
    // MVP: normalize to lowercase; future work can add synonyms and slugging.
    t = t.toLowerCase(Locale.ROOT);
    // Collapse inner whitespace.
    t = t.replaceAll("\\s+", " ");
    return t;
  }
}
