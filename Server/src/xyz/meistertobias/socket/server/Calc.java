package xyz.meistertobias.socket.server;

public class Calc {
	
	@SocketEndpoint
	public int add(int x, int y) {
		return x + y;
	}
	
	@SocketEndpoint
	public int subtract(int x, int y) {
		return x - y;
	}
	
	@SocketEndpoint
	public int multiply(int x, int y) {
		return x * y;
	}
	
	@SocketEndpoint
	public int divide(int x, int y) {
		return x / y;
	}
}
