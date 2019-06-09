package org.mcservice.geldbericht.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TreeSet;

import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.VatType;

public abstract class DbAbstractionLayer {
	
	protected String userName;
	protected String computer;
	protected Connection connection;
	boolean connectionAutocommitWithoutTransaction=false;
	boolean runningTransaction=false;
	
	public DbAbstractionLayer() {
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
	}
	
	/**
	 * This method should run once for each database during a program execution. 
	 * It checks only if the necessary database tables exist and creates them if 
	 * this is not the case. 
	 */
	public void checkAndCreateDatabase() throws SQLException {
		this.connect();
		DatabaseMetaData md = this.connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		TreeSet<String> tables=new TreeSet<String>();
		while (rs.next()) {
		  tables.add(rs.getString(3));
		}
		
		
		if(!tables.contains("Accounts"))
			this.connection.prepareStatement(this.getCreateAccountTableStatement()).execute();
		if(!tables.contains("Companies"))
			this.connection.prepareStatement(this.getCreateCompanyTableStatement()).execute();
		if(!tables.contains("VatTypes"))
			this.connection.prepareStatement(this.getCreateVatTypeTableStatement()).execute();
		
		this.close();
	}
	
	public VatType insertVatType(VatType vatType) throws SQLException {
		if(null!=vatType.getUid())
			throw new SQLException("Object to insert allready has an Uid.");
		VatType resVatType=null;
		boolean keepConnection=true;
		
		if(this.connection==null) {
			this.connect();
			keepConnection=false;
		}
		try {
			if(!keepConnection)
				this.startTransaction();
								
			String sql=this.getInsertVatTypeStatement();			
			PreparedStatement stmt=this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, Timestamp.from(vatType.getLastChange().toInstant()));
			stmt.setString(2, vatType.getName());
			stmt.setDouble(3, vatType.getValue());
			
			int affectedRows=stmt.executeUpdate();
			if( 0 == affectedRows) 
				throw new SQLException("Creating vat type did fail, no rows inserted.");
			
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                resVatType=new VatType(generatedKeys.getLong(1),vatType.getLastChange(),
	                								vatType.getName(),vatType.getValue());
	            }
	            else {
	                throw new SQLException("Creating vat type failed, no ID obtained.");
	            }
	        }
		
			if(!keepConnection)
				this.commitTransaction();
		} finally {
			if(!keepConnection)
				this.close();
		}
		
		return resVatType;
	}
	
	public Company insertCompany(Company company) throws SQLException {
		if(null!=company.getUid())
			throw new SQLException("Object to insert allready has an Uid.");
		Company resCompany=null;
		boolean keepConnection=true;
		
		if(this.connection==null) {
			this.connect();
			keepConnection=false;
		}
		try {
			if(!keepConnection)
				this.startTransaction();
								
			String sql=this.getInsertCompanyStatement();			
			PreparedStatement stmt=this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, Timestamp.from(company.getLastChange().toInstant()));
			stmt.setInt(2, company.getAccounts().size());
			stmt.setString(3, company.getCompanyNumber());
			stmt.setString(4, company.getCompanyName());
			stmt.setString(5, company.getCompanyBookkeepingAppointment());
			
			int affectedRows=stmt.executeUpdate();
			if( 0 == affectedRows) 
				throw new SQLException("Creating vat type did fail, no rows inserted.");
			
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                resCompany=new Company(generatedKeys.getLong(1),company.getLastChange(),null,
	                								company.getCompanyName(),company.getCompanyNumber(),
	                								company.getCompanyBookkeepingAppointment());
	            }
	            else {
	                throw new SQLException("Creating vat type failed, no ID obtained.");
	            }
	        }
			
			ArrayList<Account> resAccounts=new ArrayList<Account>();
			for (Account account : company.getAccounts()) 
				resAccounts.add(this.insertAccount(account,resCompany));
			
			resCompany=new Company(resCompany.getUid(),resCompany.getLastChange(),resAccounts,
									resCompany.getCompanyName(),resCompany.getCompanyNumber(),
									resCompany.getCompanyBookkeepingAppointment());
		
			if(!keepConnection)
				this.commitTransaction();
		} finally {
			if(!keepConnection)
				this.close();
		}
		
		return resCompany;
	}
		
		
	public Account insertAccount(Account account, Company company) throws SQLException {
		if(null!=account.getUid())
			throw new RuntimeException("ObjectToInsert allready has an Uid.");
		Account resAccount=null;
		boolean keepConnection=true;
		
		if(this.connection==null) {
			this.connect();
			keepConnection=false;
		}
		try {
			if(!keepConnection)
				this.startTransaction();
								
			String sql=this.getInsertAccountStatement();			
			PreparedStatement stmt=this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, Timestamp.valueOf(account.getLastChange().toLocalDateTime()));
			stmt.setString(2, account.getAccountNumber());
			stmt.setString(3, account.getAccountName());
			
			int affectedRows=stmt.executeUpdate();
			if( 0 == affectedRows) 
				throw new SQLException("Creating account did fail, no rows inserted.");
			
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                resAccount=new Account(generatedKeys.getLong(1),account.getLastChange(),
	                								account.getAccountNumber(),account.getAccountName());
	            }
	            else {
	                throw new SQLException("Creating user failed, no ID obtained.");
	            }
	        }
			
			if(!keepConnection)
				this.commitTransaction();
		} finally {
			if(!keepConnection)
				this.close();
		}
		
		return resAccount;
	}
	

	/**
	 * This method should connect to the actual database and throw an exception if this is not possible.
	 * After a call it is assumed that a valid connection exists and is stored in the connection member variable
	 * and transactions could be started. 
	 */
	protected abstract void connect() throws SQLException;
	
	/**
	 * This method should close all possibly open connections to the database and throw an exception if this is not possible.
	 * After a call it is assumed that all non committed transactions are discarded. The connection member should be a 
	 * null value after a successful call to this function.
	 */
	protected abstract void close() throws SQLException;
	
	/**
	 * This method should start a runningTransaction and throw an exception if this is not possible.
	 * After a call it is assumed that all following calls to abstract database altering methods are part of the runningTransaction.  
	 */
	protected synchronized void startTransaction() throws SQLException {
		this.runningTransaction=true;
		this.connectionAutocommitWithoutTransaction=this.connection.getAutoCommit();
		this.connection.setAutoCommit(false);
	}
	
	/**
	 * This method should commit a runningTransaction and throw an exception if this is not possible.
	 * After a successful call it is assumed that all calls to abstract database altering methods done from the 
	 * last call of startTansaction until this call are written to the database.  
	 */
	protected synchronized void commitTransaction() throws SQLException {
		this.connection.commit();
		this.connection.setAutoCommit(this.connectionAutocommitWithoutTransaction);
		this.runningTransaction=false;
	}
	
	/**
	 * This method should abort a runningTransaction and throw an exception if this is not possible.
	 * After a successful call it is assumed that all calls to abstract database altering methods done from the 
	 * call of startTansaction until this call are not written to the database, whatever happens next.  
	 */
	protected synchronized void abortTransaction() throws SQLException {
		this.connection.rollback();
		this.connection.setAutoCommit(this.connectionAutocommitWithoutTransaction);
		this.runningTransaction=false;
	}
	
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the account table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getCreateAccountTableStatement() {
		return "CREATE TABLE Accounts ("
				+ "uid BIGINT NOT NULL AUTO_INCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "company BIGINT NOT NULL, "
				+ "accountNumber VARCHAR(255), "
				+ "accountName  VARCHAR(255),"
				+ "PRIMARY KEY (uid)"
				+ ");";
	}
	
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the company table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getCreateCompanyTableStatement() {
		return "CREATE TABLE Companies ("
				+ "uid BIGINT NOT NULL AUTO_INCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "accountCount INT DEFAULT 0 NOT NULL, "
				+ "companyNumber VARCHAR(255), "
				+ "companyName  VARCHAR(255),"
				+ "companyBookkeepingAppointment  VARCHAR(255),"
				+ "PRIMARY KEY (uid)"
				+ ");";
	}
	
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the vat type table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getCreateVatTypeTableStatement() {
		return "CREATE TABLE VatTypes ("
				+ "uid BIGINT NOT NULL AUTO_INCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "name VARCHAR(255), "
				+ "value DOUBLE,"
				+ "PRIMARY KEY (uid)"
				+ ");";
	}
		
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to insert an account.
	 * Please view the source of the abstract class for a standard SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getInsertAccountStatement() {
		return "INSERT INTO Accounts(lastChange, company, accountNumber, accountName) VALUES (?,?,?,?);";
	}
	

	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to insert an account.
	 * Please view the source of the abstract class for a standard SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getInsertCompanyStatement() {
		return "INSERT INTO Company(lastChange, accountCount, companyNumber, companyName, companyBookkeepingAppointment) VALUES (?,?,?,?,?);";
	}
	
	/*
	protected String getInsertMonthAccountTurnoverStatement();
	protected String getInsertTransactionStatement();
	*/
	
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to insert an account.
	 * Please view the source of the abstract class for a standard SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	protected String getInsertVatTypeStatement() {
		return "INSERT INTO VatTypes(lastChange, name, value) VALUES (?,?,?);";
	}
	
	/*
	protected String getInsertLogEntryStatement();
	
	protected String getUpdateAccountByUidStatement();
	protected String getUpdateCompanyByUidStatement();
	protected String getUpdateMonthAccountTurnoverByUidStatement();
	protected String getUpdateTransactionByUidStatement();
	protected String getUpdateVatTypeByUidStatement();
	
	protected String getDeleteAccountByUidStatement();
	protected String getDeleteCompanyByUidStatement();
	protected String getDeleteMonthAccountTurnoverByUidStatement();
	protected String getDeleteTransactionByUidStatement();
	protected String getDeleteVatTypeByUidStatement();
	*/
	
	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to select accounts by a property.
	 * Please view the source of the abstract class for a standard SQL example
	 *  
	 * @param property The name of the property that is used to select accounts
	 * @return The SQL statement as a string.
	 */
	protected String getSelectAccountsByPropertyStatement(String property) {
		return "SELECT uid,lastChange,accountNumber,accountName FROM Accounts WHERE "+property+"=?;";
	}
	
	/*
	protected String getSelectAccountByPropertyStatement();
	protected String getSelectCompanyByPropertyStatement();
	protected String getSelectMonthAccountTurnoverByPropertyStatement();
	protected String getSelectTransactionByPropertyStatement();
	protected String getSelectVatTypeByPropertyStatement();
	*/
}
