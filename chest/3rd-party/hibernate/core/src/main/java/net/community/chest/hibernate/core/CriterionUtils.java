/*
 *
 */
package net.community.chest.hibernate.core;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.db.sql.SQLKeyword;
import net.community.chest.db.sql.SQLUtils;
import net.community.chest.math.compare.ComparableOperator;
import net.community.chest.math.strings.StringComparison;
import net.community.chest.util.datetime.TimeUnits;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.IlikeExpression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.LikeExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.NotExpression;
import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.NullExpression;
import org.hibernate.criterion.PropertyExpression;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SQLCriterion;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.util.StringHelper;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 21, 2009 10:32:53 AM
 */
public final class CriterionUtils {
    private CriterionUtils ()
    {
        // no instance
    }
    /**
     * @param org Original {@link Collection} of {@link Criterion}-s. If
     * null and need to add a criterion then a collection is allocated
     * @param c if non-null then added to the collection - ignored otherwise
     * @return updated collection - same as input if non-null original,
     * otherwise a newly allocated one. May be null/empty if null/empty
     * original collection and nothing added to it.
     */
    public static final Collection<Criterion> addCriterion (final Collection<Criterion> org, final Criterion c)
    {
        if (null == c)
            return org;

        if (null == org)
        {
            final Collection<Criterion>    crt=new LinkedList<Criterion>();
            crt.add(c);
            return crt;
        }
        else
        {
            org.add(c);
            return org;
        }
    }
    /**
     * <P>Creates a {@link String} value restriction</P>
     * @param name property/column name on which to apply the operator
     * @param op {@link StringComparison} to be applied to the string value
     * @param s String to be used as parameter
     * @param caseSensitive TRUE=execute the comparison in case sensitive mode
     * @return created {@link Criterion}
     * @throws IllegalArgumentException if null/empty name/operator/number
     * @throws IllegalStateException invalid operator
     */
    public static final Criterion createStringCriterion (
            final String name, final StringComparison op, final String s, final boolean caseSensitive)
        throws IllegalArgumentException, IllegalStateException
    {
        final String    str=SQLUtils.adjustQueryTextLiteral(s);
        if ((null == op) || (null == str) || (null == name) || (name.length() <= 0))
            throw new IllegalArgumentException("createStringCriterion(" + name + "-" + op + "-" + str + ") incomplete specification");

        switch(op)
        {
            case MATCHES        :
                return caseSensitive ? Restrictions.eq(name, str) : Restrictions.ilike(name, str, MatchMode.EXACT);
            case DIFFS    :
                return caseSensitive ? Restrictions.ne(name, str) : Restrictions.not(createStringCriterion(name,StringComparison.MATCHES,str,false));
            case STARTS    :
                return caseSensitive ? Restrictions.like(name, str, MatchMode.START) : Restrictions.ilike(name, str, MatchMode.START);
            case ENDS    :
                return caseSensitive ? Restrictions.like(name, str, MatchMode.END) : Restrictions.ilike(name, str, MatchMode.END);
            case CONTAINS        :
                return caseSensitive ? Restrictions.like(name, str, MatchMode.ANYWHERE) : Restrictions.ilike(name, str, MatchMode.ANYWHERE);

            default    :
                throw new IllegalStateException("createStringCriterion(" + name + "-" + op + "-" + str + ") invalid opcode");
        }
    }
    /**
     * @param sqlPrefix The SQL fragment that precedes the {@link String}
     * comparison
     * @param cmpOp The {@link StringComparison} to be applied
     * @param attrValue The value whose {@link Object#toString()} is to be used.
     * If null/empty then a <code>is null/is not null<code> expression is
     * generated - provided {@link StringComparison#MATCHES} or  {@link StringComparison#DIFFS}
     * are used
     * @return Created {@link Criterion}
     * @throws IllegalArgumentException if bad arguments
     */
    public static final Criterion createVirtualStringColumnCriterion (
            final String sqlPrefix, final StringComparison cmpOp, final Object attrValue)
        throws IllegalArgumentException
    {
        final int        prfxLen=(null == sqlPrefix) ? 0 : sqlPrefix.length();
        final String    sValue=(null == attrValue) ? null : SQLUtils.adjustQueryTextLiteral(attrValue.toString());
        if ((prfxLen <= 0) || (null == cmpOp))
            throw new IllegalArgumentException("createVirtualStringColumnCriterion(" + sqlPrefix + "/" + cmpOp + "/" + sValue + ") bad/incomplete arguments");

        final int            vLen=(null == sValue) ? 0 : sValue.length();
        final StringBuilder    sb=new StringBuilder(prfxLen + Math.max(0, vLen) + 32)
                                    .append(sqlPrefix)
                                    ;
        try
        {
            if (vLen <= 0)
                SQLUtils.appendNullCondition(sb, cmpOp);
            else
                SQLUtils.appendStringCondition(sb, cmpOp, sValue);
        }
        catch(IOException e)
        {
            throw new IllegalArgumentException("createVirtualStringColumnCriterion(" + sqlPrefix + "/" + cmpOp + "/" + sValue + ") " + e.getMessage());
        }

        return Restrictions.sqlRestriction(sb.toString());
    }
    /**
     * @param sqlPrefix The SQL fragment that precedes the {@link Number}
     * comparison
     * @param cmpOp The {@link ComparableOperator} to be applied
     * @param attrValue The {@link Number} value whose {@link Object#toString()}
     * is to be used. If null/empty then a <code>is null/is not null<code>
     * expression is generated - provided {@link ComparableOperator#EQ} or
     * {@link ComparableOperator#NE} are used
     * @return Created {@link Criterion}
     * @throws IllegalArgumentException if bad arguments
     */
    public static final Criterion createVirtualNumberColumnCriterion (
            final String sqlPrefix, final ComparableOperator cmpOp, final Number attrValue)
        throws IllegalArgumentException
    {
        final int        prfxLen=(null == sqlPrefix) ? 0 : sqlPrefix.length();
        final String    sValue=(null == attrValue) ? null : attrValue.toString();
        if ((prfxLen <= 0) || (null == cmpOp))
            throw new IllegalArgumentException("createVirtualNumberColumnCriterion(" + sqlPrefix + "/" + cmpOp + "/" + sValue + ") bad/incomplete arguments");

        final int            vLen=(null == sValue) ? 0 : sValue.length();
        final StringBuilder    sb=new StringBuilder(prfxLen + Math.max(0, vLen) + 32)
                                    .append(sqlPrefix)
                                    ;
        try
        {
            if (vLen <= 0)
                SQLUtils.appendNullCondition(sb, cmpOp);
            else
                SQLUtils.appendNumberCriterion(sb, cmpOp, attrValue);
        }
        catch(IOException e)
        {
            throw new IllegalArgumentException("createVirtualNumberColumnCriterion(" + sqlPrefix + "/" + cmpOp + "/" + sValue + ") " + e.getMessage());
        }

        return Restrictions.sqlRestriction(sb.toString());
    }

