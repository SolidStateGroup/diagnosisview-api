package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.service.LinkRulesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

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

    @Override
    public LinkRule addRule(LinkRuleDto linkRuleDto) {

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

    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }

    @Override
    public List<LinkRule> getLinkRules() {

        return linkRuleRepository.findAll();
    }

    @Override
    public LinkRule getLinkRule(String uuid) {

        return linkRuleRepository.getOne(uuid);
    }

    @Override
    public LinkRule updateLinkTransformation(String uuid, LinkRuleDto linkRuleDto)
            throws Exception {

        LinkRule current = linkRuleRepository.findOne(uuid);

        if (current == null) {
            throw new Exception();
        }

        current.setInstitution(linkRuleDto.getInstitution());
        current.setTransform(linkRuleDto.getTransformation());
        current.setLink(linkRuleDto.getLink());

        return linkRuleRepository.save(current);
    }

    @Override
    public void deleteLinkRule(String uuid) {

        linkRuleRepository.delete(uuid);
    }
}
