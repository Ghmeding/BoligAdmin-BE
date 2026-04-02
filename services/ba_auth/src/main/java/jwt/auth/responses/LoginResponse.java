package jwt.auth.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private long expiresAt;

    public LoginResponse(String token, long expiresAt){
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
