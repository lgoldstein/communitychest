package net.community.chest.net.proto.text.pop3;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.community.chest.net.proto.text.TextProtocolNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents a POP3 protocol commands and responses</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:58:45 AM
 */
public interface POP3Accessor extends TextProtocolNetConnection {
    /**
     * Sends the USER command
     * @param username username to be used as argument to the command
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response user (String username) throws IOException;
    /**
     * Sends the PASS command
     * @param password password to be used as argument to the command
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response pass (String password) throws IOException;
    /**
     * Performs the USER+PASS handshake
     * @param username username to be used as argument to the command
     * @param password password to be used as argument to the command
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response login (String username, String password) throws IOException;
    /**
     * Issues the STAT(us) command
     * @return POP3 response - if "+OK" then also contains the reported data parameters
     * @throws IOException if network errors
     */
    POP3StatusResponse stat () throws IOException;
    /**
     * Calls the UIDL handler for ALL messages
     * @param handler UIDL handler object
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response uidl (POP3MsgUIDLHandler handler) throws IOException;
    /**
     * Calls the UIDL handler for specified message number
     * @param msgNum message sequence number whose UIDL is requested (Note: if (=0) then ALL messages are handled)
     * @param handler UIDL handler object
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response uidl (int msgNum, POP3MsgUIDLHandler handler) throws IOException;
    /**
     * Calls the message size handler for ALL messages
     * @param handler message size handler
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response list (POP3MsgSizeHandler handler) throws IOException;
    /**
     * Calls the message size handler for specified message number
     * @param msgNum message sequence number whose size is requested (Note: if (=0) then ALL messages are handled)
     * @param handler message size handler object
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response list (int msgNum, POP3MsgSizeHandler handler) throws IOException;
    /**
     * @return all currently known messages meta information (number, size, UIDL)
     * @throws IOException if network errors
     */
    POP3MessageInfoResponse loadMessages () throws IOException;
    /**
     * RETR(ieves) the specified message full data, using the handler to inform the caller
     * @param msgNum message sequence number whose data is requested
     * @param handler message data handler to be invoked for each buffer of data
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response retr (int msgNum, POP3MsgDataHandler handler) throws IOException;
    /**
     * Dumps the specified message full data to the writer object
     * @param msgNum message sequence number to be dumped
     * @param out output {@link Writer} to dump message data into (null==error)
     * @return POP3 response
     * @throws IOException if network or dumping error(s)
     */
    POP3Response dumpMsg (int msgNum, Writer out) throws IOException;
    /**
     * Dumps the specified message full data to the output stream
     * @param msgNum message sequence number to be dumped
     * @param out The {@link OutputStream} to dump message data into (null==error)
     * @return POP3 response
     * @throws IOException if network or dumping error(s)
     */
    POP3Response dumpMsg (int msgNum, OutputStream out) throws IOException;
    /**
     * Dumps the specified message full data to the output file
     * @param msgNum message sequence number to be dumped
     * @param filePath output file path to dump message data into (null/empty==error)
     * @return POP3 response
     * @throws IOException if network or dumping error(s)
     */
    POP3Response dumpMsg (int msgNum, String filePath) throws IOException;
    /**
     * Retrieves the TOP specified message lines of data, using the handler to inform the caller
     * @param msgNum message sequence number whose data is requested
     * @param linesNum number of lines beyond the envelope to be returned
     * @param handler message data handler to be invoked for each buffer of data
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response top (int msgNum, int linesNum, POP3MsgDataHandler handler) throws IOException;
    /**
     * Dumps the specified message full data to the writer object
     * @param msgNum message sequence number to be dumped
     * @param linesNum number of lines beyond the envelope to be returned
     * @param out output {@link Writer} to dump message data into (null==error)
     * @return POP3 response
     * @throws IOException if network or dumping error(s)
     */
    POP3Response dumpPartialMsg (int msgNum, int linesNum, Writer out) throws IOException;
    /**
     * Dumps the specified message full data to the output stream
     * @param msgNum message sequence number to be dumped
     * @param linesNum number of lines beyond the envelope to be returned
     * @param out The {@link OutputStream} to dump message data into (null==error)
     * @return POP3 response
     * @throws IOException if network or dumping error(s)
     */
    POP3Response dumpPartialMsg (int msgNum, int linesNum, OutputStream out) throws IOException;
    /**
     * Issues the DELE(ete) command on specified message number
     * @param msgNum message sequence number to be deleted (starting at 1)
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response dele (int msgNum) throws IOException;
    /**
     * Sends the R(e)SET command
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response rset () throws IOException;
    /**
     * Sends the NOOP(operation) command
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response noop () throws IOException;
    /**
     * Sends the QUIT command (Note: also attempts to close the connection)
     * @return POP3 response
     * @throws IOException if network errors
     */
    POP3Response quit () throws IOException;
}
