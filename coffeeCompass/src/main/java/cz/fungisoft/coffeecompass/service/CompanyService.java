package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.Company;

public interface CompanyService {

    Company findCompanyByName(String companyName);
    List<Company> getAllCompanies();
    Company saveCompany(String comapnyName);
}
