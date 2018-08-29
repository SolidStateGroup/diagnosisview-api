package com.solidstategroup.diagnosisview.repository;

import com.solidstategroup.diagnosisview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * JPA repository for User objects.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by the given username.
     *
     * @param username String the username to lookup
     * @return the found user
     */
    User findOneByUsername(final String username);

    /**
     * Find a user by the login token.
     *
     * @param token String the login token
     * @return the user found based on their token
     */
    User findOneByToken(final String token);


    /**
     * Final all users that are expiring in the next month
     *
     * @return the user expiring soon
     */
    List<User> findByExpiryDateLessThanEqualAndActiveSubscription(final Date date, final boolean activeSubscription);
}
