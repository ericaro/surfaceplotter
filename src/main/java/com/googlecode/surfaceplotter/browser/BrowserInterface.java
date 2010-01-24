/*
    JavaBrowser - display an HTML file from within an application or an applet.

    Copyright (C) 1996  Alexey Goloshubin, Jeremy Cook

    @version 1.0   Released 20/12-1996

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Library General Public
    License as published by the Free Software Foundation; either
    version 2 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Library General Public License for more details.

    In this library we denote the Java package 'browser' to be the library
    under the GNU LGPL. Where the GNU LGPL refers to object files
    these should be understood to be files with the extension .class


    You should have received a copy of the GNU Library General Public
    License along with this library; if not, write to the Free
    Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

    A copy of the  GNU Library General Public License is also available 
    on the world-wide-web at ftp://prep.ai.mit.edu/pub/gnu/GNUinfo/LGPL

    The authors can be contacted:

    Jeremy Cook
       Jeremy.Cook@ii.uib.no   http://www.ii.uib.no/~jeremy

    Alexey Goloshubin
       s667@ii.uib.no     http://www.lstud.ii.uib.no/~s667 

    Modified by Yanto Suryono
       yanto@fedu.uec.ac.jp    http://home.ml.org/yanto
*/

package com.googlecode.surfaceplotter.browser;

import java.io.*;
import java.net.*;
import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

import com.googlecode.surfaceplotter.browser.FontInfo;
import com.googlecode.surfaceplotter.browser.Tag;



public class BrowserInterface {

  /**
   * Create a BrowserInterface object.
   * Initializes the browser data, appends a browser panel to a container
   * (Frame or Panel).
   * @param c Parent Container for the browser
   *
   */

  /*
   * Modified by Yanto Suryono
   * Date: 6 May 1997
   */
   
   public BrowserInterface(Container c) {
      cont=c;
      frame=findFrame(cont);
      frameName=frame.getTitle();
      fi = new FontInfo();

      menuMode=MENU_BOTH;
      menuHeight=100;
      r=null;

      Applet app=findApplet(c);

      if (app!=null) {
         URL u = app.getDocumentBase();
         try {
            curURL= new URL(u,"index.html");
         }
         catch(MalformedURLException e) {
            System.out.println(e.toString());
            return;
         }
      }
      else {
         File file=null;
         try {
            file = new File("index.html");
         }
         catch(NullPointerException e) {
            System.out.println(e.toString());
            return;
         }
         try {
            String str=file.getAbsolutePath();
            curURL= new URL("file:" + fileToURL(str));
         }
         catch(MalformedURLException e) {
            System.out.println(e.toString());
            return;
         }
      }

      hist = new Vector();
      histCurrent = 0;
      
      Panel panel = new Panel();
      panel.setLayout(new BorderLayout());
      
      panel1 = new BrowserPanel(this);
      panel2 = null;
      panel1.redoLayout(panel2);
      
      panel.add("Center", panel1);
      panel.add("South", status = new Label("Ready"));
      
      cont.add("Center",panel);
      setTitle(null);
   }

  /*
   * This method was modified by Yanto Suryono
   * Date: 6 May 1997
   */
   
   private String fileToURL(String src) {
      String fileurl = new String(src);
      fileurl = fileurl.replace(File.separatorChar,'/');
      
   // MS-DOS file url
      if (fileurl.indexOf(":/") == 1) {
        fileurl = "/" + fileurl.substring(0,1) +
                  "|/" + fileurl.substring(3);  
      }
      return(fileurl);
   }

   private Frame findFrame(Container c) {
      Object frame = c;
      while (! (frame instanceof Frame))
         frame = ((Component) frame).getParent();
      return((Frame)frame);
   }

   private Applet findApplet(Container c) {
      Object app = c;
      while (app!=null&& ! (app instanceof Applet))
         app = ((Component) app).getParent();
      return((Applet)app);
   }


  /**
   * Get current menu mode.
   * @return Menu mode (MENU_PANEL, MENU_INLINE or MENU_BOTH)
   */

   public int getMenuMode() {
      return(menuMode);
   }

  /**
   * Set menu mode.
   * The mode determines how the html data between
   * <TT>&lt;MENU&gt;</TT> and <TT>&lt;/MENU&gt;</TT> tags is treated.
   * Currently supoprted modes:
   * <UL>
   * <LI> MENU_PANEL - menu text is drawn in special menu panel
   * <LI> MENU_INLINE - menu text is treated as normal text
   * <LI> MENU_BOTH - menu text is drawn both in the document and in the menu panel
   * </UL>
   * @param mode Menu mode
   */

   public void setMenuMode(int mode) {
      if (mode>3) mode=3;
      if (mode<0) mode=0;
      menuMode=mode;
      URL_Process(true,true);
   }

  /**
   * Get height of the menu panel (in pixels).
   * @return Menu height
   */

   public int getMenuHeight() {
      return(menuHeight);
   }

  /**
   * Set height of the menu panel.
   * @param h Height of the menu panel
   */

   public void setMenuHeight(int h) {
      menuHeight=h;
      if (panel2!=null)
         URL_Process(true,true);
   }

  /**
   * Go back in the history list.
   * Load previous document.
   */

   public void goBack() {
      updateHist();
      if (hist.size()>1&&
          histCurrent>0) {
         histCurrent--;
         Hist h = (Hist)hist.elementAt(histCurrent);
         URL_Process(h.getURL(),h.getPos(),false);
      }
   }

  /**
   * Go forward in the history list.
   * Load next document.
   */

   public void goForward() {
      updateHist();
      if (hist.size()>1&&
          histCurrent<hist.size()-1) {
         histCurrent++;
         Hist h = (Hist)hist.elementAt(histCurrent);
         URL_Process(h.getURL(),h.getPos(),false);
      }
   }

  /**
   * Get filename of the current document
   * @return filename.
   */
 
   public String getFileName() {
      String str=curURL.getFile();
      int i=str.lastIndexOf("/");
      if (i<0) return ("no name");
      return (str.substring(i+1));
   }
 
