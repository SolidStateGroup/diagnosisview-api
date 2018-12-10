package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.model.LinkLogoDto;
import com.solidstategroup.diagnosisview.model.LinkRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LinkLogoRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRule;
import com.solidstategroup.diagnosisview.model.codes.LinkRuleMapping;
import com.solidstategroup.diagnosisview.repository.LinkLogoRuleRepository;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleMappingRepository;
import com.solidstategroup.diagnosisview.repository.LinkRuleRepository;
import com.solidstategroup.diagnosisview.service.LinkRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class LinkRulesServiceImpl implements LinkRulesService {

    private final LinkRuleRepository linkRuleRepository;
    private final LinkRuleMappingRepository linkRuleMappingRepository;
    private final LinkRepository linkRepository;
    private final LinkLogoRuleRepository linkLogoRuleRepository;

    public LinkRulesServiceImpl(LinkRuleRepository linkRuleRepository,
                                LinkRuleMappingRepository linkRuleMappingRepository,
                                LinkRepository linkRepository,
                                LinkLogoRuleRepository linkLogoRuleRepository) {

        this.linkRuleRepository = linkRuleRepository;
        this.linkRuleMappingRepository = linkRuleMappingRepository;
        this.linkRepository = linkRepository;
        this.linkLogoRuleRepository = linkLogoRuleRepository;
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
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


    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LinkLogoRule addLogoRule(LinkLogoDto linkLogoDto) throws UnsupportedEncodingException {

        return linkLogoRuleRepository.save(LinkLogoRule
                .builder()
                .linkLogo(Base64.decodeBase64(linkLogoDto.getImage().getBytes("UTF-8")))
                .logoFileType(linkLogoDto.getImageFormat())
                .startsWith(linkLogoDto.getStartsWith())
                .build());
    }

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

    @Override
    public LinkLogoRule updateLogoRule(String id, LinkLogoDto linkLogoDto) throws Exception {

        LinkLogoRule current = linkLogoRuleRepository.findOne(id);

        if (current == null) {
            throw new Exception();
        }

        LinkLogoRule
                .builder()
                .linkLogo(Base64.decodeBase64(linkLogoDto.getImage().getBytes("UTF-8")))
                .logoFileType(linkLogoDto.getImageFormat())
                .startsWith(linkLogoDto.getStartsWith())
                .id(id)
                .build();

        return linkLogoRuleRepository.save(current);
    }

    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void deleteLinkRule(String uuid) {
        linkRuleRepository.delete(uuid);
    }

    @Override
    public void deleteLinkLogoRule(String id) {
        linkLogoRuleRepository.delete(id);
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
    public LinkLogoRule getLinkLogoRule(String id) {
        return linkLogoRuleRepository.findOne(id);
    }

    private String transformLink(String original, String transform, String url) {
        return original.replace(url, transform);
    }

    @Override
    public List<LinkLogoRule> getLinkLogoRules() {
        return linkLogoRuleRepository.findAll();
    }
}
