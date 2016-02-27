#ifndef _REGVALS_H_
#define _REGVALS_H_

/*---------------------------------------------------------------------------*/
/*		Easy access for various useful registry values - if registry server is
 * NULL/empty then local machine is assumed.
 */
/*---------------------------------------------------------------------------*/

#include <util/string.h>

/*---------------------------------------------------------------------------*/

extern HRESULT RegValGetStr (LPCTSTR		pszRegServer,
									  LPVOID			hRootKey,
									  LPCTSTR		pszKeyPath,
									  LPCTSTR		lpValueName,
									  LPTSTR			pszValue,
									  const ULONG	ulSize);

/*---------------------------------------------------------------------------*/

/* key & value where default MAPI profile is stored */
extern const TCHAR szRegMAPIProfilesKey[];
extern const TCHAR szRegMAPIDeletedProfilesKey[];

/* value storing the default profile */
extern const TCHAR szRegMAPIDefaultProfileValue[];

/*---------------------------------------------------------------------------*/

#define RegValGetMAPIProfVal(srvr,vln,val,sz)	\
	RegValGetStr(srvr,(LPVOID) HKEY_CURRENT_USER,szRegMAPIProfilesKey,vln,val,sz)

/* returns name of default profile (if any) for the MAPI client */
#define RegValGetDefaultMAPIProfile(srvr,prof,len)	\
	RegValGetMAPIProfVal(srvr,szRegMAPIDefaultProfileValue,prof,len)

/*---------------------------------------------------------------------------*/

/* key & value(s) for TCP/IP related parameters */
extern const TCHAR szRegTcpIPServiceKey[];

/* value storing the DNS domain */
extern const TCHAR szRegDNSDomainValue[];

/*---------------------------------------------------------------------------*/

#define RegValGetTcpIpVal(srvr,vln,val,sz)	\
	RegValGetStr(srvr,(LPVOID) HKEY_LOCAL_MACHINE,szRegTcpIPServiceKey,vln,val,sz)

/* returns name of registered DNS domain (if any) for local host */
#define RegValGetDNSDomain(srvr,dmn,sz)	\
	RegValGetTcpIpVal(srvr, szRegDNSDomainValue, dmn, sz)

/*---------------------------------------------------------------------------*/

/*		Registry key under which all programs which can be un-installed are
 * registered. Each application appears as a sub-key.
 */
extern const TCHAR szRegUnInstallPath[];

/* Value contains the command to be used to un-install the application */
extern const TCHAR szRegUnInstallCmd[];

/* Value containing the application display name */
extern const TCHAR szRegUnInstallDisplayName[];

/*---------------------------------------------------------------------------*/

extern HRESULT RegValGetUnInstallVal (LPCTSTR		pszRegServer,
												  LPCTSTR		pszAppName,
												  LPCTSTR		lpValueName,
												  LPTSTR			pszValue,
												  const DWORD	dwSize);

/* returns command to be used to un-install the application */
#define RegValGetUnInstallCmd(srvr,app,cmd,sz) \
	RegValGetUnInstallVal(srvr,app,szRegUnInstallCmd,cmd,sz)

/* returns application display name */
#define RegValGetUnInstallDisplayName(srvr,app,nm,sz)	\
	RegValGetUnInstallVal(srvr,app,szRegUnInstallDisplayName,cmd,sz)

/*---------------------------------------------------------------------------*/

/* extracts only the executable from the returned associated "open" verb pattern */
extern HRESULT ExtractAssocExecutablePath (LPCTSTR			lpszAssocApp,
														 LPTSTR			lpszExePath,
														 const ULONG	ulMaxLen);

/* returns application associated with the specified extension.
 *
 * Note(s):
 *
 *	1. extension MUST contain leading period - e.g. ".cpp", ".doc"
 * 2. returned value may contain quotes and/or parameters templates
 */
extern HRESULT RegValGetAssocApp (LPCTSTR			pszRegServer,	/* may be NULL */
											 LPCTSTR			pszExtension,
											 LPTSTR			pszAppName,
											 const ULONG	ulNameSize);

/*---------------------------------------------------------------------------*/


/*		Callback used to enumerate associated applications - if return code is
 * other than ERROR_SUCCESS then enumeration is aborted.
 */
typedef HRESULT (*RGVE_ASSOC_APPS)(LPCTSTR	pszExtension,
											  LPCTSTR	pszAssocApp,
											  LPVOID		pArg);

/* Enumerates all file extensions and their associated applications
 *
 * Note: if callback aborts then return code is still ERROR_CANCELLED !!!
 */
extern HRESULT RegValEnumAssocApps (LPCTSTR				pszRegServer,
												RGVE_ASSOC_APPS	lpfnEcfn,
												LPVOID				pArg);

/*---------------------------------------------------------------------------*/

// file extension must include the "."
#define RegValGetAssocContentType(lpszRegServer,lpszFileExtension,lpszCType,ulMaxLen)	\
	RegValGetStr((lpszRegServer),(LPVOID) HKEY_CLASSES_ROOT,(lpszFileExtension),_T("Content Type"),(lpszCType),(ulMaxLen))

/*---------------------------------------------------------------------------*/

#define MAX_REG_LANMAN_SHARE_VALUE_LEN	(2 * MAX_PATH)

extern const TCHAR szRegLanmanSharesKey[];

