package xyz.meistertobias.socket;

import java.io.Serializable;
import java.util.function.Supplier;

public class InvocationResponse implements Serializable {
	
	public static final InvocationResponse RESPONSE_CALL_SUCCESS = new InvocationResponse(200);
	public static final InvocationResponse RESPONSE_CALL_ILLEGAL = new InvocationResponse(400);
	public static final InvocationResponse RESPONSE_CALL_ERROR = new InvocationResponse(500);
	
	private final int status;
	private Supplier<String> message = () -> null;
	
	public InvocationResponse(int status) {
		this.status = status;
	}
	
	public InvocationResponse(int status, String message) {
		this.status = status;
		this.message = (Supplier<String> & Serializable) () -> message;
	}
	
	public InvocationResponse(int status, Supplier<String> messageSupplier) {
		this.status = status;
		this.message = messageSupplier;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message.get();
	}
	
	public InvocationResponse withMessage(String message) {
		return new InvocationResponse(this.status, (Supplier<String> & Serializable) () -> message);
	}
	
	public InvocationResponse withMessage(Supplier<String> messageSupplier) {
		return new InvocationResponse(this.status, message);
	}
	
	@Override
	public String toString() {
		return "InvocationResponse{" + "status=" + status + ", message=" + message.get() + '}';
	}
}
