package site.icebang.global.config.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MyBatis에서 Jackson 라이브러리의 {@code JsonNode} 타입을 데이터베이스의 문자열 타입(예: VARCHAR, JSON)과 매핑하기 위한 커스텀 타입
 * 핸들러입니다.
 *
 * <p>이 핸들러를 통해, 애플리케이션에서는 JSON 데이터를 편리하게 {@code JsonNode} 객체로 다루고, 데이터베이스에는 해당 객체를 JSON 문자열 형태로
 * 저장하거나 읽어올 수 있습니다.
 *
 * <h2>MyBatis XML 매퍼에서의 사용 예제:</h2>
 *
 * <pre>{@code
 * <resultMap id="TaskResultMap" type="site.icebang.domain.workflow.model.Task">
 * <result property="parameters"
 * column="parameters"
 * javaType="com.fasterxml.jackson.databind.JsonNode"
 * jdbcType="VARCHAR"
 * typeHandler="site.icebang.global.config.mybatis.typehandler.JsonNodeTypeHandler"/>
 * </resultMap>
 * }</pre>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@MappedTypes(JsonNode.class)
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * {@code JsonNode} 파라미터를 DB에 저장하기 위해 JSON 문자열로 변환하여 PreparedStatement에 설정합니다.
   *
   * @param ps PreparedStatement 객체
   * @param i 파라미터 인덱스
   * @param parameter 변환할 JsonNode 객체
   * @param jdbcType JDBC 타입
   * @throws SQLException JSON 직렬화 실패 시
   */
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
    try {
      ps.setString(i, objectMapper.writeValueAsString(parameter));
    } catch (JsonProcessingException e) {
      throw new SQLException("Error converting JsonNode to String", e);
    }
  }

  /**
   * ResultSet에서 컬럼 이름으로 문자열을 가져와 {@code JsonNode} 객체로 파싱합니다.
   *
   * @param rs ResultSet 객체
   * @param columnName 컬럼 이름
   * @return 파싱된 JsonNode 객체, 원본이 null이면 null
   * @throws SQLException JSON 파싱 실패 시
   */
  @Override
  public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return parseJson(rs.getString(columnName));
  }

  /**
   * ResultSet에서 컬럼 인덱스로 문자열을 가져와 {@code JsonNode} 객체로 파싱합니다.
   *
   * @param rs ResultSet 객체
   * @param columnIndex 컬럼 인덱스
   * @return 파싱된 JsonNode 객체, 원본이 null이면 null
   * @throws SQLException JSON 파싱 실패 시
   */
  @Override
  public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return parseJson(rs.getString(columnIndex));
  }

  /**
   * CallableStatement에서 컬럼 인덱스로 문자열을 가져와 {@code JsonNode} 객체로 파싱합니다.
   *
   * @param cs CallableStatement 객체
   * @param columnIndex 컬럼 인덱스
   * @return 파싱된 JsonNode 객체, 원본이 null이면 null
   * @throws SQLException JSON 파싱 실패 시
   */
  @Override
  public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return parseJson(cs.getString(columnIndex));
  }

  /**
   * JSON 문자열을 {@code JsonNode} 객체로 변환하는 private 헬퍼 메소드입니다.
   *
   * @param json 파싱할 JSON 문자열
   * @return 파싱된 JsonNode 객체
   * @throws SQLException JSON 문자열이 유효하지 않을 경우
   */
  private JsonNode parseJson(String json) throws SQLException {
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readTree(json);
    } catch (JsonProcessingException e) {
      throw new SQLException("Error parsing JSON", e);
    }
  }
}
