package com.github.jetfuzz.urlshortener.service;

import com.github.jetfuzz.urlshortener.dto.CreateShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.UpdateShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.dto.ShortUrlStatsResponse;
import com.github.jetfuzz.urlshortener.entity.ShortUrl;
import com.github.jetfuzz.urlshortener.exception.AliasTakenException;
import com.github.jetfuzz.urlshortener.exception.ShortUrlExpiredException;
import com.github.jetfuzz.urlshortener.exception.ShortUrlNotFoundException;
import com.github.jetfuzz.urlshortener.repository.ShortUrlRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShortUrlService {

  private static final int MAX_ATTEMPTS = 5;
  private final ShortUrlRepository repository;
  private final ShortCodeGenerator generator;

  public ShortUrlService(ShortUrlRepository repository, ShortCodeGenerator generator) {
    this.repository = repository;
    this.generator = generator;
  }

  public ShortUrlResponse create(CreateShortUrlRequest request) {

    if (request.customAlias() != null) {
      String code = request.customAlias();

      try {
        return toResponse(
            repository.saveAndFlush(new ShortUrl(request.url(), code, request.expiresAt())));
      } catch (DataIntegrityViolationException _) {
        throw new AliasTakenException("Alias already taken");
      }
    }

    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      try {
        return toResponse(
            repository.saveAndFlush(
                new ShortUrl(request.url(), generator.generateShortCode(), request.expiresAt())));
      } catch (DataIntegrityViolationException _) {
      }
    }
    throw new IllegalStateException("Could not generate a unique code");
  }

  public ShortUrlResponse get(String code) {
    ShortUrl url = findCodeOrThrow(code);
    return toResponse(url);
  }

  public String redirect(String code) {
    ShortUrl url = findCodeOrThrow(code);
    if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
      throw new ShortUrlExpiredException("link has expired");
    }
    repository.incrementAccessCount(code);
    return url.getUrl();
  }

  public ShortUrlStatsResponse stats(String code) {
    ShortUrl url = findCodeOrThrow(code);
    return toStatsResponse(url);
  }

  public ShortUrlResponse update(String code, UpdateShortUrlRequest request) {
    ShortUrl url = findCodeOrThrow(code);

    url.setUrl(request.url());
    url.setExpiresAt(request.expiresAt());

    return toResponse(repository.save(url));
  }

  public void delete(String code) {
    ShortUrl url = findCodeOrThrow(code);
    repository.delete(url);
  }

  private ShortUrlResponse toResponse(ShortUrl url) {
    return new ShortUrlResponse(
        url.getId(),
        url.getUrl(),
        url.getShortCode(),
        url.getCreatedAt(),
        url.getUpdatedAt(),
        url.getExpiresAt());
  }

  private ShortUrlStatsResponse toStatsResponse(ShortUrl url) {
    return new ShortUrlStatsResponse(
        url.getId(),
        url.getUrl(),
        url.getShortCode(),
        url.getCreatedAt(),
        url.getUpdatedAt(),
        url.getExpiresAt(),
        url.getAccessCount());
  }

  private ShortUrl findCodeOrThrow(String code) {
    return repository
        .findByShortCode(code)
        .orElseThrow(() -> new ShortUrlNotFoundException("url not found"));
  }
}
