package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;

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
     * Returns all {@link LinkRule} objects currently saved in repository.
     *
     **/
    List<LinkRule> getLinkRules();

    /**
     * @param id Id of link rule
     * @return {@link LinkRule} with matching id
     */
    LinkRule getLinkRule(String id);

    LinkRule updateLinkRule(String id, LinkRuleDto linkTransformation) throws Exception;

    /**
     * Removes link rule.
     *
     * @param id Id of link rule to remove.
     */
    void deleteLinkRule(String id);
}
