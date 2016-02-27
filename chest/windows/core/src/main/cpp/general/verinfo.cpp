#include <win32/verinfo.h>
#include <winerror.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN /* do nothing */
#endif

/*---------------------------------------------------------------------------*/

/* Known version information components */
SZXTRN const TCHAR szVerCompanyName[]=_T("CompanyName");
SZXTRN const TCHAR szVerFileDescription[]=_T("FileDescription");
SZXTRN const TCHAR szVerFileVersion[]=_T("FileVersion");
SZXTRN const TCHAR szVerInternalName[]=_T("InternalName");
SZXTRN const TCHAR szVerLegalCopyright[]=_T("LegalCopyright");
SZXTRN const TCHAR szVerLegalTrademarks[]=_T("LegalTrademarks");
SZXTRN const TCHAR szVerOriginalFilename[]=_T("OriginalFilename");
SZXTRN const TCHAR szVerPrivateBuild[]=_T("PrivateBuild");
SZXTRN const TCHAR szVerProductName[]=_T("ProductName");
SZXTRN const TCHAR szVerProductVersion[]=_T("ProductVersion");
SZXTRN const TCHAR szVerSpecialBuild[]=_T("SpecialBuild");

/*---------------------------------------------------------------------------*/

/* NULL terminated list of known version components */
SZXTRN LPCTSTR lpszVerComps[]={
	szVerCompanyName,
	szVerFileDescription,
	szVerFileVersion,
	szVerInternalName,
	szVerLegalCopyright,
	szVerLegalTrademarks,
	szVerOriginalFilename,
	szVerPrivateBuild,
	szVerProductName,
	szVerProductVersion,
	szVerSpecialBuild,

	NULL		/* mark end of list */
};

/*---------------------------------------------------------------------------*/

