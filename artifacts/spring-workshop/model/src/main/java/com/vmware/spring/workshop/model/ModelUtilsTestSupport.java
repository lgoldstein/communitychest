package com.vmware.spring.workshop.model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

/**
 * @author lgoldstein
 */
public final class ModelUtilsTestSupport {
    public static final Random    RANDOMIZER=new Random(System.nanoTime());
    public static final ValueGenerator    DEFAULT_GENERATOR=new ValueGenerator() {
            @Override
            public Object generateValue(String name, Class<?> type) {
                return generateRandomValue(name, type);
            }
        };
    private ModelUtilsTestSupport() {
        // no instance
    }

    public static final List<String> initializeValues (final Object dto, final ValueGenerator vgen) throws IntrospectionException {
        final Map<String,PropertyDescriptor>    propsMap=ModelUtils.createPropertiesMap(dto.getClass());
        for (final Map.Entry<String,PropertyDescriptor> pe : propsMap.entrySet()) {
            final String                name=pe.getKey();
            final PropertyDescriptor    desc=pe.getValue();
            final Object                value=vgen.generateValue(name, desc.getPropertyType());
            final Method                sMethod=desc.getWriteMethod();
            try {
                sMethod.invoke(dto, value);
            } catch (Exception e) {
                throw new IntrospectionException(e.getClass().getSimpleName() + " on " + sMethod.getName() + "(" + value + "): " + e.getMessage());
            }
        }

        return new ArrayList<String>(propsMap.keySet());
    }

    public static final Object generateRandomValue  (final String name, final Class<?> type) {
        if ((Boolean.TYPE == type) || (Boolean.class == type))
            return Boolean.valueOf(RANDOMIZER.nextInt(Short.MAX_VALUE) == 0);
        else if ((Integer.TYPE == type) || (Integer.class == type))
            return Integer.valueOf(RANDOMIZER.nextInt(Short.MAX_VALUE));
        else if ((Long.TYPE == type) || (Long.class == type))
            return Long.valueOf(System.nanoTime() + RANDOMIZER.nextInt(Short.MAX_VALUE));
        else if (String.class == type)
            return UUID.randomUUID().toString();
        else if (Enum.class.isAssignableFrom(type)) {
            Object[]    vals=type.getEnumConstants();
            return vals[RANDOMIZER.nextInt(vals.length)];
        }

        throw new NoSuchElementException("generatedDTOValue(" + name + ")[" + type.getSimpleName() + "] cannot generate");
    }

    public static interface ValueGenerator {
        Object generateValue  (String name, Class<?> type);
    }
}
