/*
 *
 */
package net.community.chest.io.dom;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.Stack;

import net.community.chest.ParsableString;
import net.community.chest.Triplet;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneCommentImpl;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.input.TokensReader;
import net.community.chest.io.output.NullWriter;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Assumes a "well-formed" XML input - i.e.:</P></BR>
 * <UL>
 *         <LI>
 *         Elements that have no children are written on <U>one</U> line,
 *         along with any text value they may have
 *         </LI>
 *
 *         <LI>
 *         Elements that have other child elements are written on <U>separate</U>
 *         lines - i.e., start tag on one line, children on next line(s) and end
 *         tag on a new line
 *         </LI>
 *
 *         <LI>
 *         Processing instructions are written on <U>one</U> line.
 *         </LI>
 *
 *         <LI>
 *         Comments are either start/end on one line or start/end on separate lines
 *         </LI>
 * </UL>
 *
 * @author Lyor G.
 * @since Aug 26, 2009 9:02:27 AM
 */
public class PrettyPrintDocumentBuilder extends AbstractIODocumentBuilder {
    public PrettyPrintDocumentBuilder ()
    {
        super();
    }

    public static final <A extends Appendable> A appendTillCommentEnd (
            final Reader r, final A sb, final StringBuilder org)
        throws IOException
    {
        if (null == r)
            return sb;

        if (null == sb)
            throw new IOException("appendTillCommentEnd() no " + Appendable.class.getSimpleName() + " instance provided");

        final StringBuilder    workBuf=(null == org) ? new StringBuilder(80) : org;
        EOLStyle            eol=null;
        // limit to ~32K lines to avoid infinite loop
        for (int    lIndex=0; lIndex < Short.MAX_VALUE; lIndex++)
        {
            workBuf.setLength(0);

            eol = TokensReader.appendLine(workBuf, r);

            final int    dLen=workBuf.length();
            if (dLen > 0)
            {
                int    lPos=ParsableString.findNonEmptyDataEnd(workBuf);
                if ((lPos >= DOMUtils.XML_COMMENT_END.length())
                 && (lPos < dLen))
                {
                    boolean    isMatch=true;
                    for (int cPos=DOMUtils.XML_COMMENT_END.length()-1; isMatch && (cPos >= 0); cPos--,lPos--)
                    {
                        if (workBuf.charAt(lPos) != DOMUtils.XML_COMMENT_END.charAt(cPos))
                            isMatch = false;
                    }
                    if (isMatch)
                    {
                        final String    remData=
                            (lPos > 0) ? workBuf.substring(0, lPos) : null;
                        if ((remData != null) && (remData.length() > 0))
                            sb.append(remData);
                        return sb;
                    }
                }

                sb.append(workBuf);
            }

            if (eol != null)
                sb.append(eol.getStyleString());
            else // reached if XML comment end sequence not found till end of data
                throw new EOFException("appendTillCommentEnd() exhausted all data");
        }

        throw new StreamCorruptedException("appendTillCommentEnd() possible infinite loop");
    }

    public static final <A extends Appendable> A appendTillCommentEnd (
            final Reader r, final A sb, final int workBufSize)
        throws IOException
    {
        return appendTillCommentEnd(r, sb, (workBufSize <= 0) ? null : new StringBuilder(workBufSize));
    }

    public static final <A extends Appendable> A appendTillCommentEnd (
                    final Reader r, final A sb)
        throws IOException
    {
        return appendTillCommentEnd(r, sb, 80);
    }

