/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract.SecurityInterface;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess.Grant;
import com.jaeksoft.searchlib.crawler.file.process.SecurityAccess.Type;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDecimalFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;

public class FileItem extends FileInfo implements Serializable {

	public static final String UTF_8_ENCODING = "UTF-8";

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), ALREADY(
				"Already injected"), ERROR("Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043010587042224473L;

	final static ThreadSafeDecimalFormat contentLengthFormat = new ThreadSafeDecimalFormat(
			"00000000000000");

	final static ThreadSafeDateFormat dateFormat = new ThreadSafeSimpleDateFormat(
			"yyyyMMddHHmmssSSS");

	private String repository;
	private String directory;
	private List<String> subDirectory;
	private String lang;
	private String langMethod;
	private Long crawlDate;
	private Status status;

	private List<String> userAllow;
	private List<String> userDeny;
	private List<String> groupAllow;
	private List<String> groupDeny;

	protected FileItem() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		status = Status.UNDEFINED;
		repository = null;
		directory = null;
		subDirectory = null;
		lang = null;
		langMethod = null;
		crawlDate = null;
		userAllow = null;
		userDeny = null;
		groupAllow = null;
		groupDeny = null;
	}

	public FileItem(ResultDocument doc, FileItemFieldEnum fileItemFieldEnum)
			throws UnsupportedEncodingException, URISyntaxException {
		super(doc, fileItemFieldEnum);

		setRepository(doc.getValueContent(
				fileItemFieldEnum.repository.getName(), 0));
		setDirectory(doc.getValueContent(fileItemFieldEnum.directory.getName(),
				0));
		setSubDirectory(FieldValueItem.buildArrayList(doc
				.getValueArray(fileItemFieldEnum.subDirectory.getName())));

		setLang(doc.getValueContent(fileItemFieldEnum.lang.getName(), 0));

		setLangMethod(doc.getValueContent(
				fileItemFieldEnum.langMethod.getName(), 0));

		setCrawlDate(doc.getValueContent(fileItemFieldEnum.crawlDate.getName(),
				0));

		setFileExtension(doc.getValueContent(
				fileItemFieldEnum.fileExtension.getName(), 0));

		setUserAllow(FieldValueItem.buildArrayList(doc
				.getValueArray(fileItemFieldEnum.userAllow.getName())));
		setUserDeny(FieldValueItem.buildArrayList(doc
				.getValueArray(fileItemFieldEnum.userDeny.getName())));
		setGroupAllow(FieldValueItem.buildArrayList(doc
				.getValueArray(fileItemFieldEnum.groupAllow.getName())));
		setGroupDeny(FieldValueItem.buildArrayList(doc
				.getValueArray(fileItemFieldEnum.groupDeny.getName())));
	}

	public FileItem(FileInstanceAbstract fileInstance)
			throws SearchLibException {
		super(fileInstance);
		setRepository(fileInstance.getFilePathItem().toString());
		FileInstanceAbstract parentFileInstance = fileInstance.getParent();
		if (parentFileInstance != null)
			setDirectory(parentFileInstance.getURI());
		subDirectory = new ArrayList<String>();
		FileInstanceAbstract dir = fileInstance;
		while ((dir = dir.getParent()) != null)
			subDirectory.add(dir.getURI().getPath());
		setCrawlDate(System.currentTimeMillis());
		if (fileInstance instanceof SecurityInterface) {
			SecurityInterface fileInstanceSecurity = (SecurityInterface) fileInstance;
			List<SecurityAccess> securityAccesses;
			try {
				securityAccesses = fileInstanceSecurity.getSecurity();
			} catch (IOException e) {
				throw new SearchLibException(e);
			}
			setUserAllow(SecurityAccess.getIds(securityAccesses, Type.USER,
					Grant.ALLOW));
			setUserDeny(SecurityAccess.getIds(securityAccesses, Type.USER,
					Grant.DENY));
			setGroupAllow(SecurityAccess.getIds(securityAccesses, Type.GROUP,
					Grant.ALLOW));
			setGroupDeny(SecurityAccess.getIds(securityAccesses, Type.GROUP,
					Grant.DENY));
		}
	}

	public Long getCrawlDate() {
		return crawlDate;
	}

	public String getFullLang() {
		StringBuffer sb = new StringBuffer();
		if (lang != null)
			sb.append(lang);
		if (langMethod != null) {
			sb.append('(');
			sb.append(langMethod);
			sb.append(')');
		}
		return sb.toString();
	}

	public IndexDocument getIndexDocument(FileItemFieldEnum fileItemFieldEnum)
			throws UnsupportedEncodingException {
		IndexDocument indexDocument = new IndexDocument();
		populate(indexDocument, fileItemFieldEnum);
		return indexDocument;
	}

	public String getLang() {
		return lang;
	}

	public String getLangMethod() {
		return langMethod;
	}

	public String getDirectory() {
		return directory;
	}

	public List<String> getSubDirectory() {
		return subDirectory;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public void populate(IndexDocument indexDocument,
			FileItemFieldEnum fileItemFieldEnum) {
		super.populate(indexDocument, fileItemFieldEnum);

		if (repository != null)
			indexDocument.setString(fileItemFieldEnum.repository.getName(),
					repository);

		indexDocument.setString(fileItemFieldEnum.uri.getName(), getUri());

		if (directory != null)
			indexDocument.setString(fileItemFieldEnum.directory.getName(),
					directory);

		indexDocument.setStringList(fileItemFieldEnum.subDirectory.getName(),
				getSubDirectory());

		if (crawlDate != null)
			indexDocument.setString(fileItemFieldEnum.crawlDate.getName(),
					dateFormat.format(crawlDate));

		if (lang != null)
			indexDocument.setString(fileItemFieldEnum.lang.getName(), lang);
		if (langMethod != null)
			indexDocument.setString(fileItemFieldEnum.langMethod.getName(),
					langMethod);

		indexDocument.setStringList(fileItemFieldEnum.userAllow.getName(),
				getUserAllow());
		indexDocument.setStringList(fileItemFieldEnum.userDeny.getName(),
				getUserDeny());
		indexDocument.setStringList(fileItemFieldEnum.groupAllow.getName(),
				getGroupAllow());
		indexDocument.setStringList(fileItemFieldEnum.groupDeny.getName(),
				getGroupDeny());

	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLangMethod(String langMethod) {
		this.langMethod = langMethod;
	}

	private void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setDirectory(URI directoryUri) {
		if (directoryUri != null)
			this.directory = directoryUri.toASCIIString();
	}

	private void setSubDirectory(List<String> subDirectoryList) {
		this.subDirectory = subDirectoryList;
	}

	public String getRepository() {
		return repository;
	}

	private void setRepository(String r) {
		this.repository = r;
	}

	public void setCrawlDate(long d) {
		crawlDate = d;
	}

	private void setCrawlDate(String d) {
		if (d == null)
			return;
		try {
			crawlDate = dateFormat.parse(d).getTime();
		} catch (ParseException e) {
			Logging.warn(e.getMessage());
		}
	}

	/**
	 * @param userAllow
	 *            the userAllow to set
	 */
	public void setUserAllow(List<String> userAllow) {
		this.userAllow = userAllow;
	}

	/**
	 * @return the userAllow
	 */
	public List<String> getUserAllow() {
		return userAllow;
	}

	/**
	 * @param userDeny
	 *            the userDeny to set
	 */
	public void setUserDeny(List<String> userDeny) {
		this.userDeny = userDeny;
	}

	/**
	 * @return the userDeny
	 */
	public List<String> getUserDeny() {
		return userDeny;
	}

	/**
	 * @param groupAllow
	 *            the groupAllow to set
	 */
	public void setGroupAllow(List<String> groupAllow) {
		this.groupAllow = groupAllow;
	}

	/**
	 * @return the groupAllow
	 */
	public List<String> getGroupAllow() {
		return groupAllow;
	}

	/**
	 * @param groupDeny
	 *            the groupDeny to set
	 */
	public void setGroupDeny(List<String> groupDeny) {
		this.groupDeny = groupDeny;
	}

	/**
	 * @return the groupDeny
	 */
	public List<String> getGroupDeny() {
		return groupDeny;
	}
}