/*
 * 
 */
package net.community.chest.io.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import net.community.chest.io.EOLStyle;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Provides an override-able behavior for some methods that are
 * <code>private</code> in the original {@link PrintStream}</P>
 * @author Lyor G.
 * @since May 21, 2009 1:34:16 PM
 */
public abstract class AbstractPrintStream extends PrintStream {
	protected AbstractPrintStream (File file, String csn)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		super(file, csn);
	}

	protected AbstractPrintStream (File file) throws FileNotFoundException
	{
		super(file);
	}

	protected AbstractPrintStream (OutputStream o, boolean autoFlush, String encoding) throws UnsupportedEncodingException
	{
		super(o, autoFlush, encoding);
	}

	protected AbstractPrintStream (OutputStream o, boolean autoFlush)
	{
		super(o, autoFlush);
	}

	protected AbstractPrintStream (OutputStream o)
	{
		super(o);
	}

	protected AbstractPrintStream (String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		super(fileName, csn);
	}

	protected AbstractPrintStream (String fileName) throws FileNotFoundException
	{
		super(fileName);
	}
	
	public void print (char[] cbuf, int off, int len)
	{
		if (len > 0)
			append(new String(cbuf, off, len));
	}
	/*
	 * @see java.io.PrintStream#print(char[])
	 */
	@Override
	public void print (char s[])
	{
		print(s, 0, s.length);
	}
	/*
	 * @see java.io.PrintStream#write(int)
	 */
	@Override
	public void write (int b)
	{
		append((char) b);
	}
    /*
     * @see java.io.PrintStream#print(boolean)
     */
    @Override
	public void print (boolean b)
    {
    	append(String.valueOf(b));
    }
    /*
     * @see java.io.PrintStream#print(char)
     */
    @Override
	public void print (char c)
    {
    	append(String.valueOf(c));
    }
    /*
     * @see java.io.PrintStream#print(int)
     */
    @Override
	public void print (int i)
    {
    	append(String.valueOf(i));
    }
    /*
     * @see java.io.PrintStream#print(long)
     */
    @Override
	public void print (long l)
    {
    	append(String.valueOf(l));
    }
    /*
     * @see java.io.PrintStream#print(float)
     */
    @Override
    public void print (float f)
    {
    	append(String.valueOf(f));
    }
    /*
     * @see java.io.PrintStream#print(double)
     */
    @Override
	public void print (double d)
    {
    	append(String.valueOf(d));
    }
    /*
     * @see java.io.PrintStream#print(java.lang.String)
     */
    @Override
	public void print (String s)
    {
    	append((s == null) ? "null" : s);
    }
    /*
     * @see java.io.PrintStream#print(java.lang.Object)
     */
    @Override
	public void print (Object obj)
    {
    	append(String.valueOf(obj));
    }

    protected void newLine ()
    {
    	print(EOLStyle.LOCAL.getStyleString());
    }
    /*
     * @see java.io.PrintStream#println(boolean)
     */
    @Override
	public void println (boolean x)
    {
    	print(x);
    	newLine();
    }
    /*
     * @see java.io.PrintStream#println(char)
     */
    @Override
	public void println (char x)
    {
	    print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(int)
     */
    @Override
	public void println (int x)
    {
    	print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(long)
     */
    @Override
	public void println (long x)
    {
	    print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(float)
     */
    @Override
	public void println (float x)
    {
	    print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(double)
     */
    @Override
	public void println (double x)
    {
	    print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(char[])
     */
    @Override
	public void println (char x[])
    {
	    print(x);
	    newLine();
	}
    /*
     * @see java.io.PrintStream#println(java.lang.String)
     */
    @Override
	public void println (String x)
    {
    	print(x);
	    newLine();
    }
    /*
     * @see java.io.PrintStream#println(java.lang.Object)
     */
    @Override
	public void println (Object x)
    {
        print(String.valueOf(x));
        newLine();
    }
    /*
     * @see java.io.PrintStream#format(java.lang.String, java.lang.Object[])
     */
    @Override
	public PrintStream format (String format, Object ... args)
    {
    	return format(Locale.getDefault(), format, args);
    }
    /*
     * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
	public PrintStream printf (Locale l, String format, Object ... args)
    {
    	return format(l, format, args);
    }
    /*
     * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
     */
    @Override
	public PrintStream printf (String format, Object ... args)
    {
    	return printf(Locale.getDefault(), format, args);
    }
}
