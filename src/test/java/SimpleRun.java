import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;
import com.googlecode.surfaceplotter.JSurface;
import com.googlecode.surfaceplotter.AbstractSurfaceModel.Plotter;
import com.googlecode.surfaceplotter.SurfaceModel.PlotColor;
import com.googlecode.surfaceplotter.SurfaceModel.PlotType;


public class SimpleRun {
	
	
	public void testSomething() 
	{
		final AbstractSurfaceModel sm= new AbstractSurfaceModel();
		
		
		
		System.out.println("plotting");
		
		sm.setPlotFunction2(false);
		
		sm.setCalcDivisions(100);
		sm.setDispDivisions(100);
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
		
		JSurface canvas = new JSurface(sm);
		
		
		JFrame jf= new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(canvas);
		jf.pack();
		jf.setVisible(true);
		
		new Thread(new Runnable() {
				public void run()
				{
					Plotter p = sm.newPlotter(sm.getCalcDivisions());
					int im=p.getWidth();
					int jm=p.getHeight();
					for(int i=0;i<im;i++)
						for(int j=0;j<jm;j++)
						{
							float x,y;
							x=p.getX(i);
							y=p.getY(j);
							//System.out.println("setting value");
							p.setValue(i,j,f1(x,y),f2(x,y) );
							/** // force the fill to be slow to mimic a slow computation
							try{
								Thread.sleep(2);
							}catch(InterruptedException e) {System.out.println("interrupted");}
							/**/
						}
				}
			}).start();
		
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
