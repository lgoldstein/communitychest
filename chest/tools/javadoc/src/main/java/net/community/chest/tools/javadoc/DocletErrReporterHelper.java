package net.community.chest.tools.javadoc;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.SourcePosition;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Uses a cached reporter instance</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 11:14:20 AM
 */
public abstract class DocletErrReporterHelper extends DocletErrReporter {
    /**
     * Cached reporter - may be null
     */
    private DocErrorReporter    _reporter    /* =null */;
    public DocErrorReporter getReporter ()
    {
        return _reporter;
    }
    /*
     * @see net.community.chest.tools.javadoc.DocletErrReporter#print(net.community.chest.tools.javadoc.DocErrorLevel, com.sun.javadoc.SourcePosition, java.lang.String)
     */
    @Override
    public void print (DocErrorLevel lvl, SourcePosition pos, String msg)
    {
        report(getReporter(), lvl, pos, msg);
    }

    public void setReporter (DocErrorReporter reporter)
    {
        _reporter = reporter;
    }
    /**
     * Initialized constructor
     * @param reporter reporter to be used - may be null
     * @see #getReporter()
     * @see #setReporter(DocErrorReporter)
     */
    protected DocletErrReporterHelper (DocErrorReporter reporter)
    {
        super();
        setReporter(reporter);
    }
    /**
     * Initialized constructor
     * @param reporter reporter to be used - may be null. If not, then its
     * debug/verbose state is "copied" to this object as well
     * @see #getReporter()
     * @see #setReporter(DocErrorReporter)
     */
    protected DocletErrReporterHelper (DocletErrReporter reporter)
    {
        super(reporter);
        setReporter(reporter);
    }
    /**
     * Empty/default constructor - no reporter set
     * @see #DocletErrReporterHelper(DocErrorReporter)
     */
    protected DocletErrReporterHelper ()
    {
        this((DocletErrReporter) null);
    }
}
