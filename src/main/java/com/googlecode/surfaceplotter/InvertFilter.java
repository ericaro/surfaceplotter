package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * InvertFilter.java                                                                      *
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
 * The class <code>InvertFilter</code> inverts 24-bit RGB color values
 *
 * @author  Yanto Suryono
 */

public final class InvertFilter extends RGBImageFilter {
  InvertFilter() {
    canFilterIndexColorModel = true;
  }
  
  /**
   * Inverts 24-bit RGB value
   *
   * @param x   the x coordinate (ignored)
   * @param y   the y coordinate (ignored)
   * @param rgb the 32-bit alpha-RGB value
   */
   
  public int filterRGB(int x, int y, int rgb) {
    int a = rgb & 0xff000000;
    rgb = (0x00ffffff - rgb) & 0x00ffffff;
    return a|rgb;
  }
}

