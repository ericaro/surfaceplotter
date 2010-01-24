package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceSysInfo.java                                                                    *
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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

/**
 * The class <code>SurfaceSysInfo</code> produces dialog box that displays
 * system information and JVM information on which Surface Plotter is running.
 *
 * @author  Yanto Suryono
 */
 
public final class SurfaceSysInfo extends SurfaceDialog {
  /**
   * The constructor of <code>SurfaceSysInfo</code>
   *
   * @param parentframe the parent frame
   */

  SurfaceSysInfo(Frame parentframe) {      
    super(parentframe,"System Information");
    
    Panel panel, buttonpanel;
    setBackground(Color.lightGray);
    setFont(new Font("Dialog",Font.PLAIN,12));    
    setLayout(new BorderLayout(10,10));
    add("North",new Canvas());
    add("East", new Canvas());
    add("West", new Canvas());
    add("South",new Canvas());

    add("Center",panel = new Panel());    
    panel.setLayout(new BorderLayout());
    
    Panel content;
    panel.add("Center", new SurfaceBorder(content = new Panel(),10,10)); 
    panel.add("South", panel = new Panel());

    Button close;
    panel.add(close = new Button("Close"));
    
    content.setLayout(new FlowLayout());
    content.add(content = new Panel());
    content.setLayout(new BorderLayout());
    content.add("West", panel = new Panel());
    content.add("East", content = new Panel());
    
    panel.setLayout(new GridLayout(6,1));
    content.setLayout(new GridLayout(6,1));
    
    panel.add(new Label("Java Interpreter Version: "));
    panel.add(new Label("Java Vendor: "));
    panel.add(new Label("Java Vendor URL: "));
    panel.add(new Label("Java Class Version: "));
    panel.add(new Label("Operating System: "));
    panel.add(new Label("Architecture: "));
        
    content.add(new Label(System.getProperty("java.version")));
    content.add(new Label(System.getProperty("java.vendor")));
    content.add(new Label(System.getProperty("java.vendor.url")));
    content.add(new Label(System.getProperty("java.class.version")));
    content.add(new Label(System.getProperty("os.name") + " " +
                          System.getProperty("os.version")));
    content.add(new Label(System.getProperty("os.arch")));

    pack();
 
    close.requestFocus();
    showDialog();
  }
  
  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target instanceof Button) {
      dispose();
    }
    else return super.action(e,arg);
    return true; 
  }
}

