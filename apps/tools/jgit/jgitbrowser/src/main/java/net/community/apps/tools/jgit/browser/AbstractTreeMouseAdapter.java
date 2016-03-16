/*
 *
 */
package net.community.apps.tools.jgit.browser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.community.apps.tools.jgit.browser.reflog.ReflogDialog;
import net.community.chest.swing.component.menu.BasePopupMenu;
import net.community.chest.swing.component.menu.MenuUtil;
import net.community.chest.swing.options.BaseOptionPane;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.ObjectWalk;
import org.eclipse.jgit.revwalk.RevObject;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 21, 2011 10:56:48 AM
 *
 */
abstract class AbstractTreeMouseAdapter extends MouseAdapter {
    private final BasePopupMenu    _popupMenu;
    public final BasePopupMenu getPopupMenu ()
    {
        return _popupMenu;
    }

    private final JTree    _invoker;
    public final JTree getInvoker ()
    {
        return _invoker;
    }

    private final MainFrame    _frame;
    public final MainFrame getMainFrame ()
    {
        return _frame;
    }

    protected AbstractTreeMouseAdapter (MainFrame frame, JTree invoker, Element elem) throws Exception
    {
        if ((_frame=frame) == null)
            throw new IllegalStateException("No main frame instance provided");
        if ((_invoker=invoker) == null)
            throw new IllegalStateException("No invoker provided");
        _popupMenu = new BasePopupMenu(elem);
        MenuUtil.setMenuItemsHandlers(_popupMenu, getActionListenersMap());
    }

    protected static final String    LOG_CMD="log";
    protected Map<String,ActionListener> getActionListenersMap ()
    {
        final Map<String,ActionListener>    lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
        lm.put(LOG_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    showLog();
                }
            });
        return lm;
    }

    protected abstract ObjectId getSelectedNodeId (final Object selNode);
    protected void showLog ()
    {
        final JTree            invoker=getInvoker();
        final TreePath        selPath=(invoker == null) ? null : invoker.getSelectionPath();
        final Object        selNode=(selPath == null) ? null : selPath.getLastPathComponent();
        final ObjectId        id=getSelectedNodeId(selNode);
        final MainFrame        frame=(id == null) ? null : getMainFrame();
        final Repository    repo=(frame == null) ? null : frame.getRepository();
        final ObjectWalk    walker=(repo == null) ? null : new ObjectWalk(repo);
        try
        {
            final RevObject        rev=(walker == null) ? null : walker.parseAny(id);
            if (rev == null)
                return;

            final ReflogDialog    dlg=new ReflogDialog(frame, walker, rev);
            dlg.setVisible(true);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(invoker, e);
        }
    }

    protected TreeNode showPopupMenu (MouseEvent e)
    {
        if ((null == e) || (!e.isPopupTrigger()))
            return null;

        final int        x=e.getX(), y=e.getY();
        final JTree        invoker=getInvoker();
        final TreePath    path=(invoker == null) ? null : invoker.getPathForLocation(x, y),
                        selPath=(invoker == null) ? null : invoker.getSelectionPath();
        final TreeNode    node=(path == null) ? null : (TreeNode) path.getLastPathComponent(),
                        selNode=(selPath == null) ? null : (TreeNode) selPath.getLastPathComponent();
        if ((node != selNode) && (path != null))
            invoker.setSelectionPath(path);

        final JPopupMenu    menu=(node == null) ? null : getPopupMenu();
        if (menu != null)
        {
            menu.show(getInvoker(), e.getX(), e.getY());
            return node;
        }

        return null;
    }
}
