package cz.fungisoft.coffeecompass.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import lombok.extern.log4j.Log4j2;

/**
 * Java EE filter používaný pro vyvolání speciálních akcí před dalším zpracováním požadavků z clientů
 * nebo pro filtrování požadavků z clientů podle jejich typu apod. Jde o specialni filtr pro
 * sdileni mezi ruznymi url? Cross-Origin Resource Sharing (CORS) - enables the cross-domain communication,
 * tj. napr. mezi example.com a api.example.com
 * Tento filtr pravdepodobne upravuje funkcionalitu CORS, aby se nedala zneuzit k zaskodnickym utoku.
 * Také pro nastavení HTML Headers a jejich hodnot ... ?
 * 
 * @author Michal Vaclavek
 *
 */
@Log4j2
public class CORSFilter implements Filter
{
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		log.info("Filtering on ...........................................................");
		
		HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization, Origin, Accept, Access-Control-Request-Method, Access-Control-Request-Headers");
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) {}

	@Override
	public void destroy() {}
}