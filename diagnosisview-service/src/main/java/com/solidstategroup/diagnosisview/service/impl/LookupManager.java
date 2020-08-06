package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.LookupType;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import com.solidstategroup.diagnosisview.repository.LookupRepository;
import com.solidstategroup.diagnosisview.repository.LookupTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Lookup service, used to get Lookups, referenced by other objects for static data.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@Transactional(readOnly = true)
@Slf4j
@Service
public class LookupManager {
    private static final String LOOKUP_VALUE_SEQ = "lookup_value_seq";
    private final LookupRepository lookupRepository;
    private final LookupTypeRepository lookupTypeRepository;

    private final EntityManager entityManager;

    @Autowired
    public LookupManager(final LookupRepository lookupRepository,
                         final LookupTypeRepository lookupTypeRepository,
                         final EntityManager entityManager) {
        this.lookupRepository = lookupRepository;
        this.lookupTypeRepository = lookupTypeRepository;
        this.entityManager = entityManager;
    }

    public Lookup findByTypeAndValue(final LookupTypes lookupType, final String lookupValue)
            throws ResourceNotFoundException {
        return lookupRepository.findByTypeAndValue(lookupType, lookupValue)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Lookup value"));
    }

    public List<Lookup> findByType(final LookupTypes lookupType) {
        return lookupRepository.findByType(lookupType);
    }

    /**
     * Creates new Lookup value in the system.
     *
     * @param lookup a Lookup to save
     * @return saved Lookup
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Lookup create(Lookup lookup) {

        // check make sure code for the same type does not exist
        lookupRepository.findByTypeAndValue(lookup.getLookupType().getType(), lookup.getValue())
                .ifPresent(l -> {
                    throw new EntityExistsException("Lookup with this code already exist");
                });

        lookup.setId(selectIdFrom(LOOKUP_VALUE_SEQ));

        return lookupRepository.save(lookup);
    }

    /**
     * Get a Lookup by given id
     *
     * @param id an id of the Lookup to search for
     * @return Lookup
     * @throws ResourceNotFoundException
     */
    public Lookup get(final Long id) throws ResourceNotFoundException {
        return lookupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Lookup value"));
    }

    /**
     * Update Lookup
     *
     * @param id   an id of Lookup to update
     * @param data an updated Lookup values
     * @return updated Lookup
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Lookup update(final Long id, Lookup data) throws ResourceNotFoundException {

        // make sure code is unique
        lookupRepository.findByTypeAndValue(data.getLookupType().getType(), data.getValue())
                .ifPresent(l -> {
                    // ignore if the same id
                    if (!l.getId().equals(id)) {
                        throw new EntityExistsException("Lookup with this code already exist");
                    }
                });

        Lookup lookup = lookupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Lookup value"));

        lookup.setValue(data.getValue());
        lookup.setDescription(data.getDescription());
        lookup.setData(data.getData());
        lookup.setLastUpdate(new Date());

        return lookupRepository.save(lookup);
    }

    /**
     * Delete Lookup
     *
     * @param id an id of Lookup to delete
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(final Long id) throws ResourceNotFoundException {

        lookupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find Lookup value"));

        lookupRepository.deleteById(id);
    }

    public Map<String, Object> getInstitutionStats(String code) {
        return lookupRepository.getInstitutionStats(code).get(0);
    }

    public LookupType getLookupTypeByType(LookupTypes type) throws ResourceNotFoundException {
        return lookupTypeRepository.findOneByType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find LookupType"));

    }

    private long selectIdFrom(String sequence) {
        String sql = "SELECT nextval('" + sequence + "')";
        return ((BigInteger) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
