package net.community.chest.swing.component.dialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JDialog;

import net.community.chest.awt.LocalizedComponent;
import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.awt.dom.proxy.ContainerReflectiveProxy;
import net.community.chest.awt.focus.ByComponentFocusTraversalPolicy;
import net.community.chest.awt.focus.ByNameFocusTraversalPolicy;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.WindowCloseOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some useful XML creation method(s)</P>
 * 
 * @author Lyor G.
 * @since Aug 6, 2007 2:11:10 PM
 */
public class BaseDialog extends JDialog
		implements XmlConvertible<BaseDialog>, LocalizedComponent,
					Titled, Enabled, Foregrounded, Backgrounded {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6175578440759949674L;
	public BaseDialog () throws HeadlessException
	{
		super();
	}

	public BaseDialog (Frame owner) throws HeadlessException
	{
		super(owner);
	}

	public BaseDialog (Dialog owner) throws HeadlessException
	{
		super(owner);
	}

	public BaseDialog (Frame owner, boolean modal) throws HeadlessException
	{
		super(owner, modal);
	}

	public BaseDialog (Frame owner, String title) throws HeadlessException
	{
		super(owner, title);
	}

	public BaseDialog (Dialog owner, boolean modal) throws HeadlessException
	{
		super(owner, modal);
	}

	public BaseDialog (Dialog owner, String title) throws HeadlessException
	{
		super(owner, title);
	}

	public BaseDialog (Frame owner, String title, boolean modal) throws HeadlessException
	{
		super(owner, title, modal);
	}

	public BaseDialog (Dialog owner, String title, boolean modal) throws HeadlessException
	{
		super(owner, title, modal);
	}

	public BaseDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	public BaseDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException
	{
		super(owner, title, modal, gc);
	}

	public BaseDialog (Window owner, ModalityType modalityType)
	{
		super(owner, modalityType);
	}

	public BaseDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
	{
		super(owner, title, modalityType, gc);
	}

	public BaseDialog (Window owner, String title, ModalityType modalityType)
	{
		super(owner, title, modalityType);
	}

	public BaseDialog (Window owner, String title)
	{
        this(owner, title, Dialog.ModalityType.MODELESS);     
	}

	public BaseDialog (Window owner)
	{
		this(owner, Dialog.ModalityType.MODELESS);
	}

	public WindowCloseOptions getDefaultCloseOperationAction ()
	{
		return WindowCloseOptions.fromCloseOption(getDefaultCloseOperation());
	}

	public void setDefaultCloseOperationAction (final WindowCloseOptions opt)
	{
		setDefaultCloseOperation(opt.getCloseOption());
	}

	protected XmlProxyConvertible<?> getDialogConverter (final Element elem)
	{
		return (null == elem) ? null : BaseDialogReflectiveProxy.BASEDLG;
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseDialog fromXml (final Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getDialogConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					co=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (co != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched updated instances");

		return this;
	}

	public BaseDialog (final Frame owner, final Element elem) throws Exception
	{
		super(owner);

		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + "[" + Frame.class.getSimpleName() + "] mismatched instances");
	}

	public BaseDialog (final Dialog owner, final Element elem) throws Exception
	{
		super(owner);

		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + "[" + Dialog.class.getSimpleName() + "] mismatched instances");
	}

	public BaseDialog (final Window owner, final Element elem) throws Exception
	{
		super(owner);

		final Object	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + "[" + Window.class.getSimpleName() + "] mismatched instances");
	}

	public BaseDialog (final Element elem) throws Exception
	{
		this((Window) null, elem);
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}

	private Locale	_lcl	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#getDisplayLocale()
	 */
	@Override
	public synchronized Locale getDisplayLocale ()
	{
		if (null == _lcl)
			_lcl = Locale.getDefault();
		return _lcl;
	}
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#setDisplayLocale(java.util.Locale)
	 */
	@Override
	public synchronized void setDisplayLocale (Locale l)
	{
		if (_lcl != l)	// debug breakpoint
			_lcl = l;
	}

	public <C extends Component> C addConstrainedComponent (C comp, Node constValue)
	{
		return ContainerReflectiveProxy.addConstrainedComponent(this, comp, constValue);
	}

	public FocusTraversalPolicy setFocusTraversalPolicy (List<Component> comps)
	{
		if ((null == comps) || (comps.size() <= 0))
			return getFocusTraversalPolicy();

		final ByComponentFocusTraversalPolicy	p=new ByComponentFocusTraversalPolicy(comps);
		setFocusTraversalPolicy(p);
		return p;
	}

	public FocusTraversalPolicy setFocusTraversalPolicy (Component ... comps)
	{
		return setFocusTraversalPolicy(((null == comps) || (comps.length <= 0)) ? null : Arrays.asList(comps));
	}
	
	public FocusTraversalPolicy setFocusTraversalPolicy (String ... comps)
	{
		if ((null == comps) || (comps.length <= 0))
			return getFocusTraversalPolicy();

		final ByNameFocusTraversalPolicy	p=new ByNameFocusTraversalPolicy(comps);
		setFocusTraversalPolicy(p);
		return p;
	}
}
