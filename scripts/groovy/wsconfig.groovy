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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level

import org.codehaus.groovy.tools.shell.ExitNotification

/*
 * Configures useful Eclipse workspace preferences
 *
 * - If NEW file created use:
 *      eclipse.preferences.version=1 (configurable)
 *
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.ui.editors.prefs
 *         spacesForTabs=true    [General->Editors->Text Editors->Spaces for tabs]
 *
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs
 *         encoding=UTF-8    [General->Workspace->Text file encoding]
 *
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.runtime.prefs
 *         line.separator=\n    [General->Workspace->Text file new line delimiter]
 *
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs
 *         org.eclipse.jdt.core.formatter.tabulation.char=space  [Java->Code style->Formatter->Tab policy]
 *         ...attached file... - [Java->Compiler->Errors and Warnings]
 *
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.ui.prefs
 *          org.eclipse.jdt.ui.formatterprofiles= ... <setting id\="org.eclipse.jdt.core.formatter.tabulation.char" value\="space"/>\r\n    [Java->Code style->Formatter->Tab policy]
 *          org.eclipse.jdt.ui.text.code_templates_migrated=true [Java->Code Style->Templates->Comments->Overriding methods]
 *          org.eclipse.jdt.ui.text.custom_code_templates=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?><templates><template autoinsert\="false" context\="overridecomment_context" deleted\="false" description\="Comment for overriding methods" enabled\="true" id\="org.eclipse.jdt.ui.text.codetemplates.overridecomment" name\="overridecomment"/></templates>
 *          org.eclipse.jdt.ui.exception.name=e  [Java->Compiler->Errors and Warnings]
 *          org.eclipse.jdt.ui.gettersetter.use.is=true  [Java->Compiler->Errors and Warnings]
 *          org.eclipse.jdt.ui.overrideannotation=true  [Java->Compiler->Errors and Warnings]
 *          editor_folding_enabled=false [Java->Editor->Folding]
 *          editor_save_participant_org.eclipse.jdt.ui.postsavelistener.cleanup=true [Java->Editor->Save Actions->Perform selected actions on save]
 *          org.eclipse.jdt.ui.ondemandthreshold=99 [Java->Editor->Save Actions->Perform selected actions on save]
 *          org.eclipse.jdt.ui.staticondemandthreshold=99 [Java->Editor->Save Actions->Perform selected actions on save]
 *          org.eclipse.jdt.ui.ignorelowercasenames=true [Java->Editor->Save Actions]
 *          org.eclipse.jdt.ui.importorder=java;javax;org;com;  [Java->Editor->Save Actions->Organize Imports]
 *          sp_cleanup.remove_unused_imports=true       [Java->Editor->Save Actions->Configure->Unnecessary code->Remove unused imports]
 *          sp_cleanup.remove_unnecessary_casts=true    [Java->Editor->Save Actions->Configure->Unnecessary code->Remove unnecessary casts]
 *
 *  - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.wst.xml.core.prefs
 *          indentationChar=space   [XML->Xml Files->Editor->Insert using spaces]
 *          indentationSize=4       [XML->Xml Files->Editor->Insert using spaces]
 *
 *  - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs
 *          eclipse.m2.downloadJavadoc=true [Maven->Download Artifacts JavaDoc]
 *          eclipse.m2.downloadSources=true [Maven->Download Artifacts Sources]
 *
 * - TODO
 *     [V] Maven - auto-download sources and javadocs
 *     [] Java define default import order
 *     [V] Java - remove "{@non-javadoc}" remarks from overridden methods comments
 *     [V] Java - Auto-save actions:
 *          [V] Remove unused import
 *          [V] Re-order imports
 *          [V] Add missing @Override
 *          [V] Remove trailing whitespaces
 *     [V] Ensure tab length = 4 in Java formatter
 *     [V] Ditto for general text editors
 *     [V] Ditto for XML
 *     [] Ditto for properties files
 */

options = populateDefaultOptions([:])

for (int index = 0; (args != null) && (index < args.length); index++) {
    String argVal = args[index]
    if (!argVal.startsWith("--")) {
        execute(options, index, argVal)
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

dieWithMessage("Missing workspace location argument")

def execute(opts, int startIndex, String workspaceLocation) {
    Path path = Paths.get(workspaceLocation)
    path = path.normalize()
    path = path.toAbsolutePath()
    path = path.toRealPath()

    Path name = path.getFileName()
    if (!".metadata".equals(name.toString())) {
        path = path.resolve(".metadata")
    }
    processWorkspace(path, opts['ignore-exceptions'].booleanValue(), opts['dry-run'].booleanValue(), opts)
}

def processWorkspace(Path path, boolean ignoreExceptions, boolean dryRun, opts) {
    try {
        processPlugins(path.resolve(".plugins"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }
}

def processPlugins(Path path, boolean ignoreExceptions, boolean dryRun, opts) {
    try {
        processEclipseCoreRuntime(path.resolve("org.eclipse.core.runtime"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }
}

def processEclipseCoreRuntime(Path path, boolean ignoreExceptions, boolean dryRun, opts) {
    try {
        processEclipseCoreRuntimeSettings(path.resolve(".settings"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }
}

def processEclipseCoreRuntimeSettings(Path path, boolean ignoreExceptions, boolean dryRun, opts) {
    try {
        processUIEditorsPrefs(path.resolve("org.eclipse.ui.editors.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }
}

def processUIEditorsPrefs(Path path, boolean dryRun, opts) {
    info(path)
    // TODO make read/write charset configurable
    List lines = Files.readAllLines(path, StandardCharsets.UTF_8)
    int numChanged = blahblah(path, lines, workBuf, opts)
    if (numChanged <= 0) {
        if (isDebugEnabled()) {
            debug("Skip " + path + " - no changes")
        }
        return
    }

    if (!dryRun) {
        Files.write(path, lines, StandardCharsets.UTF_8)
    }

    if (isDebugEnabled()) {
        debug("Modified " + path + " - " + numChanged + " lines")
    }
}

/* ------------------------------------------------------------------------ */

def populateDefaultOptions(opts) {
    opts['eclipse.preferences.version'] = 1
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