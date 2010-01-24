package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * URLInput.java                                                                          *
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
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

/**
 * The class <code>URLInput</code> produces dialog box to allow user
 * enters the URL of image to load.
 *
 * @author  Yanto Suryono
 */

public final class URLInput extends SurfaceDialog {
  private Button ok,cancel;
  private TextField url;
  
  /**
   * The constructor of <code>URLInput</code>.
   *
   * @param parentframe the parent frame
   */
   
  URLInput(SurfaceFrame parentframe) {      
    super(parentframe,"Image URL");
    
    setBackground(Color.lightGray);
    setFont(new Font("Dialog",Font.PLAIN,12));    
    setLayout(new BorderLayout(10,10));
    add("North",new Canvas());
    add("East", new Canvas());
    add("West", new Canvas());
    add("South",new Canvas());

    Panel panel;
    add("Center",panel = new Panel());    
    panel.setLayout(new BorderLayout());
   
    Panel content;
    panel.add("Center", content = new Panel()); 
    panel.add("South", panel = new Panel());
    
    panel.add(ok = new Button("OK"));
    panel.add(cancel = new Button("Cancel"));

    // Contents
      
    content.setLayout(new BorderLayout(0,5));
    content.add("North", new Label("URL of image:"));
    content.add("Center", url = new TextField(50));     
    pack();

    ok.requestFocus(); 
    showDialog();
  }

  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target == ok) {
      dispose();
  
      String desturl = url.getText();
      desturl = desturl.trim();
      if (desturl.length() == 0) return true;
    
      ((SurfaceFrame)getParent()).loadNetworkImage(desturl);
    }
    else
    if (e.target == cancel) {
      url.setText("");
      dispose();
    }
    else return super.action(e,arg);
    return true;
  }
}

