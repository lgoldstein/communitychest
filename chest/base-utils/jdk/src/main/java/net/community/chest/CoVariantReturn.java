package net.community.chest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to denote a co-variant return type</P>
 * @author Lyor G.
 * @since Oct 22, 2007 3:58:18 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CoVariantReturn {
	// no properties
}
