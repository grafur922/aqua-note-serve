package top.clematis.aquanote.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import top.clematis.aquanote.dto.*;
import top.clematis.aquanote.pojo.User;
import top.clematis.aquanote.service.AuthService;
import top.clematis.aquanote.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;
    

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            boolean success = userService.register(request.getUserName(), request.getPassword(), request.getEmail());
            if (success) {
                User user = userService.findByEmail(request.getEmail());
                UserResponse userResponse = UserResponse.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .email(user.getEmail())
                        .createAt(user.getCreateAt())
                        .build();
                return ApiResponse.success("注册成功", userResponse);
            } else {
                return ApiResponse.error("邮箱已存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }
    

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = authService.login(request.getEmail(), request.getPassword());
            if (loginResponse == null) {
                return ApiResponse.error("邮箱或密码错误");
            }
            return ApiResponse.success("登录成功", loginResponse);
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }


    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            TokenResponse tokenResponse = authService.refresh(request.getRefreshToken());
            if (tokenResponse == null) {
                return ApiResponse.error(401, "refreshToken无效或已过期");
            }
            return ApiResponse.success("刷新成功", tokenResponse);
        } catch (Exception e) {
            return ApiResponse.error("刷新失败: " + e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            boolean ok = authService.logout(request.getRefreshToken());
            if (!ok) {
                return ApiResponse.error(400, "refreshToken无效");
            }
            return ApiResponse.success("登出成功", null);
        } catch (Exception e) {
            return ApiResponse.error("登出失败: " + e.getMessage());
        }
    }
    

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId, @AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null || jwt.getSubject() == null || !jwt.getSubject().equals(userId)) {
                return ApiResponse.error(403, "无权限");
            }
            User user = userService.findByUserId(userId);
            if (user != null) {
                UserResponse userResponse = UserResponse.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .email(user.getEmail())
                        .createAt(user.getCreateAt())
                        .build();
                return ApiResponse.success(userResponse);
            } else {
                return ApiResponse.error("用户不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
