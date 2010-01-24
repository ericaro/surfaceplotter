package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SettingPanel.java                                                                      *
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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;

/**
 * The class <code>SettingPanel</code> produces panel that contains several
 * control objects that modify Surface Plotter's settings and send commands to
 * Surface Plotter.  
 *
 * @author  Yanto Suryono
 */

public final class SettingPanel extends Panel {
  private InputField xmin,xmax,ymin,ymax,zmin,zmax;
  private InputField calcdivisions,dispdivisions,contourlines;
  private Button calc,stop,regen,rotate;
  private StaticLabel min,max;
         
  private final int MIN_DIVISIONS = 5;
  private final int MAX_DIVISIONS = 200;
  private final int DEF_DIVISIONS = 20;

  private final int MIN_CONTOURS  = 1;
  private final int MAX_CONTOURS  = 30;
  private final int DEF_CONTOURS  = 10;

  private final int SIZE1 = 3;
  private final int SIZE2 = 9;
     
  private final String[] divisionname = {"Calculate", "Display"};
  private final String[] typename = {"Minimums", "Maximums"};
  private final String[] axisname = {"x", "y", "z"};
     
  /**
   * The constructor of <code>SettingPanel</code>.
   *
   * @param init_calc_divisions initial number of divisions to calculate
   * @param init_disp_divisions initial number of divisions to display
   */

  SettingPanel(int init_calc_divisions, int init_disp_divisions) {
    StaticLabel message;
    
    setBackground(Color.lightGray);
    setLayout(new BorderLayout());
    Panel panel = new Panel();
    panel.setLayout(new GridLayout(1,2));    
    panel.add(calc = new Button("Calculate")); 
    panel.add(stop = new Button("Stop")); 
    add("North",panel);
    add("Center",panel = new Panel());
    panel.setLayout(new BorderLayout());
    panel.add("North", regen = new Button("Regenerate"));
    panel.add("Center",panel = new Panel());
    panel.setLayout(new BorderLayout());
    panel.add("North", rotate = new Button("Rotate"));
    panel.add("Center",panel = new Panel());
    panel.setLayout(new BorderLayout());
    
    Panel inputpanel = new Panel();
    panel.add("North",inputpanel);
    inputpanel.setLayout(new GridLayout(16,1));
    inputpanel.add(message = new StaticLabel("Divisions"));

    Panel subpanel;
    InputField[] inputfield = new InputField[6];
    
    for (int i=0; i < 2; i++) {
      subpanel = new Panel();
      subpanel.setLayout(new BorderLayout());
      subpanel.add("East", inputfield[i] = 
      new InputField(SIZE1,(float)MIN_DIVISIONS,(float)MAX_DIVISIONS,1.0f,true));
      subpanel.add("Center",message = new StaticLabel(divisionname[i]));
      inputpanel.add(subpanel);
    }

    calcdivisions = inputfield[0];
    calcdivisions.setText(init_calc_divisions + "");
    dispdivisions = inputfield[1];
    dispdivisions.setText(init_disp_divisions + "");

    for (int i=0; i < 2; i++) {
      inputpanel.add(message = new StaticLabel(typename[i]));
      for (int j=0; j < 3; j++) {
        subpanel = new Panel();
        subpanel.setLayout(new BorderLayout());
        subpanel.add("East", inputfield[i*3+j] = 
        new InputField(SIZE2,-1.0f/0.0f,1.0f/0.0f,1.0f,false));
        subpanel.add("Center",message = new StaticLabel(axisname[j]));
        inputpanel.add(subpanel);
      }
    }
    
    subpanel = new Panel();
    subpanel.setLayout(new BorderLayout());
    subpanel.add("East", contourlines = 
                 new InputField(SIZE1,(float)MIN_CONTOURS,
                               (float)MAX_CONTOURS,1.0f,true));
    contourlines.setText("" + DEF_CONTOURS);
    subpanel.add("Center",new StaticLabel("Contours"));
    inputpanel.add(subpanel);

    xmin = inputfield[0];
    ymin = inputfield[1];
    zmin = inputfield[2];
    xmax = inputfield[3];
    ymax = inputfield[4];
    zmax = inputfield[5];
        
    inputpanel.add(message = new StaticLabel("Minimum Z"));
    inputpanel.add(min = new StaticLabel());
    inputpanel.add(message = new StaticLabel("Maximum Z"));
    inputpanel.add(max = new StaticLabel());
  }

  /**
   * Sets variable ranges. Affects displayed values.
   *
   * @param xi the minimum x
   * @param yi the minimum y
   * @param zi the minimum z
   * @param xa the maximum x
   * @param ya the maximum y
   * @param za the maximum z
   */
   
