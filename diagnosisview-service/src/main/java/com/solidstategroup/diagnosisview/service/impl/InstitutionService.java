package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.InstitutionDto;
import com.solidstategroup.diagnosisview.model.codes.Institution;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper Service to manage Institution Lookup types.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@Service
public class InstitutionService {

    private final LookupManager lookupManager;

    @Autowired
    public InstitutionService(final LookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    /**
     * Find Institution by given lookup value
     * @param value
     * @return
     * @throws ResourceNotFoundException
     */
    public Institution getInstitution(String value) throws ResourceNotFoundException {
        return new Institution(lookupManager.findByTypeAndValue(LookupTypes.INSTITUTION_TYPE, value));
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