HRESULT GetBlkVersionInfo (LPVOID		pVersionInfo,
									LPCTSTR		lpszVerComp, /* which component */
									LPTSTR		lpszVerStr,
									const ULONG	ulMaxStrLen)
{
	if ((NULL == pVersionInfo) || IsEmptyStr(lpszVerComp) ||
		 (NULL == lpszVerStr) || (0 == ulMaxStrLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszVerStr = _T('\0');

	HRESULT	hr=ERROR_SUCCESS;
	struct LANGANDCODEPAGE {
		WORD wLanguage;
		WORD wCodePage;
	} *lpTranslate=NULL;
	UINT		cbTranslate=0U;

	/* Read the list of languages and code pages */
	if (!VerQueryValue(pVersionInfo, 
							 TEXT("\\VarFileInfo\\Translation"),
							 (LPVOID *) &lpTranslate,
							 &cbTranslate))
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);
		return hr;
	}

	/* must have at least one language and codepage */
	{
		const UINT	nXlate=(cbTranslate / (sizeof *lpTranslate));
		if ((NULL == lpTranslate) || (nXlate < 1))
			return ERROR_BAD_ENVIRONMENT;
	}

	TCHAR		szVerPath[MAX_PATH+2]=_T("");
	{
		TCHAR	szLang[MAX_WORD_HEX_DISPLAY_LENGTH+2]=_T(""), szCodePage[MAX_WORD_HEX_DISPLAY_LENGTH+2]=_T("");
		word_to_hex_argument(lpTranslate->wLanguage, szLang);
		word_to_hex_argument(lpTranslate->wCodePage, szCodePage);

		CStrlBuilder	strb(szVerPath, MAX_PATH);
		if ((hr=strb.AddStr(_T("\\StringFileInfo\\"))) != S_OK)
			return hr;
		if ((hr=strb.AddStr(szLang)) != S_OK)
			return hr;
		if ((hr=strb.AddStr(szCodePage)) != S_OK)
			return hr;
		if ((hr=strb.AddChar(_T('\\'))) != S_OK)
			return hr;
		if ((hr=strb.AddStr(lpszVerComp)) != S_OK)
			return hr;
	}

	LPCTSTR	lpszVerVal=NULL;
	UINT		uVerLength=0;
	if (!VerQueryValue(pVersionInfo, szVerPath, (LPVOID *) &lpszVerVal, &uVerLength))
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);
		return hr;
	}

	if (uVerLength >= ulMaxStrLen)
		return ERROR_NOT_ENOUGH_MEMORY;

	if (uVerLength > 0)
		_tcsncpy(lpszVerStr, lpszVerVal, uVerLength);
	lpszVerStr[uVerLength] = _T('\0');

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT GetAppVersionInfoBlock (LPCTSTR	lpszAppPath, /* NULL == auto-detect */
										  LPBYTE&	pVersionInfo /* must be released by caller */)
{
	HRESULT	hr=ERROR_SUCCESS;
	LPCTSTR	lpszAP=lpszAppPath;
	TCHAR		szFullPath[MAX_PATH+2]=_T("");
	DWORD		dwVerHnd=0, dwVerInfoSize= 0;

	pVersionInfo = NULL;

	if (IsEmptyStr(lpszAppPath))
	{
		dwVerInfoSize = GetModuleFileName(NULL, szFullPath, MAX_PATH);
		if ((0 == dwVerInfoSize) || (dwVerInfoSize > MAX_PATH))
		{
			if (S_OK == (hr=GetLastError()))
				hr = (-1);
			return hr;
		}

		lpszAP = szFullPath;
	}

	dwVerInfoSize = GetFileVersionInfoSize((LPTSTR) lpszAP, &dwVerHnd);
	if (0 == dwVerInfoSize)
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);
		return hr;
	}

	if (NULL == (pVersionInfo=new BYTE[dwVerInfoSize+sizeof(DWORD)]))
		return ERROR_NOT_ENOUGH_MEMORY;
	memset(pVersionInfo, 0, dwVerInfoSize);

	if (!GetFileVersionInfo((LPTSTR) lpszAP, dwVerHnd, dwVerInfoSize, pVersionInfo))
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);

		delete [] pVersionInfo;
		pVersionInfo = NULL;
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT GetModVersionInfoBlock (HANDLE		hModInst, /* NULL == auto-detect */
										  LPBYTE&	pVersionInfo /* must be released by caller */)
{
	HRESULT	hr=ERROR_SUCCESS;
	TCHAR		szModPath[MAX_PATH+2]=_T("");
	DWORD		dwVerInfoSize=GetModuleFileName((HMODULE)hModInst, szModPath, MAX_PATH);
	if ((0 == dwVerInfoSize) || (dwVerInfoSize > MAX_PATH))
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);
		return hr;
	}

	return GetAppVersionInfoBlock(szModPath, pVersionInfo);
}

/*---------------------------------------------------------------------------*/

