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

import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.index.StringIndex;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SortField extends Field implements CacheKeyInterface<Field> {

	private boolean desc;

	public SortField() {
	}

	public SortField(String name, boolean desc) {
		super(name);
		this.desc = desc;
	}

	public SortField(Node node) {
		super(DomUtils.getAttributeText(node, "name"));
		setDesc(DomUtils.getAttributeText(node, "direction"));
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(String v) {
		final String[] ascArray = { "+", "asc", "ascendant", "ascending" };
		for (String asc : ascArray) {
			if (asc.equalsIgnoreCase(v)) {
				desc = false;
				return;
			}
		}
		desc = true;
	}

	@Override
	public Field duplicate() {
		return new SortField(name, desc);
	}

	protected static SortField newSortField(String requestSort) {
		int c = requestSort.charAt(0);
		boolean desc = (c == '-');
		String name = (c == '+' || c == '-') ? requestSort.substring(1)
				: requestSort;
		return new SortField(name, desc);
	}

	public SorterAbstract getSorter() {
		if (name.equals("score")) {
			if (desc)
				return new DescComparableSorter<Float>();
			else
				return new AscComparableSorter<Float>();
		}
		if (desc)
			return new DescComparableSorter<String>();
		else
			return new AscComparableSorter<String>();
	}

	public StringIndex getStringIndex(ReaderLocal reader) throws IOException {
		if (name.equals("score"))
			return null;
		else
			return reader.getStringIndex(name);
	}

	@Override
	public int compareTo(Field o) {
		int c = super.compareTo(o);
		if (c != 0)
			return c;
		SortField f = (SortField) o;
		if (desc == f.desc)
			return 0;
		return desc ? -1 : 1;
	}

	@Override
	public void toString(StringBuffer sb) {
		if (desc)
			sb.append('-');
		else
			sb.append('+');
		sb.append(name);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name, "direction",
				isDesc() ? "desc" : "asc");
		xmlWriter.endElement();
	}

}
