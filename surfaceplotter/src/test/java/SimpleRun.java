import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.googlecode.surfaceplotter.AbstractSurfaceModel;
import com.googlecode.surfaceplotter.JSurface;
import com.googlecode.surfaceplotter.SurfaceModel;
import com.googlecode.surfaceplotter.AbstractSurfaceModel.Plotter;


public class SimpleRun {
	
	
	public void testSomething() 
	{
		final AbstractSurfaceModel sm= new AbstractSurfaceModel();
		
		
		
		System.out.println("plotting");
		
		sm.setPlotFunction2(true);
		
		sm.setCalcDivisions(50);
		sm.setDispDivisions(50);
		sm.setContourLines(10);
		
		sm.setXMin(-3);
		sm.setXMax(3);
		sm.setYMin(-3);
		sm.setYMax(3);
		
		sm.setBoxed(true);
		sm.setDisplayXY(true);
		sm.setExpectDelay(false);
		sm.setAutoScaleZ(true);
		sm.setDisplayZ(true);
		sm.setMesh(true);
		sm.setPlotType(sm.PLOT_TYPE_SURFACE);
		//sm.setPlotType(sm.PLOT_TYPE_WIREFRAME);
		//sm.setPlotType(sm.PLOT_TYPE_CONTOUR);
		//sm.setPlotType(sm.PLOT_TYPE_DENSITY);
		
		sm.setPlotColor(sm.PLOT_COLOR_SPECTRUM);
		//sm.setPlotColor(sm.PLOT_COLOR_DUALSHADE);
		//sm.setPlotColor(sm.PLOT_COLOR_FOG);
		//sm.setPlotColor(sm.PLOT_COLOR_OPAQUE);
		
		
		
		//( (AbstractSurfaceModel) sm).run();
		
		JSurface canvas = new JSurface(sm);
		//canvas.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		canvas.setBorder(BorderFactory.createLineBorder(Color.RED, 6)); //check if the paint is ok
		
		
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
					long lastUpdate=System.currentTimeMillis();
					for(int i=0;i<im;i++)
						for(int j=0;j<jm;j++)
						{
							float x,y;
							x=p.getX(i);
							y=p.getY(j);
							//System.out.println("setting value");
							p.setValue(i,j,f1(x,y),f2(x,y) );
							/**/ // force the fill to be slow to mimic a slow computation
							try{
								Thread.sleep(10);
							}catch(InterruptedException e) {System.out.println("interrupted");}
							/**/
							sm.refresh();
						}
					//System.out.println("Duration "+(System.currentTimeMillis()-lastUpdate));
					//( (AbstractSurfaceModel) sm).doUpdate();
					sm.refresh();
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
