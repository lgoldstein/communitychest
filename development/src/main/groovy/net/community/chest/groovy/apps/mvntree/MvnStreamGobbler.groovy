/*
 * 
 */
package net.community.chest.groovy.apps.mvntree

import java.io.BufferedReader;
import java.io.Closeable
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Parses the output from the maven command in search for dependency nodes</P>
 * @author Lyor G.
 * @since Dec 7, 2010 11:30:16 AM
 */
class MvnStreamGobbler extends Thread implements Closeable {
	private BufferedReader	rdr
	private boolean			verbose
	MvnStreamGobbler (InputStream input, boolean dumpOutput) {
		rdr = new BufferedReader(new InputStreamReader(input), 4096)
		verbose = dumpOutput
	}

	MvnDependencyNode	rootNode
	private Stack<MvnDependencyNode>	nodesStack
	/*
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run ()
	{
		for (String line=rdr.readLine(); line != null; line=rdr.readLine()) {
			if (verbose) {
				println "\t $line"
			}
			
			if (rootNode == null) {
				if ((rootNode=huntForStart(line)) == null) {
					continue
				}

				nodesStack = new Stack<MvnDependencyNode>()
				nodesStack.push(rootNode)
				println "\t\t::=> " + rootNode.toString()
			} else {
				processDependencyLine(line)
			}
		}
	}


	private static final startupPattern=/^.* maven-dependency-plugin:([A-Za-z0-9\-\.]+):tree .*$/
	private MvnDependencyNode huntForStart (String line) {
		def matcher=( line =~ startupPattern )
		if ((!matcher.matches()) || (matcher.getCount() <= 0)) {
			return null
		}
				
		if (verbose) {
			def startupIndex=line.indexOf("maven-dependency-plugin")
			def endIndex=line.indexOf(' ', startupIndex)
			def compsList=line.substring(startupIndex, endIndex).split(":")
			println "\t\t::> detected plugin version: " + compsList[1]
		}

		if ((line=rdr.readLine()) == null) {
			throw new EOFException("Premature EOF while reading first node data")
		}

		if (verbose) {
			println "\t $line"
		}

		return new MvnDependencyNode(line)
	}

	private static final depenendcyNodePattern=/^\[INFO\] (.*)$/
	private void processDependencyLine (String line) {
		def matcher=( line =~ depenendcyNodePattern)
		if ((!matcher.matches()) || (matcher.getCount() <= 0)) {
			return
		}

		def startIndex=line.indexOf('-')
		if (startIndex <= 0) {
			return
		}

		def prevChar=line.charAt(startIndex - 1)
		if ((prevChar != '+') && (prevChar != '\\')) {
			return
		}

		def depthCount=line.count('|') + 1
		def stackSize=nodesStack.size()
		def node=new MvnDependencyNode(line.substring(startIndex))
		def topNode=nodesStack.peek()
		if (stackSize < depthCount) {
			nodesStack.push(node)
		} else if (stackSize > depthCount) {
			topNode = nodesStack.pop()
		}
		node.parent = topNode
		topNode.children.add(node)
		println "\t\t::=> " + node.toString()
	}


	private MvnDependencyNode popNode (MvnDependencyNode node) {
		node.parent = curNode
		curNode.children.add(node)
		if (verbose) {
			println "\t\t::=> POP $node"
		}
		return curNode.parent
	}
	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close () throws IOException	{
		if (rdr != null) {
			try	{
				rdr.close()
			}
			finally {
				rdr = null
			}
		}
	}
}
