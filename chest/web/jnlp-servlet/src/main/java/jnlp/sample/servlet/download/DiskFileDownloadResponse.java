/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
package jnlp.sample.servlet.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

public class DiskFileDownloadResponse extends FileDownloadResponse {
	private File _file;
	public File getFile ()
	{ 
		return _file;
	}

	public void setFile (File f)
	{
		_file = f;
	}

	public DiskFileDownloadResponse (File file, String mimeType, String versionId, long lastModified)
	{
		super(mimeType, versionId, lastModified, (null == file) ? null : file.getName());
		_file = file;
	}

	public DiskFileDownloadResponse ()
	{
		this(null, null, null, 0L);
	}
	/*
	 * @see jnlp.sample.servlet.DownloadResponse.FileDownloadResponse#getContentLength()
	 */
	@Override
	public int getContentLength () throws IOException
	{ 
		final File	f=getFile();
		if ((null == f) || (!f.exists()) || (!f.isFile()))
			throw new FileNotFoundException("getContentLength(" + f + ") bad/non-existent file");

		final long	l=f.length();
		if (l > Integer.MAX_VALUE)
			throw new StreamCorruptedException("getContentLength(" + f + ") file too long: " + l);
		return (int) l;
	}
	/*
	 * @see jnlp.sample.servlet.DownloadResponse.FileDownloadResponse#getContent()
	 */
	@Override
	public InputStream getContent () throws IOException
	{ 
		final File	f=getFile();
		if ((null == f) || (!f.exists()) || (!f.isFile()))
			throw new FileNotFoundException("getContent(" + f + ") bad/non-existent file");

		return new BufferedInputStream(new FileInputStream(f));	   
	}
	/*
	 * @see jnlp.sample.servlet.DownloadResponse#toString()
	 */
	@Override
	public String toString () { return super.toString() + "[ " + getArgString() + "]"; }
	/*
	 * @see jnlp.sample.servlet.download.FileDownloadResponse#clone()
	 */
	@Override
	public DiskFileDownloadResponse /* co-variant return */ clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}