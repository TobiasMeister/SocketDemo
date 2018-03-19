package xyz.meistertobias.socket.server;

import xyz.meistertobias.socket.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepo {
	
	private List<User> users = new ArrayList<>();
	
	@SocketEndpoint
	public List<User> getUsers() {
		return users;
	}
	
	@SocketEndpoint
	public User getUser(String nickname) {
		return findUser(nickname).orElse(null);
	}
	
	@SocketEndpoint
	public void addUser(User user) {
		if (!users.contains(user)) users.add(user);
	}
	
	@SocketEndpoint
	public void deleteUser(String nickname) {
		findUser(nickname).ifPresent(users::remove);
	}
	
	private Optional<User> findUser(String nickname) {
		return users.stream().filter(user -> user.getNickname().equals(nickname)).findFirst();
	}
}
