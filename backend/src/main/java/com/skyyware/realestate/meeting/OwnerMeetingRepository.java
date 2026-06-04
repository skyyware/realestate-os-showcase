package com.skyyware.realestate.meeting;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerMeetingRepository extends JpaRepository<OwnerMeeting, UUID> {
    List<OwnerMeeting> findTop6ByPropertyOrderByMeetingDateDesc(PropertyAsset property);
}
