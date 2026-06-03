package com.skyyware.realestate.task;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTaskRepository extends JpaRepository<WorkTask, UUID> {
    List<WorkTask> findTop8ByPropertyOrderByCreatedAtDesc(PropertyAsset property);
}
