package top.clematis.aquanote.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.clematis.aquanote.dto.*;
import top.clematis.aquanote.pojo.User;
import top.clematis.aquanote.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    

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
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            if (user != null) {
                UserResponse userResponse = UserResponse.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .email(user.getEmail())
                        .createAt(user.getCreateAt())
                        .build();
                return ApiResponse.success("登录成功", userResponse);
            } else {
                return ApiResponse.error("邮箱或密码错误");
            }
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }
    

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        try {
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
