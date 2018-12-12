package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;

import java.util.List;

public interface LinkService {

    /**
     * Get a link by the id
     *
     * @param id Id of the link being sent
     * @return the full found link
     */
    Link getLink(Long id);

    /**
     * Save a link with update fields.
     *
     * @param link the link to save
     * @return the updated link
     */
    Link saveLink(Link link);

    Link upsertLink(Link link);
}
