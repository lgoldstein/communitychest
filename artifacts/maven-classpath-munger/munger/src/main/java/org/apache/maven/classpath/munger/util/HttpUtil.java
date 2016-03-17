/*
 * Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.classpath.munger.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Dec 25, 2013 8:35:40 AM
 */
public final class HttpUtil {
    private HttpUtil() {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * A {@link HostnameVerifier} that accepts all incoming hosts
     */
    public static final HostnameVerifier    ACCEPT_ALL_HOSTNAME_VERIFIER=
            new HostnameVerifier()  {
                @Override
                public boolean verify(String hostname, SSLSession session)   {
                    return true;
                }
            };

    public static final long downloadDataStream(String url, File target) throws IOException {
        return downloadDataStream(url, Collections.<String,String>emptyMap(), target);
    }

    /**
     * @param url The URL to retrieve the data from using &quot;GET&quot;
     * @param Request headers {@link Map} - ignored if {@code null} or empty
     * @param target The target {@link File} to which to write the data. If
     * the file does not exist, then the hierarchy up to it is automatically
     * created
     * @return Number of written bytes
     * @throws IOException If failed to access or write the data, or if the
     * target file exists but it is a directory
     */
    public static final long downloadDataStream(String url, Map<String,?> reqHeaders, File target) throws IOException {
        if (target.exists()) {
            if (target.isDirectory()) {
                throw new IOException("downloadDataStream(" + url + ") target is an existing directory: " + target.getAbsolutePath());
            }
        } else {
            File    parent=target.getParentFile();
            if ((!parent.exists()) && (!parent.mkdirs())) {
                throw new IOException("downloadDataStream(" + url + ") failed to create hierarchy of " + target.getAbsolutePath());
            }
        }

        OutputStream    output=new FileOutputStream(target);
        try {
            return downloadDataStream(url, reqHeaders, output);
        } finally {
            output.close();
        }
    }

    public static final long downloadDataStream(String url, OutputStream output) throws IOException {
        return downloadDataStream(url, Collections.<String,String>emptyMap(), output);
    }

    /**
     * @param url The URL to retrieve the data from using &quot;GET&quot;
     * @param Request headers {@link Map} - ignored if {@code null} or empty
     * @param output The {@link OutputStream} to which to write the data
     * @return Number of written bytes
     * @throws IOException If failed to access or write the data
     * @see #getDataStream(String, Map)
     */
    public static final long downloadDataStream(String url, Map<String,?> reqHeaders, OutputStream output) throws IOException {
        InputStream input=getDataStream(url, reqHeaders);
        if (input == null) {
            return 0L;
        }

        try {
            return copyData(input, output);
        } finally {
            input.close();
        }
    }

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final long copyData(InputStream input, OutputStream output) throws IOException {
        byte[]  buffer=new byte[DEFAULT_BUFFER_SIZE];
        long    count=0L;
        int     n=0;
        while ((n=input.read(buffer)) != (-1)) {
            output.write(buffer, 0, n);
            count += n;
        }

        return count;
    }

    public static final InputStream getDataStream(String url) throws IOException {
        return getDataStream(url, Collections.<String,String>emptyMap());
    }

    /**
     * @param url The URL to retrieve the data from using &quot;GET&quot;
     * @param Request headers {@link Map} - ignored if {@code null} or empty
     * @return An {@link InputStream} to read the data from - {@code null}
     * if &quot;File not found&quot; error (404) returned
     * @throws IOException If failed to retrieve the data
     * @see #wrapStream(HttpURLConnection)
     */
    public static final InputStream getDataStream(String url, Map<String,?> reqHeaders) throws IOException {
        HttpURLConnection   conn=openConnection(url, "GET", reqHeaders);
        int statusCode=conn.getResponseCode();
        if ((statusCode < 200) || (statusCode >= 300)) {
            if (statusCode == 404) {    // special code for "resource not found"
                return null;
            }

            String    rspMsg=conn.getResponseMessage();
            throw new IOException("getDataStream(" + url + ") failed (" + statusCode + "): " + rspMsg);
        }

        InputStream dataStream=wrapStream(conn);
        if (dataStream == null) {
            return EmptyInputStream.INSTANCE;
        } else {
            return dataStream;
        }
    }

    /**
     * @param url The {@link URL} to be used to create the connection
     * @param method The access method (e.g., &quot;GET&quot;, &quot;POST&quot;)
     * @return An initialized {@link HttpURLConnection}.
     * @throws IOException If unable to setup the connection
     * @see #openConnection(String, String, Map)
     */
    public static final HttpURLConnection openConnection(String url, String method) throws IOException {
        return openConnection(url, method, Collections.<String,String>emptyMap());
    }

    /**
     * @param url The {@link URL} to be used to create the connection
     * @param method The access method (e.g., &quot;GET&quot;, &quot;POST&quot;)
     * @param reqHeaders Request headers {@link Map} - ignored if {@code null}
     * or empty
     * @return An initialized {@link HttpURLConnection}.</BR>
     * <P><B>Note(s):</B></P></BR>
     * <P>
     *  <OL>
     *      <LI><P>
     *      If the HTTPS protocol is specified then <U>no certificate validation</U>
     *      is executed - i.e., the connection is set up to trust all hosts and
     *      certificates presented to it
     *      </LI></P></BR>
     *
     *      <LI><P>
     *      If a &quot;POST&quot; method is used then the caller must further set up
     *      the connection by specifying that it is going to output data + provide it:</BR>
     *          <P>
     *              <PRE>
     *              HttpURLConnection   conn=openConnection(...);
     *              conn.setDoOutput(true);
     *
     *              OutputStream    outData=conn.getOutputStream();
     *              try {
     *                  ...write data to be posted...
     *              } finally {
     *                  outData.close();
     *              }
     *              </PRE>
     *          </P>
     *      </LI></P></BR>
     *  </OL>
     * </P>
     * @throws IOException If unable to setup the connection
     */
    public static final HttpURLConnection openConnection(String url, String method, Map<String,?> reqHeaders) throws IOException {
        HttpURLConnection   conn=(HttpURLConnection) new URL(url).openConnection();
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection    https=(HttpsURLConnection) conn;
            https.setHostnameVerifier(ACCEPT_ALL_HOSTNAME_VERIFIER);
            https.setSSLSocketFactory(getAcceptAllSSLFactory());
        }

        conn.setConnectTimeout(10 * 1000);  // TODO use some configurable value
        conn.setReadTimeout(30 * 1000);  // TODO use some configurable value
        conn.setRequestMethod(method);

        if ((reqHeaders != null) && (reqHeaders.size() > 0)) {
            for (Map.Entry<String,?> re : reqHeaders.entrySet()) {
                String  name=re.getKey();
                Object  value=re.getValue();
                conn.setRequestProperty(name, value.toString());
            }
        }

        return conn;
    }

