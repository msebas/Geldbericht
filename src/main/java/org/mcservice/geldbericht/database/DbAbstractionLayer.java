/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.User;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.AbstractDataObject.AbstractDataObjectDatabaseQueueEntry;

public class DbAbstractionLayer {
	
	protected Connection connection;
	protected ServiceRegistry serviceRegistry;
	protected SessionFactory factory;
	boolean connectionAutocommitWithoutTransaction=false;
	boolean runningTransaction=false;
	
	public DbAbstractionLayer() {
		String propertiesFile="connection.cfg.xml";
		if(System.getenv("GELDBERICHT_CONFIGFILE") != null) {
			propertiesFile=System.getenv("GELDBERICHT_CONFIGFILE");
		}
		init(propertiesFile);
	}
	
	public DbAbstractionLayer(String propertiesFile) {
		init(propertiesFile);
	}
	
	private void init(String propertiesFile) {
		try {
			StandardServiceRegistryBuilder builder=new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml");
			
			if((new File(propertiesFile)).isFile()) {
				Properties properties = new Properties();
				properties.load(new FileInputStream(propertiesFile));
				
				Configuration configuration = new Configuration();
				configuration.configure(new File(propertiesFile));
				
				builder.applySettings(configuration.getProperties());
			} else {
				throw new FileNotFoundException(
						String.format("Unable to find database connection configuration file \"%s\"",propertiesFile));
			}
			

			ServiceRegistry serviceRegistry = builder.build();
                        
            Metadata metadata = new MetadataSources(serviceRegistry).getMetadataBuilder().build();
            factory=(SessionFactory) metadata.getSessionFactoryBuilder().build();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
	}
	
	

	public List<User> manageUsers(List<User> users, ZonedDateTime lastUpdate) {
		return mergeAll(users,User.class);
	}
	
	public List<VatType> manageVatTypes(List<VatType> vatTypes, ZonedDateTime lastUpdate) {
		return mergeAll(vatTypes,VatType.class);
	}
	
	public List<Account> manageAccounts(List<Account> accounts, ZonedDateTime lastUpdate) {
		return mergeAll(accounts,Account.class);
	}
	
	public List<Company> manageCompanies(List<Company> companies, ZonedDateTime lastUpdate) {
		return mergeAll(companies,Company.class);
	}	

	public List<Account> getAccounts(){
		return getAllData(Account.class);
	}

	public List<Company> getCompanies(){
		return getAllData(Company.class);
	}	

	public List<User> getUsers(){
		return getAllData(User.class);
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
	
	public List<VatType> getVatTypes(boolean includeDisabled){
		Session session=factory.openSession();
		try {
			CriteriaBuilder cb = session.getCriteriaBuilder();
		    CriteriaQuery<VatType> cq = cb.createQuery(VatType.class);
		    Root<VatType> rootEntry = cq.from(VatType.class);
		    CriteriaQuery<VatType> criteria = cq.select(rootEntry);
		    if(!includeDisabled) {
		    	criteria=criteria.where(cb.equal(rootEntry.get("disabledVatType"), Boolean.valueOf(false)));
		    }
	
		    Query<VatType> allQuery = session.createQuery(criteria);
		    return allQuery.getResultList();
		} finally {
			session.close();
		}
	}
	
	public User persistUser(User user) {
		return persist(user, User.class);
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
	
	public User updateUser(User user) {
		return merge(user, User.class);
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
	
	public void loadAccountsToCompany(Company company){
		if(company.isAccountsLoaded()) {
			return;
		}
		
		Session session=factory.openSession();
		try {
			session.refresh(company);
			Hibernate.initialize(company.getAccounts());
		} finally {
			session.getTransaction().rollback();
			session.close();
		}
	}

	public void loadMonthsToAccount(Account account){
		if(account.isMonthsLoaded()) {
			return;
		}
		
		Session session=factory.openSession();
		try {
			session.refresh(account);
			Hibernate.initialize(account.getBalanceMonths());
		} finally {
			session.getTransaction().rollback();
			session.close();
		}
	}

	public void loadTransactionsToMonth(MonthAccountTurnover month){
		if(month.isTransactionsLoaded()) {
			return;
		}

		Session session=factory.openSession();
		try {
			session.refresh(month);
			Hibernate.initialize(month.getTransactions());
		} finally {
			session.getTransaction().rollback();
			session.close();
		}
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
	
	protected <T extends AbstractDataObject> T merge(T data, Class<T> type){
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		
		try {
			if(data.getUid()==null) {
				Long uid=(Long) session.save(data);
				data=session.byId(type).getReference(uid);
			} else {
				session.merge(data);
			}
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
		
		return data;
	}
	
	protected <T extends AbstractDataObject> List<T> mergeAll(List<T> data, Class<T> type){
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		
		try {
			for (int i=0;i<data.size();++i) {
				if(data.get(i).getUid()==null) {
					Long uid=(Long) session.save(data.get(i));
					data.set(i,session.byId(type).getReference(uid));
				} else {
					session.merge(data.get(i));
				}
			}
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
		
		return data;
	}
	
	public void deleteData(Collection<? extends AbstractDataObject> dataList) {
		recursiveLoadData(dataList);
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		try {
			deleteData(dataList,session);
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
	}
	
	public void deleteData(Collection<? extends AbstractDataObject> dataList, Session session) {
		for(AbstractDataObject obj : dataList) {
			if(obj instanceof Company) {
				deleteData(((Company) obj).getAccounts(), session);
				((Company) obj).getAccounts().clear();
			} else if(obj instanceof Account) {
				deleteData(((Account) obj).getBalanceMonths(), session);
				((Account) obj).getBalanceMonths().clear();
			} else if(obj instanceof MonthAccountTurnover) {
				deleteData(((MonthAccountTurnover) obj).getTransactions(), session);
				((MonthAccountTurnover) obj).getTransactions().clear();
			}
			if(null != obj.getUid()) {
				session.delete(obj);
			}
		}
	}
	
	protected void recursiveLoadData(Collection<? extends AbstractDataObject> dataList) {
		for(AbstractDataObject obj : dataList) {
			if(obj instanceof Company) {
				loadAccountsToCompany((Company) obj);
				recursiveLoadData(((Company) obj).getAccounts());
			} else if(obj instanceof Account) {
				loadMonthsToAccount((Account) obj);
				Hibernate.initialize(((Account) obj).getBalanceMonths());
				recursiveLoadData(((Account) obj).getBalanceMonths());
			} else if(obj instanceof MonthAccountTurnover) {
				loadTransactionsToMonth((MonthAccountTurnover) obj);
			}
		}
	}
	
	public void mergeDataPersistanceQueue(List<AbstractDataObjectDatabaseQueueEntry> dataList) {
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		try {
			for (AbstractDataObjectDatabaseQueueEntry queueEntry : dataList) {
				if(null==queueEntry)
					continue;
				AbstractDataObject obj = queueEntry.getStateToPersist();
				if(queueEntry.isCreate()) {
					Long uid=(Long) session.save(obj);
					obj=session.byId(obj.getClass()).getReference(uid);
				} else if (queueEntry.isMerge()){
					session.merge(obj);
				} else if (queueEntry.isDelete() && null!=obj.getUid()) {
					session.delete(obj);
				}
				queueEntry.applyPersistedState(obj);
			}
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
	}
	
	public List<? extends AbstractDataObject> recursiveMergeData(List<? extends AbstractDataObject> dataList) {
		//recursiveLoadData(dataList);
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		try {
			//Here we have to persist first anything to that things persisted later hold a reference to.
			//The simplest approach is to walk down the relation graph, but to do it this was is quite slow...
			internalMergeData(dataList, session);
			
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
		return dataList;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void internalMergeData(List<? extends AbstractDataObject> dataList, Session session) {
		for(int i=0;i<dataList.size();++i) {
			AbstractDataObject obj=dataList.get(i);
			if(obj instanceof Company) {
				internalMergeData(((Company) obj).getAccounts(), session);
			} else if(obj instanceof Account) {
				Account act=(Account) obj;
				if(act.isMonthsLoaded())
					internalMergeData(act.getBalanceMonths(), session);
			} else if(obj instanceof MonthAccountTurnover) {
				MonthAccountTurnover month = ((MonthAccountTurnover) obj);
				if(month.isTransactionsLoaded())
					internalMergeData(month.getTransactions(), session);
			}
			if(null==obj.getUid()) {
				Long uid=(Long) session.save(obj);
				//This rawtype solution is secure, because the returned type is guaranteedeed to 
				//be the type of the original object by hibernate. Otherwise this would be a
				//typical casting problem...
				List l=dataList;
				l.set(i,(Object) session.byId(dataList.get(i).getClass()).getReference(uid));
			} else {
				session.merge(obj);
			}
		}
	}
	

	public void remove(MonthAccountTurnover monthAccountTurnover) {
		if(null==monthAccountTurnover.getUid()) {
			monthAccountTurnover.getAccount().getBalanceMonths().remove(monthAccountTurnover);
			monthAccountTurnover.getAccount().updateBalance();
			return;
		}
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		try {
			for(Transaction balanceTransaction:monthAccountTurnover.getTransactions()) {
				session.delete(balanceTransaction);
			}
			monthAccountTurnover.getAccount().getBalanceMonths().remove(monthAccountTurnover);
			monthAccountTurnover.getAccount().updateBalance();
			session.delete(monthAccountTurnover);
			session.save(monthAccountTurnover.getAccount());
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
	}
	
	public void remove(Transaction balanceTransaction) {
		if(null==balanceTransaction.getUid())
			return;
		Session session=factory.getCurrentSession();
		org.hibernate.Transaction transaction=session.beginTransaction();
		try {
			session.delete(balanceTransaction);
			transaction.commit();
			transaction=null;
		} finally {
			if(null!=transaction) {
				transaction.rollback();
			}
			session.close();
		}
	}

	public List<User> getUserByName(String userName) {
		Session session=factory.openSession();
		try {
			CriteriaBuilder cb = session.getCriteriaBuilder();
		    CriteriaQuery<User> cq = cb.createQuery(User.class);
		    Root<User> rootEntry = cq.from(User.class);
		    CriteriaQuery<User> criteria = cq.select(rootEntry);
	    	criteria=criteria.where(cb.equal(rootEntry.get("userName"), userName));
		    
		    Query<User> allQuery = session.createQuery(criteria);
		    return allQuery.getResultList();
		} finally {
			session.close();
		}
	}
}
