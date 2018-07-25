package com.solidstategroup.diagnosisview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.User;
import com.solidstategroup.diagnosisview.service.CodeSyncService;
import com.solidstategroup.diagnosisview.service.UserService;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Secured API controller, handles main methods.
 */
@RestController
@RequestMapping("/api/admin")
@Log
public class TempAdminApiController {

    private ObjectMapper objectMapper = new ObjectMapper();
    private CodeSyncService codeSyncService;

    /**
     * Instantiate API controller, includes required services.
     *
     * @param codeSyncService     CodeSync service
     */
    @Autowired
    public TempAdminApiController(final CodeSyncService codeSyncService) {
        this.codeSyncService = codeSyncService;
    }


    /**
     * Sync the content from PV
     *
     * @throws Exception
     */
    @RequestMapping(value = "/sync", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void syncContent()
            throws Exception {
        codeSyncService.syncCodes();
    }
}
