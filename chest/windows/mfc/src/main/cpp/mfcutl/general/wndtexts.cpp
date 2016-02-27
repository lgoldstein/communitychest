#include <mfc/general.h>
#include <win32/general.h>

/*--------------------------------------------------------------------------*/

BOOL SetWindowText (CWnd& wnd, const int nStrID)
{
	CString	strText=_T("");

	if (!strText.LoadString(nStrID))
		return FALSE;

	return ((wnd.m_hWnd != NULL) ? ::SetWindowText(wnd.m_hWnd, strText) : FALSE);
}

/*--------------------------------------------------------------------------*/

HRESULT SetUTF8WindowText (CWnd& wnd, LPCTSTR lpszUTF8str, const ULONG ulUTF8Len)
{
	if (0 == ulUTF8Len)
		wnd.SetWindowText(_T(""));

	CUTF8ToWideCharString	wcs;
	HRESULT						hr=wcs.SetString(lpszUTF8str, ulUTF8Len);
	if (S_OK == hr)
	{
		if (!SetWindowTextW(wnd.GetSafeHwnd(), wcs))
			hr = ERROR_BAD_ENVIRONMENT;
	}

	return hr;
}

/*--------------------------------------------------------------------------*/

HRESULT GetUTF8DlgItemText (const CWnd&	wnd,
									 const int		nItemID,
									 LPTSTR			lpszUTF8str,
									 const ULONG	ulMaxUTF8Len)
{
	if ((nItemID <= 0) || (NULL == lpszUTF8str) || (0 == ulMaxUTF8Len))
		return ERROR_BAD_ARGUMENTS;
	*lpszUTF8str = _T('\0');

	LPWSTR	pW=new WCHAR[ulMaxUTF8Len+2];
	if (NULL == pW)
		return ERROR_OUTOFMEMORY;
	*pW = L'\0';
	CWideCharStrBufGuard	wsg(pW);

	UINT	uLen=GetDlgItemTextW(wnd.GetSafeHwnd(), nItemID, pW, ulMaxUTF8Len);
	if ((ULONG) uLen > ulMaxUTF8Len)
		return ERROR_BUFFER_OVERFLOW;

	int	xLen=WideCharToMultiByte(CP_UTF8, 0, pW, (-1), lpszUTF8str, (int) ulMaxUTF8Len, NULL, NULL);
	if (0 == xLen)
		return GetLastError();

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT SetUTF8DlgItemText (CWnd&			wnd,
									 const int		nItemID,
									 LPCTSTR			lpszUTF8str,
									 const ULONG	ulUTF8Len)
{
	if (nItemID <= 0)
		return ERROR_BAD_ARGUMENTS;

	if (0 == ulUTF8Len)
	{
		wnd.SetDlgItemText(nItemID, _T(""));
		return S_OK;
	}

	if (NULL == lpszUTF8str)
		return ERROR_BAD_ARGUMENTS;

	CUTF8ToWideCharString	wcs;
	HRESULT						hr=wcs.SetString(lpszUTF8str, ulUTF8Len);
	if (S_OK == hr)
		SetDlgItemTextW(wnd.GetSafeHwnd(), nItemID, wcs);

	return hr;
}

/*--------------------------------------------------------------------------*/
