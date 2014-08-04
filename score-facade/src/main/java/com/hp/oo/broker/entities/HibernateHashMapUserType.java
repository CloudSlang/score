package com.hp.oo.broker.entities;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 1/30/12
 * Time: 10:05 AM
 */
public class HibernateHashMapUserType implements UserType {

    @SuppressWarnings("unchecked")
    @Override
    public int[] sqlTypes() {
        return new int[]{Types.CLOB};
    }

    @Override
    public Class returnedClass() {
        return HashMap.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y || !(null == x || null == y) && x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();

    }

    @Override
    public Object nullSafeGet(ResultSet aResultSet, String[] aStrings, SessionImplementor session, Object aObject) throws HibernateException, SQLException {
        String str = aResultSet.getString(aStrings[0]);
        if (str != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
            HashMap map;
            try {
                map = mapper.readValue(str, HashMap.class);
            } catch (Exception ex) {
                throw new HibernateException(ex);
            }
            return map;
        }
        return null;
    }


    @Override
    public void nullSafeSet(PreparedStatement aPreparedStatement, Object aObject, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (aObject == null) {
            aPreparedStatement.setNull(index, Types.VARCHAR);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            String jsonString;
            try {
                jsonString = mapper.writeValueAsString(aObject);
            } catch (Exception ex) {
                throw new HibernateException(ex);
            }
            aPreparedStatement.setString(index, jsonString);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return serializable;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}