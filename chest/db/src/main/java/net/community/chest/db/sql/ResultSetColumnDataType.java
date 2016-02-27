/*
 * 
 */
package net.community.chest.db.sql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.ClassNameMap;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * An {@link Enum} used to represent the possible {@link ResultSet} built-in
 * <I>get</I>-value methods.
 * 
 * @author Lyor G.
 * @since Apr 27, 2010 9:20:09 AM
 */
public enum ResultSetColumnDataType {
	BYTE(Byte.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Byte getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Byte.valueOf(rs.getByte(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Byte getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Byte.valueOf(rs.getByte(columnLabel));
			}
		},
	SHORT(Short.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Short getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Short.valueOf(rs.getShort(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Short getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Short.valueOf(rs.getShort(columnLabel));
			}
		},
	INTEGER(Integer.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Integer getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Integer.valueOf(rs.getInt(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Integer.valueOf(rs.getInt(columnLabel));
			}
		},
	LONG(Long.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Long getData (ResultSet rs, int columnIndex) throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Long.valueOf(rs.getLong(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Long getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Long.valueOf(rs.getLong(columnLabel));
			}
		},
	FLOAT(Float.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Float getData (ResultSet rs, int columnIndex) throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Float.valueOf(rs.getFloat(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Float.valueOf(rs.getFloat(columnLabel));
			}
		},
	DOUBLE(Double.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Double getData (ResultSet rs, int columnIndex) throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);
	
				return Double.valueOf(rs.getDouble(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Double getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Double.valueOf(rs.getDouble(columnLabel));
			}
		},
	STRING(String.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public String getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);

				return rs.getString(columnIndex);
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public String getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return rs.getString(columnLabel);
			}
		},
	BOOLEAN(Boolean.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Boolean getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);

				return Boolean.valueOf(rs.getBoolean(columnIndex));
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Boolean getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return Boolean.valueOf(rs.getBoolean(columnLabel));
			}
		},
	DATE(Date.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Date getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);

				return rs.getDate(columnIndex);
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Date getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return rs.getDate(columnLabel);
			}
		},
	TIME(Time.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Time getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);

				return rs.getTime(columnIndex);
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Time getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return rs.getTime(columnLabel);
			}
		},
	TIMESTAMP(Timestamp.class) {
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, int)
			 */
			@Override
			@CoVariantReturn
			public Timestamp getData (ResultSet rs, int columnIndex)
					throws SQLException
			{
				if (null == rs)
					throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for col=" + columnIndex);

				return rs.getTimestamp(columnIndex);
			}
			/*
			 * @see net.community.chest.db.sql.ResultSetColumnDataType#getData(java.sql.ResultSet, java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Timestamp getData (ResultSet rs, String columnLabel)
					throws SQLException
			{
				if ((null == columnLabel) || (columnLabel.length() <= 0))
					throw new SQLException(name() + " - No extraction colunmn label specified");
				if (null == rs)
					throw new SQLException(name() + " - No " + ResultSet.class.getSimpleName() + " instance for col=" + columnLabel);
				
				return rs.getTimestamp(columnLabel);
			}
		};

	private final Class<?>	_valClass;
 	/**
	 * @return {@link Class} of converted values
	 */
	public final Class<?> getValuesClass ()
	{
		return _valClass;
	}

	ResultSetColumnDataType (Class<?> valClass)
	{
		_valClass = valClass;
	}
	/**
	 * Extracts the data by invoking the {@link ResultSet} appropriate <I>get</I>-ter
	 * @param rs The {@link ResultSet}
	 * @param columnIndex The column whose data is to be extracted
	 * @return The extracted data
	 * @throws SQLException If DB error
	 */
	public abstract Object getData (ResultSet rs, int columnIndex) throws SQLException;
	/**
	 * Extracts the data by invoking the {@link ResultSet} appropriate <I>get</I>-ter
	 * @param rs The {@link ResultSet}
	 * @param columnLabel The column label whose data is to be extracted
	 * @return The extracted data
	 * @throws SQLException If DB error
	 */
	public abstract Object getData (ResultSet rs, String columnLabel) throws SQLException;
	
	public static final List<ResultSetColumnDataType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ResultSetColumnDataType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	
	public static class ResultSetDataExtractorMap extends ClassNameMap<ResultSetColumnDataType>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -9114869888137330021L;
		public ResultSetDataExtractorMap initContents (final boolean ignoreDuplicates)
		{
			for (final ResultSetColumnDataType v : VALUES)
			{
				final Class<?>					vc=v.getValuesClass();
				final ResultSetColumnDataType	prev=put(vc, v);
				if (prev != null)
				{
					if (ignoreDuplicates)	// debug breakpoint
						throw new IllegalStateException("Multiple mappings for type=" + vc.getName() + ": " + v + "/" + prev);
				}
			}
			
			return this;
		}

		public ResultSetDataExtractorMap ()
		{
			super(String.CASE_INSENSITIVE_ORDER);
			initContents(false);
		}
		/*
		 * @see net.community.chest.util.map.ClassNameMap#get(java.lang.Class)
		 */
		@Override
		public ResultSetColumnDataType get (Class<?> c)
		{
			final Class<?>	cType=ClassUtil.getPrimitiveTypeEquivalent(c);
			if (cType != null)
				return super.get(cType);
			else if (java.util.Date.class.equals(c))
				return get(java.sql.Date.class);

			return super.get(c);
		}
	}
	
	public static final ResultSetDataExtractorMap DEFAULT_MAP=new ResultSetDataExtractorMap();
}
