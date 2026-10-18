package com.github.jetfuzz.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ShortUrlRequest(
    @NotBlank(message = "URL cannot be blank")
        @URL(regexp = "^https?://.*", message = "Invalid URL format")
        @Size(max = 2048, message = "URL cannot exceed 2048 characters")
        String url) {}
