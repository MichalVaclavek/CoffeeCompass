package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    @Query("select comp from Company comp where nameOfCompany=?1")
    Company searchByName(String company);
}
