/**
 * 
 */
package org.mcservice.geldbericht.database;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Sebastian
 *
 */
public class SQLiteDB extends DbAbstractionLayer {

	String filePath=null;
	/**
	 * 
	 */
	public SQLiteDB(String filePath) {
		super();
		this.filePath=filePath;
	}

	@Override
	protected synchronized void connect() throws SQLException {
		if(this.connection!=null) {
			this.close();
		}
		
		File dbFile=new File(this.filePath);
		try {
			dbFile.createNewFile();
			if (!dbFile.canRead() || !dbFile.canWrite()) {
				throw new SQLException("SQLite database file "+dbFile.getAbsolutePath()+" is not read or writeable.");
			}
		} catch (IOException e) {
			throw new SQLException("SQLite database file "+dbFile.getAbsolutePath()+" could not be created.");
		}
		
        String url = "jdbc:sqlite:"+dbFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(url);
	}

	@Override
	protected synchronized void close() throws SQLException {
        if (this.connection != null) {
        	if(this.runningTransaction)
        		this.abortTransaction();
        	this.connection.close();
        	this.connection=null;
        }
	}

	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the account table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	@Override
	protected String getCreateAccountTableStatement() {
		return "CREATE TABLE Accounts ("
				+ "uid INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "company INTEGER, "
				+ "accountNumber VARCHAR(255), "
				+ "accountName  VARCHAR(255)"
				+ ");";
	}

	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the company table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	@Override
	protected String getCreateCompanyTableStatement() {
		return "CREATE TABLE Companies ("
				+ "uid INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "accountCount INT DEFAULT 0 NOT NULL, "
				+ "companyNumber VARCHAR(255), "
				+ "companyName  VARCHAR(255),"
				+ "companyBookkeepingAppointment  VARCHAR(255)"
				+ ");";
	}

	/**
	 * This method needs to return the raw SQL statement that should be used to 
	 * create a prepared statement to create the vat type table.
	 * Please view the source of the abstract class for a MariaDB SQL example 
	 * 
	 * @return The SQL statement as a string.
	 */
	@Override
	protected String getCreateVatTypeTableStatement() {
		return "CREATE TABLE VatTypes ("
				+ "uid INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "lastChange TIMESTAMP WITH TIME ZONE NOT NULL, "
				+ "name VARCHAR(255), "
				+ "value DOUBLE"
				+ ");";
	}
}
