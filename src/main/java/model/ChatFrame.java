package main.java.model;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.StrokeBorder;

public class ChatFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6385358475901470704L;
	private static final String version = "v0.001";

	private JTextArea chatFlow;
	private JList<String> clients;
	private JScrollPane scroll;
	private JTextField chatEnter;
	private JButton send;
	private JMenuItem connect;
	private JMenuItem disconnect;
	private JMenuItem changeName;

	public ChatFrame() {
		super("Chat Client " + version);
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setResizable(false);

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
		contentPane.setLayout(new BorderLayout(5, 5));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(contentPane, BorderLayout.CENTER);
		{
			chatFlow = new JTextArea(20, 40);
			chatFlow.setEditable(false);

			scroll = new JScrollPane(chatFlow,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setBorder(new StrokeBorder(new BasicStroke(2)));

			contentPane.add(scroll, BorderLayout.CENTER);

			clients = new JList<>();
			clients.setFixedCellWidth(100);
			JScrollPane clientScroll = new JScrollPane(clients,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			clientScroll.setBorder(new StrokeBorder(new BasicStroke(2)));
			contentPane.add(clientScroll, BorderLayout.EAST);

		}
		{
			JPanel sendPane = new JPanel();
			sendPane.setBorder(new EmptyBorder(5, 0, 0, 0));
			sendPane.setLayout(new BoxLayout(sendPane, BoxLayout.X_AXIS));
			{
				chatEnter = new JTextField(35);
				sendPane.add(chatEnter);
				send = new JButton("Senden");
				send.setMargin(new Insets(0, 0, 0, 0));
				sendPane.add(send);
				sendPane.add(Box.createRigidArea(new Dimension(120, 1)));
			}
			contentPane.add(sendPane, BorderLayout.SOUTH);

		}
		pack();
		setLocationRelativeTo(null);
	}

	public void resetChat() {
		chatFlow.setText("");
	}
	public void applyToChat(String text) {
		chatFlow.append(text + "\n");
		scroll.getVerticalScrollBar().setValue(
				scroll.getVerticalScrollBar().getMaximum());
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

	public JList<String> getClientList() {
		return clients;
	}
}
