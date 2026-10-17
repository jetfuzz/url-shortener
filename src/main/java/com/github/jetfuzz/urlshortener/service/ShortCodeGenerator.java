package com.github.jetfuzz.urlshortener.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class ShortCodeGenerator {

  private static final int CODE_LENGTH = 6;
  private static final String BASE62_ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private static final SecureRandom RANDOM = new SecureRandom();

  public String generateShortCode() {
    StringBuilder sb = new StringBuilder(CODE_LENGTH);
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(BASE62_ALPHABET.charAt(RANDOM.nextInt(BASE62_ALPHABET.length())));
    }
    return sb.toString();
  }
}
