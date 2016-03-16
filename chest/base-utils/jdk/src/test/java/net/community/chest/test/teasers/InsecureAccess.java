package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.io.File;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * Shows how re-calling a getter can be a security vulnerability
 *
 * @author Lyor G.
 * @since Apr 16, 2008 2:42:51 PM
 */
public class InsecureAccess extends TestBase {
    private static final String getAccessedFile (File f)
    {
        if (null == f)
            return null;

        if ("/etc/passwd".equalsIgnoreCase(f.getPath()))
            return null;

        return f.getPath();
    }

    private static final class MyFile extends File {
        /**
         *
         */
        private static final long serialVersionUID = 3017088457368909516L;
        private boolean    _firstCall;
        public MyFile (String path)
        {
            super(path);

            _firstCall = "/etc/passwd".equalsIgnoreCase(path);
        }
        /*
         * @see java.io.File#getPath()
         */
        @Override
        public String getPath ()
        {
            if (_firstCall)
            {
                _firstCall = false;
                // return an "innocent" file on 1st call
                return "/hello/world";
            }

            return super.getPath();
        }
    }

    public static void main (String[] args)
    {
        final int                numArgs=(null == args) ? 0 : args.length;
        final BufferedReader    in=getStdin();
        for (int    aIndex=0; ; aIndex++)
        {
            final String    path=(aIndex < numArgs) ? args[aIndex] : getval(System.out, in, "path (or Quit)");
            if ((null == path) || (path.length() <= 0))
                continue;
            if (isQuit(path)) break;

            final MyFile    f=new MyFile(path);
            final String    r=getAccessedFile(f);
            System.out.println("\t" + path + " => " + r);
        }
    }
}
