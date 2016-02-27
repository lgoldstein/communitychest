/*
 * 
 */
package net.community.chest.swing.component.panel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link BasePanel}
 * @author Lyor G.
 * @since Dec 16, 2008 10:39:26 AM
 */
public class BasePanelReflectiveProxy<P extends BasePanel> extends JPanelReflectiveProxy<P> {
	// need it for "setLocale" and other extensions
	public BasePanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected BasePanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final BasePanelReflectiveProxy<BasePanel>	BASEPNL=
			new BasePanelReflectiveProxy<BasePanel>(BasePanel.class, true);
}
