/*
 * 
 */
package net.community.chest.javaagent.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.javaagent.dumper.filter.ClassFilter;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 25, 2011 1:17:15 PM
 */
public class DumperClassFileTransformer implements ClassFileTransformer, ClassFilter {
	private final Logger	_logger=Logger.getLogger(DumperClassFileTransformer.class.getName());
	/**
	 * Option/property used to override the default output file path
	 */
	public static final String	OUTPUT_ROOTFOLDER_PROP="dumper.output.root.folder";
	public static final String	CONFIG_URL_PROP="configuration";

	private final File	_outputRoot;
	private final Configuration	_config;
	/*
	 * @see net.community.chest.javaagent.dumper.filter.ClassFilter#accept(java.lang.String)
	 */
	@Override
	public boolean accept (String className)
	{
		final ClassFilter	filter=_config.getFilter();
		if (filter == null)
			return true;
		else
			return filter.accept(className);
	}

	DumperClassFileTransformer (final Map<String,String> optsMap)
	{
		_outputRoot = resolveOutputFilePath(optsMap);
		_config = resolveConfiguration(optsMap);
	}
	/*
	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
	 */
	@Override
	public byte[] transform (final ClassLoader		loader,
							 final String 			orgName,
							 final Class<?>			classBeingRedefined,
							 final ProtectionDomain	protectionDomain,
							 final byte[]			classfileBuffer)
		throws IllegalClassFormatException
	{
		final String	className=orgName.replace('/', '.');
		try
		{
			final File	outFile=transform(className, protectionDomain, classfileBuffer, getWorkBuffer());
			if ((outFile != null) && _logger.isLoggable(Level.FINE))
				_logger.fine("transform(" + className + "): " + outFile.getAbsolutePath());
		}
		catch(IOException e)
		{
			_logger.severe("transform(" + className + ") failed (" + e.getClass().getName() + ")"
					+ " to build dump data: " + e.getMessage());
		}

		return classfileBuffer;
	}

	// NOTE: returns null if class filtered out
	File transform (final String 			orgName,
					final ProtectionDomain	protectionDomain,
					final byte[]			classfileBuffer,
					final Appendable		sb) throws IOException
	{
		final String	className=orgName.replace('/', '.');
		if (!accept(className))
		{
			if (_logger.isLoggable(Level.FINER))
				_logger.finer("transform(" + className + ") not accepted by filter");
			return null;
		}

		final CodeSource	cs=(protectionDomain == null) ? null : protectionDomain.getCodeSource();
		final URL			url=(cs == null) ? null : cs.getLocation();
		if (_logger.isLoggable(Level.FINE))
			_logger.fine("transform[](" + className + "): " + ((url == null) ? "<Unknown>" : url.toExternalForm()));

		BcelClassInfoDumper	infoDumper=new BcelClassInfoDumper(sb);
		infoDumper.dump(className, url, classfileBuffer);
		if (_logger.isLoggable(Level.FINER))
			_logger.finer("transform[](" + className + "): " + sb.toString());

		return dumpClassData(_outputRoot, className, sb);
	}

	File transform (final Class<?>			clazz,
					final ProtectionDomain	protectionDomain,
					final Appendable		sb) throws IOException
	{
		return transform(clazz.getName(), protectionDomain, clazz, sb);
	}

	File transform (final Class<?> clazz, final Appendable sb) throws IOException
	{
		return transform(clazz, clazz.getProtectionDomain(), sb);
	}

	File transform (final String 			orgName,
					final ProtectionDomain	protectionDomain,
					final Class<?>			clazz,
					final Appendable		sb) throws IOException
	{
		final String		className=orgName.replace('/', '.');
		if (!accept(className))
		{
			if (_logger.isLoggable(Level.FINER))
				_logger.finer("transform(" + className + ") not accepted by filter");
			return null;
		}

		final CodeSource	cs=(protectionDomain == null) ? null : protectionDomain.getCodeSource();
		final URL			url=(cs == null) ? null : cs.getLocation();
		if (_logger.isLoggable(Level.FINE))
			_logger.fine("transform<>(" + className + "): " + ((url == null) ? "<Unknown>" : url.toExternalForm()));

		ReflectiveClassInfoDumper	infoDumper=new ReflectiveClassInfoDumper(sb);
		infoDumper.dump(className, url, clazz);
		if (_logger.isLoggable(Level.FINER))
			_logger.finer("transform<>(" + className + "): " + sb.toString());

		return dumpClassData(_outputRoot, className, sb);
	}

