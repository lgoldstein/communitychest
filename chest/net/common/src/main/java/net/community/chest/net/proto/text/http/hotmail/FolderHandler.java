/**
 *
 */
package net.community.chest.net.proto.text.http.hotmail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2008 9:13:08 AM
 */
public final class FolderHandler {
    private FolderHandler ()
    {
        // no instance
    }
    /**
     * Class used to hold folder related flags
     * @author lyorg
     * 12/07/2004
     */
    public static class FolderFlags {
        private boolean _fHasSubFolders    /* =false */;
        private boolean _fCanHaveSubFolders=false;
        private boolean _fIsFolder=true;    // auxiliary flag

        public FolderFlags ()
        {
            super();
        }

        public boolean isSubFoldersAccessAvailable ()
        {
            return _fHasSubFolders;
        }

        public void setSubFoldersAccessAvailable (boolean fHasSubFolders)
        {
            _fHasSubFolders = fHasSubFolders;
        }

        public boolean isSubFoldersCreationAllowed ()
        {
            return _fCanHaveSubFolders;
        }

        public void setSubFoldersCreationAllowed (boolean fCanHaveSubFolders)
        {
            _fCanHaveSubFolders = fCanHaveSubFolders;
        }

        public boolean isFolder ()
        {
            return _fIsFolder;
        }

        public void setIsFolder (boolean fIsFolder)
        {
            _fIsFolder = fIsFolder;
        }
    }
    /**
     * Class used to hold folder message counts
     */
    public static class FolderMsgCounts {
        private int    _totalMsgs    /* =0 */, _unreadMsgs /* =0 */;

        public FolderMsgCounts ()
        {
            super();
        }

        public int getTotalMsgs ()
        {
            return _totalMsgs;
        }

        public void setTotalMsgs (int totalMsgs)
        {
            _totalMsgs = totalMsgs;
        }
        public int getUnreadMsgs ()
        {
            return _unreadMsgs;
        }

        public void setUnreadMsgs (int unreadMsgs)
        {
            _unreadMsgs = unreadMsgs;
        }
    }
    /**
     * Class used to hold information about a folder
     */
    public static class FolderInfo {
        private String                     _href    /* =null */, _name    /* =null */;
        private final FolderMsgCounts    _counts=new FolderMsgCounts();
        private final FolderFlags        _flags=new FolderFlags();

        public FolderInfo ()
        {
            super();
        }

        public String getHRef ()
        {
            return _href;
        }

        public void setHRef (String href)
        {
            _href = href;
        }

        public String getName ()
        {
            return _name;
        }

        public void setName (String name)
        {
            _name = name;
        }

        public boolean isValid ()
        {
            final String    n=getName(), r=getHRef();
            return (n != null) && (n.length() > 0)
                && (r != null) && (r.length() > 0);
        }

        public FolderMsgCounts getCounts ()
        {
            return _counts;
        }

