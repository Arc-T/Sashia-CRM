package ir.crm.identity.auth;

import ir.crm.identity.role.Role;
import ir.crm.identity.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
class JwtService {

    private final JwtEncoder encoder;
    private final SecurityProperties properties;

    JwtService(JwtEncoder encoder, SecurityProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    /** Subject is the login name; "perms" carries "module:action" authorities (or "*" for admins). */
    String issue(User user, Role role, List<String> permissions) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("crm-backend")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(properties.jwt().expiration()))
                .claim("uid", user.getId())
                .claim("name", user.getName())
                .claim("role", role.getKind().key())
                .claim("perms", permissions)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
