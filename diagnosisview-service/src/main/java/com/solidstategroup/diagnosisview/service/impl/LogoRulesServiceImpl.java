package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.repository.LinkLogoRuleRepository;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class LogoRulesServiceImpl implements LogoRulesService {

    private final LinkLogoRuleRepository linkLogoRuleRepository;

    public LogoRulesServiceImpl(LinkLogoRuleRepository linkLogoRuleRepository) {

        this.linkLogoRuleRepository = linkLogoRuleRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LogoRule add(LogoRuleDto logoRuleDto) throws UnsupportedEncodingException {

        return linkLogoRuleRepository.save(LogoRule
                .builder()
                .linkLogo(Base64.decodeBase64(logoRuleDto.getImage().getBytes("UTF-8")))
                .logoFileType(logoRuleDto.getImageFormat())
                .startsWith(logoRuleDto.getStartsWith())
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogoRule getLogoRule(String id) {

        return linkLogoRuleRepository.findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogoRule> getLogoRules() {

        return linkLogoRuleRepository.findAll();
    }

    @Override
    public void deleteLogoRule(String id) {

        linkLogoRuleRepository.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogoRule updateLogoRule(String id, LogoRuleDto logoRuleDto) throws Exception {

        LogoRule current = linkLogoRuleRepository.findOne(id);

        if (current == null) {
            throw new Exception();
        }

        LogoRule
                .builder()
                .linkLogo(Base64.decodeBase64(logoRuleDto.getImage().getBytes("UTF-8")))
                .logoFileType(logoRuleDto.getImageFormat())
                .startsWith(logoRuleDto.getStartsWith())
                .id(id)
                .build();

        return linkLogoRuleRepository.save(current);
    }
}
