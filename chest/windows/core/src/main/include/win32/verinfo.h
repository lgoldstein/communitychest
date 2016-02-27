#ifndef _WIN32_VERINFO_H_
#define _WIN32_VERINFO_H_

/*---------------------------------------------------------------------------*/

#include <wtypes.h>
#include <tchar.h>
#include <winver.h>

/* NOTE !!! requires linking in "version.lib" */

/*---------------------------------------------------------------------------*/

/* Known version information components */
extern const TCHAR szVerCompanyName[];
extern const TCHAR szVerFileDescription[];
extern const TCHAR szVerFileVersion[];
extern const TCHAR szVerInternalName[];
extern const TCHAR szVerLegalCopyright[];
extern const TCHAR szVerLegalTrademarks[];
extern const TCHAR szVerOriginalFilename[];
extern const TCHAR szVerPrivateBuild[];
extern const TCHAR szVerProductName[];
extern const TCHAR szVerProductVersion[];
extern const TCHAR szVerSpecialBuild[];

/* NULL terminated list of known version components */
extern LPCTSTR lpszVerComps[];

/*---------------------------------------------------------------------------*/

/* the 64-bit value is formatted into 4 dot-separated 16-bit values */
extern HRESULT FormatVersionIDInfo (const DWORD dwHi, const DWORD dwLo, LPTSTR lpszID, const UINT32 ulMaxLen);

#ifdef __cplusplus
extern HRESULT GetAppVersionInfoBlock (LPCTSTR	lpszAppPath, /* NULL/empty == auto-detect */
													LPBYTE&	pVersionInfo /* must be released by caller */);

extern HRESULT GetModVersionInfoBlock (HANDLE	hModInst, /* NULL == auto-detect */
													LPBYTE&	pVersionInfo /* must be released by caller */);

extern HRESULT GetBlkBinaryDataPtr (LPVOID pVersionInfo, /* OUT */ LPVOID& pData, /* OUT */ UINT& uLen);

extern HRESULT GetBlkFixedFileVersionInfo (LPVOID pVersionInfo, /* OUT */ VS_FIXEDFILEINFO& vsInfo);

inline HRESULT FormatFileVersionInfo (const VS_FIXEDFILEINFO& vsInfo, LPTSTR lpszID, const UINT32 ulMaxLen)
{
	return FormatVersionIDInfo(vsInfo.dwFileVersionMS, vsInfo.dwFileVersionLS, lpszID, ulMaxLen);
}

inline HRESULT FormatProductVersionInfo (const VS_FIXEDFILEINFO& vsInfo, LPTSTR lpszID, const UINT32 ulMaxLen)
{
	return FormatVersionIDInfo(vsInfo.dwProductVersionMS, vsInfo.dwProductVersionLS, lpszID, ulMaxLen);
}
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

extern HRESULT GetBlkVersionInfo (LPVOID			pVersionInfo,
											 LPCTSTR			lpszVerComp, /* which component */
											 LPTSTR			lpszVerStr,
											 const ULONG	ulMaxStrLen);

#define GetBlkVersionCompanyName(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerCompanyName,vs,sl)
#define GetBlkVersionFileDescription(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerFileDescription,vs,sl)
#define GetBlkVersionFileVersion(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerFileVersion,vs,sl)
#define GetBlkVersionInternalName(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerInternalName,vs,sl)
#define GetBlkVersionLegalCopyright(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerLegalCopyright,vs,sl)
#define GetBlkVersionLegalTrademarks(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerLegalTrademarks,vs,sl)
#define GetBlkVersionOriginalFilename(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerOriginalFilename,vs,sl)
#define GetBlkVersionPrivateBuild(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerPrivateBuild,vs,sl)
#define GetBlkVersionProductName(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerProductName,vs,sl)
#define GetBlkVersionProductVersion(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerProductVersion,vs,sl)
#define GetBlkVersionSpecialBuild(app,vs,sl)	\
	GetBlkVersionInfo(app,szVerSpecialBuild,vs,sl)

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CVerInfoBlk;

