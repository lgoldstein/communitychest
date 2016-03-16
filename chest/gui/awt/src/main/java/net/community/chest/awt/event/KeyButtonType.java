/*
 *
 */
package net.community.chest.awt.event;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>An {@link Enum} encapsulating some special {@link KeyEvent} buttons</P>
 *
 * @author Lyor G.
 * @since Apr 28, 2009 9:26:51 AM
 */
public enum KeyButtonType {
    ENTER(KeyEvent.VK_ENTER),
    BACKSPACE(KeyEvent.VK_BACK_SPACE),
    TAB(KeyEvent.VK_TAB),
    CANCEL(KeyEvent.VK_CANCEL),
    CLEAR(KeyEvent.VK_CLEAR),
    PAUSE(KeyEvent.VK_PAUSE),
    ESCAPE(KeyEvent.VK_ESCAPE),

    PAGEUP(KeyEvent.VK_PAGE_UP),
    PAGEDOWN(KeyEvent.VK_PAGE_DOWN),
    END(KeyEvent.VK_END),
    HOME(KeyEvent.VK_HOME),

    DELETE(KeyEvent.VK_DELETE),
    INSERT(KeyEvent.VK_INSERT),

    LEFT(KeyEvent.VK_LEFT),
    UP(KeyEvent.VK_UP),
    RIGHT(KeyEvent.VK_RIGHT),
    DOWN(KeyEvent.VK_DOWN),

    CAPSLOCK(KeyEvent.VK_CAPS_LOCK),
    NUMLOCK(KeyEvent.VK_NUM_LOCK),
    SCROLLLOCK(KeyEvent.VK_SCROLL_LOCK),
    PRINTSCRN(KeyEvent.VK_PRINTSCREEN),

    // modifiers
    SHIFT(KeyEvent.VK_SHIFT),
    CONTROL(KeyEvent.VK_CONTROL),
    META(KeyEvent.VK_META),
    ALT(KeyEvent.VK_ALT),
    ALTGRAPH(KeyEvent.VK_ALT_GRAPH),
    WINDOWS(KeyEvent.VK_WINDOWS);

    public static final boolean isModifier (final int kv)
    {
        if ((KeyEvent.VK_SHIFT == kv)
         || (KeyEvent.VK_CONTROL == kv)
         || (KeyEvent.VK_META == kv)
         || (KeyEvent.VK_ALT == kv)
         || (KeyEvent.VK_ALT_GRAPH == kv)
         || (KeyEvent.VK_WINDOWS == kv))
            return true;

        return false;
    }

    public static final boolean isModifier (final KeyButtonType k)
    {
        return (null == k) ? false : isModifier(k.getKeyValue());
    }

    private final int    _kv;
    public final int getKeyValue ()
    {
        return _kv;
    }

    KeyButtonType (int kv)
    {
        _kv = kv;
    }

    public static final List<KeyButtonType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final KeyButtonType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final KeyButtonType fromKeyValue (final int kv)
    {
        if ((kv <= KeyEvent.VK_UNDEFINED) || (kv > 0x0FFFF))
            return null;

        for (final KeyButtonType v : VALUES)
        {
            if ((v != null) && (kv == v.getKeyValue()))
                return v;
        }

        return null;
    }
}
