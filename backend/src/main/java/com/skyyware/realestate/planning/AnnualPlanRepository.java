package com.skyyware.realestate.planning;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnualPlanRepository extends JpaRepository<AnnualPlan, UUID> {
    List<AnnualPlan> findTop4ByPropertyOrderByFiscalYearDesc(PropertyAsset property);
}
