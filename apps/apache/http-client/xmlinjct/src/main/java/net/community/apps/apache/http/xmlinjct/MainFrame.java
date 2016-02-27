/*
 * 
 */
package net.community.apps.apache.http.xmlinjct;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.apps.apache.http.xmlinjct.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.Triplet;
import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.apache.httpclient.methods.EntityEnclosingGetMethod;
import net.community.chest.apache.log4j.Log4jUtils;
import net.community.chest.apache.log4j.factory.Log4jLoggerWrapperFactory;
import net.community.chest.io.FileUtil;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.proto.text.http.HttpUtils;
import net.community.chest.resources.XmlAnchoredResourceAccessor;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 12:00:05 PM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6076291071240056064L;
	private static LoggerWrapper	_logger	/* =null */;
	/*
	 * @see net.community.apps.common.BaseMainFrame#getLogger()
	 */
	@Override
	protected synchronized LoggerWrapper getLogger ()
	{
		if (null == _logger)
			_logger = WrapperFactoryManager.getLogger(getClass());
		return _logger;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
	 */
	@Override
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	private void showHeaders (final DocumentPanel p) throws Exception
	{
		// TODO Auto-generated method stub
	}

	private ReqDocumentPanel	_reqPanel;
	public String getRequestFilePath ()
	{
		return (null == _reqPanel) ? null : _reqPanel.getFilePath();
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			_reqPanel.setDocument(filePath);
		}
		catch(Exception e)
		{
			getLogger().error("loadFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	private RspDocumentPanel	_rspPanel;
	/*
	 * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
	 */
	@Override
	public void saveFile (final File savePath, final Element dlgElement)
	{
		final String	filePath=(null == savePath) ? null : savePath.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		if (savePath.exists())
		{
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, "Overwrite ?", filePath, JOptionPane.YES_NO_OPTION))
				return;
		}

		try
		{
			Writer	w=null;
			try
			{
				final Document				doc=_rspPanel.getDocument();
				final Source				s=new DOMSource(doc);
				final Collection<Header>	hl=_rspPanel.getHeaders();
				final Result				r;
				if ((hl != null) && (hl.size() > 0))
				{
					w = new BufferedWriter(new HeadersWriter(savePath, hl));
					r = new StreamResult(w);
				}
				else
					r = new StreamResult(savePath);

				final Transformer	t=PrettyPrintTransformer.DEFAULT;
				t.transform(s, r);
			}
			finally
			{
				FileUtil.closeAll(w);
			}

			_rspPanel.setFilePath(filePath);
		}
		catch(Exception e)
		{
			getLogger().error("saveFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	protected void showHeaders (final String pnlType) throws Exception
	{
		if ("http-request-hdrs".equalsIgnoreCase(pnlType))
			showHeaders(_reqPanel);
		else if ("http-response-hdrs".equalsIgnoreCase(pnlType))
			showHeaders(_rspPanel);
		else
			throw new UnsupportedOperationException("showHeaders(" + pnlType + ") N/A");
	}

	private HttpConnectionManager	_mgr	/* =null */;
	private synchronized HttpConnectionManager getConnectionManager ()
	{
		if (null == _mgr)
			_mgr = new SimpleHttpConnectionManager(true);
		return _mgr;
	}

	private JCheckBox _getOrPost	/* =null */;
	public boolean isGetMethodUsed ()
	{
		return (null == _getOrPost) || _getOrPost.isSelected();
	}

	private JCheckBox	_encQuery	/* =null */;
	public boolean isAutoEncodeQuery ()
	{
		return (null == _encQuery) || _encQuery.isSelected();
	}

	protected Triplet<String,Document,Header[]> runQuery (final String qryString) throws Exception
	{
		final URI					u=new URI(qryString);
		final HostConfiguration		host=new HostConfiguration();
		host.setHost(u.getHost(), u.getPort(), u.getScheme());

		final String		reqPath=getRequestFilePath();
		final HttpMethod	m;
		if (isGetMethodUsed())
		{
			if ((reqPath != null) && (reqPath.length() > 0))
				m = new EntityEnclosingGetMethod();
			else
			{
				m = new GetMethod();
				m.setFollowRedirects(true);
			}
		}
		else
			m = new PostMethod();

		{
			final String	p=u.getPath();
			if ((p != null) && (p.length() > 0))
				m.setPath(p);
		}

		{
			final String	uq=u.getQuery(), q;
			if (isAutoEncodeQuery())
				q = HttpUtils.encodeQueryParamaters(uq);
			else
				q = uq;
			if ((q != null) && (q.length() > 0))
				m.setQueryString(q);
		}

		if ((reqPath != null) && (reqPath.length() > 0))
		{
			final File			reqFile=new File(reqPath);
			final RequestEntity	reqData=new FileRequestEntity(reqFile, "text/xml");
			((EntityEnclosingMethod) m).setRequestEntity(reqData);
		}

		final HttpConnectionManager	mgr=getConnectionManager();
		final HttpClient			hc=(null == mgr) ? null : new HttpClient(mgr);
		final long					qStart=System.currentTimeMillis();
		try
		{
			final int		stCode=hc.executeMethod(host, m);
			final long		qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
			final String	stLine=HttpClientUtils.getMethodStatusLine(m);
			// TODO add handle redirection follow-up
			if (!HttpClientUtils.isOKHttpRspCode(stCode))
			{
				final byte[]	rspBody=m.getResponseBody();	// MUST call this to flush the response regardless of status code
				throw new StreamCorruptedException("Bad response code (" + stCode + ")[" + ((null == rspBody) ? 0 : rspBody.length) + " bytes] after " + qDuration + " msec.: " + stLine);
			}
				
			final Document	doc=HttpClientUtils.loadDocument(m);
			return new Triplet<String,Document,Header[]>(stLine, doc, m.getResponseHeaders());
		}
		finally
		{
			m.releaseConnection();
		}
	}

	void setQueryResult (final Triplet<String,? extends Document,Header[]> qRes, final long qDuration)
	{
		final String	stLine=(null == qRes) ? null : qRes.getV1();
		final Document	doc=(null == qRes) ? null : qRes.getV2();
		final Header[]	hdrs=(null == qRes) ? null : qRes.getV3();
		_rspPanel.setDocument(doc);
		_rspPanel.setHeaders(hdrs);
		JOptionPane.showMessageDialog(this, "After " + qDuration + " msec.", stLine, JOptionPane.INFORMATION_MESSAGE);

	}
	private JTextField	_qryField	/* =null */;
	public String getQueryString ()
	{
		return (null == _qryField) ? null : StringUtil.getCleanStringValue(_qryField.getText());
	}

	public void setQueryString (final String s)
	{
		_qryField.setText((null == s) ? "" : s);
	}

	private boolean	_running	/* =false */;
	protected boolean isRunningMode ()
	{
		return _running;
	}

	protected void setRunningMode (boolean running)
	{
		final JComponent[]	comps={
				_reqPanel,
				_rspPanel,
				_qryField,
				_encQuery,
				_getOrPost
			};
		for (final JComponent c : comps)
		{
			if ((c != null) && (c.isEnabled() == running))
				c.setEnabled(!running);
		}

		if (_running != running)
		{
			final Cursor	c=running
					? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
					: Cursor.getDefaultCursor()
					;
			if (c != null)
				setCursor(c);
			_running = running;
		}
	}

	private QueryRunner	_runner;
	Triplet<String,Document,Header[]> runQuery (QueryRunner r)
	{
		if (r != _runner)
			return getLogger().errorObject("runQuery() mismatched runner instances", null);

		final String	qryString=getQueryString();
        getLogger().info("runQuery(" + qryString + ") starting");

        final long		qStart=System.currentTimeMillis();
	    try
	    {
	    	final Triplet<String,Document,Header[]>	qRes=runQuery(qryString);
	    	final long								qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
   			getLogger().info("runQuery(" + qryString + ") ended after " + qDuration + " msec.");
   			return qRes;
   		}
	    catch(Exception e)
	    {
	    	final long	qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
	    	getLogger().error("runQuery(" + qryString + ") " + e.getClass().getName() + " after " + qDuration + " msec.: " + e.getMessage(), e);
	    	BaseOptionPane.showMessageDialog(getMainFrameInstance(), e);
	    	return null;
	    }
	}

	void signalQueryDone (QueryRunner r)
	{
		if (r != null)
		{
			if (_runner != r)
				getLogger().warn("signalQueryDone() mismatched instances");
			_runner = null;
		}

   		setRunningMode(false);
	}
	protected void runQuery ()
	{
		if (_runner != null)
		{
			JOptionPane.showMessageDialog(this, "Wait for current query to end", "Query in progress", JOptionPane.ERROR_MESSAGE);
			return;
		}

		_runner = new QueryRunner(this);
		setRunningMode(true);
		_runner.execute();
	}

	protected void clear ()
	{
		final DocumentPanel[]	pa={ _reqPanel, _rspPanel };
		for (final DocumentPanel p : pa)
		{
			if (null == p)
				continue;
			p.clearContent();
		}

		if (_qryField != null)
			_qryField.setText("");
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
	 */
	@Override
	protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
	{
		final Map<String,? extends ActionListener>	org=super.getActionListenersMap(createIfNotExist);
		if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
			return org;

		final Map<String,ActionListener>	lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());
		lm.put(LOAD_CMD, getLoadFileListener());
		lm.put(SAVE_CMD, getSaveFileListener());
		lm.put("clear", new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (final ActionEvent event)
				{
					clear();
				}
			});
		lm.put("headers", new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (final ActionEvent event)
				{
					try
					{
						showHeaders((null == event) ? null : event.getActionCommand());
					}
					catch(Exception e)
					{
						BaseOptionPane.showMessageDialog(getMainFrameInstance(), e);
					}
				}
			});

		setActionListenersMap(lm);
		return lm;
	}

	private JPanel createRequestURLPanel () throws Exception
	{
		final XmlAnchoredResourceAccessor	acc=getResourcesAnchor();
		final Element						rqpElem=acc.getSection("req-url-panel");
		final LRFieldWithButtonPanel		rqp=new LRFieldWithButtonPanel(rqpElem);
		final JButton						b=rqp.getButton();
		if (null == b)
			throw new NoSuchElementException("No query button field");
		b.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					runQuery();
				}
			});

		if (null == (_qryField=rqp.getTextField()))
			throw new NoSuchElementException("Missing query text field");

		final Element	gopElem=acc.getSection("req-method-choice"),
						encElem=acc.getSection("enc-query-choice");	
		_getOrPost = new BaseCheckBox(gopElem);
		_encQuery = new BaseCheckBox(encElem);

		final JPanel	subPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		subPanel.add(_getOrPost);
		subPanel.add(_encQuery);

		final JPanel	p=new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(rqp);
		p.add(subPanel);
		return p;
	}

	private void processMainArguments (final String ... args) throws Exception
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	op=args[aIndex];
			if ("-u".equalsIgnoreCase(op))
			{
				aIndex++;
				setQueryString(args[aIndex]);
			}
			else if ("-i".equalsIgnoreCase(op))
			{
				aIndex++;
				loadFile(new File(args[aIndex]), LOAD_CMD, null);
			}
			else if ("-o".equalsIgnoreCase(op))
			{
				aIndex++;
				_rspPanel.setFilePath(args[aIndex]);
			}
			else if ("-m".equalsIgnoreCase(op))
			{
				aIndex++;

				final String	mName=args[aIndex];
				if ("get".equalsIgnoreCase(mName))
					_getOrPost.setSelected(true);
				else if ("post".equalsIgnoreCase(mName))
					_getOrPost.setSelected(false);
				else
					throw new UnsupportedOperationException("Unknown HTTP method: " + mName);
			}
			else
				throw new NoSuchElementException("Unknown command line argument: " + op);
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();	
		try
		{
			final JPanel	p=createRequestURLPanel();
			if (p != null)
				ctPane.add(p, BorderLayout.NORTH);
		}
		catch(Exception e)
		{
			getLogger().error(e.getClass().getName() + " while build request panel: " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}

		{
			_reqPanel = new ReqDocumentPanel();
			_rspPanel = new RspDocumentPanel();
			setDropTarget(new DropTarget(_reqPanel, this));

			final JSplitPane	sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _reqPanel, _rspPanel);
			sp.setResizeWeight(0.5);

			ctPane.add(sp, BorderLayout.CENTER);
		}
	}
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(false /* don't layout the frame yet */, args);

		// initialize log4j and set the default logging factory to be the log4j one
		try
		{
			final ResourcesAnchor	a=getResourcesAnchor();
			final Document			log4jDoc=Log4jUtils.log4jInit(a);
			if (null == log4jDoc)
				throw new MissingResourceException("Missing log4j configuration file", Log4jUtils.class.getName(), Log4jUtils.DEFAULT_CONFIG_FILE_NAME);

			final LoggerWrapperFactory	cur=Log4jLoggerWrapperFactory.replaceCurrentFactory();
			if (!(cur instanceof Log4jLoggerWrapperFactory))
				getLogger().info("set " + Log4jLoggerWrapperFactory.class.getSimpleName() + " instance");
		}
		catch(Exception e)
		{
			BaseOptionPane.showMessageDialog(this, e);
			getLogger().error(e.getClass().getName() + " while initialize log4j: " + e.getMessage());
		}

		try
		{
			layoutComponent();
		}
		catch(Exception e)
		{
			getLogger().error(e.getClass().getName() + " while layout component: " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}

		try
		{
			processMainArguments(args);
		}
		catch(Exception e)
		{
			getLogger().error(e.getClass().getName() + " while process arguments: " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}
	}
}
