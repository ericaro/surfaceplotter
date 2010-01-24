package com.googlecode.surfaceplotter;
/*----------------------------------------------------------------------------------------*
 * SurfaceCanvas.java                                                                     *
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.PrintGraphics;
import java.awt.Rectangle;

/**
 * The class <code>SurfaceCanvas</code> is responsible
 * for the generation of surface images and user mouse events handling. 
 *
 * @author  Yanto Suryono
 */

public final class SurfaceCanvas extends Canvas implements Runnable {
  private static SurfaceFrame frame;              // the parent, Surface Plotter frame 
  private static Image Buffer;                    // the backing buffer
  private static Graphics BufferGC;               // the graphics context of backing buffer
  private static boolean image_drawn;             // image drawn flag
  private static Projector projector;             // the projector
  private static Thread thread;                   // current thread
  private static SurfaceVertex[][] vertex;        // vertices array
  private static boolean data_available;          // data availability flag
  private static boolean interrupted;             // interrupted flag
  private static boolean critical;                // for speed up
  private static boolean rotate;                  // rotate flag
  private static boolean printing;                // printing flag
  private static boolean contour;                 // contour flag
  private static boolean density;                 // density flag
  private static int prevwidth, prevheight;       // canvas size  
  private static int printwidth, printheight;     // print size
  private static float color;                     // color of surface
  private static SurfaceVertex cop;               // center of projection
  
  // setting variables
  
  private static int plot_mode;
  private static int calc_divisions; 
  private static boolean plotfunc1, plotfunc2, plotboth;
  private static boolean isBoxed, isMesh, isScaleBox, 
                         isDisplayXY, isDisplayZ, isDisplayGrids;
  private static float xmin,xmax,ymin;
  private static float ymax,zmin,zmax;

  // constants
                
  private static final int WIREFRAME   = 0;
  private static final int NORENDER    = 1;
  private static final int SPECTRUM    = 2;
  private static final int GRAYSCALE   = 3;
  private static final int DUALSHADE   = 4;
  
  private static final int TOP         = 0;
  private static final int CENTER      = 1;

  // for splitting polygons
  
  private static final int UPPER       =  1;
  private static final int COINCIDE    =  0;
  private static final int LOWER       = -1;
    
  /**
   * The constructor of <code>SurfaceCanvas</code>
   *
   * @param frame The parent frame. The frame must be an instance of 
   *              <code>SurfaceFrame</code>.                             
   * @see         SurfaceFrame
   */

  SurfaceCanvas(SurfaceFrame frame) {
    super();
    this.frame = frame;
    Buffer = null;
    BufferGC = null;
    image_drawn = interrupted = false;
    data_available = false;
    printing = rotate = false;
    contour = density = false;
    thread = new Thread(this);
    prevwidth = prevheight = -1;
    projector = new Projector();
    projector.setDistance(70);
    projector.set2DScaling(15);
    projector.setRotationAngle(125);
    projector.setElevationAngle(10);
    SurfaceVertex.setProjector(projector);
    vertex = new SurfaceVertex[2][];
  }
  
  /**
   * Destroys the internal image. It will force <code>SurfaceCanvas</code>
   * to regenerate all images when the <code>paint</code> method is called. 
   */

  public void destroyImage() {
    image_drawn = false;
  }

  /**
   * Sets contour plot flag. <code>SurfaceCanvas</code> will generate contour
   * plot of surface if this flag is true.
   *
   * @param contour new contour plot flag
   * @see   #setDensity
   */

  public void setContour(boolean contour) {
    this.contour = contour;
  }

  /**
   * Sets density plot flag. <code>SurfaceCanvas</code> will generate density
   * plot of surface if this flag is true. In case that the contour plot flag
   * and the density plot flag are both <code>true</code>, 
   * <code>SurfaceCanvas</code> generates the contour plot.
   *
   * @param density new density plot flag
   * @see   #setContour
   */
   
  public void setDensity(boolean density) {
    this.density = density;
  }

  /**
   * Sets the x and y ranges of calculated surface vertices. 
   * The ranges will not affect surface appearance. They affect axes 
   * scale appearance. 
   *
   * @param xmin the minimum x
   * @param xmax the maximum x
   * @param ymin the minimum y
   * @param ymax the maximum y
   */

  public void setRanges(float xmin, float xmax, float ymin, float ymax) {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
  }
  
  /**
   * Gets the current x, y, and z ranges. 
   *
   * @return array of x,y, and z ranges in order of 
   *         xmin, xmax, ymin, ymax, zmin, zmax 
   */

  public float[] getRanges() {
    float[] ranges = new float[6];
    
    ranges[0] = xmin;
    ranges[1] = xmax;
    ranges[2] = ymin;
    ranges[3] = ymax;
    ranges[4] = zmin;
    ranges[5] = zmax;
    
    return ranges;
  }
    
  /**
   * Sets the data availability flag. If this flag is <code>false</code>,
   * <code>SurfaceCanvas</code> will not generate any surface image, even
   * if the data is available. But it is the programmer's responsiblity 
   * to set this flag to <code>false</code> when data is not available. 
   *
   * @param avail the availability flag
   */

  public void setDataAvailability(boolean avail) {
    data_available = avail;
    is_data_available = avail;  // see Handlers for mouse input events section
  }

  /**
   * Sets the new vertices array of surface. 
   *
   * @param vertex the new vertices array   
   * @see   #getValuesArray
   */

  public void setValuesArray(SurfaceVertex[][] vertex) {
    this.vertex = vertex;
  }
  
  /**
   * Gets the current vertices array.
   *
   * @return current vertices array
   * @see    #setValuesArray
   */

  public SurfaceVertex[][] getValuesArray() {
    if (!data_available) return null;
    return vertex;
  }
    
  private boolean is_data_available;   // holds the original data availability flag
  private boolean dragged;             // dragged flag
  private int     click_x, click_y;    // previous mouse cursor position
    
  /**
   * <code>mouseDown</code> event handler. Sets internal tracking variables
   * for dragging operations.
   *
   * @param e the event
   * @param x the x coordinate of cursor
   * @param y the y coordinate of cursor
   */

  public boolean mouseDown(Event e, int x, int y) {
    click_x = x;
    click_y = y;
    return true;
  }
  
  /**
   * <code>mouseUp<code> event handler. Regenerates image if dragging operations 
   * have been done with the delay regeneration flag set on.
   *
   * @param e the event
   * @param x the x coordinate of cursor
   * @param y the y coordinate of cursor
   */

  public boolean mouseUp(Event e, int x, int y) {
    if (rotate || contour || density) return true; 
    if (frame.isExpectDelay() && dragged) {
      destroyImage(); 
      data_available = is_data_available;
      repaint(); 
      dragged = false;
    }
    return true; 
  }

  /**
   * <code>mouseDrag<code> event handler. Tracks dragging operations. 
   * Checks the delay regeneration flag and does proper actions.
   *
   * @param e the event
   * @param x the x coordinate of cursor
   * @param y the y coordinate of cursor
   */

  public boolean mouseDrag(Event e, int x, int y) {
    float new_value = 0.0f;

    if (rotate || contour || density) return true; 
    if (!thread.isAlive() || !data_available) {
      if (e.controlDown()) {
        projector.set2D_xTranslation(
        projector.get2D_xTranslation() + (x - click_x));
        projector.set2D_yTranslation(
        projector.get2D_yTranslation() + (y - click_y));
      }
      else
      if (e.shiftDown()) {
        new_value = projector.get2DScaling() + (y - click_y) * 0.5f;
        if (new_value > 60.0f) new_value = 60.0f;
        if (new_value < 2.0f) new_value = 2.0f;
        projector.set2DScaling(new_value);
      }
      else {
        new_value = projector.getRotationAngle() + (x - click_x);
        while (new_value > 360) new_value -= 360;
        while (new_value < 0) new_value += 360;
        projector.setRotationAngle(new_value);
        new_value = projector.getElevationAngle() + (y - click_y);
        if (new_value > 90) new_value = 90;
          else
        if (new_value < 0) new_value = 0;
        projector.setElevationAngle(new_value);
      } 
      image_drawn = false;
      if (!frame.isExpectDelay()) {
        repaint(); 
      }
      else {
        if (!dragged) {
          is_data_available = data_available;
          dragged = true;
        }
        data_available = false;
        repaint();
      }        
    }
    click_x = x;
    click_y = y;
    return true;
  }

  /**
   * Paints surface. Creates surface plot, contour plot, or density plot
   * based on current vertices array, contour plot flag, and density plot
   * flag. If no data is available, creates image of base plane and axes. 
   *
   * @param g the graphics context to paint
   * @see   #setContour
   * @see   #setDensity
   * @see   #setValuesArray
   * @see   #setDataAvailability   
   */

