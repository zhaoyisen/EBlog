package com.eblog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.MySQLContainer;

class TestcontainersMysqlSmokeTest {

  @Test
  @EnabledIfEnvironmentVariable(named = "EBLOG_ENABLE_TESTCONTAINERS", matches = "true")
  void mysqlContainerStarts() {
    MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    mysql.start();
    mysql.stop();
  }
}
