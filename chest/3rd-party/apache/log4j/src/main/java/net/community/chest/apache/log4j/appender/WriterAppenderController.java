/*
 *
 */
package net.community.chest.apache.log4j.appender;

import java.io.Writer;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Exposes some of the {@link org.apache.log4j.WriterAppender}-s functionality as an interface</P>
 * @author Lyor G.
 * @since Nov 21, 2010 1:49:38 PM
 */
public interface WriterAppenderController extends AppenderSkeletonController {
    boolean getImmediateFlush ();
    void setImmediateFlush (boolean value);

    String getEncoding ();
    void setEncoding (String value);

    void setWriter (Writer writer);
}
