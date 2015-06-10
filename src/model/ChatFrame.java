package model;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
	private JMenuItem connect;
	private JMenuItem disconnect;
	private JMenuItem changeName;

	public ChatFrame() {
		super("Chat Client " + version);
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);

		JMenuBar bar = new JMenuBar();
		{
			JMenu data = new JMenu("Datei");
			{
				connect = new JMenuItem("Verbinden");
				data.add(connect);
				disconnect = new JMenuItem("Trennen");
				disconnect.setEnabled(false);
				data.add(disconnect);
				JSeparator filler = new JSeparator();
				data.add(filler);
				JMenuItem close = new JMenuItem("Schließen");
				close.addActionListener(a -> this.dispose());
				data.add(close);
			}
			bar.add(data);

			JMenu option = new JMenu("Optionen");
			{
				changeName = new JMenuItem("Namen auswählen");
				option.add(changeName);
			}
			bar.add(option);
		}
		add(bar, BorderLayout.NORTH);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(contentPane, BorderLayout.CENTER);
		{
			chatFlow = new JTextArea(20, 40);
			chatFlow.setBorder(new StrokeBorder(new BasicStroke(2)));
			chatFlow.setEditable(false);
			contentPane.add(new JScrollPane(chatFlow), BorderLayout.CENTER);
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
		chatFlow.append(text + "\n");
	}

	public String getMessage() {
		return chatEnter.getText();
	}

	public void setMessage(String text) {
		chatEnter.setText(text);
	}

	public JTextField getInputField() {
		return chatEnter;
	}

	public JButton getSendButton() {
		return send;
	}

	public JMenuItem getConnectItem() {
		return connect;
	}

	public JMenuItem getDisconnectItem() {
		return disconnect;
	}

	public JMenuItem getChangeName() {
		return changeName;
	}
}
