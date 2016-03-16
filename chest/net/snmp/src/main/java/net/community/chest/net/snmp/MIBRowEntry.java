/*
 *
 */
package net.community.chest.net.snmp;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Used to hold information about a MIB table row</P>
 * @author Lyor G.
 * @since May 25, 2009 2:27:26 PM
 */
public class MIBRowEntry extends MIBAttributeEntry {
    /**
     *
     */
    private static final long serialVersionUID = -6417166339028432207L;
    /**
     * Default (empty) constructor
     */
    public MIBRowEntry ()
    {
        this((String) null);
    }
    /**
     * @param attrName row base name
     * @throws IllegalArgumentException if null/empty name
     */
    public MIBRowEntry (String attrName) throws IllegalArgumentException
    {
        super(attrName);
    }
    /**
     * Row columns - <B>Note:</B> each column may be some other row
     */
    private OIDStringsMap<MIBAttributeEntry>    _columns    /* =null */;
    /**
     * @return (@link Map) of current "columns" - key=OID, value=entry
     * <B>Note:</B> these entries <U>include</U> the entry row, so if
     * you want only the effective rows, then call (@link #getEffectiveColumns())
     */
    public Map<String,? extends MIBAttributeEntry> getColumns ()
    {
        return _columns;
    }

    private static final boolean compareIndicesNames (final Collection<String> idx, final Collection<String> cdx)
    {
        final int    iSize=(null == idx) ? 0 : idx.size(),
                    cSize=(null == cdx) ? 0 : cdx.size();
        if (iSize != cSize)    // check the obvious first
            return false;

        // make sure same names (case insensitive)
        final Iterator<String>    ii=idx.iterator(), ci=cdx.iterator();
        while ((ii != null) && (ci != null) && ii.hasNext() && ci.hasNext())
        {
            final String    iName=ii.next(), cName=ci.next();
            if ((null == iName) || (iName.length() <= 0)
             || (null == cName) || (cName.length() <= 0)
             || (!iName.equalsIgnoreCase(cName)))
             return false;
        }

        // make sure exhausted ALL names
        if ((null == ii) || (null == ci) || ii.hasNext() || ci.hasNext())
            return false;    // should not happen

        return true;
    }
    /**
     * Iterates over all columns and attempts to find first entry whose
     * number and name(s) of set index(es) matches the supplied one
     * @param cols row columns (including the row entry)
     * @param idx name of index(es) used
     * @return row entry (null if not found or null/empty columns/names)
     */
    private static final MIBAttributeEntry getRowEntry (final Collection<? extends MIBAttributeEntry> cols, final Collection<String> idx)
    {
        if ((null == cols) || (cols.size() <= 0)
         || (null == idx) || (idx.size() <= 0))
            return null;    // check the obvious

        for (final MIBAttributeEntry    c : cols)
        {
            final Collection<String>    cdx=(null == c) /* should not happen */ ? null : c.getIndices();
            if (compareIndicesNames(idx, cdx))
                return c;
        }

        // this point is reached if no match found
        return null;
    }
    /**
     * Row description entry - dynamically calculated on first call to
     * (@link #getRowEntry()). <B>Note:</B> any calls to
     * (@link #addColumn(MIBAttributeEntry)) null-ify it in order to force
     * its re-evaluation.
     */
    private MIBAttributeEntry    _rowEntry    /* =null */;
    public synchronized MIBAttributeEntry getRowEntry ()
    {
        if (null == _rowEntry)
        {
            final Map<String,? extends MIBAttributeEntry>    cMap=getColumns();
            final Collection<? extends MIBAttributeEntry>    cols=
                ((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();
            final Collection<String>                        tblEntry=getIndices();
            final Iterator<String>                            iTbl=
                ((null == tblEntry) || (tblEntry.size() != 1) /* we expect only ONE entry name */) ? null : tblEntry.iterator();
            final String                                    tblSeqName=
                ((iTbl != null) && iTbl.hasNext()) ? iTbl.next() : null;
            final MIBAttributeEntry                            topEntry=
                getEntryByName(cols, tblSeqName);

            // NOTE: actually, it should be the top entry, but better to be sure...
            _rowEntry = (null == topEntry) ? null : getRowEntry(cols, topEntry.getIndices());
        }

        return _rowEntry;
    }
    /**
     * Effective columns - without the row entry. <B>Note:</B> dynamically
     * calculated upon first call to (@link #getEffectiveColumns()) and
     * reset by call(s) to (@link #addColumn(MIBAttributeEntry))
     */
    private OIDStringsMap<MIBAttributeEntry>    _effCols    /* =null */;
    public synchronized Map<String,? extends MIBAttributeEntry> getEffectiveColumns ()
    {
        if (null == _effCols)
        {
            final MIBAttributeEntry                            rowEntry=getRowEntry();
            final Map<String,? extends MIBAttributeEntry>    cMap=getColumns();
            final Collection<? extends MIBAttributeEntry>    cols=
                ((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();
            if ((cols != null) && (cols.size() > 0))
            {
                for (final MIBAttributeEntry c : cols)
                {
                    if ((null == c) /* should not happen */ || (c == rowEntry) /* skip entry definition */)
                        continue;

                    if (null == _effCols)
                        _effCols = new OIDStringsMap<MIBAttributeEntry>();
                    _effCols.put(c.getOid(), c);
                }
            }
        }

        return _effCols;
    }
    /**
     * @param entries entries in which to search for the specified names - may
     * be null/empty
     * @param names requested entries names (case insensitive) - may be
     * null/empty
     * @return found entries - may be null/empty if no entries/names available
     * to begin with or none found. <B>Note:</B> if a specified name has no
     * matching entry, it is silently ignored
     */
    public static final Collection<MIBAttributeEntry> getEntriesByName(final Collection<? extends MIBAttributeEntry> entries, final Collection<String> names)
    {
        if ((null == entries) || (entries.size() <= 0)
         || (null == names) || (names.size() <= 0))
            return null;    // OK if no entries or no names

        Collection<MIBAttributeEntry>    ret=null;
        for (final String    n : names)
        {
            final MIBAttributeEntry    e=getEntryByName(entries, n);
            if (e != null) // should not be otherwise
            {
                if (null == ret)
                    ret = new LinkedList<MIBAttributeEntry>();
                ret.add(e);
            }
        }

        return ret;
    }
    /**
     * Column used as index - dynamically calculated on first call to
     * (@link #getIndexColumn()). <B>Note:</B> any calls to
     * (@link #addColumn(MIBAttributeEntry)) null-ify it in order to force
     * its re-evaluation.
     */
    private Collection<MIBAttributeEntry>    _idxCols    /* =null */;
    public synchronized Collection<? extends MIBAttributeEntry> getIndexColumns ()
    {
        if (null == _idxCols)
        {
            final MIBAttributeEntry                         eRow=getRowEntry();
            final Map<String,? extends MIBAttributeEntry>    cMap=
                (null == eRow) /* should not happen */ ? null : getEffectiveColumns();
            final Collection<? extends MIBAttributeEntry>    cols=
                ((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();

            _idxCols = getEntriesByName(cols, (null == eRow) ? null : eRow.getIndices());
        }

        return _idxCols;
    }

    /**
     * Longest common string prefix of the column(s) names (including the row
     * entry) - null/empty if no such prefix. <B>Note</B>: this value is
     * <U>dynamically</U> calculated and reset on every call to (@link #addColumn(MIBAttributeEntry))
     */
    private String    _erPrefix    /* =null */;
    private boolean    _erSet    /* =false */;
    public synchronized String getRowEntriesPrefix ()
    {
        if (!_erSet)
        {
            final Map<String,? extends MIBAttributeEntry>    cMap=getColumns();
            final Collection<? extends MIBAttributeEntry>    cols=
                ((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();
            _erPrefix = findCommonNamePrefix(cols);
            _erSet = true;
        }

        return _erPrefix;
    }

    public static final String buildIndicesEntriesList (final Collection<? extends MIBAttributeEntry> ic)
    {
        final int    numIdx=(null == ic) ? 0 : ic.size();
        if (numIdx <= 0)
            return null;

        Collection<String>    names=null;
        for (final MIBAttributeEntry e : ic)
        {
            final String    eName=(null == e) /* should not happen */ ? null : e.getAttrName();
            if ((eName != null) && (eName.length() > 0))    // should not be otherwise
            {
                if (null == names)
                    names = new LinkedList<String>();
                names.add(eName);
            }
        }

        return buildIndicesNamesList(names);
    }
    /**
     * Resets internally calculated dynamic data
     */
    protected void resetCalculatedData ()
    {
        _idxCols = null;
        _rowEntry = null;
        _effCols = null;
        _erPrefix = null;
        _erSet = false;
    }
    /*
     * @see net.community.chest.net.snmp.MIBAttributeEntry#clone()
     */
    @Override
    @CoVariantReturn
    public MIBRowEntry clone () throws CloneNotSupportedException
    {
        final MIBRowEntry                                ret=getClass().cast(super.clone());
        final Map<?,? extends MIBAttributeEntry>        cMap=getColumns();
        final Collection<? extends MIBAttributeEntry>    cl=
            ((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();
        ret.resetCalculatedData();
        if ((cl != null) && (cl.size() > 0))
        {
            for (final MIBAttributeEntry    ce : cl)
            {
                final MIBAttributeEntry    cc=(null == ce) ? null : ce.clone();
                if (cc != null)
                    ret.addColumn(ce);
            }
        }

        return ret;
    }
    /**
     * @param e column entry to be added
     * @return updated columns list
     * @throws IllegalArgumentException if null/duplicate column/OID instance
     */
    public Map<String,? extends MIBAttributeEntry> addColumn (final MIBAttributeEntry e) throws IllegalArgumentException
    {
        final String    eOID=(null == e) ? null : e.getOid();
        if ((null == eOID) || (eOID.length() <= 0))
            throw new IllegalArgumentException("addColumn(" + getAttrName() + ") null/empty column/OID");

        if (null == _columns)
            _columns = new OIDStringsMap<MIBAttributeEntry>();

        final MIBAttributeEntry    prev=_columns.put(eOID, e);
        if (prev != null)    // make sure no duplicates
            throw new IllegalArgumentException("addColumn(" + getAttrName() + ")[" + e.getAttrName() + "] duplicate entry (" + prev.getAttrName() + ") for OID=" + eOID);

        // force re-evaluation
        resetCalculatedData();

        return getColumns();
    }
    /**
     * @param cols columns to be added - may be null/empty or contain null
     * members (which are ignored)
     * @return updated columns
     * @throws IllegalArgumentException if null/duplicate column/OID instance
     */
    public Map<String,? extends MIBAttributeEntry> addColumns (final Collection<? extends MIBAttributeEntry> cols) throws IllegalArgumentException
    {
        if ((cols != null) && (cols.size() > 0))
        {
            for (final MIBAttributeEntry c : cols)
            {
                if (c != null)    // should not be otherwise
                    addColumn(c);
            }
        }

        return getColumns();
    }
    // NOTE: creates NEW list/collection instance - i.e., not shared with argument
    public void setColumns (final Collection<? extends MIBAttributeEntry> cols)
    {
        _columns = null;
        addColumns(cols);
    }
    /**
     * Copy constructor
     * @param e base attribute entry to copy from (usually the "top" SEQUENCE)
     * @throws IllegalArgumentException if null entry
     */
    public MIBRowEntry (final MIBAttributeEntry e) throws IllegalArgumentException
    {
        super(e);
    }
    /**
     * Copy constructor
     * @param e row entry to copy from
     * @throws IllegalArgumentException if null row
     */
    public MIBRowEntry (final MIBRowEntry e) throws IllegalArgumentException
    {
        super(e);

        final Map<String,? extends MIBAttributeEntry>    cMap=e.getColumns();
        final Collection<? extends MIBAttributeEntry>    cols=((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();

        setColumns(cols);
    }
    /**
     * @param anPrefix common entries prefix (may be null/empty)
     * @param attrName MIB attribute name (without its prefix stripped)
     * @return indexed attribute name expected in the implementation (null/empty
     * if null/empty input)
     */
    public static final String getRowValueAttributeName (final String anPrefix, final String attrName)
    {
        return getRowValueAttributeName(MIBAttributeEntry.getEffectiveAttributeName(anPrefix, attrName));
    }
    /**
     * @param attrName "pure" attribute name representing a table row entry
     * @return indexed attribute name expected in the implementation (null/empty
     * if null/empty input)
     */
    public static final String getRowValueAttributeName (final String attrName)
    {
        if ((null == attrName) || (attrName.length() <= 0))
            return attrName;
        else
            return attrName + "Entry";
    }
    /**
     * @param e entry that serves as SEQUENCE definition top (not validated)
     * @param eMap (@link Map) of available entries - key=OID,
     * value=(@link MIBAttributeEntry) instances.
     * @return row entry
     * @throws RuntimeException if unable to process the row
     */
    public static final MIBRowEntry buildMIBRowEntry (final MIBAttributeEntry e, final Map<String,? extends MIBAttributeEntry> eMap) throws RuntimeException
    {
        final MIBRowEntry    row=new MIBRowEntry(e);
        final String        oid=e.getOid();
        final int            oLen=(null == oid) ? 0 : oid.length();
        if (oLen <= 0)
            throw new IllegalArgumentException("buildMIBRowEntry(" + e + ") no OID");

        // NOTE !!! we rely on the fact that values are SORTED in ascending (!) OID order
        final Collection<? extends MIBAttributeEntry>    entries=eMap.values();
        for (final MIBAttributeEntry c : entries)
        {
            final String    cid=c.getOid();
            final int        cLen=(null == cid) /* should not happen */ ? 0 : cid.length();
            // shorter OID(s) obviously cannot be columns
            if (cLen < oLen)
                continue;

            // we are interested only in OID(s)
            if (SNMPProtocol.compareOIDs(oid, cid) < 0)
            {
                // stop at first OID that is not proper sub-OID of root
                if ((cLen == oLen) || (cid.charAt(oLen) != '.') || (!cid.startsWith(oid)))
                    break;

                final String    type=c.getSyntax();
                // check if column is another table
                if (SNMPProtocol.SEQUENCEMod.equalsIgnoreCase(type))
                    row.addColumn(buildMIBRowEntry(c, eMap));
                else
                    row.addColumn(c);
            }
        }

        return row;
    }
    /**
     * @param entries entries collection - may be null/empty
     * @return (@link Map) key=entry OID, matching value=(@link MIBRowEntry),
     * may be null/empty if no entries or no (@link MIBRowEntry) instances
     */
    public static final OIDStringsMap<MIBRowEntry> extractRowsEntries (final Collection<? extends MIBAttributeEntry> entries)
    {
        if ((null == entries) || (entries.size() <= 0))
            return null;

        OIDStringsMap<MIBRowEntry>    rMap=null;
        for (final MIBAttributeEntry e : entries)
        {
            if ((null == e) || (!(e instanceof MIBRowEntry)))
                continue;

            if (null == rMap)
                rMap = new OIDStringsMap<MIBRowEntry>();
            rMap.put(e.getOid(), (MIBRowEntry) e);
        }

        return rMap;
    }
}
