package net.community.chest.dom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.community.chest.ParsableString;
import net.community.chest.Triplet;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.impl.BaseNodeListImpl;
import net.community.chest.dom.impl.StandaloneAttrImpl;
import net.community.chest.dom.impl.StandaloneElementImpl;
import net.community.chest.dom.impl.StandaloneProcessingInstructionImpl;
import net.community.chest.dom.impl.StandaloneTextImpl;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.io.FileUtil;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.map.entries.StringPairEntry;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful DOM manipulation methods
 *
 * @author Lyor G.
 * @since Jul 19, 2007 2:53:57 PM
 */
public final class DOMUtils {
    private DOMUtils ()
    {
        // no instance
    }
    /**
     * Adds/Sets the XML {@link Element} attribute if its value is
     * non-null/empty - otherwise does nothing
     * @param elem element to add the attribute to
     * @param attrName attribute name to be used
     * @param attrValue value to be set
     * @return updated element
     */
    public static Element addNonEmptyAttribute (final Element elem, final String attrName, final String attrValue)
    {
        if ((attrValue != null) && (attrValue.length() > 0))
            elem.setAttribute(attrName, attrValue);

        return elem;
    }

    public static Element addNonEmptyAttributeObject (final Element elem, final String attrName, final Object attrValue)
    {
        return addNonEmptyAttribute(elem, attrName, (null == attrValue) ? null : attrValue.toString());
    }

    public static <E extends Enum<E>> Element addNonEmptyEnumAttributeName (final Element elem, final String attrName, final E attrValue)
    {
        return addNonEmptyAttribute(elem, attrName, (null == attrValue) ? null : attrValue.name());
    }
    /**
     * <P>Divides all the sub-elements in "sections" based on the supplied
     * {@link Element} tag name and tag attribute. Basically, all children
     * of the supplied root are iterated and all children that have the
     * specified tag name (case <U>insensitive</U>) are considered to be
     * "section root(s)" (i.e., elements that do not have the specified tag
     * name are <U>ignored</U>. Their name is extracted using the supplied tag
     * attribute value.</P>
     *
     * <P>Returns a {@link Map} whose key is the section "name" and value is
     * the XML {@link Element} that was identified as the section "root".
     * @param org original {@link Map} to be updated - if null and need to
     * add a section then a new one will be allocated. Otherwise this map
     * will be used to add sections to it.</P>
     * <B>Note:</B> it is highly <U>recommended</U> that the supplied map
     * instance be case <U>insensitive</U>. If null, then the method allocates
     * a {@link TreeMap} with case insensitive string key(s).
     * @param root root XML element - if null, nothing is added
     * @param tagName XML tag name used to identify a section element
     * @param nameAttr XML element attribute name used to identify the section
     * "name" (logical ID).</P>
     * @return updated map - may be null/empty if null/empty original map and
     * nothing was added.
     * @throws IllegalArgumentException if null/empty tag name/attribute
     * @throws IllegalStateException if a section with the same name already
     * mapped
     * @throws NoSuchElementException if a section element lacks a name
     * attribute
     */
    public static Map<String,Element> updateSubsections (final Map<String,Element>    org, final Element root, final String tagName, final String nameAttr)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException
    {
        if ((null == tagName) || (tagName.length() <= 0)
         || (null == nameAttr) || (nameAttr.length() <= 0))
            throw new IllegalArgumentException(ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "updateSubsections", tagName, nameAttr) + " incomplete parameters");

        final Collection<? extends Element>    el=extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        if ((null == el) || (el.size() <= 0))
            return org;

