package com.solidstategroup.diagnosisview.service.impl;


import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.model.Utils;
import com.solidstategroup.diagnosisview.repository.UserRepository;
import com.solidstategroup.diagnosisview.service.UserService;
import lombok.extern.java.Log;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * {@inheritDoc}.
 */
@Log
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;


    /**
     * Constructor for the dashboard user service.
     *
     * @param userRepository the repo to autowire
     */
    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createOrUpdateUser(final User user) throws Exception {
        //this is a new user
        if (user.getId() == null) {
            if (userRepository.findOneByUsername(user.getUsername()) != null) {
                throw new IllegalStateException("This username already exists. Please try another one");
            }

            user.setDateCreated(new Date());
            user.setSalt(Utils.generateSalt());
            user.setPassword(DigestUtils.sha256Hex(user.getStoredPassword() +
                    user.getStoredSalt()));
            user.setToken(UUID.randomUUID().toString());
        } else {
            user.setSalt(Utils.generateSalt());
            user.setPassword(DigestUtils.sha256Hex(
                    String.format("%s%s", user.getStoredPassword(), user.getStoredSalt())));
        }
        if (user.getLogoData() != null) {
            user.setProfileImage(Base64.decodeBase64(new String(user.getLogoData()).getBytes("UTF-8")));
        }

        return userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(User user) throws Exception {
        User user1 = userRepository.findOneByUsername(user.getUsername());
        userRepository.delete(user1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User login(final String username, final String password) throws Exception {
        User user = userRepository.findOneByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Please check your username and password.");
        }
        if (Utils.checkPassword(password, user.getStoredSalt(), user.getStoredPassword())) {
            return user;
        } else {
            throw new IllegalStateException("Please check your username and password");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(final String username) throws Exception {
        return userRepository.findOneByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByToken(final String token) throws Exception {
        return userRepository.findOneByToken(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAllUsers() throws Exception {
        return userRepository.findAll(new Sort(Sort.Direction.ASC, "dateCreated"));
    }
}
