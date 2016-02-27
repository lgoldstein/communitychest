package org.springframework.instrument.classloading.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author lgoldstein
 */
public class TransformingJarFile extends JarFile {
	protected final TransformationContext	xformers;
	protected final Set<String>	identityResources=
			Collections.synchronizedSet(new TreeSet<String>());

	public TransformingJarFile (JarFile jarFile, TransformationContext xformContext)
			throws IOException {
		super(new File(jarFile.getName()));
		this.xformers = xformContext;
	}

	@Override
	public synchronized InputStream getInputStream(ZipEntry ze)
			throws IOException {
		String		name=ze.getName();
		InputStream	in=super.getInputStream(ze);
		if (isTransformationAvailable(name)) {
			return getTransformedStream(name, in);
		}

		return in;
	}

	@Override
	public String toString() {
		return getName();
	}

	protected boolean isTransformationAvailable (String name) {
		return isClassEntry(name) && (!xformers.isEmpty()) && (!identityResources.contains(name));
	}

	protected boolean isClassEntry (String name) {
		if ((name != null) && name.endsWith(".class")) {
			return true;
		} else {
			return false;
		}
	}

	protected InputStream getTransformedStream (String name, InputStream in) throws IOException {
		byte[]	data;
		try {
			data = copyToByteArray(in);
		} finally {
			in.close();
		}
		
		String	className=name.replace('/', '.').substring(0, name.length() - ".class".length());
		byte[]	xformed;
		try {
			xformed = xformers.applyTransformers(className, data);
		} catch(IllegalClassFormatException e) {
			throw new StreamCorruptedException("Failed to transform " + className + ": " + e.getMessage());
		}

		if (xformed == data) {
			identityResources.add(name);
		}

		return new ByteArrayInputStream(xformed);
	}

	public static TransformingJarFile[] createReplacementFiles (TransformationContext xformContext, JarFile ... files)
			throws IOException {
		TransformingJarFile[]	result=new TransformingJarFile[files.length];
		for (int	index=0; index < files.length; index++) {
			JarFile	jarFile=files[index];
			if (jarFile instanceof TransformingJarFile) {
				result[index] = (TransformingJarFile) jarFile;
			} else {
				result[index] = new TransformingJarFile(jarFile, xformContext);
			}
		}

		return result;
	}

	/* ----------- copy/paste from FileCopyUtils to avoid ClassCircularityError ---- */
	public static final int	BUFFER_SIZE=4096;
	public static byte[] copyToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		copy(in, out);
		return out.toByteArray();
	}

	public static int copy(InputStream in, OutputStream out) throws IOException {
		try {
			int byteCount = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		}
		finally {
			IOException	ioe=null;
			try {
				in.close();
			}
			catch (IOException ex) {
				ioe = ex;
			}
			try {
				out.close();
			}
			catch (IOException ex) {
				ioe = ex;
			}

			if (ioe != null)
				throw ioe;
		}
	}

}