  /**
   * Get URL of the current document
   * @return URL name.
   */
 
   public String getURLName() {
      return (curURL.toString());
   }

   void setCursor(int c) {
      frame.setCursor(c);
   }

   void setTitle(String str) {
      if (str==null) str="no title";
      frame.setTitle(frameName+" - ["+str+"]");
   }

   BrowserPanel getMenuPanel() {
      if (panel2==null) {
         panel2 = new BrowserPanel(this);
         panel2.validate();
         panel1.redoLayout(panel2);
      }
      return(panel2);
   }
   
   void removeMenuPanel() {
      if (panel2!=null) {
         panel2=null;
         panel1.redoLayout(panel2);
      }
   }

   void updateHist() {
      if (!hist.isEmpty()) {
         Hist h = (Hist)hist.elementAt(histCurrent);
         h.setPos(panel1.getScrollY());
      }
   }

   void addHist(URL url,int pos) {
      histCurrent++;
      Hist h = new Hist(url,pos);
      if (histCurrent>=hist.size()) {
         histCurrent=hist.size();
         hist.addElement(h);
      }
      else {
         hist.setElementAt(h,histCurrent);
         hist.setSize(histCurrent+1);
      }
   }

  /*
   * Modified by Yanto Suryono
   * Date: 6 May 1997
   */
   
   URL makeURL(String url_name) {
      URL u = null;
      try {
         u = new URL(curURL,url_name);
      }
      catch(MalformedURLException e) {}
      return u;
   }
   
  /*
   * Modified by Yanto Suryono
   * Date: 6 May 1997
   */

   URL makeURL(String url_name,String ref_name) {
      URL u = null;
      try {
         if (ref_name!=null)
            u = new URL(curURL,url_name+"#"+ref_name);
         else
            u = new URL(curURL,url_name);
      }
      catch(MalformedURLException e) {}
      return u;
   }

   boolean compURLs(URL u1, URL u2) {
      boolean flag=true;
      flag&=u1.getFile()==u2.getFile();
      flag&=u1.getHost()==u2.getHost();
      return(flag);
   }

/*
   This method is called ONLY WHEN
      1. Loading a new file
      2. Going to a target in the current file
         (sometimes).
      3. Reload current file (target==null)
      
      (add to hist)
*/

  /**
   * Load and display an HTML file.
   * @param url URL (as unparsed text) to open.
   * @param target Target inside the document (null if no target).
   */

   public void URL_Process(String url, String target) {
      URL u=curURL;
      if (url!=null) u=makeURL(url,target);
   // Same document
      if (compURLs(curURL,u)) {
         int y=-1;
         y=panel1.doc.getTargetY(u.getRef());
         URL_Process(u,y,true);  
      }
   // New document
      else {
         URL_Process(u,0,true);
      }
   }

  /**
   * Load and display an HTML file.
   * @param url URL to open.
   */

   public void URL_Process(URL url) {
   // Same document
      if (compURLs(curURL,url)) {
         int y=-1;
         y=panel1.doc.getTargetY(url.getRef());
         URL_Process(url,y,true);
      }
   // New document
      else {
         URL_Process(url,0,true);
      }
   }

  /**
   * Stop loading HTML file.
   */

   public void URL_Stop() {
      if (r!=null) { 
        if (r.isAlive()) r.stop(); 
        r=null;
        setStatus("Interrupted."); 
      }
   }
   
/*
   This method is called ONLY WHEN
      1. Dimensions of the Browser have been changed
*/

   void URL_Process(boolean w, boolean h) {
      do_URL_Process(panel1,curURL,panel1.getScrollY(),w,h);
   }

/*
   This method is called ONLY WHEN
      1. Going to a given pos in current document
      2. Going to a given pos in a new document

      (if add_hist then add to hist)
*/

   void URL_Process(URL url, int pos, boolean add_hist) {
      if (url==null) url=curURL;
      
      if (add_hist&&pos>=0) {
         updateHist();
         addHist(url,pos);
      }
      do_URL_Process(panel1,url,pos,false,false);
   }

   private void do_URL_Process(
         BrowserPanel p,      // Panel to load to
         URL url,             // URL to load
         int pos,             // Pos in the document
         boolean w_flag,      // Width change
         boolean h_flag) {    // Height change
           
   // Full reload (new file, width change, Reload button)
      if (!compURLs(curURL,url)||
          w_flag ||
          (!w_flag&&!h_flag&&pos<0)) {
         removeMenuPanel();
      // New file
         if (!compURLs(curURL,url)) {
            curURL=url;
            p.setScrolls(0,0);
         }

         URL_Stop();
         p.newDocument();
         p.canvas.translate(0,0);

         p.canvas.sx=p.getScrollX();
         if (pos>=0) p.canvas.sy=pos;
         else p.canvas.sy=p.getScrollY();
         r = new ReadFileThread(this,p,curURL,pos);
         r.start();
      }
      else {
     // Height change or goto target
        
        int x = p.getScrollX();
        int y = pos;

        if (h_flag&&y<0) y=p.getScrollY();

        p.setScrolls(x,y);
        p.checkScrolls(x,y);
        x = p.getScrollX();
        y = p.getScrollY();
        p.canvas.translate(x,y);
      }
   }

  /*
   * Added by yanto Suryono
   * Date: 6 May 1997
   */
   
   void setStatus(String statusmessage) {
     status.setText(statusmessage);
   }   

   private String frameName;
   private Label status;

   private Vector hist;
   private int histCurrent;
   
   private URL curURL;
   private int menuMode,menuHeight;

  /**
   * Font information.
   * Keeps information about all the fonts used by the browser.
   */
   public  FontInfo fi;
   private Container cont;
   private Frame frame;
   private BrowserPanel panel1, panel2;
   private ReadFileThread r;

  /**
   * Menu text is displayed in the menu panel.
   */
   public final static int MENU_PANEL=0;
  /**
   * Menu text is displayed in the document.
   */
   public final static int MENU_INLINE=1;
  /**
   * Menu text is displayed in the document and in the menu panel.
   */
   public final static int MENU_BOTH=2;

}

