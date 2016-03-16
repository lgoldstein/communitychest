/*
 *
 */
package net.community.chest.swing.component.filechooser;

import java.util.Collection;

import javax.swing.filechooser.FileNameExtensionFilter;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 28, 2008 11:33:54 AM
 */
public class FileNameExtensionFilterXmlValueInstantiator
            extends BaseTypedValuesContainer<FileNameExtensionFilter>
            implements XmlValueInstantiator<FileNameExtensionFilter> {
    public FileNameExtensionFilterXmlValueInstantiator ()
    {
        super(FileNameExtensionFilter.class);
    }

    public static final String    DESCRIPTION_ATTR="description",
                                EXTS_ATTR="exts";
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public FileNameExtensionFilter fromXml (Element elem) throws Exception
    {
        final String                d=elem.getAttribute(DESCRIPTION_ATTR),
                                    xls=elem.getAttribute(EXTS_ATTR);
        final Collection<String>    xlc=StringUtil.splitString(xls, ',');
        final int                    numExts=(null == xlc) ? 0 : xlc.size();
        final String[]                xla=(numExts <= 0) ? null : xlc.toArray(new String[numExts]);
        return new FileNameExtensionFilter(d, xla);
    }

    public static final FileNameExtensionFilterXmlValueInstantiator    DEFAULT=
                                    new FileNameExtensionFilterXmlValueInstantiator();
}
