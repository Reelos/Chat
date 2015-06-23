package test.java.model;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.StrokeBorder;

public class WhisperChatDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8976367636737918998L;

	private JTextArea chatArea;
	private JScrollPane chatScroll;
	private JTextField chatField;
	private JButton chatButton;

	private String toName;
	private BufferedWriter out;

	public WhisperChatDialog(String toName, Socket socket) {
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Whisperchat mit " + toName);
		setModal(false);
		this.toName = toName;
		try {
			this.out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
		} catch (IOException e) {
		}

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(5, 5));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		{
			JLabel namePlate = new JLabel("Chat mit " + toName);
			namePlate.setHorizontalAlignment(SwingConstants.CENTER);
			add(namePlate, BorderLayout.NORTH);
		}
		{
			chatArea = new JTextArea(10, 20);
			chatArea.setEditable(false);
			chatArea.setLineWrap(true);
			chatArea.setWrapStyleWord(true);
			chatScroll = new JScrollPane(chatArea,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			chatScroll.setBorder(new StrokeBorder(new BasicStroke(2)));
			add(chatScroll, BorderLayout.CENTER);
		}
		{
			JPanel chatPane = new JPanel();
			chatPane.setLayout(new BoxLayout(chatPane, BoxLayout.LINE_AXIS));
			chatField = new JTextField();
			chatButton = new JButton("Senden");
			chatButton.setMargin(new Insets(0, 0, 0, 0));
			chatButton.addActionListener(a -> {
				String message = chatField.getText();
				try {
					out.write("/to " + this.toName + " " + message);
					out.newLine();
					out.flush();
				} catch (Exception e) {
				}
				chatField.setText("");
			});
			chatField.addKeyListener(new KeyListener(){
				@Override
				public void keyPressed(KeyEvent arg0) {
				}
				@Override
				public void keyReleased(KeyEvent arg0) {
					if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
						chatButton.doClick();
				}
				@Override
				public void keyTyped(KeyEvent arg0) {
				}
				
			});
			chatPane.add(chatField);
			chatPane.add(chatButton);
			add(chatPane, BorderLayout.SOUTH);
		}
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	public void applyToChat(String text) {
		chatArea.append(text + "\n");
		chatScroll.getVerticalScrollBar().setValue(
				chatScroll.getVerticalScrollBar().getMaximum());
		if (!isActive()) {
			getToolkit().beep();
			toFront();
		}
	}
}
