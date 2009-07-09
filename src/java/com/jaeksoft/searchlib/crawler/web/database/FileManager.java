/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.FileUtils;

public class FileManager {

	private static final String FILE_SEARCH = "fileSearch";

	public enum Field {

		FILE("path"), WHEN("when"), CONTENTBASETYPE("contentBaseType"), CONTENTTYPECHARSET(
				"contentTypeCharset"), CONTENTENCODING("contentEncoding"), CONTENTLENGTH(
				"contentLength"), LANG("lang"), LANGMETHOD("langMethod"), FETCHSTATUS(
				"fetchStatus"), RESPONSECODE("responseCode"), PARSERSTATUS(
				"parserStatus"), INDEXSTATUS("indexStatus");

		private final String name;

		private Field(String name) {
			this.name = name;
		}

		private void addFilterQuery(SearchRequest request, Object value)
				throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQuery(sb, value);
			request.addFilter(sb.toString());
		}

		private void addFilterRange(SearchRequest request, Object from,
				Object to) throws ParseException {
			StringBuffer sb = new StringBuffer();
			addQueryRange(sb, from, to);
			request.addFilter(sb.toString());
		}

		private void addQuery(StringBuffer sb, Object value) {
			sb.append(" ");
			sb.append(name);
			sb.append(":");
			sb.append(value);
		}