// Note: errors of retrieving a specific component are IGNORED and the component is reported as EMPTY
typedef HRESULT (*CVERINFO_ENUM_CFN)(const CVerInfoBlk&	vib,
												 LPCTSTR					lpszCompName,
												 LPCTSTR					lpszCompVal,
												 LPVOID					pArg,
												 BOOL&					fContEnum);

class CVerInfoBlk {
	private:
		LPBYTE	m_pVersionInfo;

		// disable copy constructor and assignment operator
		CVerInfoBlk (const CVerInfoBlk& );
		CVerInfoBlk& operator= (const CVerInfoBlk& );

	public:
		// also default constructor
		CVerInfoBlk (LPBYTE pVersionInfo=NULL)
			: m_pVersionInfo(pVersionInfo)
		{
		}

		// Note: does not release the data block !!!
		void Reset ()
		{
			m_pVersionInfo = NULL;
		}

		void Clear ()
		{
			if (m_pVersionInfo != NULL)
				delete [] m_pVersionInfo;
			Reset();
		}

		HRESULT Init (HANDLE		hModInst /* NULL == current app */)
		{
			Clear();
			return ::GetModVersionInfoBlock(hModInst, m_pVersionInfo);
		}

		HRESULT Init (LPCTSTR	lpszAppPath /* NULL == current app */)
		{
			Clear();
			return ::GetAppVersionInfoBlock(lpszAppPath, m_pVersionInfo);
		}

		HRESULT Init (LPBYTE	pVersionInfo)
		{
			if (NULL == pVersionInfo)
				return ERROR_NO_DATA;

			Clear();
			m_pVersionInfo = pVersionInfo;
		}

		const BYTE *GetVersionInfoBlock () const
		{
			return m_pVersionInfo;
		}

		HRESULT GetVersionCompInfo (LPCTSTR lpszCompName, LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return ::GetBlkVersionInfo(m_pVersionInfo, lpszCompName, lpszVal, ulMaxLen);
		}

		HRESULT GetCompanyName (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerCompanyName, lpszVal, ulMaxLen);
		}

		HRESULT GetFileDescription (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerFileDescription, lpszVal, ulMaxLen);
		}

		HRESULT GetFileVersion (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerFileVersion, lpszVal, ulMaxLen);
		}

		HRESULT GetInternalName (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerInternalName, lpszVal, ulMaxLen);
		}

		HRESULT GetLegalCopyright (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerLegalCopyright, lpszVal, ulMaxLen);
		}

		HRESULT GetLegalTrademarks (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerLegalTrademarks, lpszVal, ulMaxLen);
		}

		HRESULT GetOriginalFilename (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerOriginalFilename, lpszVal, ulMaxLen);
		}

		HRESULT GetPrivateBuild (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerPrivateBuild, lpszVal, ulMaxLen);
		}

		HRESULT GetProductName (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerProductName, lpszVal, ulMaxLen);
		}

		HRESULT GetProductVersion (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerProductVersion, lpszVal, ulMaxLen);
		}

		HRESULT GetSpecialBuild (LPTSTR lpszVal, const ULONG ulMaxLen) const
		{
			return GetVersionCompInfo(szVerSpecialBuild, lpszVal, ulMaxLen);
		}

		// Note: errors of retrieving a specific component are IGNORED and the component is reported as EMPTY
		HRESULT EnumVersionComponents (CVERINFO_ENUM_CFN	lpfnEcfn,
												 LPVOID					pArg,
												 LPTSTR					lpszValBuf,
												 const ULONG			ulMaxLen,
			/* NULL terminated */		 LPCTSTR					lpszCompNames[]=lpszVerComps) const;

		enum { DEFAULT_MAX_VERCOMP_VAL_LEN=128 };

		// Note: errors of retrieving a specific component are IGNORED and the component is reported as EMPTY
		HRESULT EnumVersionComponents (CVERINFO_ENUM_CFN	lpfnEcfn,
												 LPVOID					pArg,
				/* NULL terminated */	 LPCTSTR					lpszCompNames[]=lpszVerComps) const
		{
			TCHAR	szVal[DEFAULT_MAX_VERCOMP_VAL_LEN+2]=_T("");

			return EnumVersionComponents(lpfnEcfn, pArg, szVal, DEFAULT_MAX_VERCOMP_VAL_LEN, lpszCompNames);
		}

		HRESULT GetFixedFileInfo (/* OUT */ VS_FIXEDFILEINFO& vsInfo) const
		{
			return ::GetBlkFixedFileVersionInfo(m_pVersionInfo, vsInfo);
		}

		virtual ~CVerInfoBlk ()
		{
			Clear();
		}
};