  public void paint(Graphics g) {
    if ((bounds().width <= 0) || (bounds().height <= 0)) return;

    // backing buffer creation

    if ((bounds().width != prevwidth) || 
        (bounds().height != prevheight) || 
        (BufferGC == null)) {
      frame.setMessage("New image size: " + bounds().width + "x" + bounds().height);
      projector.setProjectionArea(new Rectangle(0,0,bounds().width, bounds().height));  
      image_drawn = false;
      if (Buffer != null) Buffer.flush();
      Buffer = createImage(bounds().width, bounds().height);
      if (BufferGC != null) BufferGC.dispose();
      BufferGC = Buffer.getGraphics();     
      prevwidth = bounds().width;
      prevheight = bounds().height;
    }

    importVariables();
        
    printing = SurfaceFrame.newJVM && (g instanceof PrintGraphics);
      
    if (printing) {

      // modifies variables

      Graphics savedgc = BufferGC;
      BufferGC = g;
      
      Dimension pagedimension = ((PrintGraphics)g).getPrintJob().getPageDimension();
      
      printwidth = pagedimension.width;
      printheight = prevheight * printwidth / prevwidth;

      if (printheight > pagedimension.height) {
        printheight = pagedimension.height;
        printwidth = prevwidth * printheight / prevheight;
      }

      float savedscalingfactor = projector.get2DScaling();
      projector.setProjectionArea(new Rectangle(0,0,printwidth,printheight));
      projector.set2DScaling(savedscalingfactor * printwidth / prevwidth);

      BufferGC.clipRect(0,0,printwidth,printheight);
            
      // starts printing thread
      
      if (!data_available) 
        drawBoxGridsTicksLabels(BufferGC,true);
      else {
        thread = new Thread(this);
        thread.start();
        while (thread.isAlive()) Thread.yield();
      }
      BufferGC.drawRect(0,0,printwidth-1,printheight-1);
      
      // restores variables
      
      projector.set2DScaling(savedscalingfactor);
      projector.setProjectionArea(new Rectangle(0,0,bounds().width, bounds().height));  
      BufferGC = savedgc;
      return;
    }
    
    if (image_drawn && (Buffer != null)) {
      g.drawImage(Buffer,0,0,this);
    }
    else
    if (data_available && !interrupted) {
      if (thread.isAlive()) {
        thread.stop();
        while (thread.isAlive()) {
          thread.yield();
        } 
      }
      thread = new Thread(this);
      thread.start();
    }
    else {
      g.setColor(Color.lightGray);
      g.fillRect(0,0,bounds().width,bounds().height); 
      if (!contour && !density) drawBoxGridsTicksLabels(g,true);
    }
    interrupted = false;
  }

  /**
   * Updates image. Just call the <code>paint</code> method to
   * avoid flickers.
   *
   * @param g the graphics context to update
   * @see   #paint
   */

  public void update(Graphics g) {
    paint(g);                        // do not erase, just paint
  }  

  /**
   * The implementation of <code>Runnable</code> interface.
   * Performs surface plotting as a background process at a separate thread.
   */

  public void run() {
    boolean repeat;

    int fontsize = (int)(Math.round(projector.get2DScaling() * 0.5));
    BufferGC.setFont(new Font("Helvetica",Font.PLAIN,fontsize));
    
    do {
      SurfaceVertex.invalidate(); 

      // contour plot      
      if (contour) plotContour(); else
      
      // density plot
      if (density) plotDensity(); else {
      
      // surface plot
        if (plot_mode == WIREFRAME)
          plotWireframe(); 
        else
          plotSurface();
      }
      
      cleanUpMemory();
      if (printing) return;
                    
      // force repaint NOW
      Graphics g = getGraphics();
      paint(g);
      g.dispose();
      
      repeat = rotate;
      if (repeat) {

        // automatically rotates surface
        float newrot = projector.getRotationAngle() + 5;
        while (newrot > 360) newrot -= 360;
        while (newrot < 0) newrot += 360;
        projector.setRotationAngle(newrot);
      }
    }
    while (repeat); 
  }

  /**
   * Starts automatic rotation. This method MUST NOT be called
   * when contour plot flag and/or density plot flag is <code>true</code>.
   */

  public void startRotation() {
    if ((thread != null) && thread.isAlive()) return;
    if (!data_available) return; 
    rotate = true;
    destroyImage(); repaint();
    frame.rotationStarts();
  }
  
  /**
   * Stops the automatic rotation. Nothing happens if automatic rotation
   * is not being performed.
   */

  public void stopRotation() {
    if ((thread == null) || !thread.isAlive() || !rotate) return;
    rotate = false;
    frame.rotationStops();
  }     

  /**
   * Interrupts current plotting operation. This will simply kill
   * the running thread and display the string <i>interrupted</i>
   * at the parent frame's status line.
   */

  public synchronized void interrupt() {
    if (thread.isAlive()) {
      thread.stop();
      try {
        thread.join();
      }
      catch (InterruptedException e) {};
      frame.setMessage("Interrupted");
      cleanUpMemory();
      rotate = false;
      image_drawn = false;
      interrupted = true;
      repaint();
    }
  }
  
  /**
   * Returns the preferred size of this object. This will be the initial
   * size of <code>SurfaceCanvas</code>.
   *
   * @return the preferred size.
   */

  public Dimension preferredSize() {
    return new Dimension(550,550);  // initial canvas size
  }

/*----------------------------------------------------------------------------------------*
 *                            Private methods begin here                                  *
 *----------------------------------------------------------------------------------------*/

  private int factor_x, factor_y;   // conversion factors 
  private int t_x, t_y, t_z;        // determines ticks density

  /**
   * Draws the bounding box of surface.
   */

  private final void drawBoundingBox() {
    Point startingpoint, projection;

    startingpoint = projector.project(factor_x*10,factor_y*10,10);
    BufferGC.setColor(Color.black);
    projection =  projector.project(-factor_x*10,factor_y*10,10);
    BufferGC.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    projection =  projector.project(factor_x*10,-factor_y*10,10);
    BufferGC.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    projection =  projector.project(factor_x*10,factor_y*10,-10);
    BufferGC.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
  }

  /**
   * Draws the base plane. The base plane is the x-y plane.
   *
   * @param g the graphics context to draw.
   * @param x used to retrieve x coordinates of drawn plane from this method. 
   * @param y used to retrieve y coordinates of drawn plane from this method.
   */

  private final void drawBase(Graphics g, int[] x, int[] y) {
    Point projection = projector.project(-10,-10,-10);                  
    x[0] = projection.x; y[0] = projection.y;
    projection = projector.project(-10, 10,-10);                  
    x[1] = projection.x; y[1] = projection.y;
    projection = projector.project( 10, 10,-10);                  
    x[2] = projection.x; y[2] = projection.y;
    projection = projector.project( 10,-10,-10);                  
    x[3] = projection.x; y[3] = projection.y;
    x[4] = x[0]; y[4] = y[0];

    if (plot_mode != WIREFRAME) {
      if (plot_mode == NORENDER)
        g.setColor(Color.lightGray);
      else
        g.setColor(new Color(192,220,192));      
        g.fillPolygon(x,y,4);
    }
    g.setColor(Color.black);
    g.drawPolygon(x,y,5);
  }

  /**
   * Draws non-surface parts, i.e: bounding box, axis grids, axis ticks,
   * axis labels, base plane. 
   *
   * @param g         the graphics context to draw
   * @param draw_axes if <code>true</code>, only draws base plane and z axis
   */

