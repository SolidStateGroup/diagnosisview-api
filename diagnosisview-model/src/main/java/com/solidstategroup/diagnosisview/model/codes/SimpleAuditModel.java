package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.solidstategroup.diagnosisview.model.BaseModel;
import com.solidstategroup.diagnosisview.model.User;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@MappedSuperclass
public abstract class SimpleAuditModel extends BaseModel {

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

    protected SimpleAuditModel() {
    }

    public Date getCreated() {
        if (created == null) {
            return null;
        }

        // account for timezone when showing in ui
        DateTimeZone dateTimeZone = DateTimeZone.UTC;
        DateTime dateTime = new DateTime(dateTimeZone.convertLocalToUTC(created.getTime(), true));

        return dateTime.toDate();
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    public void setCreator(final User creator) {
        this.creator = creator;
    }


    @PrePersist
    public void prePersist() {
        if (created == null) {
            created = new Date();
        }
    }

}

