/*
 * 
 */
package net.community.chest.groovy.apps.mvntree

import java.io.BufferedReader
import java.io.InputStream
import java.io.Closeable
import java.io.IOException
import java.util.ArrayList;
import java.util.List

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Represents the result of one dependency node parsing</P
 * @author Lyor G.
 * @since Dec 7, 2010 9:27:32 AM
 */
class MvnDependencyNode {
	String groupId
	String artifactId
	String packaging	// jar, war, etc.
	String phase = ""	// test, compile, etc. - may be empty
	String version
	MvnDependencyNode parent	// null == root
	List<MvnDependencyNode>	children

	MvnDependencyNode (String line) {
		for (int startIndex=line.indexOf(':'); startIndex >= 0; startIndex--) {
			if (line.charAt(startIndex) != ' ') {
				continue
			}

			def comps=line.substring(startIndex + 1).split(":")
			if ((comps == null) || (comps.length < 4)) {
				throw new IllegalArgumentException("Bad line format: missing components: $line")
			}

			this.groupId = comps[0]
			this.artifactId = comps[1]
			this.packaging = comps[2]
			this.version = comps[3]
			
			if (comps.length > 4) {
				this.phase = comps[4]
			}

			this.children = new ArrayList<MvnDependencyNode>()
			return
		}

		// this line is reached if no space found
		throw new IllegalArgumentException("Bad line format: no starting space: $line")
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return groupId			\
		     + ":" + artifactId	\
			 + ":" + packaging	\
			 + ":" + version	\
			 + (((phase != null) && (phase.length() > 0)) ? ":" + phase : "")
	}

	static MvnDependencyNode buildDependencyTree (File mvnCmdLocation, List<String> mvnCmdArgs, File pomFilePath, boolean verbose) {
		def osType=System.getProperty("os.name", "<unknown>").toLowerCase()
		if ((osType == null) || (osType.length() <= 0)) {
			return null
		}

		List<String> commands=[]	
		if (osType.contains("windows")) {
			commands += [] // [ "cmd.exe", "/c", "start" ]
		}
		else {
			commands += [ "/bin/sh" ]
		}
		
		commands.add(mvnCmdLocation.getAbsolutePath())
		if ((mvnCmdArgs != null) && (mvnCmdArgs.size() > 0)) {
			commands += mvnCmdArgs
		}
		
		commands += [ "-f", pomFilePath.getAbsolutePath(), "dependency:tree" ]
		ProcessBuilder	procBuilder=new ProcessBuilder(commands)
		procBuilder.directory = pomFilePath.getParentFile()
		procBuilder.redirectErrorStream = true
		
		Process				proc=procBuilder.start()
		MvnStreamGobbler	gobbler=new MvnStreamGobbler(proc.getInputStream(), verbose)
		try {
			gobbler.start()

			int	exitValue=proc.waitFor()
			if (exitValue != 0) {
				MvnTree.die "Failed to complete Maven command: $exitValue"
			}
			
			gobbler.join 5000L	// wait for the gobbler to exit on its own
			if (gobbler.isAlive()) {
				def threadState=gobbler.getState()
				throw new IllegalStateException("Gobbler stream still alive at state = $threadState")
			}
		}
		finally {
			gobbler.close()
		}

		return gobbler.rootNode
	}
}
