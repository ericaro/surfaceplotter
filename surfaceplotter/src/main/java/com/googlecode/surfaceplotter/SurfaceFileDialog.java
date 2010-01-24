package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceFileDialog.java                                                                 *
 *                                                                                        *
 * WARNING: Recompile it using JDK 1.0.2 in order to maintain backward compatibility      *
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

import java.awt.FileDialog;
import java.awt.Frame;

/**
 * The class <code>SurfaceFileDialog</code> provides interface to file dialog in 
 * JDK 1.0.2. Because of JDK incompatibilities, access to FileDialog object that was
 * compiled using JDK 1.1 will not run on Linux with JDK 1.0.2.
 *
 * @author  Yanto Suryono
 */
 
public final class SurfaceFileDialog {
  /**
   * The constructor of <code>SurfaceFileDialog</code>
   *
   * @param parentframe the parent frame
   * @param title the title
   * @param save <code>true</code> if file save dialog
   */

  SurfaceFileDialog(Frame parentframe, String title, boolean save) {      
    if (save)
      fd = new FileDialog(parentframe,title,FileDialog.SAVE);
    else
      fd = new FileDialog(parentframe,title);
  }
  
  /**
   * Shows the file dialog. This should block the current thread
   * until the user closes the dialog box.
   *
   * @return the selected file name
   */
   
  public String getFilename() {
    fd.show();    
    if (fd.getFile() != null) return fd.getDirectory() + fd.getFile();
    return null;
  }
  
  private FileDialog fd;
}

