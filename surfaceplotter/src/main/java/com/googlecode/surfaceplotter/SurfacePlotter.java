package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfacePlotter.java                                                                    *
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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Event;
import java.awt.Font;

/**
 * The class <code>SurfacePlotter</code> is the main class of Surface Plotter.
 * It extends <code>java.awt.applet.Applet</code> but also has the <code>main</code>
 * method, so the Surface Plotter can be run as an applet or an application.
 *
 * @author  Yanto Suryono
 */
 
public class SurfacePlotter extends Applet {
  public  static final String  APP_NAME    = "Surface Plotter";
  public  static final String  APP_VERSION = "Version 1.30b2";
  public  static final String  APP_VERSIGN = "1.30";
  public  static final String  APP_DATE    = "18 Oct 2001";

  private final String DISPOSE = "Dispose Surface Plotter";
  private final String SHOW    = "Show Surface Plotter";

  private Button       button;
  private SurfaceFrame frame;
    
  /**
   * Initializes Surface Plotter applet.
   */
   
  public void init() {
    setFont(new Font("Dialog", Font.BOLD, 12));
    setLayout(new BorderLayout());
    add("Center", button = new Button(DISPOSE));    
    frame = new SurfaceFrame(this);
  }
  
  /**
   * Gets Surface Plotter applet information.
   */
   
  public String getAppletInfo() {
    return APP_NAME + " " + APP_VERSION + "\n" +
           "Copyright (c) 1996-97 Yanto Suryono\n" +
           "Compiled: " + APP_DATE + "\n\n";
  }

  /**
   * Disposes Surface Plotter frame.
   */
   
  public void disposeWindow() {
    frame.hide();
    button.setLabel(SHOW);
  }

  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target == button) {
      if (button.getLabel().equals(SHOW)) {
        frame.show();
        frame.requestFocus();
        button.setLabel(DISPOSE);
      }
      else {
        frame.hide();
        button.setLabel(SHOW);
      }
      return true; 
    }
    else return super.action(e,arg);
  }
  
  /**
   * The main method. Execution starts here when Surface Plotter is
   * run as an application.
   */
   
  public static void main(String args[]) {
    new SurfaceFrame(null);
  }
}

