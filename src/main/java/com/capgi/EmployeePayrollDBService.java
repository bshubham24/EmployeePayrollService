package com.capgi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.capgi.EmployeePayrollException.ExceptionType;

public class EmployeePayrollDBService {
	private Connection getConnection() {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false"; // characterEncoding=utf8
		String userName = "root";
		String password = "CL@Liv#6@RM#13";
		Connection connection = null;
		try {
			connection = (Connection) DriverManager.getConnection(jdbcURL, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}

	public List<EmployeePayrollData> readData() throws EmployeePayrollException {
		String sql = "Select * from employee_payroll";
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				Double salary = resultSet.getDouble("basic_pay");
				LocalDate date = resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, date));
			}
		} catch (SQLException e) {
			throw new EmployeePayrollException(ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
		return employeePayrollList;
	}

}
