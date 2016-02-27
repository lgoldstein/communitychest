/*
 * 
 */
package net.community.chest.ui.components.logging;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.nio.channels.Channel;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.io.EOLStyle;
import net.community.chest.swing.text.MutableAttributeSetXmlProxy;
import net.community.chest.ui.helpers.text.HelperTextPane;
import net.community.chest.util.logging.LogLevelWrapper;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 2, 2009 10:08:12 AM
 */
public class LoggingTextPane extends HelperTextPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7603101835552314160L;
	public LoggingTextPane (Element elem, boolean autoLayout)
	{
		super(elem, autoLayout);
	}

	public LoggingTextPane (Element elem)
	{
		this(elem, true);
	}

	public LoggingTextPane (boolean autoLayout)
	{
		this(null, autoLayout);
	}

	public LoggingTextPane ()
	{
		this(true);
	}

	public LoggingTextPane (StyledDocument doc)
	{
		super(doc);
	}
	/*
	 * @see net.community.chest.swing.component.text.BaseTextPane#getPaneConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPaneConverter (Element elem)
	{
		return (null == elem) ? null : LoggingTextPaneReflectiveProxy.LOGPANE;
	}

	private Map<LogLevelWrapper,AttributeSet>	_attrsMap;
	public Map<LogLevelWrapper,AttributeSet> getLogLevelAttributesMap ()
	{
		return _attrsMap;
	}

	public AttributeSet getLogLevelAttributes (LogLevelWrapper l)
	{
		final Map<LogLevelWrapper,? extends AttributeSet>	m=
			(null == l) ? null : getLogLevelAttributesMap();
		if ((null == m) || (m.size() <= 0))
			return null;

		return m.get(l);
	}

	public void setLogLevelAttributesMap (Map<LogLevelWrapper,AttributeSet> m)
	{
		_attrsMap = m;
	}

	public void setLogLevelAttributes (LogLevelWrapper l, AttributeSet s /* null == remove */)
	{
		if (null == l)
			return;

		Map<LogLevelWrapper,AttributeSet>	m=getLogLevelAttributesMap();
		if (null == m)
		{
			if (null == s)	// if required to delete the attribute then fine...
				return;

			setLogLevelAttributesMap(new EnumMap<LogLevelWrapper,AttributeSet>(LogLevelWrapper.class));
			if (null == (m=getLogLevelAttributesMap()))
				throw new IllegalStateException("setLogLevelAttributes(" + l + ")[" + s + "] no map available though one created");
		}

		final AttributeSet	prev=(null == s) ? m.remove(l) : m.put(l, s);
		if (s == prev)	// debug breakpoint
			return;
	}
	/**
	 * The type of (E)nd-(O)f-(L)ine to use - if <code>null</code> then no
	 * EOL is generated
	 */
	private EOLStyle	_eolStyle;
	public EOLStyle getEolStyle ()
	{
		return _eolStyle;
	}

	public void setEolStyle (EOLStyle eolStyle)
	{
		_eolStyle = eolStyle;
	}
	/**
	 * Maximum number of characters to display in the pane (unlimited
	 * if non-positive).
	 */
	private int	_maxTextSize;
	public int getMaxTextSize ()
	{
		return _maxTextSize;
	}

	public void setMaxTextSize (int maxSize)
	{
		_maxTextSize = maxSize;
	}
	/**
	 * <P>Copyright 2009 as per GPLv2</P>
	 *
	 * <P>Used to find the EOL location closest to the given start offset</P>
	 * @author Lyor G.
	 * @since Aug 2, 2009 12:30:29 PM
	 */
	private static class EOLFinder extends Writer implements Channel {
		private final int	_startOffset;
		private int			_curOffset, _eolOffset;
		// returns how much of the remaining data still needs to be scanned
		private int updateCurrentOffset (final int len)
		{
			if ((len <= 0) || (_curOffset >= _startOffset))
				return len;

			final int	remOffset=_startOffset - _curOffset,
						remLen=Math.min(len, remOffset);
			_curOffset += remLen;
			return (len - remLen);
		}

		public int getEOLOffset ()
		{
			return _eolOffset;
		}

		public EOLFinder (final int	startOffset)
		{
			_startOffset = startOffset;
			_eolOffset = startOffset;
		}

		private char	_eolChar;
		/*
		 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
		 */
		@Override
		public Writer append (CharSequence csq, int start, int end) throws IOException
		{
			if (!isOpen())
				throw new IOException("append(" + csq + ")[" + start + "-" + end + "] not open");

			final int	len=end - start;
			if ((len <= 0) || ('\n' == _eolChar))
				return this;

			final int	remLen=updateCurrentOffset(len);
			if (remLen <= 0)
				return this;

			for (int	scanPos=end - remLen; scanPos < end; scanPos++, _curOffset++)
			{
				if ('\n' == (_eolChar=csq.charAt(scanPos)))
				{
					_eolOffset = _curOffset;
					break;
				}
			}

			return this;
		}
		/*
		 * @see java.io.Writer#append(java.lang.CharSequence)
		 */
		@Override
		public Writer append (CharSequence csq) throws IOException
		{
			return append(csq, 0, (null == csq) ? 0 : csq.length());
		}

		private boolean	_isOpen=true;
		/*
		 * @see java.nio.channels.Channel#isOpen()
		 */
		@Override
		public boolean isOpen ()
		{
			return _isOpen;
		}
		/*
		 * @see java.io.Writer#close()
		 */
		@Override
		public void close () throws IOException
		{
			if (isOpen())
				_isOpen = false;
		}
		/*
		 * @see java.io.Writer#flush()
		 */
		@Override
		public void flush () throws IOException
		{
			if (!isOpen())
				throw new IOException("flush() not open");
		}
		/*
		 * @see java.io.Writer#write(char[], int, int)
		 */
		@Override
		public void write (char[] cbuf, int off, int len) throws IOException
		{
			if (!isOpen())
				throw new IOException("append(" + ((len > 0) ? new String(cbuf, off, len) : "") + ")[" + off + "/" + len + "] not open");

			if ((len <= 0) || ('\n' == _eolChar))
				return;
			
			final int	remLen=updateCurrentOffset(len);
			if (remLen <= 0)
				return;

			for (int	endPos=off + len, scanPos=endPos - remLen; scanPos < endPos; scanPos++, _curOffset++)
			{
				if ('\n' == (_eolChar=cbuf[scanPos]))
				{
					_eolOffset = _curOffset;
					break;
				}
			}
		}
		/*
		 * @see java.io.Writer#write(int)
		 */
		@Override
		public void write (int c) throws IOException
		{
			if (!isOpen())
				throw new IOException("write(" + String.valueOf((char) c) + ") not open");
			if ('\n' == _eolChar)
				return;

			if (_curOffset >= _startOffset)
			{
				if ((_eolChar=(char) c) == '\n')
					_eolOffset = _curOffset;
			}

			_curOffset++;
		}
	}
	// returns cut text
	public String resizeText () throws BadLocationException
	{
		final int	maxSize=getMaxTextSize();
		if (maxSize <= 0)
			return null;

		final Document	d=getDocument();
		final int		dLen=(null == d) ? 0 : d.getLength();
		if (dLen <= maxSize)
			return null;

		int				cutOffset=dLen - maxSize;
		final EOLStyle	eol=getEolStyle();
		if (eol != null)
		{
			final EOLFinder	f=new EOLFinder(cutOffset);
			try
			{
				write(f);
			}
			catch(IOException e)
			{
				throw new BadLocationException(e.getClass().getName() + " while find EOL: " + e.getMessage(), cutOffset);
			}

			cutOffset = f.getEOLOffset();
		}

		final String	cutText=d.getText(0, cutOffset);
		d.remove(0, cutOffset);

		return cutText;
	}
	/*
	 * @see net.community.chest.swing.component.text.BaseTextPane#append(java.lang.CharSequence)
	 */
	@Override
	@CoVariantReturn
	public LoggingTextPane append (CharSequence csq) throws IOException
	{
		final Object	o=super.append(csq);
		if (o != this)
			throw new StreamCorruptedException("append(" + csq + ") mismatched instances");

		try
		{
			resizeText();
		}
		catch(BadLocationException e)
		{
			throw new StreamCorruptedException("append(" + csq + ") " + e.getClass().getName() + " while resize text: " + e.getMessage());
		}

		return this;
	}
	/**
	 * @param l {@link LogLevelWrapper} for which we want to know if this logger is
	 * enabled to log messages. This should be done whenever building the
	 * string message might take some time (which is almost always). Usually,
	 * projects define a "threshold" level below which any logging should
	 * ask if level is enabled (e.g., setting the level at INFO, means that
	 * anyone wishing to log at fine/finer/finest should call this method
	 * first before issuing the log message) 
	 * @return TRUE (default) if requested level is enabled for logging
	 */
	public boolean isEnabledFor (LogLevelWrapper l)
	{
		return (l != null);
	}
	/**
	 * @param l {@link LogLevelWrapper} at which to log the message
	 * @param msg string message to be logged
	 * @param msgAttrs The {@link AttributeSet} to use for display
	 * @param iconAttrs The {@link AttributeSet} use to show the icon
	 * (assumed to be inside the set) <U>before</U> the text - ignored
	 * if <code>null</code>
	 * @return original message
	 * @throws BadLocationException if problems inserting the new message
	 * @see #getLogLevelAttributes(LogLevelWrapper)
	 */
	public String log (LogLevelWrapper l, String msg, AttributeSet	msgAttrs, AttributeSet iconAttrs)
	 	throws BadLocationException
	{
		if ((null == msg) || (msg.length() <= 0) || (!isEnabledFor(l)))
			return msg;

		final Document		d=getDocument();
		if (iconAttrs != null)
		{
			final int	iconPos=d.getLength();
			d.insertString(iconPos, " ", iconAttrs);
		}

		final int	msgPos=d.getLength();
		d.insertString(msgPos, msg, msgAttrs);
		
		final EOLStyle	eol=getEolStyle();
		if (eol != null)
		{
			final int	eolPos=d.getLength();
			d.insertString(eolPos, eol.getStyleString(), null);
		}

		resizeText();
		return msg;
	}
	/**
	 * @param l {@link LogLevelWrapper} at which to log the message
	 * @param msg string message to be logged
	 * @param attrs The {@link AttributeSet} to use for display
	 * @param i The {@link Icon} to show <U>before</U> the text - ignored
	 * if <code>null</code>
	 * @return original message
	 * @throws BadLocationException if problems inserting the new message
	 * @see #getLogLevelAttributes(LogLevelWrapper)
	 */
	public String log (LogLevelWrapper l, String msg, AttributeSet	attrs, Icon i)
	 	throws BadLocationException
	{
		final MutableAttributeSet	iconAttrs=(null == i) ? null : new SimpleAttributeSet();
		if (iconAttrs != null)
			StyleConstants.setIcon(iconAttrs, i);
		return log(l, msg, attrs, iconAttrs);
	}
	/**
	 * @param l {@link LogLevelWrapper} at which to log the message
	 * @param msg string message to be logged
	 * @param i The {@link Icon} to show <U>before</U> the text - ignored
	 * if <code>null</code>
	 * @return original message
	 * @throws BadLocationException if problems inserting the new message
	 * @see #getLogLevelAttributes(LogLevelWrapper)
	 */
	public String log (LogLevelWrapper l, String msg, Icon i)
		throws BadLocationException
	{
		return log(l, msg, getLogLevelAttributes(l), i);
	}
	/**
	 * @param l {@link LogLevelWrapper} at which to log the message
	 * @param msg string message to be logged
	 * @param attrs The {@link AttributeSet} to use for display
	 * @return original message
	 * @throws BadLocationException if problems inserting the new message
	 * @see #getLogLevelAttributes(LogLevelWrapper)
	 */
	public String log (LogLevelWrapper l, String msg, AttributeSet	attrs)
	 	throws BadLocationException
	{
		if ((null == msg) || (msg.length() <= 0) || (!isEnabledFor(l)))
			return msg;

		final Document		d=getDocument();
		final int			msgPos=d.getLength();
		d.insertString(msgPos, msg, attrs);
		
		final EOLStyle	eol=getEolStyle();
		if (eol != null)
		{
			final int	eolPos=d.getLength();
			d.insertString(eolPos, eol.getStyleString(), null);
		}

		resizeText();
		return msg;
	}
	/**
	 * @param l {@link LogLevelWrapper} at which to log the message
	 * @param msg string message to be logged
	 * @return original message
	 * @throws BadLocationException if problems inserting the new message
	 * @see #log(LogLevelWrapper, String, AttributeSet)
	 */
	public String log (LogLevelWrapper l, String msg) throws BadLocationException
	{
		return log(l, msg, getLogLevelAttributes(l));
	}

	public static final Map<LogLevelWrapper,AttributeSet> loadLogMessagesAttributes (
			final Collection<? extends Map.Entry<String,? extends Element>>	el)
		throws Exception
	{
		if ((null == el) || (el.size() <= 0))
			return null;
		
		Map<LogLevelWrapper,AttributeSet>	ret=null;
		for (final Map.Entry<String,? extends Element>	ee : el)
		{
			final String			ln=(null == ee) ? null : ee.getKey();
			final Element			elem=(null == ee) ? null : ee.getValue();
			final LogLevelWrapper	ll=LogLevelWrapper.fromString(ln);
			if (null == ll)
				throw new NoSuchElementException("loadLogMessagesAttributes(" + DOMUtils.toString(elem) + ") unknown level: " + ln);
			if (null == elem)
				throw new NoSuchElementException("loadLogMessagesAttributes(" + ln + ") no configuration");

			final SimpleAttributeSet	sas;
			if (null == (sas=MutableAttributeSetXmlProxy.SIMPLESET.fromXml(elem)))
				throw new IllegalStateException("No instance created");

			if (null == ret)
				ret = new EnumMap<LogLevelWrapper,AttributeSet>(LogLevelWrapper.class);

			final AttributeSet	prev=ret.put(ll, sas);
			if (prev != null)
				throw new IllegalStateException("loadLogMessagesAttributes(" + DOMUtils.toString(elem) + ") multiple settings for level=" + ln);
		}

		return ret;
	}

	public static final Map<LogLevelWrapper,AttributeSet> loadLogMessagesAttributes (
			final Element root, final String eName, final String aName)
		throws Exception
	{
		final Map<String,? extends Element>								eMap=
			DOMUtils.getSubsections(root, eName, aName);
		final Collection<? extends Map.Entry<String,? extends Element>>	el=
			((null == eMap) || (eMap.size() <= 0)) ? null : eMap.entrySet();
		return loadLogMessagesAttributes(el);
	}

	public static final String	DEFAULT_LEVEL_ELEM_NAME="level",
								DEFAULT_LEVEL_ATTR_NAME="class";
	public static final Map<LogLevelWrapper,AttributeSet> loadLogMessagesAttributes (final Element root) throws Exception
	{
		return loadLogMessagesAttributes(root, DEFAULT_LEVEL_ELEM_NAME, DEFAULT_LEVEL_ATTR_NAME);
	}
	
	public Map<LogLevelWrapper,AttributeSet> setLogLevelAttributesMap (Element root) throws Exception
	{
		final Map<LogLevelWrapper,AttributeSet>	m=loadLogMessagesAttributes(root);
		setLogLevelAttributesMap(m);
		return m;
	}
}
