package com.solidstategroup.diagnosisview.model.codes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Institution model represents a Lookup of a type (LookupType) Institution.
 * <p>
 * Created by Pavlo Maksymchuk.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Institution extends LookupWrapper {
    private static final String IMAGE_URL_TEMPLATE = "/api/institutions/%s/logo";
    public static final String LOGO_DATA_FIELD = "logoData";
    public static final String IMAGE_FORMAT_FIELD = "imageFormat";
    public static final String HIDDEN_FIELD = "hidden";

    // Used when FE sends content for logo
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String logoImage;

    private String logoData;
    private String imageFormat;
    private String logoUrl;
    private boolean hidden = false;
    private Map<String, Object> stats;

    public Institution() {
        super();
    }

    public Institution(Lookup lookup) {
        super(lookup);

        // extra data is stored as map in jsonb field
        // against lookup table
        if (!CollectionUtils.isEmpty(lookup.getData())) {
            if (lookup.getData().containsKey(HIDDEN_FIELD)) {
                hidden = (boolean) lookup.getData().get(HIDDEN_FIELD);
            }
            if (lookup.getData().containsKey(LOGO_DATA_FIELD)) {
                logoData = (String) lookup.getData().get(LOGO_DATA_FIELD);
                // if no image dont return url
                logoUrl = String.format(IMAGE_URL_TEMPLATE, getId());
            }
            if (lookup.getData().containsKey(IMAGE_FORMAT_FIELD)) {
                imageFormat = (String) lookup.getData().get(IMAGE_FORMAT_FIELD);
            }
        }
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public String getLogoData() {
        return logoData;
    }

    public void setLogoData(String logoData) {
        this.logoData = logoData;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
