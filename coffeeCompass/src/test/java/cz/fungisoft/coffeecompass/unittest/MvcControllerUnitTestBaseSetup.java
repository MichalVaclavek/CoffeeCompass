package cz.fungisoft.coffeecompass.unittest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.context.WebApplicationContext;

import cz.fungisoft.coffeecompass.security.JwtTokenProviderService;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CSStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteTypeService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import cz.fungisoft.coffeecompass.service.CupTypeService;
import cz.fungisoft.coffeecompass.service.IContactMeMessageService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;
import cz.fungisoft.coffeecompass.service.image.ImageResizeAndRotateService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import cz.fungisoft.coffeecompass.service.notifications.NotificationSubscriptionService;
import cz.fungisoft.coffeecompass.service.notifications.PushNotificationService;
import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.tokens.ValidateTokenService;
import cz.fungisoft.coffeecompass.service.user.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.user.UserProfileService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.weather.WeatherApiService;
import cz.fungisoft.coffeecompass.serviceimpl.user.CustomOAuth2UserService;

/**
 * Basic setup for all Unit tests for testing Mvc Controllers, i.e. using mvcMock
 * and all relating Spring MVC functions.
 * <p>
 * The main aim is to mock all Services needed for this testing.
 * <p>
 * All MVC Controller test can extend this class to get basic setup mocked.
 *  
 * @author Michal Vaclavek
 *
 */
public abstract class MvcControllerUnitTestBaseSetup {
    
    @Autowired
    protected WebApplicationContext context;
    
    @MockBean
    @Qualifier("jwtTokenUserAuthenticationService")
    protected CustomRESTUserAuthenticationService authenticationService;
    
    @MockBean
    protected JwtTokenProviderService tokenServiceMock;
    
    @MockBean
    protected StatisticsInfoService staticService;
    
    @MockBean
    protected CoffeeSiteService coffeeSiteService;
    
    @MockBean
    protected ClientRegistrationRepository clientRegistrationRepository;
    
    @MockBean
    protected ICommentService commentService;
    
    @MockBean
    protected IStarsForCoffeeSiteAndUserService starsForCoffeeSiteAndUserService;
    
    @MockBean
    protected  OtherOfferService otherOfferService; 
    
    @MockBean
    protected CSStatusService csStatusService;
    
    @MockBean
    protected StarsQualityService starsQualityService;
    
    @MockBean
    protected PriceRangeService priceRangeService;
    
    @MockBean
    protected SiteLocationTypeService siteLocationTypeService;
    
    @MockBean
    protected CupTypeService cupTypeService;
    
    @MockBean
    protected NextToMachineTypeService nextToMachineTypeService;
    
    @MockBean
    protected CoffeeSiteTypeService coffeeSiteTypeService;
    
    @MockBean
    protected CoffeeSortService coffeeSortService;
    
    @MockBean
    protected ImageStorageService imageStorageService;
    
    @MockBean
    protected IContactMeMessageService contactMeMessageService;
    
    @MockBean
    protected ImageResizeAndRotateService imageResizeAndRotateService;
    
    @MockBean
    protected ValidateTokenService validateTokenService;
    
    @MockBean
    protected UserProfileService userProfileService;
    
    @MockBean
    protected CSRecordStatusService csRecordStatusService;
  
    @MockBean
    protected UserSecurityService userSecurityService;
    
    @MockBean
    protected TokenCreateAndSendEmailService tokenCreateAndSendEmailService;

    @MockBean
    protected CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    protected  MessageSource messages;
    
    @MockBean
    protected AccessDeniedHandler accessDeniedHandler;
    
    @MockBean
    protected PasswordEncoder passwordEncoder; // = Mockito.mock(PasswordEncoder.class);
    
    @MockBean
    protected WeatherApiService weatherService;
    
    @MockBean
    protected NotificationSubscriptionService notificationSubscriptionService;
    
    @MockBean
    protected PushNotificationService pushNotificationService;
    
}
