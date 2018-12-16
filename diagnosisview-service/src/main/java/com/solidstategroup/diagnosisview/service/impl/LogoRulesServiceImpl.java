package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.repository.LinkRepository;
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Service
public class LogoRulesServiceImpl implements LogoRulesService {

    private final LinkRepository linkRepository;
    private final LogoRuleRepository logoRuleRepository;

    public LogoRulesServiceImpl(
            LinkRepository linkRepository,
            LogoRuleRepository logoRuleRepository) {

        this.linkRepository = linkRepository;
        this.logoRuleRepository = logoRuleRepository;
    }

    private static byte[] decodeBase64Image(String image)
            throws UnsupportedEncodingException {

        return Base64.decodeBase64(image.getBytes("UTF-8"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LogoRule add(LogoRuleDto logoRuleDto) throws UnsupportedEncodingException {

        final LogoRule logoRule = logoRuleRepository.saveAndFlush(
                LogoRule
                        .builder()
                        .linkLogo(decodeBase64Image(logoRuleDto.getImage()))
                        .logoFileType(logoRuleDto.getImageFormat())
                        .startsWith(logoRuleDto.getStartsWith())
                        .overrideDifficultyLevel(logoRuleDto.getOverrideDifficultyLevel())
                        .build());

        linkRepository.addLogoRule(logoRule);

        return logoRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogoRule get(String id) {

        return logoRuleRepository.findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogoRule> getRules() {

        return logoRuleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public void delete(String id) {

        linkRepository.clearLogoRule(
                logoRuleRepository.findOne(id));

        logoRuleRepository.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LogoRule update(String id, LogoRuleDto logoRuleDto) throws Exception {

        LogoRule current = logoRuleRepository.findOne(id);

        if (current == null) {
            throw new Exception();
        }
        String currentStartsWith = current.getStartsWith();

        LogoRule newLogoRule = logoRuleRepository.saveAndFlush(
                LogoRule
                        .builder()
                        .linkLogo(decodeBase64Image(logoRuleDto.getImage()))
                        .logoFileType(logoRuleDto.getImageFormat())
                        .startsWith(logoRuleDto.getStartsWith())
                        .id(id)
                        .build());

        // If startsWith string has changed update links
        if (!StringUtils.equals(currentStartsWith, logoRuleDto.getStartsWith())) {

            linkRepository.clearLogoRule(newLogoRule);
            linkRepository.addLogoRule(newLogoRule);
        }

        return newLogoRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LogoRule> matchLinkToRule(Link link) {

        // Check if the link matches any urls for logos,
        // if it does, assign it that logo url
        if (link.getLogoRule() != null) {
            return Optional.empty();
        }

        String linkText = link.getLink();

        return logoRuleRepository
                .findAll()
                .stream()
                .filter(lr -> linkText.startsWith(lr.getStartsWith()))
                .findFirst();
    }
}
