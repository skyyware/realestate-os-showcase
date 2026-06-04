package com.skyyware.realestate.finance;

import com.skyyware.realestate.property.OwnerUnit;
import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseMoneyAssessmentRepository extends JpaRepository<HouseMoneyAssessment, UUID> {
    List<HouseMoneyAssessment> findTop8ByPropertyOrderByFiscalYearDescCreatedAtDesc(PropertyAsset property);

    List<HouseMoneyAssessment> findByPropertyAndFiscalYear(PropertyAsset property, int fiscalYear);

    List<HouseMoneyAssessment> findByUnitAndFiscalYear(OwnerUnit unit, int fiscalYear);
}
