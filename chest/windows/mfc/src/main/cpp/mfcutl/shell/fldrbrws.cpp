#include <mfc/shell.h>

/*--------------------------------------------------------------------------*/

// Note: returns ERROR_CANCELLED if user pressed the "Cancel" button
HRESULT GetChosenUserFolder (HWND		hOwner,
									  LPCTSTR	lpszTitle,
									  CString&	curFolder /* in/out */)
{
	HRESULT	hr=S_OK;
	TCHAR		szCurDir[MAX_PATH+2]=_T("");

	if (curFolder.IsEmpty())
	{
		DWORD	dw=GetCurrentDirectory(MAX_PATH, szCurDir);
		if ((0 == dw) || (dw > MAX_PATH))
		{
			hr = GetLastError();
			return hr;
		}
	}
	else	// have a current folder
	{
		if (curFolder.GetLength() >= MAX_PATH)
			return ERROR_BUFFER_OVERFLOW;
		_tcscpy(szCurDir, curFolder);
	}

	LPMALLOC pMalloc=NULL;
   if ((hr=::SHGetMalloc(&pMalloc)) != NOERROR)
		return hr;

	LPITEMIDLIST	lpItemIdList=NULL;
	BROWSEINFO		bi;
	memset(&bi, 0, (sizeof bi));

	bi.hwndOwner = hOwner;
	bi.pidlRoot = lpItemIdList;
	bi.pszDisplayName = szCurDir;
	bi.lpszTitle = lpszTitle;
	bi.ulFlags = BIF_RETURNFSANCESTORS | BIF_RETURNONLYFSDIRS;

	// This next call issues the dialog box.
	LPITEMIDLIST pidl=::SHBrowseForFolder(&bi);
	if (pidl != NULL)
   {
		if (::SHGetPathFromIDList(pidl, szCurDir))
			curFolder = szCurDir;
		else
			hr = ERROR_INVALID_FUNCTION;

		// Free the PIDL allocated by SHBrowseForFolder.
		pMalloc->Free(pidl);
		pidl = NULL;
	}
	else
		hr = ERROR_CANCELLED;

	if (lpItemIdList != NULL)
	{
		// Free the PIDL allocated for root
		pMalloc->Free(lpItemIdList);
		lpItemIdList = NULL;
	}

	// Release the shell's allocator.
	ULONG	ulRefCount=pMalloc->Release();
	return hr;
}

/*--------------------------------------------------------------------------*/
