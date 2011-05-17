import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;

import com.googlecode.surfaceplotter.JSurfacePanel;


public class SimpleRun {
	
	
	public void testSomething() 
	{
		JSurfacePanel jsp = new JSurfacePanel();
		jsp.setTitleText("Hello");
		
		JFrame jf= new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(jsp, BorderLayout.CENTER);
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
