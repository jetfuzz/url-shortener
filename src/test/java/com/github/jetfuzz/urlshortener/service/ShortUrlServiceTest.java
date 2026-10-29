package com.github.jetfuzz.urlshortener.service;

import com.github.jetfuzz.urlshortener.dto.CreateShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.entity.ShortUrl;
import com.github.jetfuzz.urlshortener.exception.AliasTakenException;
import com.github.jetfuzz.urlshortener.exception.ShortUrlExpiredException;
import com.github.jetfuzz.urlshortener.exception.ShortUrlNotFoundException;
import com.github.jetfuzz.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShortUrlServiceTest {

  @Mock private ShortUrlRepository repo;
  @Mock private ShortCodeGenerator generator;
  @InjectMocks private ShortUrlService service;
  private static final String CODE = "qwerty";

  @Test
  void create_validRequest_returnsResponseWithCode() {
    when(generator.generateShortCode()).thenReturn(CODE);
    when(repo.saveAndFlush(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

    ShortUrlResponse response =
        service.create(new CreateShortUrlRequest("https://wikipedia.com", null, null));

    assertEquals(CODE, response.shortCode());
    assertEquals("https://wikipedia.com", response.url());
  }

  @Test
  void create_codeCollision_retriesAndReturnsCode() {
    when(generator.generateShortCode()).thenReturn(CODE, "CODE02");
    when(repo.saveAndFlush(any(ShortUrl.class)))
        .thenThrow(DataIntegrityViolationException.class)
        .thenAnswer(inv -> inv.getArgument(0));

    ShortUrlResponse response =
        service.create(new CreateShortUrlRequest("https://wikipedia.com", null, null));

    assertEquals("CODE02", response.shortCode());
    assertEquals("https://wikipedia.com", response.url());
    verify(repo, times(2)).saveAndFlush(any(ShortUrl.class));
  }

  @Test
  void create_fiveCollisions_throwsIllegalStateException() {
    when(generator.generateShortCode()).thenReturn(CODE);
    when(repo.saveAndFlush(any(ShortUrl.class))).thenThrow(DataIntegrityViolationException.class);

    assertThrows(
        IllegalStateException.class,
        () -> service.create(new CreateShortUrlRequest("https://wikipedia.com", null, null)));
    verify(repo, times(5)).saveAndFlush(any());
  }

  @Test
  void create_takenAlias_throwsAliasTakenException() {
    when(repo.saveAndFlush(any(ShortUrl.class))).thenThrow(DataIntegrityViolationException.class);

    assertThrows(
        AliasTakenException.class,
        () -> service.create(new CreateShortUrlRequest("https://wikipedia.com", "alias", null)));
    verify(repo, times(1)).saveAndFlush(any());
  }

  @Test
  void get_unknownCode_throwsShortUrlNotFoundException() {
    when(repo.findByShortCode("NOPE00")).thenReturn(Optional.empty());

    assertThrows(ShortUrlNotFoundException.class, () -> service.get("NOPE00"));
  }

  @Test
  void redirect_existingCode_returnsUrlAndIncrementsCount() {
    when(repo.findByShortCode(CODE))
        .thenReturn(Optional.of(new ShortUrl("https://wikipedia.com", CODE, null)));

    String url = service.redirect(CODE);

    assertEquals("https://wikipedia.com", url);
    verify(repo).incrementAccessCount(CODE);
  }

  @Test
  void redirect_expiredCode_throwsAndDoesNotIncrement() {
    when(repo.findByShortCode("expired"))
        .thenReturn(
            Optional.of(
                new ShortUrl("https://example.com/", "expired", Instant.now().minusSeconds(60))));

    assertThrows(ShortUrlExpiredException.class, () -> service.redirect("expired"));
    verify(repo, never()).incrementAccessCount(anyString());
  }
}
