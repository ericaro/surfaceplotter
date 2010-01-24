package com.googlecode.surfaceplotter;

import java.awt.Color;

/** Interface used by JSurface for every color. Warning, some color are not suitable for some drawing, be careful to sync it with the SurfaceModel
 * 
 * @author eric
 *
 */
public interface SurfaceColor {

	public abstract Color getBackgroundColor();

	public abstract Color getLineBoxColor();

	public abstract Color getBoxColor();

	public abstract Color getLineColor();

	public abstract Color getTextColor();

	public abstract Color getLineColor(int curve, float z);

	public abstract Color getPolygonColor(int curve, float z);

	public abstract Color getFirstPolygonColor(float z);

	public abstract Color getSecondPolygonColor(float z);

}