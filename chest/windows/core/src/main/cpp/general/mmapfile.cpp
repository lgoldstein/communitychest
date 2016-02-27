#include <win32/general.h>
#include <util/string.h>
#include <util/math.h>

/*--------------------------------------------------------------------------*/

HRESULT mmap_fhmap (HANDLE hfMap, const BOOL fReadOnly, LPVOID *ppBuf)
{
	if (NULL == ppBuf)
		return ERROR_BAD_ARGUMENTS;
	*ppBuf = NULL;

	if (IsBadHandle(hfMap))
		return ERROR_INVALID_HANDLE;

	*ppBuf = MapViewOfFile(hfMap,			// file-mapping object to map into address space
							    (fReadOnly ? FILE_MAP_READ : FILE_MAP_WRITE),// access mode
								 0, 0,				// high/low-order 32 bits of file offset
								 0);				// number of bytes to map (0 == entire file)
	if (NULL == *ppBuf)
	{
		HRESULT	hr=GetLastError();
		return hr;
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT mmap_fhndl (HANDLE hFile, const BOOL fReadOnly, LPVOID *ppBuf)
{
	if (NULL == ppBuf)
		return ERROR_BAD_ARGUMENTS;
	*ppBuf = NULL;

	HRESULT	hr=ERROR_SUCCESS;
	if (IsBadHandle(hFile))
		return ERROR_INVALID_HANDLE;

	BY_HANDLE_FILE_INFORMATION	bhfi;
	memset(&bhfi, 0, (sizeof bhfi));
	if (!GetFileInformationByHandle(hFile, &bhfi))
	{
		hr = GetLastError();
		return hr;
	}

	if (bhfi.nFileSizeHigh != 0)
		return ERROR_BUFFER_OVERFLOW;

	HANDLE	hfMap=CreateFileMapping(hFile,					// handle to file to map
												NULL,						// optional security attributes
												(fReadOnly ? PAGE_READONLY : PAGE_READWRITE),		// protection for mapping object
												bhfi.nFileSizeHigh,	// high-order 32 bits of object size
												bhfi.nFileSizeLow,	// low-order 32 bits of object size
												NULL);					// name of file-mapping object
	CHandleGuard	hg(hfMap);
	if (IsBadHandle(hfMap))
	{
		hr = GetLastError();
		return hr;
	}

	return mmap_fhmap(hfMap, fReadOnly, ppBuf);
}

/*--------------------------------------------------------------------------*/

HRESULT mmap_file (LPCTSTR lpszFile, const BOOL fReadOnly, LPVOID *ppBuf)
{
	if (NULL == ppBuf)
		return ERROR_BAD_ARGUMENTS;
	*ppBuf = NULL;

	if (IsEmptyStr(lpszFile))
		return ERROR_BAD_ENVIRONMENT;

	HANDLE	hFile=CreateFile(lpszFile,
									  GENERIC_READ | (fReadOnly ? 0 : GENERIC_WRITE),	// access mode
									  FILE_SHARE_READ,					// share mode
									  NULL,									// security attributes
									  OPEN_EXISTING,						// how to create
									  FILE_ATTRIBUTE_NORMAL,			// file attributes
									  (HANDLE) NULL);						// handle to file with attributes to copy
	CHandleGuard	hg(hFile);
	if (IsBadHandle(hFile))
	{
		HRESULT hr=GetLastError();
		return hr;
	}

	return mmap_fhndl(hFile, fReadOnly, ppBuf);
}

//////////////////////////////////////////////////////////////////////////////

// minimum is nextIndex + prevIndex + keyLen + dataLen + EOS + 1 key char + 1 data byte
#define MINSMESTRUCTSIZE	((4 * sizeof(DWORD)) + (2 * sizeof(TCHAR)) + 1)

static DWORD AdjustEntryOffset (const DWORD ulMaxKeyLen, const DWORD dwMaxValSize)
{
	DWORD	ulEffSize=MINSMESTRUCTSIZE + ulMaxKeyLen + dwMaxValSize, ulRemLen=ulEffSize % sizeof(NATIVE_WORD);
	if (ulRemLen != 0)
		ulEffSize += (sizeof(NATIVE_WORD) - ulRemLen);

	return ulEffSize;
}

DWORD CSharedMemMap::GetMapMemorySize (const DWORD	dwMapSize,
													const DWORD	dwMaxKeyLen,
													const DWORD	dwMaxValues,
													const DWORD	dwMaxValSize)
{
	return sizeof(SMMDESC) + dwMapSize * sizeof(DWORD) + dwMaxValues * AdjustEntryOffset(dwMaxKeyLen, dwMaxValSize);
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::InitSharedMapEntryStruct (LPBYTE	pEntry, const DWORD ulMaxEntrySize, SMESTRUCT& sme)
{
	memset(&sme, 0, (sizeof sme));

	if ((NULL == pEntry) || (ulMaxEntrySize < MINSMESTRUCTSIZE))
		return ERROR_BAD_ARGUMENTS;

	// NOTE !!! the order MUST be same as in "UpdateSharedMapEntryStruct"
	DWORD	dwCurOffset=0;
	{
		DWORD	*vals[]={ &sme.nextIndex, &sme.prevIndex, &sme.keyLen, &sme.dataLen };
		for (unsigned vIndex=0; vIndex < 4; vIndex++, dwCurOffset += sizeof(DWORD))
			memcpy(vals[vIndex], pEntry + dwCurOffset, sizeof(DWORD));
	}

	sme.lpszKey = (LPCTSTR) (pEntry + dwCurOffset);
	sme.pData = pEntry + (dwCurOffset + (sme.keyLen + 1) * sizeof(TCHAR));

	if (sme.dataLen > 0)
	{
		// make sure data does not extend beyond entry size
		if (((sme.pData - pEntry) + sme.dataLen) > ulMaxEntrySize)
			return ERROR_BUFFER_OVERFLOW;

		sme.pData[sme.dataLen] = 0;
	}
	else	// empty data
	{
		sme.pData[0] = 0;

		// make sure key without data does not extend beyond entry size
		if ((dwCurOffset + (sme.keyLen + 1) * sizeof(TCHAR)) > ulMaxEntrySize)
			return ERROR_BUFFER_OVERFLOW;

		sme.pData = NULL;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::UpdateSharedMapEntryStruct (const SMESTRUCT& sme, LPBYTE pEntry, const DWORD ulMaxEntrySize)
{
	if ((NULL == pEntry) || (ulMaxEntrySize < MINSMESTRUCTSIZE))
		return ERROR_BAD_ARGUMENTS;

	// NOTE !!! the order MUST be same as in "InitSharedMapEntryStruct"
	DWORD			dwCurOffset=0;
	{
		const DWORD *vals[]={ &sme.nextIndex, &sme.prevIndex, &sme.keyLen, &sme.dataLen };
		for (unsigned vIndex=0; vIndex < 4; vIndex++, dwCurOffset += sizeof(DWORD))
			memcpy(pEntry + dwCurOffset, vals[vIndex], sizeof(DWORD));
	}

	// make sure data does not extend beyond entry size
	if ((dwCurOffset + (sme.keyLen + 1) * sizeof(TCHAR) + sme.dataLen) > ulMaxEntrySize)
		return ERROR_BUFFER_OVERFLOW;

	// add the key
	{
		LPTSTR	lpszKeyPos=(LPTSTR) (pEntry + dwCurOffset);
		if (sme.keyLen > 0)
			_tcsncpy(lpszKeyPos, GetSafeStrPtr(sme.lpszKey), sme.keyLen);
		lpszKeyPos[sme.keyLen] = _T('\0');

		dwCurOffset += (sme.keyLen + 1) * sizeof(TCHAR);
	}

	LPBYTE	pData=pEntry + dwCurOffset;
	if (sme.dataLen > 0)
	{
		if (NULL == sme.pData)
			return ERROR_BAD_ARGUMENTS;

		memcpy(pData, sme.pData, sme.dataLen);
	}

	pData[sme.dataLen] = 0;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

CSharedMemMap::CSharedMemMap ()
	: m_hDesc(NULL)
	, m_pDesc(NULL)
	, m_pMapItems(NULL)
	, m_pMapValues(NULL)
{
}

/*--------------------------------------------------------------------------*/

BOOL CSharedMemMap::IsInitialized () const
{
	return ((m_pDesc != NULL) && (m_pMapItems != NULL) && (m_pMapValues != NULL) && (!IsBadHandle(m_hDesc)));
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::GetAttachedMappingSize (LPCTSTR lpszMapName, DWORD& ulMapSize)
{
	if (IsEmptyStr(lpszMapName))
		return ERROR_BAD_ARGUMENTS;

	/*	NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
	 *		As per MSDN documentation, name may not contain '\' - except for
	 * special names "Global\xxx" or "Local\xxx" used for Terminal Services
	 * cases - it is assumed this object is NOT used with such cases.
	 */
	if (::_tcschr(lpszMapName, _T('\\')) != NULL)
		return ERROR_BAD_FORMAT;

	HANDLE	hfMap=::CreateFileMapping(INVALID_HANDLE_VALUE,
												  NULL,						// optional security attributes
												  PAGE_READWRITE,
												  0,							// high-order 32 bits of object size
												  sizeof(SMMDESC),		// low-order 32 bits of object size
												  lpszMapName);			// name of file-mapping object
	HRESULT			hr=::GetLastError();
	CHandleGuard	hg(hfMap);

	if (IsBadHandle(hfMap))
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);
	if (hr != ERROR_ALREADY_EXISTS)
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);

	LPSMMDESC				pDesc=(LPSMMDESC) ::MapViewOfFile(hfMap, FILE_MAP_ALL_ACCESS, 0, 0, 0);
	CMappedFileViewGuard	dvg((LPVOID &) pDesc);
	if (NULL == pDesc)
	{
		hr = ::GetLastError();
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);
	}

	ulMapSize = GetMapMemorySize(pDesc->dwMapSize, pDesc->dwMaxKeyLen, pDesc->dwMaxValues, pDesc->dwMaxValSize);
	if ((LONG) ulMapSize <= (sizeof *pDesc))
		return ERROR_BUFFER_OVERFLOW;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CSharedMemMap::AttachSharedMap (LPCTSTR lpszMapName, HANDLE& hDesc, LPSMMDESC& pDesc)
{
	DWORD		ulMapSize=0;
	HRESULT	hr=GetAttachedMappingSize(lpszMapName, ulMapSize);
	if (hr != S_OK)
		return hr;

	hDesc = ::CreateFileMapping(INVALID_HANDLE_VALUE,
										 NULL,						// optional security attributes
										 PAGE_READWRITE,
										 0,							// high-order 32 bits of object size
										 ulMapSize,		// low-order 32 bits of object size
										 lpszMapName);			// name of file-mapping object
	hr = ::GetLastError();

	if (IsBadHandle(hDesc))
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);
	if (hr != ERROR_ALREADY_EXISTS)
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);

	if (NULL == (pDesc=(LPSMMDESC) ::MapViewOfFile(hDesc, FILE_MAP_ALL_ACCESS, 0, 0, 0)))
	{
		hr = ::GetLastError();
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

static HRESULT InitMapPointers (LPBYTE pData, const DWORD dwMapSize, LPDWORD& pMapItems, LPBYTE& pMapValues)
{
	if ((NULL == pData) || (dwMapSize <= 1))
		return ERROR_BAD_ARGUMENTS;

	pMapItems = (LPDWORD) pData;
	pMapValues = pData + dwMapSize * sizeof(DWORD);

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::Attach (LPCTSTR lpszMapName)
{
	if (IsInitialized())
		return ERROR_ALREADY_INITIALIZED;

	HRESULT	hr=AttachSharedMap(lpszMapName, m_hDesc, m_pDesc);
	if (hr != S_OK)
		return hr;

	if ((hr=::InitMapPointers(m_pDesc->bData, m_pDesc->dwMapSize, m_pMapItems, m_pMapValues)) != S_OK)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::CreateSharedMap (LPCTSTR			lpszMapName,
													 const DWORD	dwMapSize,
													 const DWORD	dwMaxKeyLen,
													 const DWORD	dwMaxValues,
													 const DWORD	dwMaxValSize,
													 const BOOL		fCaseSensitive,
													 HANDLE&			hDesc,
													 LPSMMDESC&		pDesc)
{
	if (IsEmptyStr(lpszMapName) ||
		(dwMapSize <= 1) || (dwMaxKeyLen <= 1) ||
		(dwMaxValues <= 1) || (dwMaxValSize <= 1))
		return ERROR_BAD_ARGUMENTS;

	/*	NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
	 *		As per MSDN documentation, name may not contain '\' - except for
	 * special names "Global\xxx" or "Local\xxx" used for Terminal Services
	 * cases - it is assumed this object is NOT used with such cases.
	 */
	if (::_tcschr(lpszMapName, _T('\\')) != NULL)
		return ERROR_BAD_FORMAT;
	
	const DWORD	dwEffMapSize=::FindClosestPrime(dwMapSize),
					ulMapSize=GetMapMemorySize(dwEffMapSize, dwMaxKeyLen, dwMaxValues, dwMaxValSize);
	if ((LONG) ulMapSize <= (sizeof *pDesc))
		return ERROR_BUFFER_OVERFLOW;

	hDesc = ::CreateFileMapping(INVALID_HANDLE_VALUE,
										 NULL,						// optional security attributes
										 PAGE_READWRITE,
										 0,							// high-order 32 bits of object size
										 ulMapSize,		// low-order 32 bits of object size
										 lpszMapName);			// name of file-mapping object
	HRESULT	hr = ::GetLastError();

	if (IsBadHandle(hDesc) || (hr != S_OK))	// including ERROR_ALREADY_EXISTS
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);

	if (NULL == (pDesc=(LPSMMDESC) ::MapViewOfFile(hDesc, FILE_MAP_ALL_ACCESS, 0, 0, 0)))
	{
		hr = ::GetLastError();
		return ((S_OK == hr) ? ERROR_INVALID_HANDLE : hr);
	}

	pDesc->dwNumItems = 0;
	pDesc->dwMapSize = dwEffMapSize;
	pDesc->dwMaxKeyLen = dwMaxKeyLen;
	pDesc->dwMaxValues = dwMaxValues;
	pDesc->dwMaxValSize = dwMaxValSize;
	pDesc->dwEntrySize = ::AdjustEntryOffset(dwMaxKeyLen, dwMaxValSize);
	pDesc->fCaseSensitive = fCaseSensitive;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::Create (LPCTSTR			lpszMapName,
										 const DWORD	dwMapSize,
										 const DWORD	dwMaxKeyLen,
										 const DWORD	dwMaxValues,
										 const DWORD	dwMaxValSize,
										 const BOOL		fCaseSensitive)
{
	HRESULT	hr=Attach(lpszMapName);
	if (S_OK == hr)
		return ERROR_ALREADY_EXISTS;

	if (IsInitialized())
		return ERROR_ALREADY_INITIALIZED;

	if ((hr=CreateSharedMap(lpszMapName, dwMapSize, dwMaxKeyLen, dwMaxValues, dwMaxValSize, fCaseSensitive, m_hDesc, m_pDesc)) != S_OK)
		return hr;

	if ((hr=::InitMapPointers(m_pDesc->bData, m_pDesc->dwMapSize, m_pMapItems, m_pMapValues)) != S_OK)
		return hr;

	if ((hr=Clear()) != S_OK)
		return hr;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

// returns hash value normalized to actual table size
HRESULT CSharedMemMap::GetHashIndex (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwHashIdx) const
{
	if (!IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	UINT32	ulHashVal=0;
	HRESULT	hr=CStr2PtrMapper::GetHashValue (lpszKey, dwKeyLen, m_pDesc->fCaseSensitive, ulHashVal);
	if (hr != S_OK)
		return hr;

	if (0 == m_pDesc->dwMapSize)
		return ERROR_FLOPPY_ID_MARK_NOT_FOUND;

	dwHashIdx = ulHashVal % m_pDesc->dwMapSize;
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::GetValueStruct (const DWORD dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const
{
	if (!IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	if (dwValIndex >= m_pDesc->dwMaxValues)
		return ERROR_FLOPPY_BAD_REGISTERS;

	pEntry = m_pMapValues + dwValIndex * m_pDesc->dwEntrySize;

	return InitSharedMapEntryStruct(pEntry, m_pDesc->dwMaxValSize, sme);
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::GetEntryStruct (const DWORD dwMapIndex, DWORD& dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const
{
	if (!IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	if (dwMapIndex >= m_pDesc->dwMapSize)
		return ERROR_EOM_OVERFLOW;

	// if empty hash linked list then same as if entry not found
	if ((dwValIndex=m_pMapItems[dwMapIndex]) >= m_pDesc->dwMaxValues)
		return ERROR_SECTOR_NOT_FOUND;

	pEntry = m_pMapValues + dwValIndex * m_pDesc->dwEntrySize;

	return InitSharedMapEntryStruct(pEntry, m_pDesc->dwMaxValSize, sme);
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::GetEntryStruct (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const
{
	DWORD		dwHashIdx=0;
	HRESULT	hr=GetHashIndex(lpszKey, dwKeyLen, dwHashIdx);
	if (hr != S_OK)
		return hr;

	if (dwHashIdx >= m_pDesc->dwMapSize)
		return ERROR_FLOPPY_WRONG_CYLINDER;

	/*			We could do a FOREVER loop, but obviously, if we go over
	 * "maxValues" tries the entry is not there, and moreover, it is
	 * obviously an error since it points to a circular linked list.
	 */
	for (DWORD	dwValCount=0; dwValCount < m_pDesc->dwMaxValues; dwValCount++)
	{
		if (dwValCount > 0)
			hr = GetValueStruct(dwValIndex, pEntry, sme);
		else
			hr = GetEntryStruct(dwHashIdx, dwValIndex, pEntry, sme);
		if (hr != S_OK)
			return hr;

		if (sme.keyLen == dwKeyLen)
		{
			// empty key matches regardless of case
			if (0 == dwKeyLen)
				return S_OK;

			if (m_pDesc->fCaseSensitive)
			{
				if (0 == ::_tcsncmp(lpszKey, sme.lpszKey, dwKeyLen))
					return S_OK;
			}
			else
			{
				if (0 == ::_tcsnicmp(lpszKey, sme.lpszKey, dwKeyLen))
					return S_OK;
			}
		}

		// if no next value then entry not found
		if ((dwValIndex=sme.nextIndex) >= m_pDesc->dwMaxValues)
			return ERROR_SECTOR_NOT_FOUND;
	}

	// this point should not be reached
	return ERROR_DISK_RECALIBRATE_FAILED;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::FindValue (LPCTSTR lpszKey, const DWORD dwKeyLen, LPVOID& pVal, DWORD& vLen) const
{
	SMESTRUCT	sme={ 0 };
	LPBYTE		pEntry=NULL;
	DWORD			dwValIndex=0;
	HRESULT		hr=GetEntryStruct(lpszKey, dwKeyLen, dwValIndex, pEntry, sme);
	if (hr != S_OK)
		return hr;

	pVal = sme.pData;
	vLen = sme.dataLen;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::FindNumValue (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwValue) const
{
	DWORD		vLen=0;
	LPVOID	pVal=0;
	HRESULT	hr=FindValue(lpszKey, dwKeyLen, pVal, vLen);
	if (hr != S_OK)
		return hr;

	if (vLen != (sizeof dwValue))
		return ERROR_BAD_FORMAT;

	::memcpy(&dwValue, pVal, (sizeof dwValue));
	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::AddValue (LPCTSTR lpszKey, const DWORD dwKeyLen /* may be 0 */, LPCVOID pVal, const DWORD vLen /* may be 0 */)
{
	SMESTRUCT	sme={ 0 };
	LPBYTE		pEntry=NULL;
	DWORD			dwValIndex=0;
	HRESULT		hr=GetEntryStruct(lpszKey, dwKeyLen, dwValIndex, pEntry, sme);
	if (S_OK == hr)	// if already have the value, simply override it
	{
		if ((sme.dataLen=vLen) > m_pDesc->dwMaxValSize)
			return ERROR_BUFFER_OVERFLOW;

		sme.pData = (LPBYTE) pVal;

		if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
			return hr;
	}
	else	// new value
	{
		DWORD		dwHashIdx=0;

		// check where to place this new value
		if ((hr=GetHashIndex(lpszKey, dwKeyLen, dwHashIdx)) != S_OK)
			return hr;

		// check if have free values on the heap
		dwValIndex = m_pDesc->dwFreeIndex;
		if ((hr=GetValueStruct(dwValIndex, pEntry, sme)) != S_OK)
			return ERROR_NOT_ENOUGH_SERVER_MEMORY;
		// will need it to update the free heap
		const DWORD	dwNextFreeIndex=sme.nextIndex;

		sme.lpszKey = lpszKey;
		sme.keyLen = dwKeyLen;
		sme.pData = (LPBYTE) pVal;
		sme.dataLen = vLen;
		sme.prevIndex = m_pDesc->dwMaxValues;
		// place new value as first in the hash chain (if any)
		sme.nextIndex = m_pMapItems[dwHashIdx];

		/* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
		 *		The following failures are unexpected and may cause
		 *	map/heap corruption if they occur
		 */

		if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
			return ERROR_POSSIBLE_DEADLOCK;	// return a special error code for this failure

		// check if had some other sibling in the hash chain
		if (sme.nextIndex < m_pDesc->dwMaxValues)
		{
			if ((hr=GetValueStruct(sme.nextIndex, pEntry, sme)) != S_OK)
				return ERROR_TOO_MANY_LINKS;	// return special error

			// make the sibling point to the newly added entry
			sme.prevIndex = m_pDesc->dwFreeIndex;

			if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
				return ERROR_NO_ASSOCIATION;
		}

		// go to next free value on the heap
		m_pMapItems[dwHashIdx] = dwValIndex;
		m_pDesc->dwFreeIndex = dwNextFreeIndex;

		// update the "prevIndex" of the next free value in the heap
		if (S_OK == (hr=GetValueStruct(dwNextFreeIndex, pEntry, sme)))
		{
			sme.prevIndex = m_pDesc->dwMaxValues;

			if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
				return ERROR_POSSIBLE_DEADLOCK;	// return a special error code for this failure
		}

		m_pDesc->dwNumItems++;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::DeleteKey (LPCTSTR lpszKey, const DWORD dwKeyLen)
{
	SMESTRUCT	sme={ 0 };
	LPBYTE		pEntry=NULL;
	DWORD			dwValIndex=0;
	HRESULT		hr=GetEntryStruct(lpszKey, dwKeyLen, dwValIndex, pEntry, sme);
	if (hr != S_OK)
		return hr;

	// we will need this to see if we need to update the hash chain
	const DWORD	dwPrevHashIdx=sme.prevIndex, dwNextHashIndex=sme.nextIndex;

	// place removed entry as first in free heap
	::memset(&sme, 0, (sizeof sme));
	sme.nextIndex = m_pDesc->dwFreeIndex;
	sme.prevIndex = m_pDesc->dwMaxValues;

	/* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
	 *		The following failures are unexpected and may cause
	 *	map/heap corruption if they occur
	 */
	if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
		return ERROR_MESSAGE_SYNC_ONLY;

	m_pDesc->dwFreeIndex = dwValIndex;

	// check if need to link in the previous free element
	if (sme.nextIndex < m_pDesc->dwMaxValues)
	{
		if ((hr=GetValueStruct(sme.nextIndex, pEntry, sme)) != S_OK)
			return ERROR_DESTINATION_ELEMENT_FULL;

		sme.prevIndex = dwValIndex;

		if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
			return ERROR_ILLEGAL_ELEMENT_ADDRESS;
	}

	// update the map chains
	DWORD		dwHashIdx=0;
	if ((hr=GetHashIndex(lpszKey, dwKeyLen, dwHashIdx)) != S_OK)
		return ERROR_MAGAZINE_NOT_PRESENT;

	m_pMapItems[dwHashIdx] = dwNextHashIndex;

	// check if need to update the previous in chain
	if (dwPrevHashIdx < m_pDesc->dwMaxValues)
	{
		if ((hr=GetValueStruct(dwPrevHashIdx, pEntry, sme)) != S_OK)
			return ERROR_DEVICE_REINITIALIZATION_NEEDED;

		sme.nextIndex = dwNextHashIndex;

		if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
			return ERROR_DEVICE_REQUIRES_CLEANING;
	}

	// check if need to update the next in chain
	if (dwNextHashIndex < m_pDesc->dwMaxValues)
	{
		if ((hr=GetValueStruct(dwNextHashIndex, pEntry, sme)) != S_OK)
			return ERROR_DEVICE_DOOR_OPEN;

		sme.prevIndex = dwPrevHashIdx;

		if ((hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize)) != S_OK)
			return ERROR_DEVICE_NOT_CONNECTED;
	}

	if ((0 == m_pDesc->dwNumItems) || (m_pDesc->dwNumItems > m_pDesc->dwMaxValues))
		return ERROR_TOO_MANY_OPEN_FILES;

	m_pDesc->dwNumItems--;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::Clear ()
{
	if (!IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	m_pDesc->dwFreeIndex = 0;

	// mark all map items as empty
	for (DWORD	dwMapIndex=0; dwMapIndex < m_pDesc->dwMapSize; dwMapIndex++)
		m_pMapItems[dwMapIndex] = m_pDesc->dwMaxValues;

	// create a linked list of free values
	LPBYTE	pEntry=m_pMapValues;
	for (DWORD	dwValIndex=0; dwValIndex < m_pDesc->dwMaxValues; dwValIndex++, pEntry += m_pDesc->dwEntrySize)
	{
		const SMESTRUCT	sme={ dwValIndex + 1, ((dwValIndex > 0) ? dwValIndex - 1 : m_pDesc->dwMaxValues), 0, 0, NULL, NULL };
		HRESULT				hr=UpdateSharedMapEntryStruct(sme, pEntry, m_pDesc->dwMaxValSize);
		if (hr != S_OK)	// should not happen
			return hr;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::Detach ()
{
	HRESULT	hr=S_OK;

	if (m_pDesc != NULL)
	{
		CMappedFileViewGuard	fvg((LPVOID &) m_pDesc);
		HRESULT					lhr=fvg.Release();
		if (lhr != S_OK)
			hr = lhr;	// just for debugging
	}

	if (!IsBadHandle(m_hDesc))
	{
		if (!::CloseHandle(m_hDesc))
			hr = ::GetLastError();	// just for debugging
		m_hDesc = NULL;
	}

	m_pMapItems = NULL;
	m_pMapValues = NULL;

	return hr;
}

/*--------------------------------------------------------------------------*/

// dumps as CSV
HRESULT CSharedMemMap::Export (FILE *fout)
{
	if (NULL == fout)
		return ERROR_BAD_ARGUMENTS;
	if (!IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	::_ftprintf(fout, _T("mapIndex,valIndex,prevIndex,nextIndex,dataLen,key\n"));

	for (DWORD	dwMapIndex=0, dwValsNum=0; (dwMapIndex < m_pDesc->dwNumItems) && (dwValsNum < m_pDesc->dwMaxValues); dwMapIndex++)
	{
		for (DWORD	dwValIndex=m_pMapItems[dwMapIndex]; (dwValIndex < m_pDesc->dwMaxValues) && (dwValsNum < m_pDesc->dwNumItems); )
		{
			SMESTRUCT	sme={ 0 };
			LPBYTE		pEntry=NULL;
			HRESULT		hr=GetValueStruct(dwValIndex, pEntry, sme);
			if (hr != S_OK)
				return hr;

			::_ftprintf(fout, _T("%lu,%lu,%lu,%lu,%lu,%s\n"), dwMapIndex, dwValIndex, sme.prevIndex, sme.nextIndex, sme.dataLen, GetSafeStrPtr(sme.lpszKey));

			dwValIndex = sme.nextIndex;
			dwValsNum++;
		}
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMap::Export (LPCTSTR lpszFilePath)
{
	if (IsEmptyStr(lpszFilePath))
		return ERROR_BAD_ARGUMENTS;

	FILE		*fout=::_tfopen(lpszFilePath, _T("w"));
	HRESULT	hr=Export(fout);
	if (fout != NULL)
		::fclose(fout);

	return hr;
}

//////////////////////////////////////////////////////////////////////////////

CSharedMemMapEnum::CSharedMemMapEnum (const CSharedMemMap& smm)
	: m_smm(smm)
	, m_dwMapIndex(0)
	, m_dwItemNum(0)
	, m_dwValIndex(smm.GetMaxValues())
	, m_dwMapSize(smm.GetMapSize())
	, m_dwMaxVals(smm.GetMaxValues())
{
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMapEnum::GetFirst (LPCTSTR& lpszKey, LPVOID& pVal, DWORD& vLen)
{
	m_dwMapIndex = 0;
	m_dwItemNum = 0;
	m_dwValIndex = m_dwMaxVals;

	return GetNext(lpszKey, pVal, vLen);
}

/*--------------------------------------------------------------------------*/

HRESULT CSharedMemMapEnum::GetNext (LPCTSTR& lpszKey, LPVOID& pVal, DWORD& vLen)
{
	lpszKey = NULL;
	pVal = NULL;
	vLen = 0;

	if (!m_smm.IsInitialized())
		return ERROR_SERIAL_NO_DEVICE;

	for ( ; m_dwMapIndex < m_dwMapSize; m_dwMapIndex++, m_dwValIndex=m_dwMaxVals)
	{
		// make sure we do not have an infinite loop
		if (m_dwItemNum > m_smm.GetNumItems())
			return ERROR_HANDLE_DISK_FULL;

		// if at end of previous chain, check current chain head
		if (m_dwValIndex >= m_dwMaxVals)
		{
			// check if current map index contains any chain data
			if ((m_dwValIndex=m_smm.m_pMapItems[m_dwMapIndex]) >= m_dwMaxVals)
				continue;
		}

		CSharedMemMap::SMESTRUCT	sme={ 0 };
		LPBYTE							pEntry=NULL;
		HRESULT							hr=m_smm.GetValueStruct(m_dwValIndex, pEntry, sme);
		if (hr != S_OK)	// totally unexpected
			return ERROR_REM_NOT_LIST;

		lpszKey = sme.lpszKey;
		pVal = sme.pData;
		vLen = sme.dataLen;

		// if reached end of this chain, prepare for next
		if ((m_dwValIndex=sme.nextIndex) >= m_dwMaxVals)
			m_dwMapIndex++;

		m_dwItemNum++;

		return S_OK;
	}

	return ERROR_HANDLE_EOF;
}

//////////////////////////////////////////////////////////////////////////////
