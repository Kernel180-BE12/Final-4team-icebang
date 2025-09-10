package site.icebang.domain.user.dto;

import java.math.BigInteger;
import java.util.List;

import lombok.Getter;

import site.icebang.domain.auth.model.AuthCredential;

@Getter
public class UserProfileResponseDto {

  private final BigInteger id;
  private final String email;
  private final String name;
  private final List<String> roles;
  private final String status;

  public UserProfileResponseDto(AuthCredential authCredential) {
    this.id = authCredential.getId();
    this.email = authCredential.getEmail();
    this.name = authCredential.getEmail(); // name 필드가 없으면 email 사용
    this.roles = authCredential.getRoles();
    this.status = authCredential.getStatus();
  }

  public static UserProfileResponseDto from(AuthCredential authCredential) {
    return new UserProfileResponseDto(authCredential);
  }
}
