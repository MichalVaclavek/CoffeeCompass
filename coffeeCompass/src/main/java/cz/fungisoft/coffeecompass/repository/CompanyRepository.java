package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Company;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

//    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select comp from Company comp where nameOfCompany=?1")
    Company searchByName(String company);
}
