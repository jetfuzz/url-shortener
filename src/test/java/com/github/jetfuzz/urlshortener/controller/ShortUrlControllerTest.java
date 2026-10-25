package com.github.jetfuzz.urlshortener.controller;

import com.github.jetfuzz.urlshortener.dto.CreateShortUrlRequest;
import com.github.jetfuzz.urlshortener.dto.ShortUrlResponse;
import com.github.jetfuzz.urlshortener.exception.ShortUrlNotFoundException;
import com.github.jetfuzz.urlshortener.service.ShortUrlService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
public class ShortUrlControllerTest {

  private static final Instant FIXED_TIME = Instant.parse("2026-10-20T20:40:00Z");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ShortUrlService service;

  @Test
  void create_blankUrl_returns400() throws Exception {
    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"url": ""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors.url").exists());
    verifyNoInteractions(service);
  }

  @Test
  void create_badJson_returns400() throws Exception {
    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"url":
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.errors").doesNotExist());
    verifyNoInteractions(service);
  }

  @Test
  void create_validUrl_returns201() throws Exception {
    when(service.create(new CreateShortUrlRequest("https://wikipedia.com", null)))
        .thenReturn(
            new ShortUrlResponse(1L, "https://wikipedia.com", "CODE01", FIXED_TIME, FIXED_TIME));

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"url": "https://wikipedia.com"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortCode").value("CODE01"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"error", "ERROR", "Swagger-UI"})
  void create_reservedWord_returns400(String alias) throws Exception {
    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                   {"url": "https://wikipedia.com", "customAlias": "%s"}
                   """
                        .formatted(alias)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors.customAlias").exists());
    verifyNoInteractions(service);
  }

  @Test
  void get_invalidCode_returns404() throws Exception {
    when(service.get("NOPE00")).thenThrow(new ShortUrlNotFoundException("url not found"));

    mockMvc
        .perform(get("/shorten/NOPE00"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.errors").doesNotExist());
  }

  @Test
  void redirect_validCode_returns302() throws Exception {
    when(service.redirect("CODE01")).thenReturn("https://wikipedia.com");

    mockMvc
        .perform(get("/CODE01"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://wikipedia.com"));
  }
}
