package top.clematis.aquanote.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String createAt;
    private LocalDateTime lastLoginAt;
}
