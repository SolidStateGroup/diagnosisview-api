package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity class for holding rules and logos.
 */
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dv_link_logo_rule")
public class LogoRule {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    @Column(name = "link_logo")
    @JsonIgnore
    private byte[] linkLogo;

    //Used when Frontend sends content up
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String logoData;

    @Column(name = "starts_with")
    private String startsWith;

    @Column(name = "link_logo_filetype")
    private String logoFileType;
}
