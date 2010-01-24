package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * InputField.java                                                                        *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
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
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextField;

/**
 * The class <code>InputField</code> produces number input field with plus and minus
 * buttons to increase and decrease the value in the field. 
 *
 * @author  Yanto Suryono
 */
 
public final class InputField extends Panel {
  private float min_val, max_val, delta;
  private boolean int_val;
  private TextField textfield;
  
  /**
   * The constructor of <code>InputField</code>.
   *
   * @param size    the size of this input field
   * @param min     the minimum valid value
   * @param max     the maximum valid value
   * @param d       the delta value when modified using plus-minus buttons 
   * @param integer allows only integer value input if <code>true</code>
   */
   
  InputField(int size, float min, float max, float d, boolean integer) {
    super();  
    min_val = min; 
    max_val = max; 
    delta = d; 
    int_val = integer;
    
    setLayout(new BorderLayout());
    add("Center", textfield = new TextField(size));
    add("East", new PlusMinus(this));
  }

  /**
   * Gets the value 
   *
   * @return string representation of the value
   */ 
  
  public String getText() {
    return textfield.getText();
  }
  
  /**
   * Sets the value
   * 
   * @param text string representation of the value
   */
   
  public void setText(String text) {
    textfield.setText(text);
  }
  
  /**
   * Increases the value
   */
   
  public void increase() {
    float value;

    try {
      value = Float.valueOf(getText()).floatValue();
    }
    catch(NumberFormatException e) {
      value = 0;
    }    
    if (int_val) value = (int)value;
    value += delta;
    if (value > max_val) value = max_val;
    if (int_val) setText((int)value + ""); else setText(value + "");
  }

  /**
   * Decreases the value
   */
   
  public void decrease() {
    float value;

    try {
      value = Float.valueOf(getText()).floatValue();
    }
    catch(NumberFormatException e) {
      value = 0;
    }    
    if (int_val) value = (int)value;
    value -= delta;
    if (value < min_val) value = min_val;
    if (int_val) setText((int)value + ""); else setText(value + "");
  }
}

/**
 * The class <code>PlusMinus</code> produces plus and minus
 * buttons to increase and decrease the value of class <code>InputField</code> 
 *
 * @author  Yanto Suryono
 * @version 1.30b1, 3 May 1997
 * @since   1.10  
 */

final class PlusMinus extends Panel {
  private InputField Parent;
  private static int WIDTH = 15;

  /**
   * The constructor of <code>PlusMinus</code>
   *
   * @param parent the parent whose value to be changed
   */
   
  PlusMinus(InputField parent) {
    super(); 
    setFont(new Font("Helvetica", Font.PLAIN, 9));
    setLayout(new GridLayout(2,1));
    add(new Button("+"));
    add(new Button("-"));
    Parent = parent; 
  }
  
  /**
   * Determines the size of this component
   *
   * @return the size of this component
   */
   
  public Dimension preferredSize() {
    return new Dimension(WIDTH,0);
  }

  /**
   * Handles user actions.
   *
   * @param e the event
   * @param arg additional information from event manager 
   */

  public boolean action(Event e, Object arg) {
    if (e.target instanceof Button) {
      if (((Button)e.target).getLabel().equals("+")) {
        Parent.increase(); return true; 
      }
      else
      if (((Button)e.target).getLabel().equals("-")) {
        Parent.decrease(); return true;
      }
    }
    return false;
  }
}

