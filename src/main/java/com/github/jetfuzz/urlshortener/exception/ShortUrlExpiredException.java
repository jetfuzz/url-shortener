package com.github.jetfuzz.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException {
  public ShortUrlExpiredException(String message) {
    super(message);
  }
}
