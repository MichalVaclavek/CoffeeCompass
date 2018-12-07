package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CupType;

public interface CupTypeRepository extends JpaRepository<CupType, Integer>
{
    @Query("select ct from CupType ct where cupType=?1")
    public CupType searchByName(String cupType);
}
