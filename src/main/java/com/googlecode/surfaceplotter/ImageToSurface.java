package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * ImageToSurface.java                                                                    *
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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.FilteredImageSource;

/**
 * The class <code>ImageToSurface</code> is created and shown to enable user
 * interactions before converting an image to surface data.
 * 
 * @author  Yanto Suryono
 */
 
public final class ImageToSurface extends SurfaceDialog {
  private Button ok,cancel,invert,p1,p10,m1,m10;
  private StaticLabel densitydisplay,stretchlabel;
  private ImageCanvas canvas;
  private int density;
  
  private final int MINDENSITY = 5;
  private final int MAXDENSITY = 200;
  
  /**
   * The constructor of <code>ImageToSurface</code>
   *
   * @param parentframe the parent frame
   * @param image the image to be converted
   */
   
  ImageToSurface(Frame parentframe, Image image) {      
    super(parentframe,"Image to Surface");
    
    density = 20; 
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

    Panel control;
    
    panel.add(control = new Panel());
    panel.add(invert = new Button("Invert"));

    control.setLayout(new BorderLayout());
    control.add("West", densitydisplay = 
                new StaticLabel(Integer.toString(density),"00000"));
    control.add("Center", control = new Panel());
    control.setLayout(new GridLayout(1,4)); 
    control.add(p1  = new Button("+1"));  
    control.add(m1  = new Button("-1"));  
    control.add(p10 = new Button("+10"));  
    control.add(m10 = new Button("-10"));  

    content.add(canvas = new ImageCanvas(image));
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

  public boolean action(Event evt, Object arg) {
    if (evt.target instanceof Button) {
      if (evt.target == p1) {
        if (density < MAXDENSITY) {
          density++;
          setDensity();
        }
      }
      else
      if (evt.target == m1) {
        if (density > MINDENSITY) {
          density--;
          setDensity();
        }
      }
      else
      if (evt.target == p10) {
        if (density < MAXDENSITY) {
          density += 10;
          if (density > MAXDENSITY) density = MAXDENSITY;
          setDensity();
        }
      }
      else
      if (evt.target == m10) {
        if (density > MINDENSITY) {
          density -= 10;
          if (density < MINDENSITY) density = MINDENSITY;
          setDensity();
        }
      }
      else 
      if (evt.target == invert) {
        Image img = canvas.currentImage();
        Image newimg = createImage(new FilteredImageSource(
                       img.getSource(), new InvertFilter()));        
        canvas.setImage(newimg);
        img.flush();
        img = null;
      }
      else {     
        dispose();
        if (evt.target == ok) { 
          LoadObserver observer = (LoadObserver)getParent();
          observer.convertLoadedImage(canvas.currentImage(),density);
        }
      }
    }
    else return super.action(evt,arg);
    return true; 
  }

  /**
   * Sets the density of conversion grids
   */
   
  private void setDensity() {
    densitydisplay.setText(Integer.toString(density));
    canvas.setDensity(density);
  }  
}