class BrowserPanel extends Panel {
   public BrowserPanel(BrowserInterface bi) {
      browser=bi;
      fi=browser.fi;

      prevWidth=prevHeight=0;

      setLayout(new BorderLayout());

   // Init scrollbars
      scroll2 = new Scrollbar(Scrollbar.VERTICAL);
      scroll1 = new Scrollbar(Scrollbar.HORIZONTAL);

   // Init canvas
      canvas = new BrowserCanvas(browser, this);

   // Put it all in
      redoLayout(null);

      newDocument();
   }

   public void newDocument() {
      doc = new Document(browser,this);
   }

   public void redoLayout(BrowserPanel p) {
      removeAll();

   // Add menu panel
      if (p!=null) add("North",p);
      
   // Add scrollbars
      Panel panel = new Panel();
      
      add("East", scroll2);      
      add("South", panel);
      
      panel.setLayout(new BorderLayout());
      panel.add("Center", scroll1);
      panel.add("East", new ScrollEdge(scroll2,scroll1));
      
   // Add canvas
      add("Center", canvas);
      
      validate();
   }

   public boolean handleEvent(Event evt) {
      if (evt.id==Event.SCROLL_ABSOLUTE  ||
          evt.id==Event.SCROLL_LINE_DOWN ||
          evt.id==Event.SCROLL_LINE_UP   ||
          evt.id==Event.SCROLL_PAGE_DOWN ||
          evt.id==Event.SCROLL_PAGE_UP) {
         
         canvas.translate(getScrollX(),getScrollY());
         return true;
      }
      return super.handleEvent(evt);
   }

   public int getScrollX() {
      return(scroll1.getValue());
   }

   public int getScrollY() {
      return(scroll2.getValue());
   }

   public void setScrolls(int x,int y) {
      if ((canvas.width < 0) || (canvas.height < 0)) return;
      
      if (doc!=null) {

      // This has to be changed!!!

         int h=doc.maxHeight+8;
         
         if (h <= canvas.height) 
           scroll2.disable(); 
         else {
           scroll2.enable();
           scroll2.setValues(y,canvas.height,0,h);
         }
         
         if (doc.maxWidth <= canvas.width) 
           scroll1.disable();
         else {
           scroll1.enable();
           scroll1.setValues(x,canvas.width,0,doc.maxWidth);
         }
      }
      else {
        scroll1.disable();
        scroll2.disable();
      }
      scroll1.setPageIncrement(canvas.width);
      scroll2.setPageIncrement(canvas.height);
      scroll1.setLineIncrement(14);
      scroll2.setLineIncrement(14);
   }

   public boolean checkScrolls(int x, int y) {
      boolean flag=true;
      int max_value;

      max_value=scroll1.getMaximum();//-scroll1.getVisible();
      if (x>max_value) {
         scroll1.setValue(max_value);
         flag=false;
      }
      max_value=scroll2.getMaximum();//-scroll2.getVisible();
      if (y>max_value) {
         scroll2.setValue(max_value);
         flag=false;
      }
      return flag;
   }

   private Scrollbar scroll1, scroll2;
   private BrowserInterface browser;

   public  FontInfo fi;
   public  BrowserCanvas canvas;
   public  Document doc;

   private int prevWidth, prevHeight;
}

class ScrollEdge extends Canvas {
   Scrollbar vscroll, hscroll;

   ScrollEdge(Scrollbar vertscroll, Scrollbar horzscroll) {
      super();
      vscroll = vertscroll;
      hscroll = horzscroll;
   }
   
   public Dimension preferredSize() {
      return new Dimension(vscroll.preferredSize().width,
                           hscroll.preferredSize().height);  
   }
}

class BrowserCanvas extends Canvas {
   public BrowserCanvas(BrowserInterface bi, BrowserPanel p) {
      browser=bi;
      panel=p;

   // More inits for the canvas
      setFont(browser.fi.getFont(FontInfo.NORMAL));
      width=height=0;
   }

   public synchronized Dimension minimumSize() {
      Dimension d=super.minimumSize();
      d.height=browser.getMenuHeight();
      return(d);
   }

   public synchronized Dimension preferredSize() {
      Dimension d=super.preferredSize();
      d.height=browser.getMenuHeight();
      return(d);
   }

   public void move(int x, int y) {
      dx=x; dy=y;
   }

   public void translate(int x, int y) {
      dx=sx=x; dy=sy=y;
      repaint();
   }

   public boolean mouseMove(Event evt,int x,int y) {
      Document doc=panel.doc;

      x+=dx; y+=dy;
      int info=Document.INFO_NONE;
      if (doc!=null) info=doc.getInfo(x,y);

      browser.setStatus("Document Done.");
   // Is link?
      if (info>=0) {
         browser.setCursor(Frame.HAND_CURSOR);
         browser.setStatus(doc.getLinkURL(info).toString());
      }
   // Is text?
      else if (info==Document.INFO_TEXT)
         browser.setCursor(Frame.DEFAULT_CURSOR);
   // Is picture?
      else if (info==Document.INFO_PIC)
         browser.setCursor(Frame.DEFAULT_CURSOR);
   // Default
      else
         browser.setCursor(Frame.DEFAULT_CURSOR);

      return true;
   }

   public boolean mouseExit(Event evt,int x,int y) {
      browser.setCursor(Frame.DEFAULT_CURSOR);
      return true;
   }

   public boolean mouseDown(Event evt,int mx,int my) {
      Document doc=panel.doc;
      mx+=dx-BrowserCanvas.MARGIN; my+=dy;
      int info=Document.INFO_NONE;
      if (doc!=null) info=doc.getInfo(mx,my);
      if (info>=0) {
         browser.URL_Process(doc.getLinkURL(info));
      }
      return true;
   }

   public void initGraphics(Graphics g) {
      g.clipRect(BrowserCanvas.MARGIN,1,width,height-1);
      g.translate(-dx+BrowserCanvas.MARGIN,-dy);
   }

   public void makeSize() {
      Dimension d = size();
      height=d.height-2;
      width=d.width-BrowserCanvas.MARGIN*2;     
   }

