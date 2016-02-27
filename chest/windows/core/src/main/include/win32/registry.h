#ifndef _REGISTRY_H_
#define _REGISTRY_H_

/*---------------------------------------------------------------------------*/
/*		Easy access for registry keys and values.
 */
/*---------------------------------------------------------------------------*/

#ifndef WIN32
#error "This file is intended for WIN32 !!!"
#endif

#include <wtypes.h>
#include <tchar.h>
#include <winreg.h>
#include <winerror.h>

#include <_types.h>

#ifndef __cplusplus
#error "This file requires a C++ compiler !!!"
#endif

/*---------------------------------------------------------------------------*/

class CRegKeyGuard {
	private:
		HKEY&	m_hKey;

	public:
		CRegKeyGuard (HKEY& hk)
			: m_hKey(hk)
		{
		}

		void Release ()
		{
			if (m_hKey != NULL)
			{
				::RegCloseKey(m_hKey);
				m_hKey = NULL;
			}
		}

		~CRegKeyGuard ()
		{
			Release();
		}
};

/*---------------------------------------------------------------------------*/

// (abstract) base class for registry interface
class RegistryInterface {
	public:
		virtual HRESULT GetValue (LPCTSTR	lpValueName,
										  LPDWORD	pdwType,
										  LPBYTE		pBuf,
										  LPDWORD	pdwLen) const = 0;

		// quick retrieval of REG_DWORD value(s)
		virtual HRESULT GetValue (LPCTSTR lpValueName, LPDWORD	pdwValue) const = 0;

		// quick retrieval of REG_SZ, REG_MULTI_SZ and REG_BINARY value(s)
		virtual HRESULT GetValue (LPCTSTR lpValueName, LPTSTR	pszVal, const DWORD	dwMaxLen) const = 0;

		// quick retrieval for boolean flags (DWORD values => 0 == FALSE, anything else == TRUE)
		virtual HRESULT GetValue (LPCTSTR lpValueName, BOOL& fValue) const = 0;

		virtual HRESULT GetInfo (LPTSTR		lpClass,		// class string 
										 LPDWORD		lpcbClass,	// size of class string
										 LPDWORD		lpcSubKeys,	// number of subkeys 
										 LPDWORD		lpcbMaxSubKeyLen,
										 LPDWORD		lpcbMaxClassLen,
										 LPDWORD		lpcValues,// number of value entries 
										 LPDWORD		lpcbMaxValueNameLen,
										 LPDWORD		lpcbMaxValueLen,
										 LPDWORD		lpcbSecurityDescriptor,
										 PFILETIME	lpftLastWriteTime) const = 0;

		// full options
		virtual HRESULT SetValue (LPCTSTR		lpValueName, 
										  const DWORD	dwType, 
										  CONST BYTE	*lpData, 
										  const DWORD	cbData) = 0;

		// quick setting of DWORD values
		virtual HRESULT SetValue (LPCTSTR lpValueName, const DWORD dwVal) = 0;

		// quick setting of string value
		virtual HRESULT SetStrValue (LPCTSTR lpValueName, LPCTSTR lpszValue)
		{
			return SetValue(lpValueName, REG_SZ, (BYTE *) lpszValue, ((NULL == lpszValue) ? 0 : ::_tcslen(lpszValue)) * sizeof(TCHAR));
		}

		virtual HRESULT DeleteValue (LPCTSTR lpValueName) = 0;

		// for WinNT we need to delete recursively ourselves
		virtual HRESULT DeleteKey (LPCTSTR		pszKey,
											const BOOL  fRecursive=FALSE) = 0;

		virtual HRESULT CreateKey (LPCTSTR		lpSubKey,	// subkey name 
											LPTSTR		lpClass,		// class string 
											const DWORD	dwOptions,	// special options
											REGSAM		samDesired,	// security access 
											LPSECURITY_ATTRIBUTES	lpSecurityAttributes, 
											LPVOID		*phkResult, 
											LPDWORD		lpdwDisposition) = 0;

		virtual HRESULT SaveKey (LPCTSTR						lpFileName,
										 LPSECURITY_ATTRIBUTES	lpSecurityAttributes) const = 0;