    public static final <C extends Comparable<C>> Criterion createComparableCriterion (
        final String name, final ComparableOperator op, final C obj)
            throws IllegalArgumentException, IllegalStateException
    {
        if ((null == op) || (null == obj) || (null == name) || (name.length() <= 0))
            throw new IllegalArgumentException("createComparableCriterion(" + name + "-" + op + "-" + obj + ") incomplete specification");

        switch(op)
        {
            case EQ    : return Restrictions.eq(name, obj);
            case NE    : return Restrictions.ne(name, obj);
            case LT    : return Restrictions.lt(name, obj);
            case LE    : return Restrictions.le(name, obj);
            case GT    : return Restrictions.gt(name, obj);
            case GE    : return Restrictions.ge(name, obj);

            default    :
                throw new IllegalStateException("createComparableCriterion(" + name + "-" + op + "-" + obj + ") invalid opcode");
        }
    }

    public static final <C extends Comparable<C>> Criterion createComparableCriterion (
            final String name, final Map.Entry<ComparableOperator,C> cmpSpec)
        throws IllegalArgumentException, IllegalStateException
    {
        return createComparableCriterion(name, (null == cmpSpec) ? null : cmpSpec.getKey(), (null == cmpSpec) ? null : cmpSpec.getValue());
    }
    /**
     * <P>Creates a numerical value restriction</P>
     * @param <N> The type of {@link Number} being used
     * @param name property/column name on which to apply the operator
     * @param op {@link ComparableOperator} to be applied to the number
     * @param num number to be used as parameter
     * @return created {@link Criterion}
     * @throws IllegalArgumentException if null/empty name/operator/number
     * @throws IllegalStateException invalid operator (only <U>numerical</U>
     * operators can be applied - e.g., EQ, NE, LT, etc.)
     */
    public static final <N extends Number & Comparable<N>> Criterion createNumberCriterion (
            final String name, final ComparableOperator op, final N num)
                        throws IllegalArgumentException, IllegalStateException
    {
        return createComparableCriterion(name, op, num);
    }

