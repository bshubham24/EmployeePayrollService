package com.capgi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService {

	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement employeePayrollDataStatement;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

	private Connection getConnection() {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?characterEncoding=utf8"; // characterEncoding=utf8
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
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			employeePayrollList = getEmployeePayrollData(result);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
		return employeePayrollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet result) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				double salary = result.getDouble("basic_pay");
				LocalDate startDate = result.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
			}
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() throws EmployeePayrollException {

		try {
			Connection connection = this.getConnection();
			String sql = "SELECT * FROM employee_payroll WHERE name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}

	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
		return employeePayrollList;
	}

	public int updateEmployeeData(String name, double salary) throws EmployeePayrollException {
		return this.updateEmployeeDataUsingStatement(name, salary);
	}

	private int updateEmployeeDataUsingStatement(String name, double salary) throws EmployeePayrollException {
		String sql = String.format("update employee_payroll set basic_pay = %.2f where name ='%s';", salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
	}

}
