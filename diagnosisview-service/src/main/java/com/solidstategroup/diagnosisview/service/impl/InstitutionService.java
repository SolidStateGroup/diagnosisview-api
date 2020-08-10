package com.solidstategroup.diagnosisview.service.impl;

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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Creates new Institution lookup type in the system.
     *
     * @param payload
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Institution create(Institution payload) throws ResourceNotFoundException {

        Lookup lookup = lookupManager.create(toLookupEntity(payload));
        return new Institution(lookup);
    }

    /**
     * Get existing Institution lookup by given id.
     *
     * @param id an id of the Lookup to update
     * @return an Institution
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Institution get(final Long id) throws ResourceNotFoundException {
        return new Institution(lookupManager.get(id));
    }

    /**
     * Update existing Institution lookup.
     *
     * @param id      an id of the Lookup to update
     * @param payload
     * @return an updated Institution
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Institution update(final Long id, Institution payload) throws ResourceNotFoundException {
        Lookup current = lookupManager.get(id);

        Lookup lookup = toLookupEntity(payload);
        lookup.setLastUpdate(new Date());

        // if we are not replacing logo
        // set data to existing logo
        if (payload.getLogoImage() == null) {
            if (!CollectionUtils.isEmpty(current.getData())) {
                lookup.getData().put(Institution.LOGO_DATA_FIELD, current.getData().get(Institution.LOGO_DATA_FIELD));
                lookup.getData().put(Institution.IMAGE_FORMAT_FIELD, current.getData().get(Institution.IMAGE_FORMAT_FIELD));
            }
        }

        Lookup updated = lookupManager.update(id, lookup);
        return new Institution(updated);
    }

    /**
     * Delete existing Institution lookup by given id.
     *
     * @param id an id of the Lookup to delete
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(final Long id) throws ResourceNotFoundException {
        lookupManager.delete(id);
    }

    /**
     * Delete existing Institution logo image by given id.
     *
     * @param id an id of the Lookup to delete
     * @throws ResourceNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteLogo(final Long id) throws ResourceNotFoundException {
        Lookup current = lookupManager.get(id);
        // remove image keys from the data
        if (!CollectionUtils.isEmpty(current.getData())) {
            current.getData().entrySet().removeIf(d -> d.getKey().equals(Institution.LOGO_DATA_FIELD) &&
                    d.getKey().equals(Institution.IMAGE_FORMAT_FIELD));
        }
        lookupManager.update(id, current);
    }

    private Lookup toLookupEntity(Institution institution) throws ResourceNotFoundException {
        LookupType lookupType = lookupManager.getLookupTypeByType(LookupTypes.INSTITUTION_TYPE);

        Lookup lookup = new Lookup();
        lookup.setValue(institution.getCode());
        lookup.setDescription(institution.getDescription());
        lookup.setLookupType(lookupType);
        // lookup.setDvOnly(true);

        // populate json data
        Map<String, Object> data = new HashMap<>();
        data.put(Institution.HIDDEN_FIELD, institution.isHidden());
        if (!StringUtils.isEmpty(institution.getLogoImage())) {
            // store image as base64 field
            data.put(Institution.LOGO_DATA_FIELD, institution.getLogoImage());
        }
        if (!StringUtils.isEmpty(institution.getImageFormat())) {
            data.put(Institution.IMAGE_FORMAT_FIELD, institution.getImageFormat());
        }
        lookup.setData(data);

        return lookup;
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
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get a list of Institutions settings, used in FE for drop down select
     *
     * @return a list of InstitutionDto
     */
    public List<InstitutionDto> getInstitutionsConfigs() {
        return this.getAll().stream()
                // .filter(institution -> institution.getHidden() == false) // FE does filtering
                .map(institution -> new InstitutionDto(institution.getCode(),
                        institution.getDescription(),
                        institution.isHidden(),
                        institution.getLogoUrl()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
