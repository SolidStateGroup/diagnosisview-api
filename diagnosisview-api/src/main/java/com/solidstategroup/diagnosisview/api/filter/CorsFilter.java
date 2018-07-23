package com.solidstategroup.diagnosisview.api.filter;

import lombok.extern.java.Log;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS filter on all /api calls.
 */
@Log
@WebFilter(urlPatterns = {"/api/**"})
public class CorsFilter implements Filter {

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        log.info("Cors Filter initialised");
    }

    /**
     * Add CORS headers to response.
     *
     * @param servletRequest  ServletRequest request
     * @param servletResponse ServletResponse response
     * @param filterChain     FilterChain filter chain
     * @throws IOException      thrown adding headers
     * @throws ServletException thrown adding headers
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, "
                + "X-Requested-With, X-Auth-Token");
        response.setHeader("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");

        try {
            if (!((HttpServletRequest) servletRequest).getRequestURI().equals("/public/status")) {
                log.info("Url: " + ((HttpServletRequest) servletRequest).getRequestURI());
            }
        } catch (Exception e) {

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Required for override.
     */
    @Override
    public void destroy() {
    }

    /**
     * Required for override.
     *
     * @param filterConfig FilterConfig
     */
    @Override
    public void init(final FilterConfig filterConfig) {
    }
}
