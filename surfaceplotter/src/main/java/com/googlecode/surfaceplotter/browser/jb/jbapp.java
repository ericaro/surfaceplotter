package com.googlecode.surfaceplotter.browser.jb;
/*
    JB - HTML browser using the browser package

    Copyright (C) 1996  Alexey Goloshubin, Jeremy Cook

	@version 1.0	Released 20/12-1996

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

    A copy of the  GNU General Public License is also available 
    on the world-wide-web at ftp://prep.ai.mit.edu/pub/gnu/GNUinfo/GPL

    The authors can be contacted:

     Jeremy Cook
	Jeremy.Cook@ii.uib.no	http://www.ii.uib.no/~jeremy

     Alexey Goloshubin
        s667@ii.uib.no		http://www.lstud.ii.uib.no/~s667 
*/

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextField;
import java.net.MalformedURLException;
import java.net.URL;

import com.googlecode.surfaceplotter.browser.BrowserInterface;



public class jbapp extends Applet
{
	public void init() {

	// Create a menu panel
		Panel p = new Panel();
		p.setLayout(new FlowLayout());

		Font f = new Font("Helvetica",Font.BOLD,14);

		Button b = new Button("Menu");
		b.setFont(f);
		p.add(b);
		
		b = new Button("Back");
		b.setFont(f);
		p.add(b);
		
		b = new Button("Forward");
		b.setFont(f);
		p.add(b);

		textURL = new TextField("",16);
		textURL.setFont(new Font("Courier",Font.PLAIN,14));
		p.add(textURL);

		b = new Button("Start");
		b.setFont(f);
		p.add(b);


		setLayout(new BorderLayout());
		add("North",p);
		
	// Put browser into this panel
		browser = new BrowserInterface(this);
		
	// Make start url name for the demo
		try {
			start_url=new URL(
				getDocumentBase(),
				"jbdoc.html");
		}
		catch(MalformedURLException e) {
		}
	}

// Do not know why this is needed for Netscape
	public void start() {
		repaint();
		super.start();
	}

	public boolean handleEvent(Event evt) {
		if (evt.id == Event.WINDOW_DESTROY) {
			//destroy();
		}
		return super.handleEvent(evt);
	}

	public boolean action(Event evt, Object arg) {
		if (arg.equals("Start")) {
			browser.URL_Process(start_url);
			textURL.setText(browser.getFileName());
		}
		else if (arg.equals("Back")) {
			browser.goBack();
			textURL.setText(browser.getFileName());
		}
		else if (arg.equals("Forward")) {
			browser.goForward();
			textURL.setText(browser.getFileName());
		}
		else if (arg.equals("Menu"))
			browser.setMenuMode((browser.getMenuMode()+1)%3);
		else return false;
		return true;
	}

	public boolean keyDown(Event evt, int key) {
		if (key==10) {
			browser.URL_Process(textURL.getText(),null);
			textURL.setText(browser.getFileName());
			return true;
		}
		return super.keyDown(evt,key);
	}

	public boolean mouseUp(Event evt, int mx, int my) {
		textURL.setText(browser.getFileName());
		return super.mouseUp(evt,mx,my);
	}

	private URL start_url;
	private TextField textURL;
	private BrowserInterface browser;
}


