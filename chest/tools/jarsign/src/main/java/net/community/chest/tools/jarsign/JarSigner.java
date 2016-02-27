/*
 * 
 */
package net.community.chest.tools.jarsign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import net.community.chest.io.IOCopier;
import net.community.chest.io.jar.JarSignerHelper;
import net.community.chest.io.jar.ManifestInfoExtractor;
import net.community.chest.lang.StringUtil;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerInputStream;
import sun.security.x509.AlgorithmId;
import sun.tools.jar.SignatureFile;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 5, 2009 10:48:37 AM
 */
public class JarSigner extends JarSignerHelper {
	public JarSigner (String alias, PrivateKey privateKey, Collection<X509Certificate> certChain)
	{
		super(alias, privateKey, certChain);
	}

	public JarSigner (String alias, PrivateKey privateKey, X509Certificate... certChain)
	{
		super(alias, privateKey, certChain);
	}

	public JarSigner ()
	{
		super();
	}

	// create a signature file object out of the manifest and the
	// message digest
	public SignatureFile createSignatureFile (Manifest manifest) throws IOException
	{
		final byte[]					mBytes=ManifestInfoExtractor.getManifestBytes(manifest);
		final sun.tools.jar.Manifest	m=new sun.tools.jar.Manifest(mBytes);
		return new SignatureFile(m, getAlias());
	}
	// a helper function that can take entries from one jar file and
	// write it to another jar stream
	private static void writeJarEntry (JarEntry je, JarFile jarFile, JarOutputStream jos) throws IOException
	{
		jos.putNextEntry(je);

		InputStream is=jarFile.getInputStream(je);
		try
		{
			final long	cpyLen=IOCopier.copyStreams(is, jos);
			if (cpyLen < 0L)
				throw new StreamCorruptedException("writeJarEntry(" + je + ") error (" + cpyLen + ") while dump contents");
			jos.closeEntry();
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}
	// NOTE: user must close the output stream
	public void signJarFile (final JarFile jarFile, final OutputStream outputStream)
		throws NoSuchAlgorithmException, IOException
	{
		// calculate the necessary files for the signed jAR

		// get the manifest out of the jar and verify that
		// all the entries in the manifest are correct
		final Manifest manifest=ManifestInfoExtractor.getManifestFile(jarFile);
		ManifestInfoExtractor.validateEntries(manifest, jarFile);

		// create the message digest and start updating the
		// the attributes in the manifest to contain the SHA1
		// digests
		final MessageDigest messageDigest=MessageDigest.getInstance("SHA1");
		updateManifestEntries(manifest, jarFile, messageDigest, manifest.getEntries());

		// construct the signature file object and the
		// signature block objects
		final Key			k=getPrivateKey();
		final byte[]		kBytes=k.getEncoded();
		final SignerInfo	si=new SignerInfo(new DerInputStream(kBytes));
		final PKCS7			block=new PKCS7(
				new AlgorithmId[] { new AlgorithmId(AlgorithmId.DSA_oid) },
				new ContentInfo(ContentInfo.DATA_OID, null),
				getCertArray(),
				new SignerInfo[] { si });
		final SignatureFile signatureFile=createSignatureFile(manifest);
		signatureFile.setBlock(block);

		// start writing out the signed JAR file

		// write out the manifest to the output jar stream
		final JarOutputStream jos=new JarOutputStream(outputStream);
		try
		{
			final JarEntry manifestFile=new JarEntry(ManifestInfoExtractor.DEFAULT_MANIFEST_ENTRY_LOCATION);
			jos.putNextEntry(manifestFile);
			manifest.write(jos);
			jos.closeEntry();

			// write out the signature file -- the signatureFile
			// object will name itself appropriately
			final String	signatureFileName=signatureFile.getName();
			final JarEntry	signatureFileEntry=new JarEntry(signatureFileName);
			jos.putNextEntry(signatureFileEntry	);
			signatureFile.stream(jos);
			jos.closeEntry();

			// write out the signature block file -- again, the block
			// will name itself appropriately
			final String	signatureBlockName=signatureFile.getBlockName();
			final JarEntry	signatureBlockEntry=new JarEntry(signatureBlockName);
			jos.putNextEntry(signatureBlockEntry);
			block.encodeSignedData(jos);
			jos.closeEntry();

			// commit the rest of the original entries in the
			// META-INF directory. if any of their names conflict
			// 	with one that we created for the signed JAR file, then
			// 	we simply ignore it
			for (final Enumeration<JarEntry> metaEntries=(null == jarFile) ? null : jarFile.entries();
				 (metaEntries != null) && metaEntries.hasMoreElements();
				 )
			{
				final JarEntry	metaEntry=metaEntries.nextElement();
				final String	metaName=(null == metaEntry) ? null : metaEntry.getName();
				if (StringUtil.startsWith(metaName, ManifestInfoExtractor.DEFAULT_MANIFEST_FOLDER_LOCATION, false, false)
				 && (!(ManifestInfoExtractor.DEFAULT_MANIFEST_ENTRY_LOCATION.equalsIgnoreCase(metaName))
					 || signatureFileName.equalsIgnoreCase(metaName)
					 || signatureBlockName.equalsIgnoreCase(metaName)))
					 writeJarEntry(metaEntry, jarFile, jos);
			}

			// now write out the rest of the files to the stream
			for (final Enumeration<JarEntry> allEntries=(null == jarFile) ? null : jarFile.entries();
			 	 (allEntries != null) && allEntries.hasMoreElements();
			 	)
			{
				final JarEntry	entry=allEntries.nextElement();
				final String	name=(null == entry) ? null : entry.getName();
				if (!StringUtil.startsWith(name, ManifestInfoExtractor.DEFAULT_MANIFEST_FOLDER_LOCATION, false, false))
					writeJarEntry(entry, jarFile, jos);
			}
			
			jos.flush();
			jos.finish();
		}
		finally
		{
			// close the JAR file that we have been using
			if (jarFile != null)
				jarFile.close();
		}
	}
	// invokes the sun.security.tools.JarSigner.main directly
	public static void signDirectJarFile (final String jarFile,
										  final String alias,
										  final String passwd,
										  final String keyFilePath) throws Exception
	{
		final String[]	args={
				"-keystore file://" + keyFilePath,
				"-storepass " + passwd,
				jarFile,
				alias
			};
		// TODO find a way not to exit at end of signing
//		sun.security.tools.JarSigner.main(args);
	}
}