typedef CVerInfoBlk *LPCVERINFOBLK;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

extern HRESULT GetModVersionInfo (HANDLE			hModInst, /* NULL == current app */
											 LPCTSTR			lpszVerComp, /* which component */
											 LPTSTR			lpszVerStr,
											 const ULONG	ulMaxStrLen);

#define GetModVersionCompanyName(app,vs,sl)	\
	GetModVersionInfo(app,szVerCompanyName,vs,sl)
#define GetModVersionFileDescription(app,vs,sl)	\
	GetModVersionInfo(app,szVerFileDescription,vs,sl)
#define GetModVersionFileVersion(app,vs,sl)	\
	GetModVersionInfo(app,szVerFileVersion,vs,sl)
#define GetModVersionInternalName(app,vs,sl)	\
	GetModVersionInfo(app,szVerInternalName,vs,sl)
#define GetModVersionLegalCopyright(app,vs,sl)	\
	GetModVersionInfo(app,szVerLegalCopyright,vs,sl)
#define GetModVersionLegalTrademarks(app,vs,sl)	\
	GetModVersionInfo(app,szVerLegalTrademarks,vs,sl)
#define GetModVersionOriginalFilename(app,vs,sl)	\
	GetModVersionInfo(app,szVerOriginalFilename,vs,sl)
#define GetModVersionPrivateBuild(app,vs,sl)	\
	GetModVersionInfo(app,szVerPrivateBuild,vs,sl)
#define GetModVersionProductName(app,vs,sl)	\
	GetModVersionInfo(app,szVerProductName,vs,sl)
#define GetModVersionProductVersion(app,vs,sl)	\
	GetModVersionInfo(app,szVerProductVersion,vs,sl)
#define GetModVersionSpecialBuild(app,vs,sl)	\
	GetModVersionInfo(app,szVerSpecialBuild,vs,sl)

extern HRESULT GetAppVersionInfo (LPCTSTR			lpszAppPath, /* NULL == auto-detect */
											 LPCTSTR			lpszVerComp, /* which component */
											 LPTSTR			lpszVerStr,
											 const ULONG	ulMaxStrLen);

#define GetAppVersionCompanyName(app,vs,sl)	\
	GetAppVersionInfo(app,szVerCompanyName,vs,sl)
#define GetAppVersionFileDescription(app,vs,sl)	\
	GetAppVersionInfo(app,szVerFileDescription,vs,sl)
#define GetAppVersionFileVersion(app,vs,sl)	\
	GetAppVersionInfo(app,szVerFileVersion,vs,sl)
#define GetAppVersionInternalName(app,vs,sl)	\
	GetAppVersionInfo(app,szVerInternalName,vs,sl)
#define GetAppVersionLegalCopyright(app,vs,sl)	\
	GetAppVersionInfo(app,szVerLegalCopyright,vs,sl)
#define GetAppVersionLegalTrademarks(app,vs,sl)	\
	GetAppVersionInfo(app,szVerLegalTrademarks,vs,sl)
#define GetAppVersionOriginalFilename(app,vs,sl)	\
	GetAppVersionInfo(app,szVerOriginalFilename,vs,sl)
#define GetAppVersionPrivateBuild(app,vs,sl)	\
	GetAppVersionInfo(app,szVerPrivateBuild,vs,sl)
#define GetAppVersionProductName(app,vs,sl)	\
	GetAppVersionInfo(app,szVerProductName,vs,sl)
#define GetAppVersionProductVersion(app,vs,sl)	\
	GetAppVersionInfo(app,szVerProductVersion,vs,sl)
#define GetAppVersionSpecialBuild(app,vs,sl)	\
	GetAppVersionInfo(app,szVerSpecialBuild,vs,sl)

/*---------------------------------------------------------------------------*/

#endif /* _WIN32_VERINFO_H_ */