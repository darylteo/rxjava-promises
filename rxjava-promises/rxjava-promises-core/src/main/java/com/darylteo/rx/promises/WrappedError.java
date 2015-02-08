package com.darylteo.rx.promises;

public class WrappedError extends Exception {
	public WrappedError(Error e) {
		super(e);
	}

	public Error unwrap() {
		return (Error) this.getCause();
	}
}
