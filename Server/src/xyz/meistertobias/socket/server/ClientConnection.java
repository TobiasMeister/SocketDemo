package xyz.meistertobias.socket.server;

import xyz.meistertobias.socket.Config;
import xyz.meistertobias.socket.InvocationResponse;
import xyz.meistertobias.socket.Util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClientConnection extends Thread {
	
	private final Socket socket;
	private final ServerController controller;
	
	private boolean alive;
	
	public ClientConnection(Socket socket, ServerController controller) {
		this.socket = socket;
		this.controller = controller;
	}
	
	/**
	 * Builds an instance of {@link ObjectOutputStream} and immediately flushes the stream afterwards.
	 *
	 * <p> Required for try-with-resources statements to work properly.</p>
	 *
	 * <b> Build the output stream before creating the input stream! </b>
	 *
	 * @param outputStream the {@link OutputStream} to wrap
	 * @return a new {@link ObjectOutputStream} for a given {@link OutputStream}
	 * @throws IOException
	 */
	private static ObjectOutputStream buildOutput(OutputStream outputStream) throws IOException {
		var objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.flush();
		return objectOutputStream;
	}
	
	private static final Predicate<Method> ENDPOINT_FILTER = method -> {
		var socketEndpoint = method.getAnnotation(SocketEndpoint.class);
		return socketEndpoint != null && socketEndpoint.enabled();
	};
	
	@Override
	public void run() {
		alive = true;
		
		try (var in = socket.getInputStream();
			 var out = socket.getOutputStream();
			 var dataOut = buildOutput(out);
			 var dataIn = new ObjectInputStream(in)) {
			
			// Collect available methods to call via open sockets
			var methods = new HashMap<String, List<Method>>();
			methods.put("", Arrays.stream(controller.getClass().getMethods())
					.filter(ENDPOINT_FILTER)
					.collect(Collectors.toList()));
			
			var instances = controller.getClass().getFields();
			for (var instance : instances) {
				// Validate InvocationTarget annotation
				var invocationTarget = instance.getAnnotation(InvocationTarget.class);
				if (invocationTarget != null && invocationTarget.enabled()) {
					// Validate SocketEndpoint annotation
					methods.put(instance.getName(), Arrays.stream(instance.getType().getMethods())
							.filter(ENDPOINT_FILTER)
							.collect(Collectors.toList()));
				}
			}
			
			// Socket communication loop
			while (true) {
				var callArgs = dataIn.readUTF();
				
				if (callArgs.equals(Config.FUNC_SOCKET_DISCONNECT)) {
					System.out.println("[" + Util.TIMESTAMP.get() + "] " + "Client closed connection");
					dataOut.writeObject(InvocationResponse.RESPONSE_CALL_SUCCESS.withMessage("Disconnected"));
					dataOut.flush();
					break;
				}
				
				var cmd = callArgs.split("::");
				
				var inst = cmd.length > 1 ? cmd[0] : "";
				var func = cmd.length > 1 ? cmd[1] : cmd[0];
				
				// Fetch requested function
				Optional<Method> calledEndpoint = methods.get(inst).stream().filter(method -> {
					var endpoint = method.getAnnotation(SocketEndpoint.class).name();
					if (endpoint != null && !endpoint.isEmpty()) {
						return endpoint.equals(func);
					} else {
						return method.getName().equals(func);
					}
				}).findFirst();
				
				// Method call validation
				if (!calledEndpoint.isPresent()) {
					dataOut.writeObject(InvocationResponse.RESPONSE_CALL_ILLEGAL.withMessage(
							"Function call " + callArgs + " not allowed"));
					dataOut.flush();
					continue;
				} else {
					dataOut.writeObject(InvocationResponse.RESPONSE_CALL_SUCCESS.withMessage(
							"Function call " + callArgs + " allowed"));
					dataOut.flush();
				}
				
				System.out.println("[" + Util.TIMESTAMP_PRECISE.get() + "] " + "Called " + callArgs);
				
				// Call execution
				{
					var method = calledEndpoint.get();
					var instance = inst.isEmpty() ? controller : controller.getClass().getField(cmd[0]).get(controller);
					var paramDeclarations = method.getParameters();
					var parameters = new Object[paramDeclarations.length];
					
					// Read parameters from input
					for (var i = 0; i < parameters.length; i++) {
						parameters[i] = readParameter(dataIn, paramDeclarations[i].getType());
					}
					
					// Invoke method
					var returnValue = method.invoke(instance, parameters);
					
					// Write return value if available
					var returnType = method.getReturnType();
					if (returnType != Void.TYPE) {
						writeParameter(dataOut, returnType, returnValue);
						dataOut.flush();
					}
				}
				
				// Close socket on server side if client left
				if (socket.isClosed()) {
					alive = false;
				}
			}
			
		} catch (EOFException e) {
			e.printStackTrace();
			
		} catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			
		} catch (NoSuchFieldException e) {
			// Ignore, shouldn't happen
		} finally {
			alive = false;
			if (socket != null && !socket.isClosed()) {
				try {
					alive = false;
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static <T> T readParameter(ObjectInputStream inputStream, Class<T> type) throws IOException, ClassNotFoundException {
		if (type == boolean.class) {
			return (T) (Object) inputStream.readBoolean();
		} else if (type == byte.class) {
			return (T) (Object) inputStream.readByte();
		} else if (type == char.class) {
			return (T) (Object) inputStream.readChar();
		} else if (type == double.class) {
			return (T) (Object) inputStream.readDouble();
		} else if (type == float.class) {
			return (T) (Object) inputStream.readFloat();
		} else if (type == int.class) {
			return (T) (Object) inputStream.readInt();
		} else if (type == long.class) {
			return (T) (Object) inputStream.readLong();
		} else if (type == short.class) {
			return (T) (Object) inputStream.readShort();
		} else if (type == String.class) {
			return (T) inputStream.readUTF();
		} else {
			return (T) inputStream.readObject();
		}
	}
	
	private static <T> void writeParameter(ObjectOutputStream outputStream, Class<T> type, Object value) throws IOException, ClassNotFoundException {
		if (type == boolean.class) {
			outputStream.writeBoolean((boolean) value);
		} else if (type == byte.class) {
			outputStream.writeByte((byte) value);
		} else if (type == char.class) {
			outputStream.writeChar((char) value);
		} else if (type == double.class) {
			outputStream.writeDouble((double) value);
		} else if (type == float.class) {
			outputStream.writeFloat((float) value);
		} else if (type == int.class) {
			outputStream.writeInt((int) value);
		} else if (type == long.class) {
			outputStream.writeLong((long) value);
		} else if (type == short.class) {
			outputStream.writeShort((short) value);
		} else if (type == String.class) {
			outputStream.writeUTF((String) value);
		} else {
			outputStream.writeObject(value);
		}
	}
}
