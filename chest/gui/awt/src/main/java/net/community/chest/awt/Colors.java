package net.community.chest.awt;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * Copyright 2007 as per GPLv2
 *
 * Holds some well known/pre-defined {@link Color}-s
 * @author Lyor G.
 * @since Jun 24, 2007 1:37:08 PM
 */
public enum Colors {
    WHITE(Color.WHITE),
    LIGHT_GRAY(Color.LIGHT_GRAY),
    GRAY(Color.GRAY),
    DARK_GRAY(Color.DARK_GRAY),
    BLACK(Color.BLACK),
    RED(Color.RED),
    PINK(Color.PINK),
    ORANGE(Color.ORANGE),
    YELLOW(Color.YELLOW),
    GREEN(Color.GREEN),
    MAGENTA(Color.MAGENTA),
    CYAN(Color.CYAN),
    BLUE(Color.BLUE);

    private final Color    _color;
    public Color getColor ()
    {
        return _color;
    }

    Colors (Color c)
    {
        _color = c;
    }

    public static final List<Colors>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    /**
     * @param s {@link String} containing one of the defined enum values
     * @return matching enum (case insensitive) - null if no match found
     * (or if null/empty string to begin with)
     */
    public static Colors fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
    /**
     * @param c {@link Color} instance
     * @return matching enum - null if no match (or null {@link Color} to
     * begin with)
     */
    public static Colors fromColor (final Color c)
    {
        if (null == c)
            return null;

        for (final Colors v : VALUES)
        {
            if ((v != null) && c.equals(v.getColor()))
                return v;
        }

        return null;    // no match
    }
    /**
     * @param rgb The RGB value
     * @return matching enum - null if no match
     */
    public static Colors fromRGB (final int rgb)
    {
        for (final Colors v : VALUES)
        {
            final Color    c=v.getColor();
            if (c.getRGB() == rgb)
                return v;
        }

        return null;    // no match
    }
}
