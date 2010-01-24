package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * FunctionPanel.java                                                                     *
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
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextField;

/**
 * The class <code>FunctionPanel</code> contains function input fields, 
 * Surface Plotter logo, status line and several checkboxes to control 
 * Surface Plotter's behaviour. 
 *
 * @author  Yanto Suryono
 */
 
public final class FunctionPanel extends Panel {
  private TextField function1, function2;  
  private StaticLabel message;
  private Checkbox delay_regen, func1select, func2select;
  
  /**
   * The constructor of <code>FunctionPanel</code>
   */

  FunctionPanel() {
    setBackground(Color.lightGray);
    setLayout(new BorderLayout());

    Panel funcline, baseline, modeline, infopanel, subpanel, selectpanel;
    
    funcline = new Panel();
    funcline.setLayout(new BorderLayout());   
    funcline.add("West", subpanel = new Panel());
    subpanel.setLayout(new GridLayout(2,1));
    subpanel.add(selectpanel = new Panel());
    selectpanel.setLayout(new BorderLayout());
    selectpanel.add("West", func1select = new Checkbox());
    func1select.setState(true);
    selectpanel.add("Center", message = new StaticLabel("z1(x,y)"));
    subpanel.add(selectpanel = new Panel());
    selectpanel.setLayout(new BorderLayout());
    selectpanel.add("West", func2select = new Checkbox());
    selectpanel.add("Center", message = new StaticLabel("z2(x,y)"));
    funcline.add("Center", subpanel = new Panel());
    subpanel.setLayout(new GridLayout(2,1));      
    subpanel.add(function1 = new TextField());
    subpanel.add(function2 = new TextField());

    add("Center", subpanel = new Panel());
    subpanel.setLayout(new BorderLayout());
    add("South", baseline = new Panel());
    baseline.setLayout(new BorderLayout());

    subpanel.add("East", new SurfaceLogo());
    subpanel.add("Center", funcline);

    baseline.add("West", delay_regen = new Checkbox("Delay Regeneration"));    
    baseline.add("Center", message = new StaticLabel());
  }
  
  /**
   * Determines whether the delay regeneration checkbox is checked.
   *
   * @return <code>true</code> if the checkbox is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isExpectDelay() {
    return delay_regen.getState();
  }

  /**
   * Determines whether the first function is selected.
   *
   * @return <code>true</code> if the first function is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isFunction1Selected() {
    return func1select.getState();
  }
  
  /**
   * Determines whether the second function is selected.
   *
   * @return <code>true</code> if the second function is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isFunction2Selected() {
    return func2select.getState();
  }

  /**
   * Sets the text of status line
   *
   * @param text new text to be displayed
   */
       
  public void setMessage(String text) {
    message.setText(text);
  }
  
  /**
   * Moves the keyboard cursor to the error position in 
   * function definition.
   *
   * @param func the function number (1 or 2)
   * @param position the error position (zero based)
   */

  public void setErrorPosition(int func, int position) {
    position--;
    switch (func) {
       case 1: function1.select(position,position); 
               function1.requestFocus();
               break;
       case 2: function2.select(position,position); 
               function2.requestFocus();
               break;
      default:
    }
  }

  /**
   * Gets the first function defintion.
   *
   * @return the function definition
   */
   
  public String getFunction1Definition() {
    return function1.getText();
  }

  /**
   * Gets the second function defintion.
   *
   * @return the function definition
   */

  public String getFunction2Definition() {
    return function2.getText();
  }

  /**
   * Sets the first function defintion.
   *
   * @param definition of first function
   */

  public void setFunction1Definition(String definition) {
    function1.setText(definition);
  }
  
  /**
   * Sets the second function defintion.
   *
   * @param definition of second function
   */

  public void setFunction2Definition(String definition) {
    function2.setText(definition);
  }

  /**
   * Handles user actions.
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target instanceof Checkbox) {          
      if (e.target != delay_regen) return false; // propagates to parent 
    }
    else
    return super.action(e,arg);      
    return true;
  }
}


