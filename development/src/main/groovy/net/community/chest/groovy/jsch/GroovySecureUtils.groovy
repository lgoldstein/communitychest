

package net.community.chest.groovy.jsch

import java.io.EOFException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.StreamCorruptedException
import java.util.ArrayList

import net.community.chest.io.FileUtil
import net.community.chest.io.IOCopier

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Provides SSH capabilities using <A HREF="http://www.jcraft.com/jsch">Java Secure Channel</A>
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 23, 2010 11:59:52 AM
 * @see <A HREF="http://blogs.sun.com/janp/entry/how_the_scp_protocol_works">How the SCP protocol works</A>
 */
class GroovySecureUtils {
	public static final int	SSH_DEFAULT_PORT=22
	public static final List<File> scpFrom (String host, String username, String password, String remoteFilePath, boolean isRemoteDir, String localFilePath, Closure logger) {
		return scpFrom(host, SSH_DEFAULT_PORT, username, password, remoteFilePath, isRemoteDir, localFilePath, logger)
	}

	public static final List<File> scpFrom (String host, int port, String username, String password, String remoteFilePath, boolean isRemoteDir, String localFilePath, Closure logger) {
		if (logger != null) {
			logger.call "Connecting to $host on port $port"
		}

		final String	copyOption=isRemoteDir ? "-qr" : "-f"
		def				scpContext=openScpStreams(host, port, username, password, "$copyOption $remoteFilePath")
		try {
			return scpFromStreams(scpContext[1], scpContext[2], new byte[IOCopier.DEFAULT_COPY_SIZE], new ArrayList<File>(), new File(localFilePath), logger)
		} finally {
			closeScpStreams(scpContext)
		}
	}

	public static final List<File> scpTo (String host, String username, String password, String localFilePath, String remoteFilePath, Closure logger) {
		return scpTo(host, SSH_DEFAULT_PORT, username, password, localFilePath, remoteFilePath, logger)
	}

	public static final List<File> scpTo (String host, int port, String username, String password, String localFilePath, String remoteFilePath, Closure logger) {
		return scpTo(host, port, username, password, new File(localFilePath), remoteFilePath, logger)
	}

	public static final List<File> scpTo (String host, String username, String password, File localFile, String remoteFilePath, Closure logger) {
		return scpTo(host, SSH_DEFAULT_PORT, username, password, localFile, remoteFilePath, logger)
	}

	public static final List<File> scpTo (String host, int port, String username, String password, File localFile, String remoteFilePath, Closure logger) {
		if (logger != null) {
			logger.call "Connecting to $host on port $port"
		}
	
		final String	copyOption=localFile.isDirectory() ? "-XXX???" : "-p -t"
		def				scpContext=openScpStreams(host, port, username, password, "$copyOption $remoteFilePath")
		try {
			return scpToStreams(scpContext[1], scpContext[2], new byte[IOCopier.DEFAULT_COPY_SIZE], new ArrayList<File>(), localFile, remoteFilePath, logger)
		} finally {
			closeScpStreams(scpContext)
		}
	}
	// returns [ session, input-stream, output-stream ]
	private static openScpStreams (String host, int port, String username, String password, String scpCommmandArgs) {
		final Session 	session=openSecureSession(host, port, username, password)
		final Channel channel=session.openChannel("exec")
		((ChannelExec)channel).setCommand("scp $scpCommmandArgs")

		// get I/O streams for remote scp
		OutputStream 	outStream=null
		InputStream		inpStream=null
		try {
			outStream = channel.getOutputStream()
			inpStream = channel.getInputStream()
			channel.connect()
			
			return [ session, inpStream, outStream ]
		} catch(Exception e) {
			closeScpStreams([ session, inpStream, outStream ])
			throw e
		}
	}

	private static closeScpStreams (def scpContext) {
		if (scpContext == null) {
			return null
		}

		IOException	err=null
		try	{
			FileUtil.closeAll(scpContext[1], scpContext[2])
		} catch(IOException e) {
			err = e
		}

		scpContext[0].disconnect()

		if (err != null) {
			throw err
		}
		
		return scpContext
	}

	public static Session openSecureSession (String host, String username, String password) {
		return openSecureSession(host, SSH_DEFAULT_PORT, username, password)
	}