extern HRESULT RegValGetLocalSharePath (LPCTSTR			lpszServer,	/* NULL == local */
													 LPCTSTR			lpszShareName,
													 LPTSTR			lpszPath,
													 const ULONG	ulMaxLen);

typedef HRESULT (*REGVAL_SHARES_ENUM_CFN)(const ULONG	ulShareIndex,	/* starts at zero */
														LPCTSTR		lpszShareName,
														LPCTSTR		lpszSharePath,
														LPVOID		pArg,
														BOOL			*pfContEnum);

extern HRESULT RegValEnumLocalShares (LPCTSTR						lpszServer,	/* NULL == local */
												  REGVAL_SHARES_ENUM_CFN	lpfnEcfn,
												  LPVOID							pArg);

/*---------------------------------------------------------------------------*/

extern const TCHAR szRegTimeZonesRootKey[];

// usual suffix of time zones keys values
extern const TCHAR	szRegValStdTimeZoneKeyNameSuffix[];

// callback to enumerate time zones names in registry
typedef HRESULT (*REGVAL_TIMEZONES_ENUM_CFN)(const ULONG ulTzIndex,	// starts at zero
															LPCTSTR		lpszTzName,	// key name
															LPVOID		pArg,
															BOOL			*pfContEnum);

extern HRESULT RegValEnumTimeZones (LPCTSTR							lpszServer,	/* NULL == local */
												REGVAL_TIMEZONES_ENUM_CFN	lpfnEcfn,
												LPVOID							pArg);


/*---------------------------------------------------------------------------*/

typedef struct {
	LONG       Bias;
	LONG       StandardBias;
	LONG       DaylightBias;
	SYSTEMTIME StandardDate;		/* wDay is actually week number 1(1st), 5(last) */
	SYSTEMTIME DaylightDate;
} TZIREGVALSTRUCT;

extern HRESULT RegValDecodeTZIData (const BYTE buf[], const DWORD dwLen, TZIREGVALSTRUCT *pTZI);

#define MAX_TZINFO_MAPID_LEN	32

typedef struct {
	TCHAR	szKeyName[MAX_PATH+2];
	TCHAR	szDisplay[MAX_PATH+2];
	TCHAR	szStdName[MAX_PATH+2];
	TCHAR	szDstName[MAX_PATH+2];
	DWORD	dwIndex;
	TCHAR	szMapID[MAX_TZINFO_MAPID_LEN+2];
	TZIREGVALSTRUCT	tzi;
} TZREGVALINFO;

/* the values that appear in each time zone sub-key */
extern const TCHAR	szRegTzDisplayValName[];
extern const TCHAR	szRegTzDstValName[];
extern const TCHAR	szRegTzIndexValName[];
extern const TCHAR	szRegTzMapIdValName[];
extern const TCHAR	szRegTzStdValName[];
extern const TCHAR	szRegTzTZIValName[];

#ifdef __cplusplus
extern HRESULT RegValGetTimeZoneInfoKeyPath (LPCTSTR lpszKeyName, IStrlBuilder& strb);

inline HRESULT RegValGetTimeZoneInfoKeyPath (LPCTSTR lpszKeyName, LPTSTR lpszKeyPath, const UINT32 ulMaxLen)
{
	return ((NULL == lpszKeyPath) || (0 == ulMaxLen)) ? ERROR_BAD_ARGUMENTS : RegValGetTimeZoneInfoKeyPath(lpszKeyName, CStrlBuilder(lpszKeyPath, ulMaxLen));
}
#endif	/* __cplusplus */

extern HRESULT RegValGetTimeZoneInfo (LPCTSTR		lpszServer, /* NULL == local */
												  LPCTSTR		lpszKeyName,
												  TZREGVALINFO	*pInfo);

extern HRESULT RegValGetTimeZoneInfoByInfo (LPCTSTR lpszServer /* NULL == local */, const TIME_ZONE_INFORMATION *pTZ, TZREGVALINFO *pInfo);

extern HRESULT RegValGetDefaultTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, TZREGVALINFO *pInfo);

/*---------------------------------------------------------------------------*/

extern const TCHAR szRegLocalTimeZoneKey[];

extern const TCHAR szRegLclTzActiveBiasValName[];
extern const TCHAR szRegLclTzBiasValName[];
extern const TCHAR szRegLclTzDaylightBiasValName[];
extern const TCHAR szRegLclTzDaylightNameValName[];
extern const TCHAR szRegLclTzDaylightStartValName[];
extern const TCHAR szRegLclTzStandardBiasValName[];
extern const TCHAR szRegLclTzStandardNameValName[];
extern const TCHAR szRegLclTzStandardStartValName[];

typedef struct {
	LONG	activeBias;
	LONG	curBias;
	LONG	dstBias;
	LONG	stdBias;
	TCHAR	dstName[MAX_PATH+2];
	TCHAR	stdName[MAX_PATH+2];
	SYSTEMTIME	dstStart;	/* wDay is actually week number 1(1st), 5(last) */
	SYSTEMTIME	stdStart;
} LCLTZREGVALINFO;

extern HRESULT RegValGetLocalTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, LCLTZREGVALINFO *pInfo);

/* NOTE: updates only DIFFERENT values */
extern HRESULT RegValSetLocalTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, const LCLTZREGVALINFO *pInfo);

/*---------------------------------------------------------------------------*/

#endif	/* of ifdef _REGVALS_H_ */
