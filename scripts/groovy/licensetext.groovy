#!/usr/bin/env groovy

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.charset.StandardCharsets
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level

import org.codehaus.groovy.tools.shell.ExitNotification

/*
 * Makes sure that provided files contain the correct license header
 */

options = populateDefaultOptions([:])

for (int index = 0; (args != null) && (index < args.length); index++) {
	String argVal = args[index]
	if (!argVal.startsWith("--")) {
		execute(options, index, args)
		return
	}

	List parsed = parseArgVal(argVal)
	String optName = parsed[0]
	def curVal = options[optName]
	if ("verbose".equals(optName)) {
		options['verbose'] = Level.ALL
		continue
	} else if ("quiet".equals(optName)) {
		options['verbose'] = Level.OFF
		continue
	} else if ((parsed.size() == 1) && (curVal instanceof Boolean)) {
		options[optName] = Boolean.TRUE
		continue
	}

	// from here on all options must have an argument value
	if (parsed.size() < 2) {
		dieWithMessage("Unknown option: $argVal")
	}

	String optVal = parsed[1]
	if ("license".equals(optName)) {
		options[optName] = optVal
		continue
	}

	if (curVal == null) {
		dieWithMessage("Unknown option: $argVal")
	}

	if (curVal instanceof Number) {
		options[optName] = Long.valueOf(optVal)
	} else if (curVal instanceof String) {
		options[optName] = optVal
	} else if (curVal instanceof Boolean) {
		options[optName] = Boolean.valueOf(optVal)
	} else if (curVal instanceof File) {
		options[optName] = new File(optVal)
	} else if (curVale instanceof Collection) {
		options[optName] = parseStringList(optVal)
	} else {
		dieWithMessage("Unknown type for $argVal")
	}
}

execute(options, (args == null) ? 0 : args.length, (args == null) ? new String[0] : args)

def execute(opts, int startIndex, String ... args) {
	List targets = []
	for (int index = startIndex; index < args.length; index++) {
		targets << args[index]
	}

	if (targets.size() <= 0) {
		targets << System.getProperty("user.dir")
	}

	Path licenseFile = resolvePathSpecification(opts['license'])
	if (licenseFile == null) {
		dieWithMessage("Missing license file path")
	}

	List licenseLines = Files.readAllLines(licenseFile, StandardCharsets.UTF_8)
	boolean ignoreException = opts['ignore-exceptions'].booleanValue()
	boolean dryRun = opts['dry-run'].booleanValue()
	StringBuilder workBuf = new StringBuilder(Byte.MAX_VALUE)
	targets.each {
		try {
			processTarget(resolvePathSpecification(it), licenseLines, dryRun, ignoreException, workBuf, opts)
		} catch(Throwable t) {
			error(t.getClass().getSimpleName() + " while processing " + it + ": " + t.getMessage())
			if (!ignoreException) {
				throw t
			}
		}
	}
}

def processTarget(Path path, List licenseLines, boolean dryRun, boolean ignoreException, StringBuilder workBuf, opts) {
	if (Files.isDirectory(path)) {
		if (!opts['recursive'].booleanValue()) {
			if (isDebugEnabled()) {
				debug("Skip " + path + " - not recursive")
			}
			return
		}

		DirectoryStream<Path> ds = Files.newDirectoryStream(path)
		try {
			for (Path child : ds) {
				try {
					processTarget(child, licenseLines, dryRun, ignoreException, workBuf, opts)
				} catch(Throwable t) {
					error(t.getClass().getSimpleName() + " while processing " + child + ": " + t.getMessage())
					if (!ignoreException) {
						throw t
					}
				}
			}
		} finally {
			ds.close()
		}
		return
	}

	String name = path.getFileName().toString()
	int pos = name.lastIndexOf('.')
	if ((pos <= 0) || (pos >= (name.length() - 1))) {
		if (isDebugEnabled()) {
			debug("Skip " + path + " - no suffix")
		}
		return
	}

	// TODO implement a dispatch model according to the suffix
	String sfx = name.substring(pos + 1)
	List lines = null
	if ("java".equals(sfx)) {
		lines = Files.readAllLines(path, StandardCharsets.UTF_8)
	}

	if ((lines == null) || (lines.size() <= 0)) {
		if (isDebugEnabled()) {
			debug("Skip " + path + " - unsupported suffix")
		}
		return

	}
	// TODO make read/write charset configurable
	List updated = null
	if ("java".equals(sfx)) {
		updated = processJavaFile(path, licenseLines, lines, workBuf, opts)
	}

	if ((updated == null) || (updated.size() <= 0)) {
		if (isDebugEnabled()) {
			debug("Skip " + path + " - no changes")
		}
		return
	}

	info(path)

	if (!dryRun) {
		Files.write(path, updated, StandardCharsets.UTF_8)
	}
}

/* ------------------------------------------------------------------------ */

def populateDefaultOptions(opts) {
	opts['recursive'] = Boolean.TRUE
	opts['verbose'] = Level.INFO
	opts['ignore-exceptions'] = Boolean.FALSE
	opts['dry-run'] = Boolean.FALSE
	opts['javadoc-style'] = Boolean.FALSE

	return opts
}

