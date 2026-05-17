package com.ecm.common.enums;

/**
 * The data type of a single attribute definition.
 *
 * <p>Used by DocumentAttributeService to validate incoming attribute values
 * before they are persisted. The type determines which validation rule applies
 * (pattern match, numeric range check, date parse, etc.).</p>
 *
 * <p>Not responsible for: the actual validation logic — that is owned by
 * DocumentAttributeService in ecm-library-service.</p>
 */
public enum AttributeDataType {

    /** Free-form text. Length constraints defined per attribute definition. */
    STRING,

    /** Integer or decimal number. Range constraints defined per attribute definition. */
    NUMBER,

    /** ISO-8601 date (yyyy-MM-dd). No time component. */
    DATE,

    /** ISO-8601 datetime (yyyy-MM-dd'T'HH:mm:ssZ). Stored with timezone. */
    DATETIME,

    /** True / false. Accepted values: "true", "false" (case-insensitive). */
    BOOLEAN,

    /**
     * One value from a fixed allowed-values list defined in the attribute definition.
     * The allowed values list is stored in attribute_definition.allowed_values as a
     * comma-separated string.
     */
    ENUM
}