  /*
   * The dimension change detection was moved here by Yanto Suryono.
   * Date: 7 May 1997
   */
   
   public synchronized void paint(Graphics g) {
      makeSize();
      Dimension d = size();

      boolean w_flag=prevWidth!=d.width;
      boolean h_flag=prevHeight!=d.height;
      if (w_flag||h_flag) {
         prevWidth=d.width;
         prevHeight=d.height;
         browser.URL_Process(w_flag,h_flag);
         return;
      }

      g.setColor(Color.black);
      g.drawLine(0,0,d.width-1,0);
      g.drawLine(0,0,0,d.height-1);
      g.setColor(Color.white);
      g.drawLine(d.width-1,0,d.width-1,d.height-1);
      g.drawLine(0,d.height-1,d.width-1,d.height-1);
      initGraphics(g);
      if (panel.doc!=null) panel.doc.paint(g);
   }

   private BrowserInterface browser;
   private BrowserPanel panel;

   public int sx, sy, width, height;
   private int dx, dy;

   private int prevWidth = -1, prevHeight = -1;
   
   public final static int MARGIN=8;
}

class ReadFileThread extends Thread {
   public ReadFileThread(
         BrowserInterface bi,
         BrowserPanel p,
         URL u,
         int pos) {
      browser=bi;
      panel=p;
      url=u;
   }

   public void run() {
      BrowserCanvas canvas=panel.canvas;
      Document doc=panel.doc;

      int sx=canvas.sx, sy=canvas.sy;
      canvas.move(sx,sy);
      Graphics graph=canvas.getGraphics();
      if (graph != null) canvas.initGraphics(graph);

      browser.setStatus("Loading: " + url);
      pp = new com.googlecode.surfaceplotter.browser.Parser(url);

      if (pp.moreTokens()==false) {
         if (graph != null) graph.dispose();

         doc.processTag("H1");
         doc.addWord("Error");
         doc.processTag("/H1");
         doc.addWord("Unable");
         doc.addWord("to");
         doc.addWord("access");
         doc.processTag("FONT COLOR=#0000FF");
         doc.addWord(url.toString());
         doc.processTag("/FONT");
         canvas.repaint();
         browser.setStatus("Document Done.");
         return;
      }

      String token = pp.getNextToken();

      BrowserPanel mpanel=null;
      BrowserCanvas mcanvas=null;
      Document mdoc=null;
      Graphics mgraph=null;
      boolean mflag=false;

      boolean flagToMain=true;
      boolean flagToMenu=false;

      int m=browser.getMenuMode();
      if (m==BrowserInterface.MENU_PANEL) {
         flagToMain=false;
         flagToMenu=true;
      }
      else if (m==BrowserInterface.MENU_BOTH) {
         flagToMain=true;
         flagToMenu=true;
      }

   // Start processing the BODY of the document
      int count=0;
      while(pp.moreTokens()) {

         boolean flag=doc.flow.flagMenu;

         if (!flagToMain&&mflag)
            flag=mdoc.flow.flagMenu;

         if (flag) {
            if (!mflag&&flagToMenu) {

            // First time menu panel
               if (mpanel==null) {
                  mpanel=browser.getMenuPanel();
                  mcanvas=mpanel.canvas;
                  mgraph=mcanvas.getGraphics();
                  mcanvas.initGraphics(mgraph);
                  mcanvas.makeSize();
                  yield();
                  mpanel.newDocument();
                  mdoc=mpanel.doc;              
               }
               
               mdoc.flow.copy(doc.flow);
            }
            mflag=true;
         }
         else {
            if (mflag&&!flagToMain) {
               doc.flow.copy(mdoc.flow);
            }
            mflag=false;
         }

      // Send data to main window
         if (flagToMain||!mflag) {

            sx=panel.getScrollX();
            if (sx<canvas.sx) sx=canvas.sx;
            sy=panel.getScrollY();
            if (sy<canvas.sy) sy=canvas.sy;
            
            if (pp.flagTokenIsTag) {
               doc.processTag(token);
               pp.setPreformatted(doc.flow.flagNoWrap);
            }
            else {
               doc.addWord(token,pp.flagSpaceBefore);
            }
            doc.paintNew(graph);
            if (count>10) {
               panel.setScrolls(sx,sy);
               count=0;
            }
            else count++;
         }

      // Send data to menu window
         if (flagToMenu&&mflag) {

            if (pp.flagTokenIsTag) {
               mdoc.processTag(token);
               pp.setPreformatted(mdoc.flow.flagNoWrap);
            }
            else {
               mdoc.addWord(token,pp.flagSpaceBefore);
            }
            mdoc.paintNew(mgraph);
         }

         if (!pp.flagTokenIsTag)
            pp.flagSpaceBefore=false;


         token=pp.getNextToken();

         yield();
      }

      doc.flush(graph);
      graph.dispose();

      panel.setScrolls(sx,sy);
      if (!panel.checkScrolls(sx,sy)) {
         sx=panel.getScrollX();
         sy=panel.getScrollY();
         canvas.translate(sx,sy);
      }
      else canvas.repaint();

      if (mpanel!=null) {
         mdoc.flush(mgraph);
         mgraph.dispose();
         mpanel.setScrolls(
            mpanel.getScrollX(),
            mpanel.getScrollY());
         mcanvas.repaint();
      }
      
      browser.setStatus("Document Done.");
   }

   private URL url;
   private com.googlecode.surfaceplotter.browser.Parser pp;
   private BrowserPanel panel;
   private BrowserInterface browser;
}


class Document {
   public Document(BrowserInterface bi,BrowserPanel p) {
      browser=bi;
      panel=p;
      canvas=panel.canvas;
      flow = new Flow(browser,panel);
      firstLine=lastLine=null;
      prevLastLine=null;
      firstVisLine=lastVisLine=null;
      lineToPaint=null;
      curLine=addLine();
      maxHeight=0;
      maxWidth=0;
      title=null;

      targets = new Vector();
      links = new Vector();
   }

