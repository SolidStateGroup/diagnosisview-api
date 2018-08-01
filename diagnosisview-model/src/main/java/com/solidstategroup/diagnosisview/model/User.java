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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
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
public class User {
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

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String password;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String salt;

    @Column
    private Date expiryDate;

    @Column
    private Date dateCreated;

    @Type(type = "SavedUserCodeFieldArrayType")
    private List<SavedUserCode> favourites;

    @Type(type = "SavedUserCodeFieldArrayType")
    private List<SavedUserCode> history;

    @Type(type = "PaymentFieldArrayType")
    private List<PaymentDetails> paymentData;


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
     * Get the profile path.
     *
     * @return the profile iamge path
     */
    public String getProfileImagePath() {
        return String.format("/api/profile/image/%s", getUsername());
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
