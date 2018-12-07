package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.LinkLogoDto;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Link Rules service provides rule for urls that can be customized on
 * an institutional basis.
 */
public interface LinkRulesService {

    /**
     * Adds a link rule for an institution.
     *
     * @param linkRuleDto Request to add a link rule
     * @return Saved link rule
     */
    LinkRule addRule(LinkRuleDto linkRuleDto);


    /**
     * Adds a link rule for an institution.
     *
     * @param linkLogoDto Request to add a link logo rule
     * @return Saved link logo rule
     */
    LinkLogoRule addLogoRule(LinkLogoDto linkLogoDto) throws UnsupportedEncodingException;


    /**
     * Returns all {@link LinkRule} objects currently saved in repository.
     *
     **/
    List<LinkRule> getLinkRules();

    /**
     * @param id Id of link rule
     * @return {@link LinkRule} with matching id
     */
    LinkRule getLinkRule(String id);


    /**
     * @param id Id of link logo rule
     * @return {@link LinkLogoRule} with matching id
     */
    LinkLogoRule getLinkLogoRule(String id);

    /**
     * Update a given link transformation
     * @param id
     * @param linkTransformation
     * @return
     * @throws Exception
     */
    LinkRule updateLinkRule(String id, LinkRuleDto linkTransformation) throws Exception;

    /**
     * Update a link logo rule
     * @param id
     * @param linkLogoDto
     * @return
     * @throws Exception
     */
    LinkLogoRule updateLogoRule(String id, LinkLogoDto linkLogoDto) throws Exception;

    /**
     * Removes link rule.
     *
     * @param id Id of link rule to remove.
     */
    void deleteLinkRule(String id);


    /**
     * Removes link lgoo rule.
     *
     * @param id Id of link logo ule to remove.
     */
    void deleteLinkLogoRule(String id);
}
