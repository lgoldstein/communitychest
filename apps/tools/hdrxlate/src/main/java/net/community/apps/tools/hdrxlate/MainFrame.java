package net.community.apps.tools.hdrxlate;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.community.chest.awt.AWTUtils;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.ui.helpers.frame.HelperFrame;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 12:29:00 PM
 */
final class MainFrame extends HelperFrame {
    /**
     *
     */
    private static final long serialVersionUID = 8458857147165080083L;
    public static final int            MIN_WIDTH=150, MIN_HEIGHT=120, MAX_HEIGHT=150;
    public static final Dimension    DEFAULT_INITIAL_SIZE=new Dimension(400, MAX_HEIGHT);

    protected Dimension getInitialSize ()
    {
        return DEFAULT_INITIAL_SIZE;
    }

    public static final Insets    COMMON_INSETS=new Insets(5,5,5,5);

    private JTextField    _encField    /* =null */;
    protected void encodeHeader ()
    {
        try
        {
            final String    decText=(null == _decField) /* should not happen */ ? null : _decField.getText(),
                            encText=RFCHeaderDefinitions.encodeHdrValue(decText, null);
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

    private final JPanel getEncodePanel ()
    {
        final LRFieldWithButtonPanel    p=getFieldWithButtonPanel("Decode", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    decodeHeader();
                }
            });

        _encField = p.getTextField();
        return p;
    }

    private JTextField    _decField    /* =null */;
    protected void decodeHeader ()
    {
        try
        {
            final String    encText=(null == _encField) /* should not happen */ ? null : _encField.getText(),
                            decText=RFCHeaderDefinitions.decodeHdrValue(encText, true);
            if (_decField != null)    // should not be otherwise
                _decField.setText(decText);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getClass().getName() + ": " + e.getMessage(), "Decoding failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final JPanel getDecodePanel ()
    {
        final LRFieldWithButtonPanel    p=getFieldWithButtonPanel("Encode", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    encodeHeader();
                }
            });

        _decField = p.getTextField();
        return p;
    }
    /**
     * Default title of the application
     */
    public static final String    DEFAULT_TITLE="Headers translator";

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

        ctPane.add(getEncodePanel());
        ctPane.add(getDecodePanel());

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
