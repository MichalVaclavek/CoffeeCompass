package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.repository.CompanyRepository;
import cz.fungisoft.coffeecompass.service.CompanyService;

/**
 * Class to service Company/Dodavatel of the cofee machines names
 * 
 * @author Michal V8clavek
 *
 */
@Service("companyService")
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepo;
    
    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepo) {
        super();
        this.companyRepo = companyRepo;
    }

    /**
     * Tries to finds and return Company, based on it's name.
     * If not found, returns null.
     * 
     */
    @Override
    public Company findCompanyByName(String companyName) {
        Company comp = companyRepo.searchByName(companyName);
        return comp;
    }

    @Override
    @Cacheable(cacheNames = "companiesCache")
    public List<Company> getAllCompanies() {
        return companyRepo.findAll();
    }

    @Override
    public Company saveCompany(String companyName) {
        Company company = new Company();
//        company.setLongId(0);
        company.setNameOfCompany(companyName);
        return companyRepo.saveAndFlush(company);
    }
}
