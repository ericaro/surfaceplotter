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
*/

package com.googlecode.surfaceplotter.browser;

import java.awt.*;

public class FontInfo {
  /**
   * Create a FontInfo object.
   * Builds all fonts for the browser.
   */
 
   public FontInfo() {
      fonts = new Font[NUM];

   // Make all HTML fonts
      int i,j,k,idx;

      idx=0;
      for(k=0;k<NUM_TYPES;k++) {
         for(j=0;j<NUM_STYLES;j++) {
            for(i=0;i<NUM_SIZES;i++,idx++) {
               fonts[idx] = new Font(
                  types[k],
                  styles[j],
                  sizes[i*NUM_TYPES+k]);
            }
         }
      }
   }

  /**
   * Get Font from an index.
   * @param type Special font index.
   */
 
   public Font getFont(int type) {
      return(fonts[type]);
   }

  /**
   * Get font size from an index.
   * @param font Font index.
   * @return Size of the font (0-6).
   */
 
   public static int getSize(int font) {
      font%=NUM_SIZES;
      return(font);
   }
 
  /**
   * Get font style from an index.
   * @param font Font index.
   * @return Style of the font.
   */

   public static int getStyle(int font) {
      font/=NUM_SIZES;
      font%=NUM_STYLES;
      return(font);
   }

  /**
   * Get font type from an index.
   * @param font Font index.
   * @return Type of the font.
   */
 
   public static int getType(int font) {
      font/=NUM_SIZES*NUM_STYLES;
      font%=NUM_TYPES;
      return(font);
   }

  /**
   * Make font index.
   * @param type Font type.
   * @param style Font style.
   * @param size Font size.
   * @return Font index.
   */ 

   public static int makeFontIdx(int type,int style,int size) {
      return((type*NUM_STYLES+style)*NUM_SIZES+size);
   }

  /**
   * Set font type into font index
   * @param font Original font index.
   * @param type New font type.
   * @return New font index.
   */

   public static int setType(int font,int type) {
      if (type<0||type>=NUM_TYPES) return font;
      return(makeFontIdx(type,getStyle(font),getSize(font)));
   }

  /**
   * Set font style into font index
   * @param font Original font index.
   * @param style New font style.
   * @return New font index.
   */

   public static int setStyle(int font,int style) {
      if (style<0||style>=NUM_STYLES) return font;
      style|=getStyle(font);
      return(makeFontIdx(getType(font),style,getSize(font)));
   }

  /**
   * Set font size into font index
   * @param font Original font index.
   * @param size New font size.
   * @return New font index.
   */

   public static int setSize(int font,int size) {
      if (size<0||size>=NUM_SIZES) return font;
      return(makeFontIdx(getType(font),getStyle(font),size));
   }

   private Font[] fonts;

   static String types[]   ={
      "Helvetica",
      "Courier"};

   static int    styles[]  ={
      Font.PLAIN,
      Font.BOLD,
      Font.ITALIC,
      Font.BOLD|Font.ITALIC};

   static int    sizes[]   ={
      8,    6,
      12,   10,
      14,   12,
      18,   14,
      24,   18,
      28,   24,
      36,   28 };

  /**
   * Font type: Proportinal
   */
 
   public  final static int NORMAL    = 0;
 
  /**
   * Font type: Fixed width
   */ 
 
   public  final static int FIXED     = 1;

           final static int NUM_TYPES = 2;

  /**
   * Font style: Plain
   */
 
   public  final static int PLAIN = 0;
 
  /**
   * Font style: Bold
   */
 
   public  final static int BOLD =     1;
 
  /**
   * Font style: Italic
   */
 
   public  final static int ITALIC =   2;
 
  /**
   * Font style: Bold and Italic
   */
 
   public  final static int BOLD_ITALIC = 3;

           final static int NUM_STYLES  = 4;

           final static int NUM_SIZES   = 7;

           final static int NUM         = 56;

}