   public int getInfo(int ix, int iy) {
      int info=INFO_NONE;
      Line l=firstVisLine;
      while(l!=null&&l!=lastVisLine&&info==INFO_NONE) {
         if (iy>=l.getMinY()&&
             iy< l.getMaxY()) {
            info=l.getInfo(ix,iy);
         }
         l=l.getNext();
      }
      return info;
   }

   public int addLink(String full_url) {
      URL u = browser.makeURL(full_url);
      if (u!=null) links.addElement(u);
      return(links.size()-1);
   }

   public void addTarget(String name) {
      Target t = new Target(name,maxHeight);
      targets.addElement(t);
   }

   public URL getLinkURL(int i) {
      if (i<0||i>=links.size()) return null;
      return((URL)links.elementAt(i));
   }

   public int getTargetY(String name) {
      if (name==null) return -1;
      int i;
      for(i=0;i<targets.size();i++) {
         Target t = (Target)targets.elementAt(i);
         if (name.equals(t.getName()))
            return t.getPos();
      }
      return -1;
   }

   public void findVisLines(int top,int bot) {
      Line l=firstLine;
   // Find first visible line
      while(l!=null&&l.getMaxY()<top)
         l=l.getNext();
      firstVisLine=l;
   // Find last visible line
      while(l!=null&&l.getMinY()<bot)
         l=l.getNext();
      lastVisLine=l;
   }

   public void paint(Graphics g) {
      findVisLines(canvas.sy,canvas.sy+canvas.height);
      Line l=firstVisLine;
   // Draw all visible lines
      while(l!=lastVisLine&&l!=null) {
         l.paint(g,browser.fi);
         l=l.getNext();
      }
      lineToPaint=null;
   }

   public void paintNew(Graphics g) {
      if (lineToPaint!=null) {
         Line l=lineToPaint;
         while(l!=null&&l!=curLine) {
            l.paint(g,browser.fi);
            l=l.getNext();
         }
      }
      lineToPaint=null;
   }

   public void flush(Graphics g) {
      finishLine();
      paint(g);
   }

   public void addWord(String str) {
      addWord(str,true);
   }

   public void addWord(String str,boolean space_before) {

      if (str==null) return;

      if (flow.flagTitle) {
         if (title!=null) title+=" "+str;
         else title=str;
         return;
      }
      else {
         if (title!=null) {
            browser.setTitle(title);
            title=null;
         }
      }

      Font f = browser.fi.getFont(flow.fontType);
      FontMetrics fm = panel.canvas.getFontMetrics(f);

      if (curLine.isEmpty()) space_before=false;

      int len=fm.stringWidth(str);
      if (space_before) len+=fm.stringWidth(" ");

      int rest=flow.wrapRight-curLine.getMaxX();

   // Go to the next line first (if necessary)
      if (!flow.flagNoWrap&&
          !curLine.isEmpty()&&
          space_before&&
          len>rest) {
         breakLine();
         space_before=false;
      }
   // Only in PRE mode
      if (str.equals("\n")) {
         breakLines(0);
      }
      else {
         if (space_before) str=" "+str;
         len=fm.stringWidth(str);
         curLine.addWord(str,len,fm.getHeight());
      }
   }

   private Line addLine() {
      Line l = new Line(flow,flow.nextLineY);
      if (firstLine==null) {
         firstLine=l;
         lastLine=l;
         l.setNext(null);
      }
      else {
         lastLine.setNext(l);
         prevLastLine=lastLine;
         lastLine=l;
         l.setNext(null);
      }
      return(l);
   }

   private void removeLastLine() {
      if (firstLine==lastLine) {
         firstLine=null;
         lastLine=null;
         prevLastLine=null;
      }
      else {
         if (prevLastLine!=null) {
            lastLine=prevLastLine;
            prevLastLine=null;
         }
      }
   }

   public void breakLines(int n) {
      int old_h=maxHeight;
      finishLine();
      if (maxHeight>0) {
         Font f = browser.fi.getFont(flow.fontType);
         FontMetrics fm = panel.canvas.getFontMetrics(f);
         n=n*fm.getHeight();
      }
      else n=0;
   // maxHeight change - line has been added
      if (maxHeight!=old_h) 
         flow.nextLineY=maxHeight+n;
      else if (n>flow.nextLineY-maxHeight) {
         flow.nextLineY=maxHeight+n;
      }
      removeLastLine();
      curLine=addLine();
   }

   public void breakLine() {
      finishLine();
      flow.nextLineY=maxHeight;
      removeLastLine();
      curLine=addLine();
   }

   public void finishLine() {

      int align=flow.align;

      if (curLine.isEmpty()) {
         removeLastLine();
         curLine=addLine();
         return;
      }

      if (align==Flow.ALIGN_CENTER)
         curLine.alignCenter();
      else if (align==Flow.ALIGN_RIGHT)
         curLine.alignRight();

      if (curLine.getMaxX()>maxWidth) 
         maxWidth=curLine.getMaxX();
      if (curLine.getMaxY()>maxHeight) 
         maxHeight=curLine.getMaxY();

      if (curLine.getMaxY()>canvas.sy&&
          curLine.getMinY()<canvas.sy+canvas.height) {
         if (firstVisLine==null)
            firstVisLine=curLine;
         else
            lastVisLine=curLine;
         if (lineToPaint==null)
            lineToPaint=curLine;
      }

      curLine=addLine();
   }

   public void processTag(String token) {
      if (token==null) return;
      flow.processTag(new Tag(token));
   }

   private String title;

   public  Flow flow;
   private BrowserInterface browser;
   private BrowserPanel panel;
   private BrowserCanvas canvas;

   private Vector targets,links;

   public  Line curLine;

   private Line firstLine, lastLine;
   private Line prevLastLine;
   private Line firstVisLine, lastVisLine;
   private Line lineToPaint;

   public int maxWidth;
   public int maxHeight;

   public static final int 
      INFO_NONE = -3,
      INFO_PIC  = -2,
      INFO_TEXT = -1;
}

