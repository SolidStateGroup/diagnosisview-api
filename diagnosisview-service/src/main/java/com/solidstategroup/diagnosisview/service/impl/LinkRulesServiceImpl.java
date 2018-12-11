package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.service.LinkRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of {@link LinkRulesService}. Allows new rules to be
 * added and to current rules to be fetched, updated and deleted. Also
 * evicts the code cache.
 */
@Service
public class LinkRulesServiceImpl implements LinkRulesService {

    private final LinkRuleRepository linkRuleRepository;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;

    public LinkRulesServiceImpl(LinkRuleRepository linkRuleRepository,
                                LinkRuleMappingRepository linkRuleMappingRepository,
                                LinkRepository linkRepository) {

        this.linkRuleRepository = linkRuleRepository;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
        this.linkRepository = linkRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule add(LinkRuleDto linkRuleDto) {

        LinkRule linkRule = linkRuleRepository.save(LinkRule
                .builder()
                .transform(linkRuleDto.getTransformation())
                .link(linkRuleDto.getLink())
                .institution(linkRuleDto.getInstitution())
                .build());

        Set<LinkRuleMapping> linkRuleMappings =
                linkRepository
                        .findLinksByLinkIsLike("%" + linkRule.getLink() + "%")
                        .stream()
                        .map(link ->
                                LinkRuleMapping
                                        .builder()
                                        .link(link)
                                        .rule(linkRule)
                                        .institution(linkRuleDto.getInstitution())
                                        .replacementLink(transformLink(
                                                link.getLink(), linkRule.getTransform(), linkRule.getLink()))
                                        .build())
                        .collect(toSet());

        linkRuleMappingRepository.save(linkRuleMappings);

        return linkRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkRule getLinkRule(String uuid) {

        return linkRuleRepository.getOne(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LinkRule> getLinkRules() {

        return linkRuleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkRule updateLinkRule(String id, LinkRuleDto linkRuleDto)
            throws Exception {

        LinkRule current = linkRuleRepository.findOne(id);

        if (current == null) {
            throw new Exception();
        }

        current.setInstitution(linkRuleDto.getInstitution());
        current.setTransform(linkRuleDto.getTransformation());
        current.setLink(linkRuleDto.getLink());

        return linkRuleRepository.save(current);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void deleteLinkRule(String uuid) {
        linkRuleRepository.delete(uuid);
    }

    /**
     * Applies rule to link.
     */
    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }
}
