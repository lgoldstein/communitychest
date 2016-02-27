/*
 * 
 */
package net.community.chest.groovy.apps.mvntree

import java.io.File
import java.util.List

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Runs the mvn dependency:tree command using a specific POM file and
 * displays its results in a graphical UI</P>
 * @author Lyor G.
 * @since Dec 7, 2010 8:34:02 AM
 */
class MvnTree {
	/**
	 * @return The default Maven script location to be used - M2_HOME + bin + mvn.bat/sh
	 * (according to the O/S)
	 */
	static String getDefaultMavenCommandLocation () {
		def M2HomeValue=System.getenv("M2_HOME")
		if ((M2HomeValue == null) || (M2HomeValue.length() <= 0)) {
			return null
		}

		def osType=System.getProperty("os.name", "<unknown>").toLowerCase()
		if ((osType == null) || (osType.length() <= 0)) {
			return null
		}

		M2HomeValue += File.separator + "bin" + File.separator
		if (osType.contains("windows")) {
			return M2HomeValue +  "mvn.bat"
		}
		else {
			return M2HomeValue + "mvn"
		}
	}
	/**
	 * @return Default POM file path to use - CWD + pom.xml
	 */
	static String getDefaultPomFilePath () {
		def curdir=System.getProperty("user.dir")
		if ((curdir == null) || (curdir.length() <= 0)) {
			return null
		}

		return curdir + File.separator + "pom.xml"
	}
	/**
	 * Displays the provided message followed by the usage help and then exits the application
	 * @param message Message to be displayed before the usage help
	 */
	static void die (message) {
		println message
		println ""
		println "Usage: MvnTree [OPTIONS]"
		println ""
		println "\t--mvncmd <path> - path to Maven script (default=according to M2_HOME environment variable and O/S)"
		println "\t\t" + getDefaultMavenCommandLocation()
		println ""
		println "\t--mvnarg <argument> - argument to add to the Maven script invocation (e.g., --mvnarg -DskipTests=true)"
		println "\t\tNOTE: This option can be re-specified with different (!) arguments."
		println "\t\t      The arguments are used in the same order (!) as specified."
		println ""
		println "\t--pom <path> - path to POM file to be used (default=pom.xml file in CWD)"
		println "\t\t" + getDefaultPomFilePath()
		println ""
		println "\t--help - show this message"
		
		System.exit 1
	}
	/**
	 * @param args Arguments array
	 * @param argVal Current option argument value
	 * @param aIndex Current index
	 * @return The next argument relative to the current index - {@link #die()}-s
	 * with an appropriate message if no more arguments available
	 */
	static String getNextArgument (args, argVal, aIndex) {
		def	numArgs=(args == null) ? 0 : args.length
		if (aIndex >= (numArgs - 1)) {
			die "No argument for option=${ argVal }"
		}

		return args[aIndex + 1]
	}

	/* -------------------------------------------------------------------- */

	private static File	_mvnCmd
	private static List<String>	_mvnCmdArgs=[]
	private static File	_pomFilePath
	private static Boolean	_verbose

	static processMainArgs (args) {
		if ((args == null) || (args.length <= 0))
			return

		for (int aIndex=0; aIndex < args.length; aIndex++) {
			def	argVal=args[aIndex]
			if (argVal == "--mvncmd") {
				if (_mvnCmd != null) {
					die "Re-specified option=$argVal value"
				}
				
				def	cmdPath=getNextArgument(args, argVal, aIndex)
				_mvnCmd = new File(cmdPath)
				if (!_mvnCmd.exists()) {
					die "Maven command path does not exist: $cmdPath" 
				}
				
				aIndex++
			}
			else if (argVal == "--pom") {
				if (_pomFilePath != null) {
					die "Re-specified option=$argVal value"
				}

				def	pomPath=getNextArgument(args, argVal, aIndex)
				_pomFilePath = new File(pomPath)
				if (!_pomFilePath.exists()) {
					die "Specified POM file path does not exist: $pomPath" 
				}
				
				aIndex++
			}
			else if (argVal == "--mvnarg") {
				def mvnArg=getNextArgument(args, argVal, aIndex)
				if (_mvnCmdArgs.contains(mvnArg)) {
					die "Re-specified option=$argVal value=$mvnArg"
				}

				_mvnCmdArgs.add mvnArg
				aIndex++
			}
			else if (argVal == "--help") {
				die ""
			}
			else if (argVal == "--verbose") {
				if (_verbose != null) {
					die "Re-specified option=$argVal value"
				}
				_verbose = Boolean.TRUE
			}
			else {
				die "Unknown option: $argVal"
			}
		}
	}
	/**
	 * Resolves the Maven script location to be used
	 * @param curLocation Last known location (if provided via command line)
	 * @return The last known location (if not <code>null</code>) - otherwise
	 * the {@link #getDefaultMavenCommandLocation()} result. <B>Note:</B> it
	 * {@link #die()}-s with appropriate message if the default location
	 * does not exist
	 */
	static File resolveMavenCommand (File curLocation) {
		if (curLocation != null) {
			return curLocation
		}

		String defPath=getDefaultMavenCommandLocation()
		if ((defPath == null) || (defPath.length() <= 0)) {
			die "Cannot resolve default Maven script location"
		}

		File	mvnLocation=new File(defPath);
		if (!mvnLocation.exists()) {
			die "Default Maven script not found at $defPath"
		}

		return mvnLocation
	}
	/**
	 * Resolves the POM file location to be used
	 * @param curLocation Last known location (if provided via command line)
	 * @return The last known location (if not <code>null</code>) - otherwise
	 * the {@link #getDefaultPomFilePath()} result. <B>Note:</B> it
	 * {@link #die()}-s with appropriate message if the default location
	 * does not exist
	 */
	static File resolvePomFilePath (File curLocation) {
		if (curLocation != null) {
			return curLocation
		}

		String defPath=getDefaultPomFilePath()
		if ((defPath == null) || (defPath.length() <= 0)) {
			die "Cannot resolve default POM file location"
		}

		File	pomLocation=new File(defPath);
		if (!pomLocation.exists()) {
			die "Default POM file not found at $defPath"
		}

		return pomLocation
	}

	//////////////////////////////////////////////////////////////////////////

	static main(args) {
		processMainArgs(args)
		
		_mvnCmd = resolveMavenCommand(_mvnCmd)
		_pomFilePath = resolvePomFilePath(_pomFilePath)
		
		MvnDependencyNode	rootNode=MvnDependencyNode.buildDependencyTree(_mvnCmd, _mvnCmdArgs, _pomFilePath, _verbose != null)
		if (rootNode == null) {
			die "No data extracted from POM $_pomFilePath"
		}

		MvnDependencyDisplay.showDependencies rootNode
	}
}
