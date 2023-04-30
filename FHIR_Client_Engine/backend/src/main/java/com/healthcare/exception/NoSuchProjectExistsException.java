package com.healthcare.exception;


@SuppressWarnings("all")
public class NoSuchProjectExistsException extends RuntimeException {

	private String message;

	public NoSuchProjectExistsException() {
	}

	public NoSuchProjectExistsException(String msg) {
		super(msg);
		this.message = msg;
	}
}
