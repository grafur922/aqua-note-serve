package top.clematis.aquanote.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String resolveUserId(Jwt jwt, String userIdHeader, boolean debugAllowUserIdHeader) {
        if (jwt != null && StringUtils.hasText(jwt.getSubject())) {
            return jwt.getSubject();
        }
        if (debugAllowUserIdHeader && StringUtils.hasText(userIdHeader)) {
            return userIdHeader;
        }
        return null;
    }
}
