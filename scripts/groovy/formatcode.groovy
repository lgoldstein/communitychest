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
 * Formats source files by replacing tabs with spaces and removing trailing white spaces
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

	boolean ignoreException = opts['ignore-exceptions'].booleanValue()
	boolean dryRun = opts['dry-run'].booleanValue()
	List suffixes = opts['suffixes']
	StringBuilder workBuf = new StringBuilder(Byte.MAX_VALUE)
	targets.each {
		try {
			Path path = Paths.get(it)
			path = path.normalize()
			path = path.toAbsolutePath()
			path = path.toRealPath()
			processTarget(path, suffixes, dryRun, ignoreException, workBuf, opts)
		} catch(Throwable t) {
			error(t.getClass().getSimpleName() + " while processing " + it + ": " + t.getMessage())
			if (!ignoreException) {
				throw t
			}
		}
	}
}

def processTarget(Path path, List suffixes, boolean dryRun, boolean ignoreException, StringBuilder workBuf, opts) {
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
					processTarget(child, suffixes, dryRun, ignoreException, workBuf, opts)
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

	String sfx = name.substring(pos + 1)
	if (!suffixes.contains(sfx)) {
		if (isDebugEnabled()) {
			debug("Skip " + path + " - suffix not listed")
		}
		return
	}

	// TODO make read/write charset configurable
	List lines = Files.readAllLines(path, StandardCharsets.UTF_8)
	int numChanged = processContentLines(path, lines, workBuf, opts)
	if (numChanged <= 0) {
		if (isDebugEnabled()) {
			debug("Skip " + path + " - no changes")
		}
		return
	}

	info(path)

	if (!dryRun) {
		Files.write(path, lines, StandardCharsets.UTF_8)
	}

	if (isDebugEnabled()) {
		debug("Modified " + path + " - " + numChanged + " lines")
	}
}

int processContentLines(Path path, List lines, StringBuilder workBuf, opts) {
	boolean stripWhitespace = opts['strip-trailing-whitespace'].booleanValue()
	int tabLength = opts['tab-length'].intValue()
	int numChanges = 0
	for (int index = 0; index < lines.size(); index++) {
		String l = lines[index]
		String lineValue = l
		if (stripWhitespace) {
			lineValue = stripTrailingWhitespace(lineValue)
		}

		if (tabLength > 0) {
			lineValue = replaceTabsWithSpaces(lineValue, tabLength, workBuf)
		}

		if (lineValue != l) {
			lines[index] = lineValue
			numChanges++
		}
	}

	return numChanges
}

static String stripTrailingWhitespace(String s) {
	if ((s == null) || (s.length() <= 0)) {
		return s
	}

	for (int index = s.length() - 1; index >= 0; index--) {
		char c = s.charAt(index) as char
		if (" \t".indexOf(c as int) < 0) {
			return (index < (s.length() - 1)) ? s.substring(0, index + 1) : s
		}
	}

	// this point is reached if line contains only white space
	return ""
}

static String replaceTabsWithSpaces(String line, int tabLength, StringBuilder workBuf) {
	if ((tabLength <= 0) || (line == null) || (line.length() <= 0)) {
		return line
	}

	int curPos = line.indexOf('\t')
	if (curPos < 0) {
		return line
	}

	int lastPos = 0
	workBuf.setLength(0)	// start fresh
	while(curPos >= lastPos) {
		if (curPos > lastPos) {
			String plainText = line.substring(lastPos, curPos)
			workBuf.append(plainText)
		}

		for (int index = 0; index < tabLength; index++) {
			workBuf.append(' ')
		}

		lastPos = curPos + 1
		if (lastPos >= line.length()) {
			break
		}

		curPos = line.indexOf('\t', lastPos)
	}

	if (lastPos < line.length()) {
		String plainText = line.substring(lastPos)
		workBuf.append(plainText)
	}

	return workBuf.toString()
}

/* ------------------------------------------------------------------------ */

def populateDefaultOptions(opts) {
	opts['tab-length'] = 4	// zero means no processing
	opts['recursive'] = Boolean.TRUE
	opts['strip-trailing-whitespace'] = Boolean.TRUE
	opts['suffixes'] = [ 'xml', 'java', 'groovy', 'properties' ]
	opts['verbose'] = Level.INFO
	opts['ignore-exceptions'] = Boolean.FALSE
	opts['dry-run'] = Boolean.FALSE

	return opts
}

//////////////////////////////////////////////////////////////////////////////

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