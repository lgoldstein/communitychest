#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import signal

"""
Renames the files in a folder in reverse alphanumerical order
"""

# ----------------------------------------------------------------------------

def log(msg,stream):
    stream.write("%s\n" % msg)
    stream.flush()
    return msg

def info(msg):
    return log(msg, sys.stdout)

def error(msg):
    return log(msg, sys.stderr)

def die(msg=None,rc=1):
    """
    Cleanly exits the program with an error message
    """

    if msg:
        error(msg)

    sys.exit(rc)

# ----------------------------------------------------------------------------

def isEmpty(s):
    if (s is None) or (len(s) <= 0):
        return True
    else:
        return False

# ----------------------------------------------------------------------------

def exchangeFiles(target,srcName,dstName):
    if srcName == dstName:
        return

    dstPath = os.path.join(target, dstName)
    tmpPath = os.path.join(target, "%s.tmp" % dstName)
    os.rename(dstPath, tmpPath)

    srcPath = os.path.join(target, srcName)
    os.rename(srcPath, dstPath)
    os.rename(tmpPath, srcPath)    
    info("\t%s <=> %s" % (srcName, dstName))
    
def doRename(target):
    if os.path.isfile(target):
        error("Not a folder: %s" % target)
        return

    info("Processing %s" % target)
    contents = os.listdir(target)
    folders = []
    files = []
    for f in contents:
        path = os.path.join(target, f)
        if os.path.isfile(path):
            files.append(f)
        else:
            folders.append(path)

    files.sort()
    n = len(files)
    for i in range(int(n / 2)):
        srcName = files[i]
        dstName = files[n - i - 1]
        exchangeFiles(target, srcName, dstName)

    for f in folders:
        doRename(f)

# ////////////////////////////////////////////////////////////////////////////

def main(args):
    if len(args) <= 0:
        die("Missing target folder(s)")
    
    for target in args:
        doRename(os.path.realpath(target))

def signal_handler(signal, frame):
    die('Exit due to Control+C')

if __name__ == "__main__":
    pyVersion = sys.version_info
    if pyVersion.major != 2:
        die("Major Python version must be 2.x: %s" % str(pyVersion))
    if pyVersion.minor < 7:
        print "Warning: minor Python version %s should be at least 2.7+" % str(pyVersion)

    signal.signal(signal.SIGINT, signal_handler)
    if os.name == 'nt':
        print "Use Ctrl+Break to stop the script"
    else:
        print "Use Ctrl+C to stop the script"

    args = sys.argv
    main(args[1:])
