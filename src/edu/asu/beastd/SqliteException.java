// Copyright (C) 2012-2013 Stevie Robinson, Eric Alford, Tara Mendoza, Blake Tucker, Anthony Sanchez, Davenn Mannix
//
// This file is part of CouchToSqlite.
//
// CouchToSqlite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// CouchToSqlite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with CouchToSqlite.  If not, see <http://www.gnu.org/licenses/>.

package edu.asu.beastd;

/**
 * This exception is thrown when there is any exception relating to SQLite interactions. 
 * @author Team BEASTD
 */
public class SqliteException extends RuntimeException {

	private static final long serialVersionUID = 8286785776245596050L;

	public SqliteException(String message) {
		super(message);
	}

	public SqliteException(Throwable cause) {
		super(cause);
	}

	public SqliteException(String message, Throwable cause) {
		super(message, cause);
	}

}
