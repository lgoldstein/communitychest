/*
 *
 */
package net.community.chest.net.snmp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Class representing a group of entries - usually read from a MIB file</P>
 * @author Lyor G.
 * @since May 25, 2009 2:24:28 PM
 */
public class MIBGroup {
    /**
     * Default (empty) constructor
     */
    public MIBGroup ()
    {
        super();
    }
    /**
     * MIB group name (null/empty if not set)
     */
    private String    _mibName    /* =null */;
    public String getMibName ()
    {
        return _mibName;
    }

    public void setMibName (String mibName)
    {
        _mibName = mibName;
    }
    /**
     * @param mibName MIB group name
     * @throws IllegalArgumentException if null/empty name supplied
     */
    public MIBGroup (final String mibName) throws IllegalArgumentException
    {
        if ((null == (_mibName=mibName)) || (mibName.length() <= 0))
            throw new IllegalArgumentException("Null/empty MIB group name");
    }
    /**
     * MIB group entries - key=entry OID, value=entry
     */
    private Map<String,MIBAttributeEntry>    _entries    /* =null */;
    public Map<String,? extends MIBAttributeEntry> getEntries ()
    {
        return _entries;
    }
    /**
     * Longest common OID prefix of all entries in group (null/empty) if
     * no such prefix exists (usually an error...). Lazy initialized by
     * first call to (@link #getBaseOID()). <B>Note:</B> calling
     * (@link #setEntries(Collection)) or (@link #addEntry(MIBAttributeEntry))
     * null-ifies this value in order to force its re-evaluation. It is
     * therefore, highly recommended that the base OID <U>not</U> be used
     * until <U>all</U> entries have been added
     */
    private String    _baseOid    /* =null */;
    public synchronized String getBaseOID ()
    {
        if ((null == _baseOid) || (_baseOid.length() <= 0))
        {
            final Map<String,? extends MIBAttributeEntry>    eMap=getEntries();
            final Collection<? extends MIBAttributeEntry>    entries=
                ((null == eMap) || (eMap.size() <= 0)) ? null : eMap.values();
            _baseOid = MIBAttributeEntry.findBaseOid(entries);
        }

        return _baseOid;
    }
    /**
     * @param eMap entries (@link Map) - key=OID, matching (@link MIBAttributeEntry)
     * value - may be null/empty
     * @return (@link Map) key=entry OID, matching value=(@link MIBRowEntry),
     * may be null/empty if no entries or no (@link MIBRowEntry) instances
     */
    public static final OIDStringsMap<MIBRowEntry> extractRowsEntries (final Map<String,? extends MIBAttributeEntry> eMap)
    {
        return MIBRowEntry.extractRowsEntries(((null == eMap) || (eMap.size() <= 0)) ? null : eMap.values());
    }
    /**
     * dummy empty rows map - auto-allocated/cleared by call to (@link #getEmptyRows())
     */
    private static OIDStringsMap<MIBRowEntry>    _EMPTY_ROWS    /* =null */;
    private static synchronized OIDStringsMap<MIBRowEntry> getEmptyRows ()
    {
        if (null == _EMPTY_ROWS)
            _EMPTY_ROWS = new OIDStringsMap<MIBRowEntry>();
        else
            _EMPTY_ROWS.clear();

        return _EMPTY_ROWS;
    }
    /**
     * (@link Map) of defined "rows" in this group (key=OID,value=(@link MIBRowEntry) - lazy
     * initialized by first call to (@link #getRows()) and <U>dynamically</U> re-initialized
     * after any changes to the current entries
     */
    private Map<String,? extends MIBRowEntry>    _rows    /* =null */;
    public synchronized Map<String,? extends MIBRowEntry> getRows ()
    {
        if (null == _rows)
        {
            if (null == (_rows=extractRowsEntries(getEntries())))
                _rows = getEmptyRows();    // mark as non-null to avoid re-evaluation
        }

        return _rows;
    }
    /**
     * (@link Map) of defined "scalars" in this group (key=OID,value=base type) - lazy
     * initialized by first call to (@link #getScalars()) and <U>dynamically</U> re-initialized
     * after any changes to the current entries
     */
    private Map<String,? extends MIBAttributeEntry>    _scalars    /* =null */;
    public Map<String,? extends MIBAttributeEntry> getScalars ()
    {
        if (null == _scalars)
        {
            final Map<String,? extends MIBAttributeEntry>    eMap=getEntries();
            final Collection<? extends MIBAttributeEntry>        entries=((null == eMap) || (eMap.size() <= 0)) ? null : eMap.values();

            final OIDStringsMap<MIBAttributeEntry>    sMap=new OIDStringsMap<MIBAttributeEntry>();
            if ((entries != null) && (entries.size() > 0))
            {
                for (final MIBAttributeEntry e : entries)
                {
                    if ((null == e) || (e instanceof MIBRowEntry))
                        continue;

                    sMap.put(e.getOid(), e);
                }
            }

            _scalars = sMap;
        }

        return _scalars;
    }
    /**
     * Forces re-evaluation of fields whose values are derived/calculated
     * from the entries map
     */
    protected void resetCalculatedFields ()
    {
        _rows = null;
        _scalars = null;
        _baseOid = null;
    }

