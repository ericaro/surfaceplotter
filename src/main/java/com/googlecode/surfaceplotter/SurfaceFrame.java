package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceFrame.java                                                                      *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                   bug fixed       21 May 1997                                          *
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
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * The class <code>SurfaceFrame</code> produces a frame for Surface Plotter applet.
 * It also provides Surface Plotter a pulldown menu and acts as a middle man between
 * <code>SurfaceCanvas</code> class and other Surface Plotter components
 *
 * @author  Yanto Suryono
 */
 
public final class SurfaceFrame extends Frame 
             implements Runnable, LoadObserver, StretchObserver {

  private FunctionPanel function_panel;          // the function panel
  private SettingPanel setting_panel;            // the setting panel
  private SurfaceCanvas canvas;                  // the drawing canvas
  private SurfacePlotter applet;                 // the applet, if present
  private Parser parser1,parser2;                // the function parser
  private boolean func1calc, func2calc;          // calculated flag 
  private String func1name, func2name;           // function names 
  private String filename;                       // current filename 
  private Thread thread;                         // current thread
  private int calc_divisions;                    // number of divisions to calculate
  private MenuBar menubar;
  private Image sourceimage = null;
  private boolean convertimage;
    
  private static final int INIT_CALC_DIV   = 20;
  private static final int INIT_DISP_DIV   = 20;

  private static final int MODES_COUNT     = 5;
  
  public static boolean newJVM = false;
  
  /**
   * The constructor of <code>SurfaceFrame</code>
   *
   * @param applet the Surface Plotter applet
   * @see   SurfacePlotter
   */
   
  SurfaceFrame(SurfacePlotter applet) {
    super();
    
    newJVM = isNewJVM();    
    thread = new Thread(this);
    parser1 = new Parser(2);
    parser1.defineVariable(1,"x");
    parser1.defineVariable(2,"y");
    parser2 = new Parser(2);
    parser2.defineVariable(1,"x");
    parser2.defineVariable(2,"y");
    this.applet = applet;
    setFont(new Font("Helvetica", Font.PLAIN, 11));
    setBackground(Color.lightGray); 
    setLayout(new BorderLayout());
    add("North", function_panel = new FunctionPanel());
    add("East", setting_panel = new SettingPanel(INIT_CALC_DIV,INIT_DISP_DIV));
    add("Center", new SurfaceBorder(canvas = new SurfaceCanvas(this)));        
    setting_panel.setRanges(-3,-3,-1,3,3,1);

    func1name = func2name = null;    
    setMenuBar(menubar = createMenuBar());
    setFilename(null);
    pack(); show();
    canvas.repaint();
    requestFocus();
  }
  
  /**
   * Sets the text of status line
   *
   * @param text new text to be displayed
   */

  public void setMessage(String text) {
    function_panel.setMessage(text);
  }

  /**
   * Moves the keyboard cursor to the error position in 
   * function definition.
   *
   * @param func the function number (1 or 2)
   * @param position the error position (zero based)
   */

  public void setErrorPosition(int func, int position) {
    function_panel.setErrorPosition(func,position);
  }
  
  /**
   * Called when automatic rotation starts.
   */

  public void rotationStarts() {
    setting_panel.rotationStarts();
  }

  /**
   * Called when automatic rotation stops
   */

  public void rotationStops() {
    setting_panel.rotationStops();
  }

  /**
   * Parses defined functions and calculates surface vertices 
   */
    
  public void run() {
    float   stepx, stepy, x, y, v;
    float   xi,xx,yi,yx;
    float   min, max;
    boolean f1, f2;
    int     i,j,k,total;
 
    // image conversion
    
    int[]   pixels = null; 
    int     imgwidth = 0;
    int     imgheight = 0;
    
    if (convertimage) {   
      imgwidth = sourceimage.getWidth(this);
      imgheight = sourceimage.getHeight(this); 

      float scalex, scaley;
      
      if (imgwidth < imgheight) {
        scalex = 1.0f;
        scaley = (float)((double)imgheight/imgwidth);
      }
      else {
        scaley = 1.0f;
        scalex = (float)((double)imgwidth/imgheight);
      }

      if (scalex > 5.0f) scalex = 1.0f;     
      if (scaley > 5.0f) scaley = 1.0f; 
          
      setting_panel.setRanges(-scalex,-scaley,0,scalex,scaley,1.0f);
 
      try {
        pixels = new int[imgwidth*imgheight];      
      }
      catch(OutOfMemoryError e) {
        setMessage("Not enough memory");
        return;
      }
      
      setMessage("Grabbing pixels ...");
      PixelGrabber grabber = new PixelGrabber(sourceimage,0,0,
                             imgwidth,imgheight,pixels,0,imgwidth);
      
      try {
        grabber.grabPixels();
      } 
      catch (InterruptedException e) {
        setMessage("Error: Interrupted waiting for pixels !");
        return;
      }
      
      if ((grabber.status() & ImageObserver.ABORT) != 0) {
        setMessage("Error: Image fetch aborted or errored");
        return;
      }
    }

    try {
      xi = Float.valueOf(setting_panel.getXMin()).floatValue();
      yi = Float.valueOf(setting_panel.getYMin()).floatValue();
      xx = Float.valueOf(setting_panel.getXMax()).floatValue();
      yx = Float.valueOf(setting_panel.getYMax()).floatValue();
      if ((xi >= xx) || (yi >= yx)) throw new NumberFormatException();
    }
    catch(NumberFormatException e) {
      setMessage("Error in ranges"); 
      return;
    }

    canvas.setRanges(xi,xx,yi,yx); 

    if (!convertimage) {
      setMessage("parsing ...");              
    
      f1 = function_panel.isFunction1Selected();
      if (f1) {
        parser1.define(function_panel.getFunction1Definition());
        parser1.parse();
   
        if (parser1.getErrorCode() != parser1.NO_ERROR) {
          setMessage("Parse error: " + parser1.getErrorString() + 
                     " at function 1, position " + parser1.getErrorPosition());
          setErrorPosition(1,parser1.getErrorPosition());
          return;
        }
      }
    
      f2 = function_panel.isFunction2Selected();
      if (f2) {      
        parser2.define(function_panel.getFunction2Definition());
        parser2.parse();
   
        if (parser2.getErrorCode() != parser2.NO_ERROR) {
          setMessage("Parse error: " + parser2.getErrorString() + 
                     " at function 2, position " + parser2.getErrorPosition());
          setErrorPosition(2,parser2.getErrorPosition());
          return;
        }  
      }
    
      if (!f1 && !f2) {
        setMessage("No function selected");
        return;
      }
  
    }
    else {
      f1 = true; f2 = false;
    }
      
    thread.yield();
    calc_divisions = setting_panel.getCalcDivisions();
    setDataAvailability(false); 
    func1calc = f1; func2calc = f2;
    if (func1calc) {
      if (convertimage)
        func1name = new String("Converted from image");
      else
        func1name = new String(function_panel.getFunction1Definition()); 
    }
    if (func2calc)
      func2name = new String(function_panel.getFunction2Definition());
    
    stepx = (xx - xi) / calc_divisions; 
    stepy = (yx - yi) / calc_divisions;
    
    total = (calc_divisions+1)*(calc_divisions+1);

    SurfaceVertex[][] vertex = allocateMemory(f1,f2,total);
    if (vertex == null) return;
    
    max = Float.NaN;
    min = Float.NaN;
    setting_panel.setMinimumResult("");
    setting_panel.setMaximumResult("");

    canvas.destroyImage();
        
    i = 0; j = 0; k = 0; x = xi; y = yi; 
    
    float xfactor = 20/(xx-xi);
    float yfactor = 20/(yx-yi);

    if (convertimage) {
      
      xfactor *= stepx; x = 0;
      yfactor *= stepy; y = 0;
      
      imgwidth--;
      imgheight--;
      
      int xindex, yindex;

      while (i <= calc_divisions) {
        xindex = i * (imgwidth) / calc_divisions;    
         
        while (j <= calc_divisions) {
          thread.yield();

          yindex = (calc_divisions - j) * imgheight / 
                    calc_divisions * (imgwidth + 1); 
          v = (pixels[yindex+xindex] & 0xff)/255.0f;  

          if (Float.isNaN(max) || (v > max)) max = v; else
          if (Float.isNaN(min) || (v < min)) min = v;
          vertex[0][k] = new SurfaceVertex(x-10,y-10,v);
          j++; k++; y += yfactor;          
          setMessage("Converting : " + k*100/total + "% completed");
        }        
        j = 0; y = 0; i++; x += xfactor;
      }
    }
    else {
      while (i <= calc_divisions) {
        if (f1) {
          parser1.setVariable(1,x);
          parser1.setVariable(2,y);
        }
        if (f2) {
          parser2.setVariable(1,x);
          parser2.setVariable(2,y);
        }
        while (j <= calc_divisions) {
          thread.yield();
          if (f1) {
            v = (float)parser1.evaluate();
            if (Float.isInfinite(v)) v = Float.NaN;
            if (!Float.isNaN(v)) {
              if (Float.isNaN(max) || (v > max)) max = v; else
              if (Float.isNaN(min) || (v < min)) min = v;
            }
            vertex[0][k] = new SurfaceVertex((x-xi)*xfactor-10,
                                             (y-yi)*yfactor-10,v);
          }
          if (f2) {
            v = (float)parser2.evaluate();
            if (Float.isInfinite(v)) v = Float.NaN;
            if (!Float.isNaN(v)) {
              if (Float.isNaN(max) || (v > max)) max = v; else
              if (Float.isNaN(min) || (v < min)) min = v;
            }
            vertex[1][k] = new SurfaceVertex((x-xi)*xfactor-10,
                                             (y-yi)*yfactor-10,v);
          }
          j++; y += stepy;
          if (f1) parser1.setVariable(2,y); 
          if (f2) parser2.setVariable(2,y); 
          k++;
          setMessage("Calculating : " + k*100/total + "% completed");
        }
        j = 0; y = yi; i++; x += stepx;
      }
    }

    setting_panel.setMinimumResult(Float.toString(min));
    setting_panel.setMaximumResult(Float.toString(max));
    
    if (convertimage) {
      sourceimage.flush();
      sourceimage = null;  
    }
    
    canvas.setValuesArray(vertex);
    setDataAvailability(true);
    canvas.repaint(); 
  }

  /**
   * Handles events. 
   *
   * @param e the event
   */

  public boolean handleEvent(Event e) {
    if ((e.id == Event.WINDOW_DESTROY) && (e.target == this)) {
      if (applet == null) {
        dispose();
        System.exit(0);
      }
      else 
        applet.disposeWindow();
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
    if (e.target instanceof MenuItem) {
      processMenuEvent((MenuItem)e.target);
    }
    else
    if (e.target instanceof Button) {
      if (arg.equals("Calculate")) {
        synchronized (thread) {
          if (!thread.isAlive()) {           
            thread = new Thread(this);
            convertimage = false;
            thread.start();          
          }
        } 
      }
      else
      if (arg.equals("Stop")) {
        synchronized (thread) {
          if (thread.isAlive()) {
            thread.stop();
            try {
              thread.join();
            }
            catch (InterruptedException exception) {};
            setMessage("Interrupted");
            setDataAvailability(false);
            canvas.repaint();
          }
          canvas.interrupt();
          if (sourceimage != null) {
            sourceimage.flush();
            sourceimage = null;
          }
        }
      }
      else 
      if (arg.equals("Regenerate")) {
        canvas.destroyImage();
        canvas.repaint();
      }
      else
      if (arg.equals("Rotate")) {
        canvas.startRotation();
      }
      else
      if (arg.equals("Freeze")) {
        canvas.stopRotation();      
      }
    }
    else
    return super.action(e,arg);      
    return true;
  }

  /**
   * Determines whether the delay regeneration checkbox is checked.
   *
   * @return <code>true</code> if the checkbox is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isExpectDelay() {
    return function_panel.isExpectDelay();
  }

  /**
   * Determines whether to show bounding box.
   *
   * @return <code>true</code> if to show bounding box
   */
   
  public boolean isBoxed() {
    return ((CheckboxMenuItem)getMenuItem(OPT_BOXED)).getState();
  }

  /**
   * Determines whether to show x-y mesh.
   *
   * @return <code>true</code> if to show x-y mesh
   */

  public boolean isMesh() {
    return ((CheckboxMenuItem)getMenuItem(OPT_MESH)).getState();
  }

  /**
   * Determines whether to scale axes and bounding box.
   *
   * @return <code>true</code> if to scale bounding box
   */

  public boolean isScaleBox() {
    return ((CheckboxMenuItem)getMenuItem(OPT_SCALE)).getState();
  }

  /**
   * Determines whether to show x-y ticks.
   *
   * @return <code>true</code> if to show x-y ticks
   */

  public boolean isDisplayXY() {
    return ((CheckboxMenuItem)getMenuItem(OPT_XYTICKS)).getState();
  }

  /**
   * Determines whether to show z ticks.
   *
   * @return <code>true</code> if to show z ticks
   */

  public boolean isDisplayZ() {
    return ((CheckboxMenuItem)getMenuItem(OPT_ZTICKS)).getState();
  }
  
  /**
   * Determines whether to show face grids.
   *
   * @return <code>true</code> if to show face grids
   */

  public boolean isDisplayGrids() {
    return ((CheckboxMenuItem)getMenuItem(OPT_FACEGRIDS)).getState();
  }

  /**
   * Determines whether the first function is selected.
   *
   * @return <code>true</code> if the first function is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isPlotFunction1() {
    return (func1calc && function_panel.isFunction1Selected());
  }
  
  /**
   * Determines whether the first function is selected.
   *
   * @return <code>true</code> if the first function is checked, 
   *         <code>false</code> otherwise
   */

  public boolean isPlotFunction2() {
    return (func2calc && function_panel.isFunction2Selected());
  }
  
  /**
   * Gets the selected shading/plot mode
   *
   * @return the selected shading/plot mode
   */
   
  public int getPlotMode() {    
    for (int i=0; i < MODES_COUNT; i++) {   
      if (((CheckboxMenuItem)getMenuItem(OPT_SHADES+i)).getState())
      return i;
    }
    return 0;
  }
  
  /**
   * Gets the calculated number of divisions.
   *
   * @return calculated number of divisions
   */ 
  
  public int getCalcDivisions() {
    return calc_divisions;
  }

  /**
   * Gets minimum z range
   *
   * @return string representation of minimum z range
   */
   
  public String getZMin() {
    return setting_panel.getZMin();
  }

  /**
   * Gets maximum z range
   *
   * @return string representation of maximum z range
   */

  public String getZMax() {
    return setting_panel.getZMax();
  }
  
  /**
   * Gets the number of contour lines to be created
   *
   * @return the number of contour lines
   */

  public int getContourLines() {
    return setting_panel.getContourLines();
  }
  
  /**
   * Gets the number of divisions to be displayed.
   * Automatically fixes invalid values.
   *
   * @return valid number of divisions to be displayed
   */ 

  public int getDispDivisions() {
    int plot_density;

    plot_density = setting_panel.getDispDivisions();
    if (plot_density > calc_divisions) plot_density = calc_divisions;
    while ((calc_divisions % plot_density) != 0) plot_density++;
    return plot_density;
  }

  /**
   * Sets the number of divisions to be displayed.
   *
   * @param divisions number of divisions to be displayed
   */ 

  public void setDispDivisions(int divisions) {
    setting_panel.setDispDivisions(divisions);
  }
  
  /**
   * Implementation of <code>LoadObserver</code>. Called when
   * the image loading process is completed.
   *
   * @param image the loaded image
   */
      
  public void loadComplete(Image image) {
    new ImageToSurface(this,image);
  }
  
  /**
   * Implementation of <code>LoadObserver</code>. Called when
   * the image to surface conversion process should be started.
   *
   * @param image      the image to be converted
   * @param divisions  the number of divisions should be made to the image  
   */

  public void convertLoadedImage(Image image, int divisions) {
    sourceimage = image;
    if (sourceimage != null) {
      synchronized (thread) {
        if (thread.isAlive()) {
          thread.stop();
          try {
            thread.join();
          }
          catch (InterruptedException exception) {};
          setDataAvailability(false);
          canvas.repaint();
        }
      
        setMessage("Converting image ...");
        setting_panel.setCalcDivisions(divisions);
        setting_panel.setDispDivisions(divisions);
        thread = new Thread(this);
        convertimage = true;
        thread.start();        
      } 
    } 
  }
    
  /**
   * Implementation of <code>StretchObserver</code>. Called when
   * the z value stretching process is completed.
   */

  public void stretchComplete() {
    canvas.destroyImage();
    canvas.repaint();
  }
  
  /**
   * Implementation of <code>StretchObserver</code>. Called when
   * the z value stretching process should be started.
   *
   * @param vertices the vertices array whose z values are to be stretched
   * @param oldmin the current minimum z value
   * @param oldmax the current maximum z value
   * @param newmin the goal minimum z value
   * @param newmax the goal maximum z value
   */

  public void startStretch(SurfaceVertex[][] vertices, 
                           float oldmin, float oldmax, float newmin, float newmax) {
    new ZStretcher(this,vertices,oldmin,oldmax,newmin,newmax);   
    setting_panel.setMinimumResult(Float.toString(newmin));
    setting_panel.setMaximumResult(Float.toString(newmax));
  } 

  /**
   * Does the z value stretching process
   */
   
  private void stretchZ() {
    new SurfaceStretchZ(this,canvas.getValuesArray());
  }
  
  /**
   * Creates menu bar
   *
   * @return the menu bar
   */
   
  private MenuBar createMenuBar() {
    MenuBar mb = new MenuBar();
    Menu menu;
     
    mb.add(menu = new Menu("File"));
    menu.add("Open ...");
    menu.addSeparator(); 
    menu.add("Save");
    menu.add("Save As ...");
    menu.addSeparator(); 
    menu.add("Surface from Local Image File ...");
    menu.add("Surface from Network Image File ...");
    menu.addSeparator(); 
    menu.add("Print ...");    
    menu.addSeparator(); 

    if (applet == null) menu.add("Exit"); else
                        menu.add("Close"); 
           
    // No printing prior to JDK 1.1
    
    if (!newJVM) menu.getItem(8).disable();

    // No file I/O with applets
    
    if (applet != null) {
      menu.getItem(0).disable();
      menu.getItem(5).disable();
    }

    // Can not save without data
    
    menu.getItem(2).disable();
    menu.getItem(3).disable();
    
    mb.add(menu = new Menu("Edit"));
    menu.add("Stretch Z Levels ...");    

    mb.add(menu = new Menu("Options"));
    menu.add(new CheckboxMenuItem("Show Bounding Box"));    
    menu.add(new CheckboxMenuItem("Show X-Y Mesh"));    
    menu.add(new CheckboxMenuItem("Show Face Grids"));    
    menu.add(new CheckboxMenuItem("Scale Bounding Box"));    
    menu.addSeparator();
    menu.add(new CheckboxMenuItem("Show X-Y ticks"));
    menu.add(new CheckboxMenuItem("Show Z ticks"));
    menu.addSeparator();
    menu.add(new CheckboxMenuItem("Wireframe Mode"));
    menu.add(new CheckboxMenuItem("Hidden Surface Elimination"));
    menu.add(new CheckboxMenuItem("Color Spectrum Mode"));
    menu.add(new CheckboxMenuItem("Gray Scale Mode"));
    menu.add(new CheckboxMenuItem("Dual Shades Mode"));
    menu.addSeparator();
    menu.add(new CheckboxMenuItem("Surface Plot"));
    menu.add(new CheckboxMenuItem("Contour Plot"));
    menu.add(new CheckboxMenuItem("Density Plot"));
            
    // Initial states
    
    ((CheckboxMenuItem)menu.getItem(0)).setState(true);
    ((CheckboxMenuItem)menu.getItem(1)).setState(true);
    ((CheckboxMenuItem)menu.getItem(3)).setState(true);
    ((CheckboxMenuItem)menu.getItem(8)).setState(true);
    ((CheckboxMenuItem)menu.getItem(14)).setState(true);

    Menu helpmenu = new Menu("Help");
    helpmenu.add("Menu Commands");
    helpmenu.add("Function Syntax Reference");
    helpmenu.add("Parts of Surface Plotter");
    helpmenu.add("Mouse Operations");
    helpmenu.addSeparator();
    helpmenu.add("Surface Plotter Documentation");
    helpmenu.addSeparator();
    helpmenu.add("System Information");
    helpmenu.addSeparator();
    helpmenu.add("About");
    
    mb.add(helpmenu);
    mb.setHelpMenu(helpmenu);       

    return mb;
  }

  /**
   * Gets the identifier of menu item.
   *
   * @return the identifier
   * @param item the menu item
   */
   
  private int whichMenu(MenuItem item) {
    for (int i=menubar.countMenus(); --i >= 0;) {
      Menu menu = menubar.getMenu(i);
      for (int j=menu.countItems(); --j >= 0;) {
        if (menu.getItem(j) == item) return (i+1)*100+j+1;
      }
    }
    return -1; 
  }

  /**
   * Gets the menu item given the identifier 
   *
   * @return the menu item
   * @param identifier the identifier
   */
   
  private MenuItem getMenuItem(int identifier) {
    int x,y;
    
    x = identifier / 100 - 1;
    y = identifier % 100 - 1; 
    
    Menu menu = menubar.getMenu(x);
    return menu.getItem(y);
  }

  /**
   * Waits image to be loaded.
   *
   * @param image the image being loaded
   */
   
  private void waitImageLoading(Image image) {
    if (image == null) return;
    new ImageLoading(this,image);
  }
  
  /**
   * Loads local image
   */
   
  private void loadLocalImage() {
    SurfaceFileDialog fd = new SurfaceFileDialog(this,"Open",false); 
    String filename = fd.getFilename();    
    if (filename == null) return;
    Image image = getToolkit().getImage(filename);
    waitImageLoading(image);
  }
  
  /**
   * Loads network image.
   *
   * @param desturl the URL of image to load
   */
   
  public void loadNetworkImage(String desturl) {    
    URL url = null;
    try {
      String script = null;
    
      if (applet != null) script = applet.getParameter("proxy");
      if (script != null) 
        url = new URL(applet.getCodeBase(),script + "?" + desturl);
      else
        url = new URL(desturl);
    }
    catch (Exception e) {
      setMessage("Error: " + e);
      return;
    }
    
    // Does the actual image loading
    
    Image image = getToolkit().getImage(url);
    waitImageLoading(image);    
  }
  
  /**
   * Displays help file.
   */
   
  private SurfaceHelp helpwindow = null;
  
  private void displayHelp(String helpfile) {
                                                                                                
    helpfile = "../" + helpfile;

    if (applet != null) {
      URL url = applet.getCodeBase();
     
      if (url != null) {
        try {
          url = new URL(url,helpfile);
        }
        catch (MalformedURLException e) {
          return;
        }
        applet.getAppletContext().showDocument(url,"help_window");
        return;
      }
    }
    
    if (helpwindow == null) {
      helpwindow = new SurfaceHelp(helpfile,applet);
      helpwindow.show();
    }
    else 
      helpwindow.show(helpfile);
  }
  
  /**
   * Loads a Surface Plotter file
   */

  private void fileLoad() {
    SurfaceFileDialog fd = new SurfaceFileDialog(this,"Open",false); 
    String filename = fd.getFilename();    
    if (filename == null) return;
    
    File file = new File(filename);
    FileInputStream is = null;
    DataInputStream stream = null;
    
    try {
      is = new FileInputStream(file);
      stream = new DataInputStream(is);
    
      boolean valid = true;
      // reads magic number
        if (stream.readByte() != 15) valid = false;
      if (valid)
        if (stream.readByte() != 1)  valid = false;
      if (valid)
        if (stream.readByte() != 75) valid = false;

      // reads signature
      if (valid)
        if (!stream.readUTF().equals(
            SurfacePlotter.APP_NAME)) valid = false;

      if (!valid) throw new IOException("Not a Surface Plotter file");

      // reads version
      if (!stream.readUTF().equals(SurfacePlotter.APP_VERSIGN))
        throw new Exception("Invalid file version");
   
      // reads number of divisions
      int divisions = stream.readInt();

      // reads ranges
      float xi = stream.readFloat();
      float xx = stream.readFloat();
      float yi = stream.readFloat();
      float yx = stream.readFloat();
      float zi = stream.readFloat();
      float zx = stream.readFloat();
    
      // reads function availability
      boolean f1 = stream.readBoolean();
      boolean f2 = stream.readBoolean();
    
      // reads function name

      String f1name = null;
      String f2name = null;
      if (f1) f1name = new String(stream.readUTF());
      if (f2) f2name = new String(stream.readUTF());
       
      // reads vertices array

      int total = (divisions+1)*(divisions+1);
      SurfaceVertex[][] vertex = allocateMemory(f1,f2,total);
      if (vertex == null) return;

      func1calc = f1;
      func2calc = f2;
      func1name = f1name;
      func2name = f2name;
      calc_divisions = divisions;
      
      float min = Float.NaN;
      float max = Float.NaN;
      float data;
      
      if (f1)
        for (int i=0; i < total; i++) {
          data = stream.readFloat();
          vertex[0][i] = new SurfaceVertex(0,0,data);
          if (Float.isNaN(min) || (data < min)) min = data;
          if (Float.isNaN(max) || (data > max)) max = data;
        }
        
      if (f2)
        for (int i=0; i < total; i++) {
          data = stream.readFloat(); 
          vertex[1][i] = new SurfaceVertex(0,0,data); 
          if (Float.isNaN(min) || (data < min)) min = data;
          if (Float.isNaN(max) || (data > max)) max = data;
        } 

      float delta  = 20.0f / (divisions+1);
      float x = -10.0f;
      int k = 0;
      
      for (int i=0; i <= divisions; i++) {
        float y = -10.0f;
        for (int j=0; j <= divisions; j++) {
          vertex[0][k].x = x;
          vertex[0][k].y = y;
          k++; y += delta;
        }
        x += delta;
      }

      // updates display
      
      setting_panel.setRanges(xi,yi,zi,xx,yx,zx);
      canvas.setRanges(xi,xx,yi,yx);
      setting_panel.setMinimumResult(Float.toString(min));
      setting_panel.setMaximumResult(Float.toString(max));
      setting_panel.setCalcDivisions(divisions);
      setting_panel.setDispDivisions(divisions);

      if (f1name != null)
        function_panel.setFunction1Definition(f1name);
      else
        function_panel.setFunction1Definition("");      
      if (f2name != null)
        function_panel.setFunction2Definition(f2name);            
      else
        function_panel.setFunction2Definition("");

      canvas.setValuesArray(vertex);
      setDataAvailability(true);

      setFilename(filename);
      canvas.repaint();
    }
    catch (Exception e) {
      setMessage("Error: " + e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (Exception e) {}
      }
    }
  }
  
  /**
   * Saves Surface Plotter file
   */
   
  private void fileSave(String filename) {    
    if (filename == null) return;

    if (filename.endsWith(".*.*")) {
      filename = filename.substring(0,filename.length()-4);
    }

    SurfaceVertex[][] vertex = canvas.getValuesArray();
    if (vertex == null) return;

    File file = new File(filename);
    FileOutputStream os = null;
    DataOutputStream stream = null;
    
    try {
      os = new FileOutputStream(file);
      stream = new DataOutputStream(os);

      // writes magic numbers
      stream.writeByte(15);
      stream.writeByte(01);
      stream.writeByte(75);
      
      // writes signature
      stream.writeUTF(SurfacePlotter.APP_NAME);

      // writes version  
      stream.writeUTF(SurfacePlotter.APP_VERSIGN);
    
      // writes number of divisions
      stream.writeInt(calc_divisions);
    
      // writes ranges
      float[] ranges = canvas.getRanges();
      for (int i=0; i < 6; i++) stream.writeFloat(ranges[i]);
      
      // writes function availability
      stream.writeBoolean(func1calc);
      stream.writeBoolean(func2calc);
    
      // writes function name
      if (func1calc)
        stream.writeUTF(func1name);
      if (func2calc)
        stream.writeUTF(func2name);
   
      // writes vertices array
      if (func1calc)
        for (int i=0; i < vertex[0].length; i++) 
          stream.writeFloat(vertex[0][i].z);
      if (func2calc)
        for (int i=0; i < vertex[1].length; i++) 
          stream.writeFloat(vertex[1][i].z);   
      
      setFilename(filename);
    }
    catch (Exception e) {
      setMessage("Error: " + e);
    }
    finally {
      if (os != null) {
        try {
          os.close();
        }
        catch (Exception e) {}
      }
    }
  }
  
  /**
   * Inputs file name and saves Surface Plotter file
   */
   
  private void fileSaveAs() {
    SurfaceFileDialog fd = new SurfaceFileDialog(this,"Save As",true); 
    fileSave(fd.getFilename());    
  }
      
  /**
   * Processes menu events
   *
   * @param item the selected menu item
   */
   
  private void processMenuEvent(MenuItem item) {
    switch (whichMenu(item)) {
      case FILE_OPEN:       fileLoad();
                            break;
      case FILE_SAVE:       fileSave(filename);
                            break;
      case FILE_SAVEAS:     fileSaveAs();
                            break;
      case FILE_FILELOCAL:  loadLocalImage();
                            break;
      case FILE_FILENET:    urlinput = new URLInput(this);
                            break;
                            
      case FILE_PRINT:      PrintJob printjob = 
                            getToolkit().getPrintJob(
                            this,"Surface Plotter",printerprops);
                                                        
                            if (printjob != null) {          
                              Graphics pg = printjob.getGraphics();
                              if (pg != null) {
                                canvas.printAll(pg);
                                pg.dispose();         // flush page
                              }
                              printjob.end();
                            }                            
                            break;
                            
      case FILE_EXIT:       if (applet == null) {
                              dispose();
                              System.exit(0);
                            }
                            else
                              applet.disposeWindow();
                            break;

      case EDIT_STRETCHZ:   stretchZ();
                            break;
                            
      case OPT_WIREFRAME:
      case OPT_HIDDEN:
      case OPT_SPECTRUM:
      case OPT_GRAYSCALE:
      case OPT_DUALSHADES:  for (int i=0; i < MODES_COUNT; i++)
                              ((CheckboxMenuItem)
                                getMenuItem(OPT_SHADES+i)).setState(false);
                            ((CheckboxMenuItem)item).setState(true);

                            // Warning. Falls through 
      case OPT_BOXED:
      case OPT_MESH:
      case OPT_FACEGRIDS:
      case OPT_SCALE:
      case OPT_XYTICKS:
      case OPT_ZTICKS:      canvas.destroyImage(); // redraws
                            canvas.repaint(); 
                            break;

      case OPT_SURFACE:     ((CheckboxMenuItem)
                            getMenuItem(OPT_CONTOUR)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_DENSITY)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_SURFACE)).setState(true);
                            canvas.setContour(false);
                            canvas.setDensity(false);
                            setting_panel.enableRotation(true);
                            canvas.destroyImage();
                            canvas.repaint();
                            break;

      case OPT_CONTOUR:     ((CheckboxMenuItem)
                            getMenuItem(OPT_SURFACE)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_DENSITY)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_CONTOUR)).setState(true);
                            canvas.stopRotation();
                            setting_panel.enableRotation(false);
                            canvas.setContour(true);
                            canvas.setDensity(false);
                            canvas.destroyImage();
                            canvas.repaint();
                            break;
      
      case OPT_DENSITY:     ((CheckboxMenuItem)
                            getMenuItem(OPT_SURFACE)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_CONTOUR)).setState(false);
                            ((CheckboxMenuItem)
                            getMenuItem(OPT_DENSITY)).setState(true);
                            canvas.stopRotation();
                            setting_panel.enableRotation(false);
                            canvas.setContour(false);
                            canvas.setDensity(true);
                            canvas.destroyImage();
                            canvas.repaint();
                            break;

      case HELP_MENU:       displayHelp("help/menu.html");
                            break;      
      case HELP_FUNCTION:   displayHelp("help/function.html");
                            break;       
      case HELP_PARTS:      displayHelp("help/parts.html");
                            break;       
      case HELP_MOUSE:      displayHelp("help/mouse.html");
                            break;
      case HELP_DOC:        displayHelp("doc/tree.html");
                            break;
      case HELP_SYSTEMINFO: new SurfaceSysInfo(this);
                            break;
      case HELP_ABOUT:      new SurfaceAbout(this);
                            break;
      default:              break;
    }
  }
    
  /**
   * Allocates Memory
   */

  private SurfaceVertex[][] allocateMemory(boolean f1, boolean f2, int total) {
    SurfaceVertex[][] vertex = null;
   
    // Releases memory being used    
    canvas.setValuesArray(null);

    /* The following program:
      
       SurfaceVertex[][] vertex = new SurfaceVertex[2][];
 
       if (f1) vertex[0] = new SurfaceVertex[total];
       if (f2) vertex[1] = new SurfaceVertex[total];
     
     
       Didn't work with my Microsoft Internet Explorer v3.0b2.
       It resulted in a "java.lang.ArrayStoreException"  :(

     */
          
    try {
      vertex = new SurfaceVertex[2][total];
      if (!f1) vertex[0] = null;
      if (!f2) vertex[1] = null;
    }
    catch(OutOfMemoryError e) {
      setMessage("Not enough memory");
    }
    catch(Exception e) {
      setMessage("Error: " + e.toString());
    }
    return vertex;
  } 
  
  /**
   * Sets file name
   */
  
  private void setFilename(String filename) {
    this.filename = filename;
    String s = filename;
    if (s == null) s = "untitled";
    setTitle(SurfacePlotter.APP_NAME + " - [" + s + "]");   
    ((MenuItem)getMenuItem(FILE_SAVE)).enable(
     (filename != null) && (applet == null));
  }
  
  /**
   * Sets data availability flag
   */
   
  private void setDataAvailability(boolean avail) {
    ((MenuItem)getMenuItem(FILE_SAVEAS)).enable(
     avail && (applet == null));
    canvas.setDataAvailability(avail);
  }
  
  /**
   * Checks if we are running on JVM 1.1 or newer.
   * 
   * @return <code>true</code> if JVM 1.1 or newer   
   */

  private static boolean isNewJVM() {
    String[] list = Toolkit.getDefaultToolkit().getFontList();

    // the following three fonts were added since JDK 1.1
    // so if the system we are running on does not have
    // any of these fonts, it must not be JVM 1.1 or newer.
    
    for (int i=0; i < list.length; i++) {
      if ((list[i].equalsIgnoreCase("monospaced")) ||
          (list[i].equalsIgnoreCase("serif"))      ||
          (list[i].equalsIgnoreCase("sansserif"))) 
      return true;
    }
    return false;
  }
       
  private Properties printerprops = new Properties();

  // avoids garbage collected.
  
  private URLInput urlinput;
  
  private final int FILE_OPEN       = 101;
  private final int FILE_SAVE       = 103;
  private final int FILE_SAVEAS     = 104;
  private final int FILE_FILELOCAL  = 106;
  private final int FILE_FILENET    = 107;
  private final int FILE_PRINT      = 109;
  private final int FILE_EXIT       = 111;

  private final int EDIT_STRETCHZ   = 201;

  private final int OPT_BOXED       = 301;
  private final int OPT_MESH        = 302;
  private final int OPT_FACEGRIDS   = 303;
  private final int OPT_SCALE       = 304;
  private final int OPT_XYTICKS     = 306;
  private final int OPT_ZTICKS      = 307;

  private final int OPT_SHADES      = 309;
  private final int OPT_WIREFRAME   = 309;
  private final int OPT_HIDDEN      = 310;
  private final int OPT_SPECTRUM    = 311;
  private final int OPT_GRAYSCALE   = 312;
  private final int OPT_DUALSHADES  = 313;
  
  private final int OPT_SURFACE     = 315;
  private final int OPT_CONTOUR     = 316;
  private final int OPT_DENSITY     = 317;

  private final int HELP_MENU       = 401;
  private final int HELP_FUNCTION   = 402;
  private final int HELP_PARTS      = 403;
  private final int HELP_MOUSE      = 404;
  private final int HELP_DOC        = 406;

  private final int HELP_SYSTEMINFO = 408;
  private final int HELP_ABOUT      = 410;
}

