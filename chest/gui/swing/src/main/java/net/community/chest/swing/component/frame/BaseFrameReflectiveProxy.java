/*
 * 
 */
package net.community.chest.swing.component.frame;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link BaseFrame} instance
 * @author Lyor G.
 * @since Dec 16, 2008 10:46:31 AM
 */
public class BaseFrameReflectiveProxy<F extends BaseFrame> extends JFrameReflectiveProxy<F> {
	public BaseFrameReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected BaseFrameReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final BaseFrameReflectiveProxy<BaseFrame>	BASEFRM=
			new BaseFrameReflectiveProxy<BaseFrame>(BaseFrame.class, true);
}
