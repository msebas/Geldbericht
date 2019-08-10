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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.io.TempDir;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mcservice.javafx.AdvancedMatcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("DB")
class SQLiteDBTest {
	
	@Test
	void createTablesTest(@TempDir Path tempDir) throws SQLException {
		Path dbFile=tempDir.resolve("tempDb.sql");
		
		DbAbstractionLayer tstObj=new DbAbstractionLayer(dbFile.toString());
		
		VatType vat1=new VatType("Exception","E",new BigDecimal(0),false);
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
	
	@Tag("Active")
	@Test
	void insertVatTypesTest(@TempDir Path tempDir) throws SQLException {
		AdvancedMatcher a1;
		
		a1=new AdvancedMatcher("[0-9]{5}");
		a1.reset("1234");
		assertEquals("1234",a1.completeSquence());
		assertTrue(a1.hitEnd());
		a1.reset("12345");
		assertEquals("12345",a1.completeSquence());
		assertTrue(a1.requireEnd());
		
		a1=new AdvancedMatcher("[0-9]{5}-1[0-9]{1,}");
		a1.reset("1234");
		assertEquals("1234",a1.completeSquence());
		assertTrue(a1.hitEnd());
		a1.reset("12345");
		assertEquals("12345-1",a1.completeSquence());
		assertFalse(a1.requireEnd());
		a1.reset("12345-12");
		assertEquals("12345-12",a1.completeSquence());
		assertFalse(a1.requireEnd());
		
		a1=new AdvancedMatcher("(?!.*a)");
		a1.reset("No little  in this string.");
		//assertTrue(a1.matches());
		
//		
//		Pattern p; 
//		Matcher m;
//		
//		p = Pattern.compile("[0-9]{5}");
//		m=p.matcher("12345");
//		
//		assertTrue(m.matches());
//		assertTrue(!m.requireEnd());
//		
//		p = Pattern.compile("[0-9]{5,}");
//		m=p.matcher("12345");
//		
//		assertTrue(m.matches());
//		assertFalse(m.requireEnd());
//		
		
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
