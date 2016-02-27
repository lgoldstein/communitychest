package net.community.chest.ui.components.icon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Shape of filled area</P>
 * @author Lyor G.
 * @since Jan 27, 2009 1:43:28 PM
 */
public enum IconShape {
	OVAL {	// by playing with the Insets can achieve ellipse
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				if ((null == g) || (null == shapeColor) || (null == r))
					return;
	
				g.setColor(shapeColor);
				g.fillOval(r.x, r.y, r.width, r.height);
			}
		},
	RECT {	// by playing with the Insets can achieve rectangles
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				if ((null == g) || (null == shapeColor) || (null == r))
					return;
	
				g.setColor(shapeColor);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		},
	NORTHANGLE {	// triangle pointing up
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.NORTH, shapeColor);
			}
		},
	SOUTHANGLE {	// triangle pointing down
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.SOUTH, shapeColor);
			}
		},
	WESTANGLE {	// triangle pointing left
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.WEST, shapeColor);
			}
		},
	EASTANGLE {	// triangle pointing right
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.EAST, shapeColor);
			}
		},
	NWTANGLE {	// triangle pointing northwest
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.PAGE_START, shapeColor);
			}
		},
	NETANGLE {	// triangle pointing northeast
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.LINE_END, shapeColor);
			}
		},
	SETANGLE {	// triangle pointing southeast
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.PAGE_END, shapeColor);
			}
		},
	SWTANGLE {	// triangle pointing southwest
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				paintNormalizedTrangleShape(g, r, BorderLayoutPosition.LINE_START, shapeColor);
			}
		},
	/**
	 * Special placeholder for extensions
	 */
	UNKNOWN {
			@Override
			public void paintNormalizedShape (Graphics g, Rectangle r, Color shapeColor)
			{
				return;
			}
		},
	/*
	ROMBOID
	*/
	;	


	public static final Polygon createTriangle (Rectangle r, BorderLayoutPosition ptr)
	{
		if ((null == ptr) || (null == r))
			return null;

		final Polygon	p=new Polygon();
		switch(ptr)
		{
			case NORTH	:
			case SOUTH	:
				{
					final int	midWidth=r.width / 2;
					if (BorderLayoutPosition.NORTH.equals(ptr))
					{
						p.addPoint(r.x, r.y + r.height);
						p.addPoint(r.x + r.width, r.y + r.height);
						p.addPoint(r.x + midWidth, r.y);
					}
					else
					{
						p.addPoint(r.x, r.y);
						p.addPoint(r.x + r.width, r.y);
						p.addPoint(r.x + midWidth, r.y + r.height);
					}
				}
				break;

			case EAST	:
			case WEST	:
				{
					final int	midHeight=r.height / 2;
					if (BorderLayoutPosition.EAST.equals(ptr))
					{
						p.addPoint(r.x, r.y);
						p.addPoint(r.x, r.y + r.height);
						p.addPoint(r.x + r.width, r.y + midHeight);
					}
					else
					{
						p.addPoint(r.x + r.width, r.y);
						p.addPoint(r.x + r.width, r.y + r.height);
						p.addPoint(r.x, r.y + midHeight);
					}
				}
				break;

			case PAGE_START	:
				p.addPoint(r.x, r.y);
				p.addPoint(r.x, r.y + r.height);
				p.addPoint(r.x + r.width, r.y);
				break;

			case PAGE_END	:
				p.addPoint(r.x + r.width, r.y + r.height);
				p.addPoint(r.x + r.width, r.y);
				p.addPoint(r.x, r.y + r.height);
				break;

			case LINE_START	:
				p.addPoint(r.x, r.y + r.height);
				p.addPoint(r.x, r.y);
				p.addPoint(r.x + r.width, r.y + r.height);
				break;

			case LINE_END	:
				p.addPoint(r.x + r.width, r.y);
				p.addPoint(r.x + r.width, r.y + r.height);
				p.addPoint(r.x, r.y);
				break;

			default		:
				throw new IllegalArgumentException("createTriangle(" + r + ")[" + ptr + "] unknown position");
		}

		return p;
	}

	public static final void paintNormalizedTrangleShape (
			Graphics g, Rectangle r, BorderLayoutPosition ptr, Color shapeColor)
	{
		if ((null == g) || (null == shapeColor))
			return;

		final Polygon	p=createTriangle(r, ptr);
		if (null == p)
			return;

		g.setColor(shapeColor);
		g.fillPolygon(p);
	}

	public abstract void paintNormalizedShape (Graphics g, Rectangle rect, Color shapeColor);

	public static final List<IconShape>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final IconShape fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}