package com.skyyware.realestate.audit;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findTop20ByPropertyOrderByOccurredAtDesc(PropertyAsset property);
}
