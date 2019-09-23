package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.Link;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LogoRulesServiceImpl implements LogoRulesService {

    private final LogoRuleRepository logoRuleRepository;

    public LogoRulesServiceImpl(LogoRuleRepository logoRuleRepository) {

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

        logoRuleRepository.addLogoRule(logoRule);

        return logoRule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogoRule get(String id) {

        return logoRuleRepository.findById(id).orElse(null);
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

        logoRuleRepository.clearLogoRule(
                logoRuleRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Could not find LogoRule")));

        logoRuleRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LogoRule update(String id, LogoRuleDto logoRuleDto) throws Exception {

        LogoRule current = logoRuleRepository.findById(id)
                .orElseThrow(() -> new Exception());

        LogoRule.LogoRuleBuilder builder = LogoRule.builder().id(id);

        builder.linkLogo(
                logoRuleDto.getImage() != null ?
                        decodeBase64Image(logoRuleDto.getImage()) :
                        current.getLinkLogo());

        builder.logoFileType(
                logoRuleDto.getImageFormat() != null ?
                        logoRuleDto.getImageFormat() :
                        current.getLogoFileType());

        builder.startsWith(
                logoRuleDto.getStartsWith() != null ?
                        logoRuleDto.getStartsWith() :
                        current.getStartsWith());

        builder.overrideDifficultyLevel(
                logoRuleDto.getOverrideDifficultyLevel() != null ?
                        logoRuleDto.getOverrideDifficultyLevel() :
                        current.getOverrideDifficultyLevel());

        LogoRule newLogoRule =
                logoRuleRepository.saveAndFlush(builder.build());

        // If startsWith string has changed update links
        String currentStartsWith = current.getStartsWith();

        if (!StringUtils.equals(currentStartsWith, logoRuleDto.getStartsWith())) {

            logoRuleRepository.clearLogoRule(newLogoRule);
            logoRuleRepository.addLogoRule(newLogoRule);
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

        if (link.getLink() == null) {

            return Optional.empty();
        }

        return this.logoRuleRepository
                .findAll()
                .stream()
                .filter(lr -> linkText.startsWith(lr.getStartsWith()))
                .findFirst();
    }
}
