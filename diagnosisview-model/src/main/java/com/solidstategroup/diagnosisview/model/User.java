package com.solidstategroup.diagnosisview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solidstategroup.diagnosisview.type.CustomFieldArrayType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Authenticated user object extracted from JWT and used as method argument in controllers.
 */
@Data
@Entity
@Table(name = "dashboard_user")
@TypeDefs({@TypeDef(name = "CustomFieldArrayTypeObject", typeClass = CustomFieldArrayType.class)})
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String password;

    @Getter(AccessLevel.PRIVATE)
    @Column
    private String salt;

    @Column
    private String token;

    @Column
    private Date expiryDate;

    @Column
    private Date dateCreated;

    @Type(type = "CustomFieldArrayTypeObject")
    private CustomField[] customField;

    @Column(name = "profile_image")
    @JsonIgnore
    private byte[] profileImage;

    //Used when Frontend sends content up
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String logoData;

    @Column(name = "profile_image_type")
    private String profileImageFileType;

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
}
