/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.community.chest.test.TestBase;
import net.community.chest.web.servlet.framework.http.AbstractHttpServletRequest;
import net.community.chest.web.servlet.framework.http.AbstractHttpServletResponse;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Sep 13, 2010 1:21:25 PM
 *
 */
public final class HttpServletDoOperationAspectTester extends TestBase {

	private HttpServletDoOperationAspectTester ()
	{
		// no instance
	}

	public static class TestHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 9052615765066267975L;

		public TestHttpServlet ()
		{
			super();
		}
	}

	public static class TestHttpServletRequest extends AbstractHttpServletRequest {
		public TestHttpServletRequest () throws URISyntaxException
		{
			setMethod("GET");
			updateContents(new URI("http://localhost/test?method=aspect"));
		}
	}

	public static class TestHttpServletResponse extends AbstractHttpServletResponse {
		public TestHttpServletResponse ()
		{
			super();
		}
	}

	public static final void main (String[] args)
		throws ServletException, IOException, URISyntaxException
	{
		final TestHttpServlet			s=new TestHttpServlet();
		final TestHttpServletRequest	req=new TestHttpServletRequest();
		final TestHttpServletResponse	rsp=new TestHttpServletResponse();
		s.service(req, rsp);
	}
}
