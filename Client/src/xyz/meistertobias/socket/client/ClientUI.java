package xyz.meistertobias.socket.client;

import xyz.meistertobias.socket.Config;
import xyz.meistertobias.socket.InvocationResponse;
import xyz.meistertobias.socket.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ClientUI {
	
	public static void main(String[] args) {
		try (var socket = new Socket(InetAddress.getLocalHost(), Config.DEFAULT_PORT);
			 var in = socket.getInputStream();
			 var out = socket.getOutputStream();
			 var dataIn = new ObjectInputStream(in);
			 var dataOut = new ObjectOutputStream(out)) {
			
			// Socket: calc
			{
				var arithmeticOps = new String[]{"add", "subtract", "multiply", "divide"};
				
				// add, subtract, multiply, divide
				for (var arithmetic : arithmeticOps) {
					dataOut.writeUTF("calc::" + arithmetic);
					dataOut.flush();
					if (((InvocationResponse) dataIn.readObject()).getStatus() == 200) {
						dataOut.writeInt(5);
						dataOut.writeInt(9);
						dataOut.flush();
						System.out.println(dataIn.readInt());
					}
				}
			}
			
			System.out.println();
			
			// Socket: user
			{
				// addUser
				dataOut.writeUTF("userRepo::addUser");
				dataOut.flush();
				if (((InvocationResponse) dataIn.readObject()).getStatus() == 200) {
					var user = new User("Marcel", "Davis", "Leiter für Kundenzufriedenheit");
					dataOut.writeObject(user);
				}
				
				// getUsers
				dataOut.writeUTF("userRepo::getUsers");
				dataOut.flush();
				if (((InvocationResponse) dataIn.readObject()).getStatus() == 200) {
					((List<User>) dataIn.readObject()).forEach(System.out::println);
				}
				
				// getUser
				dataOut.writeUTF("userRepo::getUser");
				dataOut.flush();
				if (((InvocationResponse) dataIn.readObject()).getStatus() == 200) {
					dataOut.writeUTF("Leiter für Kundenzufriedenheit");
					dataOut.flush();
					System.out.println(dataIn.readObject());
				}
				
				// deleteUser
				dataOut.writeUTF("userRepo::deleteUser");
				dataOut.flush();
				if (((InvocationResponse) dataIn.readObject()).getStatus() == 200) {
					dataOut.writeUTF("Leiter für Kundenzufriedenheit");
				}
			}
			
			dataOut.writeUTF("SOCKET::DISCONNECT");
			dataOut.flush();
			System.out.println(dataIn.readObject());
			
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