  private final void drawBoxGridsTicksLabels(Graphics g, boolean draw_axes) {
    Point    projection, tickpos;   
    boolean  x_left = false, y_left = false;
    int      x[],y[],i;

    x = new int[5]; y = new int[5];
    if (projector == null) return;

    if (draw_axes) {
      drawBase(g,x,y);
      projection = projector.project(0,0,-10);                  
      x[0] = projection.x; y[0] = projection.y;
      projection = projector.project(10.5f,0,-10);                  
      g.drawLine(x[0],y[0],projection.x,projection.y);
      if (projection.x < x[0]) 
        outString(g,(int)(1.05*(projection.x-x[0]))+x[0],
                    (int)(1.05*(projection.y-y[0]))+y[0],"x",Label.RIGHT,TOP);
      else
        outString(g,(int)(1.05*(projection.x-x[0]))+x[0],
                    (int)(1.05*(projection.y-y[0]))+y[0],"x",Label.LEFT,TOP);
      projection = projector.project(0,11.5f,-10);                  
      g.drawLine(x[0],y[0],projection.x,projection.y);
      if (projection.x < x[0]) 
        outString(g,(int)(1.05*(projection.x-x[0]))+x[0],
                    (int)(1.05*(projection.y-y[0]))+y[0],"y",Label.RIGHT,TOP);
      else
        outString(g,(int)(1.05*(projection.x-x[0]))+x[0],
                     (int)(1.05*(projection.y-y[0]))+y[0],"y",Label.LEFT,TOP);
      projection = projector.project(0,0,10.5f);                  
      g.drawLine(x[0],y[0],projection.x,projection.y);
      outString(g,(int)(1.05*(projection.x-x[0]))+x[0],                    
                  (int)(1.05*(projection.y-y[0]))+y[0],"z",Label.CENTER,CENTER);
    } 
    else {
      factor_x = factor_y = 1;
      projection = projector.project(0,0,-10);                  
      x[0] = projection.x; 
      projection = projector.project(10.5f,0,-10);                  
      y_left = projection.x > x[0];
      i = projection.y;
      projection = projector.project(-10.5f,0,-10);
      if (projection.y > i) {
        factor_x = -1;
        y_left = projection.x > x[0];
      }
      projection = projector.project(0,10.5f,-10);                  
      x_left = projection.x > x[0];
      i = projection.y;
      projection = projector.project(0,-10.5f,-10);
      if (projection.y > i) {
        factor_y = -1;
        x_left = projection.x > x[0]; 
      }
      setAxesScale(); drawBase(g,x,y);

      if (isBoxed) {
        projection = projector.project(-factor_x*10,-factor_y*10,-10);
        x[0] = projection.x; y[0] = projection.y;
        projection = projector.project(-factor_x*10,-factor_y*10,10);                  
        x[1] = projection.x; y[1] = projection.y;
        projection = projector.project(factor_x*10,-factor_y*10,10);
        x[2] = projection.x; y[2] = projection.y;
        projection = projector.project(factor_x*10,-factor_y*10,-10);
        x[3] = projection.x; y[3] = projection.y;
        x[4] = x[0]; y[4] = y[0];
  
        if (plot_mode != WIREFRAME) {
          if (plot_mode == NORENDER)
            g.setColor(Color.lightGray);
          else
            g.setColor(new Color(192,220,192));          
          g.fillPolygon(x,y,4);
        }
        g.setColor(Color.black);
        g.drawPolygon(x,y,5);

        projection = projector.project(-factor_x*10,factor_y*10,10);
        x[2] = projection.x; y[2] = projection.y;
        projection = projector.project(-factor_x*10,factor_y*10,-10);
        x[3] = projection.x; y[3] = projection.y;
        x[4] = x[0]; y[4] = y[0];
  
        if (plot_mode != WIREFRAME) {
          if (plot_mode == NORENDER)
            g.setColor(Color.lightGray);
          else
            g.setColor(new Color(192,220,192));
          g.fillPolygon(x,y,4);
        }
        g.setColor(Color.black);
        g.drawPolygon(x,y,5);
      }
      else
      if (isDisplayZ) {
        projection = projector.project(factor_x*10,-factor_y*10,-10);
        x[0] = projection.x; y[0] = projection.y;  
        projection = projector.project(factor_x*10,-factor_y*10,10);
        g.drawLine(x[0],y[0],projection.x,projection.y);

        projection = projector.project(-factor_x*10,factor_y*10,-10);
        x[0] = projection.x; y[0] = projection.y;  
        projection = projector.project(-factor_x*10,factor_y*10,10);
        g.drawLine(x[0],y[0],projection.x,projection.y);
      }

      for (i=-9; i <= 9; i++) {
        if (isDisplayXY || isDisplayGrids) {
          if (!isDisplayGrids || (i % (t_y / 2) == 0) || isDisplayXY) {
            if (isDisplayGrids && (i % t_y == 0)) 
              projection = projector.project(-factor_x * 10,i,-10);
            else {
              if (i % t_y != 0) 
                projection = projector.project(factor_x * 9.8f,i,-10);
              else
                projection = projector.project(factor_x * 9.5f,i,-10);
            }
            tickpos = projector.project(factor_x * 10,i,-10);
            g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
            if ((i % t_y == 0) && isDisplayXY) {
              tickpos = projector.project(factor_x * 10.5f,i,-10);
              if (y_left)
                outFloat(g,tickpos.x,tickpos.y,
                         (float)((double)(i+10)/20*(ymax-ymin)+ymin),
                         Label.LEFT,TOP);
              else
                outFloat(g,tickpos.x,tickpos.y,
                         (float)((double)(i+10)/20*(ymax-ymin)+ymin),
                         Label.RIGHT,TOP);
            }
          }  
          if (!isDisplayGrids || (i % (t_x / 2) == 0) || isDisplayXY) {
            if (isDisplayGrids && (i % t_x == 0))
              projection = projector.project(i,-factor_y * 10,-10);
            else {
              if (i % t_x != 0) 
                projection = projector.project(i,factor_y * 9.8f,-10);
              else
                projection = projector.project(i,factor_y * 9.5f,-10);
            }
            tickpos = projector.project(i,factor_y * 10,-10);
            g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
            if ((i % t_x == 0) && isDisplayXY) {
              tickpos = projector.project(i,factor_y * 10.5f,-10);
              if (x_left)
                outFloat(g,tickpos.x,tickpos.y,
                         (float)((double)(i+10)/20*(xmax-xmin)+xmin),
                         Label.LEFT,TOP);
              else
                outFloat(g,tickpos.x,tickpos.y,
                         (float)((double)(i+10)/20*(xmax-xmin)+xmin),
                         Label.RIGHT,TOP);
            }
          }
        }  
        
        if (isDisplayXY) {
          tickpos = projector.project(0,factor_y * 14,-10);
          outString(g,tickpos.x,tickpos.y,"X",Label.CENTER,TOP);
          tickpos = projector.project(factor_x * 14,0,-10);
          outString(g,tickpos.x,tickpos.y,"Y",Label.CENTER,TOP);
        }

        // z grids and ticks

        if (isDisplayZ || (isDisplayGrids && isBoxed)) {
          if (!isDisplayGrids || (i % (t_z / 2) == 0) || isDisplayZ) {
            if (isBoxed && isDisplayGrids && (i % t_z == 0)) {
              projection = projector.project(-factor_x*10,-factor_y*10,i);
              tickpos = projector.project(-factor_x*10,factor_y*10,i);
            }
            else {
              if (i % t_z == 0) 
                projection = projector.project(-factor_x*10,factor_y*9.5f,i);
              else
                projection = projector.project(-factor_x*10,factor_y*9.8f,i);  
              tickpos = projector.project(-factor_x*10,factor_y*10,i);
            }                            
            g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
            if (isDisplayZ) {
              tickpos = projector.project(-factor_x*10,factor_y*10.5f,i);
              if (i % t_z == 0) {
                if (x_left)
                  outFloat(g,tickpos.x,tickpos.y,
                           (float)((double)(i+10)/20*(zmax-zmin)+zmin),
                           Label.LEFT,CENTER);
                else
                  outFloat(g,tickpos.x,tickpos.y,
                           (float)((double)(i+10)/20*(zmax-zmin)+zmin),
                           Label.RIGHT,CENTER);
              }
            }
            if (isDisplayGrids && isBoxed && (i % t_z == 0)) {
              projection = projector.project(-factor_x*10,-factor_y*10,i);
              tickpos = projector.project(factor_x*10,-factor_y*10,i);
            } 
            else {
              if (i % t_z == 0)
                projection = projector.project(factor_x*9.5f,-factor_y*10,i);
              else
                projection = projector.project(factor_x*9.8f,-factor_y*10,i);
              tickpos = projector.project(factor_x*10,-factor_y*10,i);
            }             
            g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
            if (isDisplayZ) {
              tickpos = projector.project(factor_x*10.5f,-factor_y*10,i);
              if (i % t_z == 0) {
                if (y_left)
                  outFloat(g,tickpos.x,tickpos.y,
                           (float)((double)(i+10)/20*(zmax-zmin)+zmin),
                           Label.LEFT,CENTER);
                else
                  outFloat(g,tickpos.x,tickpos.y,
                           (float)((double)(i+10)/20*(zmax-zmin)+zmin),
                           Label.RIGHT,CENTER);
              }
            }
            if (isDisplayGrids && isBoxed) {
              if (i % t_y == 0) {
                projection = projector.project(-factor_x * 10,i,-10);
                tickpos = projector.project(-factor_x * 10,i,10);
                g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
              }
              if (i % t_x  == 0) {
                projection = projector.project(i,-factor_y * 10,-10);
                tickpos = projector.project(i,-factor_y * 10,10);
                g.drawLine(projection.x,projection.y,tickpos.x,tickpos.y);
              }
            }   
          }
        }
      }
    }
  }

  /**
   * Imports setting variables from parent frame. This method is before doing 
   * any plotting operations
   *
   * @see     #SurfaceVanvas (constructor)
   */

  private void importVariables() {
    plot_mode = frame.getPlotMode();
    isBoxed = frame.isBoxed();
    isMesh = frame.isMesh();
    isScaleBox = frame.isScaleBox();
    isDisplayXY = frame.isDisplayXY();
    isDisplayZ = frame.isDisplayZ();
    isDisplayGrids = frame.isDisplayGrids();
    calc_divisions = frame.getCalcDivisions();
    plotfunc1 = frame.isPlotFunction1();
    plotfunc2 = frame.isPlotFunction2();
    plotboth = plotfunc1 && plotfunc2;
  }

  /**
   * Sets the axes scaling factor. Computes the proper axis lengths
   * based on the ratio of variable ranges. The axis lengths will
   * also affect the size of bounding box. 
   */

  private final void setAxesScale() {
    float scale_x, scale_y, scale_z ,divisor;
    int   longest;

    if (!isScaleBox) {
      projector.setScaling(1); 
      t_x = t_y = t_z = 4; 
      return;
    }

    scale_x = xmax - xmin; scale_y = ymax - ymin; scale_z = zmax - zmin;

    if (scale_x < scale_y) {
      if (scale_y < scale_z) {
        longest = 3; divisor = scale_z;
      }
      else {
        longest = 2; divisor = scale_y;
      }
    }
    else {
      if (scale_x < scale_z) {
        longest = 3; divisor = scale_z;
      }
      else {
        longest = 1; divisor = scale_x;
      }
    }
    scale_x /= divisor; scale_y /= divisor; scale_z /= divisor;
 
    if ((scale_x < 0.2f) || (scale_y < 0.2f) && (scale_z < 0.2f)) {
      switch(longest) {
        case 1: if (scale_y < scale_z) {
                  scale_y /= scale_z; scale_z = 1.0f;
                }
                else {
                  scale_z /= scale_y; scale_y = 1.0f;
                } break;
        case 2: if (scale_x < scale_z) {
                  scale_x /= scale_z; scale_z = 1.0f;
                }
                else {
                  scale_z /= scale_x; scale_x = 1.0f;
                } break;
        case 3: if (scale_y < scale_x) {
                  scale_y /= scale_x; scale_x = 1.0f;
                }
                else {
                  scale_x /= scale_y; scale_y = 1.0f;
                } break;
      }
    }
    if (scale_x < 0.2f) scale_x = 1.0f; projector.setXScaling(scale_x);
    if (scale_y < 0.2f) scale_y = 1.0f; projector.setYScaling(scale_y);
    if (scale_z < 0.2f) scale_z = 1.0f; projector.setZScaling(scale_z);

    if (scale_x < 0.5f) t_x = 8; else t_x = 4;
    if (scale_y < 0.5f) t_y = 8; else t_y = 4;
    if (scale_z < 0.5f) t_z = 8; else t_z = 4;
  }

