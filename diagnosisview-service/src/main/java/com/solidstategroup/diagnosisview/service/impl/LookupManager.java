package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lookup service, used to get Lookups, referenced by other objects for static data.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@Service
public class LookupManager {
    private final LookupRepository lookupRepository;
    private final LookupTypeRepository lookupTypeRepository;

    @Autowired
    private LookupManager(final LookupRepository lookupRepository,
                          LookupTypeRepository lookupTypeRepository) {
        this.lookupRepository = lookupRepository;
        this.lookupTypeRepository = lookupTypeRepository;
    }

    public Lookup findByTypeAndValue(final LookupTypes lookupType, final String lookupValue)
            throws ResourceNotFoundException {
        return lookupRepository.findByTypeAndValue(lookupType, lookupValue)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Lookup value"));
    }

    public List<Lookup> findByType(final LookupTypes lookupType) {
        return lookupRepository.findByType(lookupType);
    }
}
