package top.clematis.aquanote.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.clematis.aquanote.pojo.User;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);
    
    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User findByUserId(String userId);
    
    @Insert("INSERT INTO user (user_id, user_name, password, email, create_at) VALUES (#{userId}, #{userName}, #{password}, #{email}, #{createAt})")
    int insertUser(User user);
    
    @Update("UPDATE user SET last_login_at = #{lastLoginAt} WHERE user_id = #{userId}")
    int updateLastLoginTime(String userId, java.time.LocalDateTime lastLoginAt);
}
