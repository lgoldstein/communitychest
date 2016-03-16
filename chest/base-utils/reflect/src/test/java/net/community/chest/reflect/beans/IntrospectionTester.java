/*
 *
 */
package net.community.chest.reflect.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 14, 2010 9:47:55 AM
 */
public class IntrospectionTester extends TestBase {

    //////////////////////////////////////////////////////////////////////////

    public static void testBeansIntrospector (
            final BufferedReader in, final PrintStream out, final Class<?> c)
        throws IntrospectionException
    {
        out.println(c.getName());
        for ( ; ; )
        {
            final BeanInfo                info=Introspector.getBeanInfo(c);
            final PropertyDescriptor[]    props=info.getPropertyDescriptors();
            if ((props != null) && (props.length > 0))
            {
                out.println("Properties:");
                for (final PropertyDescriptor pd : props)
                {
                    out.append('\t').append(pd.getName()).println();
                    out.append("\t\tDisplay name=").println(pd.getDisplayName());
                    out.append("\t\tShort description=").println(pd.getShortDescription());
                    out.append("\t\tRead method=").println(pd.getReadMethod());
                    out.append("\t\tWrite method=").println(pd.getWriteMethod());
                    out.append("\t\tProperty type=").println(pd.getPropertyType());
                }
            }

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
    }
    /*----------------------------------------------------------------------*/

    // each argument is assumed to be the FQ name of a class
    public static void testBeansIntrospector (
            final BufferedReader in, final PrintStream out, final String ... args)
    {
        final int    numArgs=(args == null) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    cName=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "class name (or Quit)");
            if ((cName == null) || (cName.length() <= 0))
                continue;
            if (isQuit(cName))
                break;

            try
            {
                testBeansIntrospector(in, out, ClassUtil.loadClassByName(cName));
            }
            catch(Throwable t)
            {
                System.err.println(t.getClass().getName() + " while processing class=" + cName + ": " + t.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        testBeansIntrospector(in, System.out, args);
    }

}
