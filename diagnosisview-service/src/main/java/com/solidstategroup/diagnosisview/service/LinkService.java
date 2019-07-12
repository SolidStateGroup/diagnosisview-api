package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;

public interface LinkService {

    /**
     * Get a link by the id
     *
     * @param id Id of the link being sent
     * @return the full found link
     */
    Link get(Long id);

    /**
     * Updates link fields.
     *
     * @param link the link to update
     * @return the updated link
     */
    Link update(Link link);

    /**
     * Creates a new link if link does not already
     * exist or updates a link.
     *
     * @param link new link or updated link
     * @return created/updated link
     */
    Link upsert(Link link);


    Link addExternalLink(Link link, Code code) throws Exception;

    Link updateExternalLink(Link link) throws Exception;
}
