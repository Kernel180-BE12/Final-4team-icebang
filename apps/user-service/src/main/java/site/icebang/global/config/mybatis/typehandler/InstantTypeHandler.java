package site.icebang.global.config.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * MyBatis에서 Java 8의 {@code Instant} 타입을 데이터베이스의 TIMESTAMP 타입과 매핑하기 위한 커스텀 타입 핸들러입니다.
 *
 * <p>이 핸들러를 통해 애플리케이션에서는 UTC 기준의 시간을 {@code Instant} 객체로 다루고, 데이터베이스에는 해당 객체를 TIMESTAMP 형태로 저장하거나
 * 읽어올 수 있습니다.
 *
 * <h2>MyBatis XML 매퍼에서의 사용 예제:</h2>
 *
 * <pre>{@code
 * <resultMap id="WorkflowRunResultMap" type="site.icebang.domain.workflow.model.WorkflowRun">
 *     <result property="startedAt"
 *             column="started_at"
 *             javaType="java.time.Instant"
 *             jdbcType="TIMESTAMP"
 *             typeHandler="site.icebang.global.config.mybatis.typehandler.InstantTypeHandler"/>
 * </resultMap>
 * }</pre>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@MappedTypes(Instant.class)
public class InstantTypeHandler extends BaseTypeHandler<Instant> {

  /**
   * {@code Instant} 파라미터를 DB에 저장하기 위해 Timestamp로 변환하여 PreparedStatement에 설정합니다.
   *
   * @param ps PreparedStatement 객체
   * @param i 파라미터 인덱스
   * @param parameter 변환할 Instant 객체
   * @param jdbcType JDBC 타입
   * @throws SQLException 변환 실패 시
   */
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, Timestamp.from(parameter));
  }

  /**
   * ResultSet에서 컬럼 이름으로 Timestamp를 가져와 {@code Instant} 객체로 변환합니다.
   *
   * @param rs ResultSet 객체
   * @param columnName 컬럼 이름
   * @return 변환된 Instant 객체, 원본이 null이면 null
   * @throws SQLException 변환 실패 시
   */
  @Override
  public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnName);
    return timestamp != null ? timestamp.toInstant() : null;
  }

  /**
   * ResultSet에서 컬럼 인덱스로 Timestamp를 가져와 {@code Instant} 객체로 변환합니다.
   *
   * @param rs ResultSet 객체
   * @param columnIndex 컬럼 인덱스
   * @return 변환된 Instant 객체, 원본이 null이면 null
   * @throws SQLException 변환 실패 시
   */
  @Override
  public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(columnIndex);
    return timestamp != null ? timestamp.toInstant() : null;
  }

  /**
   * CallableStatement에서 컬럼 인덱스로 Timestamp를 가져와 {@code Instant} 객체로 변환합니다.
   *
   * @param cs CallableStatement 객체
   * @param columnIndex 컬럼 인덱스
   * @return 변환된 Instant 객체, 원본이 null이면 null
   * @throws SQLException 변환 실패 시
   */
  @Override
  public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Timestamp timestamp = cs.getTimestamp(columnIndex);
    return timestamp != null ? timestamp.toInstant() : null;
  }
}
