package com.github.jetfuzz.urlshortener.controller;

import com.github.jetfuzz.urlshortener.dto.ShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.dto.ShortUrlStatsResponse;
import com.github.jetfuzz.urlshortener.service.ShortUrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class ShortUrlController {

  private final ShortUrlService service;

  public ShortUrlController(ShortUrlService service) {
    this.service = service;
  }

  @PostMapping("/shorten")
  @ResponseStatus(HttpStatus.CREATED)
  public ShortUrlResponse create(@RequestBody ShortUrlRequest request) {
    return service.create(request);
  }

  @GetMapping("/shorten/{code}")
  public ShortUrlResponse get(@PathVariable String code) {
    return service.get(code);
  }

  @GetMapping("/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(service.redirect(code)));

    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  @GetMapping("/shorten/{code}/stats")
  public ShortUrlStatsResponse stats(@PathVariable String code) {
    return service.stats(code);
  }

  @PutMapping("/shorten/{code}")
  public ShortUrlResponse update(@PathVariable String code, @RequestBody ShortUrlRequest request) {
    return service.update(code, request);
  }

  @DeleteMapping("/shorten/{code}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String code) {
    service.delete(code);
  }
}
