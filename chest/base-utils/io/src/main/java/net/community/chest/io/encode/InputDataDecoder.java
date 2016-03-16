/*
 *
 */
package net.community.chest.io.encode;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 8:19:41 AM
 */
public interface InputDataDecoder extends DataInput {
    // NOTE: "readUTF" is assumed to be equivalent to "readString("UTF-8")"
    String readString (String charsetName) throws IOException;
    String readString (Charset charset) throws IOException;
    String readString (CharsetDecoder charsetDec) throws IOException;
}
