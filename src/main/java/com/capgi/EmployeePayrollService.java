package com.capgi;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeePayrollService {

	private List<EmployeePayrollData> employeePayrollList;

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private EmployeePayrollDBService employeePayrollDBService;

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	private void readEmployeePayrollData(Scanner consoleInputReader) {
		System.out.print("Enter Employee ID: ");
		int id = consoleInputReader.nextInt();
		System.out.print("Enter Employee Name: ");
		String name = consoleInputReader.next();
		System.out.print("Enter Employee Salary: ");
		double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("\nWriting Payroll to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeData(employeePayrollList);

	}

	public long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		else if (ioService.equals(IOService.DB_IO))
			return employeePayrollList.size();
		else
			return 0;
	}

	public void printData(IOService ioService) {
		new EmployeePayrollFileIOService().printData();
	}

	public static void main(String[] args) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}

	public List<EmployeePayrollData> readData(IOService fileIo) {
		if (fileIo.equals(IOService.FILE_IO)) {
			return new EmployeePayrollFileIOService().readData();
		} else
			return null;
	}

	public List<EmployeePayrollData> readEmployeePayrollData(IOService dbIo) throws EmployeePayrollException {
		employeePayrollList = new ArrayList<EmployeePayrollData>();
		employeePayrollList = employeePayrollDBService.readData();
		return employeePayrollList;
	}

	public void updateEmployeeSalary(String name, double salary) throws EmployeePayrollException {
		int result = employeePayrollDBService.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.setSalary(salary);

	}

	public boolean updateEmployeeSalaryUsingThreads(List<EmployeePayrollData> empPayrollDataList)
			throws EmployeePayrollException {
		Map<Integer, Boolean> empUpdateStatus = new HashMap<Integer, Boolean>();
		empPayrollDataList.forEach(employeePayrollData -> {
			empUpdateStatus.put(employeePayrollData.hashCode(), false);
			Runnable task = () -> {
				try {
					employeePayrollDBService.updateEmployeeSalary(employeePayrollData.getName(),
							employeePayrollData.getSalary());
				} catch (EmployeePayrollException e) {
					e.printStackTrace();
				}
				empUpdateStatus.put(employeePayrollData.hashCode(), true);
			};
			Thread thread = new Thread(task, employeePayrollData.getName());
			thread.start();
		});
		while (empUpdateStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.THREAD_FAILURE,
						e.getMessage());
			}
		}
		if (empUpdateStatus.containsValue(false))
			return false;
		return true;
	}

	public void addEmployeeToPayroll(String name, double salary, LocalDate start, String gender, String dept)
			throws EmployeePayrollException, SQLException {
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(name, salary, start, gender, dept));
	}

	public void addEmployeeToPayroll(List<EmployeePayrollData> empPayrollDataList) {
		empPayrollDataList.forEach(empPayrollData -> {
			System.out.println("Emp being Added : " + empPayrollData.getName());
			try {
				this.addEmployeeToPayroll(empPayrollData.getName(), empPayrollData.getSalary(),
						empPayrollData.getDate(), empPayrollData.getGender(), empPayrollData.getDepartment());
			} catch (EmployeePayrollException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Emp Added : " + empPayrollData.getName());
		});
		System.out.println(this.employeePayrollList);
	}

	public void addEmpToPayrollWithThreads(List<EmployeePayrollData> empPayrollDataList)
			throws EmployeePayrollException {
		Map<Integer, Boolean> employeeStatusMap = new HashMap<Integer, Boolean>();
		empPayrollDataList.forEach(employeePayrollData -> {
			employeeStatusMap.put(employeePayrollData.hashCode(), false);
			Runnable task = () -> {
				System.out.println("Employee Being Added : " + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayroll(employeePayrollData.getName(), employeePayrollData.getSalary(),
							employeePayrollData.getDate(), employeePayrollData.getGender(),
							employeePayrollData.getDepartment());
				} catch (EmployeePayrollException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				employeeStatusMap.put(employeePayrollData.hashCode(), true);
				System.out.println("Employee Added : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.getName());
			thread.start();
		});
		while (employeeStatusMap.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new EmployeePayrollException(EmployeePayrollException.ExceptionType.THREAD_FAILURE,
						e.getMessage());
			}
		}
		System.out.println(this.employeePayrollList);
	}

	public void remove(String name) throws EmployeePayrollException {
		employeePayrollDBService.remove(name);
	}

	public List<EmployeePayrollData> getEmployeePayrollDataForDateRange(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		return employeePayrollDBService.getEmployeePayrollDataForDateRange(startDate, endDate);
	}

	private EmployeePayrollData getEmployeePayrollData(String name) {
		EmployeePayrollData employeePayrollData;
		employeePayrollData = this.employeePayrollList.stream().filter(employee -> employee.getName().equals(name))
				.findFirst().orElse(null);
		return employeePayrollData;
	}

	public List<EmployeePayrollData> getEmpDataGroupedByGender(String column, String operation, String gender)
			throws EmployeePayrollException {
		return employeePayrollDBService.getEmpDataGroupedByGender(column, operation, gender);

	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

}
