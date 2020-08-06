package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Add generics for enum http://www.gabiaxel.com/2011/01/better-enum-mapping-with-hibernate.html
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_lookup_value")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Lookup extends AuditModel {

    @Column(name = "value")
    private String value;

    @Column(name = "description")
    private String description;

    @Column(name = "description_friendly")
    private String descriptionFriendly;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lookup_type_id")
    private LookupType lookupType;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(name = "dv_only")
    private Boolean dvOnly;

    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data = new HashMap<>();

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public LookupType getLookupType() {
        return lookupType;
    }

    public void setLookupType(final LookupType lookupType) {
        this.lookupType = lookupType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionFriendly() {
        return descriptionFriendly;
    }

    public void setDescriptionFriendly(String descriptionFriendly) {
        this.descriptionFriendly = descriptionFriendly;
    }

    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getDvOnly() {
        if (dvOnly == null) {
            return false;
        }
        return dvOnly;
    }

    public void setDvOnly(Boolean dvOnly) {
        this.dvOnly = dvOnly;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
