package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceDialog.java                                                                     *
 *                                                                                        *
 * WARNING: Recompile it using JDK 1.0.2 in order to maintain backward compatibility      *
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

import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Insets;

/**
 * The class <code>DialogLauncher</code> launches a dialog box by calling
 * its <code>show</code> method from a separate thread. The thread may
 * be blocked until the dialog box closes, but the thread that creates
 * this class may not.
 *
 * @author  Yanto Suryono
 */

class DialogLauncher extends Thread {
  SurfaceDialog dialog;
  
  /**
   * The constructor of <code>SurfaceDialog</code>.
   *
   * @param dialog the dialog box to launch.
   * @param title the title of the dialog box.
   * @see SurfaceDialog
   */

  DialogLauncher(SurfaceDialog dialog) {
    super();
    this.dialog = dialog;
  }
  
  public void run() {
    dialog.show();
  } 
}

/**
 * The class <code>SurfaceDialog</code> produces modal dialog box. This class 
 * extends <code>java.awt.Dialog</code> class and fixes annoying bugs.
 *
 * <b>Warning:</b> Do not call the <code>show</code> method of this class 
 * directly. Use <code>showDialog</code>instead.
 *
 * @author  Yanto Suryono
 * @version 1.30b1, 14 May 1997
 * @since   1.30b1
 */

public class SurfaceDialog extends Dialog {
  
  /**
   * The constructor of <code>SurfaceDialog</code>.
   *
   * @param parentframe the parent frame.
   * @param title the title of the dialog box.
   */
   
  SurfaceDialog(Frame parentframe, String title) {      
    super(parentframe,title,true);
    setResizable(false);
    launcher = null;
  }

  /**
   * Returns the insets of this dialog box.
   * This fixes bug in some JVM that miscalculated the
   * height of applet dialog box due to the presence
   * of security warning message.
   *
   * @return the dialog box insets.
   */
   
  public Insets insets() {
    return dialoginsets;
  }

  /**
   * Shows this dialog box. This will create a 
   * <code>DialogLauncher</code> instance and start it.
   *
   * @see DialogLauncer
   */
    
  public void showDialog() {
    if (launcher == null) {
      launcher = new DialogLauncher(this);
      launcher.start();
    }
  }

  /**
   * Disposes this dialog box. 
   */
       
  public void dispose() {
    super.dispose();
    launcher = null;
  }
  
  /**
   * Handles events.  
   *
   * @param e the event
   * @param obj additional information from event manager 
   */

  public boolean handleEvent(Event e) {
    if (e.id == Event.WINDOW_DESTROY) {
      dispose();
    }
    else 
    return super.handleEvent(e);
    return true;
  }
  
  private static Insets dialoginsets;
  private DialogLauncher launcher;
  
  /**
   * Creates an invisible frame and gets the insets of the frame.
   * This insets will be use as insets of dialog boxes.
   */
  
  static {
    Frame measurewindow;
    
    measurewindow = new Frame();
    measurewindow.addNotify();
    dialoginsets = measurewindow.insets();
    measurewindow.dispose();
  }
}

