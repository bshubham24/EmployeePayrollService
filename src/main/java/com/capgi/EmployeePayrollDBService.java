package com.capgi;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

public class EmployeePayrollDBService {
	public static void main(String[] args) {

		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false"; // characterEncoding=utf8
		String userName = "root";
		String password = "CL@Liv#6@RM#13";
		Connection connection;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver loaded");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find the driver in the classpath", e);
		}

		listDrivers();

		try {
			System.out.println("Connecting to database:" + jdbcURL);
			connection = (Connection) DriverManager.getConnection(jdbcURL, userName, password);
			System.out.println("Connection is successfull!!!" + connection);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void listDrivers() {
		Enumeration<Driver> driverList = DriverManager.getDrivers();
		while (driverList.hasMoreElements()) {
			Driver driverClass = (Driver) driverList.nextElement();
			System.out.println(" " + driverClass.getClass().getName());
		}
	}

}
