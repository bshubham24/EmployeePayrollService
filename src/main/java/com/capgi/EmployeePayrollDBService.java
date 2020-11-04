package com.capgi;

import java.sql.Connection;
import java.sql.Date;
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
		String sql = "Select * from employee_data";
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

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
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
			String sql = "SELECT * FROM employee_data WHERE name = ?";
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
		String sql = String.format("update employee_data set salary = %.2f where name ='%s';", salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
	}

	public EmployeePayrollData addEmpToPayroll(String name, double salary, LocalDate start, String gender)
			throws EmployeePayrollException {
		int id = -1;
		EmployeePayrollData employeePayrollData = null;
		String sql = String.format(
				"INSERT INTO employee_data(name, salary, start, gender) VALUES('%s', '%s', '%s', '%s');", name, salary,
				Date.valueOf(start), gender);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					id = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(id, name, salary, start, gender);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
		return employeePayrollData;
	}

	public List<EmployeePayrollData> getEmployeePayrollDataForDateRange(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		String sql = String.format("SELECT * FROM employee_data WHERE start BETWEEN '%s' AND '%s';",
				Date.valueOf(startDate), Date.valueOf(endDate));
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try (Connection connection = getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
		return employeePayrollList;
	}

	public List<EmployeePayrollData> getEmpDataGroupedByGender(String column, String operation, String gender)
			throws EmployeePayrollException {

		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		String sql = String.format("SELECT gender, %s(%s) FROM employee_data GROUP BY gender;", operation, column);
		try (Connection connection = getConnection()) {

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String gender1 = resultSet.getString("gender");
				double salary = resultSet.getDouble(operation + "(salary)");

				employeePayrollList.add(new EmployeePayrollData(gender1, salary));
			}

		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
		return employeePayrollList;

	}

}
