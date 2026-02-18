package ba.core.controller;

import java.util.List;

import ba.core.dto.CreatePropertyDTO;
import ba.core.dto.PropertyDTO;
import ba.core.exception.PropertyException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ba.core.service.PropertyService;

@RestController
@RequestMapping("/property")
public class PropertyController {

    private final PropertyService propertyService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyController.class);

    public PropertyController(PropertyService propertyService){ this.propertyService = propertyService; }

    @GetMapping("/getAllOwnerProperties")
    public ResponseEntity<List<PropertyDTO>> getUserProperties(
            @AuthenticationPrincipal Jwt jwt
    )
    {
        String ownerId = jwt.getSubject();
        List<PropertyDTO> properties = propertyService.getAllOwnerProperties(ownerId);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("getProperty/{propertyId}")
    public ResponseEntity<PropertyDTO> getProperty(
            @PathVariable String propertyId
    ){
        PropertyDTO propertyEntity = propertyService.getProperty(propertyId);
        return ResponseEntity.ok().body(propertyEntity);
    }

    @PostMapping("/createProperty")
    public ResponseEntity<?> createProperty(
//            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePropertyDTO createPropertyDto
    ){
        String propertyId;
        try {
            String ownerId = "test";
            propertyId = propertyService.createProperty(createPropertyDto, ownerId);
        } catch (PropertyException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e){
            LOGGER.error("Unexpected error", e);
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().body(String.valueOf(propertyId));
    }
}
