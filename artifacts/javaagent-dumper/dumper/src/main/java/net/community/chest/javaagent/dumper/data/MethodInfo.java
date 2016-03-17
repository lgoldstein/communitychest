/*
 *
 */
package net.community.chest.javaagent.dumper.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
 * @since Aug 11, 2011 8:39:44 AM
 */
public class MethodInfo extends AbstractInfo implements Comparable<MethodInfo>, XmlConvertible<MethodInfo> {
    private static final long serialVersionUID = -8354320382335637190L;

    public MethodInfo ()
    {
        super();
    }

    public MethodInfo (Element root) throws Exception
    {
        final MethodInfo    info=fromXml(root);
        if (this != info)
            throw new IllegalStateException("Mismatched re-constructed instances");
    }

    public final boolean isConstructor ()
    {
        final String    name=getName();
        if ((name != null) && (name.length() > 2)
         && (name.charAt(0) == '<')
         && (name.charAt(name.length() - 1) == '>'))
            return true;
        else
            return false;
    }

    private List<ParamInfo>    _parameters;
    public List<ParamInfo> getParameters ()
    {
        return _parameters;
    }

    public void setParameters (List<ParamInfo> parameters)
    {
        _parameters = parameters;
    }

    public boolean isWithParameters ()
    {
        final Collection<?>    params=getParameters();
        return (params != null) && (params.size() > 0);
    }

    private String    _returnType;
    public String getReturnType ()
    {
        return _returnType;
    }

    public void setReturnType (String returnType)
    {
        _returnType = returnType;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=DOMUtils.addNonEmptyAttribute(
                doc.createElement(InfoUtils.METHOD_ELEMENT), InfoUtils.NAME_ATTR, InfoUtils.encodeMethodName(getName()));
        InfoUtils.appendMethodModifiers(elem, getModifiers());
        DOMUtils.addNonEmptyAttribute(elem, InfoUtils.RETURN_TYPE_ATTR, getReturnType());

        final Collection<? extends ParamInfo>    params=getParameters();
        if ((params != null) && (params.size() > 0))
        {
            for (final ParamInfo pInfo : params)
                elem.appendChild(pInfo.toXml(doc));
        }

        return elem;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public MethodInfo fromXml (Element root) throws Exception
    {
        setName(root);
        setReturnType(root);
        setModifiers(root);
        setParameters(root);

        return this;
    }

    protected String setName (Element elem)
    {
        final String    name=InfoUtils.decodeMethodName(elem.getAttribute(InfoUtils.NAME_ATTR));
        if ((name != null) && (name.length() > 0))
            setName(name);
        return name;
    }

    protected String setReturnType (Element elem)
    {
        final String    type=elem.getAttribute(InfoUtils.RETURN_TYPE_ATTR);
        if ((type != null) && (type.length() > 0))
            setReturnType(type);
        return type;
    }

    protected int setModifiers (Element elem)
    {
        final int    mod=InfoUtils.getModifiers(elem);
        if (mod != 0)
            setModifiers(mod);
        return mod;
    }

    protected List<ParamInfo> setParameters (Element root) throws Exception
    {
        return setParameters(root.getElementsByTagName(InfoUtils.PARAM_ELEMENT));
    }

    protected List<ParamInfo> setParameters (NodeList list) throws Exception
    {
        final int    numNodes=(list == null) ? 0 : list.getLength();
        if (numNodes <= 0)
            return Collections.emptyList();

        final List<ParamInfo>    params=new ArrayList<ParamInfo>(numNodes);
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
            params.add(new ParamInfo((Element) list.item(nIndex)));

        setParameters(params);
        return params;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final Collection<?>    params=getParameters();
        int                    hashValue=StringUtil.getDataStringHashCode(getName(), true)
                                    + StringUtil.getDataStringHashCode(getReturnType(), true)
                                    + getModifiers()
                                    ;
        if ((params == null) || params.isEmpty())
            return hashValue;

        for (final Object pInfo : params)
            hashValue += pInfo.hashCode();

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
        if (!(obj instanceof MethodInfo))
            return false;

        if (compareTo((MethodInfo) obj) != 0)
            return false;    // debug breakpoint;

        return true;
    }
    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (MethodInfo other)
    {
        if (other == null)
            return (-1);
        if (this == other)
            return 0;

        int    nRes=StringUtil.compareDataStrings(getName(), other.getName(), true);
        if (nRes != 0)
            return nRes;

        final List<? extends ParamInfo>    tParams=getParameters(),
                                        oParams=other.getParameters();
        final int                        tNum=(tParams == null) ? 0 : tParams.size(),
                                        oNum=(oParams == null) ? 0 : oParams.size();
        if (tNum != oNum)    // less parameters comes first
            return tNum - oNum;

        for (int    pIndex=0; pIndex < tNum; pIndex++)
        {
            final ParamInfo    tInfo=tParams.get(pIndex), oInfo=oParams.get(pIndex);
            if ((nRes=tInfo.compareTo(oInfo)) != 0)
                return nRes;
        }

        if ((nRes=StringUtil.compareDataStrings(getReturnType(), other.getReturnType(), true)) != 0)
            return nRes;

        final int    tMods=getModifiers(), oMods=other.getModifiers();
        if ((nRes=Visibility.compareVisibility(tMods, oMods)) != 0)
            return nRes;

        // if all else fails, compare the raw modifiers values
        if ((nRes=(tMods - oMods)) != 0)
            return nRes;

        return 0;
    }

    public <A extends Appendable> A append (A sb) throws IOException
    {
        final Collection<? extends ParamInfo>    params=getParameters();
        final int                                numParams=(params == null) ? 0 : params.size();
        InfoUtils.startMethod(sb, getName());
        InfoUtils.appendMethodModifiers(sb, getModifiers());
        InfoUtils.appendReturnTypeAttribute(sb, getReturnType());
        if (numParams > 0)
        {
            InfoUtils.println(sb.append(" >"));
            for (final ParamInfo pInfo : params)
                InfoUtils.println(pInfo.append(sb.append('\t')));
            InfoUtils.endMethod(sb, true);
        }
        else
            InfoUtils.endMethod(sb, false);
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
            return append(new StringBuilder(Byte.MAX_VALUE)).toString();
        }
        catch(IOException e)    // unexpected
        {
            return e.getMessage();
        }
    }
}
