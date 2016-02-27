#ifndef _IISUTILS_H_
#define _IISUTILS_H_

#include <wtypes.h>
#include <tchar.h>

#include <winerror.h>

#include <wininet.h>
#include <httpext.h>
#include <httpfilt.h>

/*---------------------------------------------------------------------------*/

typedef struct {
	PFN_GETEXTENSIONVERSION	lpfnGVFunc;
	PFN_HTTPEXTENSIONPROC	lpfnEPFunc;
	PFN_TERMINATEEXTENSION	lpfnTEFunc;
} ISAPIEXTFUNCS, *LPISAPIEXTFUNCS;

extern const TCHAR szISAPIGetExtensionVersionFuncName[];
extern const TCHAR szISAPIHttpExtensionProcFuncName[];
extern const TCHAR szISAPITerminateExtensionFuncName[];

// initializes the callbacks for an ISAPI extension DLL
extern HRESULT LoadISAPIExtFunctions (HINSTANCE hDLL, ISAPIEXTFUNCS& extFuncs);

/*---------------------------------------------------------------------------*/

/* ISAPI filter DLL callbacks */
typedef BOOL	(WINAPI	*ISAPIGetFilterVersionFunc)(PHTTP_FILTER_VERSION pVer);
typedef DWORD	(WINAPI	*ISAPIHttpFilterProcFunc)(PHTTP_FILTER_CONTEXT pfc, DWORD notificationType, LPVOID pvNotification);
typedef BOOL	(WINAPI	*ISAPITerminateFilterFunc)(DWORD dwFlags);

typedef struct { 
	ISAPIGetFilterVersionFunc	lpfnGVFunc;
	ISAPIHttpFilterProcFunc		lpfnFPFunc;
	ISAPITerminateFilterFunc	lpfnTFFunc;
} ISAPIFLTFUNCS, *LPISAPIFLTFUNCS;

extern const TCHAR szISAPIGetFilterVersionFuncName[];
extern const TCHAR szISAPIHttpFilterProcFuncName[];
extern const TCHAR szISAPITerminateFilterFuncName[];

// initializes the callbacks for an ISAPI filter DLL
extern HRESULT LoadISAPIFltFunctions (HINSTANCE hDLL, ISAPIFLTFUNCS& fltFuncs);

/*---------------------------------------------------------------------------*/

/* ISAPI filter headers pre-processing callbacks */
typedef BOOL (WINAPI *HttpFilterPreProcHdrsGetHeader)(struct _HTTP_FILTER_CONTEXT	*pfc,
																		LPSTR									lpszName,
																		LPVOID								lpvBuffer,
																		LPDWORD								lpdwSize);

typedef BOOL (WINAPI *HttpFilterPreProcHdrsSetHeader)(struct _HTTP_FILTER_CONTEXT	*pfc,
																		LPSTR                         lpszName,
																		LPSTR                         lpszValue);

typedef BOOL (WINAPI *HttpFilterPreProcHdrsAddHeader)(struct _HTTP_FILTER_CONTEXT	*pfc,
																		LPSTR                         lpszName,
																		LPSTR                         lpszValue);

/* special "headers" which can be used to set/get "meta" information */
extern const TCHAR szHttpFilterURLHdr[];
extern const TCHAR szHttpFilterMethodHdr[];
extern const TCHAR szHttpFilterVersionHdr[];

/*---------------------------------------------------------------------------*/

/* URL components for get/set-ting */
typedef enum {
	URL_SCHEME=0,
	URL_HOST,
	URL_PORT,
	URL_USERNAME,
	URL_PASSWORD,
	URL_PATH,
	URL_EXTRA,
	URL_BAD_COMPONENT
} URLCOMPCASE;

#ifdef __cplusplus
inline BOOL fIsBadURLComponentCase (const URLCOMPCASE eComp)
{
	return (((unsigned) eComp) >= ((unsigned) URL_BAD_COMPONENT));
}

inline BOOL isURLAuthComponent (const URLCOMPCASE eComp)
{
	return ((URL_USERNAME == eComp) || (URL_PASSWORD == eComp));
}

inline BOOL isURLComponentEncodingRequired (const URLCOMPCASE eComp)
{
	return (isURLAuthComponent(eComp) || (URL_PATH == eComp) || (URL_EXTRA == eComp));
}
#else
#define fIsBadURLComponentCase(eComp) (((unsigned) (eComp)) >= ((unsigned) URL_BAD_COMPONENT))
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: using this class requires linking in "wininet.lib"
class CWininetURLCracker {
	protected:
		URL_COMPONENTS	m_Comps;

		virtual HRESULT GetComponentChars (LPCTSTR lpszSrc, const DWORD dwSrcLen, LPCTSTR& lpszDst, DWORD& dwDstLen) const;
		virtual HRESULT GetComponent (LPCTSTR lpszSrc, const DWORD dwSrcLen, LPTSTR lpszDst, const DWORD dwDstLen) const;

