package main.java.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecuteServer {
	public static final int bufferSize = 255;

	public static void main(String[] args) throws IOException {
		final ExecutorService inPool;
		final ServerSocket server;
		final int port;
		Scanner inline = new Scanner(System.in);
		System.out.println("Enter Port Number (0 - 65500):");
		String sPort = inline.nextLine().trim();
		try {
			port = Integer.valueOf(sPort);
		} finally {
		}

		String var = "C";
		String zusatz;
		int threads = 4;
		if (args.length > 0)
			var = args[0].toUpperCase();
		if (var == "C") {
			inPool = Executors.newCachedThreadPool();
			zusatz = "chached thread pool";
		} else {

			try {
				threads = Integer.valueOf(var.trim()) * 2;
			} catch (NumberFormatException nfe) {
			}
			inPool = Executors.newFixedThreadPool(threads);
			zusatz = threads + " thread pool";
		}
		final List<ChatClient> clientList = new ArrayList<>();
		server = new ServerSocket(port);
		System.out.println("Server is Running at Port " + port + "...");
		Thread t1 = new Thread(new NetworkService(server, inPool, clientList));
		System.out.println("Created network service with a " + zusatz);
		t1.start();
		if (inline.nextLine().trim().toLowerCase().equals("exit")) {
			System.out.println("-- Server shutted down");
			inPool.shutdown();
			try {
				inPool.awaitTermination(4L, TimeUnit.SECONDS);
				if (!server.isClosed()) {
					System.out.println("-- Server closed");
					server.close();
					System.out.println("-- Disconnecting Clients");
					clientList.forEach(a -> {
						try {
							a.getConnection().close();
						} catch (Exception e) {
						}
					});
				}
			} catch (IOException e) {
			} catch (InterruptedException ei) {
			}
		}
		inline.close();
	}
}

class NetworkService implements Runnable {
	private final ServerSocket connector;
	private final ExecutorService inPool;
	private final List<ChatClient> clientList;

	public NetworkService(ServerSocket connector, ExecutorService inPool,
			List<ChatClient> clientList) {
		this.connector = connector;
		this.inPool = inPool;
		this.clientList = clientList;
	}

	public void run() {
		try {
			while (true) {
				Socket client = connector.accept();
				BufferedReader read = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				String name = read.readLine().trim();
				clientList.add(new ChatClient(name, client));
				inPool.execute(new ClientInputHandler(clientList.get(clientList
						.size() - 1), clientList));
			}
		} catch (IOException ioe) {
			System.out.println("-- Client Lost Connection:\n"
					+ ioe.getMessage());
		} finally {
			inPool.shutdown();
			try {
				inPool.awaitTermination(4L, TimeUnit.SECONDS);
				if (!connector.isClosed()) {
					connector.close();
					System.out.println("-- Stopped Connector:");
				}
			} catch (IOException | InterruptedException ioe) {
				System.out.println("-- Stopped Service:\n" + ioe.getMessage());
			}
		}
	}
}

class ClientInputHandler implements Runnable {
	private final ChatClient client;
	private final List<ChatClient> clientList;

	public ClientInputHandler(ChatClient client, List<ChatClient> clientList) {
		this.client = client;
		this.clientList = clientList;
	}

