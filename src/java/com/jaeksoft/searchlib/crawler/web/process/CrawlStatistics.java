/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.crawler.web.process;

import java.util.Date;

public class CrawlStatistics {

	private CrawlStatistics parent;
	private volatile Date startDate;
	private volatile long startTime;
	private volatile float fetchRate;
	private volatile long fetchedCount;
	private volatile long pendingDeleteCount;
	private volatile long deletedCount;
	private volatile long parsedCount;
	private volatile long pendingUpdatedCount;
	private volatile long updatedCount;
	private volatile long pendingNewUrlCount;
	private volatile long newUrlCount;
	private volatile long ignoredCount;
	private volatile long urlListSize;
	private volatile long urlCount;
	private volatile long newHostListSize;
	private volatile long oldHostListSize;
	private volatile long newHostCount;
	private volatile long oldHostCount;

	public CrawlStatistics() {
		this(null);
	}

	public CrawlStatistics(CrawlStatistics parent) {
		this.parent = parent;
		reset();
	}

	public void reset() {
		synchronized (this) {
			startTime = System.currentTimeMillis();
			startDate = new Date(startTime);
			oldHostListSize = 0;
			newHostListSize = 0;
			fetchedCount = 0;
			pendingDeleteCount = 0;
			deletedCount = 0;
			parsedCount = 0;
			pendingUpdatedCount = 0;
			updatedCount = 0;
			pendingNewUrlCount = 0;
			newUrlCount = 0;
			ignoredCount = 0;
			fetchRate = 0;
			oldHostCount = 0;
			newHostCount = 0;
			urlListSize = 0;
			urlCount = 0;
		}
	}

	public void incPendingDeletedCount() {
		synchronized (this) {
			pendingDeleteCount++;
		}
		if (parent != null)
			parent.incPendingDeletedCount();
	}

	public void incPendingUpdatedCount() {
		synchronized (this) {
			pendingUpdatedCount++;
		}
		if (parent != null)
			parent.incPendingUpdatedCount();
	}

	public void addDeletedCount(long value) {
		synchronized (this) {
			this.deletedCount += value;
			this.pendingDeleteCount -= value;
		}
		if (parent != null)
			parent.addDeletedCount(value);
	}

	public void addPendingNewUrlCount(long value) {
		synchronized (this) {
			this.pendingNewUrlCount += value;
		}
		if (parent != null)
			parent.addPendingNewUrlCount(value);
	}

	public void addNewUrlCount(long value) {
		synchronized (this) {
			this.newUrlCount += value;
			this.pendingNewUrlCount -= value;
		}
		if (parent != null)
			parent.addNewUrlCount(value);
	}

	protected void incFetchedCount() {
		synchronized (this) {
			fetchedCount++;
			fetchRate = (float) fetchedCount
					/ ((float) (System.currentTimeMillis() - startTime) / 60000);
		}
		if (parent != null)
			parent.incFetchedCount();
	}

	protected void incParsedCount() {
		synchronized (this) {
			parsedCount++;
		}
		if (parent != null)
			parent.incParsedCount();
	}

	public void addUpdatedCount(long value) {
		synchronized (this) {
			this.updatedCount += value;
			this.pendingUpdatedCount -= value;
		}
		if (parent != null)
			parent.addUpdatedCount(value);
	}

	public void addUrlListSize(long value) {
		synchronized (this) {
			this.urlListSize += value;
		}
		if (parent != null)
			parent.addUrlListSize(value);
	}

	protected void incIgnoredCount() {
		synchronized (this) {
			ignoredCount++;
		}
		if (parent != null)
			parent.incIgnoredCount();
	}

	protected void incUrlCount() {
		synchronized (this) {
			urlCount++;
		}
		if (parent != null)
			parent.incUrlCount();
	}

	public void addNewHostListSize(long value) {
		synchronized (this) {
			newHostListSize += value;
		}
		if (parent != null)
			parent.addNewHostListSize(value);
	}

	public void addOldHostListSize(long value) {
		synchronized (this) {
			oldHostListSize += value;
		}
		if (parent != null)
			parent.addOldHostListSize(value);
	}

	public void incOldHostCount() {
		synchronized (this) {
			oldHostCount++;
		}
		if (parent != null)
			parent.incOldHostCount();
	}

	public void incNewHostCount() {
		synchronized (this) {
			newHostCount++;
		}
		if (parent != null)
			parent.incNewHostCount();
	}

	public long getOldHostListSize() {
		return oldHostListSize;
	}

	public long getNewHostListSize() {
		return newHostListSize;
	}

	public Date getStartDate() {
		return startDate;
	}

	public long getFetchedCount() {
		return fetchedCount;
	}

	public double getFetchRate() {
		return fetchRate;
	}

	public long getPendingDeletedCount() {
		return pendingDeleteCount;
	}

	public long getDeletedCount() {
		return deletedCount;
	}

	public long getParsedCount() {
		return parsedCount;
	}

	public long getPendingUpdatedCount() {
		return pendingUpdatedCount;
	}

	public long getUpdatedCount() {
		return updatedCount;
	}

	public long getPendingNewUrlCount() {
		return pendingNewUrlCount;
	}

	public long getNewUrlCount() {
		return newUrlCount;
	}

	public long getIgnoredCount() {
		return ignoredCount;
	}

	public long getOldHostCount() {
		return oldHostCount;
	}

	public long getNewHostCount() {
		return newHostCount;
	}

	public long getUrlCount() {
		return urlCount;
	}

	public long getUrlListSize() {
		return urlListSize;
	}

}