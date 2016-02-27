package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;

import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * @author lgoldstein
 */
public class TomcatContextLoadTimeWeaver extends DefaultContextLoadTimeWeaver {
	private boolean	autoReconfigureLoader;
	public TomcatContextLoadTimeWeaver() {
		super();
	}

	public TomcatContextLoadTimeWeaver(ClassLoader beanClassLoader) {
		super(beanClassLoader);
	}

	public boolean isAutoReconfigureLoader() {
		return autoReconfigureLoader;
	}

	public void setAutoReconfigureLoader(boolean autoReconfigureLoader) {
		this.autoReconfigureLoader = autoReconfigureLoader;
	}

	@Override
	protected LoadTimeWeaver createServerSpecificLoadTimeWeaver(ClassLoader classLoader) {
		if (WebappClassLoaderInstrumentor.isWebappClassLoader(classLoader)) {
			LoadTimeWeaver	weaver=createTomcatLoadTimeWeaver(classLoader);
			if (weaver != null) {
				return weaver;
			}
		}

		return super.createServerSpecificLoadTimeWeaver(classLoader);
	}

	protected LoadTimeWeaver createTomcatLoadTimeWeaver(final ClassLoader classLoader) {
		final TransformationContext	context;
		if (isAutoReconfigureLoader()) {
			context = WebappClassLoaderInstrumentor.adjustWebappClassLoader(classLoader);
		} else {
			context = new TransformationContext(classLoader);
		}

		return new LoadTimeWeaver() {
				public void addTransformer(ClassFileTransformer transformer) {
					context.addTransformer(transformer);
				}
	
				public ClassLoader getInstrumentableClassLoader() {
					return classLoader;
				}
	
				public ClassLoader getThrowawayClassLoader() {
					return classLoader;
				}
			};
	}
}