        public FolderFlags getFlags ()
        {
            return _flags;
        }
    }
    /**
     * Handler interface for extracting folder information
     */
    public static interface FolderPropHandler {
        /**
         * Updates the specific property using the node information
         * @param infoNode root node of the specific property
         * @param finfo folder information to be updated
         * @return TRUE if successful
         */
        boolean updateInfo (final Node infoNode, final FolderInfo finfo);
    }
    /**
     * Helper base class for folder properties handlers
     */
    public static abstract class AbstractFolderPropHandler implements FolderPropHandler {
        protected AbstractFolderPropHandler ()
        {
            super();
        }
        /**
         * Called by the default implementation once a non-empty string has been found
         * @param nodeVal extract string value
         * @param finfo folder info object to be updated
         * @return TRUE if successful
         */
        protected abstract boolean updateInfo (final String nodeVal, final FolderInfo finfo);
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderPropHandler#updateInfo(org.w3c.dom.Node, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderInfo)
         */
        @Override
        public boolean updateInfo (final Node infoNode, final FolderInfo finfo)
        {
            final String    nodeVal=HotmailProtocol.getNodeValue(infoNode);
            if ((nodeVal != null) && (nodeVal.length() > 0))
                return updateInfo(nodeVal, finfo);

            return false;
        }
    }
    /**
     * Handler for the folder display name
     */
    public static final class FolderDisplayNameHandler extends AbstractFolderPropHandler {
        public FolderDisplayNameHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.AbstractFolderPropHandler#updateInfo(java.lang.String, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderInfo)
         */
        @Override
        protected boolean updateInfo (final String nodeVal, final FolderInfo finfo)
        {
            if (finfo != null)
                finfo.setName(nodeVal);

            return (finfo != null);
        }
    }
    // handler for the folder reference
    public static final class FolderHRefHandler extends AbstractFolderPropHandler {
        public FolderHRefHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.AbstractFolderPropHandler#updateInfo(java.lang.String, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderInfo)
         */
        @Override
        protected boolean updateInfo (final String nodeVal, final FolderInfo finfo)
        {
            if (finfo != null)
                finfo.setHRef(nodeVal);

            return (finfo != null);
        }
    }
    /**
     * Handler for the folder message counts
     */
    public static abstract class FolderCountsHandler extends AbstractFolderPropHandler {
        protected FolderCountsHandler ()
        {
            super();
        }
        /**
         * Called to update a folder count value
         * @param nValue count value to be updated
         * @param msgCounts message counts object to be updated
         */
        protected abstract void updateCount (final int nValue, final FolderMsgCounts msgCounts);
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.AbstractFolderPropHandler#updateInfo(java.lang.String, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderInfo)
         */
        @Override
        protected boolean updateInfo (final String nodeVal, final FolderInfo finfo)
        {
            final FolderMsgCounts    msgCounts=(null == finfo) ? null : finfo.getCounts();
            if (msgCounts != null)
            {
                try
                {
                    updateCount(Integer.parseInt(nodeVal), msgCounts);
                }
                catch(NumberFormatException nfe)
                {
                    return false;
                }
            }

            return (msgCounts != null);
        }
    }
    // handler for unread count
    public static final class FolderUnreadCountHandler extends FolderCountsHandler {
        public FolderUnreadCountHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderCountsHandler#updateCount(int, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderMsgCounts)
         */
        @Override
        protected void updateCount (final int nValue, final FolderMsgCounts msgCounts)
        {
            msgCounts.setUnreadMsgs(nValue);
        }
    }
    // handler for the folder visible/total count
    public static final class FolderVisibleCountHandler extends FolderCountsHandler {
        public FolderVisibleCountHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderCountsHandler#updateCount(int, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderMsgCounts)
         */
        @Override
        protected void updateCount (final int nValue, final FolderMsgCounts msgCounts)
        {
            msgCounts.setTotalMsgs(nValue);
        }
    }
    /**
     * Handler for the folder flags
     */
    public static abstract class FolderFlagsHandler extends AbstractFolderPropHandler {
        protected FolderFlagsHandler ()
        {
            super();
        }
        /**
         * Called to update a flag
         * @param val flag value
         * @param flags flags object to be updated
         */
        protected abstract void updateFlag (final boolean val, final FolderFlags flags);
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.AbstractFolderPropHandler#updateInfo(java.lang.String, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderInfo)
         */
        @Override
        protected boolean updateInfo (final String nodeVal, final FolderInfo finfo)
        {
            final FolderFlags    flags=(null == finfo) ? null : finfo.getFlags();
            if (flags != null)
            {
                try
                {
                    updateFlag(Integer.parseInt(nodeVal) != 0, flags);
                }
                catch(NumberFormatException nfe)
                {
                    return false;
                }
            }

            return (flags != null);
        }
    }
    /**
     * Has sub-folders flag handler
     */
    public static final class FolderHasSubsFlagHandler extends FolderFlagsHandler {
        public FolderHasSubsFlagHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderFlagsHandler#updateFlag(boolean, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderFlags)
         */
        @Override
        protected void updateFlag (final boolean val, final FolderFlags flags)
        {
            flags.setSubFoldersAccessAvailable(val);
        }
    }
    /**
     * Sub-folders creation flag handler
     */
    public static final class FolderNoSubsFlagHandler extends FolderFlagsHandler {
        public FolderNoSubsFlagHandler ()
        {
            super();
        }
        /*
         * @see net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderFlagsHandler#updateFlag(boolean, net.community.chest.net.proto.text.http.hotmail.FolderHandler.FolderFlags)
         */
        @Override
        protected void updateFlag (final boolean val, final FolderFlags flags)
        {
            flags.setSubFoldersCreationAllowed(!val);
        }
    }
    // XML properties of interest for folders
    private static final String    IsFolderXmlProp="isfolder",
        FolderDisplayNameXmlProp="displayname",
        // sometimes holds the display name
        SpecialFolderXmlProp="special",
        // folder has sub-folders
        FolderHasSubsXmlProp="hassubs",
        // folder allowed to have sub-folders
        FolderHasNoSubsXmlProp="nosubs",
        FolderUnreadCountXmlProp="unreadcount",
        FolderVisibleCountXmlProp="visiblecount";
    /**
     * Handlers {@link Map} for folder properties - key=property name, value=handler object
     */
    private static final Map<String,FolderPropHandler>    folderPropHandlersMap=new TreeMap<String,FolderPropHandler>(String.CASE_INSENSITIVE_ORDER);
    static {
        final Object[] folderProps={
            "href",                     new FolderHRefHandler(),
            FolderDisplayNameXmlProp,    new FolderDisplayNameHandler(),
            SpecialFolderXmlProp,        new FolderDisplayNameHandler(),
            FolderHasSubsXmlProp,        new FolderHasSubsFlagHandler(),
            FolderHasNoSubsXmlProp,        new FolderNoSubsFlagHandler(),
            FolderUnreadCountXmlProp,    new FolderUnreadCountHandler(),
            FolderVisibleCountXmlProp,    new FolderVisibleCountHandler()
        };

        for (int    i=0; i < folderProps.length; i+= 2)
            folderPropHandlersMap.put((String) folderProps[i], (FolderPropHandler) folderProps[i+1]);
    }
    /**
     * Traverses the folder nodes looking for specific properties
     * @param fldrNodes Folder {@link NodeList} to be traversed
     * @param finfo The {@link FolderInfo} object to be updated
     * @return Initialized {@link FolderInfo} object - null if error or
     * no instance provided to start with
     */
    public static final FolderInfo updateFolderInfo (final NodeList fldrNodes, final FolderInfo finfo)
    {
        if ((null == fldrNodes) || (null == finfo))
            return finfo;

        final int    numNodes=fldrNodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    nd=fldrNodes.item(nIndex);
            if (null == nd)    // should not happen
                continue;

            final FolderPropHandler    handler=folderPropHandlersMap.get(nd.getLocalName());
            if (handler != null)
            {
                if (!handler.updateInfo(nd, finfo))
                    return null;
            }
            else if (null == updateFolderInfo(nd.getChildNodes(), finfo))
                return null;
        }

