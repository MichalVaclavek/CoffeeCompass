package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.Company;

public interface CompanyService
{
    public Company findCompanyByName(String companyName);
    public List<Company> getAllCompanies();
    public Company saveCompany(String comapnyName);
}
