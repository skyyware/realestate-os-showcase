package com.skyyware.realestate.decision;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityDecisionRepository extends JpaRepository<CommunityDecision, UUID> {
    List<CommunityDecision> findTop8ByPropertyOrderByMeetingDateDesc(PropertyAsset property);
}
