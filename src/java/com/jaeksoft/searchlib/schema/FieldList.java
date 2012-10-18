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

package com.jaeksoft.searchlib.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.index.FieldSelector;
import com.jaeksoft.searchlib.util.External.Collecter;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FieldList<T extends Field> implements
		CacheKeyInterface<FieldList<T>>, FieldSelector, Iterable<T>,
		Collecter<T> {

	private List<T> fieldList;
	private transient Set<T> fieldSet;
	private transient Map<String, T> fieldsName;
	private transient String cacheKey;

	/**
	 * Constructeur de base.
	 */
	public FieldList() {
		this.fieldsName = new TreeMap<String, T>();
		this.fieldSet = new TreeSet<T>();
		this.fieldList = new ArrayList<T>(0);
		cacheKey = null;
	}

	/**
	 * Ce constructeur cr�� une liste contenant les m�mes champs que la
	 * liste pass�e en param�tres (fl).
	 * 
	 * @param fl
	 * @throws SearchLibException
	 */
	public FieldList(FieldList<T> fl) throws SearchLibException {
		this();
		add(fl);
	}

	@SuppressWarnings("unchecked")
	public void add(FieldList<T> fl) throws SearchLibException {
		synchronized (this) {
			for (T field : fl)
				add((T) field.duplicate());
			cacheKey = null;
		}
	}

	/**
	 * Retourne le champ par d�fault du fichier de config XML.
	 * <gisearch><schema defaultField="nomchamp">...
	 * 
	 * @param document
	 * @param xPath
	 * @return Field
	 * @throws XPathExpressionException
	 */
	public T getDefaultField(Document document, XPathParser xpp)
			throws XPathExpressionException {
		Node node = xpp.getNode("/gisearch/schema");
		if (node == null)
			return null;
		return get(XPathParser.getAttributeString(node, "defaultField"));
	}

	/**
	 * Ajoute un champ � la liste
	 * 
	 * @throws SearchLibException
	 */
	public boolean add(T field) throws SearchLibException {
		synchronized (this) {
			if (!fieldSet.add(field))
				return true;
			fieldList.add(field);
			fieldsName.put(field.name, field);
			cacheKey = null;
			return true;
		}
	}

	public boolean addOrSet(T field) throws SearchLibException {
		synchronized (this) {
			T f = fieldsName.get(field.name);
			if (f == null)
				return add(field);
			f.copy(field);
			cacheKey = null;
			return true;
		}
	}

	/**
	 * Renvoie le champ � la position "index"
	 */
	public T get(int index) {
		return fieldList.get(index);
	}

	/**
	 * Renvoie le champ nomm� "name"
	 * 
	 * @param name
	 * @return Field
	 */
	public T get(String name) {
		return fieldsName.get(name);
	}

	public T get(Field field) {
		return fieldsName.get(field.name);
	}

	/**
	 * Renvoie la taille de la liste de champs.
	 */
	public int size() {
		return fieldList.size();
	}

	@Override
	public FieldSelector.Result accept(String fieldName) {
		if (this.fieldsName.containsKey(fieldName))
			return FieldSelector.Result.LOAD;
		return FieldSelector.Result.NO_LOAD;
	}

	public String[] toArrayName() {
		Set<String> set = fieldsName.keySet();
		String[] names = new String[set.size()];
		return set.toArray(names);
	}

	public void toNameList(List<String> nameList) {
		for (String name : fieldsName.keySet())
			nameList.add(name);
	}

	@Override
	public String toString() {
		String s = null;
		for (Field f : fieldList) {
			if (s == null)
				s = f.name;
			else
				s += ", " + f.name;
		}
		return s;
	}

	@Override
	public Iterator<T> iterator() {
		return this.fieldList.iterator();
	}

	public List<T> getList() {
		return fieldList;
	}

	public List<T> getSortedList() {
		synchronized (this) {
			List<T> list = new ArrayList<T>(fieldsName.size());
			Iterator<T> it = fieldSet.iterator();
			while (it.hasNext())
				list.add(it.next());
			return list;
		}
	}

	@Override
	public void addObject(T field) throws SearchLibException {
		synchronized (this) {
			add(field);
		}
	}

	public String getCacheKey() {
		synchronized (this) {
			if (cacheKey != null)
				return cacheKey;
			StringBuffer sb = new StringBuffer();
			for (Field field : fieldList)
				field.toString(sb);
			cacheKey = sb.toString();
			return cacheKey;
		}
	}

	@Override
	public int compareTo(FieldList<T> o) {
		return getCacheKey().compareTo(o.getCacheKey());
	}

	public void remove(Field field) throws SearchLibException {
		synchronized (this) {
			fieldSet.remove(field);
			fieldList.remove(field);
			fieldsName.remove(field.name);
			cacheKey = null;
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		synchronized (this) {
			for (Field field : fieldList)
				field.writeXmlConfig(xmlWriter);
		}
	}

}
