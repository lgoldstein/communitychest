package net.community.apps.tools.srvident;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.FileUtil;
import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.dns.SMTPMxRecord;
import net.community.chest.net.proto.text.NetServerIdentityAnalyzer;
import net.community.chest.net.proto.text.TextProtocolNetServerIdentity;
import net.community.chest.net.proto.text.imap4.IMAP4Protocol;
import net.community.chest.net.proto.text.imap4.IMAP4ServerIdentityAnalyzer;
import net.community.chest.net.proto.text.pop3.POP3Protocol;
import net.community.chest.net.proto.text.pop3.POP3ServerIdentityAnalyzer;
import net.community.chest.net.proto.text.smtp.SMTPProtocol;
import net.community.chest.net.proto.text.smtp.SMTPServerIdentityAnalyzer;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:57:06 AM
 */
public class IdTableModel extends EnumColumnAbstractTableModel<IdTableColumns,TextProtocolNetServerIdentity> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6370302724356407140L;

	public IdTableModel ()
	{
		super(IdTableColumns.class, TextProtocolNetServerIdentity.class);
		setColumnsValues(IdTableColumns.VALUES);
	}

	public IdTableModel (Element elem) throws Exception
	{
		this();

		if (fromXml(elem) != this)
			throw new IllegalStateException("Mismatched XML instances");
	}
	/*
	 * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
	 */
	@Override
	@CoVariantReturn
	public String getColumnValue (int rowIndex, TextProtocolNetServerIdentity row, IdTableColumns colIndex)
	{
		if (null == colIndex)
			throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");
		if (null == row)
			throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

		switch(colIndex)
		{
			case NAME		:
				return row.getHostName();
			case PROTO		:
				return row.getProtocol();
			case TYPE		:
				return row.getType();
			case VERSION	:
				return row.getVersion();
			case WELCOME	:
				return row.getWelcomeLineText();

			default			: 
				throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ") unknown column requested");
		}
	}
	/* NOTE: unexpected but handled
	 * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
	 */
	@Override
	public void setValueAt (int rowIndex, TextProtocolNetServerIdentity row, int colNum, IdTableColumns colIndex, Object value)
	{
		throw new UnsupportedOperationException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")::=" + value + " - N/A");
	}

	public TextProtocolNetServerIdentity addURLEntry (final URI u)
	{
		if (null == u)
			return null;

		final TextProtocolNetServerIdentity	tsi=new TextProtocolNetServerIdentity();
		tsi.setProtocol(u.getScheme());
		tsi.setHostName(u.getHost());
		tsi.setPort(u.getPort());

		add(tsi);
		return tsi;
	}

	public List<TextProtocolNetServerIdentity> resolveMXRecords (final String domain) throws IOException, NamingException, URISyntaxException
	{
		final DNSAccess								dns=new DNSAccess();
		final Collection<? extends SMTPMxRecord>	recs=SMTPMxRecord.mxLookup(dns, domain);
		if ((null == recs) || (recs.size() <= 0))
			throw new FileNotFoundException("No MX records found");

		List<TextProtocolNetServerIdentity>	vals=null;
		for (final SMTPMxRecord r : recs)
		{
			final TextProtocolNetServerIdentity	tsi=
				(null == r) /* should not happen */ ? null : addURLEntry(new URI("smtp://" + r.getHost()));
			if (null == tsi)	// should not happen
				continue;

			if (null == vals)
				vals = new ArrayList<TextProtocolNetServerIdentity>(recs.size());
			vals.add(tsi);
		}

		return vals;
	}

	private static final NetServerIdentityAnalyzer getAnalyzer (final String p)
	{
		if ("SMTP".equalsIgnoreCase(p))
			return SMTPServerIdentityAnalyzer.DEFAULT;
		else if ("IMAP4".equalsIgnoreCase(p))
			return IMAP4ServerIdentityAnalyzer.DEFAULT;
		else if ("POP3".equalsIgnoreCase(p))
			return POP3ServerIdentityAnalyzer.DEFAULT;
		else
			return null;
	}

	private static final int resolvePort (final String p, final int defPort)
	{
		if (defPort > 0)
			return defPort;

		if ("SMTP".equalsIgnoreCase(p))
			return SMTPProtocol.IPPORT_SMTP;
		else if ("IMAP4".equalsIgnoreCase(p))
			return IMAP4Protocol.IPPORT_IMAP4;
		else if ("POP3".equalsIgnoreCase(p))
			return POP3Protocol.IPPORT_POP3;
		else
			return Integer.MIN_VALUE;
	}

	private static final TextProtocolNetServerIdentity updateVersionData (
			final MainFrame 					parent,
			final TextProtocolNetServerIdentity tsi) throws IOException
	{
		if (null == tsi)
			return null;

		final String					proto=tsi.getProtocol();
		final NetServerIdentityAnalyzer	analyzer=getAnalyzer(proto);
		if (null == analyzer)
			throw new FileNotFoundException("No " + NetServerIdentityAnalyzer.class.getSimpleName() + " for protocol=" + proto);

		final int	port=resolvePort(proto, tsi.getPort());
		if (port <= 0)
			throw new FileNotFoundException("No port available for protocol=" + proto);

		TextNetConnection	conn=null;
		final String		wl;
		try
		{
			conn = new BufferedTextSocket();
			conn.setReadTimeout(30 * 1000);

			{
				parent.updateStatusBar("Connecting to " + tsi.getHostName() + " on port " + port);
				final long	cStart=System.currentTimeMillis();
				conn.connect(tsi.getHostName(), port);
				final long	cEnd=System.currentTimeMillis(), cDuration=cEnd - cStart;

				parent.updateStatusBar("Connected to " + tsi.getHostName() + " on port " + port + " after " + cDuration + " msec.");
			}

			{
				final long	rStart=System.currentTimeMillis();
				wl = conn.readLine();
				final long	rEnd=System.currentTimeMillis(), rDuration=rEnd - rStart;

				parent.updateStatusBar("Read welcome of " + tsi.getHostName() + " on port " + port + " after " + rDuration + " msec.");
			}
		}
		finally
		{
			FileUtil.closeAll(conn);
		}

		final Map.Entry<String,String>	id=analyzer.getServerIdentity(wl);
		if (id != null)
		{
			tsi.setType(id.getKey());
			tsi.setVersion(id.getValue());
		}

		return tsi;
	}

	public void refresh (MainFrame parent)
	{
		final int	numItems=size();
		for (int	tIndex=0; tIndex < numItems; tIndex++)
		{
			final TextProtocolNetServerIdentity	tsi=get(tIndex);
			if (null == tsi)	// should not happen
				continue;

			try
			{
				updateVersionData(parent, tsi);
				fireTableRowsUpdated(tIndex, tIndex);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(parent, "refresh(" + tsi.getHostName() + ") " + e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}

		parent.updateStatusBar("Ready");
	}
}
