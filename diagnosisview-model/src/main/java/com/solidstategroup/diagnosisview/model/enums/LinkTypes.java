package com.solidstategroup.diagnosisview.model.enums;

/**
 * Types of Links mapped to what stored in database
 */
public enum LinkTypes {
    NHS_CHOICES {
        @Override
        public long id() {
            return 135;
        }
    },
    MEDLINE_PLUS {
        @Override
        public long id() {
            return 136;
        }
    },
    CUSTOM {
        @Override
        public long id() {
            return 137;
        }
    },
    NICE_CKS {
        @Override
        public long id() {
            return 138;
        }
    },
    BMJ {
        @Override
        public long id() {
            return 987654;
        }
    };

    public abstract long id();
}
