/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface OsseLibrary extends Library {

	public OsseLibrary INSTANCE = (OsseLibrary) Native.loadLibrary(
			"OpenSearchServer_CLib", OsseLibrary.class);

	int OSSCLib_GetVersionInfo(char[] lpwszCompleteInfo, char[][] lpwszBuild,
			char[] lpwszOS, char[] lpwszCompiler, Pointer lpui32SizeOfPtr);

	Pointer OSSCLib_ExtErrInfo_Create();

	int OSSCLib_ExtErrInfo_GetErrorCode(Pointer hExtErrInfo);

	void OSSCLib_ExtErrInfo_Delete(Pointer hExtErrInfo);

	Pointer OSSCLib_Index_Create(WString wszIndexName, Pointer hExtErrInfo);

	boolean OSSCLib_Index_Close(Pointer hIndex, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_Begin(Pointer hIndex, Pointer hExtErrInfo);

	Pointer OSSCLib_Transact_Document_New(Pointer hTransact, Pointer hExtErrInfo);

	int OSSCLib_Transact_Document_AddStringTerms(Pointer hTransact,
			Pointer hDoc, WString lpwszFieldName, WString[] lplpTerm,
			int ui32NumberOfTerms, Pointer hExtErrInfo);

	boolean OSSCLib_Transact_RollBack(Pointer hTransact, Pointer hExtErrInfo);

	boolean OSSCLib_Transact_Commit(Pointer hTransact, Pointer lphDoc,
			long ui64NumberOfDocs, Pointer lpui64DocId, Pointer hExtErrInfo);

}