HRESULT GetAppVersionInfo (LPCTSTR		lpszAppPath, /* NULL == auto-detect */
									LPCTSTR		lpszVerComp, /* which component */
									LPTSTR		lpszVerStr,
									const ULONG	ulMaxStrLen)
{
	if ((NULL == lpszVerStr) || (0 == ulMaxStrLen))
		return ERROR_BAD_ARGUMENTS;
	*lpszVerStr = _T('\0');

	LPBYTE			pVersionInfo=NULL;
	CBytesBufGuard	vig(pVersionInfo);
	HRESULT	hr=GetAppVersionInfoBlock(lpszAppPath, pVersionInfo);
	if (hr != ERROR_SUCCESS)
		return hr;

	if ((hr=GetBlkVersionInfo(pVersionInfo, lpszVerComp, lpszVerStr, ulMaxStrLen)) != ERROR_SUCCESS)
		return hr;

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT GetModVersionInfo (HANDLE		hModInst, /* NULL == current app */
									LPCTSTR		lpszVerComp, /* which component */
									LPTSTR		lpszVerStr,
									const ULONG	ulMaxStrLen)
{
	HRESULT	hr=ERROR_SUCCESS;
	TCHAR		szModPath[MAX_PATH+2]=_T("");
	DWORD		dwVerInfoSize=GetModuleFileName((HMODULE)hModInst, szModPath, MAX_PATH);
	if ((0 == dwVerInfoSize) || (dwVerInfoSize > MAX_PATH))
	{
		if (S_OK == (hr=GetLastError()))
			hr = (-1);
		return hr;
	}

	return GetAppVersionInfo(szModPath, lpszVerComp, lpszVerStr, ulMaxStrLen);
}

/*---------------------------------------------------------------------------*/

HRESULT GetBlkBinaryDataPtr (LPVOID pVersionInfo, /* OUT */ LPVOID& pData, /* OUT */ UINT& uLen)
{
	HRESULT	hr=(NULL == pVersionInfo) ? ERROR_BAD_ARGUMENTS : S_OK;
	if (S_OK == hr)
	{
	   if (!VerQueryValue(pVersionInfo, _T("\\"), &pData, &uLen))
		{
			if (S_OK == (hr=GetLastError()))
				hr = (-1);
		}
	}

	if (hr != S_OK)
	{
		pData = NULL;
		uLen = 0;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT GetBlkFixedFileVersionInfo (LPVOID pVersionInfo, /* OUT */ VS_FIXEDFILEINFO& vsInfo)
{
	UINT		uLen=0;
   LPVOID	pBuf=NULL;
	HRESULT	hr=GetBlkBinaryDataPtr(pVersionInfo, pBuf, uLen);

	if (S_OK == hr)
	{
		if ((NULL == pBuf) || (uLen != (sizeof vsInfo)))
			hr = ERROR_ARENA_TRASHED;
		else
			memcpy(&vsInfo, pBuf, min(uLen, (sizeof vsInfo)));
	}

	if (hr != S_OK)
		memset(&vsInfo, 0, (sizeof vsInfo));

	return hr;
}

/*---------------------------------------------------------------------------*/

/* the 64-bit value is formatted into 4 dot-separated 16-bit values */
HRESULT FormatVersionIDInfo (const DWORD dwHi, const DWORD dwLo, LPTSTR lpszID, const UINT32 ulMaxLen)
{
	if ((NULL == lpszID) || (ulMaxLen <= MAX_BYTE_DISPLAY_LENGTH))
		return ERROR_BAD_ENVIRONMENT;

	CStrlBuilder	strb(lpszID, ulMaxLen);
	HRESULT			hr=strb.AddNum((dwHi >> 16) & 0x0000FFFF);
	if (hr != S_OK)
		return hr;
	if ((hr=strb.AddChar(_T('.'))) != S_OK)
		return hr;
	if ((hr=strb.AddNum(dwHi & 0x0000FFFF)) != S_OK)
		return hr;
	if ((hr=strb.AddChar(_T('.'))) != S_OK)
		return hr;
	if ((hr=strb.AddNum((dwLo >> 16) & 0x0000FFFF)) != S_OK)
		return hr;
	if ((hr=strb.AddChar(_T('.'))) != S_OK)
		return hr;
	if ((hr=strb.AddNum(dwLo & 0x0000FFFF)) != S_OK)
		return hr;

	return hr;
}

//////////////////////////////////////////////////////////////////////////////

// Note: errors of retrieving a specific component are IGNORED and the component is reported as EMPTY
HRESULT CVerInfoBlk::EnumVersionComponents (CVERINFO_ENUM_CFN	lpfnEcfn,
														  LPVOID					pArg,
														  LPTSTR					lpszValBuf,
														  const ULONG			ulMaxLen,
														  LPCTSTR				lpszCompNames[]) const
{
	if (NULL == lpfnEcfn)
		return ERROR_INVALID_FUNCTION;

	if (NULL == lpszCompNames)
		return ERROR_SUCCESS;

	BOOL	fContEnum=TRUE;
	for (ULONG	ulVdx=0; fContEnum; ulVdx++)
	{
		LPCTSTR	lpszCompName=lpszCompNames[ulVdx];
		if (IsEmptyStr(lpszCompName))
			break;

		HRESULT	hr=GetVersionCompInfo(lpszCompName, lpszValBuf, ulMaxLen);
		if (hr != ERROR_SUCCESS)
			*lpszValBuf = _T('\0');

		if ((hr=(*lpfnEcfn)(*this, lpszCompName, lpszValBuf, pArg, fContEnum)) != ERROR_SUCCESS)
			return hr;
	}

	return ERROR_SUCCESS;
}

//////////////////////////////////////////////////////////////////////////////