class Line implements ImageObserver {
   public Line(Flow f,int yy) {
      flow=f;
      x_start=flow.wrapLeft;
      y=yy;
      x_size=height=0;
      next=null;
      firstBox=lastBox=null;
      identBox=null;
      curBox=addBox();

      if (flow.ident>0) {
         identBox=curBox;
         identBox.setSpecial(
            Box.SPECIAL_NONE,
            flow.ident,
            flow.colorText);
         x_size=flow.ident;
         curBox=addBox();
      }
   }
   public int getInfo(int ix,int iy) {
      int info=Document.INFO_NONE;
      Box b=firstBox;
      int x=x_start;
      while(b!=null&&info==Document.INFO_NONE) {
         if (ix>=x&&ix<x+b.width)
            info=b.getInfo(ix,iy);
         x+=b.width;
         b=b.getNext();
      }
      return info;
   }
   public void paint(Graphics g,FontInfo fi) {
      Box b=firstBox;
      int x=x_start;
      while(b!=null) {
         b.paint(g,fi,x,y,height);
         x+=b.width;
         b=b.getNext();
      }
//    g.drawRect(x_start,y,x_size,height);
   }
   public int getMaxX() {
      return(x_start+x_size);
   }
   public int getMinY() {
      return(y);
   }
   public int getMaxY() {
      return(y+height);
   }
   public boolean isEmpty() {
      if (identBox!=null) {
         if (x_size-identBox.width<=0)
            return true;
      }
      return (x_size<=0);
   }

   public void addWord(String str,int len, int h) {
      x_size+=len;
      if (h>height) height=h;
      curBox.addWord(str,len);
   }

   private boolean imageError;
   private BrowserPanel  imagePanel;

   public void addImage(URL url,int w,int h,BrowserPanel p) {
      String name = com.googlecode.surfaceplotter.browser.Parser.toLocalFile(url);
      Image image = null;
      
      if (name != null) {
        image = Toolkit.getDefaultToolkit().getImage(name);
      }
      else
        image = Toolkit.getDefaultToolkit().getImage(url);
      imageError=false;
      imagePanel=p;
      
   // Problem somewhere in this loop

      while((w<=0||h<=0)&&!imageError) {
         if (w<=0) w=image.getWidth(this);
         if (h<=0) h=image.getHeight(this);
      }

      if (!imageError) {
         finishBox();
         x_size+=w;
         if (h>height) height=h;
         curBox.setImage(image,w,this);
         finishBox();
      }
   }

   public boolean imageUpdate(Image img,int info,
         int x,int y,int w,int h) {

      if ((info&ImageObserver.ABORT)!=0||
          (info&ImageObserver.ERROR)!=0) {
         imageError=true;
         return false;
      }

      if ((info&ImageObserver.ALLBITS)!=0) {
         BrowserCanvas canvas=imagePanel.canvas;
         Graphics g=canvas.getGraphics();
         canvas.initGraphics(g);
         paint(g,imagePanel.fi);
         g.dispose();

         return false;
      }

      return true;
   }

   private Box addBox() {
      Box b = new Box(flow.fontType,flow.colorText,flow.link);
      if (firstBox==null) {
         firstBox=b;
         lastBox=b;
         b.setNext(null);
      }
      else {
         lastBox.setNext(b);
         lastBox=b;
         b.setNext(null);
      }
      return(b);
   }

   public void finishBox() {
      curBox=addBox();
   }

   public void alignCenter() {
      int w=flow.wrapRight-flow.wrapLeft;
      x_start=flow.wrapLeft;
      x_start+=(w-x_size)/2;
   }

   public void alignRight() {
      x_start=flow.wrapRight-x_size;
   }

   public Line getNext() {
      return next;
   }

   public void setNext(Line l) {
      next=l;
   }

   private Flow flow;

   public int y, height;
   public int x_start, x_size;

   public Box curBox,identBox;

   private Box firstBox, lastBox;
   private Line next;
}

class Box {
// Constructor for text boxes
   public Box(int font,Color c,int link) {
      image=null;
      str=null;
      width=0;
      special=0;
      font_type=font;
      link_idx=link;
      color=c;
   }

// Convert a normal box to special
   public void setSpecial(int type,int size,Color c) {
      link_idx=-1;
      image=null;
      str=null;
      color=c;
      width=size;
      special=type;
   }

   public void setImage(Image img,int size,ImageObserver o) {
      special=0;
      str=null;
      width=size;
      image=img;
      observer=o;
   }

   public void addWord(String add_str,int size) {
      if (str==null) str=add_str;
      else str+=add_str;
      width+=size;
   }

   public int getInfo(int ix,int iy) {
      if (link_idx>=0) 
         return link_idx;
      if (special==0&&str!=null) 
         return Document.INFO_TEXT;
      return Document.INFO_NONE;
   }

   public void paint(Graphics g,FontInfo fi,int x,int y, int h) {
      if (image!=null) {
         y=y+h-image.getHeight(null);
   //    g.draw3DRect(x,y,width-1,image.getHeight(null)-1,false);
         g.drawImage(image,x,y,observer);
         if (link_idx>=0) {
            g.setColor(color);
            g.drawRect(x-1,y-1,width+2,h+2);
         }
      }
      else if (special > 0) {
         if (special==SPECIAL_HR) {
            g.setColor(color);
            int t=color.getRed();
            if (t<color.getGreen())
               t=color.getGreen();
            if (t<color.getBlue())
               t=color.getBlue();
            if (t<8)
               g.setColor(Color.white);
            g.draw3DRect(x,y,width,h,false);
         }
         else if (special==SPECIAL_SQUARE) {
            g.setColor(color);
            int size=8;
            int tx=x+width-size*3;
            int ty=y+h-size-2;
            g.fillRect(tx,ty,size,size);
         }
      }
      else if (str!=null) {
         Font f=fi.getFont(font_type);
         if (f!=g.getFont()) g.setFont(f);
         g.setColor(color);
         g.drawString(str,x,y+h);
         if (link_idx>=0)
            g.drawLine(x,y+h+1,x+width,y+h+1);
      }
   }

   public Box getNext() {
      return next;
   }

