#include <util/string.h>
#include <util/memory.h>

#include <win32/registry.h>

/*---------------------------------------------------------------------------*/

static HRESULT CloseThisKey (LPVOID hKey)
{
	if (NULL == hKey)
		return ERROR_BAD_ARGUMENTS;

	if (((LPVOID) HKEY_LOCAL_MACHINE == hKey) ||
		 ((LPVOID) HKEY_CLASSES_ROOT == hKey) ||
		 ((LPVOID) HKEY_CURRENT_CONFIG == hKey) ||
		 ((LPVOID) HKEY_CURRENT_USER == hKey) ||
		 ((LPVOID) HKEY_LOCAL_MACHINE == hKey) ||
#ifdef WINNT
		 ((LPVOID) HKEY_PERFORMANCE_DATA == hKey) ||
#endif
#ifdef WIN95
		 ((LPVOID) HKEY_DYN_DATA == hKey) ||
#endif
		 ((LPVOID) HKEY_USERS == hKey))
		return ERROR_SUCCESS;

	return RegCloseKey((HKEY) hKey);
}

/*---------------------------------------------------------------------------*/

// quick retrieval of REG_DWORD value(s)
HRESULT RegistryClass::GetValue (LPCTSTR lpValueName, LPDWORD pdwValue) const
{
	if (NULL == pdwValue)
		return ERROR_BAD_ARGUMENTS;

	DWORD		dwLen=(sizeof *pdwValue), dwType=0;
	HRESULT	hr=GetValue(lpValueName, &dwType, (LPBYTE) pdwValue, &dwLen);

	// if successful, then make sure returned type is REG_DWORD)
	if (ERROR_SUCCESS == hr)
	{
		if (dwType != REG_DWORD)
			hr = ERROR_BAD_TOKEN_TYPE;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

// quick retrieval of REG_SZ and REG_MULTI_SZ value(s)
HRESULT RegistryClass::GetValue (LPCTSTR	lpValueName, LPTSTR	pszVal, const DWORD	dwMaxLen) const
{
	if (NULL == pszVal)
		return ERROR_BAD_ARGUMENTS;

	DWORD		dwLen=dwMaxLen, dwType=0;
	HRESULT	hr=GetValue(lpValueName, &dwType, (LPBYTE) pszVal, &dwLen);

	// make sure this is either a REG_SZ, REG_MULTI_SZ or REG_BINARY
	if (ERROR_SUCCESS == hr)
	{
		if ((dwType != REG_SZ) && (dwType != REG_MULTI_SZ) &&
			 (dwType != REG_EXPAND_SZ) && (dwType != REG_BINARY))
			hr = ERROR_BAD_TOKEN_TYPE;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

// quick retrieval for boolean flags (DWORD values => 0 == FALSE, anything else == TRUE)
HRESULT RegistryClass::GetValue (LPCTSTR lpValueName, BOOL& fValue) const
{
	DWORD		dwVal=0;
	HRESULT	hr=GetValue(lpValueName, &dwVal);
	if (ERROR_SUCCESS == hr)
		fValue = (dwVal != 0);

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClass::SetKeyLocation (LPVOID hKey)
{
	if (NULL == hKey)
		return ERROR_BAD_ARGUMENTS;
	if (m_hKey != NULL)
		return ERROR_BAD_ENVIRONMENT;

	m_hKey = hKey;
	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClass::SetRegLocation (LPVOID		hRoot,
													LPCTSTR		pszKeyPath,
													const DWORD	dwFlags,
													const BOOL	fCreateIt)
{
	HRESULT hr=ERROR_SUCCESS;

	if ((m_hKey != NULL) || (NULL == hRoot))
		return ERROR_BAD_ARGUMENTS;

	// if no sub-path specified then use only the key
	if (IsEmptyStr(pszKeyPath))
		return SetKeyLocation(hRoot);

	// if key not found (and allowed to) then create it
	if (fCreateIt)
	{
		DWORD	dwDisposition=0;

		hr = RegCreateKeyEx((HKEY) hRoot, (LPCTSTR) pszKeyPath, 0, _T("REG_SZ"),
								  REG_OPTION_NON_VOLATILE, (REGSAM) dwFlags, NULL,
								  (PHKEY) &m_hKey, &dwDisposition);
	}
	else
		hr = RegOpenKeyEx((HKEY) hRoot,(LPCTSTR) pszKeyPath,0,dwFlags,(PHKEY) &m_hKey);

	if (ERROR_SUCCESS != hr)
		m_hKey = NULL;
	else
		m_fAuto = TRUE;

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClass::SetBasedLocation (const RegistryClass&	r,
													  LPCTSTR					pszSubKeyPath,
													  const DWORD				dwFlags,
													  const BOOL				fCreateIt)

{
	return SetRegLocation(r.m_hKey, pszSubKeyPath, dwFlags, fCreateIt);
}

/*---------------------------------------------------------------------------*/

// for WinNT we need to delete recursively ourselves
HRESULT RegistryClass::DeleteKey (LPCTSTR pszKey, const BOOL fRecursive)
{
	if ((NULL == m_hKey) || IsEmptyStr(pszKey))
		return ERROR_BAD_ARGUMENTS;

	if (fRecursive)
	{
		RegistryClass	rsk;
		HRESULT			hr=rsk.SetBasedLocation(*this, pszKey, KEY_ALL_ACCESS, FALSE);
		if (hr != ERROR_SUCCESS)
			return hr;

		static const UINT32	AVG_SUBKEYS_NUM=16;
		CVSDCollection		subKeys(AVG_SUBKEYS_NUM,AVG_SUBKEYS_NUM);
		RegistryClassEnum	rce(&rsk);

		static const UINT32	MAX_KEYNAME_LEN=64;
		TCHAR	szKeyName[MAX_KEYNAME_LEN+2]=_T("");
		DWORD	dwKNLen=(MAX_KEYNAME_LEN+1);

		// first, "collect" all keys since deletion changes enumeration
		for (hr=rce.GetFirstKey(szKeyName, &dwKNLen);
			 (ERROR_SUCCESS == hr);
			 dwKNLen=(MAX_KEYNAME_LEN+1), hr=rce.GetNextKey(szKeyName, &dwKNLen))
		{
			UINT32	ulKNLen=_tcslen(szKeyName);
			if ((hr=subKeys.AddItem(szKeyName, ((ulKNLen+1) * sizeof(TCHAR)))) != ERROR_SUCCESS)
				return hr;
		}

		// now delete recursively
		CVSDCollEnum	ske(subKeys);
		LPVOID			pItem=NULL;
		for (hr=ske.GetFirstItem(pItem); ERROR_SUCCESS == hr; hr=ske.GetNextItem(pItem))
			if ((hr=rsk.DeleteKey((LPCTSTR) pItem, TRUE)) != ERROR_SUCCESS)
				return hr;
	}

	return RegDeleteKey((HKEY) m_hKey, (LPCTSTR) pszKey);
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClass::Detach (void)
{
	HRESULT	hr=ERROR_SUCCESS;

	if ((NULL != m_hKey) && (m_fAuto))
		hr = CloseThisKey(m_hKey);

	m_hKey = NULL;
	m_fAuto = FALSE;

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RemoteRegistryClass::SetRemoteRegLocation (LPCTSTR		pszServer,
																	LPVOID		hKey,
																	LPCTSTR		pszKeyPath,
																	const DWORD	dwOptions,
																	const BOOL	fCreateIt)
{
	if (NULL == hKey)
		return ERROR_BAD_ARGUMENTS;
	if (NULL != m_hRemKey)
		return ERROR_BAD_ENVIRONMENT;

	HRESULT hr=RegConnectRegistry((LPTSTR) pszServer, (HKEY) hKey, (PHKEY) &m_hRemKey);
	if (ERROR_SUCCESS == hr)
	{
		hr = RegistryClass::SetRegLocation(m_hRemKey, pszKeyPath, dwOptions, fCreateIt);
		if (hr != ERROR_SUCCESS)
		{
			RegCloseKey((HKEY) m_hRemKey);
			m_hRemKey = NULL;
		}
		else
			m_pszServer = pszServer;
	}
	else
		m_hRemKey = NULL;

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RemoteRegistryClass::Detach (void)
{
	HRESULT	hr=ERROR_SUCCESS, lhr=ERROR_SUCCESS;

	if (NULL != m_hKey)
	{
		if ((lhr=CloseThisKey(m_hRemKey)) != ERROR_SUCCESS)
			hr = lhr;

		m_hRemKey = NULL;
		m_pszServer = NULL;
	}

	if ((lhr=RegistryClass::Detach()) != ERROR_SUCCESS)
		hr = lhr;

	return hr;
}

//////////////////////////////////////////////////////////////////////////////

HRESULT RegistryClassEnum::GetKey (LPTSTR		lpName,	// buffer for subkey name 
											  LPDWORD	lpcbName,// size of subkey buffer 
											  LPTSTR		lpClass, // buffer for class string 
											  LPDWORD	lpcbClass,// size of class buffer 
											  PFILETIME	lpftLastWriteTime) // last written
{
	FILETIME ft={ 0 }, *pft=((lpftLastWriteTime != NULL) ? lpftLastWriteTime : &ft);

	if ((NULL == m_hKey) || (NULL == lpName) || (!m_fIsKeyMode) ||
		 (NULL == lpcbName) || ((NULL != lpClass) && (NULL == lpcbClass)))
		return ERROR_BAD_ARGUMENTS;

	return ::RegEnumKeyEx((HKEY) m_hKey, m_dwIndex, lpName, lpcbName, NULL, lpClass, lpcbClass, pft);
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClassEnum::GetVal (LPTSTR		lpValueName,	// buffer for value string 
											  LPDWORD	lpcbValueName, // size of value buffer 
											  LPDWORD	lpType,			// type code 
											  LPBYTE		lpData,			// buffer for value data 
											  LPDWORD	lpcbData)		// size of data buffer
{
	if ((NULL == m_hKey) || (NULL == lpValueName) || m_fIsKeyMode ||
		 ((NULL != lpData) && (NULL == lpcbData)))
		return ERROR_BAD_ARGUMENTS;

	return ::RegEnumValue((HKEY) m_hKey, m_dwIndex, lpValueName, lpcbValueName, NULL, lpType, lpData, lpcbData);
}

/*---------------------------------------------------------------------------*/

HRESULT RegistryClassEnum::GetKey (LPTSTR lpName, const DWORD dwNameLen, PFILETIME lpftLastWriteTime, const bool fGetFirst)
{
	if ((NULL == lpName) || (dwNameLen <= 1))
		return ERROR_BAD_ARGUMENTS;

	DWORD		cbName=dwNameLen;
	HRESULT	hr=(fGetFirst ? GetFirstKey(lpName, &cbName, lpftLastWriteTime) : GetNextKey(lpName, &cbName, lpftLastWriteTime));
	if (hr != S_OK)
		return hr;
	if (cbName >= dwNameLen)
		return ERROR_BUFFER_OVERFLOW;
	lpName[cbName] = _T('\0');	// just making sure

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

/*		Returns a registry access object (or NULL) according to parameters:
 *
 * - if "pszRegServer" is NULL or empty then a local class is returned.
 */
HRESULT GetRegObj (LPCTSTR			pszRegServer,
						 LPVOID			hRootKey,
						 LPCTSTR			pszKeyPath,
						 LPREGCLASS&	pReg)
{
	pReg=NULL;

	if (NULL == hRootKey)
		return ERROR_INVALID_HANDLE;

	HRESULT			hr=ERROR_SUCCESS;
	if (!IsEmptyStr(pszRegServer))
	{
		RemoteRegistryClass	*rp=new RemoteRegistryClass;
		if (NULL == rp)
			return ERROR_NOT_ENOUGH_MEMORY;

		hr = rp->SetRemoteRegLocation(pszRegServer, hRootKey, pszKeyPath,
												KEY_READ, FALSE);
		pReg = rp;
	}
	else
	{
		RegistryClass	*rp=new RegistryClass;
		if (NULL == rp)
			return ERROR_NOT_ENOUGH_MEMORY;
		
		hr = rp->SetRegLocation(hRootKey, pszKeyPath, KEY_READ, FALSE);
		pReg = rp;
	}

	if (ERROR_SUCCESS != hr)
	{
		delete pReg;
		pReg = NULL;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	LPVOID	hKey;
	LPCTSTR	pszDisplayName;
} PREDEFKEY;

static const PREDEFKEY preDefKeys[]={
	{	(LPVOID) HKEY_CLASSES_ROOT,	_T("HKEY_CLASSES_ROOT")		},
	{	(LPVOID) HKEY_CURRENT_USER,	_T("HKEY_CURRENT_USER")		},
	{	(LPVOID) HKEY_LOCAL_MACHINE,	_T("HKEY_LOCAL_MACHINE")	},
	{	(LPVOID) HKEY_USERS,				_T("HKEY_USERS")				},
	{	(LPVOID) HKEY_CURRENT_CONFIG,	_T("HKEY_CURRENT_CONFIG")	},
	{	NULL,									NULL	}	// mark end of list
};

/*---------------------------------------------------------------------------*/

LPVOID PreDefKeysEnum::GetKeyByIndex (const ULONG ulIdx)
{
	LPVOID	pKey=NULL;

	if (ulIdx < PREDEF_REGKEYS_NUM)
		pKey = preDefKeys[ulIdx].hKey;

	return pKey;
}

/*---------------------------------------------------------------------------*/

/* returns one of the predefined keys (or NULL):
 *
 *			HKEY_CLASSES_ROOT
 *			HKEY_CURRENT_USER
 *			HKEY_LOCAL_MACHINE
 *			HKEY_USERS
 *			HKEY_CURRENT_CONFIG
 */
LPVOID GetPreDefinedRegKeyVal (LPCTSTR pszKeyName)
{
	const PREDEFKEY	*pKey=&preDefKeys[0];

	if ((NULL == pszKeyName) || (_T('\0') == *pszKeyName))
		return NULL;

	while ((pKey->hKey != NULL) && (pKey->pszDisplayName != NULL))
		if (stricmp(pszKeyName, pKey->pszDisplayName) == 0)
			return pKey->hKey;
		else
			pKey++;

	return NULL;
}

/*---------------------------------------------------------------------------*/

/* returns name of predefined key */
HRESULT GetPreDefinedRegKeyName (const LPVOID	hKey,
											LPTSTR			pszKeyName,
											const DWORD		dwSize)
{
	const PREDEFKEY	*pKey=&preDefKeys[0];

	if ((NULL == hKey) || (NULL == pszKeyName) || (0 == dwSize))
		return ERROR_BAD_ARGUMENTS;

	while ((pKey->hKey != NULL) && (pKey->pszDisplayName != NULL))
		if (pKey->hKey == hKey)
		{
			if (_tcslen(pKey->pszDisplayName) >= dwSize)
				return ERROR_NOT_ENOUGH_MEMORY;

			_tcscpy(pszKeyName, pKey->pszDisplayName);
			return ERROR_SUCCESS;
		}
		else
			pKey++;

	return ERROR_FILE_NOT_FOUND;
}

/*---------------------------------------------------------------------------*/
