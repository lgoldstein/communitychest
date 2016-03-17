#!/usr/bin/env groovy

/*
 * Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.logging.*
import java.security.*
import java.util.zip.*
import javax.xml.bind.DatatypeConverter

if (args == null) {
    usage()
    System.exit(1)
}

verbosity = Level.INFO

for (int index=0; index < args.length; index++) {
    String  argVal=args[index]
    if ("--help".equals(argVal)) {
        usage()
        System.exit(1)
    }

    if (argVal.charAt(0) != '-') {  // assume end of options
        if (index != (args.length - 3)) {
            dieWithUsage("Incomplete invocation arguments")
        }

        depsFile = arg2file(argVal)
        // TODO make sure none of the other arguments contain '-'
        localRepo = arg2file(args[index + 1])
        targetRoot = arg2file(args[index + 2])
        break
    } else if ("-q".equals(argVal)) {
        verbosity = Level.WARNING
    } else if ("-qq".equals(argVal)) {
        verbosity = Level.SEVERE
    } else if ("-qqq".equals(argVal)) {
        verbosity = Level.OFF
    } else if ("-v".equals(argVal)) {
        verbosity = Level.FINE
    } else if ("-vv".equals(argVal)) {
        verbosity = Level.FINER
    } else if ("-vvv".equals(argVal)) {
        verbosity = Level.ALL
    } else {
        dieWithUsage("Unknown option: $argVal")
    }
}

if ((!depsFile) || (depsFile == null)
 || (!localRepo) || (localRepo == null)
 || (!targetRoot) || (targetRoot == null)) {
    dieWithUsage("Invalid invocation arguments")
}

info("Read dependencies from $depsFile.absolutePath")
def deps=readDependencies(depsFile)
if (deps.empty) {
    info("No dependencies found")
    System.exit(0)
}

info("Create signature files: localRepo=$localRepo.absolutePath, targetRoot=$targetRoot.absolutePath")
for (def d : deps) {
    createSignatureFile(d, localRepo, targetRoot)
}

info("Done.")

//////////////////////////////////////////////////////////////////////////////

def createSignatureFile(d, File localRepo, File targetRoot) {
    String      basePath=relPath(d['groupId'].replace('.' as char, File.separatorChar), d['artifactId'], d['version'], d['artifactId'] + '-' + d['version'])
    String      filePath=basePath + '.' + d['packaging']
    Properties  signature=createJarSignature(new File(localRepo, filePath))
    writeSignature(signature, new File(targetRoot, filePath + ".signature"))
}

// TODO find a way to use same code as in the munger
Properties createJarSignature(File file) {
    debug("createJarSignature($file)")

    /*
     * NOTE: we use a ZipFile since the JarFile executes some special
     * logic for the manifest, which interferes with our signature
     * that includes it
     */
    MessageDigest   digest=MessageDigest.getInstance("SHA1")
    Properties      signature=new Properties()
    ZipFile         jarFile=new ZipFile(file)
    try {
        jarFile.entries().each{ entry ->
            String  name=entry.name
            if ((name == null) || name.empty) {
                throw new StreamCorruptedException("Null/empty entry found")
            }

            /*
             *  NOTE: in standard java signature, entries in the META-INF folder
             *  or directory entries do not participate in the signature. We sign
             *  ALL of them in order to ensure that no manipulations have been made
             *  of ANY kind - no extra empty folders, no extra files in META-INF -
             *  regardless of whether we can think of a security issue for such a
             *  manipulation
             */


            String  digestValue
            if (entry.directory) {
                digestValue = createDigest(digest, name)
            } else {
                InputStream entryData=jarFile.getInputStream(entry)
                try {
                    digestValue = updateDigest(digest, entryData)
                } finally {
                    entryData.close()
                }
            }

            Object  prev=signature.setProperty(name, digestValue)
            if (prev != null) {
                dieWithMessagee("Multiple digest entries for $name in $file.absolutePath");
            }
            trace("$name: $digestValue")
        }
    } finally {
        jarFile.close()
    }

    return signature
}

