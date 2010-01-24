package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * ZStretcher.java                                                                        *
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
 * The class <code>ZStretcher</code> produces a dialog box to indicate that
 * the z values stretching process is in progress and does the stretching
 * as a background process.
 *
 * @author  Yanto Suryono
 */
 
public final class ZStretcher extends SurfaceDialog implements Runnable {
  private Thread thread;
  private SurfaceVertex[][] vertices;
  private float oldmin,oldmax,newmin,newmax;
  private Label message1,message2;
  
  /**
   * The constructor of <code>ZStretcher</code>
   *
   * @param parentframe the parent frame
   * @param vertices the vertices array whose z values are to be stretched
   * @param oldmin the current minimum z value
   * @param oldmax the current maximum z value
   * @param newmin the goal minimum z value
   * @param newmax the goal maximum z value
   */
   
  ZStretcher(Frame parentframe, SurfaceVertex[][] vertices,
             float oldmin, float oldmax, float newmin, float newmax) {      
    super(parentframe,"Stretching Z Levels");
     
    this.vertices = vertices;
    
    this.oldmin = oldmin;
    this.oldmax = oldmax;
    this.newmin = newmin;
    this.newmax = newmax;
    
    message1 = message2 = null;   
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
                                                               
    Button button;
    panel.add(button = new Button("Abort"));
    
    content.setLayout(new BorderLayout(30,10));
    content.add("North",new Canvas());
    content.add("East", new Canvas());
    content.add("West", new Canvas());
    content.add("South",new Canvas());
    content.add("Center", content = new Panel());
        
    boolean startthread = false;    
    if (Float.isNaN(oldmin) || Float.isNaN(oldmax) ||
        Float.isNaN(newmin) || Float.isNaN(newmax) ||
        Float.isInfinite(oldmin) || Float.isInfinite(oldmax) ||
        Float.isInfinite(newmin) || Float.isInfinite(newmax)) {        

      content.add(new Label("Unable to stretch. Infinites or NaNs in range"));            
    }
    else
    if (newmin >= newmax) {
      content.add(new Label("Unable to stretch. Negative or zero interval"));                 
    }
    else {
      startthread = true;
      content.setLayout(new GridLayout(2,1));
      content.add(message1 = 
      new Label("Stretching Z levels ... Please wait"));
      content.add(message2 = 
      new Label("Warning: Hitting abort will not restore your data"));
    }
    
    pack();

    button.requestFocus(); 
    if (startthread) {
      thread = new Thread(this);
      thread.start();
    }
    showDialog();
  }
  
  /**
   * Stretches the z values.
   */
   
  public void run() {
    float oldrange = oldmax-oldmin;
    float newrange = newmax-newmin;
    float oldcenter = (oldmax+oldmin)/2;
    float newcenter = (newmax+newmin)/2;
        
    for (int i=vertices.length; --i >= 0;) {
      if (vertices[i] != null)
        for (int j=vertices[i].length; --j >= 0;) {
          Thread.yield();
          if (thread == null) return;
          
          float z = vertices[i][j].z;
          if (!Float.isNaN(z)) {
            vertices[i][j].z = ((z-oldcenter)/oldrange*newrange+newcenter);
          }
        }
    }
    
    try {
      Thread.sleep(500);
    }
    catch (InterruptedException e) {}
        
    dispose();

    StretchObserver observer = (StretchObserver)getParent();
    observer.stretchComplete();
  }
  
  /**
   * Handles events. Processes <code>java.awt.Event.WINDOW_DESTROY</code>
   * only and passes other events to the parent frame.
   *
   * @param e the event
   */

  public boolean handleEvent(Event e) {
    if (e.id == Event.WINDOW_DESTROY) {
      if ((thread != null) && thread.isAlive()) {
        thread.stop();
        thread = null;
      }
      dispose();
    }
    else return super.handleEvent(e);
    return true;
  }
  
  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target instanceof Button) {
      if ((thread != null) && thread.isAlive()) {
        thread.stop();
        thread = null;
      }
      dispose();
      if (arg.equals("Close")) {
        StretchObserver observer = (StretchObserver)getParent();
        observer.stretchComplete();
      }
    }
    else return super.action(e,arg);
    return true; 
  }
}

