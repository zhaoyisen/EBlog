package com.eblog.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class TagParserTest {

  @Test
  void normalizeLowercasesAndTrims() {
    assertEquals("java", TagParser.normalize("  JAVA "));
    assertEquals("spring boot", TagParser.normalize("spring   boot"));
    assertNull(TagParser.normalize("   "));
  }

  @Test
  void parseTagsDedupesAndNormalizes() {
    List<String> tags = TagParser.parseTags("Java, java,  Spring ,spring  boot, ,");
    assertEquals(List.of("java", "spring", "spring boot"), tags);
  }
}
