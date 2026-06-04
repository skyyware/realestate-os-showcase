package com.skyyware.realestate.audit;

import com.skyyware.realestate.identity.AppUser;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    public void record(AppUser actor, String action, String targetType, UUID targetId, String summary) {
        auditLogs.save(new AuditLog(actor, action, targetType, targetId, summary));
    }
}
