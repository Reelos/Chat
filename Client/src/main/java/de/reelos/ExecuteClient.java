package de.reelos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;

import de.reelos.model.ChatFrame;
import de.reelos.model.ConnectDialog;

public class ExecuteClient {
	Socket server = null;
	InitChangeName nameListener = new InitChangeName();
	ChatFrame chat = new ChatFrame();
	BufferedWriter out = null;
	ConnectDialog cd;
	
	public ExecuteClient() throws UnknownHostException, IOException, InterruptedException {
		cd = new ConnectDialog(chat);
		cd.ConnectButton().addActionListener(a -> {
			connect();
		});
		
		ActionListener runNameListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nameListener.setUserName(JOptionPane.showInputDialog(null, "Geben sie einen Namen ein", nameListener.getUserName()));
				try {
					out.write("/changeName " + nameListener.getUserName() + "\n");
					out.flush();
				} catch (IOException f) {

				}
			}
		};
		
		File maincfg = new File("main.cfg");
		FileReader fis;
		maincfg.createNewFile();
		fis = new FileReader(maincfg);
		char[] cfg = new char[65540];
		fis.read(cfg);
		fis.close();
		String[] cfgArgs = new String(cfg).split("\r\n");
		HashMap<String,String> config = new HashMap<>();
		Arrays.asList(cfgArgs).forEach(c -> {
			if(!c.startsWith("#")){
				String[] f = c.trim().split("=");
				if(f.length==2)
				config.put(f[0].toLowerCase(), f[1]);
			}
		});
		
		chat.getChangeName().addActionListener(nameListener);
		chat.setVisible(true);
		chat.getConnectItem().addActionListener(a -> {
			cd.setVisible(true);
			if (cd.getPort() >= 0){
			chat.getChangeName().addActionListener(runNameListener);
			chat.getConnectItem().setEnabled(false);
			chat.getDisconnectItem().setEnabled(true);
			}
		});
		
		chat.getDisconnectItem().addActionListener(b -> {
			if (!server.isClosed() && out!=null)
				try {
					out.write("/disconnect");
					out.close();
					server.close();
				} catch (IOException e) {
				} finally {
					chat.applyToChat("-- Verbindung zu Server getrennt");
					chat.getConnectItem().setEnabled(true);
					chat.getDisconnectItem().setEnabled(false);
					chat.getChangeName().removeActionListener(runNameListener);
					chat.getChangeName().addActionListener(nameListener);
					chat.getClientList().setListData(new String[] {});
				}
		});
		
		chat.getSendButton().addActionListener(b -> {
			try {
				out.write(chat.getMessage() + "\n");
				chat.setMessage("");
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		chat.getInputField().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
					try {
						out.write(chat.getMessage() + "\n");
						chat.setMessage("");
						out.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}

			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

		});
		
		if(config.containsKey("username"))
			nameListener.setUserName(config.get("username"));
		
		if(config.containsKey("serveradress")){
			cd.setIP(config.get("serveradress"));
			if(config.containsKey("autoconnect")){
				if(config.get("autoconnect").matches("true")){
					cd.ConnectButton().doClick();
				}
			}
		}
	}
	
	public void connect(){
		try {
			server = new Socket(cd.getIP(), cd.getPort());
			if (server.isConnected()) {
				chat.applyToChat("-- Verbindung zu Server " + cd.getIP() + ":" + cd.getPort() + " aufgebaut.");
				out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
				out.write(nameListener.getUserName());
				out.newLine();
				out.flush();
				Thread t1 = new Thread(new HostListener(server, chat));
				t1.start();
				out.write("/list");
				out.newLine();
				out.flush();
				chat.getChangeName().removeActionListener(nameListener);
			}
		} catch (IOException ce) {
			chat.applyToChat("-- Kein Server unter " + cd.getIP() + ":" + cd.getPort() + " gefunden");
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		ExecuteClient client = new ExecuteClient();
		client.toString();
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
				BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
				String readed = in.readLine();
				if (readed != null) {
					readed = readed.trim();
					if (readed.startsWith("#/")) {
						if (readed.startsWith("#/list")) {
							String args = readed.substring(7);
							String[] list = args.split(";");
							chat.getClientList().setListData(list);
						}
					} else
						chat.applyToChat(readed);
				}
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
		this.userName = JOptionPane.showInputDialog(null, "Geben sie einen Namen ein", this.userName);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}