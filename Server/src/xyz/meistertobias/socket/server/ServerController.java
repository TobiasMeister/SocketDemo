package xyz.meistertobias.socket.server;

public class ServerController {
	
	@InvocationTarget
	public UserRepo userRepo = new UserRepo();
	
	@InvocationTarget
	public Calc calc = new Calc();
}
