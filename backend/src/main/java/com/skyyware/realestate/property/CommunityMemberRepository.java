package com.skyyware.realestate.property;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {
    List<CommunityMember> findByPropertyOrderByCreatedAtAsc(PropertyAsset property);

    List<CommunityMember> findByEmailIgnoreCaseOrderByCreatedAtAsc(String email);

    Optional<CommunityMember> findByPropertyAndEmailIgnoreCase(PropertyAsset property, String email);
}

