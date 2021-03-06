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

package com.jaeksoft.searchlib.request;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;

public class DeleteRequest<T> implements Externalizable, Iterable<T>,
		Collecter<T> {

	private Collection<T> collection;

	public DeleteRequest() {
		collection = new ArrayList<T>();
	}

	public DeleteRequest(Collection<T> collection) {
		this.collection = collection;
	}

	public Collection<T> getCollection() {
		return collection;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(collection, out);
	}

	@Override
	public Iterator<T> iterator() {
		return collection.iterator();
	}

	@Override
	public void addObject(T field) {
		collection.add(field);
	}

}
