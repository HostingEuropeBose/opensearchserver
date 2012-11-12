/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Schema {

	private SchemaFieldList fieldList;

	private AnalyzerList analyzers;

	private AnalyzerSelector analyzerSelector;

	private ReadWriteLock rwl = new ReadWriteLock();

	private Schema() {
		fieldList = null;
		analyzers = null;
		analyzerSelector = null;
	}

	public static Schema fromXmlConfig(Config config, Node parentNode,
			XPathParser xpp) throws XPathExpressionException,
			SearchLibException {

		Schema schema = new Schema();

		schema.analyzers = AnalyzerList.fromXmlConfig(config, xpp,
				xpp.getNode(parentNode, "analyzers"));

		schema.fieldList = SchemaField.fromXmlConfig(xpp,
				xpp.getNode(parentNode, "fields"));

		schema.recompileAnalyzers();

		return schema;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		rwl.r.lock();
		try {
			writer.startElement("schema");
			analyzers.writeXmlConfig(writer);
			fieldList.writeXmlConfig(writer);
			writer.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public AnalyzerList getAnalyzerList() {
		return analyzers;
	}

	public SchemaFieldList getFieldList() {
		return fieldList;
	}

	public void recompileAnalyzers() throws SearchLibException {
		rwl.w.lock();
		try {
			analyzerSelector = new AnalyzerSelector(fieldList);
			analyzers.recompile(analyzerSelector);
		} finally {
			rwl.w.unlock();
		}
	}

	public AnalyzerSelector getAnalyzerSelector() {
		rwl.r.lock();
		try {
			return analyzerSelector;
		} finally {
			rwl.r.unlock();
		}
	}

}
