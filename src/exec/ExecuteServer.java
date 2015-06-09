package exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecuteServer {
	public static final int bufferSize = 255;

	public static void main(String[] args) throws IOException {
		final ExecutorService pool;
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
		int threads = 8;
		if (args.length > 0)
			var = args[0].toUpperCase();
		if (var == "C") {
			pool = Executors.newCachedThreadPool();
			zusatz = "chached thread pool";
		} else {

			try {
				threads = Integer.valueOf(var.trim()) * 2;
			} catch (NumberFormatException nfe) {
			}
			pool = Executors.newFixedThreadPool(threads);
			zusatz = threads + " thread pool";
		}
		server = new ServerSocket(port);
		Thread t1 = new Thread(new NetworkService(server, pool));
		if (var == "C")
			System.out.println("Created network service with a " + zusatz);
		else
			System.out.println("Created network service with a " + zusatz
					+ " (" + (threads / 2) + " Clients)");
		t1.start();
		System.out.println("Server is Running at Port " + port + "...");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Strg+C, Stopping network service");
				pool.shutdown();
				try {
					pool.awaitTermination(4L, TimeUnit.SECONDS);
					if (!server.isClosed()) {
						System.out.println("Server close");
						server.close();
					}
				} catch (IOException e) {
				} catch (InterruptedException ei) {
				}
			}
		});
	}
}

class NetworkService implements Runnable {
	private final ServerSocket connector;
	private final ExecutorService pool;

	public NetworkService(ServerSocket connector, ExecutorService pool) {
		this.connector = connector;
		this.pool = pool;
	}

	public void run() {
		try {
			while (true) {
				Socket client = connector.accept();
				System.out
						.println("Client " + client.getInetAddress().toString()
								+ " has Connected");
				pool.execute(new ClientInputHandler(client));
				pool.execute(new ClientOutputHandler(client));
			}
		} catch (IOException ioe) {
			System.out.println("-- Client Lost Connection:\n"
					+ ioe.getMessage());
		} finally {
			pool.shutdown();
			try {
				pool.awaitTermination(4L, TimeUnit.SECONDS);
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
	private Socket client;
	private String address;

	public ClientInputHandler(Socket client) {
		this.client = client;
		this.address = client.getInetAddress().toString();
	}

	public void run() {
		try {
			while (true) {

				Reader read = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				char[] buffer = new char[255];
				read.read(buffer);
				String message = new String(buffer).trim();
				if (message != "")
					System.out.println(message);

			}
		} catch (IOException ioe) {
			System.out.println("-- Client: " + address + " Lost Connection:\n"
					+ ioe.getMessage());
		}
		try {
			client.close();
		} catch (IOException e) {
			System.out.println("-- Server Error:\n" + e.getMessage());
		}
	}

}

class ClientOutputHandler implements Runnable {
	private Socket client;

	public ClientOutputHandler(Socket client) {
		this.client = client;
	}

	public void run() {

		try {
			while (true) {
				Writer out = new BufferedWriter(new OutputStreamWriter(
						client.getOutputStream()));
				out.write("Hello World\n");
				out.flush();
			}
		} catch (IOException ioe) {
		}
		try {
			client.close();
		} catch (IOException e) {
		}
	}
}