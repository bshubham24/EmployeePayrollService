package com.capgi;

public class EmployeePayrollException extends Exception {
	public enum ExceptionType {
		CONNECTION_ERROR, INVALID_INFO, THREAD_FAILURE;
	}

	ExceptionType type;

	public EmployeePayrollException(ExceptionType type, String message) {
		super(message);
		this.type = type;
	}
}
