#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import signal
import random
import string

"""
Renames all files using a randomly generated name - preserves the file type
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

def renameFile(path):
    dirName = os.path.dirname(path)
    curName = os.path.basename(path)
    fileExtension = os.path.splitext(curName)[1]
    while True:
        randName = ''.join([random.choice(string.ascii_letters + string.digits) for n in xrange(12)])
        newName = "%s%s" % (randName, fileExtension)
        newPath = os.path.join(dirName, newName)
        if not os.path.isfile(newPath):
            os.rename(path, newPath)
            return newPath
    
def doRename(target):
    if os.path.isfile(target):
        error("Not a folder: %s" % target)
        return

    info("Processing %s" % target)
    files = os.listdir(target)
    folders = []
    for f in files:
        path = os.path.join(target, f)
        if os.path.isfile(path):
            newpath = renameFile(path)
            newname = os.path.basename(newpath)
            info("\t\t%s => %s" % (f, newname))
        else:
            folders.append(path)

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
