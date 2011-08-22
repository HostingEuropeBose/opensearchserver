/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.sort;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.index.StringIndex;
import com.jaeksoft.searchlib.schema.FieldList;

public class SortList implements Externalizable, CacheKeyInterface<SortList> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8133333810675605075L;

	private FieldList<SortField> sortFieldList;

	public SortList() {
		sortFieldList = new FieldList<SortField>();
	}

	public SortList(SortList sortList) {
		sortFieldList = new FieldList<SortField>(sortList.sortFieldList);
	}

	public void add(SortField sortField) {
		sortFieldList.add(sortField);
	}

	public void add(String sortString) {
		sortFieldList.add(SortField.newSortField(sortString));
	}

	public void add(String fieldName, boolean desc) {
		sortFieldList.add(new SortField(fieldName, desc));
	}

	public FieldList<SortField> getFieldList() {
		return sortFieldList;
	}

	public SorterAbstract getSorter() {
		if (sortFieldList.size() == 0)
			return new DescScoreSorter();
		return new SortListSorter(sortFieldList);
	}

	public StringIndex[] newStringIndexArray(ReaderLocal reader)
			throws IOException {
		if (sortFieldList.size() == 0)
			return null;
		StringIndex[] stringIndexArray = new StringIndex[sortFieldList.size()];
		int i = 0;
		for (SortField field : sortFieldList)
			stringIndexArray[i++] = field.getStringIndex(reader);
		return stringIndexArray;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		sortFieldList = (FieldList<SortField>) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sortFieldList);
	}

	@Override
	public int compareTo(SortList o) {
		return sortFieldList.compareTo(o.sortFieldList);
	}

	public void remove(SortField sortField) {
		sortFieldList.remove(sortField);
	}

}
