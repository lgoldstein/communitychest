/*
 *
 */
package net.community.chest.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to annotate a class that does not have its own XML {@link org.w3c.dom.Document}
 * but rather an XML {@link org.w3c.dom.Element} inside a document. This
 * annotation allows specifying the element name and/or XML document name
 * which are assumed to exist somewhere in the class packages hierarchy.</P>
 * @author Lyor G.
 * @since Nov 10, 2008 3:30:32 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcesAnchor {
    /**
     * Constant(s) used by the {@link ResourcesAnchor} annotation
     */
    public static final String    DEFAULT_SECTION_DOC_NAME="resources.xml",
                                DEFAULT_SECTION_ELEM_NAME="section",
                                DEFAULT_SECTION_ATTR_NAME="name";
    /**
     * @return Name of XML {@link org.w3c.dom.Document} to be loaded
     */
    String documentName () default DEFAULT_SECTION_DOC_NAME;
    /**
     * @return Name of XML {@link org.w3c.dom.Element} to use for separating
     * the document into &quot;sections&quot;
     */
    String elementName () default DEFAULT_SECTION_ELEM_NAME;
    /**
     * @return Name of XML {@link org.w3c.dom.Element} attribute to use for
     * separating the document into &quot;sections&quot;
     */
    String attributeName () default DEFAULT_SECTION_ATTR_NAME;
    /**
     * @return Actual section for the UI class - default=simple class name
     */
    String sectionName () default "";
}
