package com.eblog.invite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@EnabledIfEnvironmentVariable(named = "EBLOG_ENABLE_TESTCONTAINERS", matches = "true")
class InviteCodeConcurrencyTest {

  @Container
  private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("eblog_test")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void mysqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @Autowired
  private InviteCodeService inviteCodeService;

  @Test
  void oneTimeCodeConsumesOnceUnderConcurrency() throws Exception {
    List<String> codes = inviteCodeService.createCodes(1, 1, null);
    String code = codes.get(0);

    int threads = 20;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    List<Future<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < threads; i++) {
      final long userId = i + 1L;
      futures.add(executor.submit(() -> {
        start.await();
        try {
          return inviteCodeService.consume(code, userId, "127.0.0.1");
        } finally {
          done.countDown();
        }
      }));
    }

    start.countDown();
    done.await(15, TimeUnit.SECONDS);
    executor.shutdownNow();

    int success = 0;
    for (Future<Boolean> future : futures) {
      if (Boolean.TRUE.equals(future.get(5, TimeUnit.SECONDS))) {
        success++;
      }
    }
    assertEquals(1, success);
    InviteCodeEntity entity = inviteCodeService.findByCode(code);
    assertNotNull(entity);
    assertEquals(1, entity.getUsedCount());
  }
}
