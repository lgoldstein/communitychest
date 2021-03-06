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
 * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.ui.prefs
 *          org.eclipse.jdt.ui.formatterprofiles= ... <setting id\="org.eclipse.jdt.core.formatter.tabulation.char" value\="space"/>\r\n    [Java->Code style->Formatter->Tab policy]
 *          org.eclipse.jdt.ui.text.code_templates_migrated=true [Java->Code Style->Templates->Comments->Overriding methods]
 *          org.eclipse.jdt.ui.text.custom_code_templates=... <setting id\="org.eclipse.jdt.core.formatter.indentation.size" value\="4"/>\r\n - same as 'indentationSize'
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

def execute(opts, String workspaceLocation) {
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

def processWorkspace(Path path, boolean ignoreExceptions, boolean dryRun, Map opts) {
    int numChanges = 0

    try {
        numChanges += processPlugins(path.resolve(".plugins"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    return numChanges
}

int processPlugins(Path path, boolean ignoreExceptions, boolean dryRun, Map opts) {
    int numChanges = 0
    try {
        numChanges += processEclipseCoreRuntime(path.resolve("org.eclipse.core.runtime"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    return numChanges
}

int processEclipseCoreRuntime(Path path, boolean ignoreExceptions, boolean dryRun, Map opts) {
    int numChanges
    try {
        numChanges +=  processEclipseCoreRuntimeSettings(path.resolve(".settings"), ignoreExceptions, dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    return numChanges
}

int processEclipseCoreRuntimeSettings(Path path, boolean ignoreExceptions, boolean dryRun, Map opts) {
    int numChanges = 0

    try {
        numChanges += processUIEditorsPrefs(path.resolve("org.eclipse.ui.editors.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    try {
        numChanges += processCoreResourcesPrefs(path.resolve("org.eclipse.core.resources.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    try {
        numChanges += processCoreRuntimePrefs(path.resolve("org.eclipse.core.runtime.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    try {
        numChanges += processWstXmlCorePrefs(path.resolve("org.eclipse.wst.xml.core.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    try {
        numChanges += processM2ECorePrefs(path.resolve("org.eclipse.m2e.core.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    try {
        numChanges += processJDTCorePrefs(path.resolve("org.eclipse.jdt.core.prefs"), dryRun, opts)
    } catch(Throwable t) {
        error(t.getClass().getSimpleName() + ": " + t.getMessage())
        if (!ignoreExceptions) {
            throw t
        }
    }

    return numChanges
}

int processJDTCorePrefs(Path path, boolean dryRun, Map opts) {
    info(path)

    List lines = openOrCreateFile(path, dryRun, opts)
    int numChanged = 0

    List versionProps = [ 'org.eclipse.jdt.core.compiler.codegen.targetPlatform', 'org.eclipse.jdt.core.compiler.compliance', 'org.eclipse.jdt.core.compiler.source' ]
    numChanged += multiUpdatePropertyValue(path, lines, versionProps, opts['java.compatibility.version'], opts)
    numChanged += replaceOrAddPropertyValue(path, lines, 'org.eclipse.jdt.core.formatter.tabulation.char', opts['indentationChar'], opts)

    if (!dryRun) {
        Files.write(path, lines, StandardCharsets.UTF_8)
    }

    if (isDebugEnabled()) {
        debug("Modified $path - updated $numChanged lines")
    }

    return numChanged
}

int processM2ECorePrefs(Path path, boolean dryRun, Map opts) {
    return updateProperties(path, dryRun, opts, [ 'eclipse.m2.downloadJavadoc', 'eclipse.m2.downloadSources' ])
}

int processWstXmlCorePrefs(Path path, boolean dryRun, Map opts) {
    info(path)

    List lines = openOrCreateFile(path, dryRun, opts)
    int numChanged = 0

    if (!dryRun) {
        Files.write(path, lines, StandardCharsets.UTF_8)
    }

    if (isDebugEnabled()) {
        debug("Modified $path - updated $numChanged lines")
    }

    return numChanged

    return updateProperties(path, dryRun, opts, [ 'indentationChar', 'indentationSize' ])
}

int processCoreRuntimePrefs(Path path, boolean dryRun, Map opts) {
    return updateProperties(path, dryRun, opts, [ 'line.separator' ])
}

int processCoreResourcesPrefs(Path path, boolean dryRun, Map opts) {
    return updateProperties(path, dryRun, opts, [ 'encoding' ])
}

int processUIEditorsPrefs(Path path, boolean dryRun, Map opts) {
    return updateProperties(path, dryRun, opts, [ 'spacesForTabs' ])
}

int updateProperties(Path path, boolean dryRun, Map opts, List props) {
    info(path)

    List lines = openOrCreateFile(path, dryRun, opts)
    int numChanged = 0
    props.forEach {
        numChanged += updatePropertyValue(path, lines, it, opts)
    }

    if (numChanged <= 0) {
        if (isDebugEnabled()) {
            debug("Skip $path - no changes")
        }
        return 0
    }

    if (!dryRun) {
        Files.write(path, lines, StandardCharsets.UTF_8)
    }

    if (isDebugEnabled()) {
        debug("Modified $path - updated $numChanged lines")
    }

    return numChanged
}

int updatePropertyValue(Path path, List lines, String propName, Map opts) {
    return replaceOrAddPropertyValue(path, lines, propName, opts[propName], opts)
}

// same value for ALL properties in list
int multiUpdatePropertyValue(Path path, List lines, List props, Object propValue, Map opts) {
    int numChanged = 0
    props.forEach {
        numChanged += replaceOrAddPropertyValue(path, lines, it, propValue, opts)
    }
    return numChanged
}

int replaceOrAddPropertyValue(Path path, List lines, String propName, Object propValue, Map opts) {
    for (int index = 0; index < lines.size(); index++) {
        String l = lines[index]
        l = l.trim()
        if (!l.startsWith(propName)) {
            continue
        }

        if (!opts['forced'].booleanValue()) {
            int pos = l.indexOf('=')
            String curValue = ((pos > 0) && (pos < (l.length() - 1))) ? l.substring(pos + 1).trim() : ""
            if (curValue.equals(propValue.toString())) {
                if (isDebugEnabled()) {
                    debug("$path: No change required - $l")
                }
                return 0
            }
        }

        String newLine = createLine(propName, propValue)
        info("$path: replace $l with $newLine")
        lines[index] = newLine
        return 1
    }

    // this point is reached if property not found
    String l = createLine(propName, propValue)
    lines << l
    info("$path: Added $l")
    return 1
}

List openOrCreateFile(Path path, boolean dryRun, Map opts) {
    if (Files.exists(path)) {
        // TODO make read/write charset configurable
        return Files.readAllLines(path, StandardCharsets.UTF_8)
    }

    List lines = [ createLine('eclipse.preferences.version', opts['eclipse.preferences.version']) ]
    if (dryRun) {
        info("Creating $path")
    } else {
        Files.createDirectories(path.getParent())
        // TODO make read/write charset configurable
        Files.write(Files.createFile(path), lines, StandardCharsets.UTF_8)
        info("Created $path")
    }

    return lines
}

static String createLine(String propName, Object propValue) {
    return propName + "=" + propValue
}
/* ------------------------------------------------------------------------ */

def populateDefaultOptions(opts) {
    opts['verbose'] = Level.INFO
    opts['ignore-exceptions'] = false
    opts['dry-run'] = false
    opts['forced'] = false

    /* - If NEW file created use:
     *      eclipse.preferences.version=1 (configurable)
     */
    opts['eclipse.preferences.version'] = 1

    /*
     * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.ui.editors.prefs
     *         spacesForTabs=true    [General->Editors->Text Editors->Spaces for tabs]
     */
    opts['spacesForTabs'] = true

    /* - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs
     *         encoding=UTF-8    [General->Workspace->Text file encoding]
     */
    opts['encoding'] = 'UTF-8'

    /*
     * - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.runtime.prefs
     *         line.separator=\n    [General->Workspace->Text file new line delimiter]
     */
    opts['line.separator'] = '\\n'

    /*
     *  - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.wst.xml.core.prefs
     *          indentationChar=space   [XML->Xml Files->Editor->Insert using spaces]
     *          indentationSize=4       [XML->Xml Files->Editor->Insert using spaces]
     */
    opts['indentationChar'] = 'space'
    opts['indentationSize'] = 4

    /*
     *  - .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs
     *          eclipse.m2.downloadJavadoc=true [Maven->Download Artifacts JavaDoc]
     *          eclipse.m2.downloadSources=true [Maven->Download Artifacts Sources]
     */
    opts['eclipse.m2.downloadJavadoc'] = true
    opts['eclipse.m2.downloadSources'] = true

    /*
    // .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs
     *         ...attached file... - [Java->Compiler->Errors and Warnings]
     */

    // org.eclipse.jdt.core.formatter.tabulation.char=space  [Java->Code style->Formatter->Tab policy]
    // opts['org.eclipse.jdt.core.formatter.tabulation.char'] = same as 'indentationChar'

     /* Version selection
      *
      * org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8
      * org.eclipse.jdt.core.compiler.compliance=1.8
      * org.eclipse.jdt.core.compiler.source=1.8
      */
    opts['java.compatibility.version'] = getCoreJavaVersion()
    return opts
}

//////////////////////////////////////////////////////////////////////////////

static String getCoreJavaVersion() {
    String version = System.properties['java.version']
    List comps = version.split('\\.')
    return comps[0] + '.' + comps[1]
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