  /**
   * Draws string at the specified coordinates with the specified alignment.
   *
   * @param g       graphics context to draw
   * @param x       the x coordinate
   * @param y       the y coordinate
   * @param s       the string to draw
   * @param x_align the alignment in x direction
   * @param y_align the alignment in y direction    
   */

  private final void outString(Graphics g, int x, int y, 
                               String s, int x_align, int y_align) {
    switch (y_align) {
      case TOP    : y += g.getFontMetrics(g.getFont()).getAscent(); break;
      case CENTER : y += g.getFontMetrics(g.getFont()).getAscent()/2; break;
    }
    switch (x_align) {
      case Label.LEFT  : g.drawString(s,x,y); 
                         break;
      case Label.RIGHT : g.drawString(s,x-g.getFontMetrics(
                                      g.getFont()).stringWidth(s),y); 
                         break;
      case Label.CENTER: g.drawString(s,x-g.getFontMetrics(
                                      g.getFont()).stringWidth(s)/2,y); 
                         break;
    }     
  }

  /**
   * Draws float at the specified coordinates with the specified alignment.
   *
   * @param g       graphics context to draw
   * @param x       the x coordinate
   * @param y       the y coordinate
   * @param f       the float to draw
   * @param x_align the alignment in x direction
   * @param y_align the alignment in y direction    
   */

  private final void outFloat(Graphics g, int x, int y, 
                              float f, int x_align, int y_align) {
    String s = Float.toString(f);
    outString(g,x,y,s,x_align,y_align);
  }

/*----------------------------------------------------------------------------------------*
 *                       Plotting routines and methods begin here                         *
 *----------------------------------------------------------------------------------------*/

  private static float color_factor;
  private static Point projection;   
  private static Color line_color; 
  
  private final static int poly_x[] = new int[9];                       
  private final static int poly_y[] = new int[9];                   

  /**
   * Plots a single plane
   *
   * @param vertex vertices array of the plane
   * @param verticescount number of vertices to process 
   */

  private final void plotPlane(SurfaceVertex[] vertex, int verticescount) {
    int     count,loop,index;
    float   z,result;
    boolean low1,low2;
    boolean valid1,valid2;

    if (verticescount < 3) return; 
    count = 0; z = 0.0f; line_color = Color.black;
    low1 = (vertex[0].z < zmin); valid1 = !low1 && (vertex[0].z <= zmax); index = 1;
    for (loop = 0; loop < verticescount; loop++) {
      low2 = (vertex[index].z < zmin); valid2 = !low2 && (vertex[index].z <= zmax);
      if ((valid1 || valid2) || (low1^low2)) {
        if (!valid1) {
          if (low1) result = zmin; else result = zmax;
          float ratio = (result-vertex[index].z)/(vertex[loop].z-vertex[index].z);
          float new_x = ratio*(vertex[loop].x-vertex[index].x)+vertex[index].x; 
          float new_y = ratio*(vertex[loop].y-vertex[index].y)+vertex[index].y;
          if (low1)
            projection = projector.project(new_x,new_y,-10);
          else
            projection = projector.project(new_x,new_y,10);
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;  
          count++; z += result;
        } 
        if (valid2) {
          projection = vertex[index].projection();
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;  
          count++; z += vertex[index].z;
        }
        else {
          if (low2) result = zmin; else result = zmax;
          float ratio = (result-vertex[loop].z)/(vertex[index].z-vertex[loop].z);
          float new_x = ratio*(vertex[index].x-vertex[loop].x)+vertex[loop].x; 
          float new_y = ratio*(vertex[index].y-vertex[loop].y)+vertex[loop].y;
          if (low2)
            projection = projector.project(new_x,new_y,-10);
          else
            projection = projector.project(new_x,new_y,10);
          poly_x[count] = projection.x;
          poly_y[count] = projection.y;  
          count++; z += result;
        } 
      }
      if (++index == verticescount) index = 0; 
      valid1 = valid2; low1 = low2;
    }
    if (count > 0) {
      switch (plot_mode) {
        case NORENDER:    BufferGC.setColor(Color.lightGray); 
                          break;
        case SPECTRUM:    z = 0.8f-(z/count-zmin)*color_factor;
                          BufferGC.setColor(Color.getHSBColor(z,1.0f,1.0f)); 
                          break;
        case GRAYSCALE:   z = (z/count-zmin)*color_factor;
                          BufferGC.setColor(Color.getHSBColor(0,0,z));
                          if (z < 0.3f) line_color = new Color(0.6f,0.6f,0.6f);                    
                          break; 
        case DUALSHADE:   z = (z/count-zmin)*color_factor+0.4f;
                          BufferGC.setColor(Color.getHSBColor(color,0.7f,z));
                          break;
      }
                               
      BufferGC.fillPolygon(poly_x,poly_y,count);
      BufferGC.setColor(line_color);
      if (isMesh || (plot_mode == NORENDER)) {
        poly_x[count] = poly_x[0];
        poly_y[count] = poly_y[0];
        count++;
        BufferGC.drawPolygon(poly_x,poly_y,count);
      }
    }
  }
  
  private final static SurfaceVertex upperpart[] = new SurfaceVertex[8];
  private final static SurfaceVertex lowerpart[] = new SurfaceVertex[8];

  /**
   * Given two vertices array of plane, intersects and plots them. Splits 
   * one of the planes if needed.
   *
   * @param values1 vertices array of first plane
   * @param values2 vertices array of second plane
   */

