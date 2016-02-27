package com.vmware.spring.workshop.facade.support.marshal;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.XmlMappingException;

/**
 * @author lgoldstein
 */
public abstract class AbstractJAXBContextMarshaller implements Marshaller, Unmarshaller {
	protected AbstractJAXBContextMarshaller() {
		super();
	}

	@Override
	public void marshal(Object graph, Result result)
			throws IOException, XmlMappingException {
		try {
			final JAXBContext				context=getJAXBContext();
			final javax.xml.bind.Marshaller	m=context.createMarshaller();
			m.marshal(graph, result);
		} catch(JAXBException e) {
			throw new MarshallingFailureException("Failed (" + e.getErrorCode() + ") to marshal " + graph + ": " + e.getMessage(), e);
		}
	}

	@Override
	public Object unmarshal(Source source)
			throws IOException, XmlMappingException {
		try {
			final JAXBContext					context=getJAXBContext();
			final javax.xml.bind.Unmarshaller	u=context.createUnmarshaller();
			return u.unmarshal(source);
		} catch(JAXBException e) {
			throw new UnmarshallingFailureException("Failed (" + e.getErrorCode() + ") to unmarshal : " + e.getMessage(), e);
		}
	}

	public abstract JAXBContext getJAXBContext () throws JAXBException;
}
