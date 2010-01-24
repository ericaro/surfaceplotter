package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceAbout.java                                                                      *
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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

/**
 * The class <code>SurfaceAbout</code> produces dialog box that displays
 * information about Surface Plotter.
 *
 * @author  Yanto Suryono
 */
 
public final class SurfaceAbout extends SurfaceDialog {
  /**
   * The constructor of <code>SurfaceAbout</code>
   *
   * @param parentframe the parent frame
   */

  SurfaceAbout(Frame parentframe) {      
    super(parentframe,"About");
    
    Panel panel, buttonpanel;
    setBackground(Color.lightGray);
    setFont(new Font("Dialog",Font.PLAIN,12));    
    setLayout(new BorderLayout(10,10));
    add("North",new Canvas());
    add("East", new Canvas());
    add("West", new Canvas());
    add("South",new Canvas());

    add("Center", panel = new Panel());    
    panel.setLayout(new BorderLayout());
    
    Panel content;
    panel.add("Center", new SurfaceBorder(content = new Panel(),20,10)); 
    panel.add("South", panel = new Panel());

    Button close;
    panel.add(close = new Button("Close"));    
    content.setLayout(new BorderLayout());

    Label label;
    content.add("North", 
                label = new Label(
                SurfacePlotter.APP_NAME + " " + 
                SurfacePlotter.APP_VERSION));
    label.setFont(new Font("Helvetica",Font.BOLD,16));

    content.add("Center", content = new Panel());
    content.setLayout(new GridLayout(11,1));    
    content.add(new Label("Copyright (c) 1996-97 Yanto Suryono"));
    content.add(new Label("All Rights Reserved"));
    content.add(new Label());    
    content.add(new Label("e-mail: yanto@fedu.uec.ac.jp"));
    content.add(new Label("URL: http://yanto.home.dhs.org"));
    content.add(new Label());
    content.add(new Label("Special thanks to: Mark Westenberger <mjw@www.ottawa.net>"));
    content.add(new Label());
    content.add(new Label("The HTML help browser uses JavaBrowser package"));
    content.add(new Label("written by Jeremy Cook <Jeremy.Cook@ii.uib.no> and"));
    content.add(new Label("Alexey Goloshubin <s667@ii.uib.no>"));

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

