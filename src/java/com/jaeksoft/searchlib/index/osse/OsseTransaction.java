/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index.osse;

import java.util.concurrent.locks.ReentrantLock;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.sun.jna.Pointer;

public class OsseTransaction {

	private final static ReentrantLock l = new ReentrantLock();

	private Pointer transactPtr;

	private OsseErrorHandler err;

	public OsseTransaction(OsseIndex index) {
		l.lock();
		try {
			err = new OsseErrorHandler();
			transactPtr = OsseLibrary.INSTANCE.OSSCLib_Transact_Begin(
					index.getPointer(), err.getPointer());
		} finally {
			l.unlock();
		}
	}

	final public void commit() throws SearchLibException {
		l.lock();
		try {
			if (!OsseLibrary.INSTANCE.OSSCLib_Transact_Commit(transactPtr,
					null, 0, null, err.getPointer()))
				throw new SearchLibException(err.getError());
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	public Pointer getPointer() {
		l.lock();
		try {
			return transactPtr;
		} finally {
			l.unlock();
		}
	}

	final public Pointer getErrorPointer() {
		l.lock();
		try {
			return err.getPointer();
		} finally {
			l.unlock();
		}
	}

	final public void rollback() throws SearchLibException {
		l.lock();
		try {
			if (!OsseLibrary.INSTANCE.OSSCLib_Transact_Commit(transactPtr,
					null, 0, null, err.getPointer()))
				throw new SearchLibException(err.getError());
			transactPtr = null;
		} finally {
			l.unlock();
		}
	}

	final public void release() {
		l.lock();
		try {
			try {
				if (transactPtr != null)
					rollback();
			} catch (SearchLibException e) {
				Logging.warn(e);
			}
			if (err != null) {
				err.release();
				err = null;
			}
		} finally {
			l.unlock();
		}
	}

	public void throwError() throws SearchLibException {
		throw new SearchLibException(err.getError());
	}

}
