package com.github.jetfuzz.urlshortener.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "short_urls")
public class ShortUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String url;
  private String shortCode;
  @CreationTimestamp private Instant createdAt;
  @UpdateTimestamp private Instant updatedAt;
  private long accessCount;
  private Instant expiresAt;

  protected ShortUrl() {}

  public ShortUrl(String url, String shortCode, Instant expiresAt) {
    this.url = url;
    this.shortCode = shortCode;
    this.expiresAt = expiresAt;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getShortCode() {
    return shortCode;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public long getAccessCount() {
    return accessCount;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
