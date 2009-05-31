package net.sf.openrocket.gui.main;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.ResizeLabel;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

public class AboutDialog extends JDialog {
	
	public static final String OPENROCKET_URL = "http://openrocket.sourceforge.net/";
	

	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = Prefs.getVersion();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new ResizeLabel("OpenRocket", 20), "ax 50%, wrap para");
		panel.add(new ResizeLabel("Version " + version, 3), "ax 50%, wrap 30lp");
		
		panel.add(new ResizeLabel("Copyright \u00A9 2007-2009 Sampo Niskanen"), "ax 50%, wrap para");
		
		JLabel link;
		
		if (Desktop.isDesktopSupported()) {
			
			link = new JLabel("<html><a href=\"" + OPENROCKET_URL + "\">" +
					OPENROCKET_URL + "</a>");
			link.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Desktop d = Desktop.getDesktop();
					try {
						d.browse(new URI(OPENROCKET_URL));
						
					} catch (URISyntaxException e1) {
						throw new RuntimeException("BUG: Illegal OpenRocket URL: "+OPENROCKET_URL,
								e1);
					} catch (IOException e1) {
						System.err.println("Unable to launch browser:");
						e1.printStackTrace();
					}
				}
			});
			
		} else {
			link = new JLabel(OPENROCKET_URL);
		}
		panel.add(link, "ax 50%, wrap para");
		

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		panel.add(close, "right");
		
		this.add(panel);
		this.setTitle("OpenRocket " + version);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		GUIUtil.setDefaultButton(close);
		GUIUtil.installEscapeCloseOperation(this);
	}
	
	
}
