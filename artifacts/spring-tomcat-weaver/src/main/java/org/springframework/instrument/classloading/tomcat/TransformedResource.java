package org.springframework.instrument.classloading.tomcat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.naming.resources.Resource;

/**
 * @author lgoldstein
 */
public class TransformedResource extends Resource {
	public TransformedResource(byte[] data) {
		this.binaryContent = data;
	}

	@Override
	public InputStream streamContent() throws IOException {
		return new ByteArrayInputStream(getContent());
	}

	@Override
	public void setContent(InputStream inputStream) {
		throw new UnsupportedOperationException("Override input stream N/A");
	}

	@Override
	public void setContent(byte[] binaryContent) {
		throw new UnsupportedOperationException("Override binary content N/A");
	}
}