static List processJavaFile(Path file, List licenseLines, List lines, StringBuilder workBuf, opts) {
	List headers = []
	int pkgIndex = 0
	String topLine = lines[0]
	boolean asJavadoc = opts['javadoc-style'].booleanValue()
	boolean checkHeaders = true

	// handle top line
	if ((topLine.length() >= 2) && (topLine.charAt(0) == ('/' as char)) && (topLine.charAt(1) == ('*' as char))) {
		pkgIndex++

		if (asJavadoc) {
			checkHeaders = (topLine.length() == 3) && (topLine.charAt(2) != ('*' as char))
		} else {
			checkHeaders = (topLine.length() == 2)
		}
	}

	for (; pkgIndex < lines.size(); pkgIndex++) {
		String l = lines[pkgIndex]
		if (l.startsWith("package ")) {
			break
		}

		if (checkHeaders) {
			headers << l
		}
	}

	if (pkgIndex >= lines.size()) {
		throw new StreamCorruptedException("Missing package declaration")
	}


	int numHeaders = headers.size()
	// remove last header line if followed by an empty line
	if (checkHeaders && (numHeaders >= 2)) {
		String l = headers[numHeaders - 1]
		if (l.length() == 0) {
			l = headers[numHeaders - 2]
			if (" */".equals(l)) {
				headers.remove(numHeaders - 1)
				numHeaders--
				headers.remove(numHeaders - 1)
				numHeaders--
			} else {
				checkHeaders = false
			}
		} else {
			checkHeaders = false
		}
	}

	for (int index = 0; checkHeaders && (index < headers.size()); index++) {
		String l = headers[index]
		// must have at least space followed by asteriks and then space
		if ((l.length() < 2) || (l.charAt(0) != (' ' as char)) || (l.charAt(1) != ('*' as char))) {
			checkHeaders = false
			break
		}

		// if non-empty line then it must start with a space
		if ((l.length() >= 3) && (l.charAt(2) != (' ' as char))) {
			checkHeaders = false
			break
		}

		if (l.length() >= 3) {
			headers[index] = l.substring(3)
		} else {
			headers[index] = ""
		}
	}


	if (checkHeaders && compareLicenseHeaders(licenseLines, headers)) {
		return null
	}

	headers.clear()
	headers << (asJavadoc ? '/**' : '/*')
	for (int index = 0; index < licenseLines.size(); index++) {
		String l = licenseLines[index]
		workBuf.setLength(0)
		workBuf.append(' * ').append(l)
		headers << workBuf.toString()
	}
	headers << ' */'
	headers << ''

	for ( ; pkgIndex < lines.size(); pkgIndex++) {
		String l = lines[pkgIndex]
		headers << l
	}

	return headers
}

static boolean compareLicenseHeaders(List licenseLines, List headers) {
	if (licenseLines.size() != headers.size()) {
		return false
	}

	for (int index = 0; index < licenseLines.size(); index++) {
		String l = licenseLines[index]
		String h = headers[index]
		if (!l.equals(h)) {
			return false
		}
	}

	return true
}

//////////////////////////////////////////////////////////////////////////////

static Path resolvePathSpecification(String path) {
	if ((path == null) || (path.length() <= 0)) {
		return null
	}

	Path p = Paths.get(path)
	p = p.normalize()
	p = p.toAbsolutePath()
	return p.toRealPath()
}

static String stripQuotes(String v) {
	char delim = v.charAt(0) as char
	if ((delim != ('"' as char)) && (delim != ('\'' as char))) {
		return v
	}

	int lastPos = v.lastIndexOf((int) delim)
	if (lastPos == 0) {
		throw new IllegalArgumentException("Imbalanced quotes: $v")
	}

	return v.substring(1, lastPos)
}

static List parseStringList(String list) {
	return list.split(",").collect { it }
}

static List parseArgVal(String opt) {
	int pos = opt.indexOf('=')
	if (pos < 0) {
		return [ opt.substring(2) ]
	} else {
		return [ opt.substring(2, pos), opt.substring(pos + 1) ]
	}
}

def dieWithMessage(String msg, int rc=1) {
	error(msg)
	throw new ExitNotification(rc)
}

def error(msg) {
	log(Level.SEVERE, msg)
}

def warn(msg) {
	log(Level.WARNING, msg)
}

def info(msg) {
	log(Level.INFO, msg)
}

boolean isDebugEnabled() {
	return isLevelEnabled(Level.FINE)
}

def debug(msg) {
	log(Level.FINE, msg)
}

boolean isTraceEnabled() {
	return isLevelEnabled(Level.FINER)
}

def trace(msg) {
	log(Level.FINER, msg)
}

def log(Level level, msg) {
	if (isLevelEnabled(level)) {
		String effMsg = msg.toString().replaceAll('\t','    ')
		if (effMsg.indexOf('\n') < 0) {
			println "[$level.name] $effMsg"
		} else {
			String[] lines = effMsg.split('\n')
			for (String l : lines) {
				println "[$level.name] $l"
		 }
		}
	}
}

boolean isLevelEnabled(Level level) {
	def verbosity=options['verbose']
	if (level.intValue() < verbosity.intValue()) {
		return false
	} else {
		return true
	}
}