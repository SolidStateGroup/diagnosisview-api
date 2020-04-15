package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.solidstategroup.diagnosisview.model.codes.enums.Institution;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Authenticated user object extracted from JWT and used as method argument in controllers.
 */
@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "dv_user")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @ApiModelProperty(required = true)
    private String username;

    @Column(name = "role_type")
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column
    private String occupation;

    @Column
    @Enumerated(EnumType.STRING)
    private Institution institution;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String emailAddress;

    @Column
    private String token;

    @Column
    private boolean activeSubscription;

    @Column
    private boolean deleted;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String password;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String salt;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String resetCode;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private Date resetExpiryDate;

    //Field used for android subscriptions
    //When set to false, the expiry date is used
    @Column
    private boolean autoRenewing;

    @Column
    private Date expiryDate;

    @Column
    private Date dateCreated;

    @Type(type = "jsonb")
    @Column(name = "favourites", columnDefinition = "jsonb")
    private List<SavedUserCode> favourites = new ArrayList<>();

    @Type(type = "jsonb")
    @Column(name = "history", columnDefinition = "jsonb")
    private List<SavedUserCode> history = new ArrayList<>();

    @Type(type = "jsonb")
    @Column(name = "payment_data", columnDefinition = "jsonb")
    @Builder.Default
    private List<PaymentDetails> paymentData = new ArrayList<>();

    @Transient
    private String oldPassword;

    /**
     * Gets the stored password.
     *
     * @return String stored password
     */
    @JsonIgnore
    public String getStoredPassword() {
        return password;
    }

    /**
     * Gets the stored salt.
     *
     * @return String stored salt
     */
    @JsonIgnore
    public String getStoredSalt() {
        return this.salt;
    }

    /**
     * Retruns the date the reset code expires
     *
     * @return Date reset code expiry date
     */
    @JsonIgnore
    public Date getResetExpiryDate() {
        return this.resetExpiryDate;
    }

    /**
     * Returns the reset code
     *
     * @return String the reset code
     */
    @JsonIgnore
    public String getResetCode() {
        return this.resetCode;
    }

    /**
     * Get the username
     *
     * @return The username
     */
    public String getUsername() {
        return this.username.toLowerCase().trim();
    }

    /**
     * Get the email address
     *
     * @return The email address
     */
    public String getEmailAddress() {
        return this.username.toLowerCase().trim();
    }

    /**
     * Get the type of user that is logging in.
     *
     * @return RoleType the role type
     */
    public RoleType getRoleType() {
        if (roleType == null) {
            return RoleType.USER;
        }
        return roleType;
    }
}
