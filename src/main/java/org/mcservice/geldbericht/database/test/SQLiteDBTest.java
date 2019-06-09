package org.mcservice.geldbericht.database.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.TreeSet;
import org.junit.jupiter.api.io.TempDir;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.SQLiteDB;
import org.junit.jupiter.api.Test;

class SQLiteDBTest {
	
	@Test
	void createTablesTest(@TempDir Path tempDir) throws SQLException {
		Path dbFile=tempDir.resolve("tempDb.sql");
		
		SQLiteDB tstObj=new SQLiteDB(dbFile.toString());
		
		tstObj.checkAndCreateDatabase();
		
		String url = "jdbc:sqlite:"+dbFile.toString();
		Connection connection = DriverManager.getConnection(url);
		
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		TreeSet<String> tables=new TreeSet<String>();
		while (rs.next()) {
		  tables.add(rs.getString(3));
		}
		
		assertTrue(tables.contains("Accounts"));
		assertTrue(tables.contains("Companies"));
		assertTrue(tables.contains("VatTypes"));
	}
	
	@Test
	void insertVatTypesTest(@TempDir Path tempDir) throws SQLException {
		Path dbFile=tempDir.resolve("tempDb.sql");
		
		SQLiteDB tstObj=new SQLiteDB(dbFile.toString());
		
		tstObj.checkAndCreateDatabase();
		
		String url = "jdbc:sqlite:"+dbFile.toString();
		Connection connection = DriverManager.getConnection(url);
		
		PreparedStatement stmt = connection.prepareStatement("SELECT uid, lastChange, name, value FROM VatTypes;");
		ResultSet rs = stmt.executeQuery();
		
		assertFalse(rs.next());
		
		ZonedDateTime now=ZonedDateTime.now();
		VatType vatE=new VatType((long) 0,now,"Exception",0);
		VatType vat1=new VatType(null,now,"Exception",0);
		
		assertThrows(SQLException.class, () -> tstObj.insertVatType(vatE));
		
		VatType resVat=tstObj.insertVatType(vat1);
		rs = stmt.executeQuery();
		assertTrue(rs.next());
		assertEquals(rs.getLong(1), resVat.getUid());
		assertEquals(rs.getTimestamp(2).getTime(), now.toInstant().toEpochMilli());
		assertEquals(rs.getString(3), vat1.getName());
		assertEquals(rs.getDouble(4), vat1.getValue());
		assertEquals(resVat.getName(), vat1.getName());
		assertEquals(resVat.getValue(), vat1.getValue());
		assertEquals(resVat.getLastChange(),now);
				
	}

}