    public static final <N extends Number & Comparable<N>> Criterion createNumberCriterion (
            final String name, final Map.Entry<ComparableOperator,N> cmpSpec)
        throws IllegalArgumentException, IllegalStateException
    {
        return createNumberCriterion(name, (null == cmpSpec) ? null : cmpSpec.getKey(), (null == cmpSpec) ? null : cmpSpec.getValue());
    }
    // same as createNumberCriterion only returns null if null number
    public static final <N extends Number & Comparable<N>> Criterion createOptionalNumberCriterion (
            final String name, final ComparableOperator op, final N num)
        throws IllegalArgumentException, IllegalStateException
    {
        if (null == num)
            return null;
        else
            return createNumberCriterion(name, op, num);
    }

    public static final <N extends Number & Comparable<N>> Criterion createOptionalNumberCriterion (
            final String name, final Map.Entry<ComparableOperator,N> cmpSpec)
        throws IllegalArgumentException, IllegalStateException
    {
        if (null == cmpSpec)
            return null;

        return createOptionalNumberCriterion(name, cmpSpec.getKey(), cmpSpec.getValue());
    }
    /**
     * Creates an <U>inclusive</U> numerical range restriction - i.e., one
     * where the range ends are included in the restriction. <B>Note:</B> the
     * range can be <U>"open-ended"</U> - i.e., low/high value can be null
     * (but not both), in which case it is not added to the restriction (e.g.,
     * if low value is omitted, then the query defaults to "(L)ess-or-(E)qual
     * to high value").
     * @param <N> Type of {@link Number} being used
     * @param name property/column name on which to apply the operator
     * @param lo low end (inclusive)
     * @param hi high end (inclusive)
     * @return create {@link Criterion}
     * @throws IllegalArgumentException if null/empty name or both range ends
     * not specified
     */
    public static final <N extends Number & Comparable<N>> Criterion createNumberRangeCriterion (
            final String name, final N lo, final N hi)
                throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0) || ((null == lo) && (null == hi)))
            throw new IllegalArgumentException("createNumberRangeCriterion(" + name + "[" + lo + "-" + hi + "]) incomplete specification");

        if (null == lo)
            return createNumberCriterion(name, ComparableOperator.LE, hi);
        else if (null == hi)
            return createNumberCriterion(name, ComparableOperator.GE, lo);
        else if (lo.equals(hi))
            return createNumberCriterion(name, ComparableOperator.EQ, lo);
        else
            return Restrictions.between(name, lo, hi);
    }
    // same as createNumberRangeCriterion only returns null if both range ends are null
    public static final <N extends Number & Comparable<N>> Criterion createOptionalNumberRangeCriterion (
            final String name, final N lo, final N hi)
        throws IllegalArgumentException
    {
        if ((lo != null) || (hi != null))
            return createNumberRangeCriterion(name, lo, hi);

        return null;
    }
    /**
     * Creates an {@link Enum} based criterion
     * @param <E> Type of {@link Enum} used
     * @param name property/column name on which to apply the restriction
     * @param value value to be used
     * @param matchEquals TRUE=use equality, FALSE=use in-equality operator
     * @param includeNulls TRUE=include null(s) as OR comparison
     * @return created {@link Criterion}
     * @throws IllegalArgumentException if null/empty name/value
     */
    public static final <E extends Enum<E>> Criterion createEnumCriterion (
            final String name, final E value, final boolean matchEquals, final boolean includeNulls)
                        throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0) || (null == value))
            throw new IllegalArgumentException("createEnumCriterion(" + name + "/EQ=" + matchEquals + "/" + value + ") incomplete specification");

        Criterion    c=matchEquals ? Restrictions.eq(name, value) : Restrictions.ne(name, value);
        if (includeNulls)
        {
            final Criterion    nc=Restrictions.isNull(name);
            c = Restrictions.or(c, nc);
        }

        return c;
    }
    // same as createEnumCriterion only returns null if null value
    public static final <E extends Enum<E>> Criterion createOptionalEnumCriterion (
            final String name, final E value, final boolean matchEquals, final boolean includeNulls)
    {
        if (null == value)
            return null;

        return createEnumCriterion(name, value, matchEquals, includeNulls);
    }

    public static final Criterion createOptionalStringCriterion (
            final String name, final StringComparison op, final String value, final boolean caseSensitive)
    {
        if ((null == value) || (value.length() <= 0))
            return null;
        else
            return createStringCriterion(name, op, value, caseSensitive);
    }

    public static final Criterion createTemporalCriterion (
            final String name, final ComparableOperator op, final Date d)
    {
        return createComparableCriterion(name, op, d);
    }

    public static final Criterion createOptionalTemporalCriterion (
            final String name, final ComparableOperator op, final Date d)
    {
        if (d != null)
            return createTemporalCriterion(name, op, d);

        return null;
    }

    public static final Criterion createTemporalRangeCriterion (
            final String name, final Date lo, final Date hi)
        throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0) || ((null == lo) && (null == hi)))
            throw new IllegalArgumentException("createTemporalRangeCriterion(" + name + "[" + lo + "-" + hi + "]) incomplete specification");

        if (null == lo)
            return createTemporalCriterion(name, ComparableOperator.LE, hi);
        else if (null == hi)
            return createTemporalCriterion(name, ComparableOperator.GE, lo);
        else
            return Restrictions.between(name, lo, hi);
    }

    public static final Criterion createNullCriterion (
            final String colName, final boolean useNull)
        throws IllegalArgumentException
    {
        if ((null == colName) || (colName.length() <= 0))
            throw new IllegalArgumentException("createNullCriterion(" + useNull + ") no column name specified");

        if (useNull)
            return Restrictions.isNull(colName);
        else
            return Restrictions.isNotNull(colName);
    }

    public static final Criterion createOptionalNullCriterion (
            final String colName, final Boolean stVal)
    {
        return (null == stVal) ? null : createNullCriterion(colName, stVal.booleanValue());
    }

    public static final Criterion createOptionalTemporalRangeCriterion (
            final String name, final Date lo, final Date hi)
        throws IllegalArgumentException
    {
        if ((lo != null) || (hi != null))
            return createTemporalRangeCriterion(name, lo, hi);

        return null;
    }
    /**
     * Builds/Adds {@link Criterion}-s to a {@link Collection} - ignores null values
     * @param org original {@link Collection} - if null and need to, then creates one
     * @param crits {@link Criterion}-s - null elements are ignored
     * @return resulting updated collection - may be null/empty if no original
     * collection and nothing to add to it
     */
    public static final Collection<Criterion> createOptionalCriteria (
            final Collection<Criterion> org, final Criterion ... crits)
    {
        if ((null == crits) || (crits.length <= 0))
            return org;

        Collection<Criterion>    ret=org;
        for (final Criterion c : crits)
            ret = addCriterion(ret, c);
        return ret;
    }
    /**
     * Creates a {@link Boolean} flag criterion
     * @param name property/column name on which to apply the restriction
     * @param value value to be used
     * @param matchEquals TRUE=use equality, FALSE=use in-equality operator
     * @return created {@link Criterion}
     * @throws IllegalArgumentException if null/empty name/value
     */
    public static final Criterion createBooleanCriterion (
        final String name, final Boolean value, final boolean matchEquals)
            throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0) || (null == value))
            throw new IllegalArgumentException("createBooleanCriterion(" + name + "/EQ=" + matchEquals + "/" + value + ") incomplete specification");

        if (matchEquals)
            return Restrictions.eq(name, value);
        else
            return Restrictions.ne(name, value);
    }
    // same as createBooleanCriterion only returns null if null value
    public static final Criterion createOptionalBooleanCriterion (
            final String name, final Boolean value, final boolean matchEquals)
    {
        if (null == value)
            return null;

        return createBooleanCriterion(name, value, matchEquals);
    }
    /**
     * Creates an "age" {@link Criterion} that is defined as the
     * <code>DATEDIFF</code> between <code>NOW()</code> and the specified
     * (datetime) column.
     * @param colName The (datetime) column to be used for the comparison -
     * may NOT be null/empty
     * @param op The comparison operator - must be a <U>numerical</U> one
     * (i.e., EQ, NE, LT, LE, GT, GE) - may NOT be null
     * @param ageVal The age difference (msec.) - may NOT be negative
     * @return The {@link Criterion} that can be used to restrict the DB
     * query according to the specified age and column value(s)
     * @throws IllegalArgumentException if bad/illegal arguments
     */
    public static final Criterion createAgeCriterion (
            final String colName, final ComparableOperator op, final long ageVal)
        throws IllegalArgumentException
    {
        if ((null == colName) || (colName.length() <= 0) || (ageVal < 0L) || (null == op))
            throw new IllegalArgumentException("createAgeCriterion(" + colName + "/" + op + "/" + ageVal + ") incomplete arguments");

        // age criterion is same as reverse creation time one
        final ComparableOperator    ageOp;
        switch(op)
        {
            case EQ    : ageOp = ComparableOperator.NE; break;
            case NE    : ageOp = ComparableOperator.EQ; break;
            case LT : ageOp = ComparableOperator.GT; break;
            case LE : ageOp = ComparableOperator.GE; break;
            case GT : ageOp = ComparableOperator.LT; break;
            case GE : ageOp = ComparableOperator.LE; break;

            default    :    // should not happen
                throw new IllegalArgumentException("createAgeCriterion(" + colName + "/" + op + "/" + ageVal + ") no reverse age operator");
        }

        final long    now=System.currentTimeMillis(),
                    deadLine=now - ageVal;
        return createTemporalCriterion(colName, ageOp, new Date(deadLine));
    }

    public static final Criterion createAgeCriterion (
            final String colName, final Map.Entry<ComparableOperator,? extends Number> ageVal)
        throws IllegalArgumentException
    {
        final Number    numVal=(null == ageVal) ? null : ageVal.getValue();
        return createAgeCriterion(colName, (null == ageVal) ? null : ageVal.getKey(), (null == numVal) ? (-1L) : numVal.longValue());
    }

    public static final Criterion createOptionalAgeCriterion (
            final String colName, final Map.Entry<ComparableOperator,? extends Number> ageVal)    throws IllegalArgumentException
    {
        if (null == ageVal)
            return null;
        else
            return createAgeCriterion(colName, ageVal);
    }

    public static final Criterion createTimeRangeCriterion (
            final String sqlPrefix, final long startAge, final long endAge, final long measureUnit)
        throws IllegalArgumentException
    {
        if ((null == sqlPrefix) || (sqlPrefix.length() <= 0)
         || (startAge < 0L) || (endAge < 0L) || (endAge < startAge)
         || (measureUnit <= 0L))
            throw new IllegalArgumentException("createTimeRangeCriterion(" + sqlPrefix + "[" + startAge + "-" + endAge + "]/" + measureUnit + ") incomplete arguments");

        final StringBuilder    sqlClause=new StringBuilder(sqlPrefix.length() + 64)
                .append(sqlPrefix)
                .append(' ').append(SQLKeyword.BETWEEN.getKeyword())
                    .append(' ').append(startAge / measureUnit)
                .append(' ').append(SQLKeyword.AND.getKeyword())
                    .append(' ').append(endAge / measureUnit)
                ;
        return Restrictions.sqlRestriction(sqlClause.toString());
    }

    public static final Criterion createTimeRangeCriterion (
            final String sqlPrefix, final long startAge, final long endAge, final TimeUnits measureUnit)
        throws IllegalArgumentException
    {
        return createTimeRangeCriterion(sqlPrefix, startAge, endAge, (null == measureUnit) ? (-1L) : measureUnit.getMilisecondValue());
    }

    public static final Criterion createTimeRangeCriterion (
            final String sqlPrefix, final long startAge, final long endAge)
        throws IllegalArgumentException
    {
        return createTimeRangeCriterion(sqlPrefix, startAge, endAge, TimeUnits.SECOND);
    }

    public static final Criterion createAgeRangeCriterion (
            final String colName, final long startAge, final long endAge)
        throws IllegalArgumentException
    {
        if ((null == colName) || (colName.length() <= 0)
         || (startAge < 0L) || (endAge < 0L) || (startAge > endAge))
            throw new IllegalArgumentException("createAgeRangeCriterion(" + colName + "/start=" + startAge + "/end=" + endAge + ") bad parameteres");

        final long    now=System.currentTimeMillis(),
                    startOffset=now - endAge,
                    endOffset=now - startAge;
        return Restrictions.between(colName, new Date(startOffset), new Date(endOffset));
    }

    public static final String    ALIAS_PATTERN="{alias}";
    public static final Criterion xlateCriterion (final String aliasValue, final Criterion c)
    {
        if ((null == aliasValue) || (aliasValue.length() <= 0))
            return c;

        final String    cs=(null == c) ? null : c.toString();
        if ((null == cs) || (cs.length() <= 0))
            throw new IllegalArgumentException("xlateCriterion(" + aliasValue + ")[" + Criterion.class.getSimpleName() + "@" + System.identityHashCode(c) + ") no string value");

        final String    sqlVal;
        if ((c instanceof SimpleExpression)
         || (c instanceof Junction)
         || (c instanceof NotExpression)
         || (c instanceof NullExpression)
         || (c instanceof NotNullExpression))
            sqlVal = aliasValue + "." + cs;
        else if (c instanceof SQLCriterion)
            sqlVal = StringHelper.replace(cs, ALIAS_PATTERN, aliasValue);
        else
            throw new NoSuchElementException("xlateCriterion(" + aliasValue + ")[" + cs + "] unknown " + Criterion.class.getSimpleName() + " instance: " + c.getClass().getSimpleName());

        return Restrictions.sqlRestriction(sqlVal);
    }

    public static final <A extends Appendable> A appendCriterion (
            final A sb, final String aliasValue, final Criterion c)
        throws IOException
    {
        final String    cs=(null == c) ? null : c.toString();
        if ((null == cs) || (cs.length() <= 0))
            return sb;

        if (null == sb)
            throw new IOException("appendCriterion(" + aliasValue + ")[" + cs + "] no " + Appendable.class.getSimpleName() + " instance");

        final String    csVal;
        if ((c instanceof SimpleExpression)
         || (c instanceof PropertyExpression)
         || (c instanceof Junction)
         || (c instanceof NotExpression)
         || (c instanceof NullExpression)
         || (c instanceof NotNullExpression)
         || (c instanceof IlikeExpression))
            csVal = ((null == aliasValue) || (aliasValue.length() <= 0)) ? cs : ALIAS_PATTERN + "." + cs;
        else if (c instanceof LikeExpression)    // TODO need better replacement
            csVal = ((null == aliasValue) || (aliasValue.length() <= 0)) ? cs : ALIAS_PATTERN + "." + cs;
        else if (c instanceof SQLCriterion)
            csVal = cs;
        else
            throw new FileNotFoundException("appendCriterion(" + aliasValue + ")[" + cs + "] unknown " + Criterion.class.getSimpleName() + " instance: " + c.getClass().getSimpleName());

        final String    aVal=
            ((null == aliasValue) || (aliasValue.length() <= 0)) ? csVal : StringHelper.replace(csVal, "{alias}", aliasValue);
        if ((null == aVal) || (aVal.length() <= 0))
            throw new EOFException("appendCriterion(" + aliasValue + ")[" + cs + "] no appendable value for criterion=" + csVal);

        final Appendable    a=sb.append("( ").append(aVal).append(" )");
        if (a != sb)
            throw new StreamCorruptedException("appendCriterion(" + aliasValue + ")[" + cs + "] mismatched appended instances");
        return sb;
    }

    public static final <A extends Appendable> A appendCriteria (
            final A org, final String aliasValue, final boolean conjunctive, final Collection<? extends Criterion> cl)
        throws IOException
    {
        if ((null == cl) || (cl.size() <= 0))
            return org;

        if (null == org)
            throw new IOException("appendCriteria(" + aliasValue + ") no " + Appendable.class.getSimpleName() + " instance");

        int    cIndex=0;
        A    sb=org;
        for (final Criterion c : cl)
        {
            if (null == c)
                continue;

            if (cIndex > 0)
                sb.append(conjunctive ? " AND " : " OR ");

            sb = appendCriterion(sb, aliasValue, c);
            cIndex++;
        }

        return sb;
    }

    public static final <A extends Appendable> A appendCriteria (
            final A org, final String aliasValue, final boolean conjunctive, final Criterion ... cl)
        throws IOException
    {
        return appendCriteria(org, aliasValue, conjunctive, ((null == cl) || (cl.length <= 0)) ? null : Arrays.asList(cl));
    }

    public static final String getCriteriaSQLValue (final String aliasValue, final boolean conjunctive, final Collection<? extends Criterion> cl)
    {
        final int    numCrits=(null == cl) ? 0 : cl.size();
        try
        {
            final StringBuilder    sb=
                appendCriteria((numCrits <= 0) ? null : new StringBuilder(numCrits * 64), aliasValue, conjunctive, cl);
            if ((null == sb) || (sb.length() <= 0))
                return null;

            return sb.toString();
        }
        catch(IOException e)    // should not happen
        {
            throw new RuntimeException(e);
        }
    }

    public static final String getCriteriaSQLValue (final String aliasValue, final boolean conjunctive, final Criterion ... cl)
    {
        return getCriteriaSQLValue(aliasValue, conjunctive, ((null == cl) || (cl.length <= 0)) ? null : Arrays.asList(cl));
    }
}
