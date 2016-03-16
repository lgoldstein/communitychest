package net.community.chest.net.proto.text.imap4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 11:02:59 AM
 */
public class IMAP4FolderInfo implements Serializable, PubliclyCloneable<IMAP4FolderInfo> {
    /**
     *
     */
    private static final long serialVersionUID = -5349845453081791646L;
    public IMAP4FolderInfo ()
    {
        super();
    }
    /**
     * Folder name (after translating any encoding)
     */
    private String _name /* =null */;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }
    /**
     * Hierarchy separator
     */
    private char _hirSep    /* ='\0' */;
    /**
     * @return hierarchy delimiter used for sub-folders - <B>Note:</B> may be '\0' sometimes. In this case,
     * this means that no sub-folders may be created under this folder.
     *
     */
    public char getHierarchySeparator ()
    {
        return _hirSep;
    }

    public void setHierarchySeparator (char sep)
    {
        _hirSep = sep;
    }
    /**
     * Access flags
     */
    private Collection<IMAP4FolderFlag>   _flags /* =null */;
    public Collection<IMAP4FolderFlag> getFlags ()
    {
        return _flags;
    }

    public void setFlags (Collection<IMAP4FolderFlag> flags)
    {
        _flags = flags;
    }
    /**
     * Constructs a folder info object
     * @param name folder name (may NOT be null/empty)
     * @param hirSep hierarchy separator
     * @param flags folder flags - may be null/empty
     * @throws IllegalArgumentException if null/empty name
     */
    public IMAP4FolderInfo (String name, char hirSep, Collection<IMAP4FolderFlag> flags)
    {
        this();

        if ((null == (_name=name)) || (name.length() <= 0))
            throw new IllegalArgumentException("No folder name specified");
        _hirSep = hirSep;
        _flags = flags;
    }
    /**
     * Initializes the object to bad/illegal values
     */
    public void reset ()
    {
        setName(null);
        setHierarchySeparator('\0');

        {
            final Collection<IMAP4FolderFlag>    flags=getFlags();
            if (flags != null)
                flags.clear();
        }
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof IMAP4FolderInfo)))
            return false;
        if (this == obj)
            return true;

        final IMAP4FolderInfo    fi=(IMAP4FolderInfo) obj;
        return (0 == StringUtil.compareDataStrings(getName(), fi.getName(), true))
            && (getHierarchySeparator() == fi.getHierarchySeparator())
            && IMAP4FlagValue.compareFlags(IMAP4FlagValue.buildFlagsMap(getFlags()), IMAP4FlagValue.buildFlagsMap(fi.getFlags()))
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), true)
            + getHierarchySeparator()
            + IMAP4FlagValue.calculateFlagsHashCode(getFlags())
            ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getName();
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public IMAP4FolderInfo clone () throws CloneNotSupportedException
    {
        final IMAP4FolderInfo                fi=getClass().cast(super.clone());
        final Collection<IMAP4FolderFlag>    flags=getFlags();
        if (flags != null)
        {
            final Collection<IMAP4FolderFlag>    clf=new ArrayList<IMAP4FolderFlag>(flags.size());
            for (final IMAP4FolderFlag f : flags)
            {
                if (f != null)
                    clf.add(f.clone());
            }

            fi.setFlags(clf);
        }

        return fi;
    }
    /**
     * Fixed name of INBOX folder
     */
    public static final String IMAP4_INBOX="INBOX";
    /**
     * Characters which require sending the folder as literal if they appeat in its name
     */
    public static final char[] IMAP4SpecialFolderChars={
        IMAP4Protocol.IMAP4_PARLIST_SDELIM,        /* ( */
        IMAP4Protocol.IMAP4_PARLIST_EDELIM,        /* ) */
        IMAP4Protocol.IMAP4_OCTCNT_SDELIM,        /* { */
        IMAP4Protocol.IMAP4_QUOTE_DELIM,        /* " */
//        IMAP4Protocol.IMAP4_MSGRANGE_WILDCARD,    /* * */
        IMAP4Protocol.IMAP4_LISTWILDCARD,        /* % */
        '\\',
    };
    /**
     * Checks if the folder name contains characters that require it to be sent as a literal value
     * @param v value to be checked
     * @return TRUE if value needs be sent using a literal
     */
    public static final boolean requiresLiteralSend (String v)
    {
        if (null == v)
            return true;

        for (int    i=0; i < IMAP4SpecialFolderChars.length; i++)
            if (v.indexOf(IMAP4SpecialFolderChars[i]) != (-1))
                return true;

        return false;
    }

    public static final Map<String,IMAP4FolderInfo> buildFoldersMap (final Collection<IMAP4FolderInfo> folders)
    {
        if ((null == folders) || (folders.size() <= 0))
            return null;

        final Map<String,IMAP4FolderInfo>    fMap=new TreeMap<String, IMAP4FolderInfo>();
        for (final IMAP4FolderInfo f : folders)
        {
            if (f != null)    // should not be otherwise
                fMap.put(f.getName(), f);
        }

        return fMap;
    }

    public static final boolean compareFolders (final Collection<String> folders, final Map<String,IMAP4FolderInfo> m)
    {
        if ((null == folders) || (folders.size() <= 0))
            return ((null == m) || (m.size() <= 0));
        else if ((null == m) || (m.size() <= 0))
            return false;
        else if (m.size() != folders.size())
            return false;

        for (final String f : folders)
        {
            if ((f != null) && (f.length() > 0) && (null == m.get(f)))
                return false;
        }

        return true;
    }

    public static final boolean compareFolders (final Map<String,IMAP4FolderInfo> m1, final Map<String,IMAP4FolderInfo> m2)
    {
        if ((null == m1) || (m1.size() <= 0))
            return ((null == m2) || (m2.size() <= 0));
        else if ((null == m2) || (m2.size() <= 0))
            return false;
        else if (m1.size() != m2.size())
            return false;

        return compareFolders(m1.keySet(), m2)
            && compareFolders(m2.keySet(), m1);
    }

    public static final int calculateFoldersHashCode (final Collection<IMAP4FolderInfo> folders)
    {
        int    nRes=0;
        if ((folders != null) && (folders.size() > 0))
        {
            for (final IMAP4FolderInfo f : folders)
            {
                if (f != null)
                    nRes += f.hashCode();
            }
        }

        return nRes;
    }
}
