/*
 * 
 */
package net.community.chest.io.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.encode.base64.Base64;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G. - based on Raffi Krikorian's <a href="http://www.onjava.com/pub/a/onjava/2001/04/12/signing_jar.html">article</a>
 * @since May 5, 2009 9:26:00 AM
 */
public class JarSignerHelper {
	private String _alias;
	public String getAlias ()
	{
		return _alias;
	}

	public void setAlias (String alias)
	{
		_alias = alias;
	}

	private PrivateKey _privateKey;
	public PrivateKey getPrivateKey ()
	{
		return _privateKey;
	}

	public void setPrivateKey (PrivateKey privateKey)
	{
		_privateKey = privateKey;
	}

	private Collection<X509Certificate> _certChain;
	public Collection<X509Certificate> getCertChain ()
	{
		return _certChain;
	}

	public X509Certificate[] getCertArray ()
	{
		final Collection<? extends X509Certificate>	cl=getCertChain();
		final int									numCerts=(null == cl) ? 0 : cl.size();
		return (numCerts <= 0) ? null : cl.toArray(new X509Certificate[numCerts]);
	}

	public void setCertChain (Collection<X509Certificate> certChain)
	{
		_certChain = certChain;
	}

	public Collection<X509Certificate> addCertificate (X509Certificate c)
	{
		Collection<X509Certificate>	cl=getCertChain();
		if (null == c)
			return cl;

		if (null == cl)
		{
			setCertChain(new LinkedList<X509Certificate>());
			if (null == (cl=getCertChain()))
				throw new IllegalStateException("No certificates chain available though created");
		}

		cl.add(c);
		return cl;
	}

	public JarSignerHelper (String alias, PrivateKey privateKey, Collection<X509Certificate> certChain )
	{
		_alias = alias;
	    _privateKey = privateKey;
	    _certChain = certChain;
	}

	public JarSignerHelper (String alias, PrivateKey privateKey, X509Certificate ... certChain)
	{
		this(alias, privateKey, ((null == certChain) || (certChain.length <= 0)) ? null : new ArrayList<X509Certificate>(Arrays.asList(certChain)));
	}

	public JarSignerHelper ()
	{
		this(null, null,(Collection<X509Certificate>) null);
	}
	// helper function to update the digest	
	public static final String updateDigest (
			final MessageDigest digest, final InputStream in) throws IOException
	{
		if ((null == digest) || (null == in))
			throw new IOException("Missing digest arguments");

		final byte[] buffer=new byte[IOCopier.DEFAULT_COPY_SIZE];
		for (int rLen=in.read(buffer); rLen > 0; rLen=in.read(buffer))
			digest.update(buffer, 0, rLen);

		final byte[]	dValue=digest.digest();
		return Base64.encodeToString(dValue);
	}
	// update the attributes in the manifest to have the
	// appropriate message digests. we store the new entries into
	// the entries Map and return it (we do not compute the digests
	// for those entries in the META-INF directory)
	/* returns only the CREATED attributes */ 
	public static final Map<String,Attributes> updateManifestEntries (
			final Manifest					manifest,
			final JarFile					jarFile,
			final MessageDigest				messageDigest,
			final Map<String,Attributes>	entries) throws IOException
	{
		Map<String,Attributes>	ret=null;
		for (final Enumeration<JarEntry> jarElements=(null == jarFile) ? null : jarFile.entries();
			 (jarElements != null) && jarElements.hasMoreElements();
			)
		{
			final JarEntry 	jarEntry=jarElements.nextElement();
			final String	jn=(null == jarEntry) ? null : jarEntry.getName();
			if (StringUtil.startsWith(jn, ManifestInfoExtractor.DEFAULT_MANIFEST_FOLDER_LOCATION, false, false))
				continue;

			// update the digest and record the base 64 version of it into the attribute list
			Attributes		aa=(null == manifest) ? null : manifest.getAttributes(jn);
			InputStream		in=null;
			final String	aValue;
			try
			{
				in = jarFile.getInputStream(jarEntry);

				aValue = updateDigest(messageDigest, in);
			}
			finally
			{
				FileUtil.closeAll(in);
			}

			if (aa != null)
			{
				aa.putValue("SHA1-Digest", aValue);
			}
			else if (!jarEntry.isDirectory())
			{
				/* Store away the digest into a new Attribute because we don't
				 * already have an attribute list for this entry. We do not
				 * store attributes for directories within the JAR
				 */
				aa = new Attributes();
				aa.putValue("SHA1-Digest", aValue);

				if (null == ret)
					ret = new TreeMap<String,Attributes>();

				final Attributes	prev=ret.put(jn, aa);
				if (prev != null)
					throw new StreamCorruptedException("Previous override for entry=" + jn);
				entries.put(jn, aa);	// TODO do not update the entries
			}
		}

		return entries;
	}
}
