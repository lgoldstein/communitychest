/*
 *
 */
package net.community.chest.ui.components.spinner.margin;

import net.community.chest.swing.component.spinner.JSpinnerReflectiveProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <S> The reflected {@link MarginSpinner} type
 * @author Lyor G.
 * @since Mar 11, 2009 11:31:52 AM
 *
 */
public class MarginSpinnerReflectiveProxy<S extends MarginSpinner> extends JSpinnerReflectiveProxy<S> {
    protected MarginSpinnerReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public MarginSpinnerReflectiveProxy (Class<S> objClass)
            throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final MarginSpinnerReflectiveProxy<MarginSpinner> MRGNSPIN=
        new MarginSpinnerReflectiveProxy<MarginSpinner>(MarginSpinner.class, true);
}
