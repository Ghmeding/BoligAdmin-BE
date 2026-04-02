package ba.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDTO {
    private String id;
    private String email;
    private String fullName;
}