  private final void splitPlotPlane(SurfaceVertex[] values1, SurfaceVertex[] values2) { 
    int trackposition = COINCIDE;
    int uppercount = 0, lowercount = 0;
    boolean coincide = true;
    boolean upper_first = false;
    float factor, xi ,yi, zi;        
    int i = 0, j = 0;
  
    for (int counter=0; counter <= 4; counter++) {
      if (values1[i].z < values2[i].z) {
        coincide = false;
        if (trackposition == COINCIDE) {
          trackposition = UPPER;
          upperpart[uppercount++] = values2[i];
        }
        else
        if (trackposition != UPPER) {      
        
          // intersects
          
          factor = (values1[i].z-values2[i].z)/
                   (values1[i].z-values2[i].z+values2[j].z-values1[j].z);
          if (values1[i].x==values1[j].x) {   
          
            // intersects in y direction
            
            yi = factor * (values1[j].y-values1[i].y) + values1[i].y;
            xi = values1[i].x;            
          }
          else { 
          
            // intersects in x direction
            
            xi = factor * (values1[j].x-values1[i].x) + values1[i].x;
            yi = values1[i].y;            
          }
          zi = factor * (values2[j].z-values2[i].z) + values2[i].z;

          upperpart[uppercount++] = 
          lowerpart[lowercount++] = new SurfaceVertex(xi,yi,zi);
          upperpart[uppercount++] = values2[i];                    

          trackposition = UPPER;          
        }
        else {            
          upperpart[uppercount++] = values2[i];
        }

      }
      else
      if (values1[i].z > values2[i].z) {
        coincide = false;
        if (trackposition == COINCIDE) {
          trackposition = LOWER;
          lowerpart[lowercount++] = values2[i];
        }
        else
        if (trackposition != LOWER) {      
        
          // intersects
          
          factor = (values1[i].z-values2[i].z)/
                   (values1[i].z-values2[i].z+values2[j].z-values1[j].z);
          if (values1[i].x==values1[j].x) {   
          
            // intersects in y direction
            
            yi = factor * (values1[j].y-values1[i].y) + values1[i].y;
            xi = values1[i].x;            
          }
          else {            
          
            // intersects in x direction
           
            xi = factor * (values1[j].x-values1[i].x) + values1[i].x;
            yi = values1[i].y;            
          }
          zi = factor * (values2[j].z-values2[i].z) + values2[i].z;

          lowerpart[lowercount++] = 
          upperpart[uppercount++] = new SurfaceVertex(xi,yi,zi);
          lowerpart[lowercount++] = values2[i];
  
          trackposition = LOWER;          
        }
        else {            
          lowerpart[lowercount++] = values2[i];
        }        
      }
      else {
        upperpart[uppercount++] = values2[i];
        lowerpart[lowercount++] = values2[i];
        trackposition = COINCIDE;
      }
      
      j = i;
      i = (i + 1) % 4;
    }

    if (coincide) {            // the two planes completely coincide
      plotPlane(values1,4);
    }
    else {
      if (critical) 
        upper_first = false; 
      else {
      
        /*
            Priority Determination:
       
            Theory: if center of projection (c.o.p) is above plane 0-1-2-3, then the plane 
                    below plane 0-1-2-3 should be plotted first, otherwise the plane above 
                    plane 0-1-2-3 should be plotted first.
            Task:   calculate the height of plane 0-1-2-3 at the projection of c.o.p
                    on plane xy (point c) and compare it with the height of c.o.p

            To complete the task, we first calculate the height of plane 0-1-2-3 at point
            P,Q. Fortunately, this is just a 2-dimensional problem. The plane height at 
            point P  can be calculate using line equation on plane 3-2-P. (plane 1-2-Q for 
            point Q)
            The next job is to calculate the plane height at point R. Again this is an easy
            job. Because R is always in the middle of P and Q, the plane height at R can be 
            calculated by using this formula:
             
                             zR = (zP + zQ) / 2
                             
            Note that point P, Q, R in 3-D space are ON the plane 0-1-2-3
            The final job is to calculate the plane height at point c. This the same
            with previous job:
            
                             zR = (z2 + zc) / 2
                             
                             zc = 2 * zR - z2
                            
        
             --plane xy--
             
                        |      |
             -----------0------3--------------------
                        |      |
                        |      |                             c : center of projection
             -----------1------2---------Q----------
                        |      |         |
                        |      |    R    |
                        |      |         |
                        |      P---------c
                        |      |
                        |      |
                        
        
             thus, the Java instructions for priority test might look like this: 
                
                float zP,zQ,zR;
          
                zP = (values1[2].z-values1[3].z)*(cop.x-values1[3].x)/
                     (values1[2].x-values1[3].x)+values1[3].z;
                zQ = (values1[2].z-values1[1].z)*(cop.y-values1[1].y)/
                     (values1[2].y-values1[1].y)+values1[1].z;
                zR = (zP+zQ)/2;
             
                // upper_first = 2 * zR - z2 > cop.z;
                upper_first = zP + zQ - z2 > cop.z;

               
             But, using new variables zP, zQ, and zR is not a good idea.
             It is better to calculate zR in a single instruction to speed up
             the calculation, even only by a litte. We are in hurry !   :)                        
        */
                
        if (values1[1].x == values1[2].x) {
          upper_first = (values1[2].z-values1[3].z) * (cop.x-values1[3].x) /
                        (values1[2].x-values1[3].x) + values1[3].z +
                        (values1[2].z-values1[1].z) * (cop.y-values1[1].y) /
                        (values1[2].y-values1[1].y) + values1[1].z - values1[2].z > cop.z;
        }
        else {
          upper_first = (values1[2].z-values1[1].z) * (cop.x-values1[1].x) /
                        (values1[2].x-values1[1].x) + values1[1].z +
                        (values1[2].z-values1[3].z) * (cop.y-values1[3].y) /
                        (values1[2].y-values1[3].y) + values1[3].z - values1[2].z > cop.z;
        }
      }     

      if (lowercount < 3) {
        if (upper_first) {
          color = 0.7f;
          plotPlane(upperpart,uppercount);
          color = 0.2f;
          plotPlane(values1,4);
        }
        else {
          color = 0.2f;
          plotPlane(values1,4);
          color = 0.7f;
          plotPlane(upperpart,uppercount);
        }
      }
      else
      if (uppercount < 3) {
        if (upper_first) {
          color = 0.2f;
          plotPlane(values1,4);
          color = 0.7f;
          plotPlane(lowerpart,lowercount);
        }
        else {
          color = 0.7f;
          plotPlane(lowerpart,lowercount);
          color = 0.2f;
          plotPlane(values1,4);
        }
      }
      else {
        if (upper_first) {
          color = 0.7f;
          plotPlane(upperpart,uppercount);   
          color = 0.2f;
          plotPlane(values1,4);
          color = 0.7f;
          plotPlane(lowerpart,lowercount);
        }
        else {
          color = 0.7f;
          plotPlane(lowerpart,lowercount);
          color = 0.2f;
          plotPlane(values1,4);
          color = 0.7f;
          plotPlane(upperpart,uppercount);   
        }
      } 
    }
  }
  
  /**
   * Determines whether a plane is plottable, i.e: does not have
   * invalid vertex.
   *
   * @return <code>true</code> if the plane is plottable, 
   *         <code>false</code> otherwise
   * @param values vertices array of the plane
   */

  private final static boolean plottable(SurfaceVertex[] values) {
    return (!values[0].isInvalid() &&
            !values[1].isInvalid() &&
            !values[2].isInvalid() &&            
            !values[3].isInvalid()); 
  }

  private final SurfaceVertex values1[] = new SurfaceVertex[4];                  
  private final SurfaceVertex values2[] = new SurfaceVertex[4];                  
  
  /**
   * Plots an area of group of planes
   *
   * @param start_lx start index in x direction
   * @param start_ly start index in y direction
   * @param end_lx   end index in x direction
   * @param end_ly   end index in y direction
   * @param sx       step in x direction
   * @param sy       step in y direction
   */

  private final void plotArea(int start_lx, int start_ly,
                               int end_lx, int end_ly,
                               int sx, int sy) {

    start_lx *= calc_divisions + 1;
    sx *= calc_divisions + 1;
    end_lx *= calc_divisions + 1;

    int lx = start_lx;
    int ly = start_ly;

    while (ly != end_ly) {
      if (plotfunc1) {
        values1[1] = vertex[0][lx+ly];
        values1[2] = vertex[0][lx+ly+sy];
      }
      if (plotfunc2) {
        values2[1] = vertex[1][lx+ly];  
        values2[2] = vertex[1][lx+ly+sy];
      }
        
      while (lx != end_lx) {
        thread.yield();
        if (plotfunc1) {
          values1[0] = values1[1];
          values1[1] = vertex[0][lx+sx+ly];
          values1[3] = values1[2];
          values1[2] = vertex[0][lx+sx+ly+sy];
        }
        if (plotfunc2) {
          values2[0] = values2[1];
          values2[1] = vertex[1][lx+sx+ly];
          values2[3] = values2[2];
          values2[2] = vertex[1][lx+sx+ly+sy];
        }
        if (!plotboth) {          
          if (plotfunc1) {
            if (plot_mode == DUALSHADE) color = 0.2f;
            if (plottable(values1)) plotPlane(values1,4);
          }
          else {
            if (plot_mode == DUALSHADE) color = 0.7f;
            if (plottable(values2)) plotPlane(values2,4);
          }
        }           
        else {
          if (plottable(values1)) {
            if (plottable(values2))
              splitPlotPlane(values1,values2);
            else
              plotPlane(values1,4);
          }
          else {
            if (plottable(values2)) plotPlane(values2,4);
          }
        }
        lx += sx; 
      }
      ly += sy; lx = start_lx;
    }
  }
    
  private final static Point[] testpoint = new Point[5];
  
  /**
   * Creates a surface plot
   */

  private final void plotSurface() {
    float    zi, zx;
    int      sx, sy;
    int      start_lx, end_lx;
    int      start_ly, end_ly;
    
    image_drawn = false; 
    try {
      zi = Float.valueOf(frame.getZMin()).floatValue();
      zx = Float.valueOf(frame.getZMax()).floatValue();
      if (zi >= zx) throw new NumberFormatException();
    }
    catch(NumberFormatException e) {
      frame.setMessage("Error in ranges"); 
      rotate = false;
      return;
    }

    int plot_density = frame.getDispDivisions(); 
    int multiple_factor = calc_divisions / plot_density;
    frame.setDispDivisions(plot_density);
     
    thread.yield();
    zmin = zi; zmax = zx; 
    if (rotate) frame.setMessage("rotating ..."); else
                frame.setMessage("regenerating ...");
    color_factor = 0.8f / (zmax-zmin);    
    if (plot_mode == DUALSHADE) color_factor *= 0.6f/0.8f;
    
    if (!printing) { 
      BufferGC.setColor(Color.lightGray);
      BufferGC.fillRect(0,0,bounds().width,bounds().height);
    }
    
    drawBoxGridsTicksLabels(BufferGC,false); 

    if (!plotfunc1 && !plotfunc2) {    
      if (isBoxed) drawBoundingBox();
      if (!rotate) frame.setMessage("completed"); 
      image_drawn = true;
      return; 
    }

    SurfaceVertex.setZRange(zmin,zmax);
    
    // direction test

    float distance = projector.getDistance() * projector.getCosElevationAngle();

    // cop : center of projection
    
    cop = new SurfaceVertex(distance * projector.getSinRotationAngle(), 
                            distance * projector.getCosRotationAngle(), 
                            projector.getDistance() * projector.getSinElevationAngle());
    cop.transform();
         
    boolean inc_x = cop.x > 0;
    boolean inc_y = cop.y > 0;
    
    critical = false;
     
    if (inc_x) {
      start_lx = 0; end_lx = calc_divisions; 
      sx = multiple_factor;
    }
    else {
      start_lx = calc_divisions; end_lx = 0; 
      sx = -multiple_factor; 
    }     
    if (inc_y) {
      start_ly = 0; end_ly = calc_divisions; 
      sy = multiple_factor; 
    }
    else {
      start_ly = calc_divisions; end_ly = 0; 
      sy = -multiple_factor; 
    }

    if ((cop.x > 10) || (cop.x < -10)) {
      if ((cop.y > 10) || (cop.y < -10)) {
        plotArea(start_lx,start_ly,end_lx,end_ly,sx,sy);
      }
      else {    // split in y direction
        int split_y = (int)((cop.y+10)*plot_density/20)*multiple_factor;               
        plotArea(start_lx,0,end_lx,split_y,sx,multiple_factor);
        plotArea(start_lx,calc_divisions,end_lx,split_y,sx,-multiple_factor);        
      }
    }
    else {
      if ((cop.y > 10) || (cop.y < -10)) {   // split in x direction
        int split_x = (int)((cop.x+10)*plot_density/20)*multiple_factor;               
        plotArea(0,start_ly,split_x,end_ly,multiple_factor,sy);
        plotArea(calc_divisions,start_ly,split_x,end_ly,-multiple_factor,sy);        
      }
      else {    // split in both x and y directions
        int split_x = (int)((cop.x+10)*plot_density/20)*multiple_factor;               
        int split_y = (int)((cop.y+10)*plot_density/20)*multiple_factor;               
        critical = true;
        plotArea(0,0,split_x,split_y,multiple_factor,multiple_factor);      
        plotArea(0,calc_divisions,split_x,split_y,multiple_factor,-multiple_factor);      
        plotArea(calc_divisions,0,split_x,split_y,-multiple_factor,multiple_factor);      
        plotArea(calc_divisions,calc_divisions,split_x,split_y,
                  -multiple_factor,-multiple_factor);      
      }
    }

    if (isBoxed) drawBoundingBox();
    if (!rotate) frame.setMessage("completed"); 
    image_drawn = true; 
  }
  
