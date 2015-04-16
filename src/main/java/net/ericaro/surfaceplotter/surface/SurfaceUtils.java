package net.ericaro.surfaceplotter.surface;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class SurfaceUtils {

  public static void doExportPNG(JSurface surface, File file) throws IOException {
    if (file == null)
      return;
    int h, w;
    w = 50;
    h = 30;
    java.awt.image.BufferedImage bf = new java.awt.image.BufferedImage(surface.getWidth(), surface.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = bf.createGraphics();

    g2d.setColor(java.awt.Color.white);
    g2d.fillRect(0,0,surface.getWidth() ,surface.getHeight());
    g2d.setColor(java.awt.Color.black);
    surface.export(g2d);
    // java.awt.image.BufferedImage bf2=bf.getSubimage(0,0,w,h);
    boolean b = javax.imageio.ImageIO.write(bf, "PNG", file);
  }

  /**
   * needs batik, will reintroduce it later
   * @throws ParserConfigurationException
   */
  public static void doExportSVG(JSurface surface, File file) throws IOException, ParserConfigurationException{
    if (file == null)
      return;

    // Create an instance of org.w3c.dom.Document
    org.w3c.dom.Document document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

    // Create an instance of the SVG Generator

    org.apache.batik.svggen.SVGGraphics2D svgGenerator = new org.apache.batik.svggen.SVGGraphics2D(document);

    // Ask the test to render into the SVG Graphics2D implementation
    surface.export(svgGenerator);

    // Finally, stream out SVG to the standard output using UTF-8 //
    // character to byte encoding
    boolean useCSS = true; // we want to use CSS		// style attribute
    java.io.Writer out = new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8");
    svgGenerator.stream(out, useCSS);
    out.close();
  }
}
