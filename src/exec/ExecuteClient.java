package exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import model.ChatFrame;

public class ExecuteClient {
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		ChatFrame chat = new ChatFrame();
		try {
			Socket server = new Socket("127.0.0.1", 6260);
			if(server.isConnected()) {
				chat.setVisible(true);
				Thread t1 = new Thread(new HostListener(server,chat));
				t1.start();
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						server.getOutputStream()));
				chat.getSendButton().addActionListener(a ->{
					try {
						out.write(chat.getMessage());
						chat.setMessage("");
						out.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		} catch (ConnectException ce) {
			JOptionPane.showMessageDialog(null, "There is no Server listening");
		}
	}
}

class HostListener implements Runnable{
	private final Socket server;
	private final ChatFrame chat;
	public HostListener(Socket server, ChatFrame chat){
		this.server = server;
		this.chat = chat;
	}
	
	public void run() {
		try{
			while(true){
				BufferedReader in = new BufferedReader(new InputStreamReader(
						server.getInputStream()));
				String readed = in.readLine().trim();
				chat.applyToChat(readed);
			}
		}catch(IOException e){
			chat.applyToChat("-- Lost Server Connection:\n"+e.getMessage());
		}
	}
}