	private Configuration resolveConfiguration (final Map<String,String> optsMap)
	{
		try
		{
			return resolveConfiguration(getClass(), optsMap);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static Configuration resolveConfiguration (final Class<?> anchor, final Map<String,String> optsMap) throws Exception
	{
		final URL		url=resolveConfigurationLocation(anchor, optsMap);
		final Document	doc=DOMUtils.loadDocument(url);
		return new Configuration(doc);
	}

	public static final String	DEFAULT_CONFIG_RESOURCE="/config.xml";
	static URL resolveConfigurationLocation (final Class<?> anchor, final Map<String,String> optsMap) throws MalformedURLException
	{
		final String	propVal=SystemPropertiesResolver.SYSTEM.format(optsMap.get(CONFIG_URL_PROP));
		if ((propVal == null) || (propVal.length() <= 0))
			return anchor.getResource(DEFAULT_CONFIG_RESOURCE);

		if (propVal.contains(":/"))
			return new URL(propVal);
		else	// if not a URL assume a file
			return FileUtil.toURL(new File(propVal));
	}

	static File dumpClassData (final File root, final String className, final Object sb) throws IOException
	{
		final File	outputFile=new File(root, className.replace('.', File.separatorChar) + ".xml"),
					parentFile=outputFile.getParentFile();
		if (parentFile.exists())
		{
			if (parentFile.isFile())
				throw new IOException("Target folder is an existing file: " + outputFile.getAbsolutePath());
		}
		else if (!parentFile.mkdirs())
			throw new IOException("Cannot create hierarchy of target file: " + outputFile.getAbsolutePath());

		if (outputFile.exists() && outputFile.isDirectory())
			throw new IOException("Target file is an existing directory: " + outputFile.getAbsolutePath());

		final Writer	w=new FileWriter(outputFile);
		try
		{
			w.append(sb.toString());
		}
		finally
		{
			w.close();
		}

		return outputFile;
	}

	static File resolveOutputFilePath (final Map<String,String> optsMap)
	{
		return resolveOutputRootFolder(resolveOutputFileValue(optsMap));
	}
	/**
	 * <P>Attempts to resolve the dump file output path using the following
	 * values (in the specified order):</P></BR>
	 * <UL>
	 * 		<LI>Check if a {@link OUTPUT_ROOTFOLDER_PROP} option has been specified</LI>
	 * 		<LI>Check if a {@link OUTPUT_ROOTFOLDER_PROP} system property has been defined</LI>
	 * 		<LI>Otherwise, use the CWD + class name</LI>
	 * </UL>
	 * @param optsMap The options {@link Map} used to initialize the agent
	 * @return The output file path for dumping the data
	 */
	static String resolveOutputFileValue (final Map<String,String> optsMap)
	{
		String	value=SystemPropertiesResolver.SYSTEM.format(optsMap.get(OUTPUT_ROOTFOLDER_PROP));
		if (value == null)
			value = SystemPropertiesResolver.SYSTEM.format(System.getProperty(OUTPUT_ROOTFOLDER_PROP));
		if (value == null)
			value = SysPropsEnum.USERDIR.getPropertyValue() + File.separator + DumperClassFileTransformer.class.getSimpleName();
		return value;
	}
	/**
	 * @param outputPath The dump root folder under which the dump files are created
	 * @return A {@link File} object for the path after making sure that the
	 * referenced path is not an existing file and creating the folders all
	 * the way up the hierarchy
	 */
	static File resolveOutputRootFolder (final String outputPath)
	{
		final File	outputFile=new File(outputPath);
		if (outputFile.exists())
		{
			if (outputFile.isFile())
				throw new IllegalStateException("Output file path is an existing file: " + outputPath);
		}
		else if (!outputFile.mkdirs())
			throw new IllegalStateException("Failed to create output root folder: " + outputPath);

		if ((!outputFile.canRead()) || (!outputFile.canWrite()))
			throw new IllegalStateException("Cannot access read/write output folder: " + outputPath);

		return outputFile;
	}
	/**
	 * Holds a {@link StringBuilder} per-thread so we can re-use it when
	 * generating the dump output
	 */
	private final ThreadLocal<StringBuilder>	_outputBuffer=new ThreadLocal<StringBuilder>() {
			/*
			 * @see java.lang.ThreadLocal#initialValue()
			 */
			@Override
			protected StringBuilder initialValue ()
			{
				return new StringBuilder(Byte.MAX_VALUE);
			}
		};
	private StringBuilder getWorkBuffer ()
	{
		StringBuilder	sb=_outputBuffer.get();
		if (sb.length() > 0)	// check if re-using an existing buffer
			sb.setLength(0);
		return sb;
	}
}
