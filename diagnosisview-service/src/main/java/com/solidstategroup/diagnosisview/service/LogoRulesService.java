package com.solidstategroup.diagnosisview.service;

import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

/**
 * Logo Rules service allows logo images to be stored along with url rule
 * metadata.
 */
public interface LogoRulesService {

    /**
     * Adds a logo rule.
     *
     * @param logoRuleDto Request to add a logo rule
     * @return Saved logo rule
     */
    LogoRule add(LogoRuleDto logoRuleDto) throws UnsupportedEncodingException;

    /**
     * Fetches a {@link LogoRule} by an id.
     *
     * @param id Id of logo rule
     * @return {@link LogoRule} with matching id
     */
    LogoRule get(String id);

    /**
     * Returns all {@link LogoRule} objects currently saved in repository.
     **/
    List<LogoRule> getRules();

    /**
     * Removes logo rule.
     *
     * @param id Id of logo rule to remove.
     */
    void delete(String id);

    /**
     * Update a given logo rule.
     *
     * @param id          Id of stored logo rule.
     * @param logoRuleDto Updates to logo rule
     * @return Updated logo rule
     * @throws Exception thrown if logo rule cannot be found
     */
    LogoRule update(String id, LogoRuleDto logoRuleDto) throws Exception;

    /**
     *
     * @param link
     * @return Optional {@link LogoRule} that matches link.
     */
    Optional<LogoRule> matchLinkToRule(Link link);
}
