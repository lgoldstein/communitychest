/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.management.MBeanServer;
import javax.swing.JTree;

import net.community.chest.javaagent.dumper.Configuration;
import net.community.chest.javaagent.dumper.DumperClassFileTransformer;

import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;
/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 3:37:38 PM
 */
public class IncludeExcludeFilterTest extends AbstractFilterTest {
	public IncludeExcludeFilterTest ()
	{
		super();
	}

	@Test
	public void testInclusion ()
	{
		assertFilterResult(new IncludeExcludeFilter(new PatternClassFilter("foo.bar.*"), new PatternClassFilter("bar.foo.*")),
						   Boolean.TRUE,
						   "foo.bar.TopLevelClass",
						   "foo.bar.internal.InternalClass",
						   "foo.bar.inner.Foo$InnerClass");
	}
	@Test
	public void testExclusion ()
	{
		assertFilterResult(new IncludeExcludeFilter(new PatternClassFilter("bar.foo.*"), new PatternClassFilter("foo.bar.*")),
						   Boolean.FALSE,
						   "foo.bar.TopLevelClass",
						   "foo.bar.internal.InternalClass",
						   "foo.bar.inner.Foo$InnerClass");
	}
	
	@Test
	public void testDefaultConfiguration () throws Exception
	{
		final Map<String,String>	optsMap=Collections.emptyMap();
		final Configuration			config=DumperClassFileTransformer.resolveConfiguration(getClass(), optsMap);
		final ClassFilter			filter=config.getFilter();
		assertNotNull("Default filter not found", filter);
		assertFilterResult(filter, Boolean.FALSE,
						   String.class, Serializable.class,	// java packages
						   JTree.class, MBeanServer.class);		// javax packages
		assertFilterResult(filter, Boolean.TRUE,
				   		   getClass(), IncludeExcludeFilter.class,
				   		   JavaClass.class, org.apache.regexp.StringCharacterIterator.class);
	}
}
