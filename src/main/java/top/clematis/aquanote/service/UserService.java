package top.clematis.aquanote.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.clematis.aquanote.mapper.UserMapper;
import top.clematis.aquanote.pojo.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    

    public String generateUserId() {
        return UUID.randomUUID().toString();
    }
    

    public boolean register(String userName, String password, String email) {

        if (userMapper.findByEmail(email) != null) {
            return false;
        }
        

        User user = User.builder()
                .userId(generateUserId())
                .userName(userName)
                .password(passwordEncoder.encode(password))
                .email(email)
                .createAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        
        return userMapper.insertUser(user) > 0;
    }

    public User login(String email, String password) {
        User user = userMapper.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            userMapper.updateLastLoginTime(user.getUserId(), LocalDateTime.now());
            return user;
        }
        return null;
    }
    

    public User findByUserId(String userId) {
        return userMapper.findByUserId(userId);
    }
    

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
}
