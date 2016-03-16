package net.community.apps.tools.b64xlate;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.community.chest.awt.AWTUtils;
import net.community.chest.io.encode.base64.Base64;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.ui.helpers.frame.HelperFrame;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 5, 2007 1:15:46 PM
 */
public class MainFrame extends HelperFrame {
    /**
     *
     */
    private static final long serialVersionUID = 3379252858257720544L;
    public static final int            MAX_HEIGHT=200, MIN_HEIGHT=100, MIN_WIDTH=200;
    public static final Dimension    DEFAULT_INITIAL_SIZE=new Dimension(MIN_WIDTH * 2, MAX_HEIGHT);

    protected Dimension getInitialSize ()
    {
        return DEFAULT_INITIAL_SIZE;
    }

    public static final Insets    COMMON_INSETS=new Insets(5,5,5,5);

    private JCheckBox    _binDataChkbx    /* =null */;
    protected boolean isBinaryData ()
    {
        return (_binDataChkbx != null) && _binDataChkbx.isSelected();
    }

    private JCheckBox    _padDataChkbx    /* =null */;
    protected boolean isAutoPadEnabled ()
    {
        return (_padDataChkbx != null) && _padDataChkbx.isSelected();
    }

    private final JPanel getChoicesPanel ()
    {
        final JPanel                pnl=new JPanel(new GridBagLayout());
        final GridBagConstraints    gbc=new GridBagConstraints();

        gbc.insets = COMMON_INSETS;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.5;

        _binDataChkbx = new JCheckBox("Binary data", false);
        pnl.add(_binDataChkbx, gbc);

        _padDataChkbx = new JCheckBox("Auto-pad", true);
        pnl.add(_padDataChkbx, gbc);

        return pnl;
    }

    private JTextField    _encField    /* =null */;
    protected void encodeData ()
    {
        try
        {
            final String    decText=(null == _decField) /* should not happen */ ? null : _decField.getText(),
                            encText;
            if (isBinaryData())
            {
                final byte[]    bytes=Hex.toByteArray(decText, ' ');
                encText = Base64.encodeToString(bytes);
            }
            else
            {
                encText = Base64.encode(decText);
            }

            if (_encField != null)    // should not be otherwise
                _encField.setText(encText);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getClass().getName() + ": " + e.getMessage(), "Encoding failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final LRFieldWithButtonPanel getFieldWithButtonPanel (final String btnName, final ActionListener l)
    {
        final LRFieldWithButtonPanel    p=new LRFieldWithButtonPanel();
        final JButton                    b=p.getButton();
        b.setText(btnName);
        b.addActionListener(l);

        final JTextField    f=p.getTextField();
        f.addActionListener(l);
        return p;
    }

    private final LRFieldWithButtonPanel getEncodePanel ()
    {
        final LRFieldWithButtonPanel    p=getFieldWithButtonPanel("Decode", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    decodeData();
                }
            });

        _encField = p.getTextField();
        return p;
    }

    private JTextField    _decField    /* =null */;
    protected void decodeData ()
    {
        try
        {
            final String    encData=(null == _encField) /* should not happen */ ? null : _encField.getText();
            final int        edLen=(null == encData) ? 0 : encData.length(),
                            edRem=edLen % Base64.BASE64_OUTPUT_BLOCK_LEN;
            final String    encText, decText;
            if (isAutoPadEnabled() && (edRem != 0))
            {
                final int            edPad=Base64.BASE64_OUTPUT_BLOCK_LEN - edRem;
                final StringBuilder    sb=new StringBuilder(edLen + edPad + Base64.BASE64_OUTPUT_BLOCK_LEN /* just in case */);
                sb.append(encData);

                for (int    edx=0; edx < edPad; edx++)
                    sb.append(Base64.BASE64_PAD_CHAR);
                encText = sb.toString();
            }
            else
                encText = encData;

            if (isBinaryData())
            {
                final byte[]    binData=Base64.decodeToBytes(encText);
                decText = Hex.toString(binData, ' ', true);
            }
            else
                decText = Base64.decode(encText);
            if (_decField != null)    // should not be otherwise
                _decField.setText(decText);
            // if auto-padded then update the field data
            if ((_encField != null) /* should not be otherwise */ && (encText != encData))
                _encField.setText(encText);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getClass().getName() + ": " + e.getMessage(), "Decoding failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final LRFieldWithButtonPanel getDecodePanel ()
    {
        final LRFieldWithButtonPanel    p=getFieldWithButtonPanel("Encode", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    encodeData();
                }
            });

        _decField = p.getTextField();
        return p;
    }
    /**
     * Default title of the application
     */
    public static final String    DEFAULT_TITLE="Base64 translator";

    protected void limitResizing ()
    {
        final Dimension    d=AWTUtils.checkDimensions(this, MIN_WIDTH, (-1), MIN_HEIGHT, MAX_HEIGHT);
        if (d != null)
            setSize(d);
    }
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        super.layoutComponent(elem);

        final Container    ctPane=getContentPane();
        ctPane.setLayout(new GridLayout(3, 1, 5, 5));
        // close the application if frame closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // make the frame appear in mid-screen by default
        setLocationRelativeTo(null);

        // give some initial size information
        final Dimension    dim=getInitialSize();
        setPreferredSize(dim);
        setSize(dim);

        final LRFieldWithButtonPanel    encPanel=getEncodePanel(),
                                        decPanel=getDecodePanel();
        ctPane.add(encPanel);
        ctPane.add(getChoicesPanel());
        ctPane.add(decPanel);

        setFocusTraversalPolicy(encPanel.getTextField(), encPanel.getButton(),
                                _binDataChkbx, _padDataChkbx,
                                decPanel.getTextField(), decPanel.getButton());

        addComponentListener(new ComponentAdapter() {
                /*
                 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
                 */
                @Override
                public void componentResized (ComponentEvent e)
                {
                    if (e != null)
                        limitResizing();
                }
            });
    }

    MainFrame ()
    {
        super(DEFAULT_TITLE);
    }
}
