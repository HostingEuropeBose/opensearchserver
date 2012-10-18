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

package com.jaeksoft.searchlib.schema;

import java.util.StringTokenizer;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.FieldSelector;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Field implements FieldSelector, Comparable<Field> {

	protected String name;

	public Field() {
	}

	public void copy(Field sourceField) {
		this.name = sourceField.name;
	}

	public static Field fromXmlConfig(Node node) {
		String name = DomUtils.getAttributeText(node, "name");
		if (name == null)
			return null;
		return new Field(name);
	}

	public Field(String name) {
		this.name = name;
	}

	public Field(Field field) {
		this.name = field.name;
	}

	public Field duplicate() {
		return new Field(this);
	}

	@Override
	public FieldSelector.Result accept(String fieldName) {
		if (this.name.equals(fieldName))
			return FieldSelector.Result.LOAD;
		return FieldSelector.Result.NO_LOAD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void toString(StringBuffer sb) {
		sb.append(name);
	}

	public boolean equals(Field field) {
		return field.name == this.name;
	}

	/**
	 * Alimente la liste "target" avec les champs nommé dans le champ fl.
	 * 
	 * @param fl
	 *            "Champ1,Champ2,Champ3"
	 * @param target
	 *            Liste de champs destinataire des champs trouvés
	 * @throws SearchLibException
	 */
	public static <T extends Field> void filterCopy(FieldList<T> source,
			String filter, FieldList<Field> target) throws SearchLibException {
		if (filter == null)
			return;
		StringTokenizer st = new StringTokenizer(filter, ", \t\r\n");
		while (st.hasMoreTokens()) {
			String fieldName = st.nextToken().trim();
			target.add(new Field(source.get(fieldName)));
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(Field o) {
		return name.compareTo(o.name);
	}

}
