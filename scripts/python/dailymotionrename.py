#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import signal

"""
Renames downloaded Dailymotion files to a shorter format
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

def getTargetName(name):
    if name == '.' or name == '..' or not name.endswith('.mp4'):
        return name
    
    comps = name.split(' ')
    episode = int(comps[4])
    if episode < 10:
        comps[4] = "0%d" % episode
    if comps[5] == '_':
        comps[5] = '-'
    elif comps[5] != '-':
        comps.insert(5, '-')
    
    lastIndex = len(comps)
    lastComp = comps[lastIndex - 1]
    if lastComp == 'Video.mp4':
        comps[lastIndex - 1] = '.mp4'

    penultimateComp = comps[lastIndex - 2]
    if penultimateComp == 'Dailymotion':
        del comps[lastIndex - 2]
        lastIndex = lastIndex - 1
    
    if comps[lastIndex - 2] == '-' and lastIndex > 7:
        del comps[lastIndex - 2]
        lastIndex = lastIndex - 1

    # if all we have is '.mp4' then use the preceding component to create the proper name
    if comps[lastIndex - 1] == '.mp4':
        comps[lastIndex - 1] = "%s%s" % (comps[lastIndex - 2], comps[lastIndex - 1])
        del comps[lastIndex - 2]
        lastIndex = lastIndex - 1

    return ' '.join(comps)
    
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
            newName = getTargetName(f)
            if newName == f:
                info("\t\tSkip %s" % f)
            else:
                newPath = os.path.join(target, newName)
                os.rename(path, newPath)
                info("\t\t%s => %s" % (f, newName))
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
