package com.solidstategroup.diagnosisview.service.impl;

import com.solidstategroup.diagnosisview.exceptions.ResourceNotFoundException;
import com.solidstategroup.diagnosisview.model.TagDto;
import com.solidstategroup.diagnosisview.model.Tag;
import com.solidstategroup.diagnosisview.model.codes.enums.LookupTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper Service to manage Tags Lookup types.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@Service
public class TagsService {

    private final LookupManager lookupManager;

    @Autowired
    public TagsService(final LookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    /**
     * Find Tag by given lookup value
     *
     * @param value
     * @return
     * @throws ResourceNotFoundException
     */
    public Tag getTag(String value) throws ResourceNotFoundException {
        return new Tag(lookupManager.findByTypeAndValue(LookupTypes.TAG_TYPES, value));
    }

    /**
     * Get a list of Tags in the system
     *
     * @return
     */
    public List<TagDto> getTagConfigs() {
        return lookupManager.findByType(LookupTypes.TAG_TYPES).stream()
                .map(TagDto::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
