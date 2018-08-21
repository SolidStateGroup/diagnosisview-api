package com.solidstategroup.diagnosisview.service.test

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.solidstategroup.diagnosisview.model.SavedUserCode
import com.solidstategroup.diagnosisview.model.User
import com.solidstategroup.diagnosisview.repository.UserRepository
import com.solidstategroup.diagnosisview.service.UserService
import com.solidstategroup.diagnosisview.service.impl.UserServiceImpl
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification

@ContextConfiguration(classes = TestServiceConfig)
class UserServiceTest extends Specification {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppleReceiptValidation appleReceiptValidation;

    UserService userService;
    User user;
    SavedUserCode savedCode;

    def setup() {
        userService = new UserServiceImpl(userRepository, appleReceiptValidation);


        user = new User();
        user.username = "testerman3"
        savedCode = new SavedUserCode("CODE", "type", new Date());
        List<SavedUserCode> savedUserCodeList = new ArrayList<>();
        savedUserCodeList.add(savedCode);
        if (userService.getUser(user.username) == null) {
            user = userService.createOrUpdateUser(user)
        }
    }

    def "Get User"() {
        when:
        def savedUser = userService.getUser(user.username)
        then: "should return correct user"
        savedUser != null
        savedUser.username.equals(user.username)
    }

    def "Test Remove Favourite"() {
        when:
        userService.addFavouriteToUser(user, savedCode);
        and:
        userService.deleteFavouriteToUser(user, savedCode)
        then: "should return no favourites"
        User user = userService.getUser(user.username);
        user.getFavourites().size().equals(0);
    }
}
