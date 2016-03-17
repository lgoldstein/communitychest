/*
 *
 */
package net.community.chest.javaagent.dumper.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.Visibility;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 8:35:40 AM
 */
public class ClassInfo extends AbstractInfo implements Comparable<ClassInfo>, XmlConvertible<ClassInfo> {
    private static final long serialVersionUID = -1645084622105633275L;

    public ClassInfo ()
    {
        super();
    }

    public ClassInfo (Document doc) throws Exception
    {
        this(doc.getDocumentElement());
    }

    public ClassInfo (Element root) throws Exception
    {
        final ClassInfo    info=fromXml(root);
        if (this != info)
            throw new IllegalStateException("Mismatched re-constructed instances");
    }

    private String    _location;
    public String getLocation ()
    {
        return _location;
    }

    public void setLocation (String location)
    {
        _location = location;
    }

    private Collection<MethodInfo>    _methods;
    public Collection<MethodInfo> getMethods ()
    {
        return _methods;
    }

    public void setMethods (Collection<MethodInfo> methods)
    {
        _methods = methods;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=DOMUtils.addNonEmptyAttribute(
                doc.createElement(InfoUtils.CLASS_ELEMENT), InfoUtils.NAME_ATTR, getName());
        InfoUtils.appendMethodModifiers(elem, getModifiers());
        DOMUtils.addNonEmptyAttribute(elem, InfoUtils.LOCATION_ATTR, getLocation());

        final Collection<? extends MethodInfo>    methods=getMethods();
        if ((methods != null) && (methods.size() > 0))
        {
            for (final MethodInfo mInfo : methods)
                elem.appendChild(mInfo.toXml(doc));
        }

        return elem;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public ClassInfo fromXml (Element root) throws Exception
    {
        setName(root);
        setModifiers(root);
        setLocation(root);
        setMethods(root);

        return this;
    }

    protected String setName (Element elem)
    {
        final String    name=elem.getAttribute(InfoUtils.NAME_ATTR);
        if ((name != null) && (name.length() > 0))
            setName(name);
        return name;
    }

    protected int setModifiers (Element elem)
    {
        final int    mod=InfoUtils.getModifiers(elem);
        if (mod != 0)
            setModifiers(mod);
        return mod;
    }

    protected String setLocation (Element elem)
    {
        final String    loc=elem.getAttribute(InfoUtils.LOCATION_ATTR);
        if ((loc != null) && (loc.length() > 0))
            setLocation(loc);
        return loc;
    }

    protected Collection<MethodInfo> setMethods (Element elem) throws Exception
    {
        return setMethods(elem.getElementsByTagName(InfoUtils.METHOD_ELEMENT));
    }

    protected Collection<MethodInfo> setMethods (NodeList list) throws Exception
    {
        final int    numNodes=(list == null) ? 0 : list.getLength();
        if (numNodes <= 0)
            return Collections.emptyList();

        final Collection<MethodInfo>    methods=new ArrayList<MethodInfo>(numNodes);
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final MethodInfo    mInfo=restoreMethodInfo((Element) list.item(nIndex));
            if (mInfo == null)
                continue;    // debug breakpoint
            methods.add(mInfo);
        }

        setMethods(methods);
        return methods;
    }

