package model;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.StrokeBorder;

public class ChatFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6385358475901470704L;
	private static final String version = "v0.001";

	private JTextArea chatFlow;
	private JTextField chatEnter;
	private JButton send;

	public ChatFrame() {
		super("Chat Client " + version);
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		{
			chatFlow = new JTextArea(20, 40);
			chatFlow.setBorder(new StrokeBorder(new BasicStroke(2)));
			contentPane.add(chatFlow, BorderLayout.CENTER);
		}
		{
			JPanel sendPane = new JPanel();
			{
				chatEnter = new JTextField(35);
				sendPane.add(chatEnter);
				send = new JButton("Senden");
				send.setMargin(new Insets(0, 0, 0, 0));
				sendPane.add(send);
			}
			contentPane.add(sendPane, BorderLayout.SOUTH);
		}
		pack();
		setLocationRelativeTo(null);
	}

	public void applyToChat(String text) {
		chatFlow.append(text+"\n");
	}

	public String getMessage() {
		return chatEnter.getText();
	}

	public void setMessage(String text){
		chatEnter.setText(text);
	}
	
	public JButton getSendButton() {
		return send;
	}
}