		virtual HRESULT LoadKey (LPCTSTR lpSubKey, LPCTSTR lpFile) = 0;

		virtual BOOL fIsInitialized (void) const = 0;

		virtual HRESULT Detach (void) = 0;
};	// end of registry interface base class

/*---------------------------------------------------------------------------*/

class RegistryClass : public RegistryInterface {
	private:
		BOOL	m_fAuto;

		// disable copy constructor and assignment operator
		RegistryClass (const RegistryClass& r);
		RegistryClass& operator= (const RegistryClass& r);

	protected:
		LPVOID	m_hKey;

	public:
		// Note: key is closed by destructor
		RegistryClass (LPVOID		hRoot,
							LPCTSTR		pszKeyPath,
							const DWORD	dwFlags=KEY_READ,
							const BOOL	fCreateIt=FALSE) : m_hKey(NULL),m_fAuto(FALSE)
		{
			SetRegLocation(hRoot, pszKeyPath, dwFlags, fCreateIt);
		}

		// Sets the root key to supplied parameter. Note: key is not closed
		// by destructor, unless requested
		RegistryClass (LPVOID hKey, const BOOL fAuto=FALSE)
			: m_hKey(NULL), m_fAuto(fAuto)
		{
			SetKeyLocation(hKey);
		}

		// default constructor
		RegistryClass () : m_hKey(NULL), m_fAuto(FALSE) { }

		RegistryClass (const RegistryClass&	r,
							LPCTSTR					pszSubKeyPath,
							const DWORD				dwFlags,
							const BOOL				fCreateIt)
			: m_hKey(NULL),m_fAuto(FALSE)
		{
			SetBasedLocation(r, pszSubKeyPath, dwFlags, fCreateIt);
		}

		virtual HRESULT SetKeyLocation (LPVOID hKey);

		// sets/opens the specified key (creates it if not exists and allowed to)
		virtual HRESULT SetRegLocation (LPVOID			hRoot,
												  LPCTSTR		pszKeyPath,
												  const DWORD	dwFlags,
												  const BOOL	fCreateIt);

		virtual HRESULT SetBasedLocation (const RegistryClass&	r,
													 LPCTSTR						pszSubKeyPath,
													 const DWORD				dwFlags,
													 const BOOL					fCreateIt);

		virtual LPVOID GetKeyHandle (void) const
		{
			return m_hKey;
		}

		virtual BOOL fIsInitialized (void) const
		{
			return (m_hKey != NULL);
		}

		virtual HRESULT GetValue (LPCTSTR		lpValueName,
										  LPDWORD		pdwType,
										  LPBYTE			pBuf,
										  LPDWORD		pdwLen) const
		{
			if (NULL == m_hKey)
				return ERROR_INVALID_HANDLE;
			else
				return RegQueryValueEx((HKEY) m_hKey,(LPCTSTR) lpValueName,
											  NULL, pdwType, pBuf, pdwLen);
		}

		// quick retrieval of REG_DWORD value(s)
		virtual HRESULT GetValue (LPCTSTR lpValueName, LPDWORD	pdwValue) const;

		// quick retrieval of REG_SZ, REG_MULTI_SZ and REG_BINARY value(s)
		virtual HRESULT GetValue (LPCTSTR lpValueName, LPTSTR	pszVal, const DWORD	dwMaxLen) const;

		// quick retrieval for boolean flags (DWORD values => 0 == FALSE, anything else == TRUE)
		virtual HRESULT GetValue (LPCTSTR lpValueName, BOOL& fValue) const;

		virtual HRESULT GetInfo (LPTSTR		lpClass,		// class string 
										 LPDWORD		lpcbClass,	// size of class string
										 LPDWORD		lpcSubKeys,	// number of subkeys 
										 LPDWORD		lpcbMaxSubKeyLen,
										 LPDWORD		lpcbMaxClassLen,
										 LPDWORD		lpcValues,// number of value entries 
										 LPDWORD		lpcbMaxValueNameLen,
										 LPDWORD		lpcbMaxValueLen,
										 LPDWORD		lpcbSecurityDescriptor,
										 PFILETIME	lpftLastWriteTime) const
		{
			if ((NULL == m_hKey) || ((NULL != lpClass) && (NULL == lpcbClass)))
				return ERROR_BAD_ARGUMENTS;
			return RegQueryInfoKey((HKEY) m_hKey, lpClass, lpcbClass, NULL,
										  lpcSubKeys, lpcbMaxSubKeyLen,
										  lpcbMaxClassLen, lpcValues,
										  lpcbMaxValueNameLen, lpcbMaxValueLen,
										  lpcbSecurityDescriptor, lpftLastWriteTime);
		}

