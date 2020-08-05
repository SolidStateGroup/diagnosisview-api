package com.solidstategroup.diagnosisview.service.impl;

import com.google.common.collect.ImmutableMap;
import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.InstitutionDto;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.LookupType;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper Service to manage Institution Lookup types.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@Transactional(readOnly = true)
@Slf4j
@Service
public class InstitutionService {

    private final LookupManager lookupManager;

    @Autowired
    public InstitutionService(final LookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    /**
     * Find Institution by given lookup value
     *
     * @param value
     * @return
     * @throws ResourceNotFoundException
     */
    public Institution getInstitution(String value) throws ResourceNotFoundException {
        return new Institution(lookupManager.findByTypeAndValue(LookupTypes.INSTITUTION_TYPE, value));
    }

    /**
     * Find all Institutions in the system
     *
     * @return a List of Institutions
     */
    public List<Institution> getAll() {
        return lookupManager.findByType(LookupTypes.INSTITUTION_TYPE)
                .stream()
                .map(lookup -> {
                    Institution i = new Institution(lookup);
                    i.setStats(lookupManager.getInstitutionStats(i.getCode()));
                    return i;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a list of Institution
     *
     * @return
     */
    public List<InstitutionDto> getInstitutionsConfigs() {
        return lookupManager.findByType(LookupTypes.INSTITUTION_TYPE).stream()
                .map(InstitutionDto::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