   public void setNext(Box b) {
      next=b;
   }

   public  int width;
   private int font_type;
   private int special;
   private Color color;
   private String str;
   private Image image;
   private ImageObserver observer;
   private int link_idx;

   private Box next;

   public static int 
      SPECIAL_NONE   = 1,
      SPECIAL_HR     = 2,
      SPECIAL_SQUARE = 3;
}


class Flow {
   public Flow(BrowserInterface bi, BrowserPanel p) {
      browser=bi;
      panel=p;

      width=panel.canvas.width;
      nextLineY=0;
      wrapLeft=0;
      wrapRight=width;
      alignParagraph=-1;
      alignAll=align=ALIGN_LEFT;
      flagNoWrap=false;
      flagMenu=false;
      flagTitle=false;
      link=-1;
      ident=0;
      fontType=FontInfo.makeFontIdx
         (FontInfo.NORMAL,FontInfo.PLAIN,2);

      font_stack=new int[200];
      color_stack=new Color[200];
      stack_size=0;

      colorBackground=Color.lightGray;
      colorText=Color.black;
      colorLink=Color.blue;
      panel.canvas.setBackground(colorBackground);
      panel.canvas.repaint();
   }

   public void copy(Flow f) {
      alignParagraph=f.alignParagraph;
      alignAll=f.alignAll;
      align=f.align;
      flagNoWrap=f.flagNoWrap;
      flagMenu=f.flagMenu;
      flagTitle=f.flagTitle;
      link=f.link;
      ident=f.ident;
      fontType=f.fontType;

      colorBackground=f.colorBackground;
      colorText=f.colorText;
      colorLink=f.colorLink;
      panel.canvas.setBackground(colorBackground);
      panel.canvas.repaint();
   }

