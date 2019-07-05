/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CompanyProperty {
	
	protected Company company=null;
	protected StringProperty companyName=null;
	protected StringProperty companyNumber=null;
	protected StringProperty companyBookkeepingAppointment=null;

	public CompanyProperty(Company company) {
		this.company = company;
		companyName = new SimpleStringProperty(company.getCompanyName());
		companyNumber = new SimpleStringProperty(company.getCompanyNumber());
		companyBookkeepingAppointment = new SimpleStringProperty(company.getCompanyBookkeepingAppointment());		
	}
	
	public Company updateCompany() {
		company.setCompanyName(companyName.get());
		company.setCompanyNumber(companyNumber.get());
		company.setCompanyBookkeepingAppointment(companyBookkeepingAppointment.get());
		return company;
	}

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return this.company.getUid();
	}

	/**
	 * @return the companyName
	 */
	public String getCompanyName() {
		return companyName.get();
	}

	/**
	 * @return the companyNumber
	 */
	public String getCompanyNumber() {
		return companyNumber.get();
	}

	/**
	 * @return the companyBookkeepingAppointment
	 */
	public String getCompanyBookkeepingAppointment() {
		return companyBookkeepingAppointment.get();
	}

	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName) {
		this.companyName.set(companyName);
	}

	/**
	 * @param companyNumber the companyNumber to set
	 */
	public void setCompanyNumber(String companyNumber) {
		this.companyNumber.set(companyNumber);
	}

	/**
	 * @param companyBookkeepingAppointment the companyBookkeepingAppointment to set
	 */
	public void setCompanyBookkeepingAppointment(String companyBookkeepingAppointment) {
		this.companyBookkeepingAppointment.set(companyBookkeepingAppointment);
	}

}