  private static int      contour_center_x  = 0;
  private static int      contour_center_y  = 0;
  private static int      contour_space_x   = 0;
  private static int      legend_width      = 0;
  private static int      legend_space      = 0;
  private static int      legend_length     = 0;
  private static String[] legend_label      = null;

  private static float    contour_width_x   = 0.0f;
  private static float    contour_width_y   = 0.0f;

  private static Color[]  contour_color     = null;
  private static String[] ylabels           = null;
  
  private static int[]    xpoints           = new int[8];
  private static int[]    ypoints           = new int[8];

  private static int[]    contour_x         = new int[8];
  private static int[]    contour_y         = new int[8];
  private static int      contour_n         = 0;
    
  private static int      contour_lines     = 10;
  private static float[]  delta             = new float[4];
  private static float[]  intersection      = new float[4];

  private static float contour_stepz;
  private static SurfaceVertex[] contour_vertex = new SurfaceVertex[4]; 

  private static LineAccumulator accumulator = new LineAccumulator();
  
  /**
   * Converts normalized x coordinate (-10..+10) to screen x coordinate 
   *
   * @return the screen x coordinate
   * @param x the normalized x coordinate
   */

  private static final int contourConvertX(float x) {
    return (int)Math.round(x * contour_width_x + contour_center_x);
  }
  
  /**
   * Converts normalized y coordinate (-10..+10) to screen y coordinate 
   *
   * @return the screen y coordinate
   * @param y the normalized x coordinate
   */

  private static final int contourConvertY(float y) {
    return (int)Math.round(-y * contour_width_y + contour_center_y);
  }

  /**
   * Creates bounding box for images of contour plot or density plot
   */ 

  private final void drawBoundingRect() {
    BufferGC.setColor(Color.black);
    int x1 = contourConvertX(-10);
    int y1 = contourConvertY(+10);
    int x2 = contourConvertX(+10);
    int y2 = contourConvertY(-10);        
    BufferGC.drawRect(x1,y1,x2-x1,y2-y1);

    if (isDisplayXY || isDisplayGrids) {    
      
      if (isDisplayXY) {
        x1 = contourConvertX(-10.5f);
        y1 = contourConvertY(+10.5f);
        x2 = contourConvertX(+10.5f);
        y2 = contourConvertY(-10.5f);        
        BufferGC.drawRect(x1,y1,x2-x1,y2-y1);
      }
      
      int xc,yc,labelindex = 0;
    
      for (int i=-10; i <= 10; i++) {
        if (!isDisplayGrids || (i % (t_y / 2) == 0) || isDisplayXY) {
          yc = contourConvertY(i);          
          if ((isDisplayGrids) && (i % t_y == 0)) { 
            BufferGC.drawLine(contourConvertX(-10.0f),yc,
                              contourConvertX(+10.0f),yc);
          }
          if (isDisplayXY) {
            if (i % t_y != 0) {
              BufferGC.drawLine(contourConvertX(+10.3f),yc,x2,yc);
              BufferGC.drawLine(contourConvertX(-10.3f),yc,x1,yc);
            }
            else {              
              BufferGC.drawLine(contourConvertX(+10.0f),yc,x2,yc);
              BufferGC.drawLine(contourConvertX(-10.0f),yc,x1,yc);
            }
          }
          if ((i % t_y == 0) && isDisplayXY) {
            outString(BufferGC,contourConvertX(10.7f),yc,
                      ylabels[labelindex++],Label.LEFT,CENTER);
          }
        }  
        if (!isDisplayGrids || (i % (t_x / 2) == 0) || isDisplayXY) {
          xc = contourConvertX(i);
          if ((isDisplayGrids) && (i % t_x == 0)) {
            BufferGC.drawLine(xc,contourConvertY(-10.0f),
                              xc,contourConvertY(+10.0f));
          }
          if (isDisplayXY) {
            if (i % t_x != 0) {
              BufferGC.drawLine(xc,contourConvertY(-10.3f),xc,y2);
              BufferGC.drawLine(xc,contourConvertY(+10.3f),xc,y1);
            }
            else {
              BufferGC.drawLine(xc,contourConvertY(-10.0f),xc,y2);
              BufferGC.drawLine(xc,contourConvertY(+10.0f),xc,y1);
            }
          }
          if ((i % t_x == 0) && isDisplayXY) {
            outFloat(BufferGC,xc,contourConvertY(-10.7f),
                     (float)((double)(i+10)/20*(xmax-xmin)+xmin),
                     Label.CENTER,TOP);
          }        
        }          
        if (isDisplayXY) {
          outString(BufferGC,(x1+x2)/2,
                    contourConvertY(-11.4f),"X",Label.CENTER,TOP);
          outString(BufferGC,contourConvertX(10.7f),
                    contourConvertY(-1.0f),"Y",Label.LEFT,CENTER);
        }
      }
    }
    
    if (isDisplayZ) {
      int lasty = y2, height = y2-y1;
      int divisions = contour_lines;

      x2 += contour_space_x;
      x1 = x2 - (legend_space + legend_width + legend_length);
      x2 -= legend_length;
      
      BufferGC.setColor(Color.black);
      outString(BufferGC,x2,y2,legend_label[0],Label.LEFT,CENTER);
           
      for (int i=1; i <= divisions+1; i++) {
        int y = y2 - (int)(i*height/(divisions+1));
        BufferGC.setColor(contour_color[i-1]);
        BufferGC.fillRect(x1,y,legend_width,lasty-y);
        BufferGC.setColor(Color.black);
        BufferGC.drawRect(x1,y,legend_width,lasty-y);
        outString(BufferGC,x2,y,legend_label[i],Label.LEFT,CENTER);
        lasty = y;
      }
    }
  }

  /**
   * Common method for contour plot and density plot that
   * cleans up memory used to hold temporary variables. 
   */

  private final void cleanUpMemory() {
    legend_label = null;
    contour_color = null;
    ylabels = null;
    accumulator.clearAccumulator();
  }
  
  /**
   * Common method for contour plot and density plot that
   * computes the best plot area size and position.
   */
   
