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
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteServer {
	public static final int bufferSize = 255;

	public static void main(String[] args) throws IOException {
		final ExecutorService pool;
		final ServerSocket server;
		byte[] pb = new byte[5];
		System.out.println("Enter Port Number (0 - 65500):");
		System.in.read(pb, 0, 5);
		char[] wpb = new char[5];
		for(int i=0;i<pb.length;i++){
			wpb[i] = (char)pb[i];
		}
		String sPort = new String(wpb).trim();
		int port = Integer.valueOf(sPort);
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
		if(var == "C")
			System.out.println("Created network service with a " + zusatz);
		else
			System.out.println("Created network service with a " + zusatz + " ("
					+ (threads / 2) + " Clients)");
		t1.start();
		System.out.println("Server is Running...");

		while (true) {
			try {
				Socket client = server.accept();
				while (client.isConnected()) {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(client.getInputStream()));
					char[] buffer = new char[bufferSize];
					in.read(buffer, 0, bufferSize);
					System.out.println(new String(buffer));

				}
			} catch (SocketException se) {
				System.out.println("Client Lost Connection");
			}
		}
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
				pool.execute(new ClientInputHandler(client));
				pool.execute(new ClientOutputHandler(client));
			}
		} catch (IOException ioe) {
			System.out.println("-- Client Lost Connection");
		}
	}
}

class ClientInputHandler implements Runnable {
	private Socket client;

	public ClientInputHandler(Socket client) {
		this.client = client;
	}

	public void run() {
		while (true) {
			try {
				Reader read = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				char[] buffer = new char[255];
				read.read(buffer);
				String message = new String(buffer);
				System.out.println(message);
			} catch (IOException ioe) {
				System.out.println("-- Server Error:\n" + ioe.getMessage());
			}
		}
	}

}

class ClientOutputHandler implements Runnable {
	private Socket client;

	public ClientOutputHandler(Socket client) {
		this.client = client;
	}

	public void run() {
		while (true) {
			try {
				Writer out = new BufferedWriter(new OutputStreamWriter(
						client.getOutputStream()));
				out.write("Hello World");
				out.flush();
			} catch (IOException ioe) {
				System.out.println("-- Server Error:\n" + ioe.getMessage());
			}
		}
	}
}