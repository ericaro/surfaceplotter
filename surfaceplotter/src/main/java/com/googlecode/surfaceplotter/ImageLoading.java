package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * ImageLoading.java                                                                      *
 *                                                                                        *
 * Surface Plotter   version 1.30b1  17 May 1997                                          *
 *                        bug fixed  23 May 1997                                          *
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
import java.awt.Frame;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.image.FilteredImageSource;

/**
 * The class <code>ImageLoading</code> is created and shown when an image 
 * is being loaded from local disk or from network. 
 *
 * @author  Yanto Suryono
 */

public final class ImageLoading extends SurfaceDialog implements Runnable {
  private MediaTracker tracker;
  private Thread thread = null;
  private Label message;
  
  private Image image;

  /**
   * The constructor of <code>ImageLoading</code>
   *
   * @param parentframe the parent of this dialog box
   * @param image the image that is being loaded
   */
   
  ImageLoading(Frame parentframe, Image image) {      
    super(parentframe,"Loading Image");
    
    this.image = image;
    
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
    panel.add(button = new Button("Cancel"));
    
    content.setLayout(new BorderLayout(30,10));
    content.add("North",new Canvas());
    content.add("East", new Canvas());
    content.add("West", new Canvas());
    content.add("South",new Canvas());
    content.add("Center", content = new Panel());
    
    content.add(message = new Label("Loading image ... Please wait"));
    pack();

    tracker = new MediaTracker(this);
    tracker.addImage(image,1000);
    thread = new Thread(this);
    
    button.requestFocus(); 
    thread.start();
    showDialog();
  }
  
  /**
   * Monitors the image loading process. 
   * Calls the <code>loadComplete</code> method the observer
   * when the loading process is completed.
   *
   * @see LoadObserver
   */
   
  public void run() {
    while (thread != null) {
      try {
        tracker.waitForAll();
      }
      catch (InterruptedException e) {
        message.setText("Image loading interrupted");
        thread = null;
        return;
      }
      thread = null;
      
      if ((tracker.statusAll(false) & MediaTracker.ERRORED) != 0) {
        message.setText("Error loading image");
        return;
      }
    }
    
    // Converts the image to gray scale image
    
    Image newimage = createImage(new FilteredImageSource(
                     image.getSource(), new GrayScaleFilter()));
    while ((newimage == null) ||
           (newimage.getWidth(this) == -1) ||
           (newimage.getHeight(this) == -1)) {
      try {
        Thread.currentThread().sleep(50);
      }
      catch (InterruptedException e) {}
    }
    dispose();
    LoadObserver observer = (LoadObserver)getParent();        
    observer.loadComplete(newimage);
  }
  
  /**
   * Handles events. Processes <code>java.awt.Event.WINDOW_DESTROY</code>
   * only and passes other events to the parent frame
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
   * Handles user actions. Aborts image loading if user depressed
   * the <i>Cancel</i> button. 
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
    }
    else return super.action(e,arg);
    return true; 
  }
}

