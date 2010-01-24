package com.googlecode.surfaceplotter;

import java.awt.Color;


/** Stands for a Color Model

 * @author Eric
 * @date jeudi 8 avril 2004 15:18:27
 */
public class ColorModel
{
	public static final byte DUALSHADE=0;
	public static final byte SPECTRUM=1;
	public static final byte FOG=2;
	public static final byte OPAQUE=3;
	
	
	float hue;
	float sat;
	float bright;
	float min; // hue|sat|bright of z=0
	float max; // Hue|sat|bright  of z=1
	byte mode=0;
	
	Color ocolor; // fixed color for opaque mode
	
	
	public ColorModel(byte mode,float hue,float sat, float bright,float min,float max)
	{
		this.mode=mode;
		this.hue=hue;
		this.sat=sat;
		this.bright= bright;
		this.min=min;
		this.max=max;
	}
	
	
	
	public Color getPolygonColor(float z)
	{
		if (z<0 || z>1) return Color.WHITE;
		switch(mode)
		{
		case DUALSHADE:
			{
				return Color.getHSBColor(hue,sat, norm(z));
			}
		case SPECTRUM:
			{
				return Color.getHSBColor(norm(1-z),sat, bright);
				//return Color.getHSBColor(norm(1-z),0.3f+z*(0.7f), bright);
			}
		case FOG:
			{
				return Color.getHSBColor(hue,norm(z), bright);
			}
		case OPAQUE:
			{
				if (ocolor==null) ocolor=Color.getHSBColor(hue,sat, bright);
				return ocolor;
			}
		}
		return Color.WHITE;//default
	}
	
	
	private float norm(float z)
	{
		if (min==max) return min;
		return min+z*(max-min);
	}
	
	
	/*
	case DUALSHADE:   z = (z-zi)*color_factor+dualshadeOffset;
					graphics.setColor();
					break;
				case SPECTRUM:    z = 0.8f-(z-zi)*color_factor;
					graphics.setColor(Color.getHSBColor(z,1.0f,1.0f)); 
					break;
				default:          z = (z-zi)*color_factor;
					graphics.setColor(Color.getHSBColor(0,0,z));
					if (z < 0.3f) line_color = new Color(0.6f,0.6f,0.6f);                    
					break;
					/**/
	

}//end of class
