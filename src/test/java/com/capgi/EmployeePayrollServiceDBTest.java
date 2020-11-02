package com.capgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		empPayRollService.updateEmployeeSalary("shubham", 150000.0);
		boolean result = empPayRollService.checkEmployeePayrollInSyncWithDB("shubham");
		assertTrue(result);
	}
}
