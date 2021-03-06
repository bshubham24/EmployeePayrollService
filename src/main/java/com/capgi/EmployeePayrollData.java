package com.capgi;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class EmployeePayrollData {
	private int id;
	private String name;
	private double salary;
	private LocalDate date;
	private String gender;
	private String department;
	private List<String> departmentList;

	public EmployeePayrollData(Integer id, String name, Double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public EmployeePayrollData(Integer id, String name, Double salary, LocalDate date) {
		this(id, name, salary);
		this.date = date;
	}

	public EmployeePayrollData(Integer id, String name, Double salary, LocalDate date, String gender) {
		this(id, name, salary, date);
		this.gender = gender;
	}

	public EmployeePayrollData(String gender, Double salary) {
		this.gender = gender;
		this.salary = salary;
	}

	public EmployeePayrollData(Integer id, String name, Double salary, LocalDate date, String gender,
			String department) {
		this(id, name, salary, date, gender);
		this.department = department;
	}

	public EmployeePayrollData() {
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setDepartmentList(List<String> departmentList) {
		this.departmentList = departmentList;
	}

	@Override
	public String toString() {
		return "id= " + id + ", name= " + name + ", salary= " + salary;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, gender, salary, date);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		EmployeePayrollData that = (EmployeePayrollData) o;
		return id == that.id && Double.compare(that.salary, salary) == 0 && this.name.contentEquals(that.name)
				&& this.gender.contentEquals(that.gender);
	}
}
