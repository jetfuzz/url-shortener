package com.github.jetfuzz.urlshortener.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record UpdateShortUrlRequest(
    @NotBlank(message = "URL cannot be blank")
        @URL(regexp = "^https?://.*", message = "Invalid URL format")
        @Size(max = 2048, message = "URL cannot exceed 2048 characters")
        String url,
    @Future(message = "Expiry must be in the future") @Nullable Instant expiresAt) {}
