package com.eblog.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {
  private static final long WINDOW_SECONDS = 300;
  private static final int MAX_ATTEMPTS = 10;

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<String, Bucket>();

  public boolean tryConsume(String key) {
    Bucket b = buckets.computeIfAbsent(key, k -> new Bucket());
    return b.tryConsume();
  }

  private static class Bucket {
    private long windowStart = 0;
    private int count = 0;

    synchronized boolean tryConsume() {
      long now = Instant.now().getEpochSecond();
      if (windowStart == 0 || (now - windowStart) >= WINDOW_SECONDS) {
        windowStart = now;
        count = 0;
      }
      if (count >= MAX_ATTEMPTS) {
        return false;
      }
      count += 1;
      return true;
    }
  }
}
