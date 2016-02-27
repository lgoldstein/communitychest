package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.Assert;


/**
 * @author lgoldstein
 */
public class TransformationContext {
	private final Set<ClassFileTransformer>	transformers=
			Collections.synchronizedSet(new LinkedHashSet<ClassFileTransformer>());
	protected final Logger logger=Logger.getLogger(getClass().getName());
	protected final ClassLoader classLoader;
	public TransformationContext(ClassLoader classLoader) {
		this(classLoader, Collections.<ClassFileTransformer>emptyList());
	}

	public TransformationContext(ClassLoader classLoader, ClassFileTransformer ... xformers) {
		this(classLoader, Arrays.asList(xformers));
	}

	public TransformationContext(ClassLoader classLoader, Collection<? extends ClassFileTransformer> xformers) {
		this.classLoader = classLoader;
		transformers.addAll(xformers);
	}

	public boolean addTransformer(ClassFileTransformer t) {
		Assert.notNull(t, "No transformer");
		return transformers.add(t);
	}
	
	public boolean isEmpty() {
		return transformers.isEmpty();
	}
	
	public byte[] applyTransformers (String className, byte[] data) throws IllegalClassFormatException {
		byte[]	result=data;
		for (ClassFileTransformer t : transformers) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("applyTransformers(" + className + ") invoking " + t.getClass().getName());
			}
			result = t.transform(classLoader, className, null, this.getClass().getProtectionDomain(), result);
		}

		return result;
	}

	@Override
	public String toString() {
		return WebappClassLoaderInstrumentor.getContextName(classLoader);
	}
}
