package net.community.chest.jndi;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 9:39:44 AM
 */
public class EmbeddedContext implements Context {
	/**
	 * Actual embedded context object
	 */
	private Context	_realContext;
	/**
	 * @return the currently embedded context (may be null if none set)
	 */
	public Context getRealContext ()
	{
		return _realContext;
	}
	/**
	 * @param realContext the real context to be embedded - may be null
	 */
	public void setRealContext (Context realContext)
	{
		_realContext = realContext;
	}

	private boolean	_autoClose;
	/* NOTE !!! INTERFACE NOT IMPLEMENTED DUE TO 'throws' CLAUSE MISMATCH
	 * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
	 */
	public boolean isRealClosure ()
	{
		return _autoClose;
	}
	/* NOTE !!! INTERFACE NOT IMPLEMENTED DUE TO 'throws' CLAUSE MISMATCH
	 * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
	 */
	public void setRealClosure (boolean autoClose)
	{
		_autoClose = autoClose;
	}
	/* NOTE !!! INTERFACE NOT IMPLEMENTED DUE TO 'throws' CLAUSE MISMATCH
	 * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
	 */
	public boolean isMutableRealClosure ()
	{
		return true;
	}
	/**
	 * No initial context value set - use {@link #setRealContext(Context)}
	 * at some stage.
	 * @param autoClose if TRUE, then calling {@link #close()} on the
	 * embedding object also closes the original context 
	 * @see #setRealContext(Context)
	 */
	public EmbeddedContext (boolean autoClose)
	{
		_autoClose = autoClose;
	}
	/**
	 * No initial context + automatically close any subsequent provided
	 * real context via {@link #setRealContext(Context)}
	 * @see #EmbeddedContext(boolean)
	 * @see #setRealContext(Context)
	 */
	public EmbeddedContext ()
	{
		this(true);
	}
	/**
	 * @param realContext context object to embed - may NOT be null
	 * @param autoClose if TRUE, then calling {@link #close()} on the
	 * embedding object also closes the original context 
	 * @throws NamingException if null "real" context supplied
	 */
	public EmbeddedContext (Context realContext, boolean autoClose) throws NamingException
	{
		if (null == (_realContext=realContext))
			throw new NamingException("No real context to embed");
		_autoClose = autoClose;
	}
	/**
	 * Constructor
	 * @param realContext context object to embed - may NOT be null.
	 * NOTE: when calling {@link #close()} on the embedding object also closes
	 * the original context 
	 * @throws NamingException if null "real" context supplied
	 * @see #EmbeddedContext(Context, boolean)
	 * @see #setRealClosure(boolean)
	 */
	public EmbeddedContext (Context realContext) throws NamingException
	{
		this(realContext, true);
	}
	/* @see #_autoClose
	 * @see javax.naming.Context#close()
	 */
	@Override
	public void close () throws NamingException
	{
		final Context	ctx=getRealContext();
		if (ctx != null)
		{
			try
			{
				if (isRealClosure())
					ctx.close();
			}
			finally
			{
				setRealContext(null);
			}
		}
	}
	/**
	 * Checks that the current embedded context object is non-null 
	 * @param caller caller ID to be used in thrown exception
	 * @throws NamingException if no current embedded context object
	 */
	protected void validateCurrentContext (String caller) throws NamingException
	{
		if (null == _realContext)
			throw new NamingException(caller + ": no current underlying context");
	}
	/*
	 * @see javax.naming.Context#getNameInNamespace()
	 */
	@Override
	public String getNameInNamespace () throws NamingException
	{
		validateCurrentContext("getNameInNamespace");
		return _realContext.getNameInNamespace();
	}
	/*
	 * @see javax.naming.Context#destroySubcontext(java.lang.String)
	 */
	@Override
	public void destroySubcontext (String name) throws NamingException
	{
		validateCurrentContext("destroySubcontext[String]");
		_realContext.destroySubcontext(name);
	}
	/*
	 * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
	 */
	@Override
	public void destroySubcontext (Name name) throws NamingException
	{
		validateCurrentContext("destroySubcontext[Name]");
		_realContext.destroySubcontext(name);
	}
	/*
	 * @see javax.naming.Context#unbind(java.lang.String)
	 */
	@Override
	public void unbind (String name) throws NamingException
	{
		validateCurrentContext("unbind[String]");
		_realContext.unbind(name);
	}
	/*
	 * @see javax.naming.Context#unbind(javax.naming.Name)
	 */
	@Override
	public void unbind (Name name) throws NamingException
	{
		validateCurrentContext("unbind[Name]");
		_realContext.unbind(name);
	}
	/*
	 * @see javax.naming.Context#getEnvironment()
	 */
	@Override
	public Hashtable<?,?> getEnvironment () throws NamingException
	{
		validateCurrentContext("getEnvironment");
		return _realContext.getEnvironment();
	}
	/*
	 * @see javax.naming.Context#lookup(java.lang.String)
	 */
	@Override
	public Object lookup (String name) throws NamingException
	{
		validateCurrentContext("lookup[String]");
		return _realContext.lookup(name);
	}
	/*
	 * @see javax.naming.Context#lookup(javax.naming.Name)
	 */
	@Override
	public Object lookup (Name name) throws NamingException
	{
		validateCurrentContext("lookup[Name]");
		return _realContext.lookup(name);
	}
	/*
	 * @see javax.naming.Context#lookupLink(java.lang.String)
	 */
	@Override
	public Object lookupLink (String name) throws NamingException
	{
		validateCurrentContext("lookupLink[String]");
		return _realContext.lookupLink(name);
	}
	/*
	 * @see javax.naming.Context#lookupLink(javax.naming.Name)
	 */
	@Override
	public Object lookupLink (Name name) throws NamingException
	{
		validateCurrentContext("lookupLink[Name]");
		return _realContext.lookupLink(name);
	}
	/*
	 * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
	 */
	@Override
	public Object removeFromEnvironment (String propName) throws NamingException
	{
		validateCurrentContext("removeFromEnvironment");
		return _realContext.removeFromEnvironment(propName);
	}
	/*
	 * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
	 */
	@Override
	public void bind (String name, Object obj) throws NamingException
	{
		validateCurrentContext("bind[String]");
		_realContext.bind(name, obj);
	}
	/*
	 * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
	 */
	@Override
	public void bind (Name name, Object obj) throws NamingException
	{
		validateCurrentContext("bind[Name]");
		_realContext.bind(name, obj);
	}
	/**
	 * Binds the object creating any required sub-contexts up to the path
	 * @param name JNDI name/path
	 * @param obj object to bind
	 * @return bound object - same as input
	 * @throws NamingException if unable to create sub-contexts or bind the object
	 */
	public Object makeBind (String name, Object obj) throws NamingException
	{
		return ContextUtils.bindJNDIObject(this, obj, name);
	}
	/**
	 * Binds the object creating any required sub-contexts up to the path
	 * @param name JNDI name/path
	 * @param obj object to bind
	 * @return bound object - same as input
	 * @throws NamingException if unable to create sub-contexts or bind the object
	 */
	public Object makeBind (Name name, Object obj) throws NamingException
	{
		return ContextUtils.bindJNDIObject(this, obj, name);
	}
	/*
	 * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
	 */
	@Override
	public void rebind (String name, Object obj) throws NamingException
	{
		validateCurrentContext("rebind[String]");
		_realContext.rebind(name, obj);
	}
	/*
	 * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
	 */
	@Override
	public void rebind (Name name, Object obj) throws NamingException
	{
		validateCurrentContext("rebind[Name]");
		_realContext.rebind(name, obj);
	}
	/*
	 * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
	 */
	@Override
	public void rename (String oldName, String newName) throws NamingException
	{
		validateCurrentContext("rename[String,String]");
		_realContext.rename(oldName, newName);
	}
	/*
	 * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
	 */
	@Override
	public void rename (Name oldName, Name newName) throws NamingException
	{
		validateCurrentContext("rename[Name,Name]");
		_realContext.rename(oldName, newName);
	}
	/*
	 * @see javax.naming.Context#createSubcontext(java.lang.String)
	 */
	@Override
	public Context createSubcontext (String name) throws NamingException
	{
		validateCurrentContext("createSubcontext[String]");
		return _realContext.createSubcontext(name);
	}
	/*
	 * @see javax.naming.Context#createSubcontext(javax.naming.Name)
	 */
	@Override
	public Context createSubcontext (Name name) throws NamingException
	{
		validateCurrentContext("createSubcontext[Name]");
		return _realContext.createSubcontext(name);
	}
	/*
	 * @see javax.naming.Context#getNameParser(java.lang.String)
	 */
	@Override
	public NameParser getNameParser (String name) throws NamingException
	{
		validateCurrentContext("getNameParser[String]");
		return _realContext.getNameParser(name);
	}
	/*
	 * @see javax.naming.Context#getNameParser(javax.naming.Name)
	 */
	@Override
	public NameParser getNameParser (Name name) throws NamingException
	{
		validateCurrentContext("getNameParser[Name]");
		return _realContext.getNameParser(name);
	}
	/*
	 * @see javax.naming.Context#list(java.lang.String)
	 */
	@Override
	public NamingEnumeration<NameClassPair> list (String name) throws NamingException
	{
		validateCurrentContext("list[String]");
		return _realContext.list(name);
	}
	/*
	 * @see javax.naming.Context#list(javax.naming.Name)
	 */
	@Override
	public NamingEnumeration<NameClassPair> list (Name name) throws NamingException
	{
		validateCurrentContext("list[Name]");
		return _realContext.list(name);
	}
	/*
	 * @see javax.naming.Context#listBindings(java.lang.String)
	 */
	@Override
	public NamingEnumeration<Binding> listBindings (String name) throws NamingException
	{
		validateCurrentContext("listBindings[String]");
		return _realContext.listBindings(name);
	}
	/*
	 * @see javax.naming.Context#listBindings(javax.naming.Name)
	 */
	@Override
	public NamingEnumeration<Binding> listBindings (Name name) throws NamingException
	{
		validateCurrentContext("listBindings[Name]");
		return _realContext.listBindings(name);
	}
	/*
	 * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object addToEnvironment (String propName, Object propVal) throws NamingException
	{
		validateCurrentContext("addToEnvironment");
		return _realContext.addToEnvironment(propName, propVal);
	}
	/*
	 * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
	 */
	@Override
	public String composeName (String name, String prefix) throws NamingException
	{
		validateCurrentContext("composeName[String,String]");
		return _realContext.composeName(name, prefix);
	}
	/*
	 * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
	 */
	@Override
	public Name composeName (Name name, Name prefix) throws NamingException
	{
		validateCurrentContext("composeName[Name,Name]");
		return _realContext.composeName(name, prefix);
	}
	/* @return TRUE if object is in effect the same context reference
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof Context)))
			return false;
		if (this == obj)
			return true;

		if (obj instanceof EmbeddedContext)
			return _realContext == ((EmbeddedContext) obj)._realContext;
		else
			return _realContext == obj;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return (null == _realContext) ? 0 : _realContext.hashCode();
	}

}
