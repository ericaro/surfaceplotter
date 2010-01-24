package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceBorder.java                                                                     *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
 *                                                                                        *
 * Copyright (c) Yanto Suryono <yanto@fedu.uec.ac.jp>                                     *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *                                                                                        *
 *----------------------------------------------------------------------------------------*/

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Panel;

/**
 * The class <code>SurfaceBorder</code> is a simple class that provides the given
 * component a nice 3D border.
 *
 * @author  Yanto Suryono
 */
 
public class SurfaceBorder extends Panel {
  /**
   * The constructor of <code>SurfaceBorder</code>.
   *
   * @param child the component to be provided with 3D border
   * @param bwidth the border width
   * @param bheight the border height
   */

  SurfaceBorder(Component child, int bwidth, int bheight) {
    super();
    setLayout(new BorderLayout(bwidth+2,bheight+2));
    add("North", new Canvas());
    add("South", new Canvas());
    add("East",  new Canvas());
    add("West",  new Canvas());
    add("Center", child);
  }

  /**
   * The constructor of <code>SurfaceBorder</code>.
   *
   * @param child the component to be provided with 3D border
   */
   
  SurfaceBorder(Component child) {
    this(child,0,0);
  }
  
  /**
   * Paints this component. Draws 3D border.
   *
   * @param g the graphics context to paint
   */
  
  public void paint(Graphics g) {
    g.setColor(getBackground());
    g.draw3DRect(1,1,bounds().width-2,bounds().height-2,false);
  }
  
  /**
   * Updates this component. Simply calls the <code>paint</code> method.
   *
   * @param g the graphics context to update
   */

  public void update(Graphics g) {
    paint(g);
  }
}

