package ba.core.controller;

import ba.core.dto.CreateTenantDTO;
import ba.core.exception.TenantException;
import ba.core.service.TenantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(StreamBridge streamBridge, TenantService tenantService){
        this.tenantService = tenantService; }

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantController.class);

    @PostMapping("/createTenant")
    public ResponseEntity<?> createTenant(
            @Valid @RequestBody CreateTenantDTO createTenantDTO
    ){
        String tenantId;
        tenantId = tenantService.createTenant(createTenantDTO);

        return ResponseEntity.ok().body(String.valueOf(tenantId));
    }
}