  private final void computePlotArea() {
    setAxesScale();
    contour_lines = frame.getContourLines();

    float ratio = projector.getYScaling() /
                  projector.getXScaling();
        
    float width = 0.0f;
    float height = 0.0f;
    
    if (printing) {
      width = printwidth;
      height = printheight;
    }
    else {
      width = bounds().width;
      height = bounds().height;
    }

    int fontsize = 0;
    
    if (width < height) fontsize = (int)(width / 48);
                   else fontsize = (int)(height / 48);
    BufferGC.setFont(new Font("Helvetica",Font.PLAIN,fontsize));

    FontMetrics fm = BufferGC.getFontMetrics();
    
    // Leaves boundary space
     
    width *= 0.9f;
    height *= 0.9f;
    
    int spacex = 0;
    int spacey = 0;
    
    if (isDisplayXY) {

      // Creates y labels bank
      
      int labelscount = 0, index = 0, maxwidth = 0;
      for (int i=-10; i < 10; i++) if (i % t_y == 0) labelscount++;
      ylabels = new String[labelscount];

      for (int i=-10; i < 10; i++) {
        if (i % t_y == 0) {
          ylabels[index] = 
          new String(Float.toString(
                    (float)((double)(i+10)/20*(ymax-ymin)+ymin)));
          int strwidth = fm.stringWidth(ylabels[index++]);
          if (strwidth > maxwidth) maxwidth = strwidth;
        }
      }

      spacex += maxwidth;
      spacey += fm.getMaxAscent();
    }
    
    if (isDisplayZ) {
      if (((plot_mode == WIREFRAME) || (plot_mode == NORENDER)) && contour)      
        isDisplayZ = false;
      else {
        legend_width = (int)(width * 0.05);
        spacex += legend_width * 2;
        legend_space = fontsize;
        
        // Create Z Labels
        
        int counts = contour_lines;
        legend_length = 0;
        counts += 2;
        
        legend_label = new String[counts];
        for (int i=0; i < counts; i++) {
          float label = (float)((double)(i)/(counts-1)*(zmax-zmin)+zmin);
          legend_label[i] = new String(Float.toString(label));
          int labelwidth = fm.stringWidth(legend_label[i]);
          if (labelwidth > legend_length) legend_length = labelwidth;
        }
        
        spacex += legend_length + legend_space;
      }
    }
    
    width -= spacex;
    height -= spacey;
    
    contour_width_x = width;
    contour_width_y = width * ratio;

    if (contour_width_y > height) {
      contour_width_y = height;
      contour_width_x = height / ratio;
    }
        
    float scaling_factor = 10.0f;
    if (isDisplayXY) scaling_factor = 10.7f; // + 1.4 / 2
     
    contour_width_x = contour_width_x / scaling_factor / 2;
    contour_width_y = contour_width_y / scaling_factor / 2;
        
    contour_center_x = 0;
    contour_center_y = 0;

    int x1 = contourConvertX(-scaling_factor);       
    int y1 = contourConvertY(+scaling_factor);       
    int x2 = contourConvertX(+scaling_factor) + spacex;       
    int y2 = contourConvertY(-scaling_factor) + spacey;
    
    // computes center
    
    contour_center_x = (bounds().width-(x1+x2))/2;       
    contour_center_y = (bounds().height-(y1+y2))/2;       

    contour_space_x = spacex;

    // Creates color bank
    
    contour_color = new Color[contour_lines+1];
    for (int i=0; i <= contour_lines; i++) {
      float level = (float)i / contour_lines * 0.8f;
      switch (plot_mode) {
        case DUALSHADE:
        case SPECTRUM:    level = 0.8f-level;
                          contour_color[i] = Color.getHSBColor(level,1.0f,1.0f); 
                          break;
        default:          contour_color[i] = Color.getHSBColor(0,0,level);
                          break; 
      }
    }
  }
   
  /**
   * Creates contour plot of a single area division. Called by 
   * <code>plotContour</code> method
   *
   * @see #plotContour
   */

  private final void createContour() {
    float z = zmin;
    boolean fill = true;
    
    int xmin = xpoints[0] = contourConvertX(contour_vertex[0].x); 
    int xmax = xpoints[4] = contourConvertX(contour_vertex[2].x); 

    ypoints[0] = contourConvertY(contour_vertex[0].y); 
    xpoints[2] = contourConvertX(contour_vertex[1].x); 
    ypoints[4] = contourConvertY(contour_vertex[2].y); 
    xpoints[6] = contourConvertX(contour_vertex[3].x); 

    ypoints[2] = ypoints[3] = contourConvertY(contour_vertex[1].y);        
    ypoints[6] = ypoints[7] = contourConvertY(contour_vertex[3].y);

    xpoints[1] = xpoints[3] = xpoints[5] = xpoints[7] = -1;
    
    for (int counter = 0; counter <= contour_lines+1; counter++) {    
      // Analyzes edges
      
      for (int edge = 0; edge < 4; edge++) {
        int index = (edge << 1) + 1;
        int nextedge = (edge+1) & 3;

        if (z > contour_vertex[edge].z) {
          xpoints[index-1] = -2;
          if (z > contour_vertex[nextedge].z) {
            xpoints[(index+1) & 7] = -2;
            xpoints[index] = -2;
          }
        }
        else
        if (z > contour_vertex[nextedge].z) xpoints[(index+1) & 7] = -2;
        
        if (xpoints[index] != -2) {                
          if (xpoints[index] != -1) {
            intersection[edge] += delta[edge];
            if ((index == 1) || (index == 5)) 
              ypoints[index] = contourConvertY(intersection[edge]);
            else 
              xpoints[index] = contourConvertX(intersection[edge]);
          }
          else {
            if ((z > contour_vertex[edge].z) || (z > contour_vertex[nextedge].z)) {
              switch (index) {
                case 1: delta[edge] = 
                        (contour_vertex[nextedge].y-contour_vertex[edge].y) * 
                         contour_stepz / 
                        (contour_vertex[nextedge].z-contour_vertex[edge].z);

                        intersection[edge] =                
                        (contour_vertex[nextedge].y * (z - contour_vertex[edge].z) +
                         contour_vertex[edge].y * (contour_vertex[nextedge].z - z)) /
                        (contour_vertex[nextedge].z - contour_vertex[edge].z);
                
                        xpoints[index] = xmin;
                        ypoints[index] = contourConvertY(intersection[edge]);
                        break;
                case 3: delta[edge] = 
                        (contour_vertex[nextedge].x-contour_vertex[edge].x) * 
                         contour_stepz / 
                        (contour_vertex[nextedge].z-contour_vertex[edge].z);

                        intersection[edge] =                
                        (contour_vertex[nextedge].x * (z - contour_vertex[edge].z) +
                         contour_vertex[edge].x * (contour_vertex[nextedge].z - z)) /
                        (contour_vertex[nextedge].z - contour_vertex[edge].z);

                        xpoints[index] = contourConvertX(intersection[edge]);
                        break;
                case 5: delta[edge] = 
                        (contour_vertex[edge].y-contour_vertex[nextedge].y) * 
                         contour_stepz / 
                        (contour_vertex[edge].z-contour_vertex[nextedge].z);

                        intersection[edge] =                
                        (contour_vertex[edge].y * (z - contour_vertex[nextedge].z) +
                         contour_vertex[nextedge].y * (contour_vertex[edge].z - z)) /
                        (contour_vertex[edge].z - contour_vertex[nextedge].z);
                
                        xpoints[index] = xmax;
                        ypoints[index] = contourConvertY(intersection[edge]);
                        break;
                case 7: delta[edge] = 
                        (contour_vertex[edge].x-contour_vertex[nextedge].x) * 
                         contour_stepz / 
                        (contour_vertex[edge].z-contour_vertex[nextedge].z);

                        intersection[edge] =                
                        (contour_vertex[edge].x * (z - contour_vertex[nextedge].z) +
                         contour_vertex[nextedge].x * (contour_vertex[edge].z - z)) /
                        (contour_vertex[edge].z - contour_vertex[nextedge].z);

                        xpoints[index] = contourConvertX(intersection[edge]);
                        break;
              }
            }
          }            
        }
      }
      
      // Creates polygon

      contour_n = 0;
      
      for (int index = 0; index < 8; index++) {
        if (xpoints[index] >= 0) {
          contour_x[contour_n] = xpoints[index];
          contour_y[contour_n] = ypoints[index];
          contour_n++;
        }
      }
      if ((plot_mode != WIREFRAME) &&
          (plot_mode != NORENDER)) {
        if (counter > contour_lines) {
          if (printing)
            BufferGC.setColor(Color.white);
          else
            BufferGC.setColor(Color.lightGray);
        }
        else
          BufferGC.setColor(contour_color[counter]);
        BufferGC.fillPolygon(contour_x,contour_y,contour_n);
      }
 
      // Creates contour lines
      
      if (isMesh || !fill) {
        int x = -1;
        int y = -1;
      
        for (int index = 1; index < 8; index += 2) {
          if (xpoints[index] >= 0) {
            if (x != -1) accumulator.addLine(x,y,xpoints[index],ypoints[index]);
            x = xpoints[index]; y = ypoints[index]; 
          }
        }      
        if ((xpoints[1] > 0) && (x != -1)) 
          accumulator.addLine(x,y,xpoints[1],ypoints[1]);
      }
           
      if (contour_n < 3) break;
      z += contour_stepz;
    }
  }                                  
                                  
  /**
   * Creates contour plot
   */

  private final void plotContour() {
    float zi, zx;
    
    image_drawn = false; 
    accumulator.clearAccumulator();
    
    try {
      zi = Float.valueOf(frame.getZMin()).floatValue();
      zx = Float.valueOf(frame.getZMax()).floatValue();
      if (zi >= zx) throw new NumberFormatException();
    }
    catch(NumberFormatException e) {
      frame.setMessage("Error in ranges"); 
      rotate = false;
      return;
    }

    zmin = zi; zmax = zx; 
    computePlotArea();
    
    int plot_density = frame.getDispDivisions(); 
    int multiple_factor = calc_divisions / plot_density;
    frame.setDispDivisions(plot_density);
     
    thread.yield();
    contour_stepz = (zx - zi) / (contour_lines+1);
    
    frame.setMessage("regenerating ...");
    
    if (!printing) { 
      BufferGC.setColor(Color.lightGray);
      BufferGC.fillRect(0,0,bounds().width,bounds().height);
    }
    
    if (plotfunc1 || plotfunc2) {
      int index = 0;
      int func = 0;
      if (!plotfunc1) func = 1;   // function 1 has higher priority 
      
      int delta = (calc_divisions+1)*multiple_factor;
      for (int i=0; i < calc_divisions; i += multiple_factor) {
        index = i*(calc_divisions+1);
        for (int j=0; j < calc_divisions; j += multiple_factor) {
          contour_vertex[0] = vertex[func][index];
          contour_vertex[1] = vertex[func][index+multiple_factor];
          contour_vertex[2] = vertex[func][index+delta+multiple_factor];
          contour_vertex[3] = vertex[func][index+delta];          
          createContour();
          index += multiple_factor;
        }
      }
    }

    // Contour lines    
    BufferGC.setColor(Color.black);    
    accumulator.drawAll(BufferGC);
    
    // Bounding rectangle    
    drawBoundingRect();
    
    frame.setMessage("completed"); 
    image_drawn = true; 
  }

