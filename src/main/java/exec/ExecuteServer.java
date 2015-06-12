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
				inPool.execute(new ClientInputHandler(name, client, clientList));
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
	private final Socket client;
	private final List<ChatClient> clientList;
	private String name;

	public ClientInputHandler(String userName, Socket client,
			List<ChatClient> clientList) {
		this.client = client;
		this.name = userName;
		this.clientList = clientList;
	}

	public void run() {
		try {

			System.out.println("Client " + client.getInetAddress().toString()
					+ " with name " + name + " has Connected");
			for (ChatClient s : clientList) {
				new ClientOutputHandler(s.getConnection(), "-- " + name
						+ " ist dem Chat beigetreten.").run();
				new ClientOutputHandler(s.getConnection(), "#/list"
						+ getClientList()).run();
			}
			while (true) {
				BufferedReader read = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				String message = read.readLine();
				if (message != "" && message != null && message.trim() != "")
					if (message.startsWith("/")) {
						message = message.trim();
						if (message.toLowerCase().startsWith(
								"/changeName".toLowerCase())) {
							String[] para = message.split(" ");
							if (para[1].trim() != "") {
								String nameOld = name;
								for (ChatClient c : clientList) {
									if (nameOld == c.getUserName()) {
										c.setUserName(para[1]);
										break;
									}
								}
								name = para[1];
								System.out.println("-- " + nameOld
										+ " änderte den Namen zu " + name);
								for (ChatClient s : clientList) {
									new ClientOutputHandler(s.getConnection(),
											"-- " + nameOld
													+ " änderte den Namen zu "
													+ name).run();
									new ClientOutputHandler(s.getConnection(),
											"#/list" + getClientList()).run();
								}
							}
						}
						if (message.toLowerCase().startsWith("/list")) {
							new ClientOutputHandler(client, "#/list"
									+ getClientList()).run();
						}
						if (message.toLowerCase().startsWith("/disconnect")) {
							new ClientOutputHandler(client, "-- " + name
									+ " ist jetzt Offline").run();
							System.out.println("-- " + name + " disconnected");
							client.close();
							for (ChatClient c : clientList)
								if (c.getUserName().equals(name)) {
									clientList.remove(c);
									break;
								}
							for (ChatClient c : clientList)
								new ClientOutputHandler(c.getConnection(), "#/list"
										+ getClientList()).run();
						}
					} else {
						System.out.println(name + ": " + message.trim());
						for (ChatClient s : clientList)
							new ClientOutputHandler(s.getConnection(), name
									+ ": " + message.trim()).run();
					}

			}
		} catch (IOException ioe) {
			System.out.println("-- Client: " + name + " Lost Connection:\n"
					+ ioe.getMessage());
		}
		try {
			client.close();
			for (ChatClient c : clientList)
				if (c.getUserName() == name) {
					clientList.remove(c);
					break;
				}
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

class ClientOutputHandler implements Runnable {
	private final Socket client;
	private String message;

	public ClientOutputHandler(Socket client, String message) {
		this.client = client;
		this.message = message;
	}

	public void run() {
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

	public ChatClient(String userName, Socket connection) {
		this.userName = userName;
		this.connection = connection;
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
}