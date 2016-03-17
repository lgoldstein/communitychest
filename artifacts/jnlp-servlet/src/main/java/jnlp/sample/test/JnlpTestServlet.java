/*
 *
 */
package jnlp.sample.test;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import jnlp.sample.servlet.JnlpDownloadServlet;
import jnlp.sample.servlet.JnlpResource;
import jnlp.sample.servlet.ResourceCatalog;
import jnlp.sample.servlet.download.DownloadRequest;
import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;
import net.community.chest.io.jar.JarUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 23, 2009 10:33:31 AM
 */
public class JnlpTestServlet extends JnlpDownloadServlet {
    private transient Logger _log;

    public JnlpTestServlet ()
    {
        super();
    }

    private String    _repositoryRoot    /* =null */;
    public synchronized String getRepositoryRoot ()
    {
        if ((null == _repositoryRoot)|| (_repositoryRoot.length() <= 0))
        {
            if ((null == (_repositoryRoot=System.getenv("M2_REPO")))
             || (_repositoryRoot.length() <= 0))
                _repositoryRoot = System.getProperty("jnlp.sample.test.repository.root");
        }

        return _repositoryRoot;
    }

    private String    _communityHome    /* null */;
    public synchronized String getCommunityHome ()
    {
        if (null == _communityHome)
            _communityHome = System.getenv("CHEST_HOME");
        return _communityHome;
    }

    private String getAdjustedRequestPath (final String reqPath)
    {
        final String    srvltName=getServletName();
        final int        reqLen=(null == reqPath) ? 0 : reqPath.length(),
                        snLen=(null == srvltName) ? 0 : srvltName.length();
        if ((snLen <= 0) || (reqLen <= snLen))
            return reqPath;

        final int        firstCompOffset=(reqPath.charAt(0) == '/') ? 1 : 0,
                        nextCompOffset=snLen + firstCompOffset;
        final String    firstComp=reqPath.substring(firstCompOffset, nextCompOffset);
        if (!firstComp.equalsIgnoreCase(srvltName))
            return reqPath;

        if ((reqLen > nextCompOffset)
         && (reqPath.charAt(nextCompOffset) == '/'))
            return reqPath.substring(nextCompOffset);

        return reqPath;
    }

    private File _sgnDir;
    private synchronized File getSignedFilesFolder ()
    {
        if (null == _sgnDir)
        {
            final String    ch=getCommunityHome();
            _sgnDir = new File(ch, "signed");
        }

        if (!_sgnDir.exists())
        {
            if (!_sgnDir.mkdirs())
                _log.warn("failed to created signed files folder=" + _sgnDir);
        }

        return _sgnDir;
    }

    private File signFile (final File fin)
    {
        if (!JarUtils.isJarFile(fin))
            return fin;

        final String    n=fin.getName();
        final File        d=getSignedFilesFolder(), fout=new File(d, n);
        if (fout.exists())
        {
            _log.info("signFile(" + fin + ") signed " + fout);
            return fout;
        }

        return fin;
    }

    private File getFile (final String root, final String relPath)
    {
        final int    rLen=(null == root) ? 0 : root.length(),
                    pLen=(null == relPath) ? 0 : relPath.length();
        if ((rLen <= 0) || (pLen <= 0))
            return null;

        final String    subPath=
            ('/' == File.separatorChar) ? relPath : relPath.replace('/', File.separatorChar);
        final char        rch=root.charAt(rLen - 1), pch=subPath.charAt(0);
        final String    resPath;
        if (rch == File.separatorChar)
        {
            if (pch == File.separatorChar)
                resPath = root + subPath.substring(1);
            else
                resPath = root + subPath;
        }
        else
        {
            if (pch == File.separatorChar)
                resPath = root + subPath;
            else
                resPath = root + File.separator + subPath;
        }

        return signFile(new File(resPath));
    }

    protected File getRepositoryLocation (final String reqPath)
    {
        final String    relPath=getAdjustedRequestPath(reqPath);
        File    f=getFile(getCommunityHome(), relPath);
        if ((f != null) && f.exists())
            return f;

        if ((relPath != null) && relPath.startsWith("/lib"))
            return null;

        final int    sPos=relPath.lastIndexOf('/'),
                    ePos=relPath.lastIndexOf('.');
        if ((sPos <= 0) || (ePos <= 0) || (ePos <= sPos))
            return null;

        return getFile(getRepositoryRoot(), relPath);
    }

