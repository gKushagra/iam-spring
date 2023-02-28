package com.softwright.iam.models;

public class MessageResponse {
	
	private String Message;
	
	public MessageResponse(String message) {
		this.Message = message;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}
	
}
