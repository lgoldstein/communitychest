/*
 *
 */
package net.community.chest.javaagent.dumper.ui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.component.label.BaseLabel;
import net.community.chest.swing.component.panel.BasePanel;
import net.community.chest.swing.resources.UIAnchoredResourceAccessor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 17, 2011 3:07:02 PM
 *
 */
public class NodeCellRenderer extends BasePanel implements TreeCellRenderer {
    private static final long serialVersionUID = 3012074619862892608L;

    private final Color _selFg, _selBg, _textFg, _textBg;
    private final Font _treeFont;
    private final Boolean    _drawsFocusBorderAroundIcon;
    private final UIAnchoredResourceAccessor    _resLoader;
    private final BaseLabel    _iconLabel;
    private final BaseCheckBox    _checkBox;

    public NodeCellRenderer (final UIAnchoredResourceAccessor    resLoader)
    {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
        _checkBox = new BaseCheckBox("");
        add(_checkBox);
        _iconLabel = new BaseLabel("");
        add(_iconLabel);

        _resLoader = resLoader;
        _treeFont = UIManager.getFont("Tree.font");
        _drawsFocusBorderAroundIcon = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
        _selFg = UIManager.getColor("Tree.selectionForeground");
        _selBg = UIManager.getColor("Tree.selectionBackground");
        _textFg = UIManager.getColor("Tree.textForeground");
        _textBg = UIManager.getColor("Tree.textBackground");
    }
    /*
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent (JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        final String textValue=tree.convertValueToText(value, selected, expanded, leaf, row, false);
        if (_treeFont != null)
            _iconLabel.setFont(_treeFont);

        _iconLabel.setText(textValue);
        setEnabled(tree.isEnabled());
        _checkBox.setFocusPainted((_drawsFocusBorderAroundIcon != null) && _drawsFocusBorderAroundIcon.booleanValue());

        if (selected)
        {
            _checkBox.setForeground(_selFg);
            _checkBox.setBackground(_selFg);
            _iconLabel.setForeground(_selFg);
            _iconLabel.setBackground(_selFg);
            setForeground(_selFg);
            setBackground(_selBg);
        }
        else
        {
            _checkBox.setForeground(_textFg);
            _checkBox.setBackground(_textBg);
            _iconLabel.setForeground(_textFg);
            _iconLabel.setBackground(_textBg);
            setForeground(_textFg);
            setBackground(_textBg);
        }

        if (value instanceof Selectible)
        {
            final boolean    selValue=((Selectible) value).isSelected();
            _checkBox.setSelected(selValue);

            try
            {
                final Icon        icon=_resLoader.getIcon(value.getClass().getSimpleName() + ".jpg");
                _iconLabel.setIcon(icon);
            }
            catch(Exception e)
            {
                // ignore
                System.err.println("Failed (" + e.getClass().getName() + ") "
                                 + " to load " + value.getClass().getSimpleName()
                                  + "icon: " + e.getMessage());
            }
        }
        else
        {
            _checkBox.setSelected(selected);
        }

        return this;
    }

}
