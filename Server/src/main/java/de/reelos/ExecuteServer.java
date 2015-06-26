package de.reelos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecuteServer {
	public static final int bufferSize = 255;

	public static void main(String[] args) throws IOException {
		final ExecutorService inPool;
		final ServerSocket server;
		int port = 6260;
		byte[] pb = new byte[5];
		System.out.println("Enter Port Number (0 - 65500):");
		System.in.read(pb, 0, 5);
		char[] wpb = new char[5];
		for (int i = 0; i < pb.length; i++) {
			wpb[i] = (char) pb[i];
		}
		String sPort = new String(wpb).trim();
		try {
			port = Integer.valueOf(sPort);
		} catch (NumberFormatException nfe) {
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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Strg+C, Stopping network service");
				inPool.shutdown();
				try {
					inPool.awaitTermination(4L, TimeUnit.SECONDS);
					if (!server.isClosed()) {
						System.out.println("Server close");
						server.close();
					}
				} catch (IOException e) {
				} catch (InterruptedException ei) {
				}
			}
		});
		Thread t1 = new Thread(new NetworkService(server, inPool, clientList));
		System.out.println("Created network service with a " + zusatz);
		t1.start();
	}
}

class NetworkService implements Runnable {
	private final ServerSocket connector;
	private final ExecutorService inPool;
	private final List<ChatClient> clientList;

	public NetworkService(ServerSocket connector, ExecutorService inPool, List<ChatClient> clientList) {
		this.connector = connector;
		this.inPool = inPool;
		this.clientList = clientList;
	}

	public void run() {
		Thread t1 = new Thread() {
			@Override
			public void run() {
				while (true) {
					clientList.forEach(c -> {
						if (c.getConnection().isClosed()){
							clientList.remove(c);
						}
					});
					clientList.forEach(c -> c.send("#/list " + getClientList()));
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t1.start();
		try {
			while (true) {
				Socket client = connector.accept();
				BufferedReader read = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String name = read.readLine();
				if (name != null) {
					name = name.trim();
					clientList.add(new ChatClient(name, client, clientList));
					inPool.execute(new ClientInputHandler(clientList.get(clientList.size() - 1), clientList));
				}
			}
		} catch (IOException ioe) {
			System.out.println("-- Client Lost Connection:\n" + ioe.getMessage());
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

class ClientInputHandler implements Runnable {
	private final ChatClient client;
	private final List<ChatClient> clientList;

	public ClientInputHandler(ChatClient client, List<ChatClient> clientList) {
		this.client = client;
		this.clientList = clientList;
	}

	public void run() {
		try {

			System.out.println("Client " + client.getConnection().getInetAddress().toString() + " with name " + client.getUserName() + " has Connected");
			clientList.forEach(c -> c.send("-- " + client.getUserName() + " ist dem Chat beigetreten."));
			clientList.forEach(c -> c.send("#/list" + getClientList()));

			while (true) {
				String message = client.receive();
				if (message != null){
					if (!message.equals("") && !message.trim().equals(""))
						if (message.startsWith("/")) {
							message = message.trim();
							if (message.toLowerCase().startsWith("/changeName".toLowerCase())) {
								String[] para = message.split(" ");
								if (!para[1].trim().equals("")) {
									String nameOld = client.getUserName();
									client.setUserName(para[1]);
									System.out.println("-- " + nameOld + " änderte den Namen zu " + client.getUserName());

									clientList.forEach(c -> c.send("-- " + nameOld + " änderte den Namen zu " + client.getUserName()));

									clientList.forEach(c -> c.send("#/list" + getClientList()));
								}
							}
							if (message.toLowerCase().startsWith("/list")) {
								client.send("#/list" + getClientList());
							}
							if (message.toLowerCase().startsWith("/disconnect")) {
								clientList.forEach(c -> c.send("-- " + client.getUserName() + " ist jetzt Offline"));
								System.out.println("-- " + client.getUserName() + " disconnected");
								client.getConnection().close();
								clientList.remove(client);
								clientList.forEach(c -> c.send("#/list" + getClientList()));
							}
							if (message.toLowerCase().startsWith("/to")) {
								String[] args = message.split(" ");
								if (args.length >= 3) {
									String receiver = args[1];
									String sender = client.getUserName();
									if (!sender.matches(receiver)) {
										ChatClient receivingClient = null;
										for (ChatClient c : clientList) {
											if (c.getUserName().matches(receiver)) {
												receivingClient = c;
												break;
											}
										}
										if (receivingClient != null) {
											String send = "";
											for (int i = 2; i < args.length; i++) {
												if (send.matches(""))
													send = args[i];
												else
													send += " " + args[i];
											}
											receivingClient.send("#/from " + sender + " " + send);
											receivingClient.send("Whisper from " + sender + ": " + send);
											client.send("#/to " + sender + " " + send);
											client.send("Whisper to " + receiver + ": " + send);
										} else {
											client.send("-- Der Client \"" + receiver + "\" existiert nicht.");
										}
									} else {
										client.send("-- Du kannst keine Nachrichten an dich selbst senden");
									}
								} else {
									client.send("-- Falsche Syntax: /to <Receiver> <Message>");
								}
							}
						} else {
							message = message.trim();
							System.out.println(client.getUserName() + ": " + message);
							for (ChatClient c : clientList) {
								c.send(client.getUserName() + ": " + message);
							}
						}
				}else{
					break;
				}
			}
		} catch (IOException ioe) {
			System.out.println("-- Client: " + client.getUserName() + " Lost Connection:\n" + ioe.getMessage());
		}
		try {
			client.getConnection().close();
			clientList.remove(client);
			clientList.forEach(c -> c.send("#/list" + getClientList()));
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

class ChatClient {
	private String userName;
	private final Socket connection;
	private final BufferedWriter out;
	private final BufferedReader in;
	private final List<ChatClient> clientList;

	public ChatClient(String userName, Socket connection, List<ChatClient> clientlist) throws IOException {
		this.userName = userName;
		this.connection = connection;
		this.clientList = clientlist;
		this.out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void send(String message) {
		try {
			out.write(message);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String receive() {
		String message = "";
		try {
			message = in.readLine();
		} catch (IOException e) {
			System.out.println("Client "+userName+" lost Connection: "+ e.getMessage());
			try{
				connection.close();
				clientList.remove(this);
				clientList.forEach(c-> c.send("-- "+ userName + " ist jetzt Offline."));
				return null;
			}catch(IOException e1){
				System.out.println("Unknown Error: "+e1.getMessage());
			}
		}
		return message;
	}

	public Socket getConnection() {
		return connection;
	}

	public boolean isOk() {
		return connection.isConnected();
	}
}