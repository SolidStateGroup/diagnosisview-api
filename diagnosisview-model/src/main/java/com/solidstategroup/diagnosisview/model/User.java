package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solidstategroup.diagnosisview.model.enums.RoleType;
import com.solidstategroup.diagnosisview.type.PaymentFieldArrayType;
import com.solidstategroup.diagnosisview.type.SavedUserCodeArrayType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Authenticated user object extracted from JWT and used as method argument in controllers.
 */
@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "dv_user")
@TypeDefs({@TypeDef(name = "PaymentFieldArrayType", typeClass = PaymentFieldArrayType.class),
        @TypeDef(name = "SavedUserCodeFieldArrayType", typeClass = SavedUserCodeArrayType.class)})
public class User implements UserDetails {
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
    private String institution;

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

    @Column
    private String password;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String salt;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String resetCode;

    @JsonIgnore
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

    @Type(type = "SavedUserCodeFieldArrayType")
    @Column
    private List<SavedUserCode> favourites;

    @Type(type = "SavedUserCodeFieldArrayType")
    @Column
    private List<SavedUserCode> history;

    @Type(type = "PaymentFieldArrayType")
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

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList((GrantedAuthority) () -> "ROLE_" + roleType.toString());
    }

    /**
     * Get the username
     *
     * @return The username
     */
    public String getUsername() {
        return this.username.toLowerCase().trim();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
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
