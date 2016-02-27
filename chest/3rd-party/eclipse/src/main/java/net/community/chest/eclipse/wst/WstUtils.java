/*
 * 
 */
package net.community.chest.eclipse.wst;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Various utilities associated with the Web Standard Tools</P> 
 * @author Lyor G.
 * @since Apr 26, 2009 2:04:13 PM
 */
public final class WstUtils {
	private WstUtils ()
	{
		// no instance
	}
	/**
	 * Default name of the settings file
	 */
	public static final String	COMPONENTS_FILENAME="org.eclipse.wst.common.component",
	// various XML related strings
			PROJ_MODULES_ELEM_NAME="project-modules",
				PROJ_ID_ATTR="id",
				PROJ_VER_ATTR="project-version",
			WEB_MODULE_ELEM_NAME="wb-module",
				WEB_MODULE_DEPLOY_NAME_ATTR="deploy-name",
			WEB_RESOURCE_ELEM_NAME="wb-resource",
				WEB_RESOURCE_DEPLOY_PATH_ATTR="deploy-path",
				WEB_RESOURCE_SOURCE_PATH_ATTR="source-path",
			DEPMODULE_ELEM_NAME="dependent-module",
				DEPMODULE_DEPLOY_PATH_ATTR="deploy-path",
				DEPMODULE_HANDLE_ATTR="handle",
					VARVALUE_HANDLE_VALUE_PREFIX="module:/classpath/var/",
			DEPTYPE_ELEM_NAME="dependency-type";
}
