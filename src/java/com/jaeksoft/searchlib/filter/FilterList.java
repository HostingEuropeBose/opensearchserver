/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.External.Collecter;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilterList implements Externalizable, Collecter<Filter>,
		Iterable<Filter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5575695644602182902L;

	private List<Filter> filterList;

	private transient Config config;

	public FilterList() {
		config = null;
		this.filterList = new ArrayList<Filter>();
	}

	public FilterList(FilterList fl) {
		this.config = fl.config;
		this.filterList = new ArrayList<Filter>(fl.size());
		for (Filter f : fl)
			addObject(f);
	}

	public FilterList(Config config) {
		this.filterList = new ArrayList<Filter>();
		this.config = config;
	}

	@Override
	public void addObject(Filter filter) {
		filterList.add(filter);
	}

	public void add(String req, boolean negative, Source src) {
		addObject(new Filter(req, negative, src));
	}

	public void remove(Filter filter) {
		filterList.remove(filter);
	}

	public int size() {
		return filterList.size();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(filterList, out);
	}

	@Override
	public Iterator<Filter> iterator() {
		return filterList.iterator();
	}

	public FilterHits getFilterHits(ReaderLocal reader, Field defaultField,
			Analyzer analyzer) throws IOException, ParseException {

		if (size() == 0)
			return null;

		FilterHits filterHits = new FilterHits();
		for (Filter filter : filterList)
			filterHits
					.and(reader.getFilterHits(defaultField, analyzer, filter));

		return filterHits;
	}

	public Object[] toArray() {
		return filterList.toArray();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		for (Filter filter : filterList)
			filter.writeXmlConfig(xmlWriter);
	}

}
