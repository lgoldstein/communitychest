/*
 * 
 */
package net.community.apps.tomcat.rabbitmq.peek;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.ApplicationIOUtils;
import net.community.chest.io.ApplicationOptionsParametersHolder;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.ParametersHolder;
import net.community.chest.web.servlet.ServletRequestParameters;
import net.community.chest.web.servlet.ServletUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 23, 2010 11:04:58 AM
 */
public class RabbitMQPeekServlet extends HttpServlet implements ShutdownListener {
	public RabbitMQPeekServlet ()
	{
		super();
	}
	/*
	 * @see com.rabbitmq.client.ShutdownListener#shutdownCompleted(com.rabbitmq.client.ShutdownSignalException)
	 */
	@Override
	public void shutdownCompleted (ShutdownSignalException cause)
	{
		if (cause.isHardError())
			log(cause.getMessage(), cause);
		else
			log(cause.getMessage());
	}

	private Map<String,AttributeAccessor>	_accsMap, _propsMap;
	/*
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init () throws ServletException
	{
		if (_accsMap == null)
			_accsMap = AttributeMethodType.getAllAttributes(ConnectionFactory.class);
		if (_propsMap == null)
			_propsMap = AttributeMethodType.getAllAttributes(BasicProperties.class);
		super.init();
	}

	private ConnectionFactory createConnectionFactory (final ParametersHolder reqParams) throws Exception
	{
		final ConnectionFactory	fac=new ConnectionFactory();
		for (final Map.Entry<String,? extends AttributeAccessor> ae : _accsMap.entrySet())
		{
			final String	aName=(ae == null) ? null : ae.getKey(),
							aValue=reqParams.getParameter(aName);
			if ((aValue == null) || (aValue.length() <= 0))
				continue;

			final AttributeAccessor				aa=ae.getValue();
			final Class<?>						aType=aa.getType();
			final ValueStringInstantiator<?>	vsi=ClassUtil.getJDKStringInstantiator(aType);
			final Object						o=vsi.newInstance(aValue);
			final Method						sMethod=aa.getSetter();
			sMethod.invoke(fac, o);
		}

		return fac;
	}

	protected boolean processRequest (HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
    	if ((null == req) || (null == resp))
    		throw new ServletException("No request/response");

    	final ServletRequestParameters	reqParams=new ServletRequestParameters(req);
    	final String					opReq=reqParams.getParameter("req");
    	if (!"list".equalsIgnoreCase(opReq))
    		return false;

    	try
    	{
    		final Document	doc=createResponse(createConnectionFactory(reqParams), reqParams);
    		ServletUtils.dumpDocument(doc, resp);
        	return true;
    	}
    	catch(Exception t)
    	{
    		Throwable e=t;

    		if (e instanceof InvocationTargetException)	// extract the "real" exception
    			e = ((InvocationTargetException) e).getTargetException();

    		if (e instanceof ServletException)
    			throw (ServletException) e;
    		else if (e instanceof IOException)
    			throw (IOException) e;
    		else
    			throw new ServletException(e.getClass().getName() + ": " + e.getMessage(), e);
    	}
	}

	private Document createResponse (final ConnectionFactory fac, final ParametersHolder reqParams)
		throws IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		final Connection	conn=fac.newConnection();
		try
		{
			return createResponse(conn, reqParams);
		}
		finally
		{
			conn.close();
		}
	}
	
	private Document createResponse (final Connection conn, final ParametersHolder reqParams)
		throws IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		final Channel	chann=conn.createChannel();
		try
		{
			chann.addShutdownListener(this);
			return createResponse(chann, reqParams);
		}
		finally
		{
			chann.close();
		}
	}

	private Document createResponse (final Channel chann, final ParametersHolder reqParams)
		throws IOException, ParserConfigurationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		final String	queueName=reqParams.getParameter("queueName");
		final int		startIndex=reqParams.getIntParameter("startIndex", 0),
						maxMessages=reqParams.getIntParameter("maxMessages", Byte.MAX_VALUE);
		for (int	curIndex=0; chann.isOpen() && (curIndex < startIndex); curIndex++)
		{
			final GetResponse	resp=chann.basicGet(queueName, false);
			if (resp == null)
				break;
		}

		final Document	doc=DOMUtils.createDefaultDocument();
		final Element	root=doc.createElement("messages");
		for (int	msgIndex=0; chann.isOpen() && (msgIndex < maxMessages); msgIndex++)
		{
			final GetResponse	resp=chann.basicGet(queueName, false);
			if (resp == null)
				break;

			final Element	elem=createMessageElement(doc, resp);
			if (elem == null)
				continue;
			root.appendChild(elem);
		}

		doc.appendChild(root);
		return doc;
	}

	private Element createMessageElement (final Document doc, final GetResponse resp)
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		final Element	elem=doc.createElement("message");
		{
			final Envelope	env=resp.getEnvelope();
			elem.setAttribute("exchange", env.getExchange());
			elem.setAttribute("routing-key", env.getRoutingKey());
			elem.setAttribute("delivery-tag", String.valueOf(env.getDeliveryTag()));
			elem.setAttribute("redelivered", String.valueOf(env.isRedeliver()));
		}

		{
			final byte[]	data=resp.getBody();
			final int		dataLength=(data == null) ? 0 : data.length;
			elem.setAttribute("size", String.valueOf(dataLength));
		}

		final Element	propsElem=appendMessageProperties(doc, resp.getProps());
		if (propsElem != null)
			elem.appendChild(propsElem);
		return elem;
	}

	private Element appendMessageProperties (final Document doc, final BasicProperties props)
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (props == null)
			return null;

		Element	elem=null;
		for (final AttributeAccessor aa : _propsMap.values())
		{
			final Method	gMethod=(aa == null) ? null : aa.getGetter();
			final String	gName=(gMethod == null) ? null : gMethod.getName();
			if ((gName == null) || (gName.length() <= 0))
				continue;

			final Object	aValue=gMethod.invoke(props);
			final String	aString=(aValue == null) ? null : aValue.toString();
			if ((aString == null) || (aString.length() <= 0))
				continue;

			final String	aName=aa.getName();
			final Element	aElem=doc.createElement(aName);
			aElem.setTextContent(aString);
			if (elem == null)
				elem = doc.createElement("properties");
			elem.appendChild(aElem);
		}

		return elem;
	}
    /*
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		if (!processRequest(req, resp))
			super.doGet(req, resp);
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void main (String[] args)
	{
		final Map.Entry<Map<String,List<String>>, List<String>>	argVals=
			ApplicationIOUtils.parseCommandLineArguments(Arrays.asList("--"), false, args);
		final Map<String,List<String>>							opts=
			(argVals == null) ? null : argVals.getKey();
		final ApplicationOptionsParametersHolder				optsHolder=
			new ApplicationOptionsParametersHolder(opts);
		final RabbitMQPeekServlet								servlet=
			new RabbitMQPeekServlet();

		try
		{
			servlet.init();
	
			final ConnectionFactory	fac=servlet.createConnectionFactory(optsHolder);
			final Document			doc=servlet.createResponse(fac, optsHolder);
			PrettyPrintTransformer.DEFAULT.transform(doc, System.out);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
