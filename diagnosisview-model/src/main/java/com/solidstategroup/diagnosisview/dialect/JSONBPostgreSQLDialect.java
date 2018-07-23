package com.solidstategroup.diagnosisview.dialect;

import com.solidstategroup.diagnosisview.type.CustomFieldArrayType;
import org.hibernate.dialect.PostgreSQL94Dialect;

import java.sql.Types;

/**
 * Hibernate dialect for custom JSONB supporting postgres.
 */
public class JSONBPostgreSQLDialect extends PostgreSQL94Dialect {

    /**
     * Instantiate and register column types.
     */
    public JSONBPostgreSQLDialect() {
        super();
        registerColumnType(Types.ARRAY, CustomFieldArrayType.JSONB_TYPE);
    }
}
