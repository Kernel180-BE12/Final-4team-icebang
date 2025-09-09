package com.gltkorea.icebang.unit.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UnitTestSupportTest extends UnitTestSupport {

  @Autowired private DataSource dataSource;

  @Test
  void shouldUseH2DatabaseWithMariaDBMode() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      String url = connection.getMetaData().getURL();
      assertThat(url).contains("h2:mem:testdb");

      // MariaDB 모드 확인
      Statement stmt = connection.createStatement();
      ResultSet rs =
          stmt.executeQuery(
              "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'");
      if (rs.next()) {
        assertThat(rs.getString(1)).isEqualTo("MariaDB");
      }
    }
  }

  @Test
  void shouldLoadApplicationContext() {
    // Spring Context 로딩 확인
    assertThat(dataSource).isNotNull();
  }
}
