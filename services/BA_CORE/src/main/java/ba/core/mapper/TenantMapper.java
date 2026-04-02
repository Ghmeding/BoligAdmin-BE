package ba.core.mapper;

import ba.core.dto.CreateTenantDTO;
import ba.core.dto.PropertyDTO;
import ba.core.dto.TenantDTO;
import ba.core.models.PropertyEntity;
import ba.core.models.TenantEntity;

public class TenantMapper {

    private TenantMapper() {}

    public static TenantEntity convertToEntity(CreateTenantDTO createTenantDTO, PropertyEntity propertyEntity, String ownerId) {
        if (createTenantDTO == null) return null;

        TenantEntity tenant = new TenantEntity();
        tenant.setEmail(createTenantDTO.getEmail());
        tenant.setFullName(createTenantDTO.getFullName());
        tenant.setOwnerId(ownerId);

        // We set the property proxy here
        tenant.setProperty(propertyEntity);

        return tenant;
    }

    public static TenantDTO convertToDTO(TenantEntity tenantEntity){
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(tenantDTO.getId());
        tenantDTO.setEmail(tenantEntity.getEmail());
        tenantDTO.setFullName(tenantEntity.getFullName());
        return tenantDTO;
    }
}
