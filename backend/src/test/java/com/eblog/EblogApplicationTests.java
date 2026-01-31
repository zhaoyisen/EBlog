package com.eblog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
      "spring.datasource.url=jdbc:h2:mem:eblog;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password="
    })
class EblogApplicationTests {

  @Test
  void contextLoads() {}
}
