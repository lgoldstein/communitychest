#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import signal

"""
Scans a folder and remove well-known Windows hidden files
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

def isHiddenFile(name):
    if isEmpty(name):
        return False
    if name[0] == '.':
        return True
    if name[0] == '~':
        return True
    
    name = name.lower();
    if name.startswith("albumart") or name.startswith("tmp_cover_"):
        return True
    if name == "folder.jpg":
        return True
    if name == "desktop.ini":
        return True
    if name == "thumbs.db":
        return True
    
    return False

# ----------------------------------------------------------------------------

def doScan(target):
    if os.path.isfile(target):
        error("Not a folder: %s" % target)
        return

    info("Processing %s" % target)
    os.system(u"attrib -S -H \"%s\\*.jpg\" /S" % target)
    os.system(u"attrib -S -H \"%s\\*.png\" /S" % target)
    contents = os.listdir(target)
    for f in contents:
        path = os.path.join(target, f)
        if os.path.isfile(path):
            if isHiddenFile(f.encode("utf-8")):
                os.remove(path)
                info("Removed %s" % path)
        else:
            doScan(path)

# ////////////////////////////////////////////////////////////////////////////

def main(args):
    if len(args) <= 0:
        die("Missing target folder(s)")
    
    for target in args:
        unitarget = unicode(target)
        doScan(os.path.realpath(unitarget))

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
