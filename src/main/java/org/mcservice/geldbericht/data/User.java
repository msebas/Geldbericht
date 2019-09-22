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
package org.mcservice.geldbericht.data;

import java.lang.management.ManagementFactory;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

@Entity
@Table(name = "Users")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User extends AbstractDataObject {
	
	/**
	 * The requested compute time in milliseconds for a password verify call. Defaults to 500. milliseconds. 
	 */
	public static int passwordCpuUsageTime;
	/**
	 * The number of threads used by the password generation. Defaults to the number of CPU-threads possible
	 * on this system. 
	 */
	public static int passwordCpuThreads;
	/**
	 * The amount of memory to use when generating passwords, set to roughly 3% of system memory on startup.
	 * Value is by convention of Argon2 in kilobytes.
	 * Note: This value has to be cased to an integer because of API requirements. 
	 * Keep it always below {@code Integer.MAX_VALUE} 
	 */
	public static long passwordMemory;
	
	static {
		passwordCpuThreads=Runtime.getRuntime().availableProcessors();
		if(passwordCpuThreads<1)
			passwordCpuThreads=1;		
		passwordMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()/33;
		passwordMemory /= 1024;
		if(passwordMemory<512)
			passwordMemory=512;
		if(passwordMemory>Integer.MAX_VALUE)
			passwordMemory=Integer.MAX_VALUE;
		passwordCpuUsageTime=500;
	}
		
	String userName=null;
	String passwordHash=null;

	public User(Long uid, ZonedDateTime lastChange, String userName, String passwordHash) {
		super(uid, lastChange);
		this.userName=userName;
		this.passwordHash=passwordHash;
	}
	
	public User(User user) {
		super(user.uid, user.lastChange);
		this.userName=user.userName;
		this.passwordHash=user.passwordHash;
	}

	public User() {
			super(null);
	}
	
	
	/**
	 * Encrypts and sets the password for this user. Keep in mind that
	 * the static members of this class set the amount of compute power used to verify the hash.
	 * Running this on a huge server might lead to hashes that could not be verified on a desktop.
	 * The supplied amount of memory gets silently reduced if required by the supplied time.
	 * Used is always Argon2-Hashing. 
	 * FIXME: Find a self supplying hash library with a standard format that is able to switch 
	 * the hashing algorithm to the best available choice without code changes.
	 * 
	 * The caller has to ensure that the system is in a state where performance benchmarks 
	 * are possible, this means especially that not other computation or memory access insensitive
	 * tasks are going to run or start while the benchmarking takes place.
	 *  
	 * @param password the password to set (NEVER store a password as {@code java.lang.String})
	 */
	public void setPassword(char[] password) {
		Argon2 argon2 = Argon2Factory.create();
		int iterations = 0, memoryScale=1; 
		//On machines with a huge amount of memory the supplied memory might be too much to get below the configured 
		//time in a single iteration. As the time is the main criteria to decide about the ability of a password to 
		//tackle brute force attacks reduce the memory overhead to get a meaningful iteration number...
		while(iterations<1) {
			iterations=Argon2Helper.findIterations(argon2, passwordCpuUsageTime, (int) passwordMemory/memoryScale, passwordCpuThreads);
			memoryScale*=2;
		}
		argon2 = Argon2Factory.create();
		this.passwordHash = argon2.hash(iterations, (int) passwordMemory, passwordCpuThreads, password);;
		this.lastChange=ZonedDateTime.now();
	}
	
	public boolean verifyPassword(char[] password) {
		Argon2 argon2 = Argon2Factory.create();
		try {
			return argon2.verify(getPasswordHash(), password);			
		} finally {
			argon2.wipeArray(password);
		}
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the passwordHash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		if(this.userName==userName ||
				( this.userName!=null && this.userName.equals(userName) )
				)
			return;
		this.userName = userName;
		this.lastChange=ZonedDateTime.now();
	}

	/**
	 * @param passwordHash the passwordHash to set
	 */
	public void setPasswordHash(String passwordHash) {
		if(this.passwordHash==passwordHash ||
				( this.passwordHash!=null && this.passwordHash.equals(passwordHash) )
				)
			return;
		this.passwordHash = passwordHash;
		this.lastChange=ZonedDateTime.now();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((passwordHash == null) ? 0 : passwordHash.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null || getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		User other = (User) obj;
		if (passwordHash == null) {
			if (other.passwordHash != null)
				return false;
		} else if (!passwordHash.equals(other.passwordHash))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

}
