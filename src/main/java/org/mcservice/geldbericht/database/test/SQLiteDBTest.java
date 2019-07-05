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
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.junit.jupiter.api.Test;

class SQLiteDBTest {
	
	@Test
	void createTablesTest(@TempDir Path tempDir) throws SQLException {
		Path dbFile=tempDir.resolve("tempDb.sql");
		
		DbAbstractionLayer tstObj=new DbAbstractionLayer(dbFile.toString());
		
		VatType vat1=new VatType("Exception",0,false);
		VatType vat2=tstObj.persistVatType(vat1);
		
		//tstObj.checkAndCreateDatabase();
		
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
		assertTrue(tables.contains("MonthAccountTurnovers"));
		assertTrue(tables.contains("Transactions"));
		assertTrue(tables.contains("VatTypes"));
		assertNotNull(vat2.getUid());
	}
	
	@Test
	void insertVatTypesTest(@TempDir Path tempDir) throws SQLException {
		/*
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
			*/	
	}

}
