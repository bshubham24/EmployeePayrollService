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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBService {
	private int connectionCounter = 0;
	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement employeePayrollDataStatement;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

	private synchronized Connection getConnection() {
		connectionCounter++;
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?characterEncoding=utf8";
		String userName = "root";
		String password = System.getenv("password");
		Connection connection = null;
		try {
			System.out.println("Processing Thread : " + Thread.currentThread().getName()
					+ "\nConnecting to database with id : " + connectionCounter);
			connection = (Connection) DriverManager.getConnection(jdbcURL, userName, password);
			System.out.println("Processing Thread : " + Thread.currentThread().getName() + "Id : " + connectionCounter
					+ "\nConnection successful");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}

	public List<EmployeePayrollData> readData() throws EmployeePayrollException {
		String sql = "SELECT * FROM employee_data WHERE status = 'active';";
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
				String gender = resultSet.getString("gender");
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate, gender));
			}
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() throws EmployeePayrollException {

		try {
			Connection connection = this.getConnection();
			String sql = "SELECT * FROM employee_data WHERE name = ? AND status = 'active'";
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
		String sql = String.format("update employee_data set salary = %.2f where name ='%s' AND status = 'active';",
				salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		}
	}

	public synchronized EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate start,
			String gender, String department) throws EmployeePayrollException, SQLException {
		Map<Integer, Boolean> statusMap = new HashMap<Integer, Boolean>();
		int id;
		EmployeePayrollData employeePayrollData = null;

		Connection connection = this.getConnection();
		connection.setAutoCommit(false);

		statusMap.put(1, false);
		statusMap.put(2, false);
		statusMap.put(3, false);

		Runnable empTask = () -> {
			try {
				this.addToEmpTable(name, salary, start, gender, connection);
			} catch (EmployeePayrollException e) {
				e.printStackTrace();
			}
			statusMap.put(1, true);
		};
		Thread empThread = new Thread(empTask);
		empThread.start();

		while (statusMap.get(1) == false) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable deptTask = () -> {
				try {
					this.addToDeptTable(name, department, connection);
				} catch (EmployeePayrollException e) {
					e.printStackTrace();
				}
				statusMap.put(2, true);
			};
			Thread DeptThread = new Thread(deptTask);
			DeptThread.start();

			while (statusMap.get(2) == false) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Runnable payrollTask = () -> {
					try {
						this.addToPayrollTable(name, salary, connection);
					} catch (EmployeePayrollException e) {
						e.printStackTrace();
					}
					statusMap.put(3, true);
				};
				Thread payrollThread = new Thread(payrollTask);
				payrollThread.start();

				while (statusMap.get(3) == false) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				id = this.getEmpId(connection);
				employeePayrollData = new EmployeePayrollData(id, name, salary, start, gender);
			}
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR, e.getMessage());
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR,
							e.getMessage());
				}
		}
		return employeePayrollData;

	}

	private int getEmpId(Connection connection) throws EmployeePayrollException {
		String sql = "select MAX(id) from employee_data;";
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(sql);
			resultSet.next();
			return resultSet.getInt(1);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
	}

	private int addToEmpTable(String name, double salary, LocalDate start, String gender, Connection connection)
			throws EmployeePayrollException {
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO employee_data(name, salary, start, gender) VALUES('%s', '%s', '%s', '%s');", name,
					salary, Date.valueOf(start), gender);
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					return resultSet.getInt(1);
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR,
						e.getMessage());
			}
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
		return 0;
	}

	private void addToDeptTable(String name, String department, Connection connection) throws EmployeePayrollException {
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO emp_dept (id,department) VALUES ((select id from employee_data where name='%s'),'%s');",
					name, department);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR,
						e.getMessage());
			}
		}
	}

	private void addToPayrollTable(String name, double salary, Connection connection) throws EmployeePayrollException {
		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format("INSERT INTO payroll_details "
					+ "(id, basic_pay, deductions, taxable_pay, tax, net_pay) VALUES ((select id from employee_data where name='%s'), '%s', '%s', '%s', '%s', '%s')",
					name, salary, deductions, taxablePay, tax, netPay);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.CONNECTION_ERROR,
						e.getMessage());
			}
		}
	}

	public void remove(String name) throws EmployeePayrollException {
		String sql = String.format("UPDATE employee_data SET status = 'inactive' WHERE name = '%s';", name);
		try (Connection connection = getConnection()) {
			Statement statement = connection.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.INVALID_INFO, e.getMessage());
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataForDateRange(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		String sql = String.format(
				"SELECT * FROM employee_data WHERE status = 'active' AND start BETWEEN '%s' AND '%s';",
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
		String sql = String.format("SELECT gender, %s(%s) FROM employee_data WHERE status = 'active' GROUP BY gender;",
				operation, column);
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
