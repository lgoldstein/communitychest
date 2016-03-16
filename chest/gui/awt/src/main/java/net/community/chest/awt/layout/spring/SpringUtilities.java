/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.community.chest.awt.layout.spring;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * <P>Adjusted source code from Sun</P>
 *
 * @author Lyor G.
 * @since Apr 23, 2009 8:16:06 AM
 */
public final class SpringUtilities {
    private SpringUtilities ()
    {
        // no instance
    }
    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of
     * <code>parent</code> in a grid. Each component is as big as the maximum
     * preferred width and height of the components. The parent is made just
     * big enough to fit them all.
     * @param parent The {@link Container} for creating the grid
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     * @throws IllegalArgumentException if parent container layout is not
     * {@link SpringLayout}, or non-positive rows/columns, or not enough
     * components in parent container to populate the grid
     */
    public static void makeGrid (final Container         parent,
                                 final int rows,         final int cols,
                                 final int initialX,    final int initialY,
                                 final int xPad,         final int yPad)
        throws IllegalArgumentException
    {
        if (null == parent)
            return;

        if ((rows <= 0) || (cols <= 0))
            throw new IllegalArgumentException(
                    "makeGrid(" + parent + ")[" + rows + "," + cols + "][" + initialX + "," + initialY + "]["
                    + xPad + "," + yPad + "] bad rows/columns values");

        final LayoutManager    lm=parent.getLayout();
        if (!(lm instanceof SpringLayout))
            throw new IllegalArgumentException(
                    "makeGrid(" + parent + ")[" + rows + "," + cols + "][" + initialX + "," + initialY + "]["
                    + xPad + "," + yPad + "] parent container is not " + SpringLayout.class.getSimpleName());

        final SpringLayout    layout=(SpringLayout) lm;
        final Spring        xPadSpring=Spring.constant(Math.max(xPad, 0)),
                            yPadSpring=Spring.constant(Math.max(yPad, 0)),
                            initialXSpring=Spring.constant(Math.max(initialX, 0)),
                            initialYSpring=Spring.constant(Math.max(initialY, 0));
        final int            max=rows * cols, numComps=parent.getComponentCount();
        if (max < numComps)
            throw new IllegalArgumentException(
                    "makeGrid(" + parent + ")[" + rows + "," + cols + "][" + initialX + "," + initialY + "]["
                    + xPad + "," + yPad + "] parent container contains only " + numComps
                    + " instead of the required " + max + " components");

        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.
        final Component    c0=parent.getComponent(0);
        Spring            maxWidthSpring=layout.getConstraints(c0).getWidth(),
                        maxHeightSpring=layout.getConstraints(c0).getWidth();
        for (int i = 1; i < max; i++)
        {
            final Component                    c=parent.getComponent(i);
            final SpringLayout.Constraints    cons=layout.getConstraints(c);
            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        //Apply the new width/height Spring. This forces all the
        //components to have the same size.
        for (int i = 0; i < max; i++)
        {
            final Component                    c=parent.getComponent(i);
            final SpringLayout.Constraints    cons=layout.getConstraints(c);
            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.
        SpringLayout.Constraints    lastCons=null,lastRowCons=null;
        for (int i = 0; i < max; i++)
        {
            final Component                c=parent.getComponent(i);
            SpringLayout.Constraints    cons=layout.getConstraints(c);
            if ((i % cols) == 0)
            { //start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            }
            else    //x position depends on previous component
            {
                final Spring    ec=lastCons.getConstraint(SpringLayout.EAST),
                                xVal=Spring.sum(ec, xPadSpring);
                cons.setX(xVal);
            }

            if ((i / cols) == 0) //first row
            {
                cons.setY(initialYSpring);
            }
            else    //y position depends on previous row
            {
                final Spring    sc=lastRowCons.getConstraint(SpringLayout.SOUTH),
                                yVal=Spring.sum(sc, yPadSpring);
                cons.setY(yVal);
            }

            lastCons = cons;
        }

        //Set the parent's size.
        final SpringLayout.Constraints     pCons=layout.getConstraints(parent);
        final Spring                    yc=Spring.constant(yPad),
                                        sc=lastCons.getConstraint(SpringLayout.SOUTH),
                                        ssc=Spring.sum(yc, sc),
                                        xc=Spring.constant(xPad),
                                        ec=lastCons.getConstraint(SpringLayout.EAST),
                                        eec=Spring.sum(xc, ec);
        pCons.setConstraint(SpringLayout.SOUTH, ssc);
        pCons.setConstraint(SpringLayout.EAST, eec);
    }
    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell (
                     final int row, final int col,
                     final SpringLayout    layout,
                     final Container     parent,
                     final int             cols)
    {
        final Component    c=parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of
     * <code>parent</code> in a grid. Each component in a column is as wide
     * as the maximum preferred width of the components in that column;
     * height is similarly determined for each row. The parent is made just
     * big enough to fit them all.
     * @param parent The {@link Container} to use for the grid
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     * @throws IllegalArgumentException if parent container layout is not
     * {@link SpringLayout}, or non-positive rows/columns, or not enough
     * components in parent container to populate the grid
     */
    public static void makeCompactGrid (final Container     parent,
                                        final int rows,     final int cols,
                                        final int initialX,    final int initialY,
                                        final int xPad,     final int yPad)
        throws IllegalArgumentException
    {
        if (null == parent)
            return;

        if ((rows <= 0) || (cols <= 0))
            throw new IllegalArgumentException(
                    "makeCompactGrid(" + parent + ")[" + rows + "," + cols + "][" + initialX + "," + initialY + "]["
                    + xPad + "," + yPad + "] bad rows/columns values");

        final LayoutManager    lm=parent.getLayout();
        if (!(lm instanceof SpringLayout))
            throw new IllegalArgumentException(
                    "makeCompactGrid(" + parent + ")[" + rows + "," + cols + "][" + initialX + "," + initialY + "]["
                    + xPad + "," + yPad + "] parent container is not " + SpringLayout.class.getSimpleName());

        final SpringLayout    layout=(SpringLayout) lm;
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++)
        {
            Spring width=Spring.constant(0);
            for (int r = 0; r < rows; r++)
            {
                final SpringLayout.Constraints    cc=getConstraintsForCell(r, c, layout, parent, cols);
                final Spring                    w=cc.getWidth();
                width = Spring.max(width, w);
            }

            for (int r = 0; r < rows; r++)
            {
                final SpringLayout.Constraints constraints=
                        getConstraintsForCell(r, c, layout, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }

            final Spring    xs=Spring.constant(xPad),
                            xx=Spring.sum(width, xs);
            x = Spring.sum(x, xx);
        }

        //Align all cells in each row and make them the same height.
        Spring y=Spring.constant(initialY);
        for (int r = 0; r < rows; r++)
        {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++)
            {
                final SpringLayout.Constraints    cc=getConstraintsForCell(r, c, layout, parent, cols);
                final Spring                    h=cc.getHeight();
                height = Spring.max(height, h);
            }

            for (int c = 0; c < cols; c++)
            {
                final SpringLayout.Constraints constraints=
                        getConstraintsForCell(r, c, layout, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }

            final Spring    ys=Spring.constant(yPad),
                            yy=Spring.sum(height, ys);
            y = Spring.sum(y, yy);
        }

        //Set the parent's size.
        final SpringLayout.Constraints pCons=layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
