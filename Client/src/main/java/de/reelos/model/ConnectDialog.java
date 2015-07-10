package de.reelos.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ConnectDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2197414172073917956L;

	private String ip = "";
	private int port = 6260;
	private JTextField ipField;
	private JTextField portField;
	private JButton connectButton;

	public ConnectDialog(Window parent) {
		super(parent, "Verbinden...");
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		super.setModal(true);
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		{
			JPanel mainPane = new JPanel();
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
			mainPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			{
				JLabel ipLabel = new JLabel("IP-Adresse");
				ipField = new JTextField(ip, 20);
				ipField.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent arg0) {
					}

					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							connectButton.doClick();
						}
					}

					public void keyTyped(KeyEvent e) {
					}
				});
				mainPane.add(ipLabel);
				mainPane.add(ipField);
				mainPane.add(Box.createRigidArea(new Dimension(4, 5)));
				JLabel portLabel = new JLabel("Port");
				portField = new JTextField(String.valueOf(port), 20);
				mainPane.add(portLabel);
				mainPane.add(portField);
			}
			add(mainPane, BorderLayout.CENTER);

			JPanel buttonPane = new JPanel();
			{
				JButton close = new JButton("Abbrechen");
				close.addActionListener(a -> {
					port = -1;
					dispose();
				});
				buttonPane.add(close);
				connectButton = new JButton("Verbinden");
				connectButton.addActionListener(a -> {
					ip = ipField.getText();
					try {
						port = Integer.valueOf(portField.getText());
					} catch (NumberFormatException e) {
						System.err.println("Port is NaN");
					}
					dispose();
				});
				buttonPane.add(connectButton);
			}
			add(buttonPane, BorderLayout.SOUTH);
		}
		pack();
		setLocationRelativeTo(parent);
	}

	public JButton ConnectButton(){
		return connectButton;
	}
	
	public String getIP() {
		return ip;
	}
	
	public void setIP(String adress){
		ipField.setText(adress);
		this.ip = adress;
	}

	public int getPort() {
		return port;
	}
}
