package com.github.jetfuzz.urlshortener.controller;

import com.github.jetfuzz.urlshortener.dto.ShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.service.ShortUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShortUrlController {

  private final ShortUrlService service;

  public ShortUrlController(ShortUrlService service) {
    this.service = service;
  }

  // POST /shorten
  @PostMapping("/shorten")
  @ResponseStatus(HttpStatus.CREATED)
  public ShortUrlResponse create(@RequestBody ShortUrlRequest request) {
    return service.create(request);
  }

  // GET /shorten/{code}
  @GetMapping("/shorten/{code}")
  public ShortUrlResponse get(@PathVariable String code) {
    return service.get(code);
  }

  // PUT /shorten/{code}
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
