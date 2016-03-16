package net.community.chest.rrd4j.common.core;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 2:46:10 PM
 */
public class RrdDbExt extends RrdDb {
    public RrdDbExt (RrdDef rrdDef, RrdBackendFactory factory) throws IOException
    {
        super(rrdDef, factory);
    }

    public RrdDbExt (RrdDef rrdDef) throws IOException
    {
        this(rrdDef, RrdBackendFactory.getDefaultFactory());
    }


    public RrdDbExt (Element elem) throws Exception
    {
        this(new RrdDefExt(elem), RrdBackendFactoryExt.resolveFactory(elem));
    }

    public RrdDbExt (String path, boolean readOnly, RrdBackendFactory factory) throws IOException
    {
        super(path, readOnly, factory);
    }

    public static final String    PATH_ATTR="path";
    public static final String getPath (Element elem)
    {
        return (null == elem) ? null : elem.getAttribute(PATH_ATTR);
    }

    public static final String    RDONLY_ATTR="readOnly";
    public static final boolean resolveReadOnly (Element elem, boolean readOnlyDefault) throws Exception
    {
        final String    val=elem.getAttribute(RDONLY_ATTR);
        if ((null == val) || (val.length() <= 0))
            return readOnlyDefault;

        return Boolean.parseBoolean(val);
    }

    public RrdDbExt (Element elem, boolean readOnlyDefault) throws Exception
    {
        this(getPath(elem), resolveReadOnly(elem, readOnlyDefault), RrdBackendFactoryExt.resolveFactory(elem));
    }

    public RrdDbExt (String path, boolean readOnly) throws IOException
    {
        this(path, readOnly, RrdBackendFactory.getDefaultFactory());
    }

    public RrdDbExt (String path) throws IOException
    {
        this(path, false);
    }

    public RrdDbExt (String path, RrdBackendFactory factory) throws IOException
    {
        this(path, false, factory);
    }

    public RrdDbExt (String rrdPath, String externalPath, RrdBackendFactory factory) throws IOException
    {
        super(rrdPath, externalPath, factory);
    }

    public RrdDbExt (String rrdPath, String externalPath) throws IOException
    {
        super(rrdPath, externalPath);
    }

    private Map<String,Integer>    _dsMap    /* =null */;
    /**
     * @return A {@link Map} where key=datasource name, value=data source
     * index (can be used as input {@link #getDatasource(int)}). This map
     * can also be used to populate a {@link org.rrd4j.core.Sample} in the
     * same order as the data source
     * @throws IOException if cannot access the datasources data
     */
    public synchronized Map<String,Integer> getDatasourcesIndexMap () throws IOException
    {
        if (null == _dsMap)
        {
            final String[]    names=getDsNames();
            if ((null == names) || (names.length <= 0))
                return null;

            _dsMap = new TreeMap<String,Integer>();
            for (int    dsIndex=0; dsIndex < names.length; dsIndex++)
                _dsMap.put(names[dsIndex], Integer.valueOf(dsIndex));
        }

        return _dsMap;
    }
}
