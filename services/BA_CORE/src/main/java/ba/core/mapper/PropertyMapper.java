package ba.core.mapper;

import ba.core.dto.CreatePropertyDTO;
import ba.core.dto.PropertyDTO;
import ba.core.models.PropertyEntity;

import java.time.LocalDateTime;

public class PropertyMapper {

    private PropertyMapper() {}

    public static PropertyEntity convertToEntity(CreatePropertyDTO createPropertyDto, String ownerId) {
        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.setOwnerId(ownerId);
        propertyEntity.setTitle(createPropertyDto.getTitle());
        propertyEntity.setDescription(createPropertyDto.getDescription());
        propertyEntity.setAddress(createPropertyDto.getAddress());
        propertyEntity.setMonthlyRent(createPropertyDto.getMonthlyRent());

        // Set timestamps automatically
        LocalDateTime now = LocalDateTime.now();
        propertyEntity.setCreatedAt(now);
        propertyEntity.setUpdatedAt(now);

        return propertyEntity;
    }

    public static PropertyDTO convertToDTO(PropertyEntity propertyEntity){
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setId(propertyEntity.getId());
        propertyDTO.setAddress(propertyEntity.getAddress());
        propertyDTO.setTitle(propertyEntity.getTitle());
        propertyDTO.setMonthlyRent(propertyEntity.getMonthlyRent());
        return propertyDTO;
    }
}
