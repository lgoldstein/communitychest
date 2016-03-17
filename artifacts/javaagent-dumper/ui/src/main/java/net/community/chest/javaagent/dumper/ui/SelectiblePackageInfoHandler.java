/*
 *
 */
package net.community.chest.javaagent.dumper.ui;

import net.community.chest.javaagent.dumper.ui.data.SelectiblePackageInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 12:18:48 PM
 *
 */
interface SelectiblePackageInfoHandler {
    void processSelectiblePackageInfo (SelectiblePackageInfo pkgInfo);
    void doneLoadingDumperData (DumperDataLoaderThread loader);
}
