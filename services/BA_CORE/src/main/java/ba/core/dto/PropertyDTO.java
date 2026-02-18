package ba.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDTO {
    private String id;
    private String title;
    private String address;
    private Float monthlyRent;
}
