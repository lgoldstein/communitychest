/*
 * 
 */
package net.community.chest.aspectj.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2010 9:31:33 AM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Stereotype {
	/**
	 * @return Dummy name
	 */
	String value() default "";
}
