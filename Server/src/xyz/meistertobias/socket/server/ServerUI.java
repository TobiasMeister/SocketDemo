package xyz.meistertobias.socket.server;

import xyz.meistertobias.socket.Config;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerUI {
	
	private static final List<ClientConnection> connections = new ArrayList<>();
	
	public static void main(String[] args) throws IOException {
		var serverSocket = ServerSocketFactory.getDefault().createServerSocket(Config.DEFAULT_PORT);
		var serverController = new ServerController();
		
		while (true) {
			var socket = serverSocket.accept();
			var connection = new ClientConnection(socket, serverController);
			connections.add(connection);
			connection.start();
		}
	}
}
