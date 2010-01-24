package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceHelp.java                                                                       *
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


import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.io.File;
import java.net.URL;

import com.googlecode.surfaceplotter.browser.BrowserInterface;


/**
 * The class <code>SurfaceHelp</code> produces a frame that contains BrowserInterface
 * class, an HTML browser class written by Jeremy Cook (Jeremy.Cook@ii.uib.no) and 
 * Alexey Goloshubin (s667@ii.uib.no). 
 *
 * @author  Yanto Suryono
 */

public class SurfaceHelp extends Frame {

  /**
   * The constructor of <code>SurfaceHelp</code>
   *
   * @param filename the HTML file name to browse.
   * @param applet the applet that launched this help frame.
   */

  public SurfaceHelp(String filename, Applet applet) {
    setTitle("Surface Plotter Help");
    setBackground(Color.lightGray);
    setFont(new Font("Helvetica",Font.PLAIN,12));
    
    Panel panel = new Panel();
    panel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    panel.setFont(new Font("Helvetica",Font.BOLD,12));
      
    panel.add(new Button("Back"));
    panel.add(new Button("Forward"));
    panel.add(new Button("Reload"));
    panel.add(new Button("Close"));

    add("North",panel);
    
    this.applet = applet;
    url = createURL(filename);
    browser = new BrowserInterface(this);
    resize(500,600);
  }
  
  /**
   * Shows this frame. 
   */
   
  public void show() {
    super.show();
    this.requestFocus();
    if (url != null) browser.URL_Process(url.toString(),null);    
  }

  /**
   * Shows this frame and loads new HTML file.
   *
   * @param filename the HTML file name to browse.
   */
   
  public void show(String filename) {
    url = createURL(filename);
    show();
  }
   
  /**
   * Handles events. Processes <code>java.awt.Event.WINDOW_DESTROY</code>
   * only and passes other events to the parent frame.
   *
   * @param e the event
   */
    
  public boolean handleEvent(Event e) {
    if (e.id == Event.WINDOW_DESTROY) {
      dispose();
      return true;
    }
    return super.handleEvent(e);
  }

  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */
   
  public boolean action(Event evt, Object arg) {
    if (arg.equals("Close")) {
      dispose();
    }
    else 
    if (arg.equals("Reload")) {
      browser.URL_Process(browser.getURLName(),null);
    }
    else 
    if (arg.equals("Back")) {
      browser.goBack();
    }
    else if (arg.equals("Forward")) {
      browser.goForward();
    }
    else return super.action(evt, arg);
    return true;
  }

  /**
   * Creates an URL of the given relative file name.
   *
   * @param filename the relative file name.
   * @return the URL of the given file.
   */
   
  private URL createURL(String filename) {
    // Creates file URL
    
    URL url = null;
   
    if (applet != null) {
      try {
        url = new URL(applet.getCodeBase(),filename);
      }
      catch (Exception e) {}
    }
    else {
      File file = new File(filename);
      String fileurl = file.getAbsolutePath();
      fileurl = fileurl.replace(File.separatorChar,'/');
     
      // MS-DOS file url
      if (fileurl.indexOf(":/") == 1) {
        fileurl = "/" + fileurl.substring(0,1) +
                  "|/" + fileurl.substring(3);  
      }
      try {
        url = new URL("file:" + fileurl);
      }
      catch (Exception e) {}
    }
    return url;
  }
   
  private URL url;                    // Starting URL 
  private Applet applet;              // The parent applet
  private BrowserInterface browser;   // The browser object
}


