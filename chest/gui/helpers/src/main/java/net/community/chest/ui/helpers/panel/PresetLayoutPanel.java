/*
 * 
 */
package net.community.chest.ui.helpers.panel;

import java.awt.LayoutManager;

import net.community.chest.dom.impl.StandaloneDocumentImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a one-and-only {@link LayoutManager} type
 * 
 * @param <M> The preset {@link LayoutManager}
 * @author Lyor G.
 * @since Aug 25, 2008 12:26:00 PM
 */
public class PresetLayoutPanel<M extends LayoutManager> extends HelperPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1380753226597049026L;
	private final Class<M>	_lmClass;
	public final Class<M> getLayoutManagerClass ()
	{
		return _lmClass;
	}
	/* Allow only the preset layout type
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	@Override
	public void setLayout (final LayoutManager mgr)
	{
		// NOTE !!! 1st call comes from the constructor BEFORE _lmClass is initialized
		final Class<?>	mgrClass=(null == mgr) ? null : mgr.getClass(),
						lmClass=getLayoutManagerClass();
		if ((mgrClass != null) && (lmClass != null) && (!lmClass.isAssignableFrom(mgrClass)))
			throw new IllegalArgumentException("setLayout(" + mgrClass.getName() + ") non-" + lmClass.getSimpleName() + " N/A");

		super.setLayout(mgr);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm, final Document doc, final boolean autoLayout)
	{
		super(lm, doc, false /* delay auto-layout till LM class set */);

		if (null == (_lmClass=lmClass))
			throw new IllegalArgumentException("No " + LayoutManager.class.getSimpleName() + " instance provided");

		if (autoLayout)
			layoutComponent();
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm, final Document doc)
	{
		this(lmClass, lm, doc, true);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm, final Element elem, final boolean autoLayout)
	{
		this(lmClass, lm, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm, final Element elem)
	{
		this(lmClass, lm, elem, true);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm, final boolean autoLayout)
	{
		this(lmClass, lm, (Document) null, autoLayout);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final M lm)
	{
		this(lmClass, lm, true);
	}

	protected PresetLayoutPanel (final Class<M> lmClass, final boolean autoLayout) throws Exception
	{
		this(lmClass, lmClass.newInstance(), autoLayout);
	}

	protected PresetLayoutPanel (final Class<M> lmClass) throws Exception
	{
		this(lmClass, true);
	}

	@SuppressWarnings("unchecked")
	protected PresetLayoutPanel (final M lm)
	{
		this((Class<M>) ((null == lm) ? null : lm.getClass()), lm);
	}
}
