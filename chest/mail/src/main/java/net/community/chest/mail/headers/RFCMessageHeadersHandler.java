/*
 *
 */
package net.community.chest.mail.headers;

/**
 * <P>Copyright GPLv2</P>
 *
 * <PInterface used by message headers parsers to inform handlers about
 * extracted headers</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 1:52:38 PM
 */
public interface RFCMessageHeadersHandler {
    /**
     * Called to inform about the start/end of a header's data parsing
     * @param hdrName header name (excluding the terminating ':')
     * @param fStarting if TRUE then header data <U>may</U> follow (if
     * no header data then this method will be immediately re-called
     * with a FALSE value)
     * @return 0 if successful - Note: if non-successful code is returned
     * then parsing is stopped and returned code is propagated "upwards".
     */
    int handleHeaderStage (String hdrName, boolean fStarting);
    /**
     * Called to inform about a header's data each call represents a "line" of
     * data in the header's data - i.e., if header continues beyond a single
     * line, then call index will be greater than zero.
     * @param hdrName header name (excluding the terminating ':')
     * @param hdrValue header value (may be empty/null)
     * @param callIndex number of times this method has been called
     * for this header since the header "stage" has started (starts
     * at zero and grows for each call). Note: if the same header is
     * found again "down" the stream, then its "stage" will be informed
     * and the count will re-start from zero. In other words, once a new
     * header is found, the old one is forgotten.
     * @return 0 if successful - Note: if non-successful code is returned
     * then parsing is stopped and returned code is propagated "upwards".
     */
    int handleHeaderData (String hdrName, String hdrValue, int callIndex);
}
