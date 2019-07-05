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
package org.hibernate.dialect2.identity;

import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

public class SQLiteDialectIdentityColumnSupport extends IdentityColumnSupportImpl {
	@Override
	public boolean supportsIdentityColumns() {
		return true;
	}

  /*
	public boolean supportsInsertSelectIdentity() {
    return true; // As specified in NHibernate dialect
  }
  */

	@Override
	public boolean hasDataTypeInIdentityColumn() {
		// As specified in NHibernate dialect
		// FIXME true
		return false;
	}

  /*
	public String appendIdentitySelectToInsert(String insertString) {
    return new StringBuffer(insertString.length()+30). // As specified in NHibernate dialect
      append(insertString).
      append("; ").append(getIdentitySelectString()).
      toString();
  }
  */

	@Override
	public String getIdentitySelectString(String table, String column, int type) {
		return "select last_insert_rowid()";
	}

	@Override
	public String getIdentityColumnString(int type) {
		// return "integer primary key autoincrement";
		// FIXME "autoincrement"
		return "integer";
	}
}
