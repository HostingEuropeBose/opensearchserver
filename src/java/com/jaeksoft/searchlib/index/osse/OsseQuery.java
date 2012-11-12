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

import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.query.parser.Expression.TermOperator;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class OsseQuery {

	private final List<Pointer> cursors;

	private final OsseErrorHandler error;

	private final Pointer cursorAll;

	private OsseIndex index;

	public OsseQuery(OsseIndex index, Query query) throws SearchLibException {
		this.index = index;
		cursors = new ArrayList<Pointer>(0);
		error = new OsseErrorHandler();
		cursorAll = OsseLibrary.INSTANCE.OSSCLib_QCursor_Create(
				index.getPointer(), null, null, 0,
				OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_OR, error.getPointer());
		cursors.add(cursorAll);
		query.execute(this);
		for (Pointer cursor : cursors)
			System.out.println("CURSOR:"
					+ OsseLibrary.INSTANCE
							.OSSCLib_QCursor_GetNumberOfDocuments(cursor, null,
									error.getPointer()));
	}

	public void free() {
		for (Pointer cursor : cursors)
			OsseLibrary.INSTANCE.OSSCLib_QCursor_Delete(cursor);
		cursors.clear();
		error.release();
	}

	public Pointer combineCursor(Pointer cursor1, Pointer cursor2,
			TermOperator operator) throws SearchLibException {
		if (cursor1 == null)
			cursor1 = cursorAll;
		Pointer[] combinedCursors = new Pointer[] { cursor1, cursor2 };
		int bop;
		switch (operator) {
		case FORBIDDEN:
			bop = OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_INVERTED_AND;
			break;
		case ORUNDEFINED:
			bop = OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_OR;
			break;
		default:
		case REQUIRED:
			bop = OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_AND;
			break;
		}
		Pointer cursor = OsseLibrary.INSTANCE
				.OSSCLib_QCursor_CreateCombinedCursor(combinedCursors,
						combinedCursors.length, bop, error.getPointer());
		if (cursor == null)
			throw new SearchLibException(error.getError());
		cursors.add(cursor);
		return cursor;
	}

	public Pointer createTermCursor(String field, String term)
			throws SearchLibException {
		WString[] terms = new WString[] { new WString(term) };
		Pointer cursor = OsseLibrary.INSTANCE.OSSCLib_QCursor_Create(
				index.getPointer(), new WString(field), terms, terms.length,
				OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_AND, error.getPointer());
		if (cursor == null)
			throw new SearchLibException(error.getError());
		cursors.add(cursor);
		return cursor;
	}
}
