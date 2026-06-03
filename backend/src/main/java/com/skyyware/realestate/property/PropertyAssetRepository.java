package com.skyyware.realestate.property;

import com.skyyware.realestate.identity.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyAssetRepository extends JpaRepository<PropertyAsset, UUID> {
    List<PropertyAsset> findByOwnerOrderByCreatedAtAsc(AppUser owner);

    Optional<PropertyAsset> findFirstByOwnerOrderByCreatedAtAsc(AppUser owner);
}
