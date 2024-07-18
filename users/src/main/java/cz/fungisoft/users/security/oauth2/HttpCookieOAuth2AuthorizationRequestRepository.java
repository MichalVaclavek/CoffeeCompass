package cz.fungisoft.users.security.oauth2;


/**
 * Original comment:<br>
 * The OAuth2 protocol recommends using a state parameter to prevent CSRF attacks.<br>
 * During authentication, the application sends this parameter in the authorization request,<br>
 * and the OAuth2 provider returns this parameter unchanged in the OAuth2 callback.<br>
 * The application compares the value of the state parameter returned from the OAuth2 provider<br>
 * with the value that it had sent initially. If they don’t match then it denies the authentication request.<br>
 * To achieve this flow, the application needs to store the state parameter somewhere,<br> 
 * so that it can later compare it with the state returned from the OAuth2 provider.<br> 
 * We’ll be storing the state as well as the redirect_uri in a short-lived cookie.
 * <p>
 * The following class provides functionality for storing the authorization request in cookies and retrieving it.<br>
 * 
 * @author source: https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 */
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRY_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                          .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                          .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRY_SECONDS);
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRY_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
