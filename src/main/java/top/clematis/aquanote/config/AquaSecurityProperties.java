package top.clematis.aquanote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aqua.security")
public class AquaSecurityProperties {

    private boolean debugPermitNotes = false;
    private boolean debugAllowUserIdHeader = false;

    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenTtlSeconds = 900;
        private long refreshTokenTtlSeconds = 2592000;
    }
}
