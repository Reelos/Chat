package exec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.JOptionPane;

import model.ChatFrame;
import model.ConnectDialog;

public class ExecuteClient {
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		InitChangeName nameListener = new InitChangeName();
		ChatFrame chat = new ChatFrame();
		chat.getChangeName().addActionListener(nameListener);
		chat.setVisible(true);
		ConnectDialog cd = new ConnectDialog(chat);
		chat.getConnectItem()
				.addActionListener(
						a -> {
							cd.setVisible(true);
							if (cd.getPort() >= 0)
								try {
									Socket server = new Socket(cd.getIP(), cd
											.getPort());
									if (server.isConnected()) {
										chat.applyToChat("-- Verbindung zu Server "
												+ cd.getIP()
												+ ":"
												+ cd.getPort() + " aufgebaut.");
										BufferedWriter out = new BufferedWriter(
												new OutputStreamWriter(server
														.getOutputStream()));
										out.write(nameListener.getUserName()
												+ "\n");
										out.flush();
										out.write("/list");
										out.flush();
										Thread t1 = new Thread(
												new HostListener(server, chat));
										t1.start();
										chat.getChangeName()
												.removeActionListener(
														nameListener);
										ActionListener runNameListener = new ActionListener() {
											public void actionPerformed(
													ActionEvent e) {
												nameListener
														.setUserName(JOptionPane
																.showInputDialog(
																		null,
																		"Geben sie einen Namen ein",
																		nameListener
																				.getUserName()));
												try {
													out.write("/changeName "
															+ nameListener
																	.getUserName()
															+ "\n");
													out.flush();
												} catch (IOException f) {

												}
											}
										};
										chat.getChangeName().addActionListener(
												runNameListener);
										chat.getSendButton().addActionListener(
												b -> {
													try {
														out.write(chat
																.getMessage()
																+ "\n");
														chat.setMessage("");
														out.flush();
													} catch (Exception e) {
														e.printStackTrace();
													}
												});
										chat.getInputField().addKeyListener(
												new KeyListener() {

													@Override
													public void keyPressed(
															KeyEvent arg0) {
													}

													@Override
													public void keyReleased(
															KeyEvent arg0) {
														if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
															try {
																out.write(chat
																		.getMessage()
																		+ "\n");
																chat.setMessage("");
																out.flush();
															} catch (Exception e) {
																e.printStackTrace();
															}

													}

													@Override
													public void keyTyped(
															KeyEvent arg0) {
													}

												});
										chat.getDisconnectItem()
												.addActionListener(
														b -> {
															if (!server
																	.isClosed())
																try {
																	out.write("/disconnect");
																	out.close();
																	server.close();
																} catch (IOException e) {
																} finally {
																	chat.applyToChat("-- Verbindung zu Server getrennt");
																	chat.getConnectItem()
																			.setEnabled(
																					true);
																	chat.getDisconnectItem()
																			.setEnabled(
																					false);
																	chat.getChangeName()
																			.removeActionListener(
																					runNameListener);
																	chat.getChangeName()
																			.addActionListener(
																					nameListener);
																	chat.getClientList().setListData(new String[]{});
																}
														});
										chat.getConnectItem().setEnabled(false);
										chat.getDisconnectItem().setEnabled(
												true);
									}
								} catch (IOException ce) {
									chat.applyToChat("-- Kein Server unter "
											+ cd.getIP() + ":" + cd.getPort()
											+ " gefunden");
								}
						});
	}
}

class HostListener implements Runnable {
	private final Socket server;
	private final ChatFrame chat;

	public HostListener(Socket server, ChatFrame chat) {
		this.server = server;
		this.chat = chat;
	}

	public void run() {
		try {
			while (chat.isShowing()) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						server.getInputStream()));
				String readed = in.readLine().trim();
				if (readed.startsWith("#/")) {
					if(readed.startsWith("#/list")){
						String args = readed.substring(6);
						String[] list = args.split(";");
						chat.getClientList().setListData(list);
					}
				} else
					chat.applyToChat(readed);
			}
		} catch (IOException e) {
			chat.applyToChat("-- Lost Server Connection:\n" + e.getMessage());
		}
	}
}

class InitChangeName implements ActionListener {
	private String userName = String.valueOf(new Random().nextInt(100000));

	@Override
	public void actionPerformed(ActionEvent e) {
		this.userName = JOptionPane.showInputDialog(null,
				"Geben sie einen Namen ein", this.userName);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}