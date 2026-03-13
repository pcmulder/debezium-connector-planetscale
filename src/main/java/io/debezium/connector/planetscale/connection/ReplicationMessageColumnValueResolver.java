/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.planetscale.connection;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.connector.planetscale.VitessType;

/** Resolve raw column value to Java value */
public class ReplicationMessageColumnValueResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationMessageColumnValueResolver.class);
    private static final java.util.regex.Pattern ZERO_DATE_PATTERN = java.util.regex.Pattern.compile("^\\d{4}-00-00.*$");

    public static Object resolveValue(
                                      VitessType vitessType, ReplicationMessage.ColumnValue<byte[]> value, boolean includeUnknownDatatypes) {
        if (value.isNull()) {
            return null;
        }

        switch (vitessType.getJdbcId()) {
            case Types.SMALLINT:
                return value.asShort();
            case Types.INTEGER:
                if (vitessType.isEnum()) {
                    return vitessType.getEnumOrdinal(value.asString());
                }
                return value.asInteger();
            case Types.BIGINT:
                if (vitessType.isEnum()) {
                    return vitessType.getSetNumeral(value.asString());
                }
                return value.asLong();
            case Types.BLOB:
            case Types.BINARY:
                return value.asBytes();
            case Types.DATE:
                return stringToDate(value.asString());
            case Types.TIME:
                return Time.valueOf(value.asString());
            case Types.TIMESTAMP:
                return stringToTimestamp(value.asString());
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return value.asString();
            case Types.VARCHAR:
                return value.asString();
            case Types.FLOAT:
                return value.asFloat();
            case Types.DOUBLE:
                return value.asDouble();
            default:
                break;
        }

        return value.asDefault(vitessType, includeUnknownDatatypes);
    }

    private static Timestamp stringToTimestamp(String value) {
        if (ZERO_DATE_PATTERN.matcher(value).matches()) {
            LOGGER.warn("Invalid timestamp '{}' converted to null", value);
            return null;
        }
        return Timestamp.valueOf(value);
    }

    private static Date stringToDate(String value) {
        if (ZERO_DATE_PATTERN.matcher(value).matches()) {
            LOGGER.warn("Invalid date '{}' converted to null", value);
            return null;
        }
        return Date.valueOf(value);
    }
}
