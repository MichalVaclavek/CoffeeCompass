package cz.fungisoft.coffeecompass.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String headers = convert(httpServletRequest.getHeaderNames())
                .collect(Collectors.toMap(Function.identity(), k -> httpServletRequest.getHeader(k) + "\n")).toString();
        log.info("START preHandle() -------------------------- ");
        log.info("httpServletRequest.getRequestURL().toString(): {}", httpServletRequest.getRequestURL().toString());
        log.info("httpServletRequest.getRequestURI(): {}", httpServletRequest.getRequestURI());
        log.info("httpServletRequest.getHeaderNames(): {}", headers);
        log.info("httpServletRequest.getPathInfo(): {}", httpServletRequest.getPathInfo());
        log.info("httpServletRequest.getContextPath(): {}", httpServletRequest.getContextPath());
        log.info("httpServletRequest.getQueryString(): {}", httpServletRequest.getQueryString());
        log.info("httpServletRequest.getServletPath(): {}", httpServletRequest.getServletPath());
        log.info("httpServletRequest.getRemoteUser(): {}", httpServletRequest.getRemoteUser());

        String requestedUrl = httpServletRequest.getRequestURL().toString();
        if (requestedUrl.contains("/home")
            && requestedUrl.startsWith("http")
            && httpServletRequest.getHeader("referer") != null
            && httpServletRequest.getHeader("referer").contains("/user/register")
                && (httpServletRequest.getHeader("registerRedirected") == null ||
                !httpServletRequest.getHeader("registerRedirected").contains("yes"))
        && (o instanceof ResourceHttpRequestHandler)) {
            ;
//            String path = httpServletRequest.getRequestURI();
            httpServletResponse.setHeader("registerRedirected", "yes");
            httpServletResponse.sendRedirect(requestedUrl.replace("http", "https").replace("12001", "8443"));
            log.info("STOP preHandle() -------------------------- ");
            return false;
        }
        log.info("STOP preHandle() -------------------------- ");
        return true;
    }

    static class EnumerationSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private final Enumeration<T> enumeration;

        public EnumerationSpliterator(long est, int additionalCharacteristics, Enumeration<T> enumeration) {
            super(est, additionalCharacteristics);
            this.enumeration = enumeration;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (enumeration.hasMoreElements()) {
                action.accept(enumeration.nextElement());
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (enumeration.hasMoreElements())
                action.accept(enumeration.nextElement());
        }
    }

    public static <T> Stream<T> convert(Enumeration<T> enumeration) {
        return StreamSupport.stream(new EnumerationSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED, enumeration), false);
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

}