	public static Session openSecureSession (String host, int port, String username, String password) {
		final JSch 		jsch=new JSch()
		final Session 	session=jsch.getSession(username, host, port)
		session.setUserInfo(new DefaultUserInfo(password))
		session.connect()
		return session
	}

	private static List<File> scpFromStreams (InputStream inpStream, OutputStream outStream, byte[] buf, List<File> filesList, File localFile, Closure logger) {
		sendZero(outStream, buf)

		int c=checkAck(inpStream)
		for( ; ; c=checkAck(inpStream)) {
			if (c == 'C') {
				filesList.add(scpFromFile(inpStream, outStream, buf, localFile, logger))
			} else if (c == 'D') {
				scpFromFolder(inpStream, outStream, buf, filesList, localFile, logger)
			} else if (c == (-1)) {
				break
			} else {
				throw new StreamCorruptedException("Unknown response ACK ($c) for file $localFile")
			}

			sendZero(outStream, buf)	// signal ready for next entry (if any)
		}

		return filesList
	}

	private static List<File> scpFromFolder (InputStream inpStream, OutputStream outStream, byte[] buf, List<File>	filesList, File localFile, Closure logger) {
		def cpyResponse=readCopyRespones(inpStream, buf)
		def filePerms=cpyResponse[0], fileSize=cpyResponse[1], remoteFileName=cpyResponse[2]

		return scpFromStreams(inpStream, outStream, buf, filesList, new File(localFile, remoteFileName))
	}

	private static File scpFromFile (InputStream inpStream, OutputStream outStream, byte[] buf, File localFile, Closure logger) {
		def cpyResponse=readCopyRespones(inpStream, buf)
		def filePerms=cpyResponse[0], fileSize=cpyResponse[1], remoteFileName=cpyResponse[2]
		if (filePerms != "0644") {
			throw new StreamCorruptedException("Unexpected file permissions: $filePerms")
		}

		final File	localTarget=localFile.isDirectory() ? new File(localFile, remoteFileName) : localFile
		if (logger != null) {
			logger.call("Start copying $fileSize bytes from $remoteFileName to " + localTarget.getName() + " ...")
		}

		final File	localParent=localTarget.getParentFile()
		if ((!localParent.exists()) && (!localParent.mkdirs())) {
			throw new StreamCorruptedException("Failed to create local parent: $localParent")
		}

		final OutputStream	outLocal=new FileOutputStream(localTarget)
		try {
			sendZero(outStream, buf)	// signal ready to receive file data

			final long	cpySize=IOCopier.copyStreams(inpStream, outLocal, buf, fileSize)
			if (cpySize != fileSize) {
				throw new StreamCorruptedException("Read only " + cpySize + " bytes out of expected " + fileSize + " from remote file=" + remoteFileName)
			}
		} finally {
			outLocal.close()

			if (localTarget.exists() && (!localTarget.delete())) {
				throw new StreamCorruptedException("Failed to delete corrupted local file=" + localTarget)	
			}
		}

		def	ackResponse=checkAck(inpStream)
		if (ackResponse != 0) {
			throw new StreamCorruptedException("Bad result ($ackResponse) on end of copy to $localTarget")
		}

		if (logger != null) {
			logger.call "Finished copying $fileSize bytes from $remoteFileName to " + localTarget.getName()
		}
		
		return localTarget
	}

	private static List<File> scpToStreams (InputStream inpStream, OutputStream outStream, byte[] buf, List<File> filesList, File localFile, String remoteFilePath, Closure logger) {
		int	c=checkAck(inpStream)
		if (c != 0) {
			throw new StreamCorruptedException("Bad code ($c) while waiting for remote OK on $localFile")
		}

		if (localFile.isFile()) {
			def	localSource=scpToFileStreams(inpStream, outStream, buf, localFile, remoteFilePath, logger)
			filesList.add(localSource)
		} else {
			throw new UnsupportedOperationException("scpTo from dir=$localFile N/A")
		}
		
		return filesList
	}

