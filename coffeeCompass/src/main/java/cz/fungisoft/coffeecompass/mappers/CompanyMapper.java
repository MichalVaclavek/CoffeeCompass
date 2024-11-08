package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CompanyDTO;
import cz.fungisoft.coffeecompass.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CompanyMapper {

    @Mapping(target = "id", source="extId")
    Company companyDtoToCompany(CompanyDTO companyDTO);

    @Mapping(target = "extId", source="id")
    CompanyDTO companyToCompanyDto(Company company);
}
