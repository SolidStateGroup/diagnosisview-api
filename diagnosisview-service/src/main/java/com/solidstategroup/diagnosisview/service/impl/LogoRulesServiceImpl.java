package com.solidstategroup.diagnosisview.service.impl;

import com.google.api.client.util.Base64;
import com.solidstategroup.diagnosisview.model.LogoRuleDto;
import com.solidstategroup.diagnosisview.model.codes.LogoRule;
import com.solidstategroup.diagnosisview.repository.LogoRuleRepository;
import com.solidstategroup.diagnosisview.service.LogoRulesService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class LogoRulesServiceImpl implements LogoRulesService {

    private final LogoRuleRepository logoRuleRepository;

    public LogoRulesServiceImpl(LogoRuleRepository logoRuleRepository) {

        this.logoRuleRepository = logoRuleRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "getAllCodes", allEntries = true)
    public LogoRule add(LogoRuleDto logoRuleDto) throws UnsupportedEncodingException {

        return logoRuleRepository.save(LogoRule
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

        return logoRuleRepository.findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LogoRule> getLogoRules() {

        return logoRuleRepository.findAll();
    }

    @Override
    public void deleteLogoRule(String id) {

        logoRuleRepository.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogoRule updateLogoRule(String id, LogoRuleDto logoRuleDto) throws Exception {

        LogoRule current = logoRuleRepository.findOne(id);

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

        return logoRuleRepository.save(current);
    }
}
