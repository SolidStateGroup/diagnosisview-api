package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.model.codes.Link;

import java.util.Set;

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
    Link update(Link link) throws Exception;

    /**
     * Creates a new link if link does not already
     * exist or updates a link.
     *
     * @param link     new link or updated link
     * @param fromSync a flag to indicate if the upsert triggered from sync job
     * @return created/updated link
     */
    Link upsert(Link link, boolean fromSync);

    Link addExternalLink(Link link, Code code) throws Exception;

    Link updateExternalLink(Link link) throws Exception;

    void updateExternalLinks(Link link) throws Exception;

    /**
     * Helper to check Links order
     *
     * Rules as follow:
     * order 1-9 for Green
     * order 11-19 for Amber
     * order 21-29 for Red
     *
     * We need to make sure link order is within difficulty range
     * and also that order number is unique per difficulty level.
     *
     * @param links a list of Link to check
     * @param code  a code to check links against
     * @throws Exception when one fo the links order is incorrect
     */
    void checkLinksOrder(Set<Link> links, Code code) throws Exception;
}
