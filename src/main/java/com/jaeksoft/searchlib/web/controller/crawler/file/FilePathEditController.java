/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.dropbox.core.DbxWebAuth;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FileInstanceType;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.DropboxFileInstance;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.controller.AlertController;

public class FilePathEditController extends FileCrawlerController {

	private FilePathItem currentFilePath;

	private transient FileSelectorItem currentFile;

	private transient FileSelectorItem currentFolder;

	private transient List<FileSelectorItem> currentFolderList;

	private transient FileSelectorItem[] currentFileList;

	private boolean showHidden;

	private DbxWebAuth webAuthInfo;

	private class DeleteAlert extends AlertController {

		private FilePathItem deleteFilePath;

		protected DeleteAlert(FilePathItem deleteFilePath)
				throws InterruptedException {
			super("Please, confirm that you want to delete the location: "
					+ deleteFilePath.toString(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.deleteFilePath = deleteFilePath;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getFileManager().deleteByRepository(
					deleteFilePath.toString());
			client.getFilePathManager().remove(deleteFilePath);
			onCancel();
		}
	}

	public class FileSelectorItem {

		final protected File file;

		protected FileSelectorItem(File file) {
			this.file = file;
		}

		public String getName() {
			if (file == null)
				return null;
			String n = file.getName();
			if (n != null && n.length() > 0)
				return n;
			return file.getPath();
		}

		public File getFile() {
			return file;
		}

	}

	public FilePathEditController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
		currentFilePath = null;
		currentFileList = null;
		currentFile = null;
		currentFolder = null;
		showHidden = false;
		webAuthInfo = null;
	}

