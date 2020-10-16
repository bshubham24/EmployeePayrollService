package com.capgi;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class EmployeePayrollServiceTest {
	@Test
	public void given3EmployeesWhenWrittenToFileShouldMatchNumberOfEmployeeEntries() {
		EmployeePayrollData[] arrayOfEmployees = { new EmployeePayrollData(1, "Bill gates", 1400.0),
				new EmployeePayrollData(2, "Shubham Bhawsar", 1900.0),
				new EmployeePayrollData(3, "Elon Musk", 1500.0) };
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		empPayrollService.writeEmployeePayrollData(EmployeePayrollService.IOService.FILE_IO);
		Assert.assertEquals(3, empPayrollService.countEntries(EmployeePayrollService.IOService.FILE_IO));
	}
}
