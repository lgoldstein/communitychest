#!/usr/bin/env groovy

import java.util.jar.JarEntry

import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/*
 * Locates all classes in the WAR that are provided by a JBoss module and
 * creates an exclusion clause for the module
 */
//for (String className : indexWarPath(this.args[1])) {
//	System.out.println className
//}

//for (Map.Entry<String,? extends Set<String>> fe : indexModule(new File(this.args[0]))) {
//	String		name=fe.getKey()
//	Set<String>	moduleIndex=fe.getValue()
//	System.out.println "====================== $name ===================="
//	for(String className : moduleIndex) {
//		System.out.println "\t$className"
//	}
//}

doMain(this.args)

private static void doMain(String ...args) {
	if ((args == null) || (args.length <= 0)) {
		dieWithUsage(-1, "Missing modules root folder")
	}
	
	if (args.length <= 1) {
		dieWithUsage(-1, "Missing WAR(s) list")
	}
	
	Map<String,Set<String>> modulesMap=indexModule(new File(args[0]))
	String[]				warPaths=new String[args.length - 1]
	if (args.length == 2) {
		warPaths[0] = args[1]
 	} else {
	 	System.arraycopy(args, 1, warPaths, 0, warPaths.length)
 	}
	 
	processWars(modulesMap, warPaths)
}

private static void dieWithUsage(int code, String message) {
	System.err.println message
	showUsage()
	System.exit(code)
}

private static void showUsage() {
	System.out.println "Usage: jboss7structFile <modules-root-dir> <war-1> <war-2> ..."
	System.out.println()
	System.out.println "Where:"
	System.out.println()
		System.out.println "\tmodules-root-dir - root location of JBoss modules"
		System.out.println "\twar1, 2, 3 - location of WAR file(s) to be processed"
}

private static void processWars(Map<String,Set<String>> modulesMap, String ... warPaths) {
	if ((warPaths == null) || (warPaths.length <= 0)) {
		return
	}
	
	Map<String,Set<String>>	classesMap=flipModulesMap(modulesMap)
	for (String warName : warPaths) {
		Collection<String>	modules=processWar(classesMap, warName)
		if ((modules == null) || modules.isEmpty()) {
			continue
		}
		
		if (warPaths.length > 1) {
			System.out.println "======================= $warName ===================="
		}
		System.out.println "\t\t<exclusions>"
		for(String name : modules) {
			System.out.println "\t\t\t<module name=\"$name\"/>"
		}
		System.out.println "\t\t</exclusions>"
	}
}

private static Map<String,Set<String>> flipModulesMap(Map<String,Set<String>> modulesMap) {
	if ((modulesMap == null) || modulesMap.isEmpty()) {
		return Collections.emptyMap()
	}
	
	Map<String,Set<String>>	classesMap=new TreeMap<String,Set<String>>()
	for (Map.Entry<String,? extends Set<String>> fe : modulesMap.entrySet()) {
		String	moduleName=fe.getKey()
		Collection<String>	moduleIndex=fe.getValue()
		if ((moduleIndex == null) || moduleIndex.isEmpty()) {
			continue;
		}
		
		for(String className : moduleIndex) {
			Set<String>	modules=classesMap.get(className)
			if (modules == null) {
				modules = new TreeSet<String>()
				classesMap.put(className, modules)
			}
			
			modules.add(moduleName)
		}
	}
	
	return classesMap
}

private static Set<String> processWar(Map<String,Set<String>> classesMap, String warPath) {
	Collection<String>	warIndex=indexWarPath(warPath)
	if ((warIndex == null) || warIndex.isEmpty()) {
		return Collections.emptySet()
	}
	
	Set<String>	modules=null
	for (String className : warIndex) {
		Collection<String>	names=classesMap.get(className)
		if ((names == null) || names.isEmpty()) {
			continue;
		}
		
		if (modules == null) {
			modules = new TreeSet<String>()
		}
		modules.addAll(names)
	}
	
	if (modules == null) {
		return Collections.emptySet()
	} else {
		return modules
	}
}

