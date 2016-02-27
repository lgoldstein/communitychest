/*
 * 
 */
package net.community.chest.web.jnlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import javax.jnlp.JNLPRandomAccessFile;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:34:07 PM
 */
public class JNLPRandomAccessFileImpl extends RandomAccessFile implements JNLPRandomAccessFile {
	public JNLPRandomAccessFileImpl (String name, String mode)
			throws FileNotFoundException
	{
		super(name, mode);
	}

	public JNLPRandomAccessFileImpl (File file, String mode)
			throws FileNotFoundException
	{
		super(file, mode);
	}
}
