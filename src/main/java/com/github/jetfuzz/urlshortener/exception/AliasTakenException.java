package com.github.jetfuzz.urlshortener.exception;

public class AliasTakenException extends RuntimeException {
  public AliasTakenException(String message) {
    super(message);
  }
}
