package com.solidstategroup.diagnosisview.model;

import com.solidstategroup.diagnosisview.model.codes.Lookup;
import com.solidstategroup.diagnosisview.model.codes.LookupWrapper;

/**
 * <p>
 * Tag model represents a Lookup of a type (LookupType) Tag.
 * </p>
 *
 * Created by Pavlo Maksymchuk.
 */
public class Tag extends LookupWrapper {
    public Tag() {
        super();
    }

    public Tag(Lookup lookup) {
        super(lookup);
    }
}