        return finfo;
    }
    /**
     * Extracts folder info given the root DOM element of the XML response for the folder
     * @param nd DOM root element for the folder
     * @return folder information - null if unable to extract a valid folder object
     */
    public static final FolderInfo extractFolderInfo (final Node nd)
    {
        final FolderInfo    finfo=updateFolderInfo(nd.getChildNodes(), new FolderInfo());
        if ((null == finfo) || (!finfo.isValid()))
            return null;

        return finfo;
    }
    /**
     * Extracts the folders from the given document nodes
     * @param docNodes document nodes to be checked
     * @param folders The {@link Collection} of {@link FolderInfo}-s to be updated
     * @return TRUE if successful
     */
    public static final boolean extractFoldersInfo (final NodeList docNodes, final Collection<FolderInfo> folders)
    {
        if ((null == docNodes) || (null == folders))
            return false;

        final int    numNodes=docNodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    nd=docNodes.item(nIndex);
            if (null == nd)    // should not happen
                continue;

            final String nodeName=nd.getLocalName();    // "response" node is the root of a new folder entry
            if ((nodeName != null) && (nodeName.length() > 0) && nodeName.equalsIgnoreCase("response"))
            {
                final FolderInfo    finfo=extractFolderInfo(nd);
                if (finfo != null)    // should not happen
                    folders.add(finfo);
                continue;
            }

            if (nd.hasChildNodes())
            {
                if (!extractFoldersInfo(nd.getChildNodes(), folders))
                    return false;
            }
        }

        return true;
    }
    /**
     * Extracts the folders information from returned document
     * @param rspDoc response document
     * @return folders information
     * @throws IllegalStateException if unable to extract information
     */
    public static final Collection<FolderInfo> extractFoldersInfo (final Document rspDoc) throws IllegalStateException
    {
        final Collection<FolderInfo>    folders=new LinkedList<FolderInfo>();
        if (!extractFoldersInfo((null == rspDoc) ? null : rspDoc.getChildNodes(), folders))
            throw new IllegalStateException("Cannot extract folders information");

        final int    numFolders=folders.size();
        if (numFolders <= 0)
            throw new IllegalStateException("No folders extracted");

        return folders;
    }
    /**
     * XML payload for enumerating folders
     */
    public static final String    FOLDERS_ENUM_XML_PAYLOAD=
        "<?xml version='1.0'?>\r\n"
            + "<D:propfind xmlns:D='DAV:' xmlns:hm='" + HotmailProtocol.HOTMAIL_XML_NAMESPACE + "'>\r\n"
                + "\t<D:prop>\r\n"
                    + "\t\t<D:" + IsFolderXmlProp + "/>\r\n"
                    + "\t\t<D:" + FolderDisplayNameXmlProp + "/>\r\n"
                    + "\t\t<hm:" + SpecialFolderXmlProp + "/>\r\n"
                    + "\t\t<D:" + FolderHasSubsXmlProp + "/>\r\n"
                    + "\t\t<D:" + FolderHasNoSubsXmlProp + "/>\r\n"
                    + "\t\t<hm:" + FolderUnreadCountXmlProp + "/>\r\n"
                    + "\t\t<D:" + FolderVisibleCountXmlProp + "/>\r\n"
                + "\t</D:prop>\r\n"
            + "</D:propfind>"
    ;

}