		// full options
		virtual HRESULT SetValue (LPCTSTR		lpValueName, 
										  const DWORD	dwType, 
										  CONST BYTE	*lpData, 
										  const DWORD	cbData)
		{
			if ((NULL == m_hKey) || (NULL == lpData))
				return ERROR_BAD_ARGUMENTS;
			return RegSetValueEx((HKEY) m_hKey, (LPCTSTR) lpValueName,
										0, dwType, lpData, cbData);
		}

		// quick setting of DWORD values
		virtual HRESULT SetValue (LPCTSTR lpValueName, const DWORD dwVal)
		{
			return SetValue(lpValueName, REG_DWORD,
								 (CONST BYTE *) &dwVal, (sizeof dwVal));
		}

		virtual HRESULT DeleteValue (LPCTSTR lpValueName)
		{
			if ((NULL == m_hKey) || (NULL == lpValueName))
				return ERROR_BAD_ARGUMENTS;
			else
				return RegDeleteValue((HKEY) m_hKey, (LPCTSTR) lpValueName);
		}

		// for WinNT we need to delete recursively ourselves
		virtual HRESULT DeleteKey (LPCTSTR		pszKey,
											const BOOL	fRecursive=FALSE);

		virtual HRESULT CreateKey (LPCTSTR		lpSubKey,	// subkey name 
											LPTSTR		lpClass,		// class string 
											const DWORD	dwOptions,	// special options
											REGSAM		samDesired,	// security access 
											LPSECURITY_ATTRIBUTES	lpSecurityAttributes, 
											LPVOID		*phkResult, 
											LPDWORD		lpdwDisposition)
		{
			if ((NULL == m_hKey) || (NULL == lpSubKey) || (NULL == phkResult))
				return ERROR_BAD_ARGUMENTS;
			return RegCreateKeyEx((HKEY) m_hKey, (LPCTSTR) lpSubKey, 0,
										 lpClass, dwOptions, samDesired,
										 lpSecurityAttributes,
										 (PHKEY) phkResult,
										 lpdwDisposition);
		}

		virtual HRESULT SaveKey (LPCTSTR						lpFileName,
										 LPSECURITY_ATTRIBUTES	lpSecurityAttributes) const
		{
			if ((NULL == m_hKey) || (NULL == lpFileName))
				return ERROR_BAD_ARGUMENTS;
			return RegSaveKey((HKEY) m_hKey, (LPCTSTR) lpFileName, lpSecurityAttributes);
		}

		virtual HRESULT LoadKey (LPCTSTR lpSubKey, LPCTSTR lpFile)
		{
			if ((NULL == m_hKey) || (NULL == lpSubKey) || (NULL == lpFile))
				return ERROR_BAD_ARGUMENTS;
			return RegLoadKey((HKEY) m_hKey,(LPCTSTR) lpSubKey,(LPCTSTR) lpFile);
		}

		virtual HRESULT Detach (void);

		virtual ~RegistryClass () { Detach(); }

	friend class RegistryClassEnum;
};	// end of registry class definition

typedef RegistryClass *LPREGCLASS;
typedef CAllocStructPtrGuard<RegistryClass> CRegClassGuard;

/*---------------------------------------------------------------------------*/

class RemoteRegistryClass : public RegistryClass {
	private:
		LPVOID	m_hRemKey;
		LPCTSTR	m_pszServer;

		// disable copy constructor and assignment operator
		RemoteRegistryClass (const RemoteRegistryClass& r);
		RemoteRegistryClass& operator= (const RemoteRegistryClass& r);

