package com.skyyware.realestate.communication;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityMessageRepository extends JpaRepository<CommunityMessage, UUID> {
    List<CommunityMessage> findTop8ByPropertyOrderByCreatedAtDesc(PropertyAsset property);
}
