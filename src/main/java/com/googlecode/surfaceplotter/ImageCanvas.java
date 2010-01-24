package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * ImageCanvas.java                                                                       *
 *                                                                                        *
 * Surface Plotter   version 1.30b1  17 May 1997                                          *
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * The class <code>ImageCanvas</code> is used to display the loaded image as
 * a preview before converted into surface data.
 *
 * @author  Yanto Suryono
 */

public final class ImageCanvas extends Canvas {
  private Image image;
  private int density;
  
  /**
   * The constructor of <code>ImageCanvas</code>
   *
   * @param image the image to be displayed and previewed
   */
   
  ImageCanvas(Image image) {      
    super();
    this.image = image;
    density = 20;
  }

  /**
   * Paints this canvas. Draws the image and conversion grids
   *
   * @param g the graphics context to paint                
   */

  public void paint(Graphics g) {
    g.drawImage(image,0,0,this);
    g.setColor(Color.red);

    Rectangle rect = bounds();
    int width = rect.width-1;
    int height = rect.height-1;
    
    for (int i=0; i <= density; i++) {
      int x = i*width/density;
      g.drawLine(x,0,x,height);  
    }

    for (int i=0; i <= density; i++) {
      int y = i*height/density;
      g.drawLine(0,y,width,y);  
    }
  }
  
  /**
   * Updates this canvas. Simply calls the <code>paint</code>
   * method
   *
   * @param g the graphics context to update                
   */

  public void update(Graphics g) {
    paint(g);
  }
    
  /**
   * Sets the density of the conversion grids
   *
   * @param density the new density
   */
   
  public void setDensity(int density) {
    this.density = density;
    repaint();
  }
  
  /**
   * Gets the currently previewed image
   *
   * @return the image
   */
   
  public Image currentImage() {
    return image;
  }
  
  /**
   * Sets the image to be previewed
   *
   * @param image the new image
   */
   
  public void setImage(Image image) {
    this.image = image;
    repaint();
  }
  
  /**
   * Determines the size of this canvas.
   * The size is normally the same with the image size.
   *
   * @return the preferred size
   */
   
  public Dimension preferredSize() {
    return new Dimension(image.getWidth(this),image.getHeight(this));
  }
}