    /**
     * A {@link X509TrustManager} that does not validate certificate chains
     */
    public static final X509TrustManager    TRUST_ALL_CERTS_MANAGER=
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType ) {
                    // do nothing
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType ) {
                    // do nothing
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

    /**
     * @return An {@link SSLSocketFactory} that accepts all certificates and hosts
     * @see #TRUST_ALL_CERTS_MANAGER
     */
    @SuppressWarnings("synthetic-access")
    public static final SSLSocketFactory getAcceptAllSSLFactory() {
        return SSLSocketFactoryHolder.ACCEPT_ALL_FACTORY_INSTANCE;
    }

    private static final class SSLSocketFactoryHolder {
        private static final SSLSocketFactory   ACCEPT_ALL_FACTORY_INSTANCE=createAcceptAllSSLFactory();
        private static final SSLSocketFactory createAcceptAllSSLFactory() {
            try {
                SSLContext sslContext=SSLContext.getInstance("SSL");
                // Install the all-trusting trust manager
                sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS_MANAGER }, new SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                return sslContext.getSocketFactory();
            } catch(GeneralSecurityException e) {
                throw new IllegalStateException("Failed (" + e.getClass().getSimpleName() + ") to create SSL socket factory: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param connection The {@link HttpURLConnection} after the response code
     * has been examined
     * @return The response {@link InputStream} after checking if the content encoding
     * is ZIP or GZIP. If so, then the original stream is wrapped inside the relevant
     * inflater
     * @throws IOException if failed to access the connection or find a suitable
     * wrapper (e.g., unsupported encoding specified)
     */
    public static final InputStream wrapStream(HttpURLConnection connection) throws IOException {
        InputStream inputStream=connection.getInputStream();
        String      encoding=connection.getContentEncoding();
        if (PropertiesUtil.isEmpty(encoding) || (inputStream == null)) {
            return inputStream;
        } else if ("gzip".equalsIgnoreCase(encoding)) {
            return new GZIPInputStream(inputStream);
        } else if ("zip".equalsIgnoreCase(encoding)) {
            return new ZipInputStream(inputStream);
        } else {
            throw new StreamCorruptedException("Unexpected Content-Encoding: " + encoding);
        }
    }
}
