/*
 * 
 */
package net.community.apps.apache.maven.conv2maven;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.SwingWorker;

import net.community.apps.apache.maven.conv2maven.resources.ResourcesAnchor;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.EclipseUtils;
import net.community.chest.eclipse.classpath.ClasspathFileTransformer;
import net.community.chest.eclipse.classpath.ClasspathUtils;
import net.community.chest.eclipse.project.ProjectUtils;
import net.community.chest.io.IOCopier;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2011 9:52:19 AM
 */
class ProjectConverter extends SwingWorker<Void,Map.Entry<LogLevelWrapper,String>> {
	private final MainFrame	_frame;
	public final MainFrame getMainFrame ()
	{
		return _frame;
	}

	ProjectConverter (MainFrame frame)
	{
		if (null == (_frame=frame))
			throw new IllegalStateException("No main frame instance provided");
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground () throws Exception
	{
		final MainFrame	f=getMainFrame();
		final String	root=f.getRootFolder();
		if ((root == null) || (root.length() <= 0))
			throw new IllegalStateException("No root folder available");
		if (convertProject(new File(root), f.isRecursiveScanning()))
			info("Done");
		else
			info("Cancelled");
		return null;
	}

	private boolean convertProject (final File rootFolder, final boolean recursive)
	{
		final String	name=rootFolder.getName();
		if (rootFolder.isDirectory())
		{
			if ("target".equalsIgnoreCase(name) // skip "target" named folders
			  || (name.charAt(0) == '.'))	// skip hidden folders
			return !isCancelled();
		}

		info("Checking " + rootFolder.getAbsolutePath());

		final File	mainFolder=checkMavenProject(rootFolder);
		if (mainFolder != null)
		{
			convertProjectFiles(rootFolder, mainFolder);
			return !isCancelled();
		}

		if (!recursive)
			return !isCancelled();

		final File[]	files=rootFolder.listFiles();
		for (final File f : files)
		{
			if (isCancelled())
				return false;

			if (!f.isDirectory())
				continue;

			if (!convertProject(f, recursive))
				return false;
		}

		return !isCancelled();
	}

	private boolean convertProjectFiles (final File rootFolder, final File mainFolder)
	{
		if (exists(rootFolder, BuildProject.DEFAULT_POM_FILE_NAME) == null)
		{
			warning("No POM file in " + rootFolder.getAbsolutePath());
			return false;
		}

		final boolean	webProject=isWebProject(mainFolder);
		if (!convertEclipseProject(rootFolder, webProject))
		{
			warning("No/bad project file in " + rootFolder.getAbsolutePath());
			return false;
		}

		if (!convertEclipseClasspath(rootFolder, webProject))
		{
			warning("No/bad classpath file in " + rootFolder.getAbsolutePath());
			return false;
		}

		if (!convertEclipseSettings(rootFolder, webProject))
		{
			warning("Bad settings in " + rootFolder.getAbsolutePath());
			return false;
		}

		return true;
	}

	private static boolean isWebProject (final File mainFolder)
	{
		return exists(mainFolder, "webapp") != null;
	}

	private boolean convertEclipseSettings (final File rootFolder, final boolean webProject)
	{
		final File	settingsFolder=new File(rootFolder, EclipseUtils.SETTINGS_SUB_FOLDER_NAME);
		if (!settingsFolder.exists())
		{
			if (!settingsFolder.mkdirs())
			{
				error("Failed to create " + settingsFolder.getAbsolutePath());
				return false;
			}

			debug("Created " + settingsFolder.getAbsolutePath());
		}

		if (!settingsFolder.isDirectory())
		{
			error("Not a directory: " + settingsFolder.getAbsolutePath());
			return false;
		}

		if (!checkEclipseSettingsFiles(settingsFolder,
					"org.eclipse.m2e.core.prefs", "org.maven.ide.eclipse.prefs"))
			return false;

		if (webProject)
			return checkEclipseSettingsFiles(settingsFolder, createProjectNameProps(settingsFolder),
						"org.eclipse.wst.common.component", "org.eclipse.wst.common.project.facet.core.xml");

		return true;
	}

	private boolean checkEclipseSettingsFiles (final File settingsFolder, final String ... names)
	{
		return checkEclipseSettingsFiles(settingsFolder, Collections.<String,String>emptyMap(), names);
	}

	private boolean checkEclipseSettingsFiles (final File				settingsFolder,
											   final Map<String,String> propsMap,
											   final String ... 		names)
	{
		if ((names == null) || (names.length <= 0))
			return true;

		for (final String fileName : names)
		{
			final File	file=new File(settingsFolder, fileName);
			if (file.exists())
			{
				if (!file.isFile())
				{
					error("Not a settings file: " + file.getAbsolutePath());
					return false;
				}

				continue;
			}

			if (!copyUpdateFile(file, "settings/" + fileName, propsMap))
				return false;
		}

		return true;
	}

	private boolean convertEclipseClasspath (final File rootFolder, final boolean webProject)
	{
		final File	classpathFile=new File(rootFolder, EclipseUtils.CLASSPATH_FILE_NAME);
		if (!classpathFile.exists())
		{
			if (!createDefaultClasspath(classpathFile))
				return false;
		}

		if (!classpathFile.isFile())
		{
			error("Not a file: " + classpathFile.getAbsolutePath());
			return false;
		}

		try
		{
			final Document						doc=
					(Document) DOMUtils.loadDocument(classpathFile).cloneNode(true);
			final Element						root=doc.getDocumentElement();
			final Collection<? extends Element>	entries=
					convertClasspathFile(doc, root, root.getChildNodes(), webProject);
			if ((entries != null) && (entries.size() > 0))
			{
				debug("Converting " + classpathFile.getAbsolutePath());
				ClasspathFileTransformer.DEFAULT.transform(doc, classpathFile);
			}

			return true;
		}
		catch(Exception e)
		{
			error("Failed (" + e.getClass().getSimpleName() + ")"
				+ " to parse " + classpathFile.getAbsolutePath()
				+ ": " + e.getMessage());
			return false;
		}
	}

	private Collection<Element> convertClasspathFile (final Document	doc,
													  final Element		root,
													  final NodeList	nodes,
													  final boolean		webProject)
	{
		final int			numNodes=(nodes == null) ? 0 : nodes.getLength();
		Collection<String>	containers=null;
		for (int	index=0; index < numNodes; index++)
		{
			final Node	n=nodes.item(index);
			if ((n == null) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final Element	elem=(Element) n;
			final String	tagName=elem.getTagName();
			if (!ClasspathUtils.CLASSPATHENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
				continue;
			
			final Map.Entry<String,String>	conInfo=ClasspathUtils.getContainerEntryInfo(elem);
			final String					kind=(conInfo == null) ? null : conInfo.getKey(),
											path=(conInfo == null) ? null : conInfo.getValue();
			if (!ClasspathUtils.CON_ENTRY_KIND.equalsIgnoreCase(kind))
				continue;
			if ((path == null) || (path.length() <= 0))
				continue;

			final int		sepPos=path.indexOf('/');
			final String	name=(sepPos < 0) ? path : path.substring(0, sepPos);
			if (containers == null)
				containers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			if (!containers.add(name))
				continue;
		}

		if ((containers != null) && containers.contains("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"))
			return Collections.emptyList();

		final Element	elem=ClasspathUtils.createContainerEntry(doc, "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER");
		if (webProject)
			ClasspathUtils.addWebProjectDependency(doc, elem);
		root.appendChild(elem);
		return Collections.singletonList(elem);
	}

	private boolean createDefaultClasspath (final File classpathFile)
	{
		return createOutputFile(classpathFile, "default-classpath.xml");
	}

	private boolean convertEclipseProject (final File rootFolder, final boolean	webProject)
	{
		final File	projFile=new File(rootFolder, EclipseUtils.PROJECT_FILE_NAME);
		if (!projFile.exists())
		{
			if (!createEclipseProject(projFile))
				return false;
		}

		if (!projFile.isFile())
			return false;

		try
		{
			final Document						doc=
					(Document) DOMUtils.loadDocument(projFile).cloneNode(true);
			final Element						rootElem=doc.getDocumentElement();
			final Collection<? extends Element>	cmdBuild=
					checkBuildSpecs(doc, rootElem.getElementsByTagName(ProjectUtils.BUILD_SPEC_ELEM_NAME), webProject),
												natures=
				    checkProjectNatures(doc, rootElem.getElementsByTagName(ProjectUtils.NATURES_ELEM_NAME), webProject);
			if (((cmdBuild != null) && (cmdBuild.size() > 0))
			 || ((natures != null) && (natures.size() > 0)))
			{
				debug("Converting " + projFile.getAbsolutePath());
				PrettyPrintTransformer.DEFAULT.transform(doc, projFile);
			}

			return true;
		}
		catch(Exception e)
		{
			error("Failed (" + e.getClass().getSimpleName() + ")"
				+ " to parse " + projFile.getAbsolutePath()
				+ ": " + e.getMessage());
			return false;
		}
	}

	private boolean createEclipseProject (final File projFile)
	{
		return copyUpdateFile(projFile, "default-project.xml", createProjectNameProps(projFile));
	}

	private static Map<String,String> createProjectNameProps (final File file)
	{
		final File					parent=file.getParentFile();
		final String				projName=parent.getName();
		final Map<String,String>	propsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
		propsMap.put("$$$project-name$$$", projName);
		return propsMap;
	}

	private boolean copyUpdateFile (final File outFile, final String resPath, final Map<String,String> propsMap)
	{
		if ((propsMap == null) || (propsMap.size() <= 0))
			return createOutputFile(outFile, resPath);

		try
		{
			final ResourcesAnchor	anchor=ResourcesAnchor.getInstance();
			final URL				url=anchor.getResource(resPath);
			final Reader			in=new InputStreamReader(url.openStream());
			final StringWriter		out=new StringWriter(1024);
			try
			{
				final long	cpyBytes=IOCopier.copyReaderToWriter(in, out);
				if (cpyBytes < 0L)
					throw new StreamCorruptedException("Error (" + cpyBytes + ") on copy default project contents");
			}
			finally
			{
				in.close();
			}

			String	content=out.toString();
			for (final Map.Entry<String,String> propEntry : propsMap.entrySet())
			{
				final String	propName=propEntry.getKey(),
								propValue=propEntry.getValue();
				content = content.replace(propName, propValue);
			}

			final Writer	w=new FileWriter(outFile);
			try
			{
				w.append(content);
			}
			finally
			{
				w.close();
			}

			debug("Created " + outFile.getAbsolutePath());
			return true;
		}
		catch(Exception e)
		{
			error("Failed (" + e.getClass().getSimpleName() + ")"
					+ " to create " + outFile.getAbsolutePath()
					+ ": " + e.getMessage());
				return false;
		}
	}

	private boolean createOutputFile (final File outFile, final String resPath)
	{
		try
		{
			final ResourcesAnchor	anchor=ResourcesAnchor.getInstance();
			final URL				url=anchor.getResource(resPath);
			final InputStream		in=url.openStream();
			try
			{
				final long	cpyBytes=IOCopier.copyToFile(in, outFile);
				if (cpyBytes < 0L)
					throw new StreamCorruptedException("Error (" + cpyBytes + ") on copy " + resPath + " contents");
			}
			finally
			{
				in.close();
			}

			debug("Created " + outFile.getAbsolutePath());
			return true;
		}
		catch(Exception e)
		{
			error("Failed (" + e.getClass().getSimpleName() + ")"
				+ " to create " + outFile.getAbsolutePath()
				+ ": " + e.getMessage());
			return false;
		}
	}

	private Collection<Element> checkBuildSpecs (final Document doc, final NodeList nodes, final boolean webProject)
	{
		Collection<Element>	result=null;
		final int			numNodes=(nodes == null) ? 0 : nodes.getLength();
		if (numNodes > 1)
			throw new UnsupportedOperationException("Too many build spec elements: " + numNodes);

		for (int	index=0; index < numNodes; index++)
		{
			final Node	n=nodes.item(index);
			if ((n == null) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final Element				elem=(Element) n;
			final Collection<Element>	cmds=
					checkBuildCommands(doc, elem, elem.getElementsByTagName(ProjectUtils.BUILD_CMD_ELEM_NAME), webProject);
			if ((cmds == null) || (cmds.size() <= 0))
				continue;

			if (result == null)
				result = new LinkedList<Element>(cmds);
			else
				result.addAll(cmds);
		}

		return result;
	}

	private Collection<Element> checkBuildCommands (final Document doc, final Element parent, final NodeList nodes, final boolean webProject)
	{
		Set<String>	names=null;
		final int	numNodes=(nodes == null) ? 0 : nodes.getLength();
		for (int	index=0; index < numNodes; index++)
		{
			final Node	n=nodes.item(index);
			if ((n == null) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final Element	cmdElem=(Element) n;
			final NodeList	children=cmdElem.getChildNodes();
			final int		numChildren=(children == null) ? 0 : children.getLength();
			for (int	childIndex=0; childIndex < numChildren; childIndex++)
			{
				final Node	child=children.item(childIndex);
				if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE))
					continue;

				final Element	elem=(Element) child;
				final String	tagName=elem.getTagName();
				if (!ProjectUtils.BUILD_CMD_NAME_TAG.equalsIgnoreCase(tagName))
					continue;
	
				final String	tagValue=DOMUtils.getElementStringValue(elem);
				if ((tagValue == null) || (tagValue.length() <= 0))
					continue;
				if (names == null)
					names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				if (!names.add(tagValue))
					continue;	// debug breakpoint
			}
		}

		Collection<Element>	result=
				checkProjectBuilders(doc, parent, names,
					"org.eclipse.jdt.core.javabuilder", "org.eclipse.m2e.core.maven2Builder");
		if (webProject)
		{
			final Collection<Element>	elemList=
					checkProjectBuilders(doc, parent, names,
							"org.eclipse.wst.common.project.facet.core.builder", "org.eclipse.wst.validation.validationbuilder");
			if ((result == null) || (result.size() <= 0))
				result = elemList;
			else if ((elemList != null) && (elemList.size() > 0))
				result.addAll(elemList);
		}

		return result;
	}

	private List<Element> checkProjectBuilders (final Document 				doc,
												final Element				parent,
												final Collection<String>	names,
												final String...				builders)
	{
		if ((builders == null) || (builders.length <= 0))
			return Collections.emptyList();

		List<Element>	result=null;
		for (final String name : builders)
		{
			if ((names != null) && names.contains(name))
				continue;

			final Element	elem=ProjectUtils.createBuildCommand(doc, name);
			parent.appendChild(elem);
			if (result == null)
				result = new ArrayList<Element>(builders.length);
			result.add(elem);
		}

		return result;
	}

	private Collection<Element> checkProjectNatures (final Document doc, final NodeList nodes, final boolean webProject)
	{
		final int	numNodes=(nodes == null) ? 0 : nodes.getLength();
		if (numNodes > 1)
			throw new UnsupportedOperationException("Too many natures elements: " + numNodes);

		Collection<Element>	result=null;
		for (int	index=0; index < numNodes; index++)
		{
			final Node	n=nodes.item(index);
			if ((n == null) || (n.getNodeType() != Node.ELEMENT_NODE))
				continue;

			final Element	elem=(Element) n;
			final String	tagName=elem.getTagName();
			if (!ProjectUtils.NATURES_ELEM_NAME.equalsIgnoreCase(tagName))
				continue;
			
			final Collection<String>	natures=ProjectUtils.getNatures(elem);
			Collection<Element>			natList=
					checkProjectNatures(doc, elem, natures, 
										"org.eclipse.m2e.core.maven2Nature",
										"org.eclipse.jdt.core.javanature");
			if (webProject)
			{
				final Collection<Element>	webList=
						checkProjectNatures(doc, elem, natures, 
											"org.eclipse.wst.common.modulecore.ModuleCoreNature",
											"org.eclipse.wst.common.project.facet.core.nature");
				if ((natList == null) || natList.isEmpty())
					natList = webList;
				else if ((webList != null) && (webList.size() > 0))
					natList.addAll(webList);
			}

			if ((natList == null) || natList.isEmpty())
				continue;

			if (result == null)
				result = natList;
			else
				result.addAll(natList);
		}

		return result;
	}

	private List<Element> checkProjectNatures (final Document 			doc,
											   final Element			parent,
											   final Collection<String>	natures,
											   final String ... 		names)
	{
		if ((names == null) || (names.length <= 0))
			return Collections.emptyList();

		List<Element>	result=null;
		for (final String name : names)
		{
			if ((natures != null) && natures.contains(name))
				continue;

			final Element	natureElem=ProjectUtils.createNature(doc, name);
			parent.appendChild(natureElem);
			if (result == null)
				result = new ArrayList<Element>(names.length);
			result.add(natureElem);
		}

		return result;
	}

	// returns the path to the "main"
	private static File checkMavenProject (final File file)
	{
		if ((file == null) || (!file.isDirectory()))
			return null;

		final File	srcFolder=exists(file, "src"),
					mainFolder=exists(srcFolder, "main"),
					javaFolder=exists(mainFolder, "java");
		if ((javaFolder == null) || (!javaFolder.isDirectory()))
			return null;

		return mainFolder;
	}

	private static File exists (final File folder, final String fileName)
	{
		if ((folder == null) || (!folder.isDirectory())
		 || (fileName == null) || (fileName.length() <= 0))
			return null;

		final File	file=new File(folder, fileName);
		if (file.exists())
			return file;
		else
			return null;
	}

	private Map.Entry<LogLevelWrapper,String> error (String msg)
	{
		return log(LogLevelWrapper.ERROR, msg);
	}

	private Map.Entry<LogLevelWrapper,String> warning (String msg)
	{
		return log(LogLevelWrapper.WARNING, msg);
	}

	private Map.Entry<LogLevelWrapper,String> info (String msg)
	{
		return log(LogLevelWrapper.INFO, msg);
	}

	private Map.Entry<LogLevelWrapper,String> debug (String msg)
	{
		return log(LogLevelWrapper.DEBUG, msg);
	}

	@SuppressWarnings("unchecked")
	private Map.Entry<LogLevelWrapper,String> log (LogLevelWrapper l, String msg)
	{
		if ((l == null) || (msg == null) || (msg.length() <= 0))
			return null;

		final Map.Entry<LogLevelWrapper,String>	msgEntry=
				new MapEntryImpl<LogLevelWrapper,String>(l, msg);
		publish(msgEntry);
		return msgEntry;
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<Map.Entry<LogLevelWrapper,String>> chunks)
	{
		if ((chunks == null) || chunks.isEmpty())
			return;

		final MainFrame	f=getMainFrame();
		if (f == null)
			return;

		for (final Map.Entry<LogLevelWrapper,String> msgEntry : chunks)
			f.log(msgEntry.getKey(), msgEntry.getValue());
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		final MainFrame	f=getMainFrame();
		if (f != null)
			f.signalConversionDone(this);
	}
}
