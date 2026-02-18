package ba.core.service;

import ba.core.dto.CreatePropertyDTO;
import ba.core.dto.PropertyDTO;
import ba.core.exception.PropertyException;
import ba.core.mapper.PropertyMapper;
import ba.core.models.PropertyEntity;
import ba.core.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    /**
     * Retrieves all properties stored in the system database.
     * @return a list of all {@link PropertyEntity} entities
     */
    public List<PropertyDTO> getAllProperties(){
        return propertyRepository.findAll()
                .stream()
                .map(PropertyMapper::convertToDTO)
                .toList();
    }

    /**
     * Fetches all properties belonging to a specific owner.
     * @param ownerId the unique identifier of the property owner
     * @return a list of properties associated with the given owner
     */
    public List<PropertyDTO> getAllOwnerProperties(String ownerId) {
        return propertyRepository.findByOwnerId(ownerId)
                .stream()
                .map(PropertyMapper::convertToDTO)
                .toList();
    }

    /**
     * Retrieves a property by its unique identifier.
     * * @param propertyId The UUID or String ID of the property.
     * @return The found PropertyEntity.
     * @throws EntityNotFoundException if no property exists with the given ID.
     */
    public PropertyDTO getProperty(String propertyId) {
        return propertyRepository.findById(propertyId)
                .map(PropertyMapper::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Property with id %s does not exist.", propertyId)
                ));
    }

    /**
     * Creates and persists a new property record for an owner.
     * @param input the data transfer object containing property details
     * @param ownerId the identifier of the owner creating the property
     * @return a status string indicating the result of the operation
     */
    @Transactional
    public String createProperty(CreatePropertyDTO input, String ownerId){
        try {
            PropertyEntity newPropertyEntity = propertyRepository.save(PropertyMapper.convertToEntity(input, ownerId));
            return newPropertyEntity.getId();
        } catch (Exception e) {
            throw new PropertyException(e.getMessage());
        }
    }

    public void setPropertyMonthlyRent(PropertyEntity property, Float monthlyRent){

    }
}
