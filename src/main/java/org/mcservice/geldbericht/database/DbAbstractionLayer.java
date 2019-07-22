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
package org.mcservice.geldbericht.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.VatType;

public class DbAbstractionLayer {
	
	protected String userName;
	protected String computer;
	protected Connection connection;
	protected ServiceRegistry serviceRegistry;
	protected SessionFactory factory;
	boolean connectionAutocommitWithoutTransaction=false;
	boolean runningTransaction=false;
	
	public DbAbstractionLayer(String dbPath) {
		this.userName=System.getProperty("user.name");
		if(System.getenv().containsKey("COMPUTERNAME"))
			this.computer=System.getenv().get("COMPUTERNAME");
		else if(System.getenv().containsKey("HOSTNAME"))
			this.computer=System.getenv().get("HOSTNAME");
		else {
			try	{
			    InetAddress addr;
			    addr = InetAddress.getLocalHost();
			    this.computer = addr.getHostName();
			} catch (UnknownHostException ex) {
			    this.computer="Unknown";
			}
		}
		
		try {
			StandardServiceRegistryBuilder builder=new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml");
			
			if(null!=dbPath)
				builder.applySetting("hibernate.connection.url", "jdbc:sqlite:"+dbPath);
			
            ServiceRegistry serviceRegistry = builder.build();
            
            
            Metadata metadata = new MetadataSources(serviceRegistry).getMetadataBuilder().build();
            factory=(SessionFactory) metadata.getSessionFactoryBuilder().build();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
	}
	
	public List<VatType> manageVatTypes(List<VatType> vatTypes, ZonedDateTime lastUpdate) {
		return getVatTypes();
	}
	
	public List<Account> manageAccounts(List<Account> accounts, ZonedDateTime lastUpdate) {
		return getAccounts();
	}
	
	public List<Company> manageCompanies(List<Company> companies, ZonedDateTime lastUpdate) {
		return getCompanies();
	}
	

	public List<Account> getAccounts(){
		return getAllData(Account.class);
	}

	public List<Company> getCompanies(){
		return getAllData(Company.class);
	}	

	public List<MonthAccountTurnover> getMonthAccountTurnovers(){
		return getAllData(MonthAccountTurnover.class);
	}
	
	public List<Transaction> getTransactions(){
		return getAllData(Transaction.class);
	}
	
	public List<VatType> getVatTypes(){
		return getAllData(VatType.class);
	}
	
	public Account persistAccount(Account account) {
		return persist(account, Account.class);
	}
	
	public Company persistCompany(Company company) {
		return persist(company, Company.class);
	}
	
	public MonthAccountTurnover persistMonthAccountTurnover(MonthAccountTurnover monthAccountTurnover) {
		return persist(monthAccountTurnover, MonthAccountTurnover.class);
	}

	public Transaction persistTransaction(Transaction transaction) {
		return persist(transaction, Transaction.class);
	}
	
	public VatType persistVatType(VatType vatType) {
		return persist(vatType, VatType.class);
	}
	
	public Account updateAccount(Account account) {
		return merge(account, Account.class);
	}
	
	public Company updateCompany(Company company) {
		return merge(company, Company.class);
	}
	
	public MonthAccountTurnover updateMonthAccountTurnover(MonthAccountTurnover monthAccountTurnover) {
		return merge(monthAccountTurnover, MonthAccountTurnover.class);
	}

	public Transaction updateTransaction(Transaction transaction) {
		return merge(transaction, Transaction.class);
	}
	
	public VatType updateVatType(VatType vatType) {
		return merge(vatType, VatType.class);
	}
	
	protected <T extends AbstractDataObject> T persist(T data, Class<T> type){
		T result=null;
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		
		try {
			Long uid=(Long) session.save(data);
			result=session.byId(type).getReference(uid);
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
		
		return result;
	}
	
	protected <T extends AbstractDataObject> List<T> getAllData(Class<T> type){
		Session session=factory.openSession();
		try {
			CriteriaBuilder cb = session.getCriteriaBuilder();
		    CriteriaQuery<T> cq = cb.createQuery(type);
		    Root<T> rootEntry = cq.from(type);
		    CriteriaQuery<T> all = cq.select(rootEntry);
	
		    Query<T> allQuery = session.createQuery(all);
		    return allQuery.getResultList();
		} finally {
			session.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends AbstractDataObject> T merge(T data, Class<T> type){
		T result=null;
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		
		try {
			result = (T) session.merge(data);
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
		
		return result;
	}
}