	public:
		virtual HRESULT SetRemoteRegLocation (LPCTSTR		pszServer,
														  LPVOID			hKey,
														  LPCTSTR		pszKeyPath,
														  const DWORD	dwOptions,
														  const BOOL	fCreateIt);
		// default constructor
		RemoteRegistryClass ()
			: RegistryClass(), m_hRemKey(NULL), m_pszServer(NULL) { }

		virtual BOOL fIsInitialized (void) const
		{
			return (m_hKey != NULL);
		}

		virtual HRESULT Detach (void);

		virtual ~RemoteRegistryClass () { Detach(); }
};	// end of remote registry (derived) class

/*---------------------------------------------------------------------------*/

// enumeration class for either keys or value (but not both at once)
class RegistryClassEnum {
	private:
		LPVOID	m_hKey;
		DWORD		m_dwIndex;
		BOOL		m_fIsKeyMode;

		// disable copy constructor and assignment operator
		RegistryClassEnum (const RegistryClassEnum& e);
		RegistryClassEnum& operator= (const RegistryClassEnum& e);

		HRESULT GetKey (LPTSTR		lpName,	// buffer for subkey name 
							 LPDWORD		lpcbName,// size of subkey buffer 
							 LPTSTR		lpClass, // buffer for class string 
							 LPDWORD		lpcbClass,// size of class buffer 
							 PFILETIME	lpftLastWriteTime); // last written

		HRESULT GetVal (LPTSTR	lpValueName,	// buffer for value string 
							 LPDWORD	lpcbValueName, // size of value buffer 
							 LPDWORD	lpType,			// type code 
							 LPBYTE	lpData,			// buffer for value data 
							 LPDWORD	lpcbData);		// size of data buffer

		HRESULT GetKey (LPTSTR lpName, const DWORD dwNameLen, PFILETIME lpftLastWriteTime, const bool fGetFirst);

	public:
		BOOL SetParams (const RegistryClass *pCReg)
		{
			if (NULL == pCReg)
				m_hKey = NULL;
			else
				m_hKey = pCReg->m_hKey;
			return TRUE;
		}

		RegistryClassEnum (const RegistryClass *pCReg=NULL)
		{
			SetParams(pCReg);
		}

		virtual HRESULT GetFirstKey (LPTSTR		lpName,	// subkey name 
											  LPDWORD	lpcbName,// size of subkey buffer 
											  LPTSTR		lpClass, // class string 
											  LPDWORD	lpcbClass,// size of class buffer 
											  PFILETIME	lpftLastWriteTime)
		{
			m_dwIndex = 0;
			m_fIsKeyMode = TRUE;

			return GetKey(lpName,lpcbName,lpClass,lpcbClass,lpftLastWriteTime);
		}

		virtual HRESULT GetFirstKey (LPTSTR		lpName,	// subkey name 
											  LPDWORD	lpcbName,// size of subkey buffer 
											  PFILETIME	lpftLastWriteTime=NULL)
		{
			return GetFirstKey(lpName, lpcbName, NULL, NULL, lpftLastWriteTime);
		}

		virtual HRESULT GetFirstKey (LPTSTR lpName, const DWORD dwNameLen, PFILETIME	lpftLastWriteTime=NULL)
		{
			return GetKey(lpName, dwNameLen, lpftLastWriteTime, true);
		}

		// returns ERROR_NO_MORE_ITEMS when finished
		virtual HRESULT GetNextKey (LPTSTR		lpName,	// subkey name 
											 LPDWORD		lpcbName,// size of subkey buffer 
											 LPTSTR		lpClass, // class string 
											 LPDWORD		lpcbClass,// size of class buffer 
											 PFILETIME	lpftLastWriteTime)
		{
			m_dwIndex++;

			return GetKey(lpName, lpcbName, lpClass, lpcbClass, lpftLastWriteTime);
		}

		// returns ERROR_NO_MORE_ITEMS when finished
		virtual HRESULT GetNextKey (LPTSTR		lpName,	// subkey name 
											 LPDWORD		lpcbName,// size of subkey buffer 
											 PFILETIME	lpftLastWriteTime=NULL)
		{
			return GetNextKey(lpName, lpcbName, NULL, NULL, lpftLastWriteTime);
		}

		virtual HRESULT GetNextKey (LPTSTR lpName, const DWORD dwNameLen, PFILETIME	lpftLastWriteTime=NULL)
		{
			return GetKey(lpName, dwNameLen, lpftLastWriteTime, false);
		}

