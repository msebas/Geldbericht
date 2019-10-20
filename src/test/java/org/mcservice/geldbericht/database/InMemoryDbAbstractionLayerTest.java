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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.mockito.quality.Strictness;
import org.mcservice.MockedApplicationTest;
import org.mcservice.geldbericht.data.AbstractDataObject;
import org.mcservice.geldbericht.data.Account;
import org.mcservice.geldbericht.data.Company;
import org.mcservice.geldbericht.data.MonthAccountTurnover;
import org.mcservice.geldbericht.data.Transaction;
import org.mcservice.geldbericht.data.User;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.database.DbAbstractionLayer;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

//@Tag("Active")
@Tag("DB")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class InMemoryDbAbstractionLayerTest {
	
	TreeMap<String,String> sysenv=new TreeMap<String,String>(System.getenv());
	
	@BeforeEach 
	public void initDatabase(TestInfo testInfo) throws Exception{
		URL data=this.getClass().getClassLoader().getResource("connection.cfg.xml");
    	Map<String,String> newenv=new TreeMap<String,String>(sysenv);
    	newenv.put("GELDBERICHT_CONFIGFILE",data.getPath());
    	MockedApplicationTest.setEnv(newenv);
		
		//Create the database as required by the test.
		
		
	}
	
	@Test
	public void testConstructorNoFileExists() {
		try{
			new DbAbstractionLayer("ThisDoesNotExist.xml");
		} catch (ExceptionInInitializerError e){
			assertTrue(e.getCause() instanceof FileNotFoundException);
			assertTrue(e.getCause().getMessage().contains("ThisDoesNotExist.xml"));
		}
	}
	
	@Test
	public void testConstructorNoFileDefault() throws Exception {
		Map<String,String> newenv=new TreeMap<String,String>(sysenv);
    	newenv.clear();
    	MockedApplicationTest.setEnv(newenv);
		try{
			new DbAbstractionLayer();
		} catch (ExceptionInInitializerError e){
			assertTrue(e.getCause() instanceof FileNotFoundException);
			assertTrue(e.getCause().getMessage().contains("connection.cfg.xml"));
		}
	}
	
	@Test
	public void testConstructorNoFileSysVar() throws Exception {
		Map<String,String> newenv=new TreeMap<String,String>(sysenv);
    	newenv.clear();
    	newenv.put("GELDBERICHT_CONFIGFILE","ThisIsSomeStangePath.cfg.xml");
    	MockedApplicationTest.setEnv(newenv);
		try{
			new DbAbstractionLayer();
		} catch (ExceptionInInitializerError e){
			assertTrue(e.getCause() instanceof FileNotFoundException);
			assertTrue(e.getCause().getMessage().contains("ThisIsSomeStangePath.cfg.xml"));
		}
	}

	@Test
	public void testPersistAccount() throws Exception {
		Account account=new Account(1L,ZonedDateTime.now(),null,null,null,null);		
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistAccount(account);
				
		Account tmpAccount=db.getAccounts().get(0);
		db.loadMonthsToAccount(tmpAccount);
		assertTrue(account.equals(tmpAccount, false));
	}

	@Test
	public void testPersistUser() throws Exception {
		User User=new User(1L,ZonedDateTime.now(),"username",null);		
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistUser(User);
		
		assertEquals(User,db.getUserByName("username").get(0));
	}
	
	@Test
	public void testPersistCompany() throws Exception {
		Company company=new Company(1L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null);		
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistCompany(company);
		Company persCompany=db.getCompanies().get(0);
		
		db.loadAccountsToCompany(persCompany);
		assertTrue(company.equals(persCompany,true));
	}

	@Test
	public void testPersistMonthAccountTurnover() throws Exception {
		MonthAccountTurnover month=new MonthAccountTurnover(1L,ZonedDateTime.now(),
				new ArrayList<Transaction>(),null,null,null,null,null,null,null,null);		
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistMonthAccountTurnover(month);
		MonthAccountTurnover persMonth=db.getMonthAccountTurnovers().get(0);
		
		db.loadTransactionsToMonth(persMonth);
		assertEquals(month,persMonth);
	}	

	@Test
	public void testPersistTransaction() throws Exception {
		Transaction transaction=new Transaction(1L,ZonedDateTime.now(), 0,
				null,null,null,null,null,null,null,null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistTransaction(transaction);
		Transaction persTransactions=db.getTransactions().get(0);
		
		assertEquals(transaction,persTransactions);
	}

	@Test
	public void testPersistVatType() throws Exception {
		VatType vatType=new VatType(1L,ZonedDateTime.now(), null,null, null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.persistVatType(vatType);
		VatType persVatTypes=db.getVatTypes().get(0);
		
		assertEquals(vatType,persVatTypes);
	}

	@Test
	public void testManageVatType() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		VatType vatType=new VatType(null,act, null,null, null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		VatType persVatTypes=db.manageVatTypes(new ArrayList<VatType>(List.of(vatType)),act).get(0);
		vatType=new VatType(1L,act, null,null, null, null, null);
		
		assertEquals(vatType,persVatTypes);
	}

	@Test
	public void testManageAccount() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		Account Account=new Account(null,act, null,null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		Account persAccounts=db.manageAccounts(new ArrayList<Account>(List.of(Account)),act).get(0);
		Account=new Account(1L,act, null,null, null, null);
		
		assertEquals(Account,persAccounts);
	}
	
	@Test
	public void testManageUsers() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		User User=new User(null,act, null,null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		User persUsers=db.manageUsers(new ArrayList<User>(List.of(User)),act).get(0);
		User=new User(1L,act, null,null);
		
		assertEquals(User,persUsers);
	}
	
	@Test
	public void testSelectUserByName() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.manageUsers(new ArrayList<User>(List.of(
				new User(null,act, null,null),
				new User(null,act, "name1",null),
				new User(3L  ,act, "name",null),
				new User(null,act, "name2",null),
				new User(null,act.minusDays(3), "name",null),
				new User(null,act, "nam",null))),act).get(0);
		
		List<User> users=db.getUserByName("name");
		assertEquals(2,users.size());
		assertTrue(users.contains(new User(3L  ,act, "name",null)));
		int i=0;
		for (User user : users) {
			assertEquals("name",user.getUserName());
			if(user.getLastChange().isBefore(act))
				i++;
		}
		assertEquals(1,i);
	}
	
	@Test
	public void testManageCompany() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		Company company=new Company(null,act, null,null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		Company persCompanys=db.manageCompanies(new ArrayList<Company>(List.of(company)),act).get(0);
		company=new Company(1L,act, null,null, null, null);
		
		assertTrue(company.equals(persCompanys,true));
	}
	
	@Test
	public void testGetVatTypes() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		ArrayList<VatType> types=new ArrayList<VatType>(
				List.of(new VatType(1L,act, null,null, null, true, true),
						new VatType(2L,act, null,null, null, false, false),
						new VatType(3L,act, null,null, null, false, true),
						new VatType(4L,act, null,null, null, true, false)));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.manageVatTypes(types,act);
		
		List<VatType> allTypes=db.getVatTypes();
		
		assertTrue(allTypes.size()==types.size());
		assertTrue(types.containsAll(allTypes));
	}
	
	@Test
	public void testGetVatTypesBooleanTrue() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		ArrayList<VatType> types=new ArrayList<VatType>(
				List.of(new VatType(1L,act, null,null, null, true, true),
						new VatType(2L,act, null,null, null, false, false),
						new VatType(3L,act, null,null, null, false, true),
						new VatType(4L,act, null,null, null, true, false)));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.manageVatTypes(types,act);
		
		List<VatType> allTypes=db.getVatTypes(true);
		
		assertTrue(allTypes.size()==types.size());
		assertTrue(types.containsAll(allTypes));
	}
	
	@Test
	public void testGetVatTypesBooleanFalse() throws Exception {
		ZonedDateTime act = ZonedDateTime.now();
		ArrayList<VatType> types=new ArrayList<VatType>(
				List.of(new VatType(1L,act, null,null, null, true, true),
						new VatType(2L,act, null,null, null, false, false),
						new VatType(3L,act, null,null, null, false, true),
						new VatType(4L,act, null,null, null, true, false)));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		db.manageVatTypes(types,act);
		
		List<VatType> allTypes=db.getVatTypes(false);
		
		assertTrue(allTypes.size()==2);
		assertTrue(allTypes.contains(types.get(1)));
		assertTrue(allTypes.contains(types.get(3)));
	}

	@Test
	public void testMergeAccount() throws Exception {
		Account account=new Account(1L,ZonedDateTime.now(),null,null,null,null);		
		
		DbAbstractionLayer db = Mockito.spy(new DbAbstractionLayer());
		db.updateAccount(account);
		verify(db).merge(account, Account.class);
	}
	
	@Test
	public void testMergeUsers() throws Exception {
		User user=new User(1L,ZonedDateTime.now(),null,null);		
		
		DbAbstractionLayer db = Mockito.spy(new DbAbstractionLayer());
		db.updateUser(user);
		verify(db).merge(user, User.class);
	}
	
	@Test
	public void testMergeCompany() throws Exception {
		Company company=new Company(null,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null);		
		
		DbAbstractionLayer db = Mockito.spy(new DbAbstractionLayer());
		db.updateCompany(company);
		verify(db).merge(company, Company.class);
	}

	@Test
	public void testMergeMonthAccountTurnover() throws Exception {
		MonthAccountTurnover month=new MonthAccountTurnover(null,ZonedDateTime.now(),
				new ArrayList<Transaction>(),null,null,null,null,null,null,null,null);		
		
		
		DbAbstractionLayer db = Mockito.spy(new DbAbstractionLayer());
		db.updateMonthAccountTurnover(month);
		verify(db).merge(month, MonthAccountTurnover.class);
	}	

	@Test
	public void testMergeTransaction() throws Exception {
		Transaction transaction=new Transaction(null,ZonedDateTime.now(), 0,
				null,null,null,null,null,null,null,null, null, null);
		
		DbAbstractionLayer db = Mockito.spy(new DbAbstractionLayer());
		db.updateTransaction(transaction);
		verify(db).merge(transaction,Transaction.class);
	}

	@Test
	public void testMergeVatType() throws Exception {
		VatType vatType=new VatType(null,ZonedDateTime.now(), null,null, null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		VatType persVatType=db.updateVatType(vatType);
		vatType=new VatType(1L,vatType.getLastChange(), null,null, null, null, null);
		assertEquals(vatType,persVatType);
		
		vatType.setName("name");
		vatType=db.updateVatType(vatType);
		persVatType=db.getVatTypes().get(0);
		assertEquals(vatType,persVatType);
		assertEquals("name",persVatType.getName());
		assertEquals("name",vatType.getName());
	}

	@Test
	public void testDeleteData() throws Exception {
		List<? extends AbstractDataObject> list1=new ArrayList<Company>(List.of(
				new Company(1L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				new Company(2L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				new Company(3L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null)));
		
		List<? extends AbstractDataObject> list2=new ArrayList<User>(List.of(
				new User(4L,ZonedDateTime.now(),"name1",null),
				new User(5L,ZonedDateTime.now(),"name2",null),
				new User(6L,ZonedDateTime.now(),"name3",null)));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		LinkedList<AbstractDataObject> linkedList=new LinkedList<AbstractDataObject>();
		linkedList.addAll(list1);linkedList.addAll(list2);
		db.recursiveMergeData(linkedList);
		list1=db.getCompanies();
		list2=db.getUsers();
		
		db.deleteData(List.of(list1.get(1),list2.get(1)));
		
		List<Company> companies=db.getCompanies();
		for (Company company : companies) {
			db.loadAccountsToCompany(company);
		}
		
		assertTrue(db.getUserByName("name2").isEmpty());
		assertEquals(new Company(1L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				companies.get(0));
		assertEquals(new Company(3L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				companies.get(1));
		assertEquals(2,companies.size());
	}

	@Tag("Active")
	@Test
	public void testMergeData() throws Exception {
		List<Company> list1=new ArrayList<Company>(List.of(
				new Company(1L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				new Company(null,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null),
				new Company(3L,ZonedDateTime.now(),new ArrayList<Account>(),null,null,null)));
		
		List<? extends AbstractDataObject> list2=new ArrayList<User>(List.of(
				new User(4L,ZonedDateTime.now(),"name1",null),
				new User(null,ZonedDateTime.now(),"name2",null),
				new User(6L,ZonedDateTime.now(),"name3",null)));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		
		
		LinkedList<AbstractDataObject> linkedList=new LinkedList<AbstractDataObject>();
		linkedList.addAll(list1);linkedList.addAll(list2);
		db.recursiveMergeData(linkedList);
		
		
		assertEquals(3,db.getUsers().size());
		assertTrue(list2.containsAll(db.getUsers()));
		assertEquals(3,db.getCompanies().size());
		List<Company> companies=db.getCompanies();
		for (Company company : companies) {
			db.loadAccountsToCompany(company);
		}
		for (int i = 0; i < 3; i++) {
			assertTrue(list1.get(i).equals(companies.get(i),true));
		}
	}

	@Test
	public void testDeleteMonthAccountTurnoverNull() throws Exception {
		Account accountMock=Mockito.mock(Account.class);
		@SuppressWarnings("unchecked")
		List<MonthAccountTurnover> listMock=Mockito.mock(List.class);
		when(accountMock.getBalanceMonths()).thenReturn(listMock);
		MonthAccountTurnover month=new MonthAccountTurnover(null,ZonedDateTime.now(),
				new ArrayList<Transaction>(List.of(
						new Transaction(null,ZonedDateTime.now(), 0,
								null,null,null,null,null,null,null,null, null, null),
						new Transaction(null,ZonedDateTime.now(), 0,
								null,null,null,null,null,null,null,null, null, null)
						)),null,accountMock,null,null,null,null,null,null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		
		//Assert not throw
		db.remove(month);
		verify(accountMock).updateBalance();
		verify(listMock).remove(month);
	}

	@Test
	public void testDeleteMonthAccountTurnover() throws Exception {
		Money m0=Money.of(0, "EUR");
		Money m1=Money.of(1, "EUR");
		Account rawAccount=new Account(null,null,m1,null);
		MonthAccountTurnover month=new MonthAccountTurnover(null,ZonedDateTime.now(),
				new ArrayList<Transaction>(),null,rawAccount,m1,m1,m1,m1,m1,m1);
		month.appendTranaction(new Transaction(null,ZonedDateTime.now(), 0,
								m1,m0,null,null,null,null,null,null, null, null));
		month.appendTranaction(new Transaction(null,ZonedDateTime.now(), 0,
								m1,m0,null,null,null,null,null,null, null, null));
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		
		db.updateAccount(rawAccount);
		rawAccount.addBalanceMonth(month);
		db.recursiveMergeData(month.getTransactions());
		month=db.updateMonthAccountTurnover(month);
		
		assertEquals(Money.of(3, "EUR"),rawAccount.getBalance());
		assertEquals(1,db.getMonthAccountTurnovers().size());
		assertEquals(2,db.getTransactions().size());
				
		db.remove(month);
		
		assertEquals(0,db.getMonthAccountTurnovers().size());
		assertEquals(0,db.getTransactions().size());
		assertEquals(Money.of(1, "EUR"),rawAccount.getBalance());
		assertTrue(rawAccount.getBalanceMonths().isEmpty());
	}

	@Test
	public void testDeleteTransaction() throws Exception {
		Transaction transaction=new Transaction(null,ZonedDateTime.now(), 0,
				null,null,null,null,null,null,null,null, null, null);
		
		DbAbstractionLayer db = new DbAbstractionLayer();
		//Assert not throw
		db.remove(transaction);
				
		transaction=db.updateTransaction(transaction);
		
		db.remove(transaction);
		
		assertEquals(0,db.getTransactions().size());
	}
	
}
