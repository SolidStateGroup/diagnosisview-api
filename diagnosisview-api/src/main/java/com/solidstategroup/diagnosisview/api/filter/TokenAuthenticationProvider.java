package com.solidstategroup.diagnosisview.api.filter;

import com.solidstategroup.diagnosisview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails,
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
            throws AuthenticationException {

        // NOOP
    }

    @Override
    protected UserDetails retrieveUser(
            String username,
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
            throws AuthenticationException {
        final Object token = usernamePasswordAuthenticationToken.getCredentials();

         UserDetails details = Optional
                .ofNullable(token)
                .map(String::valueOf)
                .map(userRepository::findOneByToken)
                .orElseThrow(() -> new UsernameNotFoundException("Shittt"));

        details.getAuthorities().forEach(ga -> System.out.println(ga.getAuthority()));

        return details;
    }
}
