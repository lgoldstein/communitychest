/*
 *
 */
package net.community.chest.ui.components.text;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import net.community.chest.awt.attributes.Editable;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Textable;

/**
 * <P>Copyright as per GPLv2</P>
 * Based on code published by <A HREF="mailto:santhosh@in.fiorano.com">Santhosh Kumar</A>
 * in the <A HREF="http://www.jroller.com/santhosh/entry/file_path_autocompletion">weblog</A>
 * @param <C> Type of {@link JTextComponent} being used for auto-completion
 * @author Lyor G.
 * @since Feb 24, 2011 12:22:55 PM
 */
public abstract class AutoCompleter<C extends JTextComponent>
        implements Textable, Enabled, Editable {
    private final JList<String> _choicesList=new JList<String>();
    public JList<String> getChoicesList ()
    {
        return _choicesList;
    }

    private final JPopupMenu    _choicesPopup=new JPopupMenu();
    public JPopupMenu getChoicesPopup ()
    {
        return _choicesPopup;
    }

    private final C    _textComp;
    public C getTextComponent ()
    {
        return _textComp;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        final JTextComponent    textComp=getTextComponent();
        return (textComp == null) ? null : textComp.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Enabled#isEnabled()
     */
    @Override
    public boolean isEnabled ()
    {
        final JTextComponent    textComp=getTextComponent();
        return (textComp != null) && textComp.isEnabled();
    }
    /*
     * @see net.community.chest.awt.attributes.Editable#isEditable()
     */
    @Override
    public boolean isEditable ()
    {
        final JTextComponent    textComp=getTextComponent();
        return (textComp != null) && textComp.isEditable();
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String text)
    {
        final JTextComponent    textComp=getTextComponent();
        if (textComp != null)
            textComp.setText((text == null) ? "" : text);
    }
    /*
     * @see net.community.chest.awt.attributes.Enabled#setEnabled(boolean)
     */
    @Override
    public void setEnabled (boolean enabled)
    {
        final JTextComponent    textComp=getTextComponent();
        if (textComp != null)
            textComp.setEnabled(enabled);
    }
    /*
     * @see net.community.chest.awt.attributes.Editable#setEditable(boolean)
     */
    @Override
    public void setEditable (boolean enabled)
    {
        final JTextComponent    textComp=getTextComponent();
        if (textComp != null)
            textComp.setEditable(enabled);
    }

    public static final String AUTOCOMPLETER_CLIENT_PROP="AUTOCOMPLETERCLIENTPROP"; // NOI18N
    protected AutoCompleter (C comp) throws IllegalArgumentException
    {
        if ((_textComp=comp) == null)
            throw new IllegalArgumentException("No text component provided");
        _textComp.putClientProperty(AUTOCOMPLETER_CLIENT_PROP, this);

        final JScrollPane scroll=new JScrollPane(_choicesList);
        scroll.setBorder(null);

        _choicesList.setFocusable(false);
        scroll.getVerticalScrollBar().setFocusable(false);
        scroll.getHorizontalScrollBar().setFocusable(false);

        _choicesPopup.setBorder(BorderFactory.createLineBorder(Color.black));
        _choicesPopup.add(scroll);

        if (_textComp instanceof JTextField)
        {
            _textComp.registerKeyboardAction(_showAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
            _textComp.getDocument().addDocumentListener(_documentListener);
        }
        else
            _textComp.registerKeyboardAction(_showAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),JComponent.WHEN_FOCUSED);

        _textComp.registerKeyboardAction(_upAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
        _textComp.registerKeyboardAction(_hidePopupAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        _choicesPopup.addPopupMenuListener(new PopupMenuListener() {
                /*
                 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
                 */
                @Override
                public void popupMenuWillBecomeVisible (PopupMenuEvent e)
                {
                    // ignored
                }
                /*
                 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
                 */
                @Override
                public void popupMenuWillBecomeInvisible (PopupMenuEvent e)
                {
                    getTextComponent().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
                }
                /*
                 * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
                 */
                @Override
                public void popupMenuCanceled (PopupMenuEvent e)
                {
                    // ignored
                }
            });
        _choicesList.addMouseListener(new MouseAdapter() {

                /*
                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseClicked (MouseEvent e)
                {
                    final Object        src=(e == null) ? null : e.getSource();
                    final JList<String>    cList=getChoicesList();
                    if ((src != cList) || (e.getClickCount() <= 1))
                        return;

                    getChoicesPopup().setVisible(false);
                    acceptedListItem(cList.getSelectedValue());
                }
            });
        _choicesList.setRequestFocusEnabled(false);
    }

    private static final Action    _acceptAction=new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = 1878074721651669839L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                final JComponent        tf=(JComponent) e.getSource();
                final AutoCompleter<?>    completer=(AutoCompleter<?>) tf.getClientProperty(AUTOCOMPLETER_CLIENT_PROP);
                completer.getChoicesPopup().setVisible(false);
                completer.acceptedListItem(completer.getChoicesList().getSelectedValue());
            }
        };

    private final DocumentListener _documentListener=new DocumentListener() {
            /*
             * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
             */
            @Override
            public void insertUpdate (DocumentEvent e)
            {
                showPopup();
            }
            /*
             * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
             */
            @Override
            public void removeUpdate (DocumentEvent e)
            {
                showPopup();
            }
            /*
             * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
             */
            @Override
            public void changedUpdate (DocumentEvent e)
            {
                // ignored
            }
        };

    protected void showPopup ()
    {
        _choicesPopup.setVisible(false);

        if (_textComp.isEnabled() && updateListData() && _choicesList.getModel().getSize() != 0)
        {
            if (!(_textComp instanceof JTextField))
                _textComp.getDocument().addDocumentListener(_documentListener);
            _textComp.registerKeyboardAction(_acceptAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

            final int size=_choicesList.getModel().getSize();
            _choicesList.setVisibleRowCount(size < 10 ? size : 10);

            final int x;
            try
            {
                final Caret        caret=_textComp.getCaret();
                final int         pos=(caret == null) ? 0 : Math.min(caret.getDot(), caret.getMark());
                final TextUI    ui=_textComp.getUI();
                final Rectangle    rec=(ui == null) ? null : ui.modelToView(_textComp, pos);
                if (rec == null)    // can happen if text field initialized too soon
                    return;

                x = rec.x;
            }
            catch(BadLocationException e)    // should not happen
            {
                throw new RuntimeException("showPopup()", e);
            }
            _choicesPopup.show(_textComp, x, _textComp.getHeight());
        }
        else
            _choicesPopup.setVisible(false);
        _textComp.requestFocus();
    }

    private static final Action _showAction=new AbstractAction() {
            /**
         *
         */
        private static final long serialVersionUID = 5759998729287393296L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                final JComponent        tf=(JComponent) e.getSource();
                final AutoCompleter<?>    completer=(AutoCompleter<?>) tf.getClientProperty(AUTOCOMPLETER_CLIENT_PROP);
                if (tf.isEnabled())
                {
                    if (completer.getChoicesPopup().isVisible())
                        completer.selectNextPossibleValue();
                    else
                        completer.showPopup();
                }
            }
        };

    private static final Action _upAction=new AbstractAction() {
            /**
         *
         */
        private static final long serialVersionUID = 7199115378813203480L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                final JComponent        tf=(JComponent) e.getSource();
                final AutoCompleter<?>    completer=(AutoCompleter<?>) tf.getClientProperty(AUTOCOMPLETER_CLIENT_PROP);
                if (tf.isEnabled())
                {
                    if (completer.getChoicesPopup().isVisible())
                        completer.selectPreviousPossibleValue();
                }
            }
        };

    private static final Action _hidePopupAction=new AbstractAction() {
            /**
         *
         */
        private static final long serialVersionUID = 8948856448568380287L;

            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                final JComponent        tf=(JComponent) e.getSource();
                final AutoCompleter<?>    completer=(AutoCompleter<?>) tf.getClientProperty(AUTOCOMPLETER_CLIENT_PROP);
                if (tf.isEnabled())
                    completer.getChoicesPopup().setVisible(false);
            }
        };
    /**
     * Selects the next item in the list. It won't change the selection if the
     * currently selected item is already the last item.
     */
    protected void selectNextPossibleValue ()
    {
        final int si=_choicesList.getSelectedIndex();
        if (si < _choicesList.getModel().getSize() - 1) {
            _choicesList.setSelectedIndex(si + 1);
            _choicesList.ensureIndexIsVisible(si + 1);
        }
    }
    /**
     * Selects the previous item in the list. It won't change the selection if
     * the currently selected item is already the first item.
     */
    protected void selectPreviousPossibleValue ()
    {
        final int si=_choicesList.getSelectedIndex();
        if (si > 0) {
            _choicesList.setSelectedIndex(si - 1);
            _choicesList.ensureIndexIsVisible(si - 1);
        }
    }

    // update list model depending on the data in textfield
    protected abstract boolean updateListData ();

    // user has selected some item in the list. update textfield accordingly...
    protected abstract void acceptedListItem (String selected);
}

