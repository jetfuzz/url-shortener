package com.github.jetfuzz.urlshortener.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShortCodeGeneratorTest {

  private final ShortCodeGenerator generator = new ShortCodeGenerator();

  @Test
  void generateShortCode_returnsCorrectCodeFormat() {
    for (int i = 0; i < 100; i++) {
      String code = generator.generateShortCode();
      assertTrue(code.matches("^[a-zA-Z0-9]+$"));
      assertEquals(6, code.length());
    }
  }
}
