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
import java.net.*;

import com.googlecode.surfaceplotter.browser.Tag;

/*
   BUG: There might be a problem if '\n' comes in the middle of
   a tag in <PRE> </PRE> block.
*/

public class Parser {
   /**
   * Create a parser object.
   * Opens and reads an URL.
   * @param fileURL URL to open.
   */
   public Parser(URL fileURL) {
      InputStream in;

      String filename = toLocalFile(fileURL);
      if (filename != null) {
         try {
            in = (InputStream)new FileInputStream(filename);
         }
         catch (Exception e) {
            more = false;
            return;
         }         
      }
      else {
         try {
            in = fileURL.openStream();
         }
         catch(IOException e) {
            more = false;
            return;
         }
      }
      DataInputStream input=new DataInputStream(in);

      size=0;
      byte [] buf = new byte[4096];

      int r;

      do {
         r=0;
         try {
            do {
               r=input.read(buf);
            } while(r==0);
         }
         catch(IOException e) {
            r=-1;
         }

         if (r>0) {
            byte [] buf2 = buffer;
            buffer = new byte[size+r];
            int i;
            for(i=0;i<size;i++)
               buffer[i]=buf2[i];
            for(i=0;i<r;i++)
               buffer[i+size]=buf[i];
            size+=r;
         }
      } while(r>=0);

      try {
         input.close();
      }
      catch(IOException e) {
      }

      next_pos=0;
      more=true;

      flagTokenIsTag=false;
      flagTokenIsSpecial=false;
      flagPreMode=false;
      flagSpaceBefore=false;
   }

  /**
   * Turn Preformatted mode ON/OFF.
   * If it is ON all whitespace characters are treated as tokens.
   * @param flag If true Preformatted mode is ON, if false - OFF.
   */

   public void setPreformatted(boolean flag) {
      flagPreMode=flag;
   }
 
  /**
   * Check if more tokens are available.
   * @return true if more tokens available, false otherwise.
   */
 
   public boolean moreTokens () {
      return(more&&next_pos<=size);
   }

  /**
   * Get the next token.
   * @return Token as a string.
   */
 
   public String getNextToken() {

      byte ch;
      int pos;

   // Assume that this is not a tag and not special
      flagTokenIsTag=false;
      flagTokenIsSpecial=false;

      while(next_pos<size) {
         
      // Get leading spaces
         pos=next_pos;
         next_pos=findFirstNonBlank(pos);

      // Return blanks if in PRE mode
         if (next_pos>pos) {
            if (flagPreMode)
               return(makeString(pos,next_pos));
            else
               flagSpaceBefore=true;
         }

      // get current character
         if (next_pos>=size) return null;
         ch=buffer[next_pos];

      // If in PRE mode check for '\n'  and  '\t'
         if (flagPreMode) {
            if (ch=='\n') {next_pos++; return("\n");}
            if (ch=='\t') {next_pos++; return("\t");}
            if (ch=='\r') {next_pos++; continue;}
         }

      // Check for special character: &xxx;
         if ((ch=='&') && 
             ((buffer[next_pos+1]!='\n') &&
              (buffer[next_pos+1]!='\r') &&
              (buffer[next_pos+1]!='\t') &&
              (buffer[next_pos+1]!=' '))) {
            flagTokenIsSpecial=true;
            pos=next_pos+1;
            next_pos=findEndSpecial(pos);
            return(makeSpecial(makeString(pos,next_pos-1)));
         }
      // Check for a tag or a comment
         else if (ch=='<') {
            if (buffer[next_pos+1]=='!'&&
                buffer[next_pos+2]=='-'&&
                buffer[next_pos+3]=='-') {
               next_pos=findEndComment(next_pos+4);
            }
            else {
               flagTokenIsTag=true;
               pos=next_pos+1;
               next_pos=findEndTag(pos);
               return(makeString(pos,next_pos-1));
            }
         }
      // This is a normal word
         else {
            pos=next_pos;
            next_pos=findEndWord(pos+1);
            return(makeString(pos,next_pos));
         }
      }

      more=false;
      return(null);

   }

   String makeString(int p1, int p2) {
      return(new String(buffer,0,p1,p2-p1));
   }

   String makeSpecial(String s) {
      char[] buf = new char[1];
      s=s.toUpperCase();
           if (s.equals("NBSP")) s=" ";
      else if (s.equals("LT")) s="<";
      else if (s.equals("GT")) s=">";
      else if (s.equals("AMP")) s="&";
      else if (s.equals("QUOT")) s="\"";
      else if (s.equals("COPY")) {
         buf[0]=169;
         s = new String(buf);
      }
      else if (s.equals("REG")) {
         buf[0]=174;
         s = new String(buf);       
      }
      else if (s.charAt(0)=='#') {
         buf[0]=(char)Tag.decStrToInt(s.substring(1));
         s = new String(buf);
      }
      else s=" ";
      return (s);
   }

   int findFirstNonBlank(int pos) {
      int p=pos;
   // In PRE mode only spaces are considered
      if (flagPreMode) {
         while(p<size) {
            byte c=buffer[p];
            if (c!=' ') break;
            p++;
         }
      }
   // In normal mode: space, newline, tab
      else {
         while(p<size) {
            byte c=buffer[p];
            if (c!=' '&&c!='\n'&&c!='\r'&&c!='\t') break;
            p++;
         }
      }
      return p;
   }

   int findEndWord(int pos) {
      int p=pos;
      while(p<size) {
         byte c=buffer[p];
         if (c==' '||c=='\n'||c=='\t'||c=='\r'||
             c=='&'||c=='<') break;
         p++;
      }
      return p;
   }

   int findEndComment(int pos) {
      int p=pos;
      while(p<size-2) {
         if (buffer[p]  =='-'&&
             buffer[p+1]=='-'&&
             buffer[p+2]=='>') {
            p+=3;
            break;
         }
         p++;
      }
      return p;
   }

   int findEndTag(int pos) {
      int p=pos;
      while(p<size) {
         if (buffer[p]=='>') {
            p++;
            break;
         }
         p++;
      }
      return p;
   }

   int findEndSpecial(int pos) {
      int p=pos;
      while(p<size) {
         if (buffer[p]==';') {
            p++;
            break;
         }
         p++;
      }
      return p;
   }

  /*
   * Added by Yanto Suryono
   * Date: 6 May 1997
   */
   
   static String toLocalFile(URL url) {
      if (url.toString().startsWith("file:")) {   
         String filename = url.toString();
      // Strips off the "file:"
         filename = filename.substring(5);    
         if (filename.charAt(2) == '|') {
           filename = filename.substring(1);
           filename = filename.replace('|',':');
         }
         filename = filename.replace('/',File.separatorChar);

         int index = filename.indexOf('#');
         if (index != -1) filename = filename.substring(0,index);
         return filename;
      }
      else return null;
   }

// private FileInputStream TokenStream;
   private boolean flagPreMode, more;
   private int next_pos, size;
   private byte[] buffer;

  /**
   * True if current token is a Tag
   */
 
   public  boolean   flagTokenIsTag;
 
  /**
   * True if current token is a special symbol (&amp;...;).
   * The special symbols are converted into normal characters
   * in the parser automatically.
   */
 
   public  boolean   flagTokenIsSpecial;

  /**
   * True if there were some spaces before current token.
   * This variable has to be cleared.
   */

   public  boolean   flagSpaceBefore;
}
