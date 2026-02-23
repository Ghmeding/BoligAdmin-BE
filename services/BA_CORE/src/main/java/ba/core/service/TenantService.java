package ba.core.service;

import ba.core.dto.CreateTenantDTO;
import ba.core.mapper.TenantMapper;
import ba.core.models.PropertyEntity;
import ba.core.models.TenantEntity;
import ba.core.repository.PropertyRepository;
import ba.core.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    private final StreamBridge streamBridge;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    public TenantService(StreamBridge streamBridge, TenantRepository tenantRepository, PropertyRepository propertyRepository){
        this.streamBridge = streamBridge;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public String createTenant(CreateTenantDTO createTenantDTO) {
        PropertyEntity propertyEntity = propertyRepository.findById(createTenantDTO.getPropertyId())
            .orElseThrow(() -> new RuntimeException("Property Not Found"));

        if(propertyEntity.getTenant() != null){
            throw new IllegalStateException("This property already has an active tenant");
        }
        
        TenantEntity tenantEntity = TenantMapper.convertToEntity(createTenantDTO, propertyEntity);
        streamBridge.send("tenantCreatedProducer-out-0", createTenantDTO);

        return tenantRepository.save(tenantEntity).getId();
    }
}
