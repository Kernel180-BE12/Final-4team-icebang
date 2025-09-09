// package com.gltkorea.icebang;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import java.sql.Connection;
// import java.sql.SQLException;
// import java.util.Optional;
//
// import javax.sql.DataSource;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.jdbc.Sql;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.gltkorea.icebang.dto.UserDto;
// import com.gltkorea.icebang.mapper.UserMapper;
//
// @SpringBootTest
// @Import(TestcontainersConfiguration.class)
// @AutoConfigureTestDatabase(replace = Replace.NONE)
// @ActiveProfiles("test") // application-test-unit.yml 설정을 활성화
// @Transactional // 테스트 후 데이터 롤백
// @Sql(
//    scripts = {"classpath:sql/create-01-schema.sql", "classpath:sql/insert-user-data.sql"},
//    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
// class DatabaseConnectionTest {
//
//  @Autowired private DataSource dataSource;
//
//  @Autowired private UserMapper userMapper; // JPA Repository 대신 MyBatis Mapper를 주입
//
//  @Test
//  @DisplayName("DataSource를 통해 DB 커넥션을 성공적으로 얻을 수 있다.")
//  void canGetDatabaseConnection() {
//    try (Connection connection = dataSource.getConnection()) {
//      assertThat(connection).isNotNull();
//      assertThat(connection.isValid(1)).isTrue();
//      System.out.println("DB Connection successful: " + connection.getMetaData().getURL());
//    } catch (SQLException e) {
//      org.junit.jupiter.api.Assertions.fail("Failed to get database connection", e);
//    }
//  }
//
//  @Test
//  @DisplayName("MyBatis Mapper를 통해 '홍길동' 사용자를 이메일로 조회")
//  void findUserByEmailWithMyBatis() {
//    // given
//    String testEmail = "hong.gildong@example.com";
//
//    // when
//    Optional<UserDto> foundUser = userMapper.findByEmail(testEmail);
//
//    // then
//    // 사용자가 존재하고, 이름이 '홍길동'인지 확인
//    assertThat(foundUser).isPresent();
//    assertThat(foundUser.get().getName()).isEqualTo("홍길동");
//    System.out.println("Successfully found user with MyBatis: " + foundUser.get().getName());
//  }
//
//  @Test
//  @DisplayName("샘플 데이터가 올바르게 삽입되었는지 확인")
//  void verifyAllSampleDataInserted() {
//    // 사용자 데이터 확인
//    Optional<UserDto> hong = userMapper.findByEmail("hong.gildong@example.com");
//    assertThat(hong).isPresent();
//    assertThat(hong.get().getName()).isEqualTo("홍길동");
//
//    Optional<UserDto> kim = userMapper.findByEmail("kim.chulsu@example.com");
//    assertThat(kim).isPresent();
//    assertThat(kim.get().getName()).isEqualTo("김철수");
//
//    System.out.println("샘플 데이터 삽입 성공 - 홍길동, 김철수 확인");
//  }
// }
