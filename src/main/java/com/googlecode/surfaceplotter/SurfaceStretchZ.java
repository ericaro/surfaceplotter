package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceStretchZ.java                                                                   *
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
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;

/**
 * The class <code>SurfaceStretchZ</code> produces dialog box to allow user
 * enters the stretch parameters before doing z values stretching process.
 *
 * @author  Yanto Suryono
 */

public final class SurfaceStretchZ extends SurfaceDialog {
  private Button ok,cancel;
  private Checkbox absolute;
  private Scrollbar scrollbar;
  private StaticLabel percentage;
  private float minimum, maximum;
  private InputField min,max;
  private SurfaceVertex[][] vertices;
  
  /**
   * The constructor of <code>SurfaceStretchZ</code>.
   *
   * @param parentframe the parent frame
   * @param vertices    the vertices array whose z values to be stretched
   */
   
  SurfaceStretchZ(Frame parentframe, SurfaceVertex[][] vertices) {      
    super(parentframe,"Stretch Z Levels");
    
    this.vertices = vertices;
    
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
    
    Panel content = null;

    if (vertices == null) {
      panel.add("Center", new SurfaceBorder(
                          new Label("No data to process !"),20,10));
    }
    else panel.add("Center", content = new Panel()); 
    panel.add("South", panel = new Panel());
    
    if (vertices != null) panel.add(ok = new Button("OK"));
    panel.add(cancel = new Button("Cancel"));

    // Contents
      
    if (vertices != null) {
      CheckboxGroup group = new CheckboxGroup();

      content.setLayout(new GridLayout(5,1));      
      content.add(new Checkbox("Proportional Stretch",group,true));

      content.add(panel = new Panel());
      
      panel.setLayout(new BorderLayout(5,0));
      panel.add("East", percentage = new StaticLabel("100 %","1000 %"));
      panel.add("Center",panel = new Panel());      
      panel.setLayout(new BorderLayout());
      panel.add("North", scrollbar = 
                         new Scrollbar(Scrollbar.HORIZONTAL,100,10,10,200));
      scrollbar.setPageIncrement(10);
      scrollbar.setLineIncrement(1);
      
      content.add(absolute = new Checkbox("Absolute Stretch",group,false));
      content.add(panel = new Panel());
      panel.setLayout(new GridLayout(1,2));
      panel.add(new StaticLabel("Minimum Z"));
      panel.add(min = new InputField(10,-1.0f/0.0f,1.0f/0.0f,1.0f,false));
      content.add(panel = new Panel());
      panel.setLayout(new GridLayout(1,2));
      panel.add(new StaticLabel("Maximum Z"));
      panel.add(max = new InputField(10,-1.0f/0.0f,1.0f/0.0f,1.0f,false));      

      minimum = Float.NaN;
      maximum = Float.NaN;

      for (int i=vertices.length; --i >= 0;) {
        if (vertices[i] != null) 
          for (int j=vertices[i].length; --j >= 0;) {
            float value = vertices[i][j].z;
            if (!Float.isNaN(value)) {
              if (Float.isNaN(minimum) || (value < minimum)) minimum = value;
              if (Float.isNaN(maximum) || (value > maximum)) maximum = value;
            }
          }
      }    
      
      min.setText(Float.toString(minimum)); 
      max.setText(Float.toString(maximum)); 
    }

    pack();
    if (vertices != null) 
      ok.requestFocus(); 
    else
      cancel.requestFocus();

    showDialog();
  }

  /**
   * Handles events.  
   *
   * @param e the event
   * @param obj additional information from event manager 
   */

  public boolean handleEvent(Event e) {
    if (e.target == scrollbar) {
      boolean result = super.handleEvent(e);
      percentage.setText(scrollbar.getValue() + " %");
    }
    else 
    return super.handleEvent(e);
    return true;
  }
  
  /**
   * Handles user actions.  
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target == ok) {
      float newmin,newmax;
      newmin = newmax = Float.NaN;
      
      if (absolute.getState()) {
        try {
          newmin = Float.valueOf(min.getText()).floatValue();
          newmax = Float.valueOf(max.getText()).floatValue();
        }
        catch (NumberFormatException exception) {} 
      }
      else {
        int percentage = scrollbar.getValue();
        float range = maximum-minimum;
        if (!Float.isInfinite(range) && !Float.isNaN(range)) {
          range = range * percentage / 200.0f;
          float center = (maximum+minimum)/2;
          newmin = center-range;
          newmax = center+range;
        }       
      }
      dispose();
      StretchObserver observer = (StretchObserver)getParent();
      observer.startStretch(vertices,minimum,maximum,newmin,newmax);
    }
    else
    if (e.target == cancel) {
      dispose();
    }
    else return super.action(e,arg);
    return true;
  }
}

