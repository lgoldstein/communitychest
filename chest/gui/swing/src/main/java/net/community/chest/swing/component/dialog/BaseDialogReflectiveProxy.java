/*
 * 
 */
package net.community.chest.swing.component.dialog;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The reflected {@link BaseDialog} instance
 * @author Lyor G.
 * @since Dec 16, 2008 10:43:16 AM
 */
public class BaseDialogReflectiveProxy<D extends BaseDialog> extends JDialogReflectiveProxy<D> {
	// need it for "setLocale" and other extensions
	public BaseDialogReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected BaseDialogReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final BaseDialogReflectiveProxy<BaseDialog>	BASEDLG=
			new BaseDialogReflectiveProxy<BaseDialog>(BaseDialog.class, true);
}
