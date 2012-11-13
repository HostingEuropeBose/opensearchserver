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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.query.Query;
import com.jaeksoft.searchlib.query.parser.Expression.TermOperator;
import com.jaeksoft.searchlib.result.collector.AbstractCollector;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class OsseQuery {

	private final List<Pointer> cursors;

	private final OsseErrorHandler error;

	private OsseIndex index;

	private final Pointer finalCursor;

	public OsseQuery(OsseIndex index, Query query) throws SearchLibException {
		this.index = index;
		cursors = new ArrayList<Pointer>(0);
		error = new OsseErrorHandler();
		finalCursor = query.execute(this);
	}

	public void free() {
		for (Pointer cursor : cursors)
			OsseLibrary.INSTANCE.OSSCLib_QCursor_Delete(cursor);
		cursors.clear();
		error.release();
	}

	public Pointer combineCursor(Pointer cursor1, Pointer cursor2,
			TermOperator operator) throws SearchLibException {
		Pointer[] combinedCursors = new Pointer[] { cursor1, cursor2 };

		int bop;
		switch (operator) {
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
		System.out.println("COMBINED CURSOR = " + cursorLength(cursor));
		return cursor;
	}

	public Pointer createTermCursor(String field, String term,
			TermOperator operator) throws SearchLibException {

		int bop;
		switch (operator) {
		case FORBIDDEN:
			bop = OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_INVERTED_AND;
			break;
		default:
		case REQUIRED:
			bop = OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_AND;
			break;
		}

		WString[] terms = new WString[] { new WString(term) };
		Pointer cursor = OsseLibrary.INSTANCE.OSSCLib_QCursor_Create(
				index.getPointer(), new WString(field), terms, terms.length,
				bop, error.getPointer());
		if (cursor == null)
			throw new SearchLibException(error.getError());
		cursors.add(cursor);

		System.out.println("TERM CURSOR - " + field + ":" + term + " ("
				+ cursorLength(cursor) + ")");
		return cursor;
	}

	public Pointer matchAll() {
		Pointer cursor = OsseLibrary.INSTANCE.OSSCLib_QCursor_Create(
				index.getPointer(), null, null, 0,
				OsseLibrary.OSSCLIB_QCURSOR_UI32BOP_OR, error.getPointer());
		cursors.add(cursor);
		return cursor;
	}

	public long cursorLength(Pointer cursor) {
		return OsseLibrary.INSTANCE.OSSCLib_QCursor_GetNumberOfDocuments(
				cursor, null, error.getPointer());
	}

	public void collect(AbstractCollector collector) throws SearchLibException,
			IOException {
		long[] docIdBuffer = new long[10000];
		long docPosition = 0;
		IntByReference bSuccess = new IntByReference();
		for (;;) {
			System.out.println("COLLECT " + docPosition);
			long length = OsseLibrary.INSTANCE.OSSCLib_QCursor_GetDocumentIds(
					finalCursor, docIdBuffer, docIdBuffer.length, docPosition,
					false, bSuccess, error.getPointer());
			if (bSuccess.getValue() == 0)
				throw new SearchLibException(error.getError());
			if (length == 0)
				break;
			for (int i = 0; i < length; i++)
				collector.collect(docIdBuffer[i]);
			docPosition = length;
		}
	}
}
