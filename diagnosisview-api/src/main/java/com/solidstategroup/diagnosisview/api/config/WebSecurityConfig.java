package com.solidstategroup.diagnosisview.api.config;

import com.solidstategroup.diagnosisview.api.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
* Web security configuration, including SAML handling.
* See https://github.com/vdenotaris/spring-boot-security-saml-sample
*/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Long MAX_AUTH_AGE = 604800L;
    private static final int HTTPS_SOCKET = 443;

    @Value("${JWT_ENABLED:false}")
    private String jwtEnabled;

    /**
    * Defines the web based security configuration.
    * @param http HttpSecurity, allows configuring web based security for specific http requests
    * @throws Exception configuring HTTP security
    */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        http
            .headers().frameOptions().sameOrigin();
        http
            .csrf()
            .disable();
        http
                .headers()
                .contentTypeOptions().and()
                .cacheControl().disable();
        http
            .addFilterAfter(new CorsFilter(), BasicAuthenticationFilter.class);

        http
            .authorizeRequests()
                .antMatchers("/**").permitAll()
            .antMatchers("/api/**").permitAll()
            .antMatchers("/public/**").permitAll()
            .antMatchers("/favicon.ico").anonymous()
            .anyRequest().authenticated();
        http
            .logout()
            .logoutSuccessUrl("/");
    }

}
