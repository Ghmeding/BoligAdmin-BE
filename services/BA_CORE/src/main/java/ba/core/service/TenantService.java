package ba.core.service;

import ba.core.dto.CreateTenantDTO;
import ba.core.mapper.TenantMapper;
import ba.core.models.PropertyEntity;
import ba.core.models.TenantEntity;
import ba.core.repository.PropertyRepository;
import ba.core.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    public TenantService(TenantRepository tenantRepository, PropertyRepository propertyRepository){
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public String createTenant(CreateTenantDTO createTenantDTO) {
        PropertyEntity propertyEntity = propertyRepository.getReferenceById(createTenantDTO.getPropertyId());

        TenantEntity tenantEntity = TenantMapper.convertToEntity(createTenantDTO, propertyEntity);

        return tenantRepository.save(tenantEntity).getId();
    }
}
