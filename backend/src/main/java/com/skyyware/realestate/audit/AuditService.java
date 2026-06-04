package com.skyyware.realestate.audit;

import com.skyyware.realestate.identity.AppUser;
import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    public void record(AppUser actor, String action, String targetType, UUID targetId, String summary) {
        auditLogs.save(new AuditLog(actor, null, action, targetType, targetId, summary));
    }

    public void record(AppUser actor, PropertyAsset property, String action, String targetType, UUID targetId, String summary) {
        auditLogs.save(new AuditLog(actor, property, action, targetType, targetId, summary));
    }

    public List<AuditLog> findForProperty(PropertyAsset property) {
        return auditLogs.findTop20ByPropertyOrderByOccurredAtDesc(property);
    }
}
