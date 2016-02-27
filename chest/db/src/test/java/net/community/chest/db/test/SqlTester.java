package net.community.chest.db.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.net.ConnectException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import net.community.chest.db.DriverUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2008 10:43:21 AM
 */
public class SqlTester extends DBTestBase {
	private SqlTester ()
	{
		super();
	}

	//////////////////////////////////////////////////////////////////////////

	// args[0]=driver class, args[1]=url, args[2]=username, args[3]=password, args[4...] SQL statements
	protected static final int testSQLQueries (final PrintStream out, final BufferedReader in, final String ... args)
	{
		Connection	conn=null;
		try
		{
			if (null == (conn=getConnection(out, in, args)))
				throw new ConnectException("No SQL connection generated");

			return testSQLQueries(out, in, conn, cleanConnectionArguments(args));
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return (-1);
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch(Exception ce)
				{
					System.err.println(ce.getClass().getName() + " on close: " + ce.getMessage());
				}
				finally
				{
					conn = null;
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	// args[0]=DSN na,e, args[1...] SQL statements
	protected static final int testDSNQueries (final PrintStream out, final BufferedReader in, final String ... args)
	{
		Connection	conn=null;
		try
		{
			if (null == (conn=getDSNConnection(out, in, args)))
				throw new ConnectException("No SQL connection generated");

			return testSQLQueries(out, in, conn, cleanDSNConnectionArguments(args));
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return (-1);
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch(Exception ce)
				{
					System.err.println(ce.getClass().getName() + " on close: " + ce.getMessage());
				}
				finally
				{
					conn = null;
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testShowClasspathDrivers (final PrintStream out, final BufferedReader in, final String /* ignored */ ... args)
	{
		for ( ; ; )
		{
			try
			{
				final Map<? extends File,? extends Collection<Class<?>>>								dm=
					DriverUtils.getMatchingClasspathDrivers(null);
				final Collection<? extends Map.Entry<? extends File,? extends Collection<Class<?>>>>	dl=
					((null == dm) || (dm.size() <= 0)) ? null : dm.entrySet();
				if ((dl != null) && (dl.size() > 0))
				{
					for (final Map.Entry<? extends File,? extends Collection<Class<?>>> de : dl)
					{
						final File					f=(null == de) ? null : de.getKey();
						final Collection<Class<?>>	cl=(null == de) ? null : de.getValue();
						if ((null == f) || (null == cl) || (cl.size() <= 0))
							continue;

						out.println("\t" + f);
						for (final Class<?> cc : cl)
						{
							if (null == cc)
								continue;
							out.println("\t\t" + cc.getName());
						}
					}
				}
	
				final String	ans=getval(out, in, "again [y]/n");
				if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
					continue;

				return 0;
			}
			catch(Exception ce)
			{
				System.err.println(ce.getClass().getName() + " : " + ce.getMessage());
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testSQLQueries(System.out, in, args);
//		final int				nErr=testDSNQueries(System.out, in, args);
		final int				nErr=testShowClasspathDrivers(System.out, in, args);
		if (nErr != 0)
			System.err.println("Failed: err=" + nErr);
		else
			System.out.println("Finished");
	}
}
