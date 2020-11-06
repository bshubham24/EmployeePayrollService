package com.capgi;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class EmployeePayrollServiceRestAPITest {
	@Before
	public void initialize() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	private EmployeePayrollData[] getEmpList() {
		Response response = RestAssured.get("/employees");
		EmployeePayrollData[] arrOfEmp = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrOfEmp;
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));
		long entries = empPayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(2, entries);
	}
}