	public:
		// also default constructor
		CWininetURLCracker (LPCTSTR lpszURL=NULL)
		{
			// Note: for default constructor we will get an "ERROR_NO_DATA" error
			HRESULT	hr=Crack(lpszURL);
		}

		CWininetURLCracker (LPCTSTR lpszURL, const DWORD dwLen)
		{
			HRESULT	hr=Crack(lpszURL, dwLen);
		}

		CWininetURLCracker (const URL_COMPONENTS& urlComps)
			: m_Comps(urlComps)
		{
		}

		virtual const URL_COMPONENTS& GetComponents () const
		{
			return m_Comps;
		}

		// Note(s):
		//
		//	1. any previous values are reset
		// 2. input string must remain valid during invocation of "Get" methods
		virtual HRESULT Crack (LPCTSTR lpszURL, const DWORD dwLen);
		virtual HRESULT Crack (LPCTSTR lpszURL)
		{
			return ((NULL == lpszURL) ? Crack(NULL, 0) : Crack(lpszURL, ::_tcslen(lpszURL)));
		}

		virtual HRESULT GetSchemeChars (LPCTSTR& lpszScheme, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszScheme, m_Comps.dwSchemeLength, lpszScheme, dwLen);
		}

		virtual HRESULT GetScheme (LPTSTR lpszScheme, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszScheme, m_Comps.dwSchemeLength, lpszScheme, dwLen);
		}

		virtual HRESULT GetSchemeCharsByPort (const int nPort, LPCTSTR& lpszScheme, DWORD& dwLen) const;
		virtual HRESULT GetSchemeByPort (const int nPort, LPTSTR lpszScheme, const DWORD dwLen) const;

		virtual HRESULT GetHostNameChars (LPCTSTR& lpszHostName, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszHostName, m_Comps.dwHostNameLength, lpszHostName, dwLen);
		}

		virtual HRESULT GetHostName (LPTSTR lpszHostName, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszHostName, m_Comps.dwHostNameLength, lpszHostName, dwLen);
		}

		virtual HRESULT GetPort (int& nPort) const;

		virtual HRESULT GetUserNameChars (LPCTSTR& lpszUserName, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszUserName, m_Comps.dwUserNameLength, lpszUserName, dwLen);
		}

		virtual HRESULT GetUserName (LPTSTR lpszUserName, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszUserName, m_Comps.dwUserNameLength, lpszUserName, dwLen);
		}

		virtual HRESULT GetPasswordChars (LPCTSTR& lpszPassword, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszPassword, m_Comps.dwPasswordLength, lpszPassword, dwLen);
		}

		virtual HRESULT GetPassword (LPTSTR lpszPassword, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszPassword, m_Comps.dwPasswordLength, lpszPassword, dwLen);
		}

		virtual HRESULT GetUrlPathChars (LPCTSTR& lpszUrlPath, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszUrlPath, m_Comps.dwUrlPathLength, lpszUrlPath, dwLen);
		}

		virtual HRESULT GetUrlPath (LPTSTR lpszUrlPath, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszUrlPath, m_Comps.dwUrlPathLength, lpszUrlPath, dwLen);
		}

		virtual HRESULT GetExtraInfoChars (LPCTSTR& lpszExtraInfo, DWORD& dwLen) const
		{
			return GetComponentChars(m_Comps.lpszExtraInfo, m_Comps.dwExtraInfoLength, lpszExtraInfo, dwLen);
		}

		virtual HRESULT GetExtraInfo (LPTSTR lpszExtraInfo, const DWORD dwLen) const
		{
			return GetComponent(m_Comps.lpszExtraInfo, m_Comps.dwExtraInfoLength, lpszExtraInfo, dwLen);
		}

		virtual HRESULT GetComponent (const URLCOMPCASE eComp, LPTSTR lpszComp, const DWORD dwLen) const;

		virtual void Reset ()
		{
			::memset(&m_Comps, 0, (sizeof m_Comps));
		}

		virtual ~CWininetURLCracker ()
		{
//			Reset();
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// Note: using this class requires linking in "wininet.lib"
class CWininetURLBuilder : public CWininetURLCracker {
	protected:
		virtual HRESULT SetComponentChars (LPTSTR& lpszComp, DWORD& dwCompLen, LPCTSTR lpszCompVal, const DWORD dwCompValLen);

		virtual HRESULT SetComponent (LPTSTR& lpszComp, DWORD& dwCompLen, LPCTSTR lpszCompVal)
		{
			return SetComponentChars(lpszComp, dwCompLen, lpszCompVal, ((NULL == lpszCompVal) ? 0 : ::_tcslen(lpszCompVal)));
		}

	public:
		CWininetURLBuilder ()
			: CWininetURLCracker()
		{
		}

		// Note: all string values must remain valid until "Create" is called
		CWininetURLBuilder (LPCTSTR	lpszScheme,	// if NULL/empty then port is used to determine it
								  LPCTSTR	lpszHostName,
								  const int	nPort=0,	// 0 == same as default for the scheme
								  LPCTSTR	lpszUserName=NULL,
								  LPCTSTR	lpszPassword=NULL,
								  LPCTSTR	lpszUrlPath=NULL,
								  LPCTSTR	lpszExtraInfo=NULL)
		{
			HRESULT	hr=SetParams(lpszScheme, lpszHostName, nPort, lpszUserName, lpszPassword, lpszUrlPath, lpszExtraInfo);
		}

		// Note: resets the current contents
		virtual HRESULT SetParams (LPCTSTR		lpszScheme,		// if NULL/empty then port is used to determine it
											LPCTSTR		lpszHostName,
											const	int	nPort=0,	// 0 == same as default for the scheme
											LPCTSTR		lpszUserName=NULL,
											LPCTSTR		lpszPassword=NULL,
											LPCTSTR		lpszUrlPath=NULL,
											LPCTSTR		lpszExtraInfo=NULL);

		virtual HRESULT SetSchemeChars (LPCTSTR lpszScheme, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszScheme, m_Comps.dwSchemeLength, lpszScheme, dwLen);
		}

		virtual HRESULT SetScheme (LPCTSTR lpszScheme)
		{
			return SetComponent(m_Comps.lpszScheme, m_Comps.dwSchemeLength, lpszScheme);
		}

		virtual HRESULT SetHostNameChars (LPCTSTR lpszHostName, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszHostName, m_Comps.dwHostNameLength, lpszHostName, dwLen);
		}

		virtual HRESULT SetHostName (LPCTSTR lpszHostName)
		{
			return SetComponent(m_Comps.lpszHostName, m_Comps.dwHostNameLength, lpszHostName);
		}

		virtual HRESULT SetProtocolPort (const int nPort);

		// translated from string to number
		virtual HRESULT SetProtocolPort (LPCTSTR lpszPort);

		virtual HRESULT SetSchemeByPort (const int nPort);

		virtual HRESULT SetUserNameChars (LPCTSTR lpszUserName, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszUserName, m_Comps.dwUserNameLength, lpszUserName, dwLen);
		}

		virtual HRESULT SetUserName (LPCTSTR lpszUserName)
		{
			return SetComponent(m_Comps.lpszUserName, m_Comps.dwUserNameLength, lpszUserName);
		}

		virtual HRESULT SetPasswordChars (LPCTSTR lpszPassword, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszPassword, m_Comps.dwPasswordLength, lpszPassword, dwLen);
		}

		virtual HRESULT SetPassword (LPCTSTR lpszPassword)
		{
			return SetComponent(m_Comps.lpszPassword, m_Comps.dwPasswordLength, lpszPassword);
		}

		virtual HRESULT SetUrlPathChars (LPCTSTR lpszUrlPath, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszUrlPath, m_Comps.dwUrlPathLength, lpszUrlPath, dwLen);
		}

		virtual HRESULT SetUrlPath (LPCTSTR lpszUrlPath)
		{
			return SetComponent(m_Comps.lpszUrlPath, m_Comps.dwUrlPathLength, lpszUrlPath);
		}

		virtual HRESULT SetExtraInfoChars (LPCTSTR lpszExtraInfo, const DWORD dwLen)
		{
			return SetComponentChars(m_Comps.lpszExtraInfo, m_Comps.dwExtraInfoLength, lpszExtraInfo, dwLen);
		}

		virtual HRESULT SetExtraInfo (LPCTSTR lpszExtraInfo)
		{
			return SetComponent(m_Comps.lpszExtraInfo, m_Comps.dwExtraInfoLength, lpszExtraInfo);
		}

		virtual HRESULT SetComponent (const URLCOMPCASE eComp, LPTSTR lpszComp);

		virtual HRESULT Create (LPTSTR lpszURL, const DWORD dwMaxLen);

		// Note: resets the current contents
		virtual HRESULT Create (LPTSTR lpszURL, const DWORD dwMaxLen,
										LPCTSTR		lpszScheme,
										LPCTSTR		lpszHostName,
										const	int	nPort=0,	// 0 == same as default for the scheme
										LPCTSTR		lpszUserName=NULL,
										LPCTSTR		lpszPassword=NULL,
										LPCTSTR		lpszUrlPath=NULL,
										LPCTSTR		lpszExtraInfo=NULL);

		virtual ~CWininetURLBuilder ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* _IISUTILS_H_ */
