import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;
import com.googlecode.surfaceplotter.JSurface;
import com.googlecode.surfaceplotter.JSurfacePanel;
import com.googlecode.surfaceplotter.VerticalConfigurationPanel;
import com.googlecode.surfaceplotter.AbstractSurfaceModel.Plotter;
import com.googlecode.surfaceplotter.SurfaceModel.PlotColor;
import com.googlecode.surfaceplotter.SurfaceModel.PlotType;


public class SimpleRun {
	
	
	public void testSomething() 
	{
		final AbstractSurfaceModel sm= new AbstractSurfaceModel();
		
		
		System.out.println("plotting");
		
		sm.setPlotFunction2(false);
		
		sm.setCalcDivisions(200);
		sm.setDispDivisions(200);
		sm.setContourLines(10);
		
		sm.setXMin(-3);
		sm.setXMax(3);
		sm.setYMin(-3);
		sm.setYMax(3);
		
		sm.setBoxed(false);
		sm.setDisplayXY(false);
		sm.setExpectDelay(false);
		sm.setAutoScaleZ(true);
		sm.setDisplayZ(false);
		sm.setMesh(false);
		sm.setPlotType(PlotType.SURFACE);
		//sm.setPlotType(PlotType.WIREFRAME);
		//sm.setPlotType(PlotType.CONTOUR);
		//sm.setPlotType(PlotType.DENSITY);
		
		sm.setPlotColor(PlotColor.SPECTRUM);
		//sm.setPlotColor(PlotColor.DUALSHADE);
		//sm.setPlotColor(PlotColor.FOG);
		//sm.setPlotColor(PlotColor.OPAQUE);
		
		
		//( (AbstractSurfaceModel) sm).run();
		
		
//		
//		sm.setCalcDivisions(100);
//		sm.setDispDivisions(100);
//		sm.setContourLines(10);
		
		VerticalConfigurationPanel conf = new VerticalConfigurationPanel();
		conf.setModel(sm);
		JSurface canvas = new JSurface(sm);
		
		JFrame jf= new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new JSurfacePanel(), BorderLayout.CENTER);
		jf.pack();
		jf.setVisible(true);
		
		
		
		//sm.doRotate();
		
		//canvas.doPrint();
		//sm.doCompute();
	}
	
	public static float f1( float x, float y)
	{
		//		System.out.print('.');
		return (float)( Math.sin(x*x+y*y)/(x*x+y*y));
		//return (float)(10*x*x+5*y*y+8*x*y -5*x+3*y);
	}
	
	public static float f2( float x, float y)
	{
		return (float)(Math.sin(x*x-y*y)/(x*x+y*y));
		//return (float)(10*x*x+5*y*y+15*x*y-2*x-y);
	}
	
	
	
	
	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				new SimpleRun().testSomething() ;
			}
		});
		
	}
	
}
