package com.vmware.spring.workshop.facade.support.marshal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.Modifier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @author lgoldstein
 */
public class JaxbClassesMarshaller extends AbstractJAXBContextMarshaller {
	private final JAXBContext	_jaxbContext;
	private final Class<?>[]	_boundClasses;
	public JaxbClassesMarshaller(final Class<?> ... rootClasses) throws JAXBException {
		this((rootClasses == null) ? Collections.<Class<?>>emptyList() : new HashSet<Class<?>>(Arrays.asList(rootClasses)));
	}
	public JaxbClassesMarshaller(final Collection<Class<?>> rootClasses) throws JAXBException {
		Assert.isTrue(!CollectionUtils.isEmpty(rootClasses), "No root classes");

		final Set<Class<?>>	boundClasses=new HashSet<Class<?>>(rootClasses.size() * 4);
		for (final Class<?> dtoClass : rootClasses) {
			Assert.state(dtoClass != null, "Null root class found");
			final List<Class<?>>	hierarchy=ClassUtils.getAllSuperclasses(dtoClass);
			Assert.isTrue(hierarchy.remove(Object.class), "Root object not in hierarchy of " + dtoClass.getSimpleName());
			Assert.isTrue(hierarchy.add(dtoClass), "Root class not added " + dtoClass.getSimpleName());
			boundClasses.addAll(hierarchy);
		}

		_boundClasses = boundClasses.toArray(new Class[boundClasses.size()]);
		_jaxbContext = JAXBContext.newInstance(_boundClasses);
	}

	@Override
	public JAXBContext getJAXBContext() throws JAXBException {
		return _jaxbContext;
	}

	@Override
	public boolean supports(final Class<?> clazz) {
		return (clazz != null) && ArrayUtils.contains(_boundClasses, clazz);
	}

	@Override
	public String toString() {
		return StringUtils.join(_boundClasses, ',');
	}

	public static final Set<Class<?>> extractRootElements (final Collection<String> packages)
			throws ClassNotFoundException, LinkageError {
		if (CollectionUtils.isEmpty(packages))
			return Collections.emptySet();

		final Thread		thread=Thread.currentThread();
		final ClassLoader	cl=thread.getContextClassLoader();
		final Set<Class<?>> result=new HashSet<Class<?>>();
		for (final String pkgName : packages) {
			Assert.hasText(pkgName, "Null/empty package name");
			final String	facPath=pkgName + ".ObjectFactory";
			if (!org.springframework.util.ClassUtils.isPresent(facPath, cl))
				throw new IllegalStateException("No object factory in " + pkgName);
			
			final Class<?>		objFactory=org.springframework.util.ClassUtils.forName(facPath, cl);
			final XmlRegistry	registry=objFactory.getAnnotation(XmlRegistry.class);
			if (registry == null)
				throw new IllegalStateException("Object factory not marked as XmlRegistry in " + pkgName);

			for (final Method method : objFactory.getMethods()) {
				final Class<?>	rootClass=getGeneratedElementType(method);
				if (rootClass == null)
					continue;
				if (!result.add(rootClass))
					continue;	// debug breakpoint
			}
		}

		return result;
	}
	
	public static final Class<?> getGeneratedElementType (final Method method) {
		if (method == null)
			return null;

		{
			final int	mods=method.getModifiers();
			if (Modifier.isAbstract(mods)
			 || Modifier.isStatic(mods)		// must be a public non-static non-abstract method
			 || (!Modifier.isPublic(mods)))
				return null;
		}

		final Class<?>[]	params=method.getParameterTypes();
		if (!ArrayUtils.isEmpty(params))
			return null;	// must be a no-args creator

		final Class<?>	retVal=method.getReturnType();
		{
			final int	mods=retVal.getModifiers(); 
			if (org.springframework.util.ClassUtils.isPrimitiveOrWrapper(retVal)	// not a primitive or its wrapper
			 || retVal.isArray()						// not an array
			 || Void.class.isAssignableFrom(retVal)		// not void
			 || Void.TYPE.isAssignableFrom(retVal)
			 || Modifier.isAbstract(mods)				// not abstract
			 || (!Modifier.isPublic(mods)))				// public
				return null;
		}

		final XmlRootElement	rootElement=retVal.getAnnotation(XmlRootElement.class);
		if (rootElement == null)	// must be annotated as root element
			return null;
		
		return retVal;
	}
}
