package net.community.chest.db.sql;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link Types} values into {@link Enum}-s
 *
 * @author Lyor G.
 * @since May 21, 2008 4:00:49 PM
 */
public enum TypesEnum {
     BIT(Types.BIT),
     TINYINT(Types.TINYINT),
     SMALLINT(Types.SMALLINT),
     INTEGER(Types.INTEGER),
     BIGINT(Types.BIGINT),
     FLOAT(Types.FLOAT),
     REAL(Types.REAL),
     DOUBLE(Types.DOUBLE),
     NUMERIC(Types.NUMERIC),
     DECIMAL(Types.DECIMAL),
     CHAR(Types.CHAR),
     VARCHAR(Types.VARCHAR),
     LONGVARCHAR(Types.LONGVARCHAR),
     DATE(Types.DATE),
     TIME(Types.TIME),
     TIMESTAMP(Types.TIMESTAMP),
     BINARY(Types.BINARY),
     VARBINARY(Types.VARBINARY),
     LONGVARBINARY(Types.LONGVARBINARY),
     NULL(Types.NULL),
     OTHER(Types.OTHER),
     JAVA_OBJECT(Types.JAVA_OBJECT),
     DISTINCT(Types.DISTINCT),
     STRUCT(Types.STRUCT),
     ARRAY(Types.ARRAY),
     BLOB(Types.BLOB),
     CLOB(Types.CLOB),
     REF(Types.REF),
     DATALINK(Types.DATALINK),
     BOOLEAN(Types.BOOLEAN),
     /* JDK 1.6 types */
     ROWID(Types.ROWID),
     NCHAR(Types.NCHAR),
     NVARCHAR(Types.NVARCHAR),
     LONGNVARCHAR(Types.LONGNVARCHAR),
     NCLOB(Types.NCLOB),
     SQLXML(Types.SQLXML);

    private final int _type;
    public final int getTypeValue ()
    {
        return _type;
    }

    TypesEnum (int type)
    {
        _type = type;
    }

    public static final List<TypesEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final TypesEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final TypesEnum fromTypeValue (final int t)
    {
        for (final TypesEnum v : VALUES)
        {
            if ((v != null) && (v.getTypeValue() == t))
                return v;
        }

        return null;
    }
}
