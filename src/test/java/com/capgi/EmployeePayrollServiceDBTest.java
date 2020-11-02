package com.capgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.capgi.EmployeePayrollService.IOService;

public class EmployeePayrollServiceDBTest {
	@Test
	public void whenRecordsAreRetrievedFromEmpDatabaseShouldMatchTheEmpCount() throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData(IOService.DB_IO);
		assertEquals(4, employeePayrollData.size());
	}

	@Test
	public void whenSalaryIsUpdated_ShouldMatchInDBAndList() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		List<EmployeePayrollData> empPayrollList = empPayRollService.readEmployeePayrollData(IOService.DB_IO);
		empPayRollService.updateEmployeeSalary("anant", 100000.0);
		boolean result = empPayRollService.checkEmployeePayrollInSyncWithDB("anant");
		assertTrue(result);
	}

	@Test
	public void whenDataForDateRangeIsRetrieved_ShouldReturnEmpCount() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		LocalDate startDate = LocalDate.of(2020, 9, 16);
		LocalDate endDate = LocalDate.now();
		List<EmployeePayrollData> empPayrollList = empPayRollService.getEmployeePayrollDataForDateRange(startDate,
				endDate);
		assertEquals(2, empPayrollList.size());
	}

	@Test
	public void givenDBFindSumOfSalaryOfMale_shouldReturnSum() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		List<EmployeePayrollData> empPayrollList = empPayRollService.getEmpDataGroupedByGender("basic_pay", "SUM", "M");
		double sum = empPayrollList.get(0).getSalary();
		assertEquals(250000.00, sum, 0);
	}

	@Test
	public void givenDBFindAvgOfSalaryOfMale_shouldReturnSum() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		List<EmployeePayrollData> empPayrollList = empPayRollService.getEmpDataGroupedByGender("basic_pay", "Avg", "M");
		double sum = empPayrollList.get(0).getSalary();
		assertEquals(125000.00, sum, 0);
	}

	@Test
	public void givenDBFindMaxOfSalaryOfMale_shouldReturnSum() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		List<EmployeePayrollData> empPayrollList = empPayRollService.getEmpDataGroupedByGender("basic_pay", "MAX", "M");
		double sum = empPayrollList.get(0).getSalary();
		assertEquals(150000.00, sum, 0);
	}

	@Test
	public void givenDBFindMinOfSalaryOfFemale_shouldReturnSum() throws EmployeePayrollException {
		EmployeePayrollService empPayRollService = new EmployeePayrollService();
		List<EmployeePayrollData> empPayrollList = empPayRollService.getEmpDataGroupedByGender("basic_pay", "MIN", "F");
		String gender = empPayrollList.get(1).getGender();
		double sum = empPayrollList.get(1).getSalary();
		assertEquals(42000, sum, 0);
		assertEquals("F", gender);
	}
}
