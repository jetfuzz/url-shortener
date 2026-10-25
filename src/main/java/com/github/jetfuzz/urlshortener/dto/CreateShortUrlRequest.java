package com.github.jetfuzz.urlshortener.dto;

import com.github.jetfuzz.urlshortener.validation.NotReserved;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateShortUrlRequest(
    @NotBlank(message = "URL cannot be blank")
        @URL(regexp = "^https?://.*", message = "Invalid URL format")
        @Size(max = 2048, message = "URL cannot exceed 2048 characters")
        String url,
    @Size(min = 3, max = 32, message = "Alias must be 3-32 characters")
        @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Alias may only contain letters, digits, - and _")
        @NotReserved
        String customAlias) {}
