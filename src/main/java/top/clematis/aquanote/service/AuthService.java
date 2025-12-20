package top.clematis.aquanote.service;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.clematis.aquanote.config.AquaSecurityProperties;
import top.clematis.aquanote.dto.LoginResponse;
import top.clematis.aquanote.dto.TokenResponse;
import top.clematis.aquanote.dto.UserResponse;
import top.clematis.aquanote.mapper.RefreshTokenMapper;
import top.clematis.aquanote.pojo.RefreshToken;
import top.clematis.aquanote.pojo.User;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Base64;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenMapper refreshTokenMapper;
    private final SecretKey jwtSecretKey;
    private final AquaSecurityProperties securityProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResponse login(String email, String password) {
        User user = userService.login(email, password);
        if (user == null) {
            return null;
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshTokenAndPersist(user.getUserId());

        UserResponse userResponse = UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .createAt(user.getCreateAt())
                .build();

        return LoginResponse.builder()
                .user(userResponse)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(securityProperties.getJwt().getAccessTokenTtlSeconds())
                .build();
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String tokenHash = sha256Base64(refreshToken);
        RefreshToken stored = refreshTokenMapper.findByTokenHash(tokenHash);
        if (stored == null) {
            return null;
        }
        if (stored.getRevokedAt() != null) {
            return null;
        }
        if (stored.getExpiresAt() == null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        String newRefreshToken = generateRefreshTokenAndPersist(stored.getUserId());
        String newHash = sha256Base64(newRefreshToken);

        int revoked = refreshTokenMapper.revoke(tokenHash, newHash);
        if (revoked == 0) {
            return null;
        }

        User user = userService.findByUserId(stored.getUserId());
        if (user == null) {
            return null;
        }

        String accessToken = generateAccessToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(securityProperties.getJwt().getAccessTokenTtlSeconds())
                .build();
    }

    @Transactional
    public boolean logout(String refreshToken) {
        String tokenHash = sha256Base64(refreshToken);
        return refreshTokenMapper.revokeWithoutReplacement(tokenHash) > 0;
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        long ttlSeconds = securityProperties.getJwt().getAccessTokenTtlSeconds();

        try {
            JWTClaimsSet nimbusClaims = new JWTClaimsSet.Builder()
                    .subject(user.getUserId())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim("email", user.getEmail())
                    .claim("userName", user.getUserName())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                    nimbusClaims
            );
            signedJWT.sign(new MACSigner(jwtSecretKey.getEncoded()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("生成JWT失败", e);
        }
    }

    private String generateRefreshTokenAndPersist(String userId) {
        String refreshToken = generateRandomToken();
        String tokenHash = sha256Base64(refreshToken);

        long ttlSeconds = securityProperties.getJwt().getRefreshTokenTtlSeconds();
        RefreshToken entity = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(ttlSeconds))
                .build();

        refreshTokenMapper.insert(entity);
        return refreshToken;
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
