/*
 * 
 */
package jnlp.sample.util.log;

import java.util.ResourceBundle;

import javax.servlet.ServletConfig;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 23, 2009 2:41:12 PM
 */
public interface LoggerManager {
	void initLogger (ServletConfig config, ResourceBundle resources);
	Logger getLogger (String loggerName);
}
