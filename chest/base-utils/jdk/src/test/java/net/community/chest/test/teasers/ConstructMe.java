package net.community.chest.test.teasers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.community.chest.lang.SysPropsEnum;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Shows that serialization bypasses normal construction</P>
 *
 * @author Lyor G.
 * @since Jun 18, 2008 1:41:53 PM
 */
public class ConstructMe extends TestBase implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -2926129265508472273L;
    private static ConstructMe    _instance    /* =null */;
    public ConstructMe ()
    {
        synchronized(ConstructMe.class)
        {
            if (_instance != null)
                throw new IllegalStateException("Too many references created");

            _instance = this;
        }

        System.out.println("I was constructed");
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public ConstructMe /* co-variant return */ clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    private void writeObject (ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        System.out.println("writeObject");
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        System.out.println("readObject");
    }

    public static void main (String[] args)
    {
        final String    tmpDir=SysPropsEnum.JAVAIOTMPDIR.getPropertyValue(),
                        clsFile=tmpDir + File.separator + "ConstructMe.ext";

        ObjectOutputStream    oo=null;
        try
        {
            oo = new ObjectOutputStream(new FileOutputStream(clsFile));
            oo.writeObject(new ConstructMe());
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        finally
        {
            if (oo != null)
            {
                try
                {
                    oo.close();
                }
                catch(IOException e)
                {
                    // ignored
                }
            }
        }

        ObjectInputStream    oi=null;
        try
        {
            oi = new ObjectInputStream(new FileInputStream(clsFile));

            final Object    o=oi.readObject();
            System.out.println("Read instance of " + o.getClass().getName());
            if (o instanceof ConstructMe)
            {
                final ConstructMe    cm=((ConstructMe) o).clone();
                System.out.println("Cloned instance of " + cm.getClass().getName());
            }
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        finally
        {
            if (oi != null)
            {
                try
                {
                    oi.close();
                }
                catch(IOException e)
                {
                    // ignored
                }
            }
        }
    }
}