    public static final <D extends Document> D parse (
            final Reader r, final boolean includeComments, final D doc)
        throws SAXException, IOException, DOMException
    {
        if (null == r)
            return doc;

        if (null == doc)
            throw new SAXException("parse(comments=" + includeComments + ") no " + Document.class.getSimpleName() + " instance provided");

        final StringBuilder    sb=new StringBuilder(80);
        EOLStyle            eol=null;
        Stack<Node>            nodes=null;
        Node                curNode=doc;
        NullWriter            nullWriter=null;
        try {
            do
            {
                sb.setLength(0);    // reset
                eol = TokensReader.appendLine(sb, r);

                final int    dLen=sb.length();
                if (dLen <= 0)
                    continue;

                final String    data=sb.toString(),
                                trim=data.trim();
                final int        tLen=(null == trim) ? 0 : trim.length();
                if (tLen <= 0)
                    continue;

                if (StringUtil.startsWith(trim, DOMUtils.XML_COMMENT_START, false, true))
                {
                    final boolean    inlineComment=
                        StringUtil.endsWith(trim, DOMUtils.XML_COMMENT_END, true, true);
                    String            cText=null;
                    if (inlineComment)
                    {
                        if (includeComments && (tLen > DOMUtils.MIN_XML_COMMENT_LEN))
                            cText = sb.substring(DOMUtils.XML_COMMENT_START.length(), tLen - DOMUtils.XML_COMMENT_END.length()).trim();
                    }
                    else
                    {
                        if (!includeComments)
                        {
                            if (null == nullWriter)
                                nullWriter = new NullWriter();
                            if (!nullWriter.isOpen())
                                nullWriter.setClosed(false);
                        }

                        final StringBuilder    workBuf=includeComments ? null : sb;
                        final Appendable    outBuf=includeComments ? sb : nullWriter;
                        appendTillCommentEnd(r, outBuf, workBuf);

                        cText =
                            (includeComments && (sb.length() > 0)) ? sb.toString().trim() : null;
                    }

                    if ((cText != null) && (cText.length() > 0))
                    {
                        final Comment    c=(null == doc)
                            ? new StandaloneCommentImpl(cText)
                            : doc.createComment(cText);
                        curNode.appendChild(c);
                    }
                }
                else if (StringUtil.startsWith(trim, DOMUtils.XML_PROCINST_START, true, true))
                {
                    final ProcessingInstruction    pi=
                        DOMUtils.parseProcessingInstructionString(doc, trim);
                    if (pi != null)
                        curNode.appendChild(pi);
                }
                else if (StringUtil.startsWith(trim, DOMUtils.XML_TAG_CLOSURE_SEQ, true, true))
                {
                    final Map.Entry<String,? extends Number>    pe=
                        DOMUtils.parseElementCloseTagName(trim);
                    final String                                ctName=
                        (null == pe) ? null : pe.getKey();
                    final Number                                lp=
                        (null == pe) ? null : pe.getValue();
                    final int                                    lastPos=
                        (null == lp) ? Integer.MIN_VALUE : lp.intValue();
                    if ((null == ctName) || (ctName.length() <= 0)
                     || (null == lp) || (lastPos < tLen))
                        throw new SAXException("parse(comments=" + includeComments + ") bad " + Element.class.getSimpleName() + " close tag: " + trim);

                    final short    nodeType=(null == curNode) ? (-1) : curNode.getNodeType();
                    if (nodeType != Node.ELEMENT_NODE)
                        throw new SAXException("parse(comments=" + includeComments + ") bad " + Element.class.getSimpleName() + " close tag=" + trim + " closing a non-element node type=" + nodeType);

                    final String    tagName=((Element) curNode).getTagName();
                    if (!ctName.equals(tagName))
                        throw new SAXException("parse(comments=" + includeComments + ") bad " + Element.class.getSimpleName() + " close tag=" + trim + " mismatch - expected=" + tagName);

                    // make sure closing a pushed node
                    if ((null == nodes) || nodes.isEmpty())
                        throw new SAXException("parse(comments=" + includeComments + ") bad " + Element.class.getSimpleName() + " close tag=" + trim + " cannot pop parent node");

                    if (null == (curNode=nodes.pop()))    // should not happen
                        throw new SAXException("parse(comments=" + includeComments + ") bad " + Element.class.getSimpleName() + " close tag=" + trim + " no parent node popped");
                }
                else
                {
                    final Triplet<? extends Element,? extends CharacterData,Boolean>    pe=
                        DOMUtils.parseElementString(doc, trim);
                    final Element                                                        elem=
                        (null == pe) ? null : pe.getV1();
                    if (null == elem)
                        throw new SAXException("parse(comments=" + includeComments + ") no " + Element.class.getSimpleName() + " data extracted from " + trim);

                    curNode.appendChild(elem);

                    final Boolean    end=pe.getV3();
                    // check if inline end
                    if ((end != null) && (!end.booleanValue()))
                    {
                        // elements that have text cannot have children
                        final Node    val=pe.getV2();
                        if (val != null)
                            throw new SAXException("parse(comments=" + includeComments + ") non-terminated element cannot have text: " + trim);

                        if (null == nodes)
                            nodes = new Stack<Node>();
                        nodes.push(curNode);
                        curNode = elem;
                    }
                }
            } while (eol != null);
        } finally {
            if (nullWriter != null) {
                nullWriter.close();
            }
        }

        if ((nodes != null) && (!nodes.isEmpty()))
            throw new StreamCorruptedException("parse(comments=" + includeComments + ") unbalanced XML elements at end of data");

        if (curNode != doc)
            throw new SAXException("parse(comments=" + includeComments + ") mismatched node instance at end of data");

        return doc;
    }
    /*
     * @see javax.xml.parsers.DocumentBuilder#newDocument()
     */
    @Override
    public Document newDocument ()
    {
        return new StandaloneDocumentImpl();
    }

    public Document parse (Reader r, boolean includeComments)
        throws SAXException, IOException, DOMException
    {
        if (null == r)
            throw new IOException("parse(comments=" + includeComments + ") no " + Reader.class.getSimpleName() + " instance provided");

        return parse(r, includeComments, newDocument());
    }
    /*
     * @see net.community.chest.io.dom.AbstractIODocumentBuilder#parse(java.io.Reader)
     */
    @Override
    public Document parse (Reader r) throws SAXException, IOException, DOMException
    {
        return parse(r, false);
    }

    public static final PrettyPrintDocumentBuilder    DEFAULT=new PrettyPrintDocumentBuilder();
}
