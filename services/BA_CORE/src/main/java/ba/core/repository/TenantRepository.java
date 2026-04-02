package ba.core.repository;

import ba.core.models.PropertyEntity;
import ba.core.models.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {
    List<TenantEntity> findByOwnerId(String ownerId);
}
