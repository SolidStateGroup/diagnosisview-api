package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.LinkMappingDto;
import com.solidstategroup.diagnosisview.model.codes.LinkMapping;

import java.util.List;

/**
 * Link Mapping service provides mapping for urls that can be customized on
 * an institutional basis.
 */
public interface LinkMappingService {

    /**
     * Adds a link mapping for an institution.
     *
     * @param linkMappingDto Request to add a link mapping
     * @return Saved link mapping
     */
    LinkMapping addLinkTransformation(LinkMappingDto linkMappingDto);

    /**
     * Returns all {@link LinkMapping} objects currently saved in repository.
     *
     **/
    List<LinkMapping> getLinkTransformations();

    /**
     * @param id Id of link mapping
     * @return {@link LinkMapping} with matching id
     */
    LinkMapping getLinkTransformation(String id);

    LinkMapping updateLinkTransformation(String uuid, LinkMappingDto linkTransformation) throws Exception;

    /**
     * Removes link mapping.
     *
     * @param id Id of link mapping to remove.
     */
    void deleteLinkTransformation(String id);
}