	private static File scpToFileStreams (InputStream inpStream, OutputStream outStream, byte[] buf, File localFile, String remoteFilePath, Closure logger) {
		// send "C0644 filesize filename", where filename should not include '/'
		final long	fileSize=localFile.length()
		if (logger != null) {
			logger.call "Start copy $fileSize bytes from $localFile..."
		}
		sendCommand(inpStream, outStream, "C0644 $fileSize " + localFile.getName() + "\n")

		final InputStream	inLocal=new FileInputStream(localFile)
		try {
			final long	cpySize=IOCopier.copyStreams(inLocal, outStream, buf, fileSize)
			if (cpySize != fileSize) {
				throw new StreamCorruptedException("Written only $cpySize bytes out of expected $fileSize from remote file=$remoteFilePath")
			}
		} finally {
			inLocal.close()
		}

		sendZero(outStream, buf)	// signal end of file copy

		final int	c=checkAck(inpStream)
		if (c != 0) {
			throw new StreamCorruptedException("Bad code ($c) while waiting for end-of-copy ack on $remoteFilePath")
		}

		if (logger != null) {
			logger.call "End copy $fileSize bytes from $localFile..."
		}

		return localFile
	}

	private static sendCommand (InputStream inpStream, OutputStream outStream, String cmd) {
		def	buf=cmd.getBytes()
		outStream.write buf
		outStream.flush()

		final int	c=checkAck(inpStream)
		if (c != 0) {
			throw new StreamCorruptedException("Bad code ($c) while waiting for remote OK command=$cmd")
		}
	}

	// returns a list consisting of [ file permissions (string),  file size (long), remote name (string) ]
	private static readCopyRespones (InputStream inpStream, byte[] buf) {
		int	readLen=readTillChar(inpStream, (char) ' ', buf)
		if (readLen <= 0) {
			throw new StreamCorruptedException("No file permissions value found");
		}

		final String filePerms=new String(buf, 0, readLen)
		if ((readLen=readTillChar(inpStream, (char) ' ', buf)) <= 0) {
			throw new StreamCorruptedException("No file size value found")
		}

		final long fileSize=Long.parseLong(new String(buf, 0, readLen))
		if ((readLen=readTillChar(inpStream, (char) 0x0a, buf)) <= 0) {
			throw new StreamCorruptedException("No file name value found")
		}
		
		final String	remoteFileName=new String(buf, 0, readLen)
		return [ filePerms, fileSize, remoteFileName ]
	}

	private static void sendZero (OutputStream outStream, byte[] workBuf) {
		workBuf[0] = 0; 
		outStream.write(workBuf, 0, 1)
		outStream.flush()
	}

	private static int readTillChar (InputStream inpStream, char ch, byte[] workBuf) {
		return readTillChar(inpStream, ch, workBuf, 0)
	}

	private static int readTillChar (InputStream inpStream, char ch, byte[] workBuf, int startIndex) {
		for (int curIndex=startIndex; curIndex < workBuf.length; curIndex++) {
			final int	val=inpStream.read()
			if (val < 0) {
				throw new EOFException("Premature EOF after " + (curIndex - startIndex) + " characters till char='$ch'")
			}
			if (val == ch) {
				return curIndex - startIndex
			}
			workBuf[curIndex] = (byte) (val & 0x00FF);
		}
		
		throw new StreamCorruptedException("Too much data after reading " + (workBuf.length - startIndex) + " characters till char='$ch'")
	}

	private static int checkAck (final InputStream inpStream) {
		final int b=inpStream.read()
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1 EOF
		if ((0 == b) || ((-1) == b)) {
			return b
		}

		if ((1 == b) || (2 == b)) {
			final StringBuilder sb=new StringBuilder(80).append("Error " + b + ": ")
			for (int	c=inpStream.read(); c != '\n'; c=inpStream.read()) {
				sb.append((char)c)
			}

			throw new IOException(sb.toString())
		}

		return b	// some character maybe
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args) {
		String	host=args[0]
		int		port=SSH_DEFAULT_PORT, portIndex=host.lastIndexOf(':')
		if (portIndex > 0) {
			port = Integer.parseInt(host.substring(portIndex + 1))
			host = host.substring(0, portIndex)
		}

		def username=args[1], password=args[2], remoteType=args[3], opType=args[4], remotePath=args[5], filesList
		def localFilePath=(args.length < 7) ? System.getProperty("user.dir") : args[6]
		if (opType == "from") {
			filesList = scpFrom(host, port, username, password, remotePath, remoteType == "dir", localFilePath, { println it })
		} else {
			filesList = scpTo(host, port, username, password, localFilePath, remotePath, { println it })
		}
		for (def localFile in filesList) {
			println "\t$localFile"
		}

	}
}