    protected URL getRepositoryResource (final String reqPath) throws MalformedURLException
    {
        final File    f=getRepositoryLocation(reqPath);
        if (f != null)
        {
            final URL        fileURL=f.toURI().toURL();
            final Logger    l=LoggerFactory.getLogger(JnlpTestServlet.class);
            if (l.isDebugLevel())
                l.debug("getRepositoryResource(" + reqPath + ") => " + fileURL);
            return fileURL;
        }

        return null;
    }

    private class CatalogOverride extends ResourceCatalog {
        public CatalogOverride (ServletContext servletContext)
        {
            super(servletContext);
        }
        /*
         * @see jnlp.sample.servlet.ResourceCatalog#lookupDirect(jnlp.sample.servlet.download.DownloadRequest, java.lang.String, jnlp.sample.servlet.JnlpResource[])
         */
        @Override
        protected int lookupDirect (DownloadRequest dreq, String reqVersion, JnlpResource[] result)
        {
            if ((null == reqVersion) || (reqVersion.length() <= 0))
                return super.lookupDirect(dreq, reqVersion, result);

            final String     path=dreq.getPath();
            final int        idx=(null == path) ? (-1) : path.lastIndexOf('.');
            if (idx < 0)    // if no extension then assume no resource
                return DownloadResponse.ERR_10_NO_RESOURCE;

            final String        subPath=path.substring(0, idx),    // excluding the '.;
                                name=path.substring(path.lastIndexOf('/') + 1, idx),
                                ext=path.substring(idx),    // including the '.'
                                newPath=subPath + "/" + reqVersion + "/" + name + "-" + reqVersion + ext;
            final JnlpResource    res=
                new JnlpResource(getServletContext(), name, reqVersion, dreq.getOS(), dreq.getArch(), dreq.getLocale(), newPath, reqVersion);
            if (!res.exists())
                return super.lookupDirect(dreq, reqVersion, result);

            if (_log.isDebugLevel())
                _log.debug("lookupDirect(" + path + ")[" + reqVersion + "] => " + res);

            result[0] = res;
            return DownloadResponse.STS_00_OK;
        }
    }
    /*
     * @see jnlp.sample.servlet.JnlpDownloadServlet#getResourceCatalog(boolean)
     */
    @Override
    protected synchronized ResourceCatalog getResourceCatalog (boolean createIfNotExist)
    {
        ResourceCatalog    c=super.getResourceCatalog(false);
        if ((null == c) && createIfNotExist)
        {
            c = new CatalogOverride(getServletContext());
            setResourceCatalog(c);
        }

        return c;
    }

    private class ContextEmbedder implements InvocationHandler {
        private final ServletContext    _realContext;
        public ContextEmbedder (final ServletContext ctx)
        {
            if (null == (_realContext=ctx))
                throw new IllegalArgumentException("No real " + ServletContext.class.getSimpleName() + " instance provided");
        }
        /*
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke (Object proxy, Method method, Object[] args) throws Throwable
        {
            if ((args != null) && (args.length == 1) && (args[0] instanceof String))
            {
                final String    n=method.getName(), argVal=args[0].toString();
                if ("getResource".equalsIgnoreCase(n))
                {
                    final URL    resURL=getRepositoryResource(argVal);
                    if (resURL != null)
                        return resURL;
                }
                else if ("getRealPath".equalsIgnoreCase(n))
                {
                    final File    f=getRepositoryLocation(argVal);
                    if (f != null)
                        return f.getAbsolutePath();
                }
            }

            return method.invoke(_realContext, args);
        }
    }

    private ServletContext    _context;
    /*
     * @see javax.servlet.GenericServlet#getServletContext()
     */
    @Override
    public synchronized ServletContext getServletContext ()
    {
        if (null == _context)
        {
            final Class<?>[]        ifc={ ServletContext.class };
            final ServletContext    ctx=super.getServletContext();
            _context = (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(), ifc, new ContextEmbedder(ctx));
        }

        return _context;
    }

    /*
     * @see jnlp.sample.servlet.JnlpDownloadServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init (ServletConfig config) throws ServletException
    {
        super.init(config);

        _log = LoggerFactory.getLogger(getClass());
    }
}
