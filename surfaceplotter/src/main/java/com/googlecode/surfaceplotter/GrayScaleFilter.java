package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * GrayScaleFilter.java                                                                   *
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

import java.awt.image.RGBImageFilter;

/**
 * The class <code>GrayScaleFilter</code> filters 24-bit RGB color 
 * values into gray scale values
 *
 * @author  Yanto Suryono
 */

public final class GrayScaleFilter extends RGBImageFilter {

  /**
   * The contructor of <code>GrayScaleFilter</code>
   */
   
  GrayScaleFilter() {
    canFilterIndexColorModel = true;
  }
  
  /**
   * Filters 24-bit RGB value into the correspondence gray scale value
   * by computing the average of the red, green, and blue values
   *
   * @param x   the x coordinate (ignored)
   * @param y   the y coordinate (ignored)
   * @param rgb the 32-bit alpha-RGB value
   */
   
  public int filterRGB(int x, int y, int rgb) {
    int a = rgb & 0xff000000;
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >>  8) & 0xff;
    int b = (rgb >>  0) & 0xff;

    b = (r + g + b)/3;
    g = b << 8;
    r = g << 8;
    
    return a|r|g|b;
  }
}

