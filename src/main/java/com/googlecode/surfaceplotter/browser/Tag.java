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

import java.io.*;
import java.awt.*;

public class Tag {
  /**
   * Create a Tag object from a string.
   * The string should contain an HTML tag.
   * @param tag_str The string with the tag.
   */

   public Tag(String tag_str) {
      str=tag_str;
      str.trim();
   }

  /**
   * Get name of the HTML tag.
   * @return Tag name.
   */

   public String getValue() {
      int i=0;
      while(i<str.length()) {
         char c=str.charAt(i);
         if (c==' '||c=='\t'||c=='\n'||c=='\r') break;
         i++;
      }
      String val = str.substring(0,i);
      val=val.toUpperCase();
      return(val);
   }

  /**
   * Check if the tag has the specified parameter
   * @param param Parameter name
   * @return true if such parameter exists, false otherwise.
   */
 
   public boolean isParam(String param) {
      param=param.toUpperCase();
      if (str.indexOf(param)>=0) return true;
      param=param.toLowerCase();
      if (str.indexOf(param)>=0) return true;
      return false;
   }

  /**
   * Read value of the parameter as URL name.
   * @param param Parameter name.
   * @return URL name or null.
   */
 
   public String getURLParam(String param) {
      String str = getStringParam(param,false);
      if (str==null) return null;

      int start=str.indexOf(":");
      if (start<0) start=0;
      else start++;

      int end=str.indexOf("#");
      if (end==start) return null;

      if (end==-1) end=str.length();
      return (str.substring(start,end));
   }
   
  /**
   * Read value of the parameter as a target name.
   * @param param Parameter name.
   * @return target name or null.
   */
 
   public String getTargetParam(String param) {
      String str = getStringParam(param,false);
      if (str==null) return null;

      int i=str.indexOf("#");
      if (i==-1) return null;
      return (str.substring(i+1));
   }

  /**
   * Read value of the parameter as Color.
   * @param param Parameter name.
   * @return Color.
   */
 
   public Color getColorParam(String param) {
      String str = getStringParam(param);
      if (str==null) return null;

      if (str.charAt(0)!='#') return null;

      int r=hexStrToInt(str.substring(1,3));
      int g=hexStrToInt(str.substring(3,5));
      int b=hexStrToInt(str.substring(5,7));

      if (r==-1||g==-1||b==-1) return null;

      return(new Color(r,g,b));
   }

  /**
   * Read value of the parameter as a decimal integer
   * @param param Parameter name.
   * @return The integer ot -1
   */
 
   public int getIntParam(String param) {
      String str = getStringParam(param);
      if (str==null) return -1;
      return(decStrToInt(str));
   }

  /**
   * Read value of the parameter as a string and convert it to upper case.
   * @param param Parameter name.
   * @return String value in upper case.
   */
 
   public String getStringParam(String param) {
      return(getStringParam(param,true));
   }
 
  /**
   * Read value of the parameter as a string
   * @param param Parameter name.
   * @param to_upper If true the resulting string is converted to upper case,
   * if false it is returned as is.
   * @return String value.
   */

   public String getStringParam(String param,boolean to_upper) {
      param=param.toUpperCase();
      int i = str.indexOf(param);
      if (i==-1) {
         param=param.toLowerCase();
         i=str.indexOf(param);
         if (i==-1) return null;
      }

   // Skip param name
      i+=param.length();
      boolean eq_flag=false;
      boolean quote_flag=false;

   // Find beginning of param value
      while(i<str.length()&&quote_flag==false) {
         char c=str.charAt(i);
         if (c!=' '&&c!='\t'&&c!='\n'&&c!='\r'&&
             c!='='&&c!='\"') break;

         if (c=='=')  eq_flag=true;
         if (c=='\"') quote_flag=true;

         i++;
      }

   // Value was not found
      if (i>=str.length()||eq_flag==false) return null;

      int start_pos=i;

   // If quote was found, read until next quote.
   // Otherwise read until space.
      if (quote_flag) {
         while(i<str.length()) {
            if (str.charAt(i)=='\"') break;
            i++;
         }
      }
      else {
         while(i<str.length()) {
            char c=str.charAt(i);
            if (c==' '||c=='\t'||c=='\n'||c=='\r')
               break;
            i++;
         }
      }

      if (i>str.length()) return null;

      String val=str.substring(start_pos,i);
      if (quote_flag==false&&to_upper) 
         val=val.toUpperCase();

      return(val);
   }
   
  /**
   * Converts a string to a non-negative integer.
   * @param str String containing a decimal number.
   * @return Non-negative integer value or -1 if the conversion is
   * not possible.
   */
 
   public static int decStrToInt(String str) {
      int i=str.length()-1,p=1,val=0;
      while(i>=0) {
         char c=str.charAt(i);
         if (c>='0'&&c<='9') {
            val+=(c-'0')*p;
         }
         else return -1;
         i--; p*=10;
      }
      return(val);
   }

  /**
   * Converts a string to a non-negative integer.
   * @param str String containing a hex number.
   * @return Non-negative integer value or -1 if the conversion is
   * not possible.
   */
 
   public static int hexStrToInt(String str) {
      int i=str.length()-1,p=1,val=0;
      while(i>=0) {
         char c=str.charAt(i);
         if (c>='0'&&c<='9') {
            val+=(c-'0')*p;
         }
         else if (c>='a'&&c<='f') {
            val+=((c-'a')+10)*p;
         }
         else if (c>='A'&&c<='F') {
            val+=((c-'A')+10)*p;
         }
         else return -1;
         i--; p*=16;
      }
      return(val);
   }


   private String str;
}
