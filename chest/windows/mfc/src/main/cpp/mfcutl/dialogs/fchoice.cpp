#include <mfc/dialogs.h>
#include <util/string.h>
#include <win32/general.h>

/*--------------------------------------------------------------------------*/

// returns ERROR_CANCELLED if user cancelled rather than OK
HRESULT GetUserChosenFiles (CWnd			*pParent,
									 CString&	iDir,		// in/out
									 LPCTSTR		lpszFilter,
									 LPCTSTR		lpszDefExt,
									 const BOOL	fSaveIt,
									 CString&	filePath)
{
	filePath = _T("");

	if (IsEmptyStr(lpszDefExt) || IsEmptyStr(lpszFilter))
		return ERROR_BAD_ARGUMENTS;

	CFileDialog	fileDlg((!fSaveIt), (_T('.') == *lpszDefExt) ? (lpszDefExt + 1) : lpszDefExt, NULL, OFN_OVERWRITEPROMPT | OFN_HIDEREADONLY, lpszFilter, pParent);
	fileDlg.m_ofn.lpstrInitialDir = iDir;
	int nRes=fileDlg.DoModal();
	if (nRes != IDOK)
		return ERROR_CANCELLED;

	filePath = fileDlg.GetPathName();
	int	nLen=filePath.GetLength();

	// update initial dir...
	for (int i=nLen-1; i > 0; i--)
	{
		TCHAR	cDelim=filePath[i];

		if ((_T('/') == cDelim) || (_T('\\') == cDelim))
		{
			iDir = filePath.Left(i+1);
			break;
		}
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT BuildChosenFilesFilter (LPCTSTR		lpszExts[],	// last entry = NULL
										  LPCTSTR		lpszDescs[],	// last entry = NULL
										  CString& 		fltr)	// in out
{
	fltr = _T("");

	if ((NULL == lpszExts) || (NULL == lpszDescs))
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	i=0; ; i++)
	{
		LPCTSTR			lpszExtPar=lpszExts[i], lpszDesc=lpszDescs[i];
		const BOOLEAN	haveExt=!IsEmptyStr(lpszExtPar), haveDesc=(!IsEmptyStr(lpszDesc));
		if ((!haveExt) || (!haveDesc))
		{
			// BOTH must be empty
			if (haveExt || haveDesc)
				return ERROR_ARENA_TRASHED;

			break;
		}

		LPCTSTR	lpszExt=(_T('.') == *lpszExtPar) ? (lpszExtPar + 1) : lpszExtPar;
		if (IsEmptyStr(lpszExt))
			return ERROR_SECTOR_NOT_FOUND;

		CString	subFltr=_T("");
		subFltr.Format(_T("%s (*.%s)|*.%s|"), lpszDesc, lpszExt, lpszExt);

		fltr += subFltr;
	}

	if (fltr.GetLength() <= 0)
		return ERROR_NO_DATA;	// make sure something added

	fltr += _T("|");	// mark end
	return S_OK;
}

/*--------------------------------------------------------------------------*/

// returns ERROR_CANCELLED if user cancelled rather than OK
HRESULT GetUserChosenFiles (CWnd			*pParent,
									 CString&	iDir,		// in/out
									 LPCTSTR		lpszExts[],	// last entry = NULL
									 LPCTSTR		lpszDescs[],	// last entry = NULL
									 LPCTSTR		lpszDefExt,
									 const BOOL	fSaveIt,
									 CString&	filePath)
{
	filePath = _T("");

	if ((NULL == lpszExts) || (NULL == lpszDescs))
		return ERROR_BAD_ARGUMENTS;

	CString	fltr=_T("");
	HRESULT	hr=BuildChosenFilesFilter(lpszExts, lpszDescs, fltr);
	if (hr != S_OK)
		return hr;

	return GetUserChosenFiles(pParent, iDir, fltr, lpszDefExt, fSaveIt, filePath);
}

/*--------------------------------------------------------------------------*/

HRESULT GetUserChosenFile (CWnd			*pParent,
									CString&		iDir,		// in/out
									LPCTSTR		lpszExtPar,
									LPCTSTR		lpszDesc,
									const BOOL	fSaveIt,
									CString&		filePath)
{
	LPCTSTR lpszExts[]={ lpszExtPar, NULL };
	LPCTSTR lpszDescs[]={ lpszDesc, NULL };
	return GetUserChosenFiles(pParent, iDir, lpszExts, lpszDescs, lpszExtPar, fSaveIt, filePath);
}

/*--------------------------------------------------------------------------*/

HRESULT GetUserChosenFile (CWnd			*pParent,
									const int	nID,
									LPCTSTR		lpszDesc,
									const BOOL	fSaveIt,
									CString&		filePath)
{
	filePath = _T("");

	if (NULL == pParent)
		return ERROR_BAD_ARGUMENTS;

	CString		strVal=_T("");
	const int	nLen=pParent->GetDlgItemText(nID, strVal);
	const int	nExtPos=strVal.ReverseFind(_T('.'));
	CString		strExt=((nExtPos > 0) ? strVal.Right(nLen - nExtPos) : _T(""));
	const int	nDirPos=strVal.ReverseFind(_T('\\'));
	CString		strDir=((nDirPos > 0) ? strVal.Left(nDirPos) : _T(""));
	CString		strName=((nDirPos > 0) ? strVal.Right(nLen - nDirPos - 1) : _T(""));

	if (strDir.IsEmpty())
	{
		TCHAR		szAppDir[MAX_PATH+2]=_T("");
		HRESULT	hr=GetAppDirectory(szAppDir, MAX_PATH);
		if (hr != S_OK)
			return hr;

		strDir = szAppDir;
	}

	if (strExt.IsEmpty())
		strExt = _T("*");

	return GetUserChosenFile(pParent, strDir, strExt, lpszDesc, fSaveIt, filePath);
}

/*--------------------------------------------------------------------------*/

HRESULT UpdateUserChosenFile (CWnd			*pParent,
										const int	nID,
										LPCTSTR		lpszDesc)
{
	CString	filePath=_T("");
	HRESULT	hr=GetUserChosenFile(pParent, nID, lpszDesc, FALSE, filePath);
	if (hr != S_OK)
		return hr;

	pParent->SetDlgItemText(nID, filePath);
	return S_OK;
}

/*--------------------------------------------------------------------------*/
