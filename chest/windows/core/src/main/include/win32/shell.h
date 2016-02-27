#ifndef _WIN32UTL_SHELL_H_
#define _WIN32UTL_SHELL_H_

#include <wtypes.h>
#include <tchar.h>

#include <shlobj.h>

typedef IShellFolder *LPSHELLFOLDER;
typedef IShellLink	*LPSHELLLINK;
typedef IPersistFile	*LPPERSISTFILE;

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CCOMInitializer {
	private:
		HRESULT	m_hr;	// initialization error

	public:
		CCOMInitializer (LPVOID lpReserved=NULL)
		{
			m_hr = ::CoInitialize(lpReserved);
		}

		virtual BOOL IsInitialized () const
		{
			return ((S_OK == m_hr) || (S_FALSE == m_hr));
		}

		virtual HRESULT GetInitializationError () const
		{
			return m_hr;
		}

		virtual ~CCOMInitializer ()
		{
			if (IsInitialized())
				CoUninitialize();
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// general shell objects guard class
template<class SHTP> class CShellTypeGuard {
	protected:
		SHTP&	m_pObj;

		// disable copy constructor and assignment operator
		CShellTypeGuard (const CShellTypeGuard& );
		CShellTypeGuard& operator= (const CShellTypeGuard& );

	public:
		CShellTypeGuard (SHTP& pObj) : m_pObj(pObj) { }

		virtual ULONG Release ()
		{
			if (m_pObj != NULL)
			{
				ULONG	ulRefCount=m_pObj->Release();
				m_pObj = NULL;
				return ulRefCount;
			}

			return 0;
		}

		virtual ~CShellTypeGuard ()
		{ 
			const ULONG	ulRefCount=Release();
		}
};	// end of guard class template

typedef CShellTypeGuard<LPSHELLFOLDER> CShellFolderGuard;
typedef CShellTypeGuard<LPENUMIDLIST> CShellIDEnumGuard;
typedef CShellTypeGuard<LPSHELLLINK> CShellLinkGuard;
typedef CShellTypeGuard<LPPERSISTFILE> CPersistFileGuard;

#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CSHMallocInterface {
	private:
		LPMALLOC	m_pMalloc;

	public:
		virtual HRESULT ReInit ()
		{
			const ULONG	ulRefCount=Release();
			HRESULT		hr=::SHGetMalloc(&m_pMalloc);
			return hr;
		}

		CSHMallocInterface ()
			: m_pMalloc(NULL)
		{
			const HRESULT	hr=ReInit();
		}

		virtual ULONG Release ()
		{
			ULONG	ulRefCount=0;

			if (m_pMalloc != NULL)
			{
				ulRefCount = m_pMalloc->Release();
				m_pMalloc = NULL;
			}

			return ulRefCount;
		}

		virtual ULONG Attach (LPMALLOC pMalloc)
		{
			ULONG	ulRefCount=Release();
			if ((m_pMalloc=pMalloc) != NULL)
				ulRefCount = m_pMalloc->AddRef();
			else
				ulRefCount = 0;

			return ulRefCount;
		}

		CSHMallocInterface (LPMALLOC pMalloc)
			: m_pMalloc(NULL)
		{
			const ULONG	ulRefCount=Attach(pMalloc);
		}

		virtual ULONG Attach (const CSHMallocInterface& mi)
		{
			return Attach(mi.m_pMalloc);
		}

		CSHMallocInterface (const CSHMallocInterface& mi)
			: m_pMalloc(NULL)
		{
			const ULONG	ulRefCount=Attach(mi);
		}

		virtual CSHMallocInterface& operator= (const CSHMallocInterface& mi)
		{
			ULONG	ulRefCount=Release();
			ulRefCount = Attach(mi.m_pMalloc);
			return (*this);
		}

		virtual operator LPMALLOC()
		{
			return m_pMalloc;
		}

		virtual ~CSHMallocInterface ()
		{
			const ULONG	ulRefCount=Release();
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CSHItemIDListGuard {
	private:
		CSHMallocInterface	m_shmi;
		LPITEMIDLIST&			m_pIDL;

	public:
		CSHItemIDListGuard (LPITEMIDLIST& pIDL)
			: m_shmi(), m_pIDL(pIDL)
		{
		}

		CSHItemIDListGuard (CSHMallocInterface& shmi, LPITEMIDLIST& pIDL)
			: m_shmi(shmi), m_pIDL(pIDL)
		{
		}

		CSHItemIDListGuard (LPMALLOC pMalloc, LPITEMIDLIST& pIDL)
			: m_shmi(pMalloc), m_pIDL(pIDL)
		{
		}

		virtual ~CSHItemIDListGuard ()
		{
			if (m_pIDL != NULL)
			{
				LPMALLOC	pMalloc=m_shmi;
				if (NULL == pMalloc)
					throw ERROR_BAD_ENVIRONMENT;
				else
					pMalloc->Free((LPVOID) m_pIDL);

				m_pIDL = NULL;
			}
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* see "SHGetSpecialFolderLocation" for available folder ID(s) */
extern HRESULT GetSystemShellFolderPath (const int nFolderID, LPTSTR lpszPath, const ULONG ulMaxLen);

#ifdef __cplusplus
inline HRESULT GetStartupShellFolderPath (LPTSTR lpszPath, const ULONG ulMaxLen)
{
	return GetSystemShellFolderPath(CSIDL_STARTUP, lpszPath, ulMaxLen);
}

inline HRESULT GetDesktopShellFolderPath (LPTSTR lpszPath, const ULONG ulMaxLen)
{
	return GetSystemShellFolderPath(CSIDL_DESKTOP, lpszPath, ulMaxLen);
}
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#define SHELL_LINK_SUFFIX		_T("lnk")
#define SHELL_LINKFILE_SUFFIX	_T(".")##SHELL_LINK_SUFFIX

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern HRESULT GetShellLinkFileObject (LPCTSTR lpszFilePath, LPSHELLLINK& pSLObj, const DWORD dwMode);

inline HRESULT OpenShellLinkFileObject (LPCTSTR lpszFilePath, LPSHELLLINK& pSLObj)
{
	return GetShellLinkFileObject(lpszFilePath, pSLObj, STGM_READ);
}

inline HRESULT CreateShellLinkFileObject (LPCTSTR lpszFilePath, LPSHELLLINK& pSLObj)
{
	return GetShellLinkFileObject(lpszFilePath, pSLObj, STGM_CREATE | STGM_WRITE);
}

extern HRESULT GetShellLinkFilePath (LPCTSTR lpszFilePath, LPTSTR lpszShortcut, const ULONG ulMaxLen);

extern HRESULT CreateShellLinkShortcut (LPCTSTR	lpszLinkPath,
													 LPCTSTR	lpszFilePath,
													 LPCTSTR	lpszWorkDir,		// NULL == same as file
													 LPCTSTR	lpszDescription);	// NULL == same as file
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#endif	/* of _WIN32UTL_SHELL_H_ */