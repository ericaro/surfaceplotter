import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

import javax.swing.JPanel;

import com.googlecode.surfaceplotter.JSurfacePanel;



/**
 * @author User #1
 */
public class Sample extends JPanel {
	public Sample() {
		initComponents();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		surfacePanel1 = new JSurfacePanel();

		//======== this ========
		setLayout(new BorderLayout());

		//---- surfacePanel1 ----
		surfacePanel1.setTitleText("title");
		surfacePanel1.setBackground(Color.white);
		surfacePanel1.setTitleFont(surfacePanel1.getTitleFont().deriveFont(surfacePanel1.getTitleFont().getStyle() | Font.BOLD, surfacePanel1.getTitleFont().getSize() + 6f));
		surfacePanel1.setConfigurationVisible(false);
		add(surfacePanel1, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSurfacePanel surfacePanel1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
