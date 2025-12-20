package top.clematis.aquanote.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.clematis.aquanote.pojo.RefreshToken;

@Mapper
public interface RefreshTokenMapper {

    @Insert("INSERT INTO refresh_token (token_hash, user_id, expires_at, created_at) VALUES (#{tokenHash}, #{userId}, #{expiresAt}, #{createdAt})")
    int insert(RefreshToken refreshToken);

    @Select("SELECT * FROM refresh_token WHERE token_hash = #{tokenHash}")
    RefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);

    @Update("UPDATE refresh_token SET revoked_at = NOW(), replaced_by_hash = #{replacedByHash} WHERE token_hash = #{tokenHash} AND revoked_at IS NULL")
    int revoke(@Param("tokenHash") String tokenHash, @Param("replacedByHash") String replacedByHash);

    @Update("UPDATE refresh_token SET revoked_at = NOW() WHERE token_hash = #{tokenHash} AND revoked_at IS NULL")
    int revokeWithoutReplacement(@Param("tokenHash") String tokenHash);
}
