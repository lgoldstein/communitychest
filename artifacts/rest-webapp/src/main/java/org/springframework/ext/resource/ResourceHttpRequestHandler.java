package org.springframework.ext.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

public class ResourceHttpRequestHandler implements HttpRequestHandler {

	private static final Log logger = LogFactory.getLog(ResourceHttpRequestHandler.class);

	private Resource resourceDirectory;
	
	private int maxAge = 31556926;
	
	private FileMediaTypeMap fileMediaTypeMap = new DefaultFileMediaTypeMap();
	
	public ResourceHttpRequestHandler (Resource resDir)
	{
		Assert.notNull(resDir, "The resource directory may not be null");
		this.resourceDirectory = resDir;
	}

	public static final String	DEFAULT_REQUEST_METHOD="GET";
	/*
	 * @see org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleRequest (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		final String	reqMethod=(null == request) ? null : request.getMethod();
		if (!DEFAULT_REQUEST_METHOD.equals(reqMethod))
		{
			throw new HttpRequestMethodNotSupportedException(reqMethod,
					new String[] {DEFAULT_REQUEST_METHOD },
					getClass().getSimpleName() + " only supports " + DEFAULT_REQUEST_METHOD + " requests");
		}

		final List<Resource> 	resources=getResources(request);
		final int				numResources=(null == resources) ? 0 : resources.size();
		if (numResources <= 0) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (checkNotModified(resources, request, response)) {
			return;
		}

		prepareResponse(resources, response);
		writeResponse(resources, response);
	}

	private List<Resource> getResources (final HttpServletRequest request)
		throws ServletException, IOException
	{
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (path == null) {
			throw new IllegalStateException("Required request attribute '" + HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");			
		}

		String[] resourceElements = path.split(",");
		if (resourceElements.length == 1 && resourceElements[0].length() == 0) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		List<Resource> resources = new ArrayList<Resource>(resourceElements.length);
		String[] dirAndFilename = splitDirectoryAndFilename(resourceElements[0]);
		String dir = dirAndFilename[0];
		String filename = dirAndFilename[1];
		Resource parent = dir != null ? this.resourceDirectory.createRelative(dir) : this.resourceDirectory;
		addResource(parent, filename, resources);
		if (resourceElements.length > 1) {
			for (int i = 1; i < resourceElements.length; i++) {
				addResource(parent, resourceElements[i], resources);
			}					
		}
		return resources;
	}
	
	private boolean checkNotModified (
			final Collection<? extends Resource> resources,
			final HttpServletRequest request,
			final HttpServletResponse response)
		throws IOException
	{
		long lastModifiedTimestamp = -1;
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");			
		for (final Resource resource : resources) {
			long resourceLastModified = resource.lastModified();
			if (resourceLastModified > lastModifiedTimestamp) {
				lastModifiedTimestamp = resourceLastModified;
			}				
		}
		boolean notModified = ifModifiedSince >= (lastModifiedTimestamp / 1000 * 1000);
		if (notModified) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			response.setDateHeader("Last-Modified", lastModifiedTimestamp);
		}
		return notModified;
	}
	
	private void prepareResponse (final Collection<? extends Resource> 	resources,
								  final HttpServletResponse 			response)
	{
		MediaType mediaType = null;
		int contentLength = 0;
		for (final Resource resource : resources) {
			try {
				File file = resource.getFile();
				if (mediaType == null) {
					mediaType = fileMediaTypeMap.getMediaType(file.getName());
				}
				contentLength += file.length();				
			} catch (IOException e) {
				// ignored
			}
		}
		if (mediaType != null) {
			response.setContentType(mediaType.toString());
		}
		response.setContentLength(contentLength);
		if (this.maxAge > 0) {
			// HTTP 1.0 header
			response.setDateHeader("Expires", System.currentTimeMillis() + this.maxAge * 1000L);
			// HTTP 1.1 header
			response.setHeader("Cache-Control", "max-age=" + this.maxAge);
		}
	}
	
	private void writeResponse (final Collection<? extends Resource> 	resources,
								final HttpServletResponse				response)
		throws IOException
	{
		for (final Resource resource : resources)
		{
			InputStream in = null;
			try
			{
				in = resource.getInputStream();

				final OutputStream	out=response.getOutputStream();
				final long			bytesCopied=IOUtils.copyLarge(in, out);
				if (bytesCopied < 0L)
					throw new StreamCorruptedException(
						"Bad number of copied bytes (" + bytesCopied + ") for resource=" + resource.getFilename());
				if (logger.isDebugEnabled())
					logger.debug("writeResponse(" + resource.getFile() + ") copied " + bytesCopied + " bytes");
			}
			finally
			{
				if (in != null)
					in.close();
			}
		}
	}
	
	private String[] splitDirectoryAndFilename (String firstResourceElement) {
		int index = firstResourceElement.lastIndexOf("/");
		String dir;
		if (index == -1) {
			dir = null;
		} else {
			dir = firstResourceElement.substring(0, index + 1);
		}
		String filename = firstResourceElement.substring(index + 1, firstResourceElement.length());
		return new String[] { dir, filename };
	}
	
	private void addResource(Resource parent, String name, Collection<Resource> resources) throws IOException {
		if (name.length() > 0) {
			Resource resource = parent.createRelative(name);
			if (isAllowed(resource)) {
				resources.add(resource);
			}
		}
	}
	
	private boolean isAllowed(Resource resource) throws IOException {
		return resource.exists() && resource.getFile().isFile();
	}
		
	// TODO promote to top-level and make reusable
	// TODO check ServletContext.getMimeType(String) first
	
	public interface FileMediaTypeMap {
		MediaType getMediaType(String fileName);
	}
	
	public static class DefaultFileMediaTypeMap implements FileMediaTypeMap {

		private static final boolean jafPresent =
			ClassUtils.isPresent("javax.activation.FileTypeMap", ContentNegotiatingViewResolver.class.getClassLoader());

		private boolean useJaf = true;

		private ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<String, MediaType>();
		public DefaultFileMediaTypeMap ()
		{
			super();
		}
		/*
		 * @see org.springframework.ext.resource.ResourceHttpRequestHandler.FileMediaTypeMap#getMediaType(java.lang.String)
		 */
		@Override
		public MediaType getMediaType(String filename) {
			String extension = StringUtils.getFilenameExtension(filename);
			if (!StringUtils.hasText(extension)) {
				return null;
			}
			extension = extension.toLowerCase(Locale.ENGLISH);
			MediaType mediaType = this.mediaTypes.get(extension);
			if (mediaType == null && useJaf && jafPresent) {
				mediaType = ActivationMediaTypeFactory.getMediaType(filename);
				if (mediaType != null) {
					this.mediaTypes.putIfAbsent(extension, mediaType);
				}
			}
			return mediaType;
		}
		