		private void addQueryRange(StringBuffer sb, Object from, Object to) {
			sb.append(" ");
			sb.append(name);
			sb.append(":[");
			sb.append(from);
			sb.append(" TO ");
			sb.append(to);
			sb.append("]");
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private final Client fileDbClient;
	private final Client targetClient;

	public FileManager(Client client, File dataDir) throws SearchLibException,
			URISyntaxException, FileNotFoundException {
		dataDir = new File(dataDir, "file_crawler_url");
		if (!dataDir.exists())
			dataDir.mkdir();

		this.fileDbClient = new Client(dataDir, "/file_config.xml", true);
		targetClient = client;
	}

	public Client getFileDbClient() {
		return fileDbClient;
	}

	public void injectPaths(List<PathItem> pathList) throws SearchLibException {
		List<FileItem> fileList = new ArrayList<FileItem>();

		Iterator<PathItem> it = pathList.iterator();
		while (it.hasNext()) {
			PathItem item = (PathItem) it.next();

			if (item.getStatus() == PathItem.Status.INJECTED)
				FileUtils.addChildren(fileList, item);
		}

		deleteFiles(fileList);
		inject(fileList);
	}

	public void deleteFiles(List<FileItem> theNew) throws SearchLibException {
		List<String> toDelete = new ArrayList<String>();

		List<FileItem> files = new ArrayList<FileItem>();
		getFiles(fileQuery(), null, false, 0, 100000, files);
		Iterator<FileItem> iterator = files.iterator();

		while (iterator.hasNext()) {
			FileItem item = (FileItem) iterator.next();
			String current = item.getPath();

			boolean keep = false;
			for (FileItem currentPath : theNew) {
				if (currentPath.getPath() != null
						&& currentPath.getPath().contains(current)) {
					keep = true;
					break;
				}
			}

			if (!keep)
				toDelete.add(current);
		}

		deleteFiles(toDelete);

	}

	public void deletePath(String path) throws SearchLibException {
		try {
			targetClient.deleteDocument(path);
			fileDbClient.deleteDocument(path);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	private void deleteFiles(Collection<String> workDeleteUrlList)
			throws SearchLibException {
		try {
			targetClient.deleteDocuments(workDeleteUrlList);
			fileDbClient.deleteDocuments(workDeleteUrlList);
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	public boolean exists(String path) throws SearchLibException {
		SearchRequest request = getPathSearchRequest();
		request.setQueryString("path:\"" + path + '"');
		return (getFiles(request, null, false, 0, 10, null) > 0);
	}

	public void inject(List<FileItem> list) throws SearchLibException {
		synchronized (this) {
			try {
				List<IndexDocument> injectList = new ArrayList<IndexDocument>();
				for (FileItem item : list) {
					if (exists(item.getPath()))
						item.setStatus(FileItem.Status.ALREADY);
					else
						injectList.add(item.getIndexDocument());
				}

				if (injectList.size() == 0)
					return;

				fileDbClient.updateDocuments(injectList);
				int injected = 0;

				for (FileItem item : list) {
					if (item.getStatus() == FileItem.Status.UNDEFINED) {
						item.setStatus(FileItem.Status.INJECTED);
						injected++;
					}
				}

				if (injected > 0)
					fileDbClient.reload(null);
			} catch (NoSuchAlgorithmException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (InstantiationException e) {
				throw new SearchLibException(e);
			} catch (IllegalAccessException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			}
		}
	}

	/*
	 * private void filterQueryToFetchOld(SearchRequest request, Date
	 * fetchIntervalDate) throws ParseException { StringBuffer query = new
	 * StringBuffer(); query.append("when:[00000000000000 TO ");
	 * query.append(UrlItem.getWhenDateFormat().format(fetchIntervalDate));
	 * query.append("]"); request.addFilter(query.toString()); }
	 * 
	 * private void filterQueryToFetchNew(SearchRequest request) throws
	 * ParseException { StringBuffer query = new StringBuffer();
	 * query.append("fetchStatus:"); query.append(FetchStatus.UN_FETCHED.value);
	 * request.addFilter(query.toString()); }
	 */

	/*
	 * public Date getPastDate(int fetchInterval) { return new
	 * Date(System.currentTimeMillis() - (long) fetchInterval 1000 * 86400); }
	 */

	/*
	 * private void getFacetLimit(Field field, SearchRequest searchRequest, int
	 * limit, List<NamedItem> list) throws IOException, ParseException,
	 * SyntaxError, URISyntaxException, ClassNotFoundException,
	 * InterruptedException, SearchLibException, InstantiationException,
	 * IllegalAccessException { Result result =
	 * fileDbClient.search(searchRequest); Facet facet =
	 * result.getFacetList().getByField(field.name); for (FacetItem facetItem :
	 * facet) { if (limit-- == 0) break; if (facetItem.getCount() == 0)
	 * continue; String term = facetItem.getTerm(); if (term == null) continue;
	 * if (term.length() == 0) continue; synchronized (list) { list.add(new
	 * NamedItem(term, facetItem.getCount())); } }
	 * 
	 * }
	 */

	/*
	 * private SearchRequest getHostFacetSearchRequest() { SearchRequest
	 * searchRequest = fileDbClient.getNewSearchRequest();
	 * searchRequest.setDefaultOperator("OR"); searchRequest.setRows(0);
	 * searchRequest.getFacetFieldList().add(new FacetField("host", 1, false));
	 * return searchRequest; }
	 */

	private SearchRequest getPathSearchRequest() throws SearchLibException {
		SearchRequest searchRequest = fileDbClient.getNewSearchRequest();
		searchRequest.setDefaultOperator("OR");
		searchRequest.setRows(0);
		searchRequest.addReturnField("path");
		searchRequest.addReturnField("contentBaseType");
		searchRequest.addReturnField("contentTypeCharset");
		searchRequest.addReturnField("contentEncoding");
		searchRequest.addReturnField("contentLength");
		searchRequest.addReturnField("lang");
		searchRequest.addReturnField("langMethod");
		searchRequest.addReturnField("when");
		searchRequest.addReturnField("responseCode");
		searchRequest.addReturnField("parserStatus");
		searchRequest.addReturnField("fetchStatus");
		searchRequest.addReturnField("indexStatus");
		return searchRequest;
	}

	/*
	 * public void getOldHostToFetch(Date fetchIntervalDate, int limit,
	 * List<NamedItem> hostList) throws SearchLibException, ParseException,
	 * IOException, SyntaxError, URISyntaxException, ClassNotFoundException,
	 * InterruptedException, InstantiationException, IllegalAccessException {
	 * SearchRequest searchRequest = getHostFacetSearchRequest();
	 * searchRequest.setQueryString("*:*"); filterQueryToFetchOld(searchRequest,
	 * fetchIntervalDate); // getFacetLimit(Field.HOST, searchRequest, limit,
	 * hostList); }
	 */

	/*
	 * public void getNewHostToFetch(int limit, List<NamedItem> hostList) throws
	 * SearchLibException, ParseException, IOException, SyntaxError,
	 * URISyntaxException, ClassNotFoundException, InterruptedException,
	 * InstantiationException, IllegalAccessException { SearchRequest
	 * searchRequest = getHostFacetSearchRequest();
	 * searchRequest.setQueryString("*:*");
	 * filterQueryToFetchNew(searchRequest); // getFacetLimit(Field.HOST,
	 * searchRequest, limit, hostList); }
	 */

	/*
	 * public void getStartingWith(String queryString, Field field, String
	 * start, int limit, List<NamedItem> list) throws ParseException,
	 * IOException, SyntaxError, URISyntaxException, ClassNotFoundException,
	 * InterruptedException, SearchLibException, InstantiationException,
	 * IllegalAccessException { SearchRequest searchRequest =
	 * fileDbClient.getNewSearchRequest(field + "Facet");
	 * searchRequest.setQueryString(queryString);
	 * searchRequest.getFilterList().add(field + ":" + start + "*",
	 * Source.REQUEST); getFacetLimit(field, searchRequest, limit, list); }
	 */

	/*
	 * public void getOldUrlToFetch(NamedItem host, Date fetchIntervalDate, long
	 * limit, List<UrlItem> urlList) throws SearchLibException, ParseException,
	 * IOException, SyntaxError, URISyntaxException, ClassNotFoundException,
	 * InterruptedException, InstantiationException, IllegalAccessException {
	 * SearchRequest searchRequest = fileDbClient
	 * .getNewSearchRequest(FILE_SEARCH); searchRequest.addFilter("host:\"" +
	 * SearchRequest.escapeQuery(host.getName()) + "\"");
	 * searchRequest.setQueryString("*:*"); filterQueryToFetchOld(searchRequest,
	 * fetchIntervalDate); searchRequest.setRows((int) limit); Result result =
	 * fileDbClient.search(searchRequest); for (ResultDocument item : result)
	 * urlList.add(new UrlItem(item)); }
	 */

	/*
	 * public void getNewUrlToFetch(NamedItem host, long limit, List<UrlItem>
	 * urlList) throws SearchLibException, ParseException, IOException,
	 * SyntaxError, URISyntaxException, ClassNotFoundException,
	 * InterruptedException, InstantiationException, IllegalAccessException {
	 * SearchRequest searchRequest = fileDbClient
	 * .getNewSearchRequest(FILE_SEARCH); searchRequest.addFilter("host:\"" +
	 * SearchRequest.escapeQuery(host.getName()) + "\"");
	 * searchRequest.setQueryString("*:*");
	 * filterQueryToFetchNew(searchRequest); searchRequest.setRows((int) limit);
	 * Result result = fileDbClient.search(searchRequest); for (ResultDocument
	 * item : result) urlList.add(new UrlItem(item)); }
	 */

	public SearchRequest fileQuery() throws SearchLibException {
		return fileQuery("", null, null, null, null, null, null, null, null,
				null, null, null, null, null);
	}

	public SearchRequest fileQuery(String like, String lang, String langMethod,
			String contentBaseType, String contentTypeCharset,
			String contentEncoding, Integer minContentLength,
			Integer maxContentLength, FetchStatus fetchStatus,
			Integer responseCode, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate)
			throws SearchLibException {
		try {

			SearchRequest searchRequest = fileDbClient
					.getNewSearchRequest(FILE_SEARCH);

			StringBuffer query = new StringBuffer();
			if (like != null) {
				like = like.trim();
				if (like.length() > 0) {
					Field.FILE.addQuery(query, SearchRequest.escapeQuery(like));
					query.append("*");
				}
			}

			if (lang != null) {
				lang = lang.trim();
				if (lang.length() > 0)
					Field.LANG.addFilterQuery(searchRequest, SearchRequest
							.escapeQuery(lang));
			}
			if (langMethod != null) {
				langMethod = langMethod.trim();
				if (langMethod.length() > 0)
					Field.LANGMETHOD.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(langMethod));
			}
			if (contentBaseType != null) {
				contentBaseType = contentBaseType.trim();
				if (contentBaseType.length() > 0)
					Field.CONTENTBASETYPE.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentBaseType));
			}
			if (contentTypeCharset != null) {
				contentTypeCharset = contentTypeCharset.trim();
				if (contentTypeCharset.length() > 0)
					Field.CONTENTTYPECHARSET.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentTypeCharset));
			}
			if (contentEncoding != null) {
				contentEncoding = contentEncoding.trim();
				if (contentEncoding.length() > 0)
					Field.CONTENTENCODING.addFilterQuery(searchRequest,
							SearchRequest.escapeQuery(contentEncoding));
			}

			if (responseCode != null)
				Field.RESPONSECODE.addFilterQuery(searchRequest, responseCode);
			if (fetchStatus != null && fetchStatus != FetchStatus.ALL)
				Field.FETCHSTATUS.addFilterQuery(searchRequest,
						fetchStatus.value);
			if (parserStatus != null && parserStatus != ParserStatus.ALL)
				Field.PARSERSTATUS.addFilterQuery(searchRequest,
						parserStatus.value);
			if (indexStatus != null && indexStatus != IndexStatus.ALL)
				Field.INDEXSTATUS.addFilterQuery(searchRequest,
						indexStatus.value);

			if (minContentLength != null || maxContentLength != null) {
				String from, to;
				DecimalFormat df = UrlItem.getContentLengthFormat();
				if (minContentLength == null)
					from = df.format(0);
				else
					from = df.format(minContentLength);
				if (maxContentLength == null)
					to = df.format(Integer.MAX_VALUE);
				else
					to = df.format(maxContentLength);
				Field.CONTENTLENGTH.addQueryRange(query, from, to);
			}

			if (startDate != null || endDate != null) {
				String from, to;
				SimpleDateFormat df = UrlItem.getWhenDateFormat();
				if (startDate == null)
					from = "00000000000000";
				else
					from = df.format(startDate);
				if (endDate == null)
					to = "99999999999999";
				else
					to = df.format(endDate);
				Field.WHEN.addFilterRange(searchRequest, from, to);
			}

			if (query.length() == 0)
				query.append("*:*");
			searchRequest.setQueryString(query.toString().trim());
			return searchRequest;
		} catch (ParseException e) {
			throw new SearchLibException(e);
		}
	}

	public long getFiles(SearchRequest searchRequest, Field orderBy,
			boolean orderAsc, long start, long rows, List<FileItem> list)
			throws SearchLibException {
		searchRequest.setStart((int) start);
		searchRequest.setRows((int) rows);
		try {
			if (orderBy != null)
				searchRequest.addSort(orderBy.name, !orderAsc);
			Result result = fileDbClient.search(searchRequest);
			if (list != null)
				for (ResultDocument doc : result)
					list.add(new FileItem(doc));
			return result.getNumFound();
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (RuntimeException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	public void reload(boolean optimize) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (optimize) {
			fileDbClient.reload(null);
			fileDbClient.getIndex().optimize(null);
			targetClient.reload(null);
			targetClient.getIndex().optimize(null);
		}
		fileDbClient.reload(null);
		targetClient.reload(null);
	}

	/*
	 * public void updateUrlItem(UrlItem urlItem) throws SearchLibException {
	 * try { IndexDocument indexDocument = new IndexDocument();
	 * urlItem.populate(indexDocument);
	 * fileDbClient.updateDocument(indexDocument); } catch
	 * (NoSuchAlgorithmException e) { throw new SearchLibException(e); } catch
	 * (IOException e) { throw new SearchLibException(e); } catch
	 * (URISyntaxException e) { throw new SearchLibException(e); } catch
	 * (InstantiationException e) { throw new SearchLibException(e); } catch
	 * (IllegalAccessException e) { throw new SearchLibException(e); } catch
	 * (ClassNotFoundException e) { throw new SearchLibException(e); } }
	 */

	/*
	 * public void updateCrawls(List<Crawl> crawls) throws SearchLibException {
	 * try { // Update target index List<IndexDocument> documents = new
	 * ArrayList<IndexDocument>(crawls .size()); for (Crawl crawl : crawls) {
	 * IndexDocument indexDocument = crawl.getTargetIndexDocument();
	 * documents.add(indexDocument); } targetClient.updateDocuments(documents);
	 * 
	 * // Update URL DB documents.clear(); for (Crawl crawl : crawls) {
	 * IndexDocument indexDocument = new IndexDocument();
	 * crawl.getUrlItem().populate(indexDocument); documents.add(indexDocument);
	 * } fileDbClient.updateDocuments(documents);
	 * 
	 * } catch (NoSuchAlgorithmException e) { throw new SearchLibException(e); }
	 * catch (IOException e) { throw new SearchLibException(e); } catch
	 * (URISyntaxException e) { throw new SearchLibException(e); } catch
	 * (InstantiationException e) { throw new SearchLibException(e); } catch
	 * (IllegalAccessException e) { throw new SearchLibException(e); } catch
	 * (ClassNotFoundException e) { throw new SearchLibException(e); } }
	 */

	/*
	 * public void updateUrlItems(List<UrlItem> urlItems) throws
	 * SearchLibException { try { List<IndexDocument> documents = new
	 * ArrayList<IndexDocument>( urlItems.size()); for (UrlItem urlItem :
	 * urlItems) { IndexDocument indexDocument = new IndexDocument();
	 * urlItem.populate(indexDocument); documents.add(indexDocument); }
	 * fileDbClient.updateDocuments(documents); } catch
	 * (NoSuchAlgorithmException e) { throw new SearchLibException(e); } catch
	 * (IOException e) { throw new SearchLibException(e); } catch
	 * (URISyntaxException e) { throw new SearchLibException(e); } catch
	 * (InstantiationException e) { throw new SearchLibException(e); } catch
	 * (IllegalAccessException e) { throw new SearchLibException(e); } catch
	 * (ClassNotFoundException e) { throw new SearchLibException(e); } }
	 */

}