	public void run() {
		try {

			System.out.println("Client "
					+ client.getConnection().getInetAddress().toString()
					+ " with name " + client.getUserName() + " has Connected");
			for (ChatClient s : clientList) {
				s.send("-- " + client.getUserName()
						+ " ist dem Chat beigetreten.");
				s.send("#/list" + getClientList());
			}
			while (true) {
				BufferedReader read = new BufferedReader(new InputStreamReader(
						client.getConnection().getInputStream()));
				String message = read.readLine();
				if (message != "" && message != null && message.trim() != "")
					if (message.startsWith("/")) {
						message = message.trim();
						if (message.toLowerCase().startsWith(
								"/changeName".toLowerCase())) {
							String[] para = message.split(" ");
							if (para.length > 1) {
								if (para[1].trim() != "") {
									String nameOld = client.getUserName();
									client.setUserName(para[1]);
									System.out.println("-- " + nameOld
											+ " änderte den Namen zu "
											+ client.getUserName());
									for (ChatClient s : clientList) {

										s.send("-- " + nameOld
												+ " änderte den Namen zu "
												+ client.getUserName());

										s.send("#/list" + getClientList());
									}
								}
							} else {
								client.send("Feher: Falsche Syntax: /changeName <UserName>");
							}
						}
						if (message.toLowerCase().startsWith("/list")) {
							client.send("#/list" + getClientList());
						}
						if (message.toLowerCase().startsWith("/disconnect")) {
							for(ChatClient s: clientList){
							s.send("-- " + client.getUserName()
									+ " ist jetzt Offline");
							}
							System.out.println("-- " + client.getUserName()
									+ " disconnected");
							client.getConnection().close();

							clientList.remove(client);

							for (ChatClient c : clientList)
								c.send("#/list" + getClientList());
						}
						if (message.toLowerCase().startsWith("/to")) {
							String[] para = message.split(" ");
							if(para.length > 2) {
								if(!para[1].equals(client.getUserName())){
									ChatClient toTarget = null;
									for(ChatClient cc: clientList){
										if(cc.getUserName().equals(para[1])){
											toTarget = cc;
											break;
										}
									}
									if(!(toTarget == null)) {
										if(!para[2].isEmpty()) {
											String whisper = "";
											for(int i=2;i<para.length;i++){
												if(whisper.isEmpty())
													whisper += para[i];
												else
													whisper += " "+para[i];
											}
											toTarget.send(client.getUserName()+"<Whisper>: "+whisper);
											client.send(client.getUserName()+"<Whisper>: "+whisper);
											toTarget.send("#/to "+client.getUserName()+"<Whisper>: "+whisper);
											client.send("#/to "+ client.getUserName()+"<Whisper>: "+whisper);
										}else{
											client.send("-- Du kannst keine Leeren Nachrichten verschicken");
										}
									}else{
										client.send("-- Dieser User Existiert nicht");
									}
								}else{
									client.send("-- Du kannst keine Nachrichten an dich Selbet verschicken!");
								}
							}else{
								client.send("-- Falsche Syntax: /to <UserName> <Message>");
							}
						}
					} else {
						System.out.println(client.getUserName() + ": "
								+ message.trim());
						for (ChatClient s : clientList)
							s.send(client.getUserName() + ": " + message.trim());
					}

			}
		} catch (IOException ioe) {
			System.out.println("-- Client: " + client.getUserName()
					+ " Lost Connection:\n" + ioe.getMessage());
		}
		try {
			client.getConnection().close();
			clientList.remove(client);
		} catch (IOException e) {
			System.out.println("-- Server Error:\n" + e.getMessage());
		}
	}

	public String getClientList() {
		String list = "";
		for (ChatClient c : clientList) {
			if (list == "")
				list = c.getUserName();
			else
				list += ";" + c.getUserName();
		}
		list += "\n";
		return list;
	}
}

class ClientOutputHandler {
	private final Socket client;

	public ClientOutputHandler(Socket client) {
		this.client = client;
	}

	public void send(String message) {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream()));
			out.write(message + "\n");
			out.flush();
		} catch (IOException ioe) {
		}
	}
}

class ChatClient {
	private String userName;
	private final Socket connection;
	private ClientOutputHandler out;

	public ChatClient(String userName, Socket connection) {
		this.userName = userName;
		this.connection = connection;
		this.out = new ClientOutputHandler(connection);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Socket getConnection() {
		return connection;
	}

	public boolean isOk() {
		return connection.isConnected();
	}

	public void send(String message) {
		out.send(message);
	}
}