   public void processTag(Tag tag) {
      BrowserCanvas canvas=panel.canvas;
      Document doc=panel.doc;
      
      String val=tag.getValue();
      String param;

      Line line=doc.curLine;

   // <BODY>
      if (val.equals("BODY")) {
         Color c=tag.getColorParam("BGCOLOR");
         if (c!=null) {
            colorBackground=c;
            canvas.setBackground(colorBackground);
            canvas.repaint();
         }
         c=tag.getColorParam("TEXT");
         if (c!=null) colorText=c;
         c=tag.getColorParam("LINK");
         if (c!=null) colorLink=c;
      }
   // <TITLE>
      else if (val.equals("TITLE")) {
         flagTitle=true;
      }
   // </TITLE>
      else if (val.equals("/TITLE")) {
         flagTitle=false;
      }
/*
   // <BASE>
      else if (val.equals("BASE")) {
         param=tag.getURLParam("HREF");
         if (param!=null) {
            browser.setBaseURL(param);
         }
      }
*/
   // <P>
      else if (val.equals("P")) {
         doc.breakLines(1);
         alignParagraph=-1;
         param=tag.getStringParam("ALIGN");
         if (param!=null) {
            if (param.equals("CENTER"))
               alignParagraph=ALIGN_CENTER;
            else if (param.equals("RIGHT"))
               alignParagraph=ALIGN_RIGHT;
            else
               alignParagraph=ALIGN_LEFT;
         }
      }
   // </P>
      else if (val.equals("/P")) {
         doc.breakLines(1);
         alignParagraph=-1;
      }
   // <CENTER>
      else if (val.equals("CENTER")) {
         doc.breakLine();
         alignAll=ALIGN_CENTER;
      }
   // </CENTER>
      else if (val.equals("/CENTER")) {
         doc.breakLine();
         alignAll=ALIGN_LEFT;
      }
   // <PRE>
      else if (val.equals("PRE")) {
         alignParagraph=ALIGN_LEFT;
         flagNoWrap=true;

         int font=FontInfo.setType(fontType,FontInfo.FIXED);
         pushFont(font);
         doc.breakLines(1);
      }
   // </PRE>
      else if (val.equals("/PRE")) {
         alignParagraph=-1;
         flagNoWrap=false;
         popFont();
         doc.breakLines(1);
      }

   // <IMG>
      else if (val.equals("IMG")) {
         doc.breakLines(1);
         line=doc.curLine;
         param=tag.getStringParam("SRC");
         if (param!=null) {
            int w=tag.getIntParam("WIDTH");
            int h=tag.getIntParam("HEIGHT");
            line.addImage(browser.makeURL(param),w,h,panel);
            line.finishBox();
         }
      }

   // <A>
      else if (val.equals("A")) {
         if (tag.isParam("HREF")) {
            param=tag.getStringParam("HREF");
            link=doc.addLink(param);
            pushFont(fontType,colorLink);
         }
         else {
            param=tag.getStringParam("NAME");
            if (param!=null) {
               doc.addTarget(param);
            }
            pushFont(fontType);
         }
         line.finishBox();
      }
   // </A>
      else if (val.equals("/A")) {
         popFont();
         link=-1;
         line.finishBox();
      }
   // <BR>
      else if (val.equals("BR")) {
         doc.breakLines(0); // 1 ???
      }
   // <HR>
      else if (val.equals("HR")) {
         doc.breakLines(1);

         Box b = doc.curLine.curBox;
         b.setSpecial(Box.SPECIAL_HR,
            wrapRight-wrapLeft,
            colorBackground);
         doc.curLine.x_size+=wrapRight-wrapLeft;
         int h=tag.getIntParam("SIZE");
         if (h<=2) h=2;
         doc.curLine.height=h;
         doc.breakLines(1);
      }
   // <UL>
      else if (val.equals("UL")||val.equals("OL")) {
         ident+=32;
         doc.breakLines(0);
      }
   // </UL>
      else if (val.equals("/UL")||val.equals("/OL")) {
         ident-=32;
         doc.breakLines(0);
      }
   // <MENU>
      else if (val.equals("MENU")) {
         flagMenu=true;
         ident+=32;
         doc.breakLines(0);
      }
   // </MENU>
      else if (val.equals("/MENU")) {
         flagMenu=false;
         ident-=32;
         doc.breakLines(0);
      }
   // <LI>
      else if (val.equals("LI")) {
         doc.breakLines(0);

         Box b=doc.curLine.identBox;
         if (b!=null)
            b.setSpecial(
               Box.SPECIAL_SQUARE,
               ident,colorText);
      }
   // <DL>
      else if (val.equals("DL")) {
         ident+=32;
         doc.breakLines(0);
      }
   // </DL>
      else if (val.equals("/DL")) {
         ident-=32;
         doc.breakLines(1);
      }
   // <DT>
      else if (val.equals("DT")) {
         ident-=32;
         doc.breakLines(0);
         ident+=32;
      }
   // <DD>
      else if (val.equals("DD")) {
         doc.breakLines(0);
      }
   // <B> or <STRONG>
      else if (val.equals("B")||val.equals("STRONG")) {
         int font=FontInfo.setStyle(fontType,FontInfo.BOLD);
         pushFont(font);
         line.finishBox();
      }
   // </B> or </STRONG>
      else if (val.equals("/B")||val.equals("/STRONG")) {
         popFont();
         line.finishBox();
      }
   // <I> or <CITE> or <EM>
      else if (val.equals("I")||val.equals("CITE")||val.equals("EM")) {
         int font=FontInfo.setStyle(fontType,FontInfo.ITALIC);
         pushFont(font);
         line.finishBox();
      }
   // </I> or </CITE> or </EM>
      else if (val.equals("/I")||val.equals("/CITE")||val.equals("/EM")) {
         popFont();
         line.finishBox();
      }
   // <TT> or <CODE>
      else if (val.equals("TT")||val.equals("CODE")) {
         int font=FontInfo.setType(fontType,FontInfo.FIXED);
         pushFont(font);
         line.finishBox();
      }
   // </TT> or </CODE>
      else if (val.equals("/TT")||val.equals("/CODE")) {
         popFont();
         line.finishBox();
      }

   // <FONT>
      else if (val.equals("FONT")) {
         Color color=tag.getColorParam("COLOR");
         int font=fontType;
         param=tag.getStringParam("SIZE");
         if (param!=null) {
            int size=FontInfo.getSize(fontType);
            char c=param.charAt(0);
            if (c=='+')      {size+=param.charAt(1)-'0';}
            else if (c=='-') {size-=param.charAt(1)-'0';}
            else if (c>='1'&&c<='7') {size=c-'1';}
            if (size>6) size=6;
            if (size<0) size=0;
            font=FontInfo.setSize(font,size);
         }

         if (color!=null) pushFont(font,color);
         else pushFont(font);
         line.finishBox();
      }

   // </FONT>
      else if (val.equals("/FONT")) {
         popFont();
         line.finishBox();
      }
   // <SMALL> or <BIG>
      else if (val.equals("SMALL")||val.equals("BIG")) {
         int size=FontInfo.getSize(fontType);
         if (val.equals("SMALL")&&size>0) size--;
         else if (val.equals("BIG")&&size<6) size++;
         pushFont(FontInfo.setSize(fontType,size));
         line.finishBox();
      }
   // </SMALL> or </BIG>
      else if (val.equals("/SMALL")||val.equals("/BIG")) {
         popFont();
         line.finishBox();
      }
   // <H?>
      else if (val.length()==2&&val.charAt(0)=='H') {
         char size=val.charAt(1);
         if (size>='1'&&size<='6') {

            int font = FontInfo.setSize(fontType,5-(size-'1'));
            font = FontInfo.setStyle(font,FontInfo.BOLD);
            pushFont(font);

            alignParagraph=-1;
            param=tag.getStringParam("ALIGN");
            if (param!=null) {
               if (param.equals("CENTER"))
                  alignParagraph=ALIGN_CENTER;
               else if (param.equals("RIGHT"))
                  alignParagraph=ALIGN_RIGHT;
               else
                  alignParagraph=ALIGN_LEFT;
            }

            doc.breakLines(0);
         }
      }
   // </H?>
      else if (val.length()==3&&val.charAt(0)=='/'&&val.charAt(1)=='H') {
         char ch=val.charAt(2);
         if (ch>='1'&&ch<='6') {
            popFont();
            alignParagraph=-1;
            doc.breakLines(1);
         }
      }

   // Make current alignment
      if (alignParagraph<0) align=alignAll;
      else align=alignParagraph;
   }

   void pushFont(int font) {
      font_stack[stack_size]=fontType;
      color_stack[stack_size]=null;
      stack_size++;
      fontType=font;
   }

   void pushFont(int font,Color c) {
      font_stack[stack_size]=fontType;
      color_stack[stack_size]=colorText;
      stack_size++;
      fontType=font;
      colorText=c;
   }
   void popFont() {
      if (stack_size>0) {
         stack_size--;
         fontType=font_stack[stack_size];
         if (color_stack[stack_size]!=null) {
            colorText=color_stack[stack_size];
            color_stack[stack_size]=null;
         }
      }
   }

   private int[]   font_stack;
   private Color[] color_stack;
   private int stack_size;
   
   private BrowserInterface browser;
   private BrowserPanel panel;
   private int width;
   public int alignParagraph;
   public int alignAll;

   public int ident;
   public int align;
   public int link;
   public int nextLineY;
   public int wrapLeft, wrapRight;
   public boolean flagNoWrap;
   public boolean flagMenu;
   public boolean flagTitle;
   public int fontType;
   public Color colorBackground;
   public Color colorText;
   public Color colorLink;

   public final static int
      ALIGN_LEFT=0,
      ALIGN_CENTER=1,
      ALIGN_RIGHT=2;
}


class Hist {
   public Hist(URL u, int y) {url=u;pos=y;}
   
   int getPos()       {return(pos);}
   URL getURL()       {return(url);}
   void setPos(int y) {pos=y;}

   private URL url;
   private int pos;
}

class Target {
   public Target(String n, int y) {name=n;pos=y;}
   
   int getPos()       {return(pos);}
   String getName()   {return(name);}
   void setPos(int y) {pos=y;}

   private String name;
   private int pos;
}