		/**
		 * Inner class to avoid hard-coded JAF dependency.
		 */
		private static final class ActivationMediaTypeFactory {
			private ActivationMediaTypeFactory ()
			{
				// no instance
			}

			private static final FileTypeMap fileTypeMap;
			static {
				fileTypeMap = loadFileTypeMapFromContextSupportModule();
			}

			@SuppressWarnings("synthetic-access")
			private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
				// see if we can find the extended mime.types from the context-support module
				Resource mappingLocation = new ClassPathResource("org/springframework/mail/javamail/mime.types");
				if (mappingLocation.exists()) {
					if (logger.isTraceEnabled()) {
						logger.trace("Loading Java Activation Framework FileTypeMap from " + mappingLocation);
					}
					InputStream inputStream = null;
					try {
						inputStream = mappingLocation.getInputStream();
						return new MimetypesFileTypeMap(inputStream);
					}
					catch (IOException ex) {
						// ignore
					}
					finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							}
							catch (IOException ex) {
								// ignore
							}
						}
					}
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Loading default Java Activation Framework FileTypeMap");
				}
				return FileTypeMap.getDefaultFileTypeMap();
			}

			public static MediaType getMediaType(String fileName) {
				String mediaType = fileTypeMap.getContentType(fileName);
				return StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null;
			}
		}
	}
}