		virtual HRESULT GetFirstValue (LPTSTR	lpValueName,	// value string 
												 LPDWORD	lpcbValueName, // size of buffer 
												 LPDWORD	lpType,			// type code 
												 LPBYTE	lpData,			// value data 
												 LPDWORD	lpcbData)		// size of data
		{
			m_dwIndex = 0;
			m_fIsKeyMode = FALSE;

			return GetVal(lpValueName, lpcbValueName, lpType, lpData, lpcbData);
		}

		virtual HRESULT GetFirstValue (LPTSTR	lpValueName,	// value string 
												 LPDWORD	lpcbValueName) // size of buffer 
		{
			return GetFirstValue(lpValueName, lpcbValueName, NULL, NULL, NULL);
		}

		virtual HRESULT GetFirstValue (LPTSTR			lpValueName,// value string 
												 const DWORD	cbValueName)
		{
			DWORD	dwSize=cbValueName;

			return GetFirstValue(lpValueName, &dwSize);
		}

		// returns ERROR_NO_MORE_ITEMS when finished
		virtual HRESULT GetNextValue (LPTSTR	lpValueName,	// value string 
												LPDWORD	lpcbValueName, // size of buffer 
												LPDWORD	lpType,			// type code 
												LPBYTE	lpData,			// value data 
												LPDWORD	lpcbData)		// size of data
		{
			m_dwIndex++;
			return GetVal(lpValueName, lpcbValueName, lpType, lpData, lpcbData);
		}

		virtual HRESULT GetNextValue (LPTSTR	lpValueName,	// value string 
												LPDWORD	lpcbValueName) // size of buffer
		{
			return GetNextValue(lpValueName, lpcbValueName, NULL, NULL, NULL);
		}

		virtual HRESULT GetNextValue (LPTSTR		lpValueName,// value string 
												const DWORD	cbValueName)
		{
			DWORD	dwSize=cbValueName;
			return GetNextValue(lpValueName, &dwSize);
		}

		virtual ~RegistryClassEnum () {}
};	// end of registry key enumeration

/*---------------------------------------------------------------------------*/

/*		Returns a registry access object (or NULL) according to parameters:
 *
 * - if "pszRegServer" is NULL or empty then a local class is returned.
 */
extern HRESULT GetRegObj (LPCTSTR		pszRegServer,
								  LPVOID			hRootKey,
								  LPCTSTR		pszKeyPath,
								  LPREGCLASS&	pReg);

/*---------------------------------------------------------------------------*/

#define PREDEF_REGKEYS_NUM	5

/* returns one of the predefined keys (or NULL):
 *
 *			HKEY_CLASSES_ROOT
 *			HKEY_CURRENT_USER
 *			HKEY_LOCAL_MACHINE
 *			HKEY_USERS
 *			HKEY_CURRENT_CONFIG
 */
extern LPVOID GetPreDefinedRegKeyVal (LPCTSTR pszKeyName);

/* returns name of predefined key */
extern HRESULT GetPreDefinedRegKeyName (const LPVOID	hKey,
													 LPTSTR			pszKeyName,
													 const DWORD	dwSize);

/*---------------------------------------------------------------------------*/

class PreDefKeysEnum {
	private:
		ULONG		m_ulIdx;
		LPVOID	m_pPrevKey;

		static LPVOID GetKeyByIndex (const ULONG ulIdx);

	public:
		PreDefKeysEnum () { m_ulIdx = 0; m_pPrevKey = NULL; }
		virtual ~PreDefKeysEnum () {}

		virtual LPVOID GetFirstKey (void)
		{
			m_ulIdx = 0;
			m_pPrevKey = GetKeyByIndex(m_ulIdx);
			return m_pPrevKey;
		}

		virtual LPVOID GetNextKey (void)
		{
			if (m_pPrevKey != NULL)
			{
				m_ulIdx++;
				m_pPrevKey = GetKeyByIndex(m_ulIdx);
			}

			return m_pPrevKey;
		}
};

/*---------------------------------------------------------------------------*/

#endif	/* of ifdef _REGISTRY_H_ */
