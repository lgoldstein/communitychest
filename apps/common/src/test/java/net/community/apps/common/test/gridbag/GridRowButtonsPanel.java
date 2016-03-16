package net.community.apps.common.test.gridbag;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.community.chest.awt.layout.BaseFlowLayout;
import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.util.map.BooleansMap;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:40:52 PM
 */
public class GridRowButtonsPanel extends JPanel implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 3311646105804278344L;

    private static class ListenersCollections extends LinkedList<ActionListener> {
        /**
         *
         */
        private static final long serialVersionUID = -9005926525862604740L;

        protected ListenersCollections ()
        {
            super();
        }
    }

    private final BooleansMap<ListenersCollections>    _listenersMap=new BooleansMap<ListenersCollections>(ListenersCollections.class, true);
    public boolean addButtonListener (final Boolean btnType, final ActionListener l)
    {
        if (null == l)
            return false;

        ListenersCollections    c=_listenersMap.get(btnType);
        if (null == c)
        {
            c = new ListenersCollections();
            _listenersMap.put(btnType, c);
        }

        c.add(l);
        return true;
    }

    public boolean removeButtonListener (final Boolean btnType, final ActionListener l)
    {
        final ListenersCollections    c=(null == l) ? null : _listenersMap.get(btnType);
        if ((null == c) || (c.size() <= 0))
            return false;

        return c.remove(l);
    }

    public int fireActionListeners (final Boolean btnType, final ActionEvent event)
    {
        final ListenersCollections    c=_listenersMap.get(btnType);
        final int                    numListeners=(null == c) ? 0 : c.size();
        if (numListeners > 0)
        {
            for (final ActionListener l : c)
                l.actionPerformed(event);
        }

        return numListeners;
    }

    private final JButton    _addBtn= new JButton("Add"), _delBtn=new JButton("Delete");
    public JButton getButton (final boolean getAddBtn)
    {
        return getAddBtn ? _addBtn : _delBtn;
    }

    public final boolean isAddButton (final Object obj)
    {
        return (obj == _addBtn);
    }

    public final boolean isDelButton (final Object obj)
    {
        return (obj == _delBtn);
    }

    public final Boolean classifyButtonType (final Object obj)
    {
        if (isAddButton(obj))
            return Boolean.TRUE;
        else if (isDelButton(obj))
            return Boolean.FALSE;
        else
            return null;
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (final ActionEvent event)
    {
        final Object    src=(null == event) ? null : event.getSource();
        final Boolean    btnType=classifyButtonType(src);
        if (null == btnType)
        {
            System.err.println("Unknown object source: " + src);
            return;
        }

        final int    numSpecific=fireActionListeners(btnType, event),
                    numGeneral=fireActionListeners(null, event);
        System.out.println("actionPerformed(add=" + btnType + ") informed " + numSpecific + " specific listeners and " + numGeneral + " general listeners");
    }

    public GridRowButtonsPanel ()
    {
        super(new BaseFlowLayout(FlowLayoutAlignment.RIGHT));

        _delBtn.addActionListener(this);
        _addBtn.addActionListener(this);

        add(_delBtn);
        add(_addBtn);
    }
}