  /**
   * Creates density plot
   */

  private final void plotDensity() {
    float zi,zx,z;
    
    image_drawn = false; 
    try {
      zi = Float.valueOf(frame.getZMin()).floatValue();
      zx = Float.valueOf(frame.getZMax()).floatValue();
      if (zi >= zx) throw new NumberFormatException();
    }
    catch(NumberFormatException e) {
      frame.setMessage("Error in ranges"); 
      rotate = false;
      return;
    }

    zmin = zi; zmax = zx; 
    computePlotArea();
    int plot_density = frame.getDispDivisions(); 
    int multiple_factor = calc_divisions / plot_density;
    frame.setDispDivisions(plot_density);
     
    thread.yield();
    frame.setMessage("regenerating ...");
    
    color_factor = 0.8f / (zx-zi);    
    if (plot_mode == DUALSHADE) color_factor *= 0.6f/0.8f;
    
    if (!printing) { 
      BufferGC.setColor(Color.lightGray);
      BufferGC.fillRect(0,0,bounds().width,bounds().height);
    }
    
    if (plotfunc1 || plotfunc2) {
      int index = 0;
      int func = 0;
      if (!plotfunc1) func = 1;   // function 1 has higher priority 
      
      int delta = (calc_divisions+1)*multiple_factor;
      for (int i=0; i < calc_divisions; i += multiple_factor) {
        index = i*(calc_divisions+1);
        for (int j=0; j < calc_divisions; j += multiple_factor) {
          contour_vertex[0] = vertex[func][index];
          contour_vertex[1] = vertex[func][index+multiple_factor];
          contour_vertex[2] = vertex[func][index+delta+multiple_factor];
          contour_vertex[3] = vertex[func][index+delta];          

          int x = contourConvertX(contour_vertex[1].x);
          int y = contourConvertY(contour_vertex[1].y);
          int w = contourConvertX(contour_vertex[3].x)-x;
          int h = contourConvertY(contour_vertex[3].y)-y;

          z = 0.0f; boolean error = false;
          for (int loop = 0; loop < 4; loop++) {
            if (Float.isNaN(contour_vertex[loop].z)) {
              error = true; break;
            }
            z += contour_vertex[loop].z;
          }
          if (error) {
            index += multiple_factor; continue;
          }
          
          z /= 4; line_color = Color.black;          

          switch (plot_mode) {
            case DUALSHADE:
            case SPECTRUM:    z = 0.8f-(z-zi)*color_factor;
                              BufferGC.setColor(Color.getHSBColor(z,1.0f,1.0f)); 
                              break;
            default:          z = (z-zi)*color_factor;
                              BufferGC.setColor(Color.getHSBColor(0,0,z));
                              if (z < 0.3f) line_color = new Color(0.6f,0.6f,0.6f);                    
                              break;
          }
          
          BufferGC.fillRect(x,y,w,h);
          if (isMesh) {
            BufferGC.setColor(line_color);
            BufferGC.drawRect(x,y,w,h);
          }
          index += multiple_factor;
        }
      }
    }

    // Bounding rectangle
    drawBoundingRect();
        
    frame.setMessage("completed"); 
    image_drawn = true; 
  }

  /**
   * Creates wireframe plot 
   */

  private final void plotWireframe() {
    int      i,j,k;
    int      plot_density,multiple_factor; 
    int      counter;    
    float    zi,zx;    
    float    z;
    float    lx = 0, ly = 0, lastz = 0;
    Point    lastproj = new Point(0,0);
    boolean  error,lasterror,invalid;
      
    image_drawn = false; projection = new Point(0,0); 
    try {
      zi = Float.valueOf(frame.getZMin()).floatValue();
      zx = Float.valueOf(frame.getZMax()).floatValue();
      if (zi >= zx) throw new NumberFormatException();
    }
    catch(NumberFormatException e) {
      frame.setMessage("Error in ranges"); 
      rotate = false;
      return;
    }

    plot_density = frame.getDispDivisions();
    multiple_factor = calc_divisions / plot_density;
    frame.setDispDivisions(plot_density);

    zmin = zi; zmax = zx; 
    if (rotate) frame.setMessage("rotating ..."); else
                frame.setMessage("regenerating ...");

    if (!printing) { 
      BufferGC.setColor(Color.lightGray);
      BufferGC.fillRect(0,0,bounds().width,bounds().height);
    }
    
    thread.yield();
    drawBoxGridsTicksLabels(BufferGC,false);  
    BufferGC.setColor(Color.black);
 
    SurfaceVertex.setZRange(zmin,zmax);

    for (int func = 0; func < 2; func++) {

      if ((func == 0) && !plotfunc1) continue;
      if ((func == 1) && !plotfunc2) continue;
      
      i = 0; j = 0; k = 0; counter = 0;
      
      // plot - x direction

      while (i <= calc_divisions) {
        lasterror = true;
        if (counter == 0) {
          while (j <= calc_divisions) {
            thread.yield();
            z = vertex[func][k].z;
            invalid = Float.isNaN(z);
            if (!invalid) {
              if (z < zmin) {
                error = true;
                float ratio = (zmin-lastz)/(z-lastz);
                projection = projector.project(ratio*(vertex[func][k].x-lx)+lx,
                                               ratio*(vertex[func][k].y-ly)+ly,-10);         
              }
              else
              if (z > zmax) {
                error = true;
                float ratio = (zmax-lastz)/(z-lastz);
                projection = projector.project(ratio*(vertex[func][k].x-lx)+lx,
                                               ratio*(vertex[func][k].y-ly)+ly,10);
              }
              else {
                error = false;
                projection = vertex[func][k].projection();
              }
              if (lasterror && (!error) && (j != 0)) {
                if (lastz > zmax) {
                  float ratio = (zmax-z)/(lastz-z);
                  lastproj = projector.project(
                             ratio*(lx-vertex[func][k].x)+vertex[func][k].x,
                             ratio*(ly-vertex[func][k].y)+vertex[func][k].y,10);
                }
                else
                if (lastz < zmin) {
                  float ratio = (zmin-z)/(lastz-z);
                  lastproj = projector.project(
                             ratio*(lx-vertex[func][k].x)+vertex[func][k].x,
                             ratio*(ly-vertex[func][k].y)+vertex[func][k].y,-10);         
                }
              } else invalid = error && lasterror;
            } else error = true;
            if (!invalid && (j != 0)) {
              BufferGC.drawLine(lastproj.x, lastproj.y, projection.x, projection.y);
            } 
            lastproj = projection;
            lasterror = error;
            lx = vertex[func][k].x; 
            ly = vertex[func][k].y; 
            lastz = z;
            j++; k++;       
          }  
        }
        else k += calc_divisions+1;
        j = 0; i++;  
        counter = (counter + 1) % multiple_factor;
      }
    
      // plot - y direction
   
      i = 0; j = 0; k = 0; counter = 0; 

      while (j <= calc_divisions) {
        lasterror = true;
        if (counter == 0) {
          while (i <= calc_divisions) {
            thread.yield();
            z = vertex[func][k].z;
            invalid = Float.isNaN(z);
            if (!invalid) {
              if (z < zmin) {
                error = true;
                float ratio = (zmin-lastz)/(z-lastz);
                projection = projector.project(ratio*(vertex[func][k].x-lx)+lx,
                                               ratio*(vertex[func][k].y-ly)+ly,-10);         
              }
              else
              if (z > zmax) {
                error = true;
                float ratio = (zmax-lastz)/(z-lastz);
                projection = projector.project(ratio*(vertex[func][k].x-lx)+lx,
                                               ratio*(vertex[func][k].y-ly)+ly,10);
              }
              else {
                error = false;
                projection = vertex[func][k].projection();
              }
              if (lasterror && (!error) && (i != 0)) {
                if (lastz > zmax) {
                  float ratio = (zmax-z)/(lastz-z);
                  lastproj = projector.project(
                             ratio*(lx-vertex[func][k].x)+vertex[func][k].x,
                             ratio*(ly-vertex[func][k].y)+vertex[func][k].y,10);
                }
                else
                if (lastz < zmin) {
                  float ratio = (zmin-z)/(lastz-z);
                  lastproj = projector.project(
                             ratio*(lx-vertex[func][k].x)+vertex[func][k].x,
                             ratio*(ly-vertex[func][k].y)+vertex[func][k].y,-10);         
                }
              } else invalid = error && lasterror;
            } else error = true;
            if (!invalid && (i != 0)) {
              BufferGC.drawLine(lastproj.x, lastproj.y, projection.x, projection.y);
            } 
            lastproj = projection;
            lasterror = error;
            lx = vertex[func][k].x; 
            ly = vertex[func][k].y; 
            lastz = z;
            i++; k += calc_divisions+1;       
          }  
        }
        i = 0; k = ++j;
        counter = (counter + 1) % multiple_factor;
      }
    }
    if (isBoxed) drawBoundingBox();
    if (!rotate) frame.setMessage("completed"); 
    image_drawn = true; 
  }
}