	public FileInstanceType[] getTypeList() throws SearchLibException {
		return FileInstanceType.values();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		FilePathItem filePathItem = getFilePathItemEdit();
		if (filePathItem == currentFilePath || filePathItem == null) {
			super.reload();
			return;
		}
		try {
			currentFilePath = filePathItem;
			if ("file".equals(filePathItem.getType().getScheme())) {
				String path = filePathItem.getPath();
				if (path != null) {
					File f = new File(path);
					if (f.exists()) {
						setCurrentFolder(f.getParentFile());
						setCurrentFile(new FileSelectorItem(new File(path)));
					}
				}
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		super.reload();
	}

	/**
	 * 
	 * @return the current FilePathItem
	 */
	public FilePathItem getCurrentFilePath() {
		return currentFilePath;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return isNoFilePathSelected() ? "Add a new location"
				: "Edit the selected location";
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
		setFilePathItemEdit(null);
		reload();
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		FilePathItem filePath = getFilePathItemSelected();
		if (filePath == null)
			return;
		new DeleteAlert(filePath);
	}

	@Command
	public void onCheck() throws InterruptedException, InstantiationException,
			IllegalAccessException, SearchLibException, URISyntaxException,
			UnsupportedEncodingException {
		if (currentFilePath == null)
			return;
		new AlertController("Test results: " + currentFilePath.check());
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException,
			URISyntaxException {
		Client client = getClient();
		if (client == null)
			return;
		FilePathManager filePathManager = client.getFilePathManager();
		FilePathItem checkFilePath = filePathManager.get(currentFilePath);
		FilePathItem selectedFilePath = getFilePathItemSelected();
		if (selectedFilePath == null) {
			if (checkFilePath != null) {
				new AlertController("The location already exists");
				return;
			}
		} else {
			if (checkFilePath != null)
				if (checkFilePath.hashCode() != selectedFilePath.hashCode()) {
					new AlertController("The location already exists");
					return;
				}
			filePathManager.remove(selectedFilePath);
		}
		filePathManager.add(currentFilePath);
		onCancel();
	}

	private FileSelectorItem[] getList(File[] files) {
		if (files == null)
			return null;
		FileSelectorItem[] items = new FileSelectorItem[files.length];
		int i = 0;
		for (File file : files)
			items[i++] = new FileSelectorItem(file);
		return items;

	}

	public FileSelectorItem[] getCurrentFileList() throws SearchLibException,
			IOException {
		synchronized (this) {
			if (currentFileList != null)
				return currentFileList;
			File[] files = null;
			getCurrentFolder();
			if (currentFolder == null) {
				files = File.listRoots();
			} else {
				if (!isShowHidden())
					files = currentFolder.file
							.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
				else
					files = currentFolder.file.listFiles();
			}
			currentFileList = getList(files);
			return currentFileList;
		}
	}

	@NotifyChange("*")
	public void setCurrentFile(FileSelectorItem item) {
		currentFile = item;
	}

	public FileSelectorItem getCurrentFile() {
		return currentFile;
	}

	public FileSelectorItem getCurrentFolder() throws SearchLibException,
			IOException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (currentFolder == null
					&& ClientFactory.INSTANCE.properties.isChroot())
				setCurrentFolder(StartStopListener.OPENSEARCHSERVER_DATA_FILE);
			return currentFolder;
		}
	}

	public List<FileSelectorItem> getFolderTree() {
		return currentFolderList;
	}

	public boolean isNotRoot() {
		return currentFolder != null;
	}

	public boolean isLocalFileType() {
		if (currentFilePath == null)
			return false;
		return "file".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotLocalFileType() {
		return !isLocalFileType();
	}

	public boolean isSwiftFileType() {
		if (currentFilePath == null)
			return false;
		return "swift".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotSwiftFileType() {
		return !isSwiftFileType();
	}

	public AuthType[] getSwiftAuthTypes() {
		return AuthType.values();
	}

	public boolean isDomain() {
		if (currentFilePath == null)
			return false;
		return "smb".equals(currentFilePath.getType().getScheme());
	}

	public boolean isNotSelectedFile() {
		return currentFile != null;
	}

	public boolean isSelectedFile() {
		return !isNotSelectedFile();
	}

	public void setCurrentFolder(FileSelectorItem fileSelectorItem)
			throws IOException, SearchLibException {
		if (fileSelectorItem != null)
			if (!ClientFactory.INSTANCE.properties
					.checkChrootQuietly(fileSelectorItem.file))
				return;
		currentFolder = fileSelectorItem;
		currentFolderList = null;
		if (currentFolder != null) {
			currentFolderList = new ArrayList<FileSelectorItem>();
			FileSelectorItem f = currentFolder;
			for (;;) {
				currentFolderList.add(0, new FileSelectorItem(f.file));
				File p = f.file.getParentFile();
				if (p == null)
					break;
				f = new FileSelectorItem(p);
				if (!ClientFactory.INSTANCE.properties
						.checkChrootQuietly(f.file))
					break;
			}
		}
		currentFileList = null;
		currentFile = null;
		reload();
	}

	private void setCurrentFolder(File file) throws IOException,
			SearchLibException {
		if (file == null)
			setCurrentFolder((FileSelectorItem) null);
		else
			setCurrentFolder(new FileSelectorItem(file));
	}

	public FileInstanceType getCurrentFileType() {
		if (currentFilePath == null)
			return null;
		return currentFilePath.getType();
	}

	public void setCurrentFileType(FileInstanceType type)
			throws SearchLibException {
		currentFilePath.setType(type);
		reload();
	}

	public boolean isShowHidden() {
		return showHidden;
	}

	public void setShowHidden(boolean b) throws SearchLibException {
		showHidden = b;
		currentFileList = null;
		reload();
	}

	@Command
	public void onOpenFile() throws IOException, SearchLibException {
		if (currentFile != null)
			if (currentFile.file.isDirectory())
				setCurrentFolder(currentFile);
	}

	@Command
	public void onSelectFile() throws SearchLibException {
		if (currentFile != null) {
			currentFilePath.setPath(currentFile.file.getAbsolutePath());
			reload();
		}
	}

	@Command
	@NotifyChange("*")
	public void onRefreshList() {
		currentFileList = null;
	}

	@Command
	public void onParentFolder() throws IOException, SearchLibException {
		if (currentFolder != null)
			setCurrentFolder(currentFolder.file.getParentFile());
	}

	public boolean isDropbox() {
		if (currentFilePath == null)
			return false;
		return currentFilePath.getType().is(DropboxFileInstance.class);
	}

	@Command
	public void onDropboxAuthRequest() throws MalformedURLException,
			SearchLibException {
		webAuthInfo = DropboxFileInstance.requestAuthorization();
		reload();
		throw new SearchLibException("Not yet implemented");
		// Executions.getCurrent().sendRedirect(null, "_blank");
	}

	@Command
	public void onDropboxConfirmAuth() throws SearchLibException,
			InterruptedException {
		StringBuffer uid = new StringBuffer();
		String atp = DropboxFileInstance.retrieveAccessToken(webAuthInfo, uid);
		if (uid.length() == 0) {
			new AlertController("The Dropbox authentication process failed");
			return;
		}
		currentFilePath.setHost(uid.toString() + ".dropbox.com");
		currentFilePath.setUsername("");
		currentFilePath.setPassword(atp);
		reload();
	}

	public boolean isDropboxWebAuthInfo() {
		return webAuthInfo != null;
	}

	public boolean isNotDropboxWebAuthInfo() {
		return !isDropboxWebAuthInfo();
	}

	public String getDropboxAuthUrl() throws SearchLibException {
		if (webAuthInfo == null)
			return null;
		throw new SearchLibException("Not yet implemented");
	}

}
