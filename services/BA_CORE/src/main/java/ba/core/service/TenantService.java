package ba.core.service;

import ba.core.dto.CreateTenantDTO;
import ba.core.dto.TenantDTO;
import ba.core.mapper.PropertyMapper;
import ba.core.mapper.TenantMapper;
import ba.core.models.PropertyEntity;
import ba.core.models.TenantEntity;
import ba.core.repository.PropertyRepository;
import ba.core.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * Creates a new tenant for the specified property.
     * If the property already has an active tenant, an {@link IllegalStateException} is thrown.
     * Additionally, triggers an event to notify other services of the new tenant creation.
     *
     * @param createTenantDTO the data transfer object containing the details of the tenant to be created,
     *                        including the property ID, email, and full name of the tenant
     * @return the unique identifier (ID) of the newly created tenant
     * @throws RuntimeException          if the property specified by the property ID is not found
     * @throws IllegalStateException     if the specified property already has an active tenant
     */
    @Transactional
    public String createTenant(CreateTenantDTO createTenantDTO, String ownerId) {
        PropertyEntity propertyEntity = propertyRepository.findById(createTenantDTO.getPropertyId())
            .orElseThrow(() -> new RuntimeException("Property Not Found"));

        if(propertyEntity.getTenant() != null){
            throw new IllegalStateException("This property already has an active tenant");
        }
        
        TenantEntity tenantEntity = TenantMapper.convertToEntity(createTenantDTO, propertyEntity, ownerId);
        streamBridge.send("tenantCreatedProducer-out-0", createTenantDTO);

        return tenantRepository.save(tenantEntity).getId();
    }

    /**
     * Retrieves a list of all tenants from the system.
     * Each tenant is represented as a data transfer object (DTO).
     *
     * @return a list of {@link TenantDTO} instances representing all tenants
     */
    public List<TenantDTO> getAllTenants(){
        return tenantRepository.findAll()
                .stream()
                .map(TenantMapper::convertToDTO)
                .toList();
    }

    public List<TenantDTO> getAllOwnerTenants(String ownerId){
        return tenantRepository.findByOwnerId(ownerId)
                .stream()
                .map(TenantMapper::convertToDTO)
                .toList();
    }


}
