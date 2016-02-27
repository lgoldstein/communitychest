	/*
 * 
 */
package net.community.chest.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 12, 2010 7:53:40 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PACKAGE })
@Inherited
public @interface TestAnnotation {
	String value();
}
