package com.vmware.spring.workshop.facade.support.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.xml.MarshallingView;

/**
 * @author lgoldstein
 */
@View("genericRESTView")
public class GenericRESTView extends MarshallingView {
    public static final String    MODEL_VALUE_KEY="modelValueKey";
    @Inject
    public GenericRESTView (@Named("oxmMarshaller") final Marshaller marshaller) {
        super(marshaller);
        setModelKey(MODEL_VALUE_KEY);
    }
}