        Map<String,Element>    ret=org;
        for (final Element elem : el)
        {
            final String    elemName=(null == elem) ? null : elem.getTagName();
            if (!tagName.equalsIgnoreCase(elemName))
                continue;    // should not happen since we used "getElementsByTagName"

            final String    name=elem.getAttribute(nameAttr);
            if ((null == name) || (name.length() <= 0))
                throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "updateSubsections", tagName, nameAttr) + " missing name attribute in " + Element.class.getName() + ")");

            if (ret != null)
            {
                final Element    prev=ret.get(name);
                if (prev != null)
                    throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "updateSubsections", tagName, nameAttr) + " duplicate section name=" + name + ": " + toString(elem) + " and " + toString(prev));
            }
            else
                ret = new TreeMap<String,Element>(String.CASE_INSENSITIVE_ORDER);
            ret.put(name, elem);
        }

        return ret;
    }

    public static Map<String,Element> getSubsections (final Element root, final String tagName, final String nameAttr)
    {
        return updateSubsections(null, root, tagName, nameAttr);
    }

    public static Map<String,Element> updateSubsections (final Map<String,Element>    org, final Document doc, final String tagName, final String nameAttr)
    {
        return updateSubsections(org, (null == doc) ? null : doc.getDocumentElement(), tagName, nameAttr);
    }

    public static Map<String,Element> getSubsections (final Document doc, final String tagName, final String nameAttr)
    {
        return getSubsections((null == doc) ? null : doc.getDocumentElement(), tagName, nameAttr);
    }
    /**
     * Seeks for the <U>first</U> <U><B>direct</B></U> child node of the given
     * element that is report as being a {@link org.w3c.dom.Node#TEXT_NODE}
     * @param e element whose value is required (may be null, in which case
     * behavior is as if value not found)
     * @return "value" node (null if not found)
     */
    public static final Node getElementValueNode (final Element e)
    {
        final NodeList    chldrn=(null == e) ? null : e.getChildNodes();
        final int        numChldrn=(null == chldrn) ? 0 : chldrn.getLength();
        for (int    cIndex=0; cIndex < numChldrn; cIndex++)
        {
            final Node    chld=chldrn.item(cIndex);
            final short    chldType=(null == chld) ? (short) (-1) : chld.getNodeType();
            if ((Node.TEXT_NODE == chldType) || (Node.CDATA_SECTION_NODE == chldType))
                return chld;
        }

        return null;
    }
    /**
     * @param val Original {@link String} value
     * @return Trimmed value after replacing all characters in {@link ParsableString#WHITESPACE_CHARS}
     * with spaces
     * @see ParsableString#WHITESPACE_CHARS
     * @see String#trim()
     */
    public static final String trimElementStringValue (final String val)
    {
        final int        vLen=(null == val) ? 0 : val.length();
        if (vLen <= 0)
            return val;

        String    ret=val;
        for (int cIndex=0; cIndex < ParsableString.WHITESPACE_CHARS.length(); cIndex++)
        {
            final char    ech=ParsableString.WHITESPACE_CHARS.charAt(cIndex);
            ret = ret.replace(ech, ' ');
        }

        return ret.trim();
    }
    /**
     * @param e element whose value is required
     * @return <U>trimmed</U> value string
     * @see #getElementValueNode(Element)
     * @see #trimElementStringValue(String)
     */
    public static final String getElementStringValue (final Element e)
    {
        final Node        nv=getElementValueNode(e);
        final String    val=(null == nv) ? null : nv.getNodeValue();
        return trimElementStringValue(val);
    }
    /**
     * Creates an element with the specified name and value
     * @param doc document to be used to create the {@link Element} and
     * its associated text node - may NOT be null
     * @param eName element name - may NOT be <I>null</I>/empty
     * @param value assigned value - may be <I>null</I>/empty
     * @return created element
     * @throws IllegalArgumentException if bad/illegal element/document
     * @throws DOMException if internal error
     */
    public static final Element createElementValue (final Document doc, final String eName, final String value)
    {
        if ((null == doc) || (null == eName) || (eName.length() <= 0))
            throw new IllegalArgumentException("No document/element name");

        final Element    e=doc.createElement(eName);
        final Node        vNode=doc.createTextNode((null == value) ? "" : value);
        e.appendChild(vNode);

        return e;
    }
    /**
     * Creates an element with the specified name and value - provided the
     * value is not <I>null</I>/empty
     * @param doc document to be used to create the {@link Element} and
     * its associated text node - may NOT be null
     * @param eName element name - may NOT be <I>null</I>/empty
     * @param value assigned value - may be <I>null</I>/empty
     * @return created element or <I>null</I> if <I>null</I>/empty value
     * @throws IllegalArgumentException if bad/illegal element/document
     * @throws DOMException if internal error
     */
    public static final Element createOptionalElement (final Document doc, final String eName, final String value)
    {
        if ((null == doc) || (null == eName) || (eName.length() <= 0))
            throw new IllegalArgumentException("No document/element name");

        if ((null == value) || (value.length() <= 0))
            return null;

        return createElementValue(doc, eName, value);
    }
    /**
     * Creates an element with the specified name and value and appends it as
     * child of specified root - provided the value is not <I>null</I>/empty
     * @param doc document to be used to create the {@link Element} and
     * its associated text node - may NOT be null
     * @param eParent - parent under which created element (if any) should
     * be appended
     * @param eName element name - may NOT be <I>null</I>/empty
     * @param value assigned value - may be <I>null</I>/empty
     * @return created/appended element or <I>null</I> if <I>null</I>/empty value
     * @throws IllegalArgumentException if bad/illegal element/document
     * @throws DOMException if internal error
     */
    public static final Element appendOptionalElement (final Document doc, final Element eParent, final String eName, final String value)
    {
        if (null == eParent)
            throw new IllegalArgumentException("No parent element");

        final Element    e=createOptionalElement(doc, eName, value);
        if (e != null)
            eParent.appendChild(e);

        return e;
    }
    // delimiters definitions
    public static final char    XML_ELEM_START_DELIM='<',
                                XML_ELEM_CLOSURE_DELIM='/',
                                XML_ATTR_VALUE_SEP='=',
                                XML_ELEM_END_DELIM='>';
    public static final String    XML_TAG_CLOSURE_SEQ="</",
                                XML_TAG_INLINE_SEQ="/>";
    // comment start/end string(s)
    public static final String    XML_COMMENT_START="<!--",
                                XML_COMMENT_END="-->";
    public static final int MIN_XML_COMMENT_LEN=
        XML_COMMENT_START.length() + 1 /* space */ + XML_COMMENT_END.length();
    // character(s) used to delimit a value escape sequence

    public static final String    XML_CDATA_START="<![CDATA[",
                                XML_CDATA_END="]]>";
    public static final char    VALATTR_ESCAPE_SEQ_SDELIM='&',
                                VALATTR_ESCAPE_SEQ_EDELIM=';';
    public static final String    VALATTR_AMP_ESCAPE_SEQ="&amp;",
                                VALATTR_QUOT_ESCAPE_SEQ="&quot;",
                                VALATTR_APOS_ESCAPE_SEQ="&apos;",
                                VALATTR_LT_ESCAPE_SEQ="&lt;",
                                VALATTR_GT_ESCAPE_SEQ="&gt;";
    /**
     * <P>Applies the attribute value escape rules (as defined by the "Essential
     * XML Quick Reference" book by Aaron Skonnard &amp; Martin Gudgin, 3rd
     * printing, Oct. 2002):</P></BR>
     * <UL>
     *         <LI>
     *         The less-than character (&lt;) cannot appear inside an element
     *         text or attribute value because it is interpreted as the start of
     *         an element.
     *         </LI>
     *
     *         <LI>
     *         The ampersand character (&amp;) cannot appear inside an element
     *         text or attribute value because it is interpreted as the start of
     *         an entity reference.
     *         </LI>
     *
     *      <LI>
     *      The apostrophe (') and quote (&quot;) characters <U>may</U> also
     *      need to be encoded depending on the delimiter used for the
     *      attribute value - i.e., the quote need be encoded if apostrophe is
     *      used as delimiter or vice versa.
     *      </LI>
     *
     *      <LI>
     *      The greater-than (&gt;) character seldom needs escaping but many
     *      implementations prefer to encode it for consistency with the
     *      less-than character (&lt;).
     *      </LI>
     * </UL>
     * @param ch Character value to be checked if requires escaping
     * @param quoteChar The current character used to quote the attribute
     * value - quote (&quot;) or apostrophe (').
     * @param gtEscape TRUE=escape the greater-than (&gt;) character as well
     * @return The escape sequence to be used - <code>null</code> if no
     * escaping is required
     */
    public static final String getEscapedCharValue (
            final char ch, final char quoteChar, final boolean gtEscape)
    {
        switch(ch)
        {
            case '&'    :
                return VALATTR_AMP_ESCAPE_SEQ;
            case '<'    :
                return VALATTR_LT_ESCAPE_SEQ;
            case '>'    :
                return gtEscape ? VALATTR_GT_ESCAPE_SEQ : null;
            case '\''    :
                return (quoteChar == ch) ? VALATTR_APOS_ESCAPE_SEQ : null;
            case '"'    :
                return (quoteChar == ch) ? VALATTR_QUOT_ESCAPE_SEQ : null;
            default        :
                return null;
        }
    }
    /**
     * Reverses the effects of {@link #getEscapedCharValue(char, char, boolean)}
     * @param org Suspected escape sequence - <B>Note:</B> escape sequence
     * matching is executed case <U>insensitive</U>.
     * @return Replacement character - '\0' if no replacement required
     */
    public static final char getUnscapedCharValue (final CharSequence org)
    {
        final String    s=
            ((null == org) || (org.length() <= 0)) ? null : org.toString();
        final int        sLen=(null == s) ? 0 : s.length();
        if (sLen <= 2)    // min. escape sequence is "&x;"
            return '\0';

        // NOTE: comparison order is according to expected likelihood
        if (VALATTR_AMP_ESCAPE_SEQ.equalsIgnoreCase(s))
            return '&';
        else if (VALATTR_QUOT_ESCAPE_SEQ.equalsIgnoreCase(s))
            return '"';
        else if (VALATTR_LT_ESCAPE_SEQ.equalsIgnoreCase(s))
            return '<';
        else if (VALATTR_APOS_ESCAPE_SEQ.equalsIgnoreCase(s))
            return '\'';
        else if (VALATTR_GT_ESCAPE_SEQ.equalsIgnoreCase(s))
            return '>';

        return '\0';    // no match found
    }
    /**
     * Escapes a text value according to the XML rules
     * @param s Original text value
     * @param quoteChar The current character used to quote the attribute
     * value - quote (&quot;) or apostrophe (').
     * @param gtEscape TRUE=escape the greater-than (&gt;) character as well
     * @return Escaped text value - same as input if nothing escaped
     * @see #getEscapedCharValue(char, char, boolean)
     */
    public static final CharSequence escapeTextValue (
            final CharSequence s, final char quoteChar, final boolean gtEscape)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return s;

        StringBuilder    sb=null;
        int                curPos=0, lastPos=0;
        for ( ; curPos < sLen; curPos++)
        {
            final char            ch=s.charAt(curPos);
            final CharSequence    rep=getEscapedCharValue(ch, quoteChar, gtEscape);
            final int            repLen=(null == rep) ? 0 : rep.length();
            if (repLen <= 0)
                continue;

            if (null == sb)
                sb = new StringBuilder(sLen + repLen + 16 /* just in case */);
            if (curPos > lastPos)
            {
                final CharSequence    cs=s.subSequence(lastPos, curPos);
                sb.append(cs);
            }
            sb.append(rep);
            lastPos = curPos + 1;
        }

        // check if needed to replace anything
        if (null == sb)
            return s;

        // check if any remainder(s)
        if (lastPos < sLen)
        {
            final CharSequence    cs=s.subSequence(lastPos, sLen);
            sb.append(cs);
        }

        return sb.toString();
    }
    /**
     * Reverses the effect(s) of the (@link {@link #escapeTextValue(CharSequence, char, boolean)}
     * method
     * @param s Original text value
     * @return Translated value - same as input if nothing to translate.
     * <B>Note:</B> if any unknown escape sequence is encountered then it is
     * returned as-is. Same goes for un-escaped characters (e.g., &amp;, &lt;,
     * &gt;, &quot;)
     */
    public static final CharSequence unescapeTextValue (final CharSequence s)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 2)    // min. escape sequence is "&x;"
            return s;

        StringBuilder    sb=null;
        int                lastPos=0, curPos=0;
        for ( ; curPos < sLen; curPos++)
        {
            final char    ch=s.charAt(curPos);
            if (ch != VALATTR_ESCAPE_SEQ_SDELIM)
                continue;

            curPos++;    // skip delimiter
            for (int    eStart=curPos-1; curPos < sLen; curPos++)
            {
                final char    tc=s.charAt(curPos);
                // if start of another escape sequence then assume this one is a bust
                if (VALATTR_ESCAPE_SEQ_SDELIM == tc)
                {
                    // compensate for the automatic increment of the main loop
                    curPos--;
                    break;
                }
                if (tc != VALATTR_ESCAPE_SEQ_EDELIM)
                    continue;

                curPos++;    // skip end delimiter
                final CharSequence    ec=s.subSequence(eStart, curPos);
                final char            rc=getUnscapedCharValue(ec);
                if (rc != '\0')
                {
                    if (null == sb)
                        sb = new StringBuilder(sLen /* actually we could allocate less */);

                    if (eStart > lastPos)
                    {
                        final CharSequence    cs=s.subSequence(lastPos, eStart);
                        sb.append(cs);
                    }

                    sb.append(rc);
                    lastPos = curPos;
                }

                break;
            }
        }

        // check if needed to replace anything
        if (null == sb)
            return s;

        // check if any remainder(s)
        if (lastPos < sLen)
        {
            final CharSequence    cs=s.subSequence(lastPos, sLen);
            sb.append(cs);
        }

        return sb.toString();
    }
    // returns a value >= dataLen if all characters are white space
    public static final int findElementStartPosition (
            final CharSequence elemData, final int startPos, final int dataLen)
        throws DOMException
    {
        final int    dLen=startPos + dataLen;
        for (int    curPos=startPos    ; curPos < dLen; curPos++)
        {
            final char    ch=elemData.charAt(curPos);
            if (XML_ELEM_START_DELIM == ch)
                return curPos;
            if (ParsableString.isEmptyChar(ch))
                continue;    // ignore white space

            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "findElementStartPosition", elemData) + " bad character (" + String.valueOf(ch) + ") while looking for start delimiter");
        }

        // this point is reached if all characters were white space
        return dLen;
    }
    // processing instruction start/end string(s)
    public static final String    XML_PROCINST_START="<?",
                                XML_PROCINST_END="?>";
    public static final ProcessingInstruction parseProcessingInstructionString (
            final Document doc, final CharSequence cs, final int startPos, final int len)
    {
        final int    maxPos=startPos + len;
        int            curPos=findElementStartPosition(cs, startPos, len);
        // check if found XML start delimiter
        if (curPos >= maxPos)
            return null;

        {
            curPos++;
            final char    ch=(curPos < maxPos) ? cs.charAt(curPos) : '\0';
            if (ch != '?')
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseProcessingInstructionString", cs) + " bad/missing character (" + String.valueOf(ch) + ") while checking start delimiter");
        }

        // find tag name - defined as 1st sequence after the '?'
        int    nStart=(curPos+1);    // skip '?'
        for (curPos=nStart ; curPos < maxPos; curPos++)
        {
            final char    ch=cs.charAt(curPos);
            if (ParsableString.isEmptyChar(ch) || ('?' == ch))
                break;
        }

        final int    nLen=(curPos - nStart);
        if (nLen <= 0)
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseProcessingInstructionString", cs) + " bad/missing tag name");

        // find end of instruction
        final String    tagName=cs.subSequence(nStart, curPos).toString();
        for (nStart=curPos; curPos < maxPos; curPos++)
        {
            final char    ch=cs.charAt(curPos);
            if ('?' == ch)
                break;
        }

        final int        dLen=(curPos - nStart);
        final String    tagData=    // OK if no data
            (dLen <= 0) ? "" : cs.subSequence(nStart, curPos).toString().trim();
        // check ending sequence
        final char[]    ls={
                (curPos < maxPos) ? cs.charAt(curPos) : '\0',
                (curPos < (maxPos-1)) ? cs.charAt(curPos + 1) : '\0'
            };
        for (int    lIndex=0; lIndex < ls.length; lIndex++)
        {
            final char    l1=ls[lIndex], e1=XML_PROCINST_END.charAt(lIndex);
            if (l1 != e1)
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseProcessingInstructionString", cs) + " bad/missing end sequence delimiter(s)");
        }

        if (doc != null)
            return doc.createProcessingInstruction(tagName, tagData);
        else
            return new StandaloneProcessingInstructionImpl(tagName, tagData);
    }

    public static final ProcessingInstruction parseProcessingInstructionString (
                final Document doc, final CharSequence cs)
    {
        return parseProcessingInstructionString(doc, cs, 0, (null == cs) ? 0 : cs.length());
    }

    public static final ProcessingInstruction parseProcessingInstructionString (
            final CharSequence cs, final int startPos, final int len)
    {
        return parseProcessingInstructionString(null, cs, startPos, len);
    }

    public static final ProcessingInstruction parseProcessingInstructionString (final CharSequence cs)
    {
        return parseProcessingInstructionString(cs, 0, (null == cs) ? 0 : cs.length());
    }
    /**
     * Parses the XML element string and extracts the tag name. The code
     * assumes that the provided start position is either at the XML element
     * start delimiter or at some position before it - provided all characters
     * up to the delimiter are "white space"
     * @param elemData The element data {@link CharSequence}
     * @param startPos Start position to look for the tag name
     * @param dataLen Maximum available characters for parsing (starting at the
     * specified position)
     * @return Parsing result represented as a {@link java.util.Map.Entry} whose key=the
     * extracted tag name, value=the position of the 1st character after the
     * tag name (i.e., the last parsed position). May be null if all data is
     * white space.
     * @throws DOMException If invalid XML element tag name sequence (e.g.,
     * bad XML delimiter, zero length tag, etc.)
     */
    public static final Map.Entry<String,Integer> parseElementOpenTagName (
            final CharSequence elemData, final int startPos, final int dataLen)
        throws DOMException
    {
        final int    dLen=startPos + dataLen;
        int    curPos=findElementStartPosition(elemData, startPos, dLen);
        // check if found XML start delimiter
        if (curPos >= dLen)
            return null;

        int    lastPos=curPos+1;
        for (curPos++ /* skip start delimiter */; curPos < dLen; curPos++)
        {
            final char    ch=elemData.charAt(curPos);
            // first white space of '/' or '>' character marks end of tag name
            if ((XML_ELEM_END_DELIM == ch)
             || (XML_ELEM_CLOSURE_DELIM == ch)
             || ParsableString.isEmptyChar(ch))
                break;
        }

        final int            seqLen=(curPos - lastPos);
        final CharSequence    tagSeq=
            (seqLen <= 0) ? null : elemData.subSequence(lastPos, curPos);
        final int            tagLen=(null == tagSeq) ? 0 : tagSeq.length();
        if (tagLen <= 0)
            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementOpenTagName", elemData) + " zero length element tag name");

        return new MapEntryImpl<String,Integer>(tagSeq.toString(), Integer.valueOf(curPos));
    }
    /**
     * Extracts the closing element tag name. The code assumes that the
     * provided start position is either at the XML element closing tag
     * or at some position before it - provided all characters up to the
     * delimiter are "white space".
     * @param elemData The element data {@link CharSequence}
     * @param startPos Start position to look for the tag name
     * @param dataLen Maximum available characters for parsing (starting at the
     * specified position)
     * @return Parsing result represented as a {@link java.util.Map.Entry} whose key=the
     * extracted tag name, value=the position of the 1st character after the
     * tag name (i.e., the last parsed position). May be null if all data is
     * white space.
     * @throws DOMException If invalid XML element tag name sequence (e.g.,
     * bad XML delimiter, zero length tag, not a closing tag, etc.)
     */
    public static final Map.Entry<String,Integer> parseElementCloseTagName (
            final CharSequence elemData, final int startPos, final int dataLen)
        throws DOMException
    {
        final int    dLen=startPos + dataLen;
        int    curPos=findElementStartPosition(elemData, startPos, dLen);
        // check if found XML start delimiter
        if (curPos >= dLen)
            return null;

        if ((curPos == (dLen-1)) || (elemData.charAt(curPos+1) != XML_ELEM_CLOSURE_DELIM))
            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementCloseTagName", elemData) + " missing tag end signal char");

        curPos += 2;    // skip "</"
        for (int    lastPos=curPos; curPos < dLen; curPos++)
        {
            char    ch=elemData.charAt(curPos);
            if ((ch == XML_ELEM_END_DELIM) || ParsableString.isEmptyChar(ch))
            {
                final CharSequence    n=
                    (lastPos >= curPos) ? null : elemData.subSequence(lastPos, curPos);
                if ((null == n) || (n.length() <= 0))
                    throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementCloseTagName", elemData) + " missing closing tag name");

                // if stopped due to white space make sure all remaining characters are white - space
                if (ch != XML_ELEM_END_DELIM)
                {
                    for (curPos++; curPos < dLen; curPos++)
                    {
                        if ((ch=elemData.charAt(curPos)) == XML_ELEM_END_DELIM)
                            break;

                        if (!ParsableString.isEmptyChar(ch))
                            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementCloseTagName", elemData) + " invalid closing tag characters after name: " + String.valueOf(ch));
                    }

                    // make sure stopped because we have found the end delimiter
                    if (ch != XML_ELEM_END_DELIM)
                        break;    // this will cause an appropriate exception to be thrown
                }

                return new MapEntryImpl<String,Integer>(n.toString(), Integer.valueOf(curPos+1));
            }

            if (XML_ELEM_CLOSURE_DELIM == ch)
                throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementCloseTagName", elemData) + " invalid closing tag character: " + String.valueOf(ch));
        }

        throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementCloseTagName", elemData) + " missing tag end delimiter");
    }

    public static final Map.Entry<String,Integer> parseElementCloseTagName (
            final CharSequence elemData)
        throws DOMException
    {
        return parseElementCloseTagName(elemData, 0, (null == elemData) ? 0 : elemData.length());
    }
    /**
     * <P>Parses a <U>single</U> XML element string (closing or not does not
     * matter), and makes it look like an {@link Element} object with some
     * limited functionality, but good enough for API(s) that use the returned
     * element as read-only input for their workings (e.g., GUI resources)</P>
     * @param owner {@link Document} owner - if not null then it is used to
     * create the {@link Element}/{@link Attr}/{@link Text} nodes. Otherwise
     * standalone implementations are used
     * @param elemData {@link String} containing XML formatted element data
     * (may NOT be null/empty if non-null owner specified). <B>Note:</B>
     * first non-whitespace character in the data MUST be the starting
     * delimiter "&lt;", though the parser will stop when closing delimiter
     * "&gt;" is found regardless of whether it is the last one or not.
     * @param startPos start position (inclusive) to parse
     * @param dataLen max. characters available for parsing
     * @return Parsed result as a {@link Triplet} whose 1st value is the
     * {@link Element}, 2nd value=the attached {@link Text} (null if no
     * text associated) and 3rd value a {@link Boolean} indicating whether
     * the element was terminated. <B>Note:</B> returned value may be null
     * if no owner specified and null/empty string data to begin with
     * @throws DOMException if cannot parse the string data
     */
    public static final Triplet<Element,Text,Boolean> parseElementString (
            final Document owner, final CharSequence elemData, final int startPos, final int dataLen)
        throws DOMException
    {
        // extract tag name
        final int                        dLen=startPos + dataLen;
        final Map.Entry<String,Integer>    te=parseElementOpenTagName(elemData, startPos, dLen);
        if (null == te)
        {
            if (owner != null)
                throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " no element tag name extracted");

            return null;    // OK if no owner
        }

        final Element    elem=(null == owner)
            ? new StandaloneElementImpl(te.getKey())
            : owner.createElement(te.getKey())
            ;
        Text textValue=null;
        // extract attributes and/or text value (if any)
        for (int    curPos=te.getValue().intValue() ; curPos < dLen; curPos++)
        {
            final char    ch=elemData.charAt(curPos);
            switch(ch)
            {
                case XML_ELEM_CLOSURE_DELIM    :
                    // make sure character immediately following the closure is the '>'
                    if ((curPos >= (dLen-1)) || (elemData.charAt(curPos+1) != XML_ELEM_END_DELIM))
                        throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " invalid XML element closure sequence");
                    return new Triplet<Element,Text,Boolean>(elem, textValue, Boolean.TRUE);

                    // this point is reached if element not terminated in-line but rather may have a value text
                case XML_ELEM_END_DELIM    :
                    {
                        for (curPos++ /* skip element end delimiter */; curPos < dLen; curPos++)
                        {
                            final char    tc=elemData.charAt(curPos);
                            if (!ParsableString.isEmptyChar(tc))
                                break;
                        }

                        if (curPos >= dLen)    // all whitespace till end
                            return new Triplet<Element,Text,Boolean>(elem, textValue, Boolean.FALSE);

                        for (int    textPos=curPos; curPos < dLen; curPos++)
                        {
                            final char    tc=elemData.charAt(curPos);
                            if (tc != XML_ELEM_START_DELIM)    // look for end-tag
                                continue;

                            if (textPos < curPos)
                            {
                                final CharSequence    tv=
                                    elemData.subSequence(textPos, curPos),
                                                    txv=tv.toString().trim(),
                                                    efv=unescapeTextValue(txv);
                                if ((efv != null) && (efv.length() > 0))
                                {
                                    textValue = (null == owner)
                                        ? new StandaloneTextImpl(efv.toString())
                                        : owner.createTextNode(efv.toString())
                                        ;
                                    elem.appendChild(textValue);
                                }
                            }

                            break;
                        }

                        // at this point curPos should point to the end-tag 1st character
                        final Map.Entry<String,Integer>    ct=parseElementCloseTagName(elemData, curPos, dLen);
                        if (null == ct)
                            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " missing XML element close tag");

                        final String    cn=ct.getKey(), tn=elem.getTagName();
                        // NOTE: we compare case sensitive
                        if (StringUtil.compareDataStrings(cn, tn, true) != 0)
                            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " mismatched XML element close tag: got=" + cn + "/expected=" + tn);
                    }

                    return new Triplet<Element,Text,Boolean>(elem, textValue, Boolean.TRUE);

                case ' '    :
                case '\t'    :
                    // ignore white space
                    break;

                default        :    // assume this is the start of an attribute specification
                    {
                        String    attrName=null;
                        for (int    lastPos=curPos; curPos < dLen; curPos++)
                        {
                            if (elemData.charAt(curPos) != XML_ATTR_VALUE_SEP)
                                continue;

                            final int            seqLen=(curPos - lastPos);
                            final CharSequence    tagSeq=(seqLen <= 0) ? null : elemData.subSequence(lastPos, curPos);
                            final int            tagLen=(null == tagSeq) ? 0 : tagSeq.length();

                            attrName = (tagLen <= 0) ? null : tagSeq.toString();
                            break;
                        }

                        // make sure extracted attribute name
                        if ((null == attrName) || (attrName.length() <= 0))
                            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " invalid element attribute name");

                        for (curPos++ /* skip value separator */; curPos < dLen; curPos++)
                        {
                            final char    tch=elemData.charAt(curPos);
                            if (!ParsableString.isEmptyChar(tch))
                                break;
                        }

                        final char    valDelim=
                            (curPos >= dLen) ? '\0' : elemData.charAt(curPos) ;
                        if ((valDelim != '\'') && (valDelim != '"'))    // make sure have valid value delimiter
                            throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " missing attribute=" + attrName + " value start delimiter");

                        curPos++;    // skip delimiter

                        Attr    a=null;
                        boolean    okIfNoAttr=false;
                        for (int    lastPos=curPos; curPos < dLen; curPos++)
                        {
                            if (elemData.charAt(curPos) != valDelim)
                                continue;

                            final int            seqLen=(curPos - lastPos);
                            final CharSequence    tagSeq=
                                (seqLen <= 0) ? null : elemData.subSequence(lastPos, curPos),
                                                attrVal=unescapeTextValue(tagSeq);

                            if ((attrVal != null) && (attrVal.length() > 0))
                            {
                                a = (null == owner)
                                    ? new StandaloneAttrImpl(elem, attrName)
                                    : owner.createAttribute(attrName)
                                    ;
                                if (a != null)
                                    a.setValue(attrVal.toString());
                            }
                            else    // Ignore null/empty values
                                okIfNoAttr = true;
                            break;
                        }

                        // make sure extracted an attribute
                        if (null == a)
                        {
                            if (!okIfNoAttr)
                                throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " missing attribute=" + attrName + " value end delimiter");
                        }
                        else
                            elem.setAttributeNode(a);
                    }
            }    // end of switch
        }    // end of loop on element data

        // this point is reached if exhausted all data before end delimiter found
        throw new DOMException(DOMException.SYNTAX_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "parseElementString", elemData) + " missing element end delimiter");
    }

    public static final Triplet<Element,Text,Boolean> parseElementString (
            final Document owner, final CharSequence elemData)
        throws DOMException
    {
        return parseElementString(owner, elemData, 0, (null == elemData) ? 0 : elemData.length());
    }

    public static final Triplet<Element,Text,Boolean> parseElementString (
            final CharSequence elemData, final int startPos, final int dLen)
        throws DOMException
    {
        return parseElementString(null, elemData, startPos, dLen);
    }

    public static final Triplet<Element,Text,Boolean> parseElementString (final CharSequence elemData)
        throws DOMException
    {
        return parseElementString(elemData, 0, (null == elemData) ? 0 : elemData.length());
    }

    public static final Collection<CharSequence> extractElementsList (final CharSequence elemData) throws DOMException
    {
        final int                    dLen=(null == elemData) ? 0 : elemData.length();
        Collection<CharSequence>    subElems=null;
        for (int curPos=0; curPos < dLen; curPos++)
        {
            for ( ; curPos < dLen; curPos++)
            {
                final char    ch=elemData.charAt(curPos);
                if (XML_ELEM_START_DELIM == ch)
                    break;

                if (ParsableString.isEmptyChar(ch))
                    continue;    // ignore white space

                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "extractElementsList", elemData) + " bad character (" + String.valueOf(ch) + ") while looking for start delimiter");
            }

            if (curPos >= dLen)
                break;    // OK if reached end without finding any start delimiter

            int    startPos=curPos;
            for (curPos++ /* skip delimiter */; curPos < dLen; curPos++)
            {
                final char    ch=elemData.charAt(curPos);
                if (XML_ELEM_END_DELIM == ch)
                    break;
            }

            if (curPos >= dLen)    // make sure found end delimiter
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, ClassUtil.getArgumentsExceptionLocation(DOMUtils.class, "extractElementsList", elemData) + " missing end delimiter");

            final CharSequence    seqElem=elemData.subSequence(startPos, curPos + 1);
            if (null == subElems)
                subElems = new LinkedList<CharSequence>();
            subElems.add(seqElem);
        }

        return subElems;
    }

    public static final NodeList parseElementsList (final CharSequence elemData) throws DOMException
    {
        final Collection<? extends CharSequence>    subElems=extractElementsList(elemData);
        final int                                    numElems=(null == subElems) ? 0 : subElems.size();
        if (numElems <= 0)
            return null;

        final BaseNodeListImpl    nodes=(numElems <= 0) ? null : new BaseNodeListImpl(numElems);
        for (final CharSequence seqElem : subElems)
        {
            final Triplet<? extends Element,?,?>    pe=
                parseElementString(seqElem);
            final Element                            elem=
                (null == pe) ? null : pe.getV1();
            if (elem != null)    // should not be otherwise
                nodes.add(elem);
        }

        return nodes;
    }

    public static final List<Attr> getNodeAttributesList (final NamedNodeMap attrs)
    {
        final int            numAttrs=(null == attrs) /* OK */ ? 0 : attrs.getLength();
        final List<Attr>    aList=(numAttrs <= 0) ? null : new ArrayList<Attr>(numAttrs);
        for (int    aIndex=0; aIndex < numAttrs; aIndex++)
        {
            final Node    n=attrs.item(aIndex);
            if ((null == n) || (n.getNodeType() != Node.ATTRIBUTE_NODE))
                continue;

            aList.add((Attr) n);
        }

        return aList;
    }

    public static final List<Attr> getNodeAttributesList (final Node n)
    {
        return (null == n) /* OK */ ? null : getNodeAttributesList(n.getAttributes());
    }

    // case insensitive Map of non-empty attributes - throws exception if same attribute re-specified
    public static final Map<String,String> updateNodeAttributes (final Map<String,String> org, final Node n, final boolean ignoreDuplicates) throws DOMException
    {
        final Collection<? extends Attr>    aList=getNodeAttributesList(n);
        if ((null == aList) || (aList.size() <= 0))
            return org;

        Map<String,String>    aMap=org;
        for (final Attr a : aList)
        {
            final String    aName=(null == a) /* should not happen */ ? null : a.getName(),
                            aValue=(null == a) /* should not happen */ ? null : a.getValue();
            if ((null == aName) || (aName.length() <= 0)    // should not happen
             || (null == aValue) || (aValue.length() <= 0))    // ignore empty value
                continue;

            if (aMap != null)
            {
                final String    prev=aMap.get(aName);
                if (prev != null)
                {
                    if (ignoreDuplicates || prev.equals(aValue))
                        continue;    // ignore if same value (not nice, but...)

                    final String    nn=(n instanceof Element) ? ((Element) n).getTagName() : n.getNodeName();
                    throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, "updateNodeAttributes(" + nn + ") attribute '" + aName + "' value re-specified: prev=" + prev + "/new=" + aValue);
                }
            }
            else
                aMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

            aMap.put(aName, aValue);
        }

        return aMap;
    }

    public static final Map<String,String> getNodeAttributes (final Node n, final boolean ignoreDuplicates) throws DOMException
    {
        return updateNodeAttributes(null, n, ignoreDuplicates);
    }

    private static DocumentBuilderFactory    _defFactory    /* =null */;
    /**
     * @return A {@link DocumentBuilderFactory} instance initialized to
     * most "liberal" settings (e.g., no validation, ignore comments,
     * ignore whitespace, etc.)
     */
    public static synchronized DocumentBuilderFactory getDefaultDocumentsFactory ()
    {
        if (null == _defFactory)
        {
            _defFactory = DocumentBuilderFactory.newInstance();
            _defFactory.setCoalescing(true);
            _defFactory.setValidating(false);
            _defFactory.setIgnoringComments(true);
            _defFactory.setIgnoringElementContentWhitespace(true);
            _defFactory.setNamespaceAware(true);
        }

        return _defFactory;
    }
    /**
     * Loads a {@link Document} using a default {@link DocumentBuilderFactory}
     * @param in The <U>open</U> (and positioned) {@link InputStream} from
     * which to load the document
     * @return Loaded {@link Document}
     * @throws IOException If cannot access the {@link InputStream}
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Document loadDocument (final InputStream in)
        throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilderFactory    docFactory=getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder;
        synchronized(docFactory)
        {
            docBuilder = docFactory.newDocumentBuilder();
        }

        return docBuilder.parse(in);
    }
    /**
     * Loads a {@link Document} using a default {@link DocumentBuilderFactory}
     * @param resURL The {@link URL} from which to load the document
     * @return Loaded {@link Document}
     * @throws IOException If cannot access the URL
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Document loadDocument (final URL resURL)
        throws IOException, ParserConfigurationException, SAXException
    {
        InputStream    in=null;
        try
        {
            in = resURL.openStream();
            return loadDocument(in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * Loads a {@link Document} using a default {@link DocumentBuilderFactory}
     * @param f The {@link File} path from which to load the document
     * @return Loaded {@link Document}
     * @throws IOException If cannot access the {@link File}
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Document loadDocument (final File f)
        throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilderFactory    docFactory=getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder;
        synchronized(docFactory)
        {
            docBuilder = docFactory.newDocumentBuilder();
        }

        return docBuilder.parse(f);
    }
    /**
     * Loads a {@link Document} using a default {@link DocumentBuilderFactory}
     * @param filePath file path from which to load the document
     * @return Loaded {@link Document}
     * @throws IOException If cannot access the file
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Document loadDocument (final String filePath)
        throws IOException, ParserConfigurationException, SAXException
    {
        InputStream    in=null;
        try
        {
            in = new FileInputStream(filePath);
            return loadDocument(in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * Loads a {@link Document} using a default {@link DocumentBuilderFactory}
     * @param xml A {@link String} containing the XML document
     * @return Loaded {@link Document}
     * @throws IOException If cannot access the {@link Reader} used to embed the string
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Document loadDocumentFromString (final String xml)
        throws ParserConfigurationException, SAXException, IOException
    {
        if ((null == xml) || (xml.length() <= 0))
            return null;

        final DocumentBuilderFactory    docFactory=getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder;
        synchronized(docFactory)
        {
            docBuilder = docFactory.newDocumentBuilder();
        }

        Reader    r=null;
        try
        {
            r = new StringReader(xml);
            return docBuilder.parse(new InputSource(r));
        }
        finally
        {
            FileUtil.closeAll(r);
        }
    }
    /**
     * Creates a &quot;default&quot; {@link Document} using the {@link DocumentBuilder}
     * returned from {@link #getDefaultDocumentsFactory()} call
     * @return Created {@link Document} instance
     * @throws ParserConfigurationException If failed to initialize the parser
     */
    public static final Document createDefaultDocument () throws ParserConfigurationException
    {
        final DocumentBuilderFactory    docFactory=getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder;
        synchronized(docFactory)
        {
            docBuilder = docFactory.newDocumentBuilder();
        }

        return docBuilder.newDocument();
    }

    private static TransformerFactory    _txFactory    /* =null */;
    public static synchronized TransformerFactory getDefaultDocumentsTransformerFactory ()
    {
        if (null == _txFactory)
            _txFactory = SAXTransformerFactory.newInstance();
        return _txFactory;
    }


    private static final Transformer getDefaultDocumentsTransformer (String method, boolean useXmlDecl) throws TransformerConfigurationException
    {
        if ((null == method) || (method.length() <= 0))
            throw new TransformerConfigurationException("getDefaultDocumentsTransformer(" + method + ") unknown method");

        final TransformerFactory    tf=getDefaultTransformerFactory();
        final Transformer            t;
        synchronized(tf)
        {
            t = tf.newTransformer();
        }

        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.METHOD, method);
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, useXmlDecl ? "no" : "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        return t;
    }

    public static final Transformer getDefaultHtmlTransformer () throws TransformerConfigurationException
    {
        return getDefaultDocumentsTransformer("html", false);
    }

    public static final Transformer getDefaultXmlTransformer () throws TransformerConfigurationException
    {
        return getDefaultDocumentsTransformer("xml", true);
    }
    // returns null if not found, otherwise the found type node
    public static final Node findTypedChild (NodeList nodes, NodeTypeEnum ... types)
    {
        if ((null == types) || (types.length <= 0))
            return null;

        final int    numNodes=(null == nodes) ? 0 : nodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nodes.item(nIndex);
            if (null == n)
                continue;

            final short    nt=n.getNodeType();
            for (final NodeTypeEnum t : types)
            {
                if ((t != null) && (nt == t.getNodeType()))
                    return n;
            }
        }

        return null;
    }
    // returns null if not found, otherwise the found type node
    public static final Node findTypedChild (Node n, NodeTypeEnum ... types)
    {
        return (null == n) ? null : findTypedChild(n.getChildNodes(), types);
    }
    // returns null if not found, otherwise the found type node
    public static final Node findTypedChild (NodeList nodes, short ... types)
    {
        if ((null == types) || (types.length <= 0))
            return null;

        final int    numNodes=(null == nodes) ? 0 : nodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nodes.item(nIndex);
            if (null == n)
                continue;

            final short    nt=n.getNodeType();
            for (final short t : types)
            {
                if (nt == t)
                    return n;
            }
        }

        return null;
    }
    // returns null if not found, otherwise the found type node
    public static final Node findTypedChild (Node n, short ... types)
    {
        return (null == n) ? null : findTypedChild(n.getChildNodes(), types);
    }

    public static final <W extends Appendable> W appendAttribute (
            final W w, final boolean addSpace, final CharSequence aName, final CharSequence aValue)
        throws IOException
    {
        if ((null == aName) || (aName.length() <= 0))
        {
            if ((aValue != null) && (aValue.length() > 0))
                throw new StreamCorruptedException("appendAttribute(" + aName + "/" + aValue + ")  no attribute name");
            return w;
        }

        if (addSpace)    //    separate from previous data
            w.append(' ');
        w.append(aName)
         .append(XML_ATTR_VALUE_SEP)
         .append('"')
         ;

        final CharSequence    eValue=escapeTextValue(aValue, '"', true);
        if ((eValue != null) && (eValue.length() > 0))    // OK if null value
            w.append(eValue);

        w.append('"');
        return w;
    }

    public static final <W extends Appendable> W appendAttribute (
            final Attr a, final W w, final boolean addSpace) throws IOException
    {
        if (null == a)
            return w;

        if (null == w)
            throw new IOException("appendAttribute(" + a + ") no " + Attr.class.getSimpleName() + "/" + Appendable.class.getSimpleName() + " provided");

        return appendAttribute(w, addSpace, a.getName(), a.getValue());
    }

    public static final String toString (final Attr a)
    {
        try
        {
            final CharSequence    sb=
                (null == a) ? null : appendAttribute(a, new StringBuilder(64), false);
            if ((null == sb) || (sb.length() <= 0))
                return null;

            return sb.toString();
        }
        catch (IOException e)    // should not happen
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final <W extends Appendable> W appendAttributes (Collection<? extends Attr> aList, W org) throws IOException
    {
        final int    aSize=(null == aList) ? 0 : aList.size();
        if (aSize <= 0)
            return org;

        W    w=org;
        for (final Attr a : aList)
        {
            if (null == a)    // should not happen
                continue;

            w = appendAttribute(a, w, true);
        }

        return w;
    }

    public static final <W extends Appendable> W appendAttributes (NamedNodeMap attrs, W org, Comparator<? super Attr> c) throws IOException
    {
        final List<? extends Attr>    aList=DOMUtils.getNodeAttributesList(attrs);
        if (c != null)
        {
            final int    aSize=(null == aList) ? 0 : aList.size();
            if (aSize > 1)
                Collections.sort(aList, c);
        }

        return appendAttributes(aList, org);
    }

    public static final <W extends Appendable> W appendAttributes (Node n, W org, Comparator<? super Attr> c) throws IOException
    {
        return appendAttributes((null == n) ? null : n.getAttributes(), org, c);
    }

    public static final String toAttributesString (NamedNodeMap m, Comparator<? super Attr> c)
    {
        try
        {
            final int            numAttrs=(null == m) ? 0 : m.getLength();
            final CharSequence    sb=(numAttrs <= 0) ? null : appendAttributes(m, new StringBuilder(numAttrs * 32), c);
            if ((null == sb) || (sb.length() <= 0))
                return null;

            return sb.toString();
        }
        catch (IOException e)    // should not happen
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final String toAttributesString (NamedNodeMap m)
    {
        return toAttributesString(m, AttrNameComparator.CASE_INSENSITIVE_ATTR_NAME);
    }

    public static final String toAttributesString (Node n, Comparator<? super Attr> c)
    {
        return toAttributesString((null == n) ? null : n.getAttributes(), c);
    }

    public static final String toAttributesString (Node n)
    {
        return toAttributesString(n, AttrNameComparator.CASE_INSENSITIVE_ATTR_NAME);
    }

    public static final <W extends Appendable> W appendElementData (Element elem, W org, Comparator<? super Attr> c, boolean followChildren, boolean closeIt, CharSequence indent) throws IOException
    {
        if (elem == null)
            return org;

        W    w=org;
        if ((indent != null) && (indent.length() > 0))
            w.append(indent);

        final String    tagName=elem.getTagName();
        w.append(XML_ELEM_START_DELIM).append(tagName);

        w = appendAttributes(elem.getAttributes(), w, c);

        if (followChildren)
        {
            final Collection<? extends Element>    chList=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
            if ((chList != null) && (chList.size() > 0))
            {
                w = FileUtil.writeln(w, ">");    // close parent element

                final String    subIndent=(null == indent) ? "\t" : indent + "\t";
                for (final Element chElem : chList)
                {
                    if (null == chElem)    // should not happen
                        continue;

                    // NOTE !!! in sub-call, we follow the children AND close all sub-items
                    w = appendElementData(chElem, w, c, true, true, subIndent);
                }

                if (closeIt)
                {
                    if ((indent != null) && (indent.length() > 0))
                        w.append(indent);
                    w.append(XML_TAG_CLOSURE_SEQ).append(tagName).append(XML_ELEM_END_DELIM);
                    return FileUtil.writeln(w);
                }
                else
                    return w;
            }
        }

        if (closeIt)
        {
            // check if have any text value
            final String    txtValue=getElementStringValue(elem);
            if ((txtValue != null) && (txtValue.length() > 0))
            {
                w.append(XML_ELEM_END_DELIM).append(txtValue);
                w.append(XML_TAG_CLOSURE_SEQ).append(tagName).append(XML_ELEM_END_DELIM);
                return FileUtil.writeln(w);
            }

            return FileUtil.writeln(w, XML_TAG_INLINE_SEQ);
        }
        else
            return FileUtil.writeln(w, ">");
    }

    public static final <W extends Appendable> W appendElementData (Element elem, W org, Comparator<? super Attr> c, boolean followChildren, boolean closeIt) throws IOException
    {
        return appendElementData(elem, org, c, followChildren, closeIt, "");
    }
    /**
     * @param <O> Expected element attribute value type
     * @param elem The {@link Element} from which to extract the attribute
     * {@link String} value
     * @param attrName Attribute name whose value is to be used for conversion
     * @param vsi The {@link ValueStringInstantiator} to use to convert the
     * retrieved {@link String} into an {@link Object}
     * @return Convert {@link Object} - null if no element/attribute/value
     * and/or converter. <B>Note:</B> if the {@link ValueStringInstantiator}
     * returns <code>null</code> itself then this cannot be distinguished from
     * the former situation.
     * @throws Exception if cannot convert the value {@link String} into an
     * {@link Object}
     */
    public static final <O> O getElementAttributeValue (final Element elem, final String attrName, final ValueStringInstantiator<? extends O> vsi) throws Exception
    {
        final String    val=
            ((null == elem) || (null == attrName) || (attrName.length() <= 0) || (null == vsi)) ? null : elem.getAttribute(attrName);
        if ((null == val) || (val.length() <= 0))
            return null;

        return vsi.newInstance(val);
    }
    /**
     * @param elemsMap Initial {@link Map} of {@link Element}-s whose
     * entire attributes are requested. Key=identifier, value=associated
     * XML element
     * @param ignoreDuplicates <code>false</code>=throw {@link DOMException}
     * if same pair remapped and not same value
     * @return A {@link Map} where key=same identifier as the original map,
     * value={@link Map} of all attributes extracted from the original
     * associated element (key=attribute name, value=data)
     * @throws DOMException if same value remapped and <code>ignoreDuplicates</code>
     * is <code>false</code>
     */
    public static final Map<String,Map<String,String>> getAllValuesMap (
            final Map<String,Element> elemsMap, final boolean ignoreDuplicates) throws DOMException
    {
        final Collection<? extends Map.Entry<String,Element>>    valsElems=
            ((null == elemsMap) || (elemsMap.size() <= 0)) ? null : elemsMap.entrySet();
        if ((null == valsElems) || (valsElems.size() <= 0))
            return null;

        Map<String,Map<String,String>>    valsMap=null;
        for (final Map.Entry<String,Element> ve : valsElems)
        {
            if (null == ve)    // should not happen
                continue;

            final String    valName=ve.getKey();
            if ((null == valName) || (valName.length() <= 0))
                continue;    // TODO consider throwing an exception

            final Map<String,String>    attrsMap=getNodeAttributes(ve.getValue(), ignoreDuplicates);
            if ((null == attrsMap) || (attrsMap.size() <= 0))
                continue;    // TODO consider throwing an exception

            if (null == valsMap)
                valsMap = new TreeMap<String, Map<String,String>>(String.CASE_INSENSITIVE_ORDER);
            valsMap.put(valName, attrsMap);
        }

        return valsMap;
    }
    /**
     * @param attrsMap A {@link Map} of extracted attributes for an element
     * @param attrName Requested attribute name 'filter'
     * @return A {@link Map} of all values that contain the requested
     * attribute - key=value identifier (same as original map),
     * value=requested attribute data. May be null/empty if no initial map
     * or none of the values has data for the requested attribute
     * @see #getAllValuesMap(Map, boolean) for extracting all attributes for elements
     */
    public static final Map<String,String> getValuesAttributes (final Map<String,Map<String,String>> attrsMap, final String attrName)
    {
        final Collection<? extends Map.Entry<String,Map<String,String>>>    valsEntries=
            ((null == attrName) || (attrName.length() <= 0) || (null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.entrySet();
        if ((null == valsEntries) || (valsEntries.size() <= 0))
            return null;

        Map<String,String>    res=null;
        for (final Map.Entry<String,Map<String,String>> ve : valsEntries)
        {
            if (null == ve)
                continue;

            final String    vn=ve.getKey();
            if ((null == vn) || (vn.length() <= 0))
                continue;    // TODO consider throwing an exception

            final Map<String,String>    va=ve.getValue();
            final String                vv=((null == va) || (va.size() <= 0)) ? null : va.get(attrName);
            if ((null == vv) || (vv.length() <= 0))
                continue;    // OK if no attribute value

            if (null == res)
                res = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            res.put(vn, vv);
        }

        return res;
    }
    /**
     * @param <N> The type of extracted {@link Node}
     * @param nodeClass The expected {@link Class} of extracted nodes.
     * <B>Note:</B> if does not match specified <code>nodeType</code>
     * parameter the {@link ClassCastException} will occur
     * @param nodes A {@link NodeList} whose specific {@link Node} types we
     * want to extract
     * @param nodeType The node types to extract - must match the expected
     * node cClass
     * @return A {@link Collection} of all {@link Node}-s of specified type
     */
    public static final <N extends Node> Collection<N> extractAllNodes (final Class<N> nodeClass, final NodeList nodes, final short nodeType)
    {
        final int        numNodes=(null == nodes) ? 0 : nodes.getLength();
        Collection<N>    el=null;
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nodes.item(nIndex);
            if ((null == n) || (n.getNodeType() != nodeType))
                continue;

            if (null == el)
                el = new LinkedList<N>();
            el.add(nodeClass.cast(n));
        }

        return el;
    }

    public static final Collection<? extends Node> extractAllNodes (final NodeList nodes, final NodeTypeEnum nodeType)
    {
        return (null == nodeType) ? null : extractAllNodes(nodeType.getNodeClass(), nodes, nodeType.getNodeType());
    }
    /**
     * @param <N> The type of extracted {@link Node}
     * @param nodeClass The expected {@link Class} of extracted nodes.
     * <B>Note:</B> if does not match specified <code>nodeType</code>
     * parameter the {@link ClassCastException} will occur
     * @param root A {@link Node} whose child nodes are to be extracted
     * @param nodeType The node types to extract - must match the expected
     * node class
     * @return A {@link Collection} of all {@link Node}-s of specified type
     */
    public static final <N extends Node> Collection<N> extractAllNodes (final Class<N> nodeClass, final Node root, final short nodeType)
    {
        return (null == root) ? null : extractAllNodes(nodeClass, root.getChildNodes(), nodeType);
    }

    public static final Collection<? extends Node> extractAllNodes (final Node root, final NodeTypeEnum nodeType)
    {
        return ((null == root) || (null == nodeType)) ? null : extractAllNodes(root.getChildNodes(), nodeType);
    }

    public static final <A extends Appendable> A appendElementString (
            final A sb, final String tagName, final Collection<? extends Attr> al, final CharSequence textValue)
        throws IOException
    {
        if (null == sb)
            throw new IOException("appendElementString(" + tagName + ")[" + textValue + "] no " + Appendable.class.getSimpleName() + " instance provided");

        sb.append(XML_ELEM_START_DELIM)
          .append(tagName)
          ;
        appendAttributes(al, sb);

        // if have text then add it to the element display
        final CharSequence    effValue=escapeTextValue(textValue, '\0', true);
        if ((effValue != null) && (effValue.length() > 0))
            sb.append(XML_ELEM_END_DELIM)
              .append(textValue)
              .append(XML_ELEM_START_DELIM)
              .append(XML_ELEM_CLOSURE_DELIM)
              .append(tagName)
              .append(XML_ELEM_END_DELIM)
              ;
        else
            sb.append(XML_ELEM_CLOSURE_DELIM).append(XML_ELEM_END_DELIM);

        return sb;
    }

    public static final <A extends Appendable> A appendElementString (final A sb, final Element elem) throws IOException
    {
        if (null == elem)
            return sb;

        return appendElementString(sb, elem.getTagName(), getNodeAttributesList(elem), getElementStringValue(elem));
    }

    public static final String toString (final Element elem)
    {
        final String                        tagName=
            (null == elem) ? null : elem.getTagName(),
                                            textValue=
            (null == elem) ? null : getElementStringValue(elem);
        final Collection<? extends Attr>    al=getNodeAttributesList(elem);
        final int                            numEntries=(null == al) ? 0 : al.size(),
                                            tnLen=(null == tagName) ? 0 : tagName.length(),
                                            vlLen=(null == textValue) ? 0 : textValue.length(),
                                            sbLen=
            Math.max(0, tnLen) + 8 + ((vlLen > 0) ? (tnLen + vlLen + 4) : 0) + numEntries * 64;
        if ((tnLen <= 0) && (numEntries <= 0))
            return null;

        try
        {
            return appendElementString(new StringBuilder(sbLen), tagName, al, textValue).toString();
        }
        catch(IOException e)    // should not happen
        {
            return null;
        }
    }
    /**
     * @param <V> Expected element value type
     * @param el A {@link Collection} of XML {@link Element}-s each
     * representing a value to be extracted from it
     * @param xmlInst The {@link XmlValueInstantiator} to be used for
     * converting the XML element to an actual {@link Object} (<B>Note:</B>
     * <code>null</code> values returned from {@link XmlValueInstantiator#fromXml(Element)}
     * are <U>ignored</U>)
     * @return A {@link Collection} with the reconstructed values - may be
     * null/empty
     * @throws Exception if cannot reconstruct some value(s) from the XML
     * element(s)
     */
    public static final <V> Collection<V> extractValues (final Collection<? extends Element> el, final XmlValueInstantiator<V> xmlInst) throws Exception
    {
        final int    numElems=(null == el) ? 0 : el.size();
        if (numElems <= 0)
            return null;

        final Collection<V> vals=new ArrayList<V>(numElems);
        for (final Element elem : el)
        {
            final V    v=(null == elem) ? null : xmlInst.fromXml(elem);
            if (v != null)
                vals.add(v);
        }

        return vals;
    }
    /**
     * @param <V> Expected element value type
     * @param nodes A {@link NodeList} whose {@link Element}-s are assumed
     * to contain values to be re-constructed
     * @param xmlInst The {@link XmlValueInstantiator} to be used for
     * converting the XML element to an actual {@link Object} (<B>Note:</B>
     * <code>null</code> values returned from {@link XmlValueInstantiator#fromXml(Element)}
     * are <U>ignored</U>)
     * @return A {@link Collection} with the reconstructed values - may be
     * null/empty
     * @throws Exception if cannot reconstruct some value(s) from the XML
     * element(s)
     */
    public static final <V> Collection<V> extractValues (final NodeList nodes, final XmlValueInstantiator<V> xmlInst) throws Exception
    {
        return extractValues(extractAllNodes(Element.class, nodes, Node.ELEMENT_NODE), xmlInst);
    }
    /**
     * @param <V> Expected element value type
     * @param root A 'root' XML {@link Element} whose sub-elements are assumed
     * to contain values to be re-constructed
     * @param xmlInst The {@link XmlValueInstantiator} to be used for
     * converting the XML element to an actual {@link Object} (<B>Note:</B>
     * <code>null</code> values returned from {@link XmlValueInstantiator#fromXml(Element)}
     * are <U>ignored</U>)
     * @return A {@link Collection} with the reconstructed values - may be
     * null/empty
     * @throws Exception if cannot reconstruct some value(s) from the XML
     * element(s)
     */
    public static final <V> Collection<V> extractValues (final Element root, final XmlValueInstantiator<V> xmlInst) throws Exception
    {
        return (null == root) ? null : extractValues(root.getChildNodes(), xmlInst);
    }
    /**
     * @param <V> Expected element value type
     * @param doc A {@link Document} whose root contains sub-elements assumed
     * to contain values to be re-constructed
     * @param xmlInst The {@link XmlValueInstantiator} to be used for
     * converting the XML element to an actual {@link Object} (<B>Note:</B>
     * <code>null</code> values returned from {@link XmlValueInstantiator#fromXml(Element)}
     * are <U>ignored</U>)
     * @return A {@link Collection} with the reconstructed values - may be
     * null/empty
     * @throws Exception if cannot reconstruct some value(s) from the XML
     * element(s)
     */
    public static final <V> Collection<V> extractValues (final Document doc, final XmlValueInstantiator<V> xmlInst) throws Exception
    {
        return (null == doc) ? null : extractValues(doc.getDocumentElement(), xmlInst);
    }
    /**
     * @param doc The {@link Document} to use to create XML {@link Element}-s
     * via call to {@link XmlConvertible#toXml(Document)}
     * @param root The root XML {@link Element} to append the sub-elements to
     * @param items A {@link Collection} of {@link XmlConvertible} items - may
     * be null/empty
     * @return Root element after appending to it all non-null elements
     * @throws Exception If @link XmlConvertible#toXml(Document)} call failed
     */
    public static final Element appendConvertibleItems (
                    final Document                                    doc,
                    final Element                                    root,
                    final Collection<? extends XmlConvertible<?>>    items) throws Exception
    {
        if ((null == items) || (items.size() <= 0))
            return root;

        for (final XmlConvertible<?> x : items)
        {
            final Element    xElem=(null == x) ? null : x.toXml(doc);
            if (null == xElem)
                continue;
            root.appendChild(xElem);
        }

        return root;
    }

    private static TransformerFactory    _xFactory    /* =null */;
    public static final synchronized TransformerFactory getDefaultTransformerFactory ()
    {
        if (null == _xFactory)
            _xFactory = TransformerFactory.newInstance();
        return _xFactory;
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT {@link Source}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslSource The XSLT {@link Source} - if null then nothing is done
     * (i.e., same as input is returned)
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     */
    public static final Document xlateDocument (final Document doc, final Source xslSource) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslSource))
            return doc;

        final TransformerFactory    xt=DOMUtils.getDefaultTransformerFactory();
        final Transformer            t;
        synchronized(xt)
        {
            t = xt.newTransformer(xslSource);
        }

        final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
        final Document                    resDoc=docBuilder.newDocument();
        t.transform(new DOMSource(doc), new DOMResult(resDoc));

        return resDoc;
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT {@link InputStream}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param in The XSLT {@link InputStream} - if null then nothing is done
     * (i.e., same as input is returned). <B>Note:</B> it is up to the
     * <U>caller</U> to call {@link InputStream#close()} upon return from this
     * method (successful or otherwise)
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, Source)
     */
    public static final Document xlateDocument (final Document doc, final InputStream in) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == in))
            return doc;

        return xlateDocument(doc, new StreamSource(in));
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT resource {@link URL}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslURL The XSLT {@link URL} - if null then nothing is done
     * (i.e., same as input is returned). <B>Note:</B> if {@link URL#openStream()}
     * returns null then no translation is done
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws IOException if failed to open the resource URL
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, InputStream)
     */
    public static final Document xlateDocument (final Document doc, final URL xslURL) throws IOException, ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslURL))
            return doc;

        InputStream    in=null;
        try
        {
            if (null == (in=xslURL.openStream()))
                return doc;

            return xlateDocument(doc, in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT {@link Document}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslDoc The XSLT {@link Document} - if null then nothing is done
     * (i.e., same as input is returned).
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, Source)
     */
    public static final Document xlateDocument (final Document doc, final Document xslDoc) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslDoc))
            return doc;

        return xlateDocument(doc, new DOMSource(xslDoc));
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT {@link File}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslFile The XSLT {@link File} - if null then nothing is done
     * (i.e., same as input is returned).
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, Source)
     */
    public static final Document xlateDocument (final Document doc, final File xslFile) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslFile))
            return doc;

        return xlateDocument(doc, new StreamSource(xslFile));
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT file path
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslFile The XSLT file path - if null then nothing is done
     * (i.e., same as input is returned).
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, File)
     */
    public static final Document xlateDocument (final Document doc, final String xslFile) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslFile) || (xslFile.length() <= 0))
            return doc;

        return xlateDocument(doc, new File(xslFile));
    }
    /**
     * Translates an XML {@link Document} into another using the provided
     * XSLT {@link Reader}
     * @param doc The source {@link Document} - if null then nothing is done
     * @param xslRdr The XSLT {@link Reader} - if null then nothing is done
     * (i.e., same as input is returned). <B>Note:</B> it is up to the
     * <U>caller</U> to call {@link Reader#close()} once this method returns
     * (whether successfully or otherwise)
     * @return Translated document - may be null (if original was null) or
     * same as original (if no XSLT source)
     * @throws TransformerException if failed to transform
     * @throws ParserConfigurationException  if malformed document or XSLT
     * @see #xlateDocument(Document, Source)
     */
    public static final Document xlateDocument (final Document doc, final Reader xslRdr) throws ParserConfigurationException, TransformerException
    {
        if ((null == doc) || (null == xslRdr))
            return doc;

        return xlateDocument(doc, new StreamSource(xslRdr));
    }
    // especially for StandaloneElement(s)
    public static final NodeList getElementsByTagNameNS (Element elem, String namespaceURI, String localName) throws DOMException
    {
        if (null == elem)
            return null;

        if ((null == namespaceURI) || (namespaceURI.length() <= 0)
         || (null == localName) || (localName.length() <= 0))
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "getElementsByTagNameNS(" + namespaceURI + "[" + localName + "] incomplete arguments");

        final boolean            isNSWildcard="*".equals(namespaceURI),
                                isLocalWildcard="*".equals(localName);
        final NodeList            children=elem.getChildNodes();
        final int                numChildren=(null == children) ? 0 : children.getLength();
        final BaseNodeListImpl    res=new BaseNodeListImpl(Math.max(1, numChildren));
        for (int    cIndex=0; cIndex < numChildren; cIndex++)
        {
            final Node    n=children.item(cIndex);
            if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            final Element    e=(Element) n;
            final String    eName=e.getTagName(), eNS=e.getNamespaceURI();
            if ((null == eName) || (eName.length() <= 0))
                continue;    // should not happen

            if (!isNSWildcard)
            {
                if (!namespaceURI.equalsIgnoreCase(eNS))
                    continue;
            }

            if (!isLocalWildcard)
            {
                if (!localName.equalsIgnoreCase(eName))
                    continue;
            }

            res.add(n);
        }

        return res;
    }
    // especially for StandaloneElement(s)
    public static final NodeList getElementsByTagName (Element elem, String name)
    {
        return getElementsByTagNameNS(elem, "*", name);
    }

    public static final Element getElementById (Element elem, String id, boolean ret1st) throws IllegalStateException
    {
        final Collection<? extends Element>    el=
            ((null == id) || (id.length() <= 0)) ? null : extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
        if ((null == el) || (el.size() <= 0))
            return null;

        Element    ret=null;
        for (final Element se : el)
        {
            final NamedNodeMap    attrs=(null == se) ? null : se.getAttributes();
            final int            numAttrs=(null == attrs) ? 0 : attrs.getLength();
            for (int    aIndex=0; aIndex < numAttrs; aIndex++)
            {
                final Node    n=attrs.item(aIndex);
                if ((null == n) || (n.getNodeType() != Node.ATTRIBUTE_NODE))
                    continue;

                final Attr    a=(Attr) n;
                if (!a.isId())
                    continue;

                final String    v=a.getNodeValue();
                if (!id.equals(v))
                    continue;

                if (ret1st)
                    return se;

                if (ret != null)
                    throw new IllegalStateException("getElementById(" + DOMUtils.toString(elem) + ")[" + id + "] multiple matches found: " + DOMUtils.toString(se) + " and " + DOMUtils.toString(ret));
                ret = se;
            }
        }

        return ret;
    }
    /**
     * @param parent Parent {@link Node} - ignore if <code>null</code>
     * @return A {@link Map} of all the children of the parent node where
     * key=child {@link NodeTypeEnum}, value=a {@link Collection} of all the
     * children of specified type
     * @throws NoSuchElementException If found a {@link Node} with an unknown
     * type.
     */
    public static final Map<NodeTypeEnum,Collection<Node>> extractChildrenMap (final Node parent) throws NoSuchElementException
    {
        final NodeList                        nl=(null == parent) ? null : parent.getChildNodes();
        final int                            numNodes=(null == nl) ? 0 : nl.getLength();
        Map<NodeTypeEnum,Collection<Node>>    ret=null;
        for (int nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nl.item(nIndex);
            if (null == n)
                continue;

            final NodeTypeEnum    t=NodeTypeEnum.fromNode(n);
            if (null == t)
                throw new NoSuchElementException("extractChildren(" + parent + ") unknown node type (" + n.getNodeType() + ") for node=" + n.getNodeName());

            if (null == ret)
                ret = new EnumMap<NodeTypeEnum,Collection<Node>>(NodeTypeEnum.class);

            Collection<Node>    tl=ret.get(t);
            if (null == tl)
            {
                tl = new LinkedList<Node>();
                ret.put(t, tl);
            }
            tl.add(n);
        }

        return ret;
    }
    /**
     * Looks for the <U>first</U> {@link Attr}-ibute of a {@link Node} whose
     * name matches one of the names provided
     * @param n The {@link Node} to check
     * @param caseSensitive Use case sensitive (or not) comparison for
     * attribute name comparison
     * @param names The {@link Collection} of names to look for.
     * @return The first match (<code>null</code> if no match found or no
     * node/attributes/names). <B>Note:</B> the order in which the node
     * attributes are checked is not defined - i.e., if more than one name
     * appears then the choice which is the "match" is <B>indeterminate</B>
     */
    public static final Attr findFirstAttribute (Node n, boolean caseSensitive, Collection<String> names)
    {
        final int                            numNames=(null == names) ? 0 : names.size();
        final Collection<? extends Attr>    al=
            ((null == n) || (numNames <= 0)) ? null : extractAllNodes(Attr.class, n, Node.ATTRIBUTE_NODE);
        final int            numAttrs=(null == al) ? 0 : al.size();
        if (numAttrs <= 0)
            return null;

        final Comparator<? super String>    c=caseSensitive ? null : String.CASE_INSENSITIVE_ORDER;
        for (final Attr a : al)
        {
            final String    an=(null == a) ? null : a.getName();
            if ((null == an) || (an.length() <= 0))
                continue;

            if (CollectionsUtils.containsElement(names, an, c))
                return a;
        }

        return null;
    }
    /**
     * Looks for the <U>first</U> {@link Attr}-ibute of a {@link Node} whose
     * name matches one of the names provided
     * @param n The {@link Node} to check
     * @param caseSensitive Use case sensitive (or not) comparison for
     * attribute name comparison
     * @param names The names to look for.
     * @return The first match (<code>null</code> if no match found or no
     * node/attributes/names). <B>Note:</B> the order in which the node
     * attributes are checked is not defined - i.e., if more than one name
     * appears then the choice which is the "match" is <B>indeterminate</B>
     */
    public static final Attr findFirstAttribute (Node n, boolean caseSensitive, String ... names)
    {
        return (null == n) ? null : findFirstAttribute(n, caseSensitive, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, names));
    }
    /**
     * Looks for the <U>first</U> {@link Attr}-ibute of a {@link Node} whose
     * name matches one of the names provided
     * @param n The {@link Node} to check
     * @param caseSensitive Use case sensitive (or not) comparison for
     * attribute name comparison
     * @param names The {@link Collection} of names to look for.
     * @return The first match value (<code>null</code> if no match found or
     * no node/attributes/names). <B>Note:</B> the order in which the node
     * attributes are checked is not defined - i.e., if more than one name
     * appears then the choice which is the "match" is <B>indeterminate</B>
     */
    public static final String findFirstAttributeValue (Node n, boolean caseSensitive, Collection<String> names)
    {
        final Attr    a=findFirstAttribute(n, caseSensitive, names);
        if (null == a)
            return null;

        return a.getValue();
    }
    /**
     * Looks for the <U>first</U> {@link Attr}-ibute of a {@link Node} whose
     * name matches one of the names provided
     * @param n The {@link Node} to check
     * @param caseSensitive Use case sensitive (or not) comparison for
     * attribute name comparison
     * @param names The names to look for.
     * @return The first match value (<code>null</code> if no match found or
     * no node/attributes/names). <B>Note:</B> the order in which the node
     * attributes are checked is not defined - i.e., if more than one name
     * appears then the choice which is the "match" is <B>indeterminate</B>
     */
    public static final String findFirstAttributeValue (Node n, boolean caseSensitive, String ... names)
    {
        return (null == n) ? null : findFirstAttributeValue(n, caseSensitive, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, names));
    }
    /**
     * Converts a processing instruction (format=<pre><?name a1="v1" a2="v2"?></pre>)
     * into an XML element having the same tag and attributes
     * @param doc The {@link Document} to use in order to create the XML
     * element - if <code>null</code> then a standalone implementation will be used
     * @param pi The {@link ProcessingInstruction} - ignored if <code>null</code>
     * @return The equivalent XML {@link Element} - may be <code>null</code> if
     * no initial processing instruction instance
     * @throws DOMException if malformed data format
     */
    public static final Element toElement (final Document doc, final ProcessingInstruction pi)
        throws DOMException
    {
        if (null == pi)
            return null;

        final String    pn=pi.getTarget(), pd=pi.getData();
        final int        nLen=(null == pn) ? 0 : pn.length(),
                        dLen=(null == pd) ? 0 : pd.length(),
                        tLen=nLen + Math.max(dLen,0) + 8;
        if (nLen <= 0)
            throw new DOMException(DOMException.SYNTAX_ERR, "toElement(" + pi + ") no " + ProcessingInstruction.class.getSimpleName() + " target name");

        final String    elemData=new StringBuilder(tLen)
                                    .append(XML_ELEM_START_DELIM)
                                    .append(pn)
                                    .append(' ')
                                    .append(pd)
                                    .append(XML_TAG_INLINE_SEQ)
                                .toString();
        final Triplet<? extends Element,? extends CharacterData,Boolean>    pe=
            DOMUtils.parseElementString(doc, elemData);
        final Element                                                        elem=
            (null == pe) ? null : pe.getV1();
        if (elem != null)
        {
            final CharacterData    txt=pe.getV2();
            if (txt != null)    // if have an element then no text is expected
                throw new DOMException(DOMException.SYNTAX_ERR, "toElement(" + pi + ") text value found: " + txt.getData());

            final Boolean    end=pe.getV3();    // if have an element then we terminated properly
            if ((null == end) || (!end.booleanValue()))
                throw new DOMException(DOMException.SYNTAX_ERR, "toElement(" + pi + ") improper termination");
        }

        return elem;
    }

    public static final Element toElement (final ProcessingInstruction pi)
        throws DOMException
    {
        return toElement(null, pi);
    }
    /**
     * Copies/duplicates the attributes to the target {@link Element}
     * @param srcList A {@link Collection} of name/value "pairs" represented
     * as {@link java.util.Map.Entry}-ies where key=name, value=value. <B>Note:</B>
     * if a name has null/empty value then it is <U>skipped</U>
     * @param dst Target element - if <code>null</code> then nothing is done
     * @throws DOMException If found a non-null/empty value that has no name
     */
    public static final void copyAttributes (
            final Collection<? extends Map.Entry<?,?>> srcList, final Element dst) throws DOMException
    {
        if ((null == dst) || (null == srcList) || (srcList.size() <= 0))
            return;

        for (final Map.Entry<?,?> ae : srcList)
        {
            final Object    nmo=(null == ae) ? null : ae.getKey(),
                            vlo=(null == ae) ? null : ae.getValue();
            final String    n=(null == nmo) ? null : nmo.toString(),
                            v=(null == vlo) ? null : vlo.toString();
            final int        vLen=(null == v) ? 0 : v.length();
            if ((null == n) || (n.length() <= 0))
            {
                if (vLen <= 0)
                    continue;

                throw new DOMException(DOMException.NAMESPACE_ERR, "copyAttributes(" + toString(dst) + ") value=" + v + " has no name");
            }
            else if (vLen <= 0)    // if no value but valid name then ignore
                continue;

            dst.setAttribute(n, v);
        }
    }

    public static final List<Map.Entry<String,String>> getAttributePairsList (final Collection<? extends Attr> al)
    {
        final int                                numAttrs=(null == al) ? 0 : al.size();
        final List<Map.Entry<String,String>>    pl=(numAttrs <= 0) ? null : new ArrayList<Map.Entry<String,String>>(numAttrs);
        if (numAttrs <= 0)
            return pl;

        for (final Attr a : al)
        {
            final String    n=(null == a) ? null : a.getName(),
                            v=(null == a) ? null : a.getValue();
            final int        vLen=(null == v) ? 0 : v.length();
            if ((null == n) || (n.length() <= 0))
            {
                if (vLen <= 0)
                    continue;

                throw new DOMException(DOMException.NAMESPACE_ERR, "getAttributePairsList(" + toString(a) + ") value=" + v + " has no name");
            }
            else if (vLen <= 0)    // if no value but valid name then ignore
                continue;

            pl.add(new StringPairEntry(n, v));
        }

        return pl;
    }

    public static final List<Map.Entry<String,String>> getAttributePairsList (final NamedNodeMap attrs) throws DOMException
    {
        return getAttributePairsList(getNodeAttributesList(attrs));
    }

    public static final List<Map.Entry<String,String>> copyAttributes (final NamedNodeMap attrs, final Element dst) throws DOMException
    {
        final List<Map.Entry<String,String>>    al=(null == dst) ? null : getAttributePairsList(attrs);
        copyAttributes(al, dst);
        return al;
    }

    public static final List<Map.Entry<String,String>> copyAttributes (final Node n, final Element dst) throws DOMException
    {
        return copyAttributes((null == n) ? null : n.getAttributes(), dst);
    }

    public static final <A extends Appendable> A appendComment (final A w, final CharSequence data) throws IOException
    {
        if ((null == data) || (data.length() <= 0))
            return w;

        if (null == w)
            throw new IOException("appendComment(" + data + ") no " + Appendable.class.getSimpleName() + " instance");

        w.append("<!-- ")
         .append(data)
         .append(" -->");
        return w;
    }

    public static final <A extends Appendable> A appendComment (final A w, final Comment c) throws IOException
    {
        return appendComment(w, (null == c) ? null : c.getData());
    }

    public static final <N extends Node> N appendChildren (final N root, final Collection<? extends Node> children)
    {
        if ((root == null) || (children == null) || children.isEmpty())
            return root;

        for (final Node child : children)
        {
            if (child == null)
                continue;    // debug breakpoint
            root.appendChild(child);
        }

        return root;
    }

    public static final Element createOptionalRoot (final Document doc, final String rootName, final Collection<? extends Element> children)
    {
        if ((children == null) || children.isEmpty())
            return null;

        return appendChildren(doc.createElement(rootName), children);
    }
}
