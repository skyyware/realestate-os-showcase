package com.skyyware.realestate.activity;

import com.skyyware.realestate.identity.AppUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, UUID> {
    List<ActivityEvent> findTop12ByUserOrderByCreatedAtDesc(AppUser user);
}
