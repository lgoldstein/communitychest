
package net.community.chest.net.proto.text.http.hotmail;

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Helper functions for handling the login response</P>
 * @author Lyor G.
 * @since Aug 3, 2008 9:05:08 AM
 */
public final class LoginHandler {
    private LoginHandler ()
    {
        // no instance
    }

    public static final String
        // root folder access URI
        MsgFolderRootProp="msgfolderroot",
        // INBOX folder access URI
        InboxProp="inbox",
        // deleted items access URI
        DeletedItemsProp="deleteditems",
        // sent items folder access URI
        SentItemsProp="sentitems",
        // outbox folder access URI
        OutboxProp="outbox",
        // drafts folder access URI
        DraftsProp="drafts",
        // contacts folder access URI
        ContactsProp="contacts",
        // message to send access URI
        SendMsgProp="sendmsg";
    /**
     * XML payload used for login
     */
    public static final String    LOGIN_XML_PAYLOAD=
        "<?xml version=\"1.0\"?>\r\n"
            + "<D:propfind xmlns:D=\"DAV:\" xmlns:h=\"" + HotmailProtocol.HOTMAIL_SCHEMA_URI
            + "\" xmlns:hm=\"" + HotmailProtocol.HOTMAIL_XML_NAMESPACE + "\">\r\n"
                + "\t<D:prop>\r\n"
                    + "\t\t<h:adbar/>\r\n"
                    + "\t\t<hm:" + ContactsProp + "/>\r\n"
                    + "\t\t<hm:" + InboxProp + "/>\r\n"
                    + "\t\t<hm:" + OutboxProp + "/>\r\n"
                    + "\t\t<hm:" + SendMsgProp + "/>\r\n"
                    + "\t\t<hm:" + SentItemsProp + "/>\r\n"
                    + "\t\t<hm:" + DeletedItemsProp + "/>\r\n"
                    + "\t\t<hm:" + DraftsProp + "/>\r\n"
                    + "\t\t<hm:" + MsgFolderRootProp + "/>\r\n"
                    + "\t\t<h:maxpoll/>\r\n"
                    + "\t\t<h:sig/>\r\n"
                + "\t</D:prop>\r\n"
            + "</D:propfind>"
    ;
    /**
     * minimum set of properties required after login
     */
    private static final String[]    _mandatoryProps={
        MsgFolderRootProp,
        InboxProp,
        DeletedItemsProp,
        SentItemsProp,
        SendMsgProp
    };
    /**
     * mandatory properties of interest for quick access - key=value=property name
     */
    private static final Map<String,String>    _mandatoryPropsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
    static {    // initialize quick access mandatory properties of interest
        for (final String p : _mandatoryProps)
            _mandatoryPropsMap.put(p, p);
    }
    // login properties of interest
    private static final String[]    _loginProps={
        MsgFolderRootProp,
        InboxProp,
        DeletedItemsProp,
        SentItemsProp,
        SendMsgProp,
        OutboxProp,
        DraftsProp,
        ContactsProp
    };
    /**
     * login properties of interest for quick access - name=value=property name
     */
    private static final Map<String,String>    _loginPropsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
    static {    // initialize quick access login properties of interest
        for (final String p : _loginProps)
            _loginPropsMap.put(p, p);
    }
    /**
     * Extracts the value of the login property of interest
     * @param propNode document node element that is the root of the login property
     * @param nodeName name of extracted property value
     * @param clProps map into which to add the property value
     * @return TRUE if successful
     */
    public static final boolean extractLoginProperty (final Node propNode, final String nodeName, final Map<String,String> clProps)
    {
        if ((null == clProps) || (null == propNode) || (null == nodeName) || (nodeName.length() <= 0))
            return false;

        final short    nodeType=propNode.getNodeType();
        if (nodeType != Node.ELEMENT_NODE)
            return false;

        final NodeList    propChildren=propNode.getChildNodes();
        final int        numChildren=(null == propChildren) ? 0 : propChildren.getLength();
        if (numChildren <= 0)
            return false;

        // NOTE !!! we actually expect only one value, but we do a whole loop (which might cause errors)
        for (int    chIndex=0; chIndex < numChildren; chIndex++)
        {
            final Node    rspChild=propChildren.item(chIndex);
            if (null == rspChild)    // should not happen
                continue;

            final String    nodeValue=rspChild.getNodeValue();
            if ((null == nodeValue) || (nodeValue.length() <= 0))
            {
                // mandatory nodes must have a non-empty value
                if (_mandatoryPropsMap.get(nodeName) != null)
                    return false;
            }
            else
                clProps.put(nodeName, nodeValue);
        }

        return true;
    }
    /**
     * Checks recursively all nodes for properties of interest from the login authorization response
     * @param rspChildren nodes list - may be null/empty
     * @param clProps properties map to be updated
     * @return TRUE if successful
     */
    public static final boolean extractLoginProperties (final NodeList rspChildren, final Map<String,String> clProps)
    {
        if (null == clProps)
            return false;

        final int    numChildren=(null == rspChildren) ? 0 : rspChildren.getLength();
        for (int    chIndex=0; chIndex < numChildren; chIndex++)
        {
            final Node    rspChild=rspChildren.item(chIndex);
            if (null == rspChild)    // should not happen
                continue;

            final String    nodeName=rspChild.getLocalName();
            if ((nodeName != null) && (nodeName.length() > 0) && (_loginPropsMap.get(nodeName) != null))
            {
                if (!extractLoginProperty(rspChild, nodeName, clProps))
                    return false;

                // no need to check sub-nodes
                continue;
            }

            // traverse node recursively
            if (!extractLoginProperties(rspChild.getChildNodes(), clProps))
                return false;
        }

        return true;
    }
    /**
     * Extracts properties of interest from the authorization OK response - e.g.,
     * folders locations, sending access URI, etc.
     * @param rspDoc response XML document
     * @param clProps properties map to be updated - key=property name (case insensitive)
     * @return TRUE if successful
     */
    public static final boolean extractLoginProperties (final Document rspDoc, final Map<String,String> clProps)
    {
        if (null == rspDoc)
            return false;

        if (!extractLoginProperties(rspDoc.getChildNodes(), clProps))
            return false;

        // make sure we have the mandatory properties
        for (int    mpIndex=0; mpIndex < _mandatoryProps.length; mpIndex++)
        {
            final String    mpValue=clProps.get(_mandatoryProps[mpIndex]);
            if ((null == mpValue) || (mpValue.length() <= 0))
                return false;
        }

        return true;
    }
}