    public void setEntries (final Map<String,MIBAttributeEntry> entries)
    {
        _entries = entries;
        resetCalculatedFields();
    }
    /**
     * @param e entry to be added
     * @return previous entry mapped to the OID (null if none)
     * @throws IllegalArgumentException if null entry/OID
     */
    public MIBAttributeEntry addEntry (final MIBAttributeEntry e) throws IllegalArgumentException
    {
        final String    oid=(null == e) ? null : e.getOid();
        if ((null == oid) || (oid.length() <= 0))
            throw new IllegalArgumentException("addEntry(" + e + ") no OID/entry");

        if (null == _entries)
            _entries = new OIDStringsMap<MIBAttributeEntry>();

        resetCalculatedFields();

        return _entries.put(oid, e);
    }
    /**
     * Adds all entries to the MIB group
     * @param entries entries to be added - may be null/empty
     * @param unique if TRUE then throws (@link IllegalStateException) if
     * an added entry already exists
     * @return updated entries map
     * @throws IllegalStateException if an added entry already exists and the
     * <I>unique</I> parameter has been set to TRUE
     * @throws IllegalArgumentException if null OID was found
     * @see #addEntry(MIBAttributeEntry)
     */
    public Map<String,? extends MIBAttributeEntry> addEntries (final Collection<? extends MIBAttributeEntry> entries, final boolean unique) throws IllegalStateException
    {
        if ((entries != null) && (entries.size() > 0))
        {
            for (final MIBAttributeEntry e : entries)
            {
                if (e != null)    // should not be otherwise
                {
                    final MIBAttributeEntry    prev=addEntry(e);
                    if ((prev != null) && unique)
                        throw new IllegalStateException("addEntries() ambiguous entries (" + e.getAttrName() + "/" + prev.getAttrName() + ") for OID=" + e.getOid());
                }
            }
        }

        return getEntries();
    }
    /**
     * @param entries read entries - may be null/empty
     * @return (@link Map) of entries - key=OID, value=entry instance (may be
     * null/empty if no entries to begin with)
     * @throws RuntimeException if null/empty OID or same OID mapped for
     * another entry
     */
    private static final Map<String,? extends MIBAttributeEntry> mapReadEntries (final Collection<? extends MIBAttributeEntry> entries) throws RuntimeException
    {
        if ((null == entries) || (entries.size() <= 0))
            return null;

        final Map<String,MIBAttributeEntry>    eMap=new OIDStringsMap<MIBAttributeEntry>();
        for (final MIBAttributeEntry e : entries)
        {
            final String    oid=(null == e) /* should not happen */ ? null : e.getOid();
            if ((null == oid) || (oid.length() <= 0))    // should not happen
                throw new IllegalArgumentException("mapReadEntries() no OID for entry=" + e);

            final MIBAttributeEntry    prev=eMap.put(oid, e);
            if (prev != null)    // not allowed to have duplicate mappings for same OID
                throw new IllegalStateException("mapReadEntries() OID=" + oid + " already assigned to entry=" + prev);
        }

        return eMap;
    }
    // returns updated collection of removed entries
    private static final Collection<MIBAttributeEntry> removeOID (final Collection<MIBAttributeEntry> eColl, final Map<String,? extends MIBAttributeEntry> eMap, final String oid)
    {
        if ((null == oid) || (oid.length() <= 0)
         || (null == eMap) || (eMap.size() <= 0))
            return eColl;

        final MIBAttributeEntry    e=eMap.remove(oid);
        if (null == e)
            return eColl;

        // allocate a collection if one not provided
        if (null == eColl)
        {
            final Collection<MIBAttributeEntry>    entries=new LinkedList<MIBAttributeEntry>();
            entries.add(e);
            return entries;
        }

        eColl.add(e);
        return eColl;
    }
    // returns updated collection of removed entries
    private static final Collection<MIBAttributeEntry> removeRowEntries (final Collection<MIBAttributeEntry> eColl, final Map<String,? extends MIBAttributeEntry> eMap, final MIBRowEntry row)
    {
        if ((null == row) || (null == eMap) || (eMap.size() <= 0))
            return eColl;

        Collection<MIBAttributeEntry>    entries=removeOID(eColl, eMap, row.getOid());

        // remove the columns (if any)
        final Map<String,? extends MIBAttributeEntry>    cMap=row.getColumns();
        final Collection<? extends MIBAttributeEntry>    cols=((null == cMap) || (cMap.size() <= 0)) ? null : cMap.values();
        if ((cols != null) && (cols.size() > 0))
        {
            for (final MIBAttributeEntry c : cols)
            {
                if (c != null)    // should not be otherwise
                {
                    if (c instanceof MIBRowEntry)
                        entries = removeRowEntries(entries, eMap, (MIBRowEntry) c);
                    else
                        entries = removeOID(entries, eMap, c.getOid());
                }
            }
        }

        return entries;
    }
    // returns collection of removed entries
    private static final Collection<? extends MIBAttributeEntry> removeRowsEntries (final Map<String,? extends MIBAttributeEntry> eMap, final Map<String,? extends MIBRowEntry> rMap)
    {
        if ((null == rMap) || (rMap.size() <= 0)
         || (null == eMap) || (eMap.size() <= 0))
            return null;

        final Collection<? extends MIBRowEntry>    rows=rMap.values();
        Collection<MIBAttributeEntry>            entries=null;
        for (final MIBRowEntry r : rows)
            entries = removeRowEntries(entries, eMap, r);

        return entries;
    }
    /**
     * Builds a MIB group from the "raw" entries (i.e., rows/columns are all
     * "mixed" in as "simple" entries)
     * @param mibName logical name for the MIB group - may NOT be null/empty
     * @param entries "raw" entries
     * @return MIB group data
     * @throws RuntimeException if unable to process the entries
     */
    public static final MIBGroup buildMIBGroup (final String mibName, final Collection<? extends MIBAttributeEntry> entries) throws RuntimeException
    {
        final Map<String,? extends MIBAttributeEntry>    eMap=mapReadEntries(entries);
        final MIBGroup                                        grp=new MIBGroup(mibName);

        // first process all rows - thus "cleaning up" the entries map and leaving only the scalars in it
        {
            Map<String,MIBRowEntry>    rMap=null;
            for (final MIBAttributeEntry e : entries)
            {
                final String    type=(null == e) /* should not happen */ ? null : e.getSyntax();
                if (!SNMPProtocol.SEQUENCEMod.equalsIgnoreCase(type))
                    continue;

                final MIBRowEntry    row=MIBRowEntry.buildMIBRowEntry(e, eMap);
                if (null == rMap)
                    rMap = new OIDStringsMap<MIBRowEntry>();

                rMap.put(row.getOid(), row);
                grp.addEntry(row);
            }

            // remove the rows and all their columns from the entries map - thus leaving only scalars
            removeRowsEntries(eMap, rMap);
        }

        // assume whatever is left is a scalar
        final Collection<? extends MIBAttributeEntry>    scalars=
            ((null == eMap) || (eMap.size() <= 0)) ? null : eMap.values();
        if ((null == scalars) || (scalars.size() <= 0))
            return grp;

        for (final MIBAttributeEntry s : scalars)
        {
            if (s == null)    // should not be otherwise
                continue;

            s.adjustScalarOid();
            grp.addEntry(s);
        }

        return grp;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getMibName();
    }
}
