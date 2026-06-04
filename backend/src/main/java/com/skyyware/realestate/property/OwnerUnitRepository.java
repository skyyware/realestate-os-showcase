package com.skyyware.realestate.property;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerUnitRepository extends JpaRepository<OwnerUnit, UUID> {
    List<OwnerUnit> findByProperty(PropertyAsset property);

    boolean existsByPropertyAndOwnerEmailIgnoreCase(PropertyAsset property, String ownerEmail);
}
