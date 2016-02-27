package net.community.chest.awt.layout.border;

import java.awt.BorderLayout;
import java.awt.Component;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 18, 2007 4:45:50 PM
 */
public class BaseBorderLayout extends BorderLayout
		implements XmlConvertible<BaseBorderLayout>, PubliclyCloneable<BaseBorderLayout> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4841217007372425802L;
	public BaseBorderLayout ()
	{
		super();
	}

	public BaseBorderLayout (int hgap, int vgap)
	{
		super(hgap, vgap);
	}

	public void addLayoutComponent (Component comp, BorderLayoutPosition position)
	{
		super.addLayoutComponent(comp, (null == position) ? null : position.getPosition());
	}

	public XmlProxyConvertible<?> getLayoutConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : BorderLayoutReflectiveProxy.BORDER;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseBorderLayout fromXml (final Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getLayoutConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

		return this;
	}

	public BaseBorderLayout (final Element elem) throws Exception
	{
		if (fromXml(elem) != this)
			throw new IllegalStateException("Mismatched constructed instances");
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public BaseBorderLayout clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}