    protected MethodInfo restoreMethodInfo (Element elem) throws Exception
    {
        return new MethodInfo(elem);
    }
    // since mapping is by simple name, there can be several matches
    public Map<String,Collection<MethodInfo>> getMethodsMap ()
    {
        final Collection<? extends MethodInfo>    methods=getMethods();
        if ((methods == null) || methods.isEmpty())
            return Collections.emptyMap();

        final Map<String,Collection<MethodInfo>>    methodsMap=new TreeMap<String,Collection<MethodInfo>>();
        for (final MethodInfo mInfo : methods)
        {
            final String            mName=mInfo.getName();
            Collection<MethodInfo>    mList=methodsMap.get(mName);
            if (mList == null)
            {
                mList = new LinkedList<MethodInfo>();
                methodsMap.put(mName, mList);
            }

            mList.add(mInfo);
        }

        return methodsMap;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        int                    hashValue=StringUtil.getDataStringHashCode(getName(), true)
                                    + StringUtil.getDataStringHashCode(getLocation(), true)
                                    + getModifiers()
                                    ;
        final Collection<?>    methods=getMethods();
        if ((methods == null) || methods.isEmpty())
            return hashValue;

        for (final Object mInfo : methods)
            hashValue += mInfo.hashCode();

        return hashValue;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof ClassInfo))
            return false;
        if (compareTo((ClassInfo) obj) != 0)
            return false;    // debug breakpoint

        return true;
    }

    public <A extends Appendable> A append (A sb) throws IOException
    {
        InfoUtils.println(InfoUtils.appendClassHeader(sb, getName(), getModifiers(), getLocation()));

        final Collection<? extends MethodInfo>    methods=getMethods();
        if ((methods != null) && (methods.size() > 0))
        {
            for (final MethodInfo mInfo : methods)
            {
                sb.append('\t');
                InfoUtils.println(mInfo.append(sb));
            }
        }

        InfoUtils.appendClassFooter(sb);
        return sb;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        try
        {
            return append(new StringBuilder(Byte.MAX_VALUE * 2)).toString();
        }
        catch(IOException e)    // unexpected
        {
            return e.getMessage();
        }
    }
    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (ClassInfo other)
    {
        if (other == null)
            return (-1);
        if (this == other)
            return 0;

        int    nRes=StringUtil.compareDataStrings(getName(), other.getName(), true);
        if (nRes != 0)
            return nRes;

        final Map<String,? extends Collection<? extends MethodInfo>>    tMap=getMethodsMap(),
                                                                        oMap=other.getMethodsMap();
        if ((nRes=compareMethodsMap(tMap, oMap)) != 0)
            return nRes;
        if ((nRes=compareMethodsMap(oMap, tMap)) != 0)
            return 0 - nRes;

        final int    tMods=getModifiers(), oMods=other.getModifiers();
        if ((nRes=Visibility.compareVisibility(tMods, oMods)) != 0)
            return nRes;

        // if all else fails, compare the raw modifiers values
        if ((nRes=(tMods - oMods)) != 0)
            return nRes;

        if ((nRes=StringUtil.compareDataStrings(getLocation(), other.getLocation(), true)) != 0)
            return nRes;

        return 0;
    }

    private static int compareMethodsMap (final Map<String,? extends Collection<? extends MethodInfo>> m1,
                                          final Map<String,? extends Collection<? extends MethodInfo>> m2)
    {
        final int    s1=(m1 == null) ? 0 : m1.size(), s2=(m2 == null) ? 0 : m2.size();
        int            nRes=s1 - s2;
        if (nRes != 0)
            return nRes;
        if (s1 <= 0)
            return 0;

        for (final Map.Entry<String,? extends Collection<? extends MethodInfo>> me : m1.entrySet())
        {
            final String                            mName=me.getKey();
            final Collection<? extends MethodInfo>    l1=me.getValue(), l2=m2.get(mName);
            if ((nRes=compareMethodsList(l1, l2)) != 0)
                return nRes;
            if ((nRes=compareMethodsList(l2, l1)) != 0)
                return 0 - nRes;
        }

        return 0;
    }

    private static int compareMethodsList (final Collection<? extends MethodInfo> l1,
                                           final Collection<? extends MethodInfo> l2)
    {
        final int    s1=(l1 == null) ? 0 : l1.size(), s2=(l2 == null) ? 0 : l2.size();
        int            nRes=s1 - s2;
        if (nRes != 0)
            return nRes;
        if (s1 <= 0)
            return 0;

        for (final MethodInfo    m1 : l1)
        {
            for (final MethodInfo    m2 : l2)
            {
                if ((nRes=m1.compareTo(m2)) == 0)
                    break;
            }

            // must wait till we checked all the methods to see if found a match
            if (nRes != 0)
                return (-1);
        }

        return 0;
    }
}
