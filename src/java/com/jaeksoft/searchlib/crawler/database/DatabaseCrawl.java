/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawl implements Comparable<DatabaseCrawl> {

	private String name;

	private String url;

	private String driverClass;

	private String user;

	private String password;

	private String sql;

	private LanguageEnum lang;

	private FieldMap fieldMap;

	public DatabaseCrawl() {
		name = null;
		url = null;
		driverClass = null;
		user = null;
		password = null;
		sql = null;
		lang = LanguageEnum.UNDEFINED;
		fieldMap = new FieldMap();
	}

	public DatabaseCrawl(DatabaseCrawl crawl) {
		this();
		crawl.copyTo(this);
	}

	public void copyTo(DatabaseCrawl crawl) {
		crawl.name = this.name;
		crawl.url = this.url;
		crawl.driverClass = this.driverClass;
		crawl.user = this.user;
		crawl.password = this.password;
		crawl.sql = this.sql;
		crawl.lang = this.lang;
		this.fieldMap.copyTo(crawl.fieldMap);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the driverClass
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * @param driverClass
	 *            the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	/**
	 * @return the fieldMap
	 */
	public FieldMap getFieldMap() {
		return fieldMap;
	}

	protected final static String DBCRAWL_NODE_NAME = "databaseCrawl";
	protected final static String DBCRAWL_ATTR_NAME = "name";
	protected final static String DBCRAWL_ATTR_DRIVER_CLASS = "driverClass";
	protected final static String DBCRAWL_ATTR_USER = "user";
	protected final static String DBCRAWL_ATTR_PASSWORD = "password";
	protected final static String DBCRAWL_ATTR_URL = "url";
	protected final static String DBCRAWL_ATTR_LANG = "lang";
	protected final static String DBCRAWL_NODE_NAME_SQL = "sql";
	protected final static String DBCRAWL_NODE_NAME_MAP = "map";

	public static DatabaseCrawl fromXml(XPathParser xpp, Node item)
			throws XPathExpressionException {
		DatabaseCrawl crawl = new DatabaseCrawl();
		crawl.setName(XPathParser.getAttributeString(item, DBCRAWL_ATTR_NAME));
		crawl.setDriverClass(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_DRIVER_CLASS));
		crawl.setUser(XPathParser.getAttributeString(item, DBCRAWL_ATTR_USER));
		crawl.setPassword(XPathParser.getAttributeString(item,
				DBCRAWL_ATTR_PASSWORD));
		crawl.setUrl(XPathParser.getAttributeString(item, DBCRAWL_ATTR_URL));
		crawl.setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				item, DBCRAWL_ATTR_LANG)));
		Node sqlNode = xpp.getNode(item, DBCRAWL_NODE_NAME_SQL);
		if (sqlNode != null)
			crawl.setSql(xpp.getNodeString(sqlNode));
		Node mapNode = xpp.getNode(item, DBCRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			crawl.fieldMap.load(xpp, mapNode);
		return crawl;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(DBCRAWL_NODE_NAME, DBCRAWL_ATTR_NAME, getName(),
				DBCRAWL_ATTR_DRIVER_CLASS, getDriverClass(), DBCRAWL_ATTR_USER,
				getUser(), DBCRAWL_ATTR_PASSWORD, getPassword(),
				DBCRAWL_ATTR_URL, getUrl(), DBCRAWL_ATTR_LANG, getLang()
						.getCode());
		xmlWriter.startElement(DBCRAWL_NODE_NAME_MAP);
		fieldMap.store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.startElement(DBCRAWL_NODE_NAME_SQL);
		xmlWriter.textNode(getSql());
		xmlWriter.endElement();
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(DatabaseCrawl o) {
		return this.name.compareTo(o.name);
	}
}