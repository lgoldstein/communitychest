package com.vmware.spring.workshop.facade;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.ReflectionUtils;

import com.vmware.spring.workshop.model.ModelUtils;
import com.vmware.spring.workshop.model.ModelUtilsTestSupport;

/**
 * @author lgoldstein
 */
public abstract class AbstractFacadeTestSupport extends AbstractJUnit4SpringContextTests {
	public static final String	DEFAULT_TEST_CONTEXT="classpath:META-INF/fcdSupportContext.xml";
	protected final Random	RANDOMIZER=new Random(System.nanoTime());

	protected AbstractFacadeTestSupport() {
		super();
	}

	protected List<String> initializeDTOValues (final Object dto) throws IntrospectionException {
		final Map<String,PropertyDescriptor>	propsMap=ModelUtils.createPropertiesMap(dto.getClass());
		for (final Map.Entry<String,PropertyDescriptor> pe : propsMap.entrySet()) {
			final String				name=pe.getKey();
			final PropertyDescriptor	desc=pe.getValue();
			final Object				value=generateDTOValue(name, desc.getPropertyType());
			final Method				sMethod=desc.getWriteMethod();
			ReflectionUtils.invokeMethod(sMethod, dto, value);
		}

		return new ArrayList<String>(propsMap.keySet());
	}

	protected Object generateDTOValue  (final String name, final Class<?> type) {
		return ModelUtilsTestSupport.generateRandomValue(name, type);
	}

}
