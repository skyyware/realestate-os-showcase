package com.skyyware.realestate.finance;

import com.skyyware.realestate.property.PropertyAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceEventRepository extends JpaRepository<FinanceEvent, UUID> {
    List<FinanceEvent> findTop8ByPropertyOrderByBookedOnDesc(PropertyAsset property);

    List<FinanceEvent> findByProperty(PropertyAsset property);
}
