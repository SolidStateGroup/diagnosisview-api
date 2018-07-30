package com.solidstategroup.diagnosisview.api.controller;

import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class BaseRepository {
    @Autowired
    private UserService userService;


    /**
     * Get the user based on the user token.
     *
     * @param request
     * @return User the user or null
     */
    protected User getUserFromRequest(final HttpServletRequest request) throws Exception {
        return userService.getUserByToken(getToken(request));
    }




    /**
     * Get token from X-Auth-Token or fallback to ?token=XXX query parameter.
     *
     * @param request ServletRequest to get token from
     * @return String token value
     */
    protected String getToken(final HttpServletRequest request) {
        String token = request.getHeader("X-Auth-Token");

        if (StringUtils.isEmpty(token) || "undefined".equals(token)) {
            // not in header
            token = request.getParameter("token");
        }

        return token;
    }

}
