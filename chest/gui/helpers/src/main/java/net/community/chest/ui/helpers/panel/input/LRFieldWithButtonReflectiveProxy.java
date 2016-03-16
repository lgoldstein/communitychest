/*
 *
 */
package net.community.chest.ui.helpers.panel.input;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link LRFieldWithButtonPanel}
 * @author Lyor G.
 * @since Aug 20, 2008 3:15:53 PM
 */
public class LRFieldWithButtonReflectiveProxy<P extends LRFieldWithButtonPanel>
        extends FieldWithButtonPanelReflectiveProxy<P> {
    public LRFieldWithButtonReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected LRFieldWithButtonReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final LRFieldWithButtonReflectiveProxy<LRFieldWithButtonPanel>    LRFLDBTNPNL=
        new LRFieldWithButtonReflectiveProxy<LRFieldWithButtonPanel>(LRFieldWithButtonPanel.class, true);
}
