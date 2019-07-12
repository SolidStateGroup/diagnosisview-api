package com.solidstategroup.diagnosisview.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidstategroup.diagnosisview.model.PaymentDetails;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample CustomField JSONB type handler for custom postgres dialect.
 */
public class PaymentFieldArrayType implements UserType {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String JSONB_TYPE = "jsonb";

    @Override
    public final Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public final Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    @Override
    public final Serializable disassemble(final Object value) throws HibernateException {
        return (ArrayList<PaymentDetails>) this.deepCopy(value);
    }

    @Override
    public final boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public final int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public final boolean isMutable() {
        return true;
    }

    @Override
    public final Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session,
            final Object owner) throws HibernateException, SQLException {
        try {
            final String json = resultSet.getString(names[0]);
            if (json != null) {
                return OBJECT_MAPPER.readValue(json, new TypeReference<List<PaymentDetails>>() { });
            }
            return null;
        } catch (IOException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public final void nullSafeSet(final PreparedStatement statement, final Object value,
            final int index, final SessionImplementor session) throws HibernateException, SQLException {
        try {
            String json = null;
            if (value != null) {
                json = OBJECT_MAPPER.writeValueAsString(value);
            }
            PGobject pgo = new PGobject();
            pgo.setType(JSONB_TYPE);
            pgo.setValue(json);
            statement.setObject(index, pgo);
        } catch (JsonProcessingException e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public final Object replace(final Object original, final Object target, final Object owner)
            throws HibernateException {
        return original;
    }

    @Override
    public final Class<ArrayList> returnedClass() {
        return ArrayList.class;
    }

    @Override
    public final int[] sqlTypes() {
        return new int[] {
            Types.ARRAY
        };
    }
}
