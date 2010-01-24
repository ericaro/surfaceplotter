package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * StretchObserver.java                                                                   *
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

/**
 * The <code>Stretchbserver</code> interface is implemented by  
 * class that is intended to be notified when the z value stretching 
 * process should be started and when the z value stretching process is completed. 
 *
 * @author  Yanto Suryono
 */

public interface StretchObserver {  
  /**
   * Notifies that the z value stretching process is completed.
   */
   
  public void stretchComplete();

  /**
   * Notifies that the z value stretching process should be started.
   *
   * @param vertices the vertices array whose z values are to be stretched
   * @param oldmin the current minimum z value
   * @param oldmax the current maximum z value
   * @param newmin the goal minimum z value
   * @param newmax the goal maximum z value
   */

  public void startStretch(SurfaceVertex[][] vertices, 
                           float oldmin, float oldmax, float newmin, float newmax);
}

