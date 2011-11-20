import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;

import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.surface.AbstractSurfaceModel;
import net.ericaro.surfaceplotter.surface.ArraySurfaceModel;
import net.ericaro.surfaceplotter.surface.SurfaceVertex;

public class SimpleRun {

	public void testSomething() {
		JSurfacePanel jsp = new JSurfacePanel();
		jsp.setTitleText("Hello");

		JFrame jf = new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(jsp, BorderLayout.CENTER);
		jf.pack();
		jf.setVisible(true);

		Random rand = new Random();
		int max = 10;
		float[][] z1 = new float[max][max];
		float[][] z2 = new float[max][max];
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max; j++) {
				z1[i][j] = rand.nextFloat() * 20 - 10f;
				z2[i][j] = rand.nextFloat() * 20 - 10f;
			}
		}
		ArraySurfaceModel sm = new ArraySurfaceModel();
		sm.setValues(0f,10f,0f,10f,max, z1, null);
		jsp.setModel(sm);
		// sm.doRotate();

		// canvas.doPrint();
		// sm.doCompute();
	}

	public static float f1(float x, float y) {
		// System.out.print('.');
		return (float) (Math.sin(x * x + y * y) / (x * x + y * y));
		// return (float)(10*x*x+5*y*y+8*x*y -5*x+3*y);
	}

	public static float f2(float x, float y) {
		return (float) (Math.sin(x * x - y * y) / (x * x + y * y));
		// return (float)(10*x*x+5*y*y+15*x*y-2*x-y);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new SimpleRun().testSomething();
			}
		});

	}

}
