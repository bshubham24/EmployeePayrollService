package com.capgi;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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

	private Response addEmpToJsonServer(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	private Response updateEmployeeSalary(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.put("/employees/" + employeePayrollData.getId());
	}

	private Response deleteEmployee(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.delete("/employees/" + employeePayrollData.getId());
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));
		long entries = empPayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(2, entries);
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseAndCount()
			throws EmployeePayrollException, SQLException {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));
		EmployeePayrollData employeePayrollData = new EmployeePayrollData(0, "kiara", 8000.0);
		Response response = addEmpToJsonServer(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
		empPayrollService.addEmployeeToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
		long entries = empPayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}

	@Test
	public void givenListOfEmployees_WhenAdded_ShouldMatch201ResponseAndCount()
			throws EmployeePayrollException, SQLException {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));

		EmployeePayrollData[] arrOfEmpPayroll = { new EmployeePayrollData(0, "Ananya", 3000.0),
				new EmployeePayrollData(0, "Virat", 9000.0) };
		for (EmployeePayrollData employeePayrollData : arrOfEmpPayroll) {
			Response response = addEmpToJsonServer(employeePayrollData);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);

			employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
			empPayrollService.addEmployeeToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
		}
		long entries = empPayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(7, entries);
	}

	@Test
	public void givenEmployeeSalary_WhenUpdated_ShouldMatch200Response() throws EmployeePayrollException {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));

		empPayrollService.updateEmployeeSalary("Ananya", 8000.0, EmployeePayrollService.IOService.REST_IO);
		EmployeePayrollData employeePayrollData = empPayrollService.getEmployeePayrollData("Ananya");
		Response response = updateEmployeeSalary(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
	}

	@Test
	public void givenEmployeeData_WhenDeleted_ShouldMatch200Response() {
		EmployeePayrollData[] arrOfEmp = getEmpList();
		EmployeePayrollService empPayrollService;
		empPayrollService = new EmployeePayrollService(Arrays.asList(arrOfEmp));

		EmployeePayrollData employeePayrollData = empPayrollService.getEmployeePayrollData("Ananya");
		Response response = deleteEmployee(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);

		empPayrollService.deleteEmployee("Virat", EmployeePayrollService.IOService.REST_IO);
		long entries = empPayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}
}
