/*
 * 
 */
package net.community.chest.io.encode;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 8:35:04 AM
 */
public interface OutputDataEncoder extends DataOutput {
	// NOTE: "writeUTF(s)" is assumed to be equivalent to "writeString(s, "UTF-8")"
	void writeString (String s, String charsetName) throws IOException;
	void writeString (String s, Charset charset) throws IOException;
	void writeString (String s, CharsetEncoder charsetEnc) throws IOException;
}
