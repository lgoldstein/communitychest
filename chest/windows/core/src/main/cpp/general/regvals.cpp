#include <limits.h>

#include <util/string.h>

#include <tchar.h>
#include <winerror.h>

#include <win32/general.h>
#include <win32/registry.h>
#include <win32/regvals.h>

#ifdef __cplusplus
#define XTRN extern
#else
#define XTRN
#endif

/*---------------------------------------------------------------------------*/

static HRESULT GetAdminObj (LPCTSTR lpszServer, LPVOID hRootKey, LPCTSTR lpszKeyPath, const BOOL fCreateIfNotExist, LPREGCLASS& pReg)
{
	HRESULT	hr=S_OK;

	pReg = NULL;

	if (IsEmptyStr(lpszServer))
	{
		RegistryClass	*rp=new RegistryClass;
		if (NULL == (pReg=rp))
			return ERROR_NOT_ENOUGH_MEMORY;

		hr = rp->SetRegLocation(hRootKey, lpszKeyPath, KEY_ALL_ACCESS, fCreateIfNotExist);
	}
	else
	{
		RemoteRegistryClass	*rp=new RemoteRegistryClass;
		if (NULL == (pReg=rp))
			return ERROR_NOT_ENOUGH_MEMORY;

		hr = rp->SetRemoteRegLocation(lpszServer, hRootKey, lpszKeyPath, KEY_ALL_ACCESS, fCreateIfNotExist);
	}

	if (hr != S_OK)
	{
		delete pReg;
		pReg = NULL;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValGetStr (LPCTSTR			pszRegServer,
							 LPVOID			hRootKey,
							 LPCTSTR			pszKeyPath,
							 LPCTSTR			lpValueName,
							 LPTSTR			pszValue,
							 const ULONG	ulSize)
{
	LPREGCLASS	pReg=NULL;
	HRESULT		hr=GetRegObj(pszRegServer, hRootKey, pszKeyPath, pReg);

	if (ERROR_SUCCESS == hr)
		hr = pReg->GetValue(lpValueName, pszValue, ulSize);

	if (pReg != NULL)
	{
		delete pReg;
		pReg = NULL;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

XTRN const TCHAR szRegMAPIProfilesKey[]=
	_T("Software\\Microsoft\\Windows NT\\CurrentVersion\\Windows Messaging Subsystem\\Profiles");
XTRN const TCHAR szRegMAPIDeletedProfilesKey[]=
	_T("Software\\Microsoft\\Windows NT\\CurrentVersion\\Windows Messaging Subsystem\\Deleted Profiles");

XTRN const TCHAR szRegMAPIDefaultProfileValue[]=_T("DefaultProfile");

/*---------------------------------------------------------------------------*/

XTRN const TCHAR szRegTcpIPServiceKey[]=_T("SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters");
XTRN const TCHAR szRegDNSDomainValue[]=_T("Domain");

/*---------------------------------------------------------------------------*/

/*		Registry key under which all programs which can be un-installed are
 * registered. Each application appears as a sub-key.
 */
XTRN const TCHAR szRegUnInstallPath[]=
	_T("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall");

/* Value contains the command to be used to un-install the application */
XTRN const TCHAR szRegUnInstallCmd[]=_T("UninstallString");

/* Value containing the application display name */
XTRN const TCHAR szRegUnInstallDisplayName[]=_T("DisplayName");

/*---------------------------------------------------------------------------*/

HRESULT RegValGetUnInstallVal (LPCTSTR			pszRegServer,
										 LPCTSTR			pszAppName,
										 LPCTSTR			lpValueName,
										 LPTSTR			pszValue,
										 const DWORD	dwSize)
{
	TCHAR		szAppKey[MAX_PATH], *lsp=szAppKey;

	if (IsEmptyStr(pszAppName))
		return ERROR_BAD_ARGUMENTS;

	if ((_tcslen(pszAppName)+_tcslen(szRegUnInstallPath)+1) >= MAX_PATH)
		return ERROR_NOT_ENOUGH_MEMORY;

	*lsp = _T('\0');
	lsp = strlcat(lsp, szRegUnInstallPath);
	lsp = strladdch(lsp, _T('\\'));
	lsp = strlcat(lsp, pszAppName);

	return RegValGetStr(pszRegServer, (LPVOID) HKEY_LOCAL_MACHINE,
							  szAppKey, lpValueName, pszValue, dwSize);
}

/*---------------------------------------------------------------------------*/

/*		Scans sub-keys for an associated app. and returns the name of the one
 * containing the word "open" in it (case insensitive).
 */
static HRESULT RegValGetOpenSubKey (const RegistryClass	*pReg,
												LPTSTR					pszOpenSubKey,
												const ULONG				ulKeyNameLen)
{
	static const TCHAR	szOpenSubStr[]=_T("open");

	HRESULT				hr=ERROR_SUCCESS;
	RegistryClassEnum	re;
	DWORD					dwSize=ulKeyNameLen;

	if ((NULL == pReg) || (NULL == pszOpenSubKey) || (0 == ulKeyNameLen))
		return ERROR_BAD_ARGUMENTS;

	if (!re.SetParams(pReg))
		return ERROR_INVALID_FUNCTION;

	/* scan all sub-keys */
	for (hr = re.GetFirstKey(pszOpenSubKey, &dwSize);
		  ERROR_SUCCESS == hr;
		  dwSize = ulKeyNameLen, hr=re.GetNextKey(pszOpenSubKey, &dwSize))
	{
		BOOL	fFound=FALSE;

		/* check if (case insensitive) sub-string */
		for (LPCTSTR p=pszOpenSubKey; (*p != _T('\0')) && (!fFound); p++)
		{
			LPCTSTR q=szOpenSubStr;
			for (LPCTSTR r=p; *q != _T('\0'); q++, r++)
				if (toupper(*q) != toupper(*r)) break;

			if (_T('\0') == *q)
				fFound = TRUE;
		}

		if (fFound) break;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

/* returns name of sub-key containing the associated app "open" command */
static HRESULT RegValGetAssocOpenSubKey (LPCTSTR		pszRegServer,
													  LPCTSTR		pszKeysRoot,
													  LPTSTR			pszOpenSubKey,
													  const ULONG	ulKeyNameLen)
{
	if (IsEmptyStr(pszKeysRoot) || (NULL == pszOpenSubKey) || (0 == ulKeyNameLen))
		return ERROR_BAD_ARGUMENTS;
	*pszOpenSubKey = _T('\0');

	LPREGCLASS	pReg=NULL;
	HRESULT		hr=GetRegObj(pszRegServer, (LPVOID) HKEY_CLASSES_ROOT, pszKeysRoot, pReg);
	if (ERROR_SUCCESS == hr)
		hr = RegValGetOpenSubKey(pReg, pszOpenSubKey, ulKeyNameLen);

	if (pReg != NULL)
	{
		delete pReg;
		pReg = NULL;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

/* extracts only the executable from the returned associated "open" verb pattern */
HRESULT ExtractAssocExecutablePath (LPCTSTR		lpszAssocApp,
												LPTSTR		lpszExePath,
												const ULONG	ulMaxLen)
{
	UINT32	ulPathLen=0;
	LPCTSTR	lpszStartPos=lpszAssocApp, lpszEndPos=lpszStartPos;

	if (IsEmptyStr(lpszAssocApp) || (NULL == lpszExePath) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszExePath = _T('\0');

	if (_T('\"') == *lpszAssocApp)
	{
		for (lpszStartPos++, lpszEndPos++; (*lpszEndPos != _T('\"')) && (*lpszEndPos != _T('\0')); lpszEndPos++);
		if (*lpszEndPos != _T('\"'))
			return ERROR_BAD_FORMAT;
	}
	else
	{
		for (lpszEndPos++; (*lpszEndPos != _T(' ')) && (*lpszEndPos != _T('\0')); lpszEndPos++);
	}

	if ((ulPathLen=(lpszEndPos - lpszStartPos)) >= ulMaxLen)
		return ERROR_BUFFER_OVERFLOW;

	_tcsncpy(lpszExePath, lpszStartPos, ulPathLen);
	lpszExePath[ulPathLen] = _T('\0');

	return S_OK;
}

/*---------------------------------------------------------------------------*/

/* returns application associated with the specified extension.
 *
 * Note(s):
 *
 *	1. extension MUST contain leading period - e.g. ".cpp", ".doc"
 * 2. returned value may contain quotes and/or parameters templates
 */
HRESULT RegValGetAssocApp (LPCTSTR		pszRegServer,	/* may be NULL */
									LPCTSTR		pszExtension,
									LPTSTR		pszAppName,
									const ULONG	ulNameSize)
{
	static const TCHAR szAssocShellSubKey[]=_T("\\shell");
	static const TCHAR szAssocCmdSubKey[]=_T("\\command");
#define MAX_SKEY	64
	HRESULT	hr=ERROR_SUCCESS;
	DWORD		dwSize=0;
	TCHAR		szKeyVal[MAX_PATH+2]=_T(""), *lsp=szKeyVal, szOpenSubKey[MAX_SKEY+2]=_T("");

	if ((NULL == pszExtension) || (_T('.') != *pszExtension) ||
		 (NULL == pszAppName) || (0 == ulNameSize))
		return ERROR_BAD_ARGUMENTS;

	/* get indirect pointer */
	hr = RegValGetStr(pszRegServer, (LPVOID) HKEY_CLASSES_ROOT, pszExtension, NULL, szKeyVal, MAX_PATH);
	if (hr != ERROR_SUCCESS)
		return hr;

	/* make sure we can build sub-key path */
	if ((_tcslen(szKeyVal)+_tcslen(szAssocShellSubKey)) >= MAX_PATH)
		return ERROR_NOT_ENOUGH_MEMORY;

	lsp = strlast(szKeyVal);
	lsp = strlcat(lsp, szAssocShellSubKey);

	/* find out the "open" sub key (sometimes is called something else...) */
	hr = RegValGetAssocOpenSubKey(pszRegServer, szKeyVal, szOpenSubKey, MAX_SKEY);
	if (hr != ERROR_SUCCESS)
		return hr;

	if ((_tcslen(szKeyVal)+_tcslen(szOpenSubKey)+_tcslen(szAssocCmdSubKey)+1) >= MAX_PATH)
		return ERROR_NOT_ENOUGH_MEMORY;

	/* build command key path */
	lsp = strladdch(lsp, _T('\\'));
	lsp = strlcat(lsp, szOpenSubKey);
	lsp = strlcat(lsp, szAssocCmdSubKey);

	/* get "open" command (if any) */
	hr = RegValGetStr(pszRegServer, (LPVOID) HKEY_CLASSES_ROOT,
							szKeyVal, NULL, szKeyVal, MAX_PATH);
	if (hr != ERROR_SUCCESS)
		return hr;

	dwSize = ExpandEnvironmentStrings(szKeyVal, pszAppName, ulNameSize);
	if ((0 == dwSize) || (dwSize >= ulNameSize))
		return ERROR_NOT_ENOUGH_MEMORY;

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

static HRESULT RegValEnumAssocApps (LPCTSTR					pszRegServer,
												const RegistryClass	*pReg,
												RGVE_ASSOC_APPS		lpfnEcfn,
												LPVOID					pArg)
{
	HRESULT				hr=ERROR_SUCCESS;
	RegistryClassEnum	re;
	TCHAR					szSubKey[MAX_PATH];
	DWORD					dwSize=MAX_PATH;

	if ((NULL == lpfnEcfn) || (NULL == pReg))
		return ERROR_BAD_ARGUMENTS;

	if (!re.SetParams(pReg))
		return ERROR_BAD_ENVIRONMENT;

	for (hr = re.GetFirstKey(szSubKey, &dwSize);
		  ERROR_SUCCESS == hr;
		  dwSize=MAX_PATH, hr = re.GetNextKey(szSubKey, &dwSize))
	{
		char	szAppName[MAX_PATH];

		if (_T('.') != szSubKey[0])
			continue;

		szAppName[0] = _T('\0');
		hr = RegValGetAssocApp(pszRegServer, szSubKey, szAppName, MAX_PATH);
		if ((hr != ERROR_SUCCESS) || (_T('\0') == szAppName[0]))
		{
			/* if not found for this extension, keep looking for others */
			hr = ERROR_SUCCESS;
			continue;
		}

		hr = (*lpfnEcfn)(szSubKey, szAppName, pArg);
		if (hr != ERROR_SUCCESS)
		{
			hr = ERROR_CANCELLED;
			break;
		}
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValEnumAssocApps (LPCTSTR			pszRegServer,
									  RGVE_ASSOC_APPS	lpfnEcfn,
									  LPVOID				pArg)
{
	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	LPREGCLASS	pReg=NULL;
	HRESULT		hr=GetRegObj(pszRegServer, (LPVOID) HKEY_CLASSES_ROOT, NULL, pReg);
	if (ERROR_SUCCESS == hr)
		hr = RegValEnumAssocApps(pszRegServer, pReg, lpfnEcfn, pArg);

	if (pReg != NULL)
	{
		delete pReg;
		pReg = NULL;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

static HRESULT FindSharePathSubValue (LPCTSTR	lpszCurPos, LPCTSTR& lpszSharePath)
{
	if (IsEmptyStr(lpszCurPos))
		return ERROR_NO_DATA;

	LPCTSTR	lpszSubValueEnd=_tcschr(lpszCurPos, _T('='));
	if (IsEmptyStr(lpszSubValueEnd))
		return ERROR_BAD_FORMAT;

	const DWORD	dwVPLen=(lpszSubValueEnd - lpszCurPos);
	static const TCHAR	szPathSubValuePrefix[]=_T("Path");
	if ((0 != _tcsnicmp(lpszCurPos, szPathSubValuePrefix, dwVPLen)) || (_tcslen(szPathSubValuePrefix) != dwVPLen))
		return ERROR_SECTOR_NOT_FOUND;

	lpszSharePath = (lpszSubValueEnd + 1);	// skip the '='
	return S_OK;
}

/*---------------------------------------------------------------------------*/

static HRESULT ExtractLocalShareRegValue (LPCTSTR lpszValue, LPTSTR lpszPath, const ULONG ulMaxLen)
{
	if (IsEmptyStr(lpszValue) || (NULL == lpszPath) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszPath = _T('\0');

	for (LPCTSTR	lpszCurPos=lpszValue; *lpszCurPos != _T('\0'); lpszCurPos++)
	{
		LPCTSTR	lpszSharePath=NULL;
		HRESULT	hr=FindSharePathSubValue(lpszCurPos, lpszSharePath);
		if (S_OK == hr)
		{
			const ULONG	ulSVLen=_tcslen(lpszSharePath);
			if (ulSVLen >= ulMaxLen)
				return ERROR_BUFFER_OVERFLOW;

			_tcscpy(lpszPath, lpszSharePath);
			return S_OK;
		}

		const ULONG	ulCPLen=_tcslen(lpszCurPos);
		lpszCurPos += ulCPLen;
	}

	// this point is reached if the path specification was not found
	return ERROR_SECTOR_NOT_FOUND;
}

/*---------------------------------------------------------------------------*/

XTRN const TCHAR szRegLanmanSharesKey[]=_T("SYSTEM\\CurrentControlSet\\Services\\lanmanserver\\Shares");

HRESULT RegValGetLocalSharePath (LPCTSTR		lpszServer,	/* NULL == local */
											LPCTSTR		lpszShareName,
											LPTSTR		lpszPath,
											const ULONG	ulMaxLen)
{
	if (IsEmptyStr(lpszShareName) || (NULL == lpszPath) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszPath = _T('\0');

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	HRESULT			hr=GetRegObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szRegLanmanSharesKey, pReg);
	if (hr != S_OK)
		return hr;

	TCHAR	szShareValue[MAX_REG_LANMAN_SHARE_VALUE_LEN+2]=_T("");
	if ((hr=pReg->GetValue(lpszShareName, szShareValue, MAX_REG_LANMAN_SHARE_VALUE_LEN)) != S_OK)
		return hr;

	if ((hr=ExtractLocalShareRegValue(szShareValue, lpszPath, ulMaxLen)) != S_OK)
		return hr;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValEnumLocalShares (LPCTSTR						lpszServer,	/* NULL == local */
										 REGVAL_SHARES_ENUM_CFN	lpfnEcfn,
										 LPVOID						pArg)
{
	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	HRESULT			hr=GetRegObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szRegLanmanSharesKey, pReg);
	if (hr != S_OK)
		return hr;

	RegistryClassEnum	rce(pReg);
	ULONG					ulShareIndex=0;
	TCHAR					szShareName[ARGUMENT_LENGTH+2]=_T("");
	BOOL					fContEnum=TRUE;
	for (hr=rce.GetFirstValue(szShareName, ARGUMENT_LENGTH), ulShareIndex=0; (S_OK == hr) && fContEnum; hr=rce.GetNextValue(szShareName, ARGUMENT_LENGTH), ulShareIndex++)
	{
		TCHAR	szShareValue[MAX_REG_LANMAN_SHARE_VALUE_LEN+2]=_T("");
		if ((hr=pReg->GetValue(szShareName, szShareValue, MAX_REG_LANMAN_SHARE_VALUE_LEN)) != S_OK)
			return hr;

		TCHAR	szSharePath[MAX_PATH+2] = _T("");
		if ((hr=ExtractLocalShareRegValue(szShareValue, szSharePath, MAX_PATH)) != S_OK)
			return hr;

		if ((hr=(*lpfnEcfn)(ulShareIndex, szShareName, szSharePath, pArg, &fContEnum)) != S_OK)
			return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

XTRN const TCHAR szRegTimeZonesRootKey[]=_T("SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Time Zones");

// usual suffix of time zones keys values
XTRN const TCHAR	szRegValStdTimeZoneKeyNameSuffix[]=_T("Standard Time");

HRESULT RegValEnumTimeZones (LPCTSTR							lpszServer,	/* NULL == local */
									  REGVAL_TIMEZONES_ENUM_CFN	lpfnEcfn,
									  LPVOID								pArg)
{
	if (NULL == lpfnEcfn)
		return ERROR_INVALID_FUNCTION;

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	HRESULT			hr=GetRegObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szRegTimeZonesRootKey, pReg);
	if (hr != S_OK)
		return hr;

	RegistryClassEnum	rce(pReg);
	ULONG					ulTzIndex=0;
	BOOL					fContEnum=TRUE;
	TCHAR					szTzName[ARGUMENT_LENGTH+2]=_T("");
	for (hr=rce.GetFirstKey(szTzName, ARGUMENT_LENGTH), ulTzIndex=0; (S_OK == hr) && fContEnum; hr=rce.GetNextKey(szTzName, ARGUMENT_LENGTH), ulTzIndex++)
		if ((hr=(*lpfnEcfn)(ulTzIndex, szTzName, pArg, &fContEnum)) != S_OK)
			return hr;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

class CTZIDataBufConsumer : public IBufConsumer {
	public:
		CTZIDataBufConsumer ()
			: IBufConsumer()
		{
		}

		CTZIDataBufConsumer (LPBYTE pBuf, const DWORD dwStartPos, const DWORD dwMaxLen)
			: IBufConsumer(pBuf, dwStartPos, dwMaxLen)
		{
		}

		CTZIDataBufConsumer (LPBYTE pBuf, const DWORD dwMaxLen)
			: IBufConsumer(pBuf, dwMaxLen)
		{
		}

		virtual HRESULT GetSystemTime (SYSTEMTIME& s);
};

HRESULT CTZIDataBufConsumer::GetSystemTime (SYSTEMTIME& s)
{
	::memset(&s, 0, (sizeof s));

	WORD	*pwVals[]={
		&s.wYear, &s.wMonth, &s.wDayOfWeek, &s.wDay,
		&s.wHour, &s.wMinute, &s.wSecond, &s.wMilliseconds
	};
	const unsigned	wValsNum=(sizeof pwVals) / (sizeof pwVals[0]);

	for (unsigned i=0; i < wValsNum; i++)
	{
		HRESULT	hr=GetWord(*pwVals[i], true);
		if (hr != S_OK)
			return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValDecodeTZIData (const BYTE buf[], const DWORD dwLen, TZIREGVALSTRUCT *pTZI)
{
	if ((NULL == buf) || (dwLen <= 1) || (NULL == pTZI))
		return ERROR_BAD_ARGUMENTS;
	memset(pTZI, 0, (sizeof *pTZI));

	CTZIDataBufConsumer	ibc((BYTE *) buf, dwLen);
	HRESULT					hr=ibc.GetDword((DWORD &) pTZI->Bias, true);
	if (hr != S_OK)
		return hr;
	if ((hr=ibc.GetDword((DWORD &) pTZI->StandardBias, true)) != S_OK)
		return hr;
	if ((hr=ibc.GetDword((DWORD &) pTZI->DaylightBias, true)) != S_OK)
		return hr;
	if ((hr=ibc.GetSystemTime(pTZI->StandardDate)) != S_OK)
		return hr;
	if ((hr=ibc.GetSystemTime(pTZI->DaylightDate)) != S_OK)
		return hr;

	const DWORD	dwRemLen=ibc.GetRemainLen();
	if (dwRemLen > 0)
		return ERROR_IO_INCOMPLETE;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

class CTZIDataBufBuilder : public IBufBuilder {
	public:
		CTZIDataBufBuilder (LPBYTE pBuf, const UINT32 ulMaxLen)
			: IBufBuilder(pBuf, ulMaxLen)
		{
		}

		virtual HRESULT AddSystemTime (const SYSTEMTIME& s);

		virtual ~CTZIDataBufBuilder ()
		{
		}
};

HRESULT CTZIDataBufBuilder::AddSystemTime (const SYSTEMTIME& s)
{
	const WORD	wVals[]={
		s.wYear, s.wMonth, s.wDayOfWeek, s.wDay,
		s.wHour, s.wMinute, s.wSecond, s.wMilliseconds
	};
	const unsigned	wValsNum=(sizeof wVals) / (sizeof wVals[0]);

	for (unsigned i=0; i < wValsNum; i++)
	{
		HRESULT	hr=AddWord(wVals[i], true);
		if (hr != S_OK)
			return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValEncodeTZIData (const TZIREGVALSTRUCT *pTZI, BYTE buf[], const DWORD dwMaxLen, DWORD *dwLen)
{
	if ((NULL == buf) || (dwMaxLen <= 1) || (NULL == pTZI) || (NULL == dwLen))
		return ERROR_BAD_ARGUMENTS;
	memset(buf, 0, dwMaxLen);
	*dwLen = 0;

	CTZIDataBufBuilder	ibc(buf, dwMaxLen);
	HRESULT					hr=ibc.AddDword(pTZI->Bias, true);
	if (hr != S_OK)
		return hr;
	if ((hr=ibc.AddDword((DWORD &) pTZI->StandardBias, true)) != S_OK)
		return hr;
	if ((hr=ibc.AddDword((DWORD &) pTZI->DaylightBias, true)) != S_OK)
		return hr;
	if ((hr=ibc.AddSystemTime(pTZI->StandardDate)) != S_OK)
		return hr;
	if ((hr=ibc.AddSystemTime(pTZI->DaylightDate)) != S_OK)
		return hr;

	*dwLen = ibc.GetCurLen();
	return S_OK;
}

/*---------------------------------------------------------------------------*/

XTRN const TCHAR	szRegTzDisplayValName[]=_T("Display");
XTRN const TCHAR	szRegTzDstValName[]=_T("Dlt");
XTRN const TCHAR	szRegTzIndexValName[]=_T("Index");
XTRN const TCHAR	szRegTzMapIdValName[]=_T("MapID");
XTRN const TCHAR	szRegTzStdValName[]=_T("Std");
XTRN const TCHAR	szRegTzTZIValName[]=_T("TZI");

/*---------------------------------------------------------------------------*/

HRESULT RegValGetTimeZoneInfoKeyPath (LPCTSTR lpszKeyName, IStrlBuilder& strb)
{
	HRESULT	hr=strb.AddStr(szRegTimeZonesRootKey);
	if (hr != S_OK)
		return hr;
	if ((hr=strb.AddChar(_T('\\'))) != S_OK)
		return hr;
	if ((hr=strb.AddStr(lpszKeyName)) != S_OK)
		return hr;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValGetTimeZoneInfo (LPCTSTR			lpszServer, /* NULL == local */
										 LPCTSTR			lpszKeyName,
										 TZREGVALINFO	*pInfo)
{
	if (IsEmptyStr(lpszKeyName) || (NULL == pInfo))
		return ERROR_BAD_ARGUMENTS;
	memset(pInfo, 0, (sizeof *pInfo));

	TCHAR		szKeyPath[MAX_PATH+2]=_T("");
	HRESULT	hr=RegValGetTimeZoneInfoKeyPath(lpszKeyName, szKeyPath, MAX_PATH);
	if (hr != S_OK)
		return hr;

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	if ((hr=GetRegObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szKeyPath, pReg)) != S_OK)
		return hr;

	if ((hr=pReg->GetValue(szRegTzDisplayValName, pInfo->szDisplay, MAX_PATH)) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegTzDstValName, pInfo->szDstName, MAX_PATH)) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegTzStdValName, pInfo->szStdName, MAX_PATH)) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegTzMapIdValName, pInfo->szMapID, MAX_TZINFO_MAPID_LEN)) != S_OK)
		return hr;

	if ((hr=pReg->GetValue(szRegTzIndexValName, &(pInfo->dwIndex))) != S_OK)
		return hr;

	BYTE	tziData[sizeof(TZIREGVALSTRUCT) + MAX_PATH]={ 0 };	// should be more than enough
	DWORD	dwType=(~0), dwLen=(sizeof tziData);
	if ((hr=pReg->GetValue(szRegTzTZIValName, &dwType, tziData, &dwLen)) != S_OK)
		return hr;
	if (dwType != REG_BINARY)
		return ERROR_BAD_ENVIRONMENT;

	if ((hr=RegValDecodeTZIData(tziData, dwLen, &(pInfo->tzi))) != S_OK)
		return hr;

	const size_t	knLen=_tcslen(lpszKeyName);
	if (knLen > MAX_PATH)
		return ERROR_BUFFER_OVERFLOW;
	_tcscpy(pInfo->szKeyName, lpszKeyName);

	return S_OK;
}

/*---------------------------------------------------------------------------*/

// NOTE: 1. creates key if not found, 2. overrides only values that are not the same
HRESULT RegValSetTimeZoneInfo (LPCTSTR					lpszServer, /* NULL == local */
										 LPCTSTR					lpszKeyName,
										 const TZREGVALINFO	*pInfo)
{
	if (IsEmptyStr(lpszKeyName) || (NULL == pInfo))
		return ERROR_BAD_ARGUMENTS;

	TCHAR		szKeyPath[MAX_PATH+2]=_T("");
	HRESULT	hr=RegValGetTimeZoneInfoKeyPath(lpszKeyName, szKeyPath, MAX_PATH);
	if (hr != S_OK)
		return hr;

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	if ((hr=GetAdminObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szKeyPath, TRUE, pReg)) != S_OK)
		return hr;

	TZREGVALINFO	tzInfo={ 0 };
	bool				tziAvailable=true;
	if ((hr=RegValGetTimeZoneInfo(lpszServer, lpszKeyName, &tzInfo)) != S_OK)
	{
		memset(&tzInfo, 0, (sizeof tzInfo));	// OK if failed - assume everything will be re-written
		tziAvailable = false;
	}

	if ((!tziAvailable) || (_tcsicmp(pInfo->szDisplay, tzInfo.szDisplay) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegTzDisplayValName, pInfo->szDisplay)) != S_OK)
			return hr;
	}

	if ((!tziAvailable) || (_tcsicmp(pInfo->szDstName, tzInfo.szDstName) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegTzDstValName, pInfo->szDstName)) != S_OK)
			return hr;
	}

	if ((!tziAvailable) || (_tcsicmp(pInfo->szStdName, tzInfo.szStdName) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegTzStdValName, pInfo->szStdName)) != S_OK)
			return hr;
	}

	const TZIREGVALSTRUCT &tzi=tzInfo.tzi, &izi=pInfo->tzi;
	BYTE	tziData[sizeof(TZIREGVALSTRUCT) + MAX_PATH]={ 0 };
	DWORD	dwType=(~0), dwLen=(sizeof tziData);
	// we need to know if value set
	bool	tziDataSet=tziAvailable;
	if (tziAvailable && (S_OK == (hr=pReg->GetValue(szRegTzTZIValName, &dwType, tziData, &dwLen))))
	{
		if (dwType != REG_BINARY)
			return ERROR_BAD_ENVIRONMENT;

		tziDataSet = true;
	}
	else
		tziDataSet = false;

	BYTE iziData[(sizeof tziData)]={ 0 };
	if ((hr=RegValEncodeTZIData(&izi, iziData, (sizeof iziData), &dwLen)) != S_OK)
		return hr;

	if ((!tziDataSet) ||
		 (tzi.Bias != izi.Bias) ||
		 (tzi.DaylightBias != izi.DaylightBias) ||
		 (tzi.StandardBias != izi.StandardBias) ||
		 (tzi.StandardDate.wDay != izi.StandardDate.wDay) ||
		 (tzi.StandardDate.wMonth != izi.StandardDate.wMonth) ||
		 (tzi.DaylightDate.wDay != izi.DaylightDate.wDay) ||
		 (tzi.DaylightDate.wMonth != izi.DaylightDate.wMonth))
	{
		if ((hr=pReg->SetValue(szRegTzTZIValName, REG_BINARY, iziData, dwLen)) != S_OK)
			return hr;
	}

	if ((!tziAvailable) || (_tcsicmp(pInfo->szMapID, tzInfo.szMapID) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegTzMapIdValName, pInfo->szMapID)) != S_OK)
			return hr;
	}

	if ((!tziAvailable) || (pInfo->dwIndex != tzInfo.dwIndex))
	{
		if ((hr=pReg->SetValue(szRegTzIndexValName, pInfo->dwIndex)) != S_OK)
			return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

class CTZFARGS {
	private:
		const LPCTSTR	m_lpszServer;
		const LPCTSTR	m_lpszStdName;
		const LPCTSTR	m_lpszDstName;
		TZREGVALINFO&	m_tzInfo;
		bool				m_fInitialized;

		HRESULT CheckTimeZone (LPCTSTR lpszTzName, BOOL& fContEnum);

	public:
		CTZFARGS (LPCTSTR lpszServer, LPCTSTR lpszStdName, LPCTSTR lpszDstName, TZREGVALINFO& tzInfo)
			: m_lpszServer(lpszServer)
			, m_lpszStdName(lpszStdName)
			, m_lpszDstName(lpszDstName)
			, m_tzInfo(tzInfo)
			, m_fInitialized(false)
		{
		}

		// callback to enumerate time zones names in registry
		static HRESULT tzEnum (const ULONG ulTzIndex,	// starts at zero
									  LPCTSTR		lpszTzName,	// key name
									  LPVOID			pArg,
									  BOOL			*pfContEnum)
		{
			return (((NULL == pArg) || (NULL == pfContEnum)) ? ERROR_BAD_ENVIRONMENT : ((CTZFARGS *) pArg)->CheckTimeZone(lpszTzName, *pfContEnum));
		}

		const bool HaveTzInfo () const
		{
			return m_fInitialized;
		}
};

HRESULT CTZFARGS::CheckTimeZone (LPCTSTR lpszTzName, BOOL& fContEnum)
{
	if (m_fInitialized)
		return ERROR_ALREADY_EXISTS;

	HRESULT	hr=::RegValGetTimeZoneInfo(m_lpszServer, lpszTzName, &m_tzInfo);
	if (hr != S_OK)
		return hr;

	if ((!IsEmptyStr(m_lpszStdName)) && (!m_fInitialized))
	{
		if (0 == ::_tcsicmp(m_tzInfo.szStdName, m_lpszStdName))
			m_fInitialized = true;
	}

	if ((!IsEmptyStr(m_lpszDstName)) && (!m_fInitialized))
	{
		if (0 == ::_tcsicmp(m_tzInfo.szDstName, m_lpszDstName))
			m_fInitialized = true;
	}

	if (!m_fInitialized)
		::memset(&m_tzInfo, 0, (sizeof m_tzInfo));
	else
		fContEnum = FALSE;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValGetTimeZoneInfoByInfo (LPCTSTR lpszServer /* NULL == local */, const TIME_ZONE_INFORMATION *pTZ, TZREGVALINFO *pInfo)
{
	if ((NULL == pInfo) || (NULL == pTZ))
		return ERROR_BAD_ARGUMENTS;
	memset(pInfo, 0, (sizeof *pInfo));

	HRESULT						hr=S_OK;
	CWideCharToUTF8String	stdName, dstName;
	const LPCTSTR				lpszStdName=((hr=stdName.SetString(pTZ->StandardName)) != S_OK) ? (LPCTSTR) NULL : (LPCTSTR) stdName;
	const LPCTSTR				lpszDstName=((hr=dstName.SetString(pTZ->DaylightName)) != S_OK) ? (LPCTSTR) NULL : (LPCTSTR) dstName;
	if (IsEmptyStr(lpszStdName) && IsEmptyStr(lpszDstName))
		return ERROR_BAD_ENVIRONMENT;

	CTZFARGS	tza(lpszServer, lpszStdName, lpszDstName, *pInfo);
	if ((hr=RegValEnumTimeZones(lpszServer, CTZFARGS::tzEnum, (LPVOID) &tza)) != S_OK)
		return hr;

	if (!tza.HaveTzInfo())
		return ERROR_SECTOR_NOT_FOUND;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValGetDefaultTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, TZREGVALINFO *pInfo)
{
	if (NULL == pInfo)
		return ERROR_BAD_ARGUMENTS;
	memset(pInfo, 0, (sizeof *pInfo));

	TIME_ZONE_INFORMATION	tzInfo={ 0 };
	DWORD							dwRes=::GetTimeZoneInformation(&tzInfo);
	if (TIME_ZONE_ID_INVALID == dwRes)
	{
		dwRes = ::GetLastError();
		return ((S_OK == dwRes) ? (-1) : dwRes);
	}

	return RegValGetTimeZoneInfoByInfo(lpszServer, &tzInfo, pInfo);
}

/*---------------------------------------------------------------------------*/

XTRN const TCHAR szRegLocalTimeZoneKey[]=_T("SYSTEM\\CurrentControlSet\\Control\\TimeZoneInformation");

XTRN const TCHAR szRegLclTzActiveBiasValName[]=_T("ActiveTimeBias");
XTRN const TCHAR szRegLclTzBiasValName[]=_T("Bias");
XTRN const TCHAR szRegLclTzDaylightBiasValName[]=_T("DaylightBias");
XTRN const TCHAR szRegLclTzDaylightNameValName[]=_T("DaylightName");
XTRN const TCHAR szRegLclTzDaylightStartValName[]=_T("DaylightStart");
XTRN const TCHAR szRegLclTzStandardBiasValName[]=_T("StandardBias");
XTRN const TCHAR szRegLclTzStandardNameValName[]=_T("StandardName");
XTRN const TCHAR szRegLclTzStandardStartValName[]=_T("StandardStart");

/*---------------------------------------------------------------------------*/

class CLclTZIDataBufConsumer : public CTZIDataBufConsumer {
	public:
		CLclTZIDataBufConsumer (LPBYTE pBuf, const UINT32 ulMaxLen)
			: CTZIDataBufConsumer(pBuf, ulMaxLen)
		{
		}

		virtual HRESULT GetSystemTime (SYSTEMTIME& s);

		virtual ~CLclTZIDataBufConsumer ()
		{
		}
};

HRESULT CLclTZIDataBufConsumer::GetSystemTime (SYSTEMTIME& s)
{
	HRESULT	hr=CTZIDataBufConsumer::GetSystemTime(s);
	if (hr != S_OK)
		return hr;

	// NOTE !!! for some reason this happens
	const WORD	ts=s.wDayOfWeek;
	s.wDayOfWeek = s.wDay;
	s.wDay = ts;
	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT RegValGetLocalTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, LCLTZREGVALINFO *pInfo)
{
	if (NULL == pInfo)
		return ERROR_BAD_ARGUMENTS;
	memset(pInfo, 0, (sizeof pInfo));

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	HRESULT			hr=GetRegObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szRegLocalTimeZoneKey, pReg);
	if (hr != S_OK)
		return hr;

	if ((hr=pReg->GetValue(szRegLclTzActiveBiasValName, (LPDWORD) &(pInfo->activeBias))) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegLclTzBiasValName, (LPDWORD) &(pInfo->curBias))) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegLclTzDaylightBiasValName, (LPDWORD) &(pInfo->dstBias))) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegLclTzDaylightNameValName, pInfo->dstName, MAX_PATH)) != S_OK)
		return hr;

	BYTE	sysData[(sizeof pInfo->dstStart) + NAME_LENGTH /* more than enough */]={ 0 };
	DWORD	dwType=(~0), dwLen=(sizeof sysData);
	if ((hr=pReg->GetValue(szRegLclTzDaylightStartValName, &dwType, sysData, &dwLen)) != S_OK)
		return hr;
	if (dwType != REG_BINARY)
		return ERROR_ARENA_TRASHED;

	{
		CLclTZIDataBufConsumer	ibc(sysData, dwLen);
		if ((hr=ibc.GetSystemTime(pInfo->dstStart)) != S_OK)
			return hr;

		if (ibc.GetRemainLen() != 0)	// make sure decoded EXACT system time value
			return ERROR_IO_INCOMPLETE;
	}

	if ((hr=pReg->GetValue(szRegLclTzStandardBiasValName, (LPDWORD) &(pInfo->stdBias))) != S_OK)
		return hr;
	if ((hr=pReg->GetValue(szRegLclTzStandardNameValName, pInfo->stdName, MAX_PATH)) != S_OK)
		return hr;

	dwType=(~0); dwLen=(sizeof sysData);
	if ((hr=pReg->GetValue(szRegLclTzStandardStartValName, &dwType, sysData, &dwLen)) != S_OK)
		return hr;
	if (dwType != REG_BINARY)
		return ERROR_ARENA_TRASHED;

	{
		CLclTZIDataBufConsumer	ibc(sysData, dwLen);
		if ((hr=ibc.GetSystemTime(pInfo->stdStart)) != S_OK)
			return hr;

		if (ibc.GetRemainLen() != 0)	// make sure decoded EXACT system time value
			return ERROR_IO_INCOMPLETE;
	}

	return S_OK;	
}

/*---------------------------------------------------------------------------*/

class CLclTZIDataBufBuilder : public CTZIDataBufBuilder {
	public:
		CLclTZIDataBufBuilder (LPBYTE pBuf, const UINT32 ulMaxLen)
			: CTZIDataBufBuilder(pBuf, ulMaxLen)
		{
		}

		virtual HRESULT AddSystemTime (const SYSTEMTIME& s);

		virtual ~CLclTZIDataBufBuilder ()
		{
		}
};

HRESULT CLclTZIDataBufBuilder::AddSystemTime (const SYSTEMTIME& s)
{
	SYSTEMTIME	ts(s);
	// NOTE !!! for some reason we have to do this
	const WORD	tts=ts.wDayOfWeek;
	ts.wDayOfWeek = ts.wDay;
	ts.wDay = tts;

	return CTZIDataBufBuilder::AddSystemTime(ts);
}

/* NOTE: updates only DIFFERENT values */
HRESULT RegValSetLocalTimeZoneInfo (LPCTSTR lpszServer /* NULL == local */, const LCLTZREGVALINFO *pInfo)
{
	if (NULL == pInfo)
		return ERROR_BAD_ARGUMENTS;

	LCLTZREGVALINFO	lclInfo={ 0 };
	HRESULT				hr=RegValGetLocalTimeZoneInfo(lpszServer, &lclInfo);
	const bool			lclAvailable=(S_OK == hr);
	if (hr != S_OK)
		memset(&lclInfo, 0, (sizeof lclInfo));

	LPREGCLASS		pReg=NULL;
	CRegClassGuard	rcg(pReg);
	if ((hr=GetAdminObj(lpszServer, (LPVOID) HKEY_LOCAL_MACHINE, szRegLocalTimeZoneKey, TRUE, pReg)) != S_OK)
		return hr;

	if ((!lclAvailable) || (pInfo->activeBias != lclInfo.activeBias))
	{
		if ((hr=pReg->SetValue(szRegLclTzActiveBiasValName, (DWORD) pInfo->activeBias)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) || (pInfo->curBias != lclInfo.curBias))
	{
		if ((hr=pReg->SetValue(szRegLclTzBiasValName, (DWORD) pInfo->curBias)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) || (pInfo->dstBias != lclInfo.dstBias))
	{
		if ((hr=pReg->SetValue(szRegLclTzDaylightBiasValName, (DWORD) pInfo->dstBias)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) || (_tcsicmp(pInfo->dstName, lclInfo.dstName) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegLclTzDaylightNameValName, pInfo->dstName)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) ||
		 (pInfo->dstStart.wDay != lclInfo.dstStart.wDay) ||
		 (pInfo->dstStart.wMonth != lclInfo.dstStart.wMonth) ||
		 (pInfo->dstStart.wYear != lclInfo.dstStart.wYear))
	{
		BYTE	sysData[(sizeof pInfo->dstStart) + NAME_LENGTH /* more than enough */]={ 0 };
		CTZIDataBufBuilder	ibc(sysData, (sizeof sysData));

		if ((hr=ibc.AddSystemTime(pInfo->dstStart)) != S_OK)
			return hr;
		if ((hr=pReg->SetValue(szRegLclTzDaylightStartValName, REG_BINARY, sysData, ibc.GetCurLen())) != S_OK)
			return hr;
	}

	if ((!lclAvailable) || (pInfo->stdBias != lclInfo.stdBias))
	{
		if ((hr=pReg->SetValue(szRegLclTzStandardBiasValName, (DWORD) pInfo->stdBias)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) || (_tcsicmp(pInfo->stdName, lclInfo.stdName) != 0))
	{
		if ((hr=pReg->SetStrValue(szRegLclTzStandardNameValName, pInfo->stdName)) != S_OK)
			return hr;
	}

	if ((!lclAvailable) ||
		 (pInfo->stdStart.wDay != lclInfo.stdStart.wDay) ||
		 (pInfo->stdStart.wMonth != lclInfo.stdStart.wMonth) ||
		 (pInfo->stdStart.wYear != lclInfo.stdStart.wYear))
	{
		BYTE	sysData[(sizeof pInfo->stdStart) + NAME_LENGTH /* more than enough */]={ 0 };
		CTZIDataBufBuilder	ibc(sysData, (sizeof sysData));

		if ((hr=ibc.AddSystemTime(pInfo->stdStart)) != S_OK)
			return hr;
		if ((hr=pReg->SetValue(szRegLclTzStandardStartValName, REG_BINARY, sysData, ibc.GetCurLen())) != S_OK)
			return hr;
	}
	return S_OK;	
}

/*---------------------------------------------------------------------------*/
