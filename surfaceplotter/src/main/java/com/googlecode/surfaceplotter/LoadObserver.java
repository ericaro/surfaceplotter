package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * LoadObserver.java                                                                      *
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

import java.awt.Image;

/**
 * The <code>LoadObserver</code> interface is implemented by  
 * class that is intended to be notified when the image loading process is completed
 * and when the conversion process should be started 
 *
 * @author  Yanto Suryono
 */
 
public interface LoadObserver {
  /**
   * Notifies that the image loading process is completed.
   * This method will not be called id user aborted the loading process.
   *
   * @param image the loaded image
   */
    
  public void loadComplete(Image image);

  /**
   * Notifies that the image to surface conversion process should be started.
   *
   * @param image      the image to be converted
   * @param divisions  the number of divisions should be made to the image  
   */
    
  public void convertLoadedImage(Image image, int divisions);
}

