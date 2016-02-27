#include <win32/general.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

HRESULT GetCurrentAppName (LPCTSTR& lpszAppName)
{
	static TCHAR	szAppName[MAX_PATH+2]=_T("");
	HRESULT	hr=ERROR_SUCCESS;

	lpszAppName = NULL;

	if (szAppName[0] != _T('\0'))
	{
		lpszAppName = szAppName;
		return ERROR_SUCCESS;
	}

	DWORD dwLen=GetModuleFileName(NULL, szAppName, (MAX_PATH+1));
	if ((0 == dwLen) || (dwLen > MAX_PATH))
	{
		szAppName[0] = _T('\0');
		if (S_OK == (hr=GetLastError()))
			hr = ERROR_BUFFER_OVERFLOW;
		return hr;
	}
	
	LPTSTR	lpszA=strlast(szAppName);
	LPCTSTR	lpszSfx=NULL;
	for (lpszA--; lpszA > szAppName; lpszA--)
	{
		if ((_T('.') == *lpszA) && (NULL == lpszSfx))
		{
			lpszSfx = (lpszA+1);
			*lpszA = _T('\0');
		}

		if ((_T('/') == *lpszA) || (_T('\\') == *lpszA))
		{
			lpszA++;
			break;
		}
	}

	if (lpszA > szAppName)
		_tcscpy(szAppName, lpszA);

	lpszAppName = szAppName;
	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT GetModuleDirectory (HMODULE hInst, LPTSTR lpszDir, const ULONG ulMaxLen)
{
	DWORD		dwLen=0;
	LPTSTR	lpszLast=NULL;

	if ((NULL == lpszDir) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;

	dwLen = GetModuleFileName(hInst, lpszDir, ulMaxLen);
	if ((0 == dwLen) || (dwLen > ulMaxLen))
	{
		HRESULT	hr=GetLastError();
		if (S_OK == hr)
			hr = ERROR_BUFFER_OVERFLOW;
		return hr;
	}

	if (NULL == (lpszLast=_tcsrchr(lpszDir, _T('\\'))))
		return ERROR_BAD_FORMAT;

	*lpszLast = _T('\0');
	return S_OK;
}

/*---------------------------------------------------------------------------*/

/*		To be used for getting a handle for the current thread if needs to be
 * accessed from another thread.
 *
 *	Note: GetCurrentThread() returns a PSEUDO-handle which cannot be used
 *			by another thread (e.g. to suspend/terminate)
 */
HRESULT GetSafeCurrentThreadHandle (HANDLE& hSafe)
{
	BOOL	fSuccess=DuplicateHandle(GetCurrentProcess(),
											 GetCurrentThread(),
											 GetCurrentProcess(),
											 &hSafe,
											 0, FALSE, DUPLICATE_SAME_ACCESS);
	if (fSuccess)
		return ERROR_SUCCESS;

	HRESULT	hr=GetLastError();
	hSafe = NULL;
	return hr;
}

/*---------------------------------------------------------------------------*/