def String updateDigest(MessageDigest digest, InputStream entryData) {
    byte[]  buffer=new byte[4096]
    int     read=0
    while ((read=entryData.read(buffer)) > 0) {
        digest.update(buffer, 0, read)
    }

    return getDigestValue(digest)
}

def String createDigest(MessageDigest digest, String entryData) {
    return createDigestValue(digest, entryData.getBytes('UTF-8'))
}

def String createDigestValue(MessageDigest digest, byte... data) {
    digest.update(data)
    return getDigestValue(digest)
}

def String getDigestValue(MessageDigest digest) {
    byte[]  digestValue=digest.digest()
    return DatatypeConverter.printBase64Binary(digestValue)
}

def writeSignature(Properties signature, File signatureFile) {
    debug("writeSignature($signatureFile)")

    File    parentDir=signatureFile.parentFile
    if (!parentDir.exists()) {
        if (!parentDir.mkdirs()) {
            dieWithMessage("Failed to create $parentDir.absolutePath")
        }
    }

    signatureFile.withWriter('UTF-8') { writer ->
        signature.store(writer, "Generated by Groovy script")
    }
}

// TODO find a way to use the same code as in the munger's JarValidationUtils

String relPath(String... comps) {
     return comps.join(File.separator)
}

def arg2file(String arg) {
    if ((arg == null) || arg.empty || (arg.charAt(0) == '-')) {
        return null
    } else {
        return new File(arg)
    }
}

// TODO auto-detect if '.xml' or '.log'
// TODO use same code as in the munger
def readDependencies(File listFile) {
    def deps=[]

    listFile.eachLine {
        def d=parseDependency(it)
        if (d != null) {
            debug("$d")
            deps << d
        }
    }

    return deps
}

def parseDependency(String line) {
    line = line.trim()
    if (line.empty) {
        return null
    }

    trace("$line")

    if (!line.startsWith("[INFO]")) {
        return null
    }

    int startPos=line.indexOf(' ')
    if (startPos <= 0) {
        return null
    }

    line = line.substring(startPos + 1).trim()
    if (line.empty) {
        return null
    }

    if (line.contains(":compile") || line.contains(":runtime")) {
        String[]    comps=line.split(":")
        return [
            groupId : comps[0].trim(),
            artifactId : comps[1].trim(),
            packaging: comps[2].trim(),
            version: comps[3].trim(),
            scope: comps[4].trim()
        ]
    }

    return null
}

def dieWithUsage(String msg, int rc=1) {
    warn(msg)
    warn("")
    usage()
    System.exit(rc)
}

def dieWithMessage(String msg, int rc=1) {
    error(msg)
    System.exit(rc)
}

def usage() {
    info("Usage: generateDepenenciesSignatures [OPTIONS] <dependencies-list-file> <local-repository-root> <target-signatures-root>")
    info("")
    info("Where:")
    info("")
    info("    dependencies-list-file - fully qualified path of the '.log' dependencies list")
    info("    local-repository-root - fully qualified path of the local repository root")
    info("    target-signatures-root - fully qualified path of the root folder for creating the signature files")
    info("")
    info("OPTIONS:")
    info("")
    info("    --help - this usage message")
    info("    -q - quiet (WARNING(s) only)")
    info("    -qq - more quiet (ERROR(s) only)")
    info("    -qqq - totally quiet (no messages)")
    info("    -v - verbose")
    info("    -vv - more verbose")
    info("    -vv - a lot more verbose")
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

def debug(msg) {
    log(Level.FINE, "    $msg")
}

def trace(msg) {
    log(Level.FINER, "        $msg")
}

def log(Level level, msg) {
    if (level.intValue() < verbosity.intValue()) {
        return
    } else {
        println level.name + ": " + msg
    }
}
