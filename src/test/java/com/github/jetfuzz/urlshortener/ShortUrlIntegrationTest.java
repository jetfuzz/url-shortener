package com.github.jetfuzz.urlshortener;

import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShortUrlIntegrationTest {

  @Autowired private ShortUrlRepository repository;
  @Autowired private RestTestClient restTestClient;

  @BeforeEach
  void clearDb() {
    repository.deleteAll();
  }

  @Test
  void shortUrlLifecycle_CreateRedirectReadUpdateDelete() {

    String originalUrl = "https://example.com/";
    String updatedUrl = "https://wikipedia.com/";

    ShortUrlResponse created =
        restTestClient
            .post()
            .uri("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("url", originalUrl))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ShortUrlResponse.class)
            .returnResult()
            .getResponseBody();

    assertThat(created.url()).isEqualTo(originalUrl);
    assertThat(created.shortCode()).isNotBlank();
    String code = created.shortCode();

    restTestClient
        .get()
        .uri("/{code}", code)
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .location(originalUrl);

    restTestClient
        .get()
        .uri("/shorten/{code}/stats", code)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessCount")
        .isEqualTo(1);

    ShortUrlResponse updated =
        restTestClient
            .put()
            .uri("/shorten/{code}", code)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("url", updatedUrl))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ShortUrlResponse.class)
            .returnResult()
            .getResponseBody();

    assertThat(updated.url()).isEqualTo(updatedUrl);
    assertThat(updated.updatedAt()).isAfter(created.updatedAt());

    restTestClient
        .get()
        .uri("/{code}", code)
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .location(updatedUrl);

    restTestClient
        .get()
        .uri("/shorten/{code}/stats", code)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessCount")
        .isEqualTo(2);

    restTestClient.delete().uri("/shorten/{code}", code).exchange().expectStatus().isNoContent();

    restTestClient.get().uri("/shorten/{code}", code).exchange().expectStatus().isNotFound();
  }

  @Test
  void redirect_concurrentRequests_countsEveryHit() throws Exception {

    ShortUrlResponse response =
        restTestClient
            .post()
            .uri("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("url", "https://example.com/"))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ShortUrlResponse.class)
            .returnResult()
            .getResponseBody();

    String code = response.shortCode();

    int numberOfThreads = 50;

    CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      executor.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              restTestClient.get().uri("/{code}", code).exchange().expectStatus().isFound();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              finishLatch.countDown();
            }
          });
    }

    readyLatch.await();
    startLatch.countDown();
    finishLatch.await();
    executor.shutdown();

    restTestClient
        .get()
        .uri("/shorten/{code}/stats", code)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessCount")
        .isEqualTo(numberOfThreads);
  }
}
