package ba.core.controller;

import ba.core.dto.CreateTenantDTO;
import ba.core.dto.TenantDTO;
import ba.core.exception.TenantException;
import ba.core.service.TenantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(StreamBridge streamBridge, TenantService tenantService){
        this.tenantService = tenantService; }

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantController.class);


    @GetMapping("/getAllTenants")
    public ResponseEntity<?> getAllTenants(){
        try {
            List<TenantDTO> tenants = tenantService.getAllTenants();
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            throw new TenantException(e.getMessage());
        }
    }

    @GetMapping("/getAllOwnerTenants")
    public ResponseEntity<?> getAllOwnerTenants(
            @AuthenticationPrincipal Jwt jwt
    ){
        try {
            String ownerId = jwt.getClaim("userId");
            List<TenantDTO> tenants = tenantService.getAllOwnerTenants(ownerId);
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            throw new TenantException(e.getMessage());
        }
    }

    @PostMapping("/createTenant")
    public ResponseEntity<?> createTenant(
            @Valid @RequestBody CreateTenantDTO createTenantDTO,
            @AuthenticationPrincipal Jwt jwt
    ){
        String tenantId;
        String ownerId = jwt.getClaim("userId");
        tenantId = tenantService.createTenant(createTenantDTO, ownerId);

        return ResponseEntity.ok().body(String.valueOf(tenantId));
    }
}
