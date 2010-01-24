package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceLogo.java                                                                       *
 *                                                                                        *
 * Surface Plotter   version 1.20     8 Nov 1996                                          *
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;

/**
 * The class <code>SurfaceLogo</code> produces Surface Plotter logo.
 *
 * @author  Yanto Suryono
 */
 
public final class SurfaceLogo extends Panel {
  private final int SPACE = 10;
  private int preferredwidth;
    
  /**
   * The constructor of <code>SurfaceLogo</code>
   */
   
  SurfaceLogo() {
    super();
    preferredwidth = 0;
  }

  /**
   * Notifies the logo that it has been added to a container.
   * Modifies the font to be bigger and bold-faced.
   */
   
  public void addNotify() {
    super.addNotify();
    setFont(new Font(getFont().getName(),Font.BOLD,getFont().getSize()+1));

    FontMetrics fm = getFontMetrics(getFont());
    if (fm != null) {
      preferredwidth = fm.stringWidth(SurfacePlotter.APP_NAME)+2*SPACE;
    }    
  }
    
  /**
   * Paints the logo.
   *
   * @param g the graphics context to paint
   */
   
  public void paint(Graphics g) {    
    FontMetrics fm = g.getFontMetrics(g.getFont());
    int leveladjust = (bounds().height-4-2*fm.getAscent())/4,x,y;

    x = (bounds().width-fm.stringWidth(SurfacePlotter.APP_NAME))/2; 
    y = bounds().height/2-leveladjust;

    g.setColor(Color.black);
    g.drawString(SurfacePlotter.APP_NAME,x,y);
    g.setColor(Color.white);
    g.drawString(SurfacePlotter.APP_NAME,x-1,y-1);

    x = (bounds().width-fm.stringWidth(SurfacePlotter.APP_VERSION))/2; 
    y = bounds().height-3-leveladjust;

    g.setColor(Color.black);
    g.drawString(SurfacePlotter.APP_VERSION,x,y);
    g.setColor(Color.yellow);
    g.drawString(SurfacePlotter.APP_VERSION,x-1,y-1);

    g.setColor(Color.lightGray);
    g.draw3DRect(0,0,bounds().width-1,bounds().height-1,false);
    g.draw3DRect(1,1,bounds().width-3,bounds().height-3,false);
  }
  
  /**
   * Determines the size of this logo.
   *
   * @return the size of this logo
   */

  public Dimension preferredSize() {
    return new Dimension(preferredwidth,0);
  }
}


