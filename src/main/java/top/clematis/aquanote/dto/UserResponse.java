package top.clematis.aquanote.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String userName;
    private String email;
    private String createAt;
}
