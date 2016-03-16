package net.community.chest.db.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.community.chest.db.DriverUtils;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2008 12:23:58 PM
 */
public class DBTestBase extends TestBase {
    protected DBTestBase ()
    {
        super();
    }

    /*----------------------------------------------------------------------*/

    public static final Driver registerDriver (final PrintStream out, final Class<?> driverClass) throws Exception
    {
        for (final Enumeration<? extends Driver>    dvl=DriverManager.getDrivers();
         (dvl != null) && dvl.hasMoreElements();
         )
        {
            final Driver    d=dvl.nextElement();
            final Class<?>    c=(null == d) ? null : d.getClass();
            if ((c != null) && driverClass.isAssignableFrom(c))
            {
                out.println("Found pre-registered driver=" + d);
                return d;
            }

            out.println("Skipping " + d.getMajorVersion() + "." + d.getMinorVersion() + "[JBDC=" + d.jdbcCompliant() + "]: " + d);
        }

        final Driver    d=Driver.class.cast(driverClass.newInstance());
        DriverManager.registerDriver(d);
        out.println("Registered " + driverClass.getName() + ": " + d);
        return d;
    }

    public static final Driver registerDriver (final PrintStream out, final String driverClass) throws Exception
    {
        return registerDriver(out, ClassUtil.loadClassByName(driverClass));
    }

    /*----------------------------------------------------------------------*/

    public static final Connection getConnection (final PrintStream out, final String driverClass, String url, String user, String password) throws Exception
    {
        final Driver    d=registerDriver(out, driverClass);
        if (null == d)
            throw new NoSuchElementException("No driver class=" + driverClass + " found");

        return DriverManager.getConnection(url, user, password);
    }

    /*----------------------------------------------------------------------*/

    private static final String[]    CONN_PROMPTS={
        "Driver class",
        "URL",
        "User",
        "Password"
    };

    public static final int    NUM_CONN_ARGS=CONN_PROMPTS.length;
    public static final Connection getConnection (PrintStream out, BufferedReader in, String ... args) throws Exception
    {
        final String[]    vals=resolveTestParameters(out, in, args, CONN_PROMPTS);
        return getConnection(out, vals[0], vals[1], vals[2], vals[3]);
    }

    public static final Connection getDSNConnection (PrintStream out, BufferedReader in, String ... args) throws Exception
    {
        final String[]    vals=resolveTestParameters(out, in, args, "DSN Name");
        return DriverUtils.getBuiltInODBCConnection(vals[0], null);
    }

    /*----------------------------------------------------------------------*/

    public static final String[] cleanConnectionArguments (final String... args)
    {
        return cleanArgumentsList(NUM_CONN_ARGS, args);
    }

    public static final String[] cleanDSNConnectionArguments (final String ... args)
    {
        return cleanArgumentsList(1, args);
    }

    /*----------------------------------------------------------------------*/

    public static final SQLResultPrinter testSQLQueries (
            final PrintStream out, final BufferedReader in,
            final Connection conn, final String sqlQuery,
            final SQLResultPrinter org)
    {
        SQLResultPrinter    srp=org;
        for ( ; ; )
        {
            Statement    s=null;
            try
            {
                if (null == (s=conn.createStatement()))
                    throw new IllegalStateException("No statement created");

                final int        spPos=sqlQuery.indexOf(' ');
                final String    verb=(spPos < 0) ? sqlQuery : sqlQuery.substring(0, spPos);
                if ("SELECT".equalsIgnoreCase(verb))
                {
                    final long        qStart=System.currentTimeMillis();
                    final ResultSet    rs=s.executeQuery(sqlQuery);
                    final long        qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                    out.println("Fetched items in " + qDuration + " msec. for " + sqlQuery + ":");

                    if (null == srp)
                        srp = new SQLResultPrinter(out);
                    srp.processResultSetRows(rs);
                }
                else
                {
                    final long    qStart=System.currentTimeMillis();
                    final int    rowCount=s.executeUpdate(sqlQuery);
                    final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                    out.println("\t" + rowCount + " rows affected in " + qDuration + " msec.");
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            finally
            {
                if (s != null)
                {
                    try
                    {
                        s.close();
                    }
                    catch(Exception ce)
                    {
                        System.err.println(ce.getClass().getName() + " on close: " + ce.getMessage());
                    }
                }
            }

            final String    ans=getval(out, in, "again [y]/n/q");
            if ((ans != null) && (ans.length() > 0) && ('y' != Character.toLowerCase(ans.charAt(0))))
                break;
        }

        return srp;
    }

    /* -------------------------------------------------------------------- */

    public static final int testSQLQueries (
            final PrintStream out, final BufferedReader in, final Connection conn, final String ... args)
    {
        final int            numArgs=(null == args) ? 0 : args.length;
        SQLResultPrinter    srp=null;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    sqlQuery=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "SQL query (or QUIT)");
            if ((null == sqlQuery) || (sqlQuery.length() <= 0))
                continue;
            if (isQuit(sqlQuery)) break;

            srp = testSQLQueries(out, in, conn, sqlQuery, srp);
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/
}
