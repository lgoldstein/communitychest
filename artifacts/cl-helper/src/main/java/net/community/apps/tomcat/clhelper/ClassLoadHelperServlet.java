/*
 * 
 */
package net.community.apps.tomcat.clhelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 13, 2011 7:54:30 AM
 */
public class ClassLoadHelperServlet extends HttpServlet {
	private static final long serialVersionUID = 7688564682224295111L;

	public ClassLoadHelperServlet ()
	{
		super();
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
	/*
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		if (!processRequest(req, resp))
			super.doPost(req, resp);
	}

	protected boolean processRequest (HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		if ((null == req) || (null == resp))
			throw new ServletException("No request/response");

		loadClass(req.getParameter("class"), req.getParameter("mode"), resp);
		return true;
	}

	private static final void loadClass (String className, String loadMode, HttpServletResponse resp)
			throws IOException
	{
		final ClassLoader	clThread=resolveClassLoader(loadMode);
		final Class<?>		ctc=(clThread == null) ? null : clThread.getClass();
		final String		ctcName=(ctc == null) ? null : ctc.getName();
		final PrintWriter	w=resp.getWriter(); 
		try
		{
			resp.setContentType("text/plain");
			// must be 1st, otherwise anything written prior is discarded 
			resp.setStatus(HttpServletResponse.SC_OK);

			w.println("Loading " + className + " via " + ctcName + ": " + clThread);
			final Class<?>	clazz=(clThread == null) ? null : clThread.loadClass(className);
			{
				final URL	loc=getSourceCodeLocation(clazz);
				w.println("Loaded from location=" + ((loc == null) ? "UNKNOWN" : loc.toExternalForm()));
			}

			showClassLoaderHierarchy("+++++++++++ Loaded class loaders hierarchy +++++++++", clazz.getClassLoader(), w);
			showClassLoaderHierarchy("+++++++++++ Context thread loaders hierarchy +++++++++", clThread, w);
		}
		catch(Throwable t)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			t.fillInStackTrace();

			final StackTraceElement[]	ste=t.getStackTrace();
			w.println(t.getClass().getName() + ": " + t.getMessage());
			w.println("!!!!!!!!!!!!!!!! Exceptions stack trace !!!!!!!!!!!!!!!");
			for (final StackTraceElement e : ste)
				w.println(e);
			w.flush();
		}
		finally
		{
			w.close();
		}
	}

	private static final ClassLoader resolveClassLoader (String loadMode)
	{
		final Thread		curThread=Thread.currentThread();
		final ClassLoader	clThread=curThread.getContextClassLoader();
		if ("url".equalsIgnoreCase(loadMode))
			return new WrapperURLClassLoader(clThread);

		return clThread;
	}

	private static final void showClassLoaderHierarchy (String title, ClassLoader root, PrintWriter w)
	{
		w.println(title);

		final StringBuilder	sb=new StringBuilder().append('\t');
		for (ClassLoader	cl=root; cl != null; cl = cl.getParent())
		{
			final int		sbLen=sb.length();
			final URL		loc=getSourceCodeLocation(cl);
			final Class<?>	clc=(cl == null) ? null : cl.getClass();
			final String	clcName=(clc == null) ? null : clc.getName();
			sb.append('\t')
			  .append(clcName)
			  .append(": ")
			  .append(String.valueOf(cl))
			  .append('[').append((loc == null) ? "UNKNOWN" : loc.toExternalForm()).append(']')
			  ;
			w.println(sb.toString());
			sb.setLength(sbLen + 1);	// skip the extra TAB
		}

		w.flush();
	}

	private static final URL getSourceCodeLocation (final Object obj)
	{
		return (obj == null) ? null : getSourceCodeLocation(obj.getClass());
	}
	
	private static final URL getSourceCodeLocation (final Class<?> clazz)
	{
		return (clazz == null) ? null : getSourceCodeLocation(clazz.getProtectionDomain()); 
	}
	
	private static final URL getSourceCodeLocation (final ProtectionDomain pd)
	{
		return (pd == null) ? null : getSourceCodeLocation(pd.getCodeSource());
	}
	
	private static final URL getSourceCodeLocation (final CodeSource cs)
	{
		return (cs == null) ? null : cs.getLocation();
	}
}