private static Map<String,Set<String>> indexModule(File path) {
	if (!path.isDirectory()) {
		return Collections.emptyMap()
	}

	File	moduleXml=new File(path, "module.xml")
	if (moduleXml.canRead()) {
		return indexModuleJars(extractModuleName(moduleXml), path)
	}
	
	File[]	files=path.listFiles()
	if ((files == null) || (files.length <= 0)) {
		return Collections.emptyMap()
	}
	
	Map<String,Set<String>>	filesMap=null
	for (File f : files) {
		Map<String,? extends Set<String>>	fileIndex=indexModule(f)
		if ((fileIndex == null) || fileIndex.isEmpty()) {
			continue
		}
		
		for (Map.Entry<String,? extends Set<String>> fe : fileIndex.entrySet()) {
			String		name=fe.getKey()
			Set<String>	newValues=fe.getValue()
			if ((newValues == null) || newValues.isEmpty()) {
				continue
			}

			if (filesMap == null) {
				filesMap = new TreeMap<String,Set<String>>()
			}
			Set<String>	curValues=filesMap.get(name)
			if (curValues == null) {
				filesMap.put(name, newValues)
			} else {
				curValues.addAll(newValues)
			}
		}
	}
	
	if (filesMap == null) {
		return Collections.emptyMap()
	} else {
		return filesMap
	}
}

private static String extractModuleName(File moduleXml) {
	def	moduleDefinition=new XmlParser().parse(moduleXml)
	return moduleDefinition.attribute("name")
}

private static Map<String,Set<String>> indexModuleJars(String name, File root) {
	File[]	files=root.listFiles()
	if ((files == null) || (files.length <= 0)) {
		return Collections.emptyMap()
	}

	Set<String>	classIndex=null
	for (File f : files) {
		if ((!f.isFile()) || (!f.canRead()) || (!f.getName().endsWith(".jar"))) {
			continue
		}
		
		Set<String>	jarIndex=indexJarFile(f);
		if ((jarIndex == null) || jarIndex.isEmpty()) {
			continue
		}
		
		if (classIndex == null) {
			classIndex = jarIndex
		} else {
			classIndex.addAll(jarIndex)
		}
	}
	
	if ((classIndex == null) || classIndex.isEmpty()) {
		return Collections.emptyMap()
	}
	
	return Collections.singletonMap(name,  classIndex)
}

private static Set<String> indexWarPath(String path) {
	return indexWarFile(new File(path));
}

private static Set<String> indexWarFile(File file) {
	ZipInputStream	warStream=new ZipInputStream(new BufferedInputStream(new FileInputStream(file), 4096))
	try {
		return indexWarStream(warStream)
	} finally {
		warStream.close()
	}
}

private static Set<String> indexWarStream(ZipInputStream warStream) {
	Set<String>	classIndex=new TreeSet<String>()
	for(ZipEntry ze=warStream.getNextEntry(); ze != null; ze=warStream.getNextEntry()) {
		String	name=ze.getName()
		if (name.startsWith("WEB-INF/classes/")) {
			updateWebInfClass(classIndex, warStream, name.substring("WEB-INF/classes/".length()))
		} else if (name.startsWith("WEB-INF/lib/")) {
			updateWebInfLib(classIndex, warStream, name.substring("WEB-INF/lib/".length()))
		}
		
		warStream.closeEntry();
	}
	
	return classIndex
}

private static String updateWebInfClass(Set<String> classIndex, ZipInputStream warStream, String entryName) {
	if (entryName.endsWith(".class")) {
		classIndex.add(entryName)
		return entryName
	} else {
		return null
	}
}

private static Set<String> updateWebInfLib(Set<String> classIndex, ZipInputStream warStream, String entryName) {
	if (entryName.endsWith(".jar")) {
		Set<String>	jarIndex=indexEmbeddedJarStream(warStream)
		classIndex.addAll(jarIndex)
		return jarIndex;
	} else {
		return Collections.emptySet()
	}
}

private static Set<String> indexEmbeddedJarStream(InputStream orgStream) {
	JarInputStream	jarStream=new JarInputStream(new NonClosingInputStream(orgStream));
	try {
		return indexJarStream(jarStream)
	} finally {
		jarStream.close()
	}
}

private static Set<String> indexJarFile(File file) {
	JarInputStream	jarStream=new JarInputStream(new BufferedInputStream(new FileInputStream(file), 4096))
	try {
		return indexJarStream(jarStream)
	} finally {
		jarStream.close()
	}
}

private static Set<String> indexJarStream(JarInputStream jarStream) {
	Set<String>	jarIndex=new TreeSet<String>()
	for (JarEntry entry=jarStream.getNextJarEntry(); entry != null; entry=jarStream.getNextJarEntry()) {
		String	name=entry.getName();
		if (name.endsWith(".class")) {
			jarIndex.add(name);
		}
	}
	
	return jarIndex;
}


class NonClosingInputStream extends FilterInputStream {
    NonClosingInputStream(InputStream s) {
        super(s)
    }
    
    @Override
    public void close() throws IOException {
        // ignored
    }
}