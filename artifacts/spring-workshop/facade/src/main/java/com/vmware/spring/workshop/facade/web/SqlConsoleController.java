package com.vmware.spring.workshop.facade.web;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.vmware.spring.workshop.facade.beans.ScriptSubmissionBean;

/**
 * @author lgoldstein
 */
@Controller("sqlConsoleController")
@RequestMapping("/sql")
@SessionAttributes(types={ ScriptSubmissionBean.class })
public class SqlConsoleController extends AbstractWebController {
	private final DataSource	_dataSource;

	@Inject
	public SqlConsoleController(final DataSource dataSource) {
		_dataSource = dataSource;
	}

	private static final String	SUBMISSION_ATTR="scriptSubmissionBean";

	@RequestMapping(method=RequestMethod.GET)
	public String showConsole (final Model model) {
		model.addAttribute(SUBMISSION_ATTR, new ScriptSubmissionBean());
		return getTopLevelViewPath("console");
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/executeScript")
	public String executeScript (			final Model 				model,
	   	@ModelAttribute(SUBMISSION_ATTR)	final ScriptSubmissionBean	submissionBean,
	   										final SessionStatus 		status) {
        final String	sql=submissionBean.getScript();
		Assert.hasText(sql, "No SQL query provided");
        try {
        	if (_logger.isTraceEnabled())
        		_logger.trace("Executing SQL:\n" + sql);
            submissionBean.setResult(executeQuery(new StringBuilder(256), _dataSource, sql));
        } catch (Exception e) {
        	_logger.warn(e.getClass().getSimpleName() + " running SQL: " + e.getMessage(), e);
        	submissionBean.setResult("Exception:  (" + e.getClass().getName() + ") " + e.getMessage());
        }

        status.setComplete();
		return getTopLevelViewPath("console");
	}
	
	private <A extends Appendable> A executeQuery (final A sb, final DataSource dataSource, final String sql)
			throws SQLException, IOException {
		final Connection	conn=dataSource.getConnection();
		try {
			return executeQuery(sb, conn, sql);
		} finally {
			conn.close();
		}
	}

	private <A extends Appendable> A executeQuery (final A sb, final Connection conn, final String sql)
			throws SQLException, IOException {
		final Statement	stmt=conn.createStatement();
		try {
			if (!StringUtils.startsWithIgnoreCase(sql, "SELECT")) {
				final int	numRows=stmt.executeUpdate(sql);
				sb.append(String.valueOf(numRows)).append(" rows affected.");
				return sb;
			}

			final ResultSet									rs=stmt.executeQuery(sql);
			final ResultSetMetaData							metaData=rs.getMetaData();
			final List<? extends Map.Entry<String,Integer>>	resTypes=appendResultTitle(sb, metaData);
			final int										numCols=resTypes.size();
			while (rs.next()) {
				for (int	colIndex=1; colIndex <= numCols; colIndex++) {
					final Object	value=resolveColumnValue(rs, colIndex, resTypes.get(colIndex - 1));
					if (colIndex > 1)
						sb.append(',');
					sb.append(String.valueOf(value));
				}
				sb.append("\r\n");
			}
			
			return sb;
		} finally {
			stmt.close();
		}
	}

	private Object resolveColumnValue (final ResultSet rs, final int colIndex, final Map.Entry<String,Integer> typeInfo)
			throws SQLException, IOException {
		final String	name=typeInfo.getKey();
		final int		typeValue=typeInfo.getValue().intValue();
		switch(typeValue) {
			case Types.BIGINT 	:
				return Long.valueOf(rs.getLong(colIndex));
			case Types.BIT		:
			case Types.BOOLEAN	:
				return Boolean.valueOf(rs.getBoolean(colIndex));
			case Types.CHAR		:
				return Character.valueOf((char) rs.getShort(colIndex));
			case Types.DATE		:
				return rs.getDate(colIndex);
			case Types.DOUBLE	:
				return Double.valueOf(rs.getDouble(colIndex));
			case Types.FLOAT	:
				return Float.valueOf(rs.getFloat(colIndex));
			case Types.INTEGER	:
				return Integer.valueOf(rs.getInt(colIndex));
			case Types.NULL		:
				return null;
			case Types.SMALLINT	:
				return Short.valueOf(rs.getShort(colIndex));
			case Types.TIME		:
				return rs.getTime(colIndex);
			case Types.TIMESTAMP:
				return rs.getTimestamp(colIndex);
			case Types.TINYINT	:
				return Byte.valueOf(rs.getByte(colIndex));
			case Types.VARCHAR	:
				return rs.getString(colIndex);
			case Types.NCHAR	:
			case Types.NVARCHAR	:
			case Types.LONGNVARCHAR	:
				return rs.getNString(colIndex);
			default	:
				throw new StreamCorruptedException("appendColumnValue(" + name + ") Unknown type: " + typeValue);
		}
	}

	private List<Map.Entry<String,Integer>> appendResultTitle (final Appendable sb, final ResultSetMetaData	metaData)
			throws SQLException, IOException {
		final int	numCols=metaData.getColumnCount();
		if (numCols <= 0)	// can happen for updates, deletions, etc.
			return Collections.emptyList();

		final List<Map.Entry<String,Integer>>	result=new ArrayList<Map.Entry<String,Integer>>(numCols);	
		for (int	colIndex=1; colIndex <= numCols; colIndex++) {
			final String	name=metaData.getColumnLabel(colIndex);
			final int		type=metaData.getColumnType(colIndex);
			result.add(ImmutablePair.of(name, Integer.valueOf(type)));
			if (colIndex > 1)
				sb.append(',');
			sb.append(name);
		}
		sb.append("\r\n");
		return result;
	}
}
