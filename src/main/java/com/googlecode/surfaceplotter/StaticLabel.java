package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * StaticLabel.java                                                                       *
 *                                                                                        *
 * Surface Plotter   version 1.30b1  17 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
 *                                                                                        *
 * Copyright (c) Yanto Suryono <yanto@fedu.uec.ac.jp>                                     *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU Lesser General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU Lesser General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *                                                                                        *
 *----------------------------------------------------------------------------------------*/

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * The class <code>StaticLabel</code> produces label component with 3D border.
 *
 * @author  Yanto Suryono
 */
 
public final class StaticLabel extends Canvas {
  private String text;
  private String reftext;
  private final int margin = 5; 
  private int width;
  private boolean clear;
  
  /**
   * The constructor of <code>StaticLabel</code>.
   *
   * @param text    the text to display
   * @param reftext the text to be a reference when computing component size
   */
   
  StaticLabel(String text, String reftext) {
    super();
    clear = true; width = 0;
    this.text = new String(text);
    this.reftext = new String(reftext);
  }
  
  /**
   * The constructor of <code>StaticLabel</code>.
   * Use the given text as reference text.
   *
   * @param text the text to display, also the reference text
   */
      
  StaticLabel(String text) {
    this(text,text);
  }

  /**
   * The constructor of <code>StaticLabel</code>.
   * No text to display at the first time. The size of this component
   * depends on the value computed by the layout manager.
   */
    
  StaticLabel() {
    this("");
  }
  
  /**
   * Paints this component.
   *
   * @param g the graphics context to paint
   */
   
  public void paint(Graphics g) {
    int height = bounds().height;
    FontMetrics fm = g.getFontMetrics();
    int y = (height + fm.getMaxAscent()) / 2;   
    if (clear) {
      g.setColor(getBackground());
      g.fillRect(margin,1,bounds().width-margin-1,height-2);    
      g.setColor(getForeground());
      clear = false;
    }
    g.drawString(text,margin,y);    
    g.setColor(getBackground());
    g.draw3DRect(0,0,bounds().width-1,height-1,false);
  }

  /**
   * Updates this component. Simply calls the <code>paint</code> method.
   *
   * @param g the graphics context to update
   */

  public void update(Graphics g) {
    paint(g);
  }
    
  /**
   * Sets the text to display.
   *
   * @param newtext the new text
   */
   
  public void setText(String newtext) {
    if (text.equals(newtext)) return;
    text = new String(newtext);
    clear = true;
    
    // forces repaint now
    
    Graphics g = getGraphics();
    paint(g);
    g.dispose();
  }
   
  /**
   * Notifies the label that it has been added to a container.
   * Computes the preferred width.
   */
   
  public void addNotify() {
    super.addNotify();

    FontMetrics fm = getFontMetrics(getFont());
    if (fm != null) {
      width = fm.stringWidth(reftext) + margin*2;
    }    
  }
  
  /**
   * Determines the size of this component.
   *
   * @return the size of this component
   */
   
  public Dimension preferredSize() {
    return new Dimension(width,0);
  }
}
