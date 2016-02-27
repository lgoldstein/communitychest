/*
 * 
 */
package net.community.chest.aspectj.test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2010 9:37:53 AM
 */
public aspect StereotypeAspect {
	/* automatically annotate all classes that have a @Stereotype
	 * meta-annotated annotation with @AnnotatedStereotypeCollected
	 * so we can handle them in the AnnotatedStereotypeAspect.
	 * Example pattern for Spring @Component stereotypes...
	 */
	declare @type : @(@Stereotype *) * : @AnnotatedStereotypeCollected;
}
