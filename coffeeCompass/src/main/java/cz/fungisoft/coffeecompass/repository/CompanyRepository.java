package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Company;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    @Query("select comp from Company comp where nameOfCompany=?1")
    Company searchByName(String company);
}
