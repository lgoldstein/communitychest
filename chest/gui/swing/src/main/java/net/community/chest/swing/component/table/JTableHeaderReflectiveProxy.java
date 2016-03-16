/*
 *
 */
package net.community.chest.swing.component.table;

import javax.swing.table.JTableHeader;

import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <H> The reflected {@link JTableHeader}
 * @author Lyor G.
 * @since Sep 23, 2008 10:32:56 AM
 */
public class JTableHeaderReflectiveProxy<H extends JTableHeader> extends JComponentReflectiveProxy<H> {
    public JTableHeaderReflectiveProxy (Class<H> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JTableHeaderReflectiveProxy (Class<H> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final JTableHeaderReflectiveProxy<JTableHeader>    TBLHDR=
        new JTableHeaderReflectiveProxy<JTableHeader>(JTableHeader.class, true);
}
