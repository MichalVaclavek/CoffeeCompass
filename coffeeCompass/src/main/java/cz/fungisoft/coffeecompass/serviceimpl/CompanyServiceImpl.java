package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.repository.CompanyRepository;
import cz.fungisoft.coffeecompass.service.CompanyService;

@Service("companyService")
@Transactional
public class CompanyServiceImpl implements CompanyService
{
    @Autowired
    private CompanyRepository companyRepo;
    
    @Override
    public Company findCompanyByName(String companyName) {
        return companyRepo.searchByName(companyName);
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepo.findAll();
    }

    @Override
    public Company saveCompany(String companyName) {
        Company company = new Company();
        company.setId(0);
        company.setNameOfCompany(companyName);
        return companyRepo.saveAndFlush(company);
    }
}