  public void setRanges(float xi, float yi, float zi, 
                        float xa, float ya, float za) {
    xmin.setText(String.valueOf(xi));
    ymin.setText(String.valueOf(yi));
    zmin.setText(String.valueOf(zi));
    xmax.setText(String.valueOf(xa));
    ymax.setText(String.valueOf(ya));
    zmax.setText(String.valueOf(za));
  }
  
  /**
   * Gets the current minimum x value.
   *
   * @return string representation of the minimum x
   */
   
  public String getXMin() {
    return xmin.getText();
  }

  /**
   * Gets the current minimum y value.
   *
   * @return string representation of the minimum y
   */

  public String getYMin() {
    return ymin.getText();
  }
  
  /**
   * Gets the current minimum z value.
   *
   * @return string representation of the minimum z
   */

  public String getZMin() {
    return zmin.getText();
  }
  
  /**
   * Gets the current maximum x value.
   *
   * @return string representation of the maximum x
   */

  public String getXMax() {
    return xmax.getText();
  }

  /**
   * Gets the current maximum y value.
   *
   * @return string representation of the maximum y
   */

  public String getYMax() {
    return ymax.getText();
  }
  
  /**
   * Gets the current maximum z value.
   *
   * @return string representation of the maximum z
   */

  public String getZMax() {
    return zmax.getText();
  }
  
  /**
   * Called when automatic rotation starts.
   */
      
  public void rotationStarts() {
    calc.disable();
    stop.disable();
    regen.disable();
    zmin.disable();
    zmax.disable();    
    rotate.setLabel("Freeze");
  }

  /**
   * Called when automatic rotation stops
   */

  public void rotationStops() {
    calc.enable();
    stop.enable();
    regen.enable();
    zmin.enable();
    zmax.enable();    
    rotate.setLabel("Rotate");
  }

  /**
   * Enables or disables automatic rotation.
   *
   * @param enable if <code>true</code>, enables automatic rotation,
            otherwise disables it.
   */
   
  public void enableRotation(boolean enable) {
    if (enable) rotate.enable(); else rotate.disable();
  }
  
  /**
   * Sets the minimum value of calculated values. Affects displayed value.
   *
   * @param value the minimum value
   */
   
  public void setMinimumResult(String value) {
    min.setText(value);
  }
  
  /**
   * Sets the maximum value of calculated values. Affects displayed value.
   *
   * @param value the maximum value
   */

  public void setMaximumResult(String value) {
    max.setText(value);
  }
  
  /**
   * Gets the number of contour lines to be created
   *
   * @return the number of contour lines
   */
   
  public int getContourLines() {
    int value;

    try {
      value = Integer.parseInt(contourlines.getText());
    }
    catch(NumberFormatException e) {
      value = -1;
    }
    if ((value < MIN_CONTOURS) || (value > MAX_CONTOURS)) {
      value = DEF_CONTOURS; 
      contourlines.setText("" + DEF_CONTOURS);
    }
    return value;
  }

  /**
   * Gets the number of divisions to be calculated.
   * Automatically fixes invalid values.
   *
   * @return valid number of divisions to be calculated
   */ 
   
  public int getCalcDivisions() {
    int value;

    try {
      value = Integer.parseInt(calcdivisions.getText());
    }
    catch(NumberFormatException e) {
      value = -1;
    }
    if ((value < MIN_DIVISIONS) || (value > MAX_DIVISIONS)) {
      value = DEF_DIVISIONS; 
      calcdivisions.setText("" + DEF_DIVISIONS);
    }
    return value; 
  }

  /**
   * Gets the number of divisions to be displayed.
   * Automatically fixes invalid values.
   *
   * @return valid number of divisions to be displayed
   */ 
  
  public int getDispDivisions() {
    int value;

    try {
      value = Integer.parseInt(dispdivisions.getText());
    }
    catch(NumberFormatException e) {
      value = -1;
    }
    if ((value < MIN_DIVISIONS) || (value > MAX_DIVISIONS)) {
      value = DEF_DIVISIONS; 
      dispdivisions.setText("" + DEF_DIVISIONS);
    }
    return value; 
  }

  /**
   * Sets the number of divisions to be calculated.
   *
   * @param divisions number of divisions to be calculated
   */ 

  public void setCalcDivisions(int divisions) {
    calcdivisions.setText(Integer.toString(divisions));
  }

  /**
   * Sets the number of divisions to be displayed.
   *
   * @param divisions number of divisions to be displayed
   */ 

  public void setDispDivisions(int divisions) {
    dispdivisions.setText(Integer.toString(divisions));
  }  
}

