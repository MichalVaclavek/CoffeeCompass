package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import cz.fungisoft.coffeecompass.configuration.CoffeeCompassConfiguration;
import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapperImpl;
import cz.fungisoft.coffeecompass.repository.CoffeeSitePageableRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.StarsForCoffeeSiteAndUserRepository;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CSStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteTypeService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import cz.fungisoft.coffeecompass.service.CompanyService;
import cz.fungisoft.coffeecompass.service.CupTypeService;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteBuilder;

@SpringBootTest(classes = {
        CoffeeCompassConfiguration.class,
        CoffeeSiteServiceImpl.class,
        CoffeeSiteMapperImpl.class
})
class CoffeeSiteCachingTest {

    @MockBean
    private CoffeeSiteRepository coffeeSiteRepository;

    @MockBean
    private CoffeeSitePageableRepository coffeeSitePageableRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private CoffeeSortService coffeeSortService;

    @MockBean
    private CoffeeSiteTypeService coffeeSiteTypeService;

    @MockBean
    private CSStatusService coffeeSiteStatusService;

    @MockBean
    private PriceRangeService priceRangeService;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private CSRecordStatusService csRecordStatusService;

    @MockBean
    private SiteLocationTypeService siteLocationTypeService;

    @MockBean
    private CupTypeService cupTypeService;

    @MockBean
    private NextToMachineTypeService nextToMachineTypeService;

    @MockBean
    private OtherOfferService otherOfferService;

    @MockBean
    private StarsForCoffeeSiteAndUserRepository siteStarsRepo;

    @MockBean
    private CoffeeSiteRecordStatusRepository coffeeSiteRecordStatusRepository;

    @MockBean
    private ImageStorageService imageStorageService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private ConfigProperties configProperties;

    @MockBean
    private ImagesService imagesService;

    @Autowired
    private CoffeeSiteService coffeeSiteService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Optional.ofNullable(cacheManager.getCache("coffeeSitesCache")).ifPresent(cache -> cache.clear());
    }

    @Test
    void deleteShouldEvictCachedPaginatedSitesOfLoggedInUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("tester");

        CoffeeSite site = new CoffeeSiteBuilder()
                .setName("Cached Site")
                .setOriginalUser(user)
                .setSiteType("automat")
                .setStatusSitu(CoffeeSiteStatusEnum.INSERVICE)
                .setRecordStatus(CoffeeSiteRecordStatusEnum.ACTIVE)
                .build();
        site.setId(UUID.randomUUID());
        site.setCreatedOn(LocalDateTime.now());

        PageRequest pageable = PageRequest.of(0, 5);
        Page<CoffeeSite> firstPage = new PageImpl<>(java.util.List.of(site), pageable, 1);
        Page<CoffeeSite> secondPage = Page.empty(pageable);

        when(userService.getCurrentLoggedInUser()).thenReturn(Optional.of(user));
        when(userService.hasADMINRole(any(User.class))).thenReturn(false);
        when(userService.hasADMINorDBARole(any(User.class))).thenReturn(false);
        when(imagesService.getBasicObjectImageUrl(anyString())).thenReturn(Optional.empty());
        when(imageStorageService.isImageAvailableForSiteId(any(UUID.class))).thenReturn(false);
        when(siteStarsRepo.getNumOfHodnoceniForSite(any(UUID.class))).thenReturn(0);
        when(coffeeSitePageableRepository.findByOriginalUserAndRecordStatusStatusNot(eq(user), eq("CANCELED"), eq(pageable)))
                .thenReturn(firstPage)
                .thenReturn(secondPage);

        Page<?> cachedResult = coffeeSiteService.findAllNotCancelledFromLoggedInUserPaginated(pageable);
        assertThat(cachedResult.getContent()).hasSize(1);

        coffeeSiteService.delete(site.getId().toString());

        Page<?> refreshedResult = coffeeSiteService.findAllNotCancelledFromLoggedInUserPaginated(pageable);
        assertThat(refreshedResult.getContent()).isEmpty();

        verify(coffeeSitePageableRepository, times(2))
                .findByOriginalUserAndRecordStatusStatusNot(eq(user), eq("CANCELED"), eq(pageable));
        verify(coffeeSiteRepository).deleteById(site.getId());
    }
}
