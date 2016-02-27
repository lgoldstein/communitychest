#include <mfc/general.h>

/*--------------------------------------------------------------------------*/

HRESULT InitFieldsTitles (WXLTITLE	xlt[])
{
	if (NULL == xlt)
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	i=0; ; i++)
	{
		const WXLTITLE&	xt=xlt[i];
		if (NULL == xt.pWnd)
			break;

		CWnd&		wnd=*xt.pWnd;
		if (!SetWindowText(wnd, xt.nTitleID))
		{
			CString	fldTitle=_T("");
			fldTitle.Format(_T("Title=%d"), xt.nTitleID);
			wnd.SetWindowText(fldTitle);
		}
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT InitFieldsTexts (WXLTEXT	xlt[])
{
	if (NULL == xlt)
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	i=0; ; i++)
	{
		const WXLTEXT&	xt=xlt[i];
		if (NULL == xt.pWnd)
			break;

		CWnd&		wnd=*xt.pWnd;
		LPCTSTR	lpszText=xt.lpszWndText;
		SetWindowText(wnd, ((NULL == lpszText) ? _T("") : lpszText));
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT InitFieldsTitles (const CWnd&	wnd, const WXLID2TXT xlt[])
{
	if (NULL == xlt)
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	i=0; ; i++)
	{
		const WXLID2TXT&	xt=xlt[i];
		if (xt.nID <= 0)
			break;

		CWnd	*pFld=wnd.GetDlgItem(xt.nID);
		if (NULL == pFld)
			return ERROR_NO_DATA;

		LPCTSTR	lpszText=xt.lpszWndText;
		SetWindowText(*pFld, ((NULL == lpszText) ? _T("") : lpszText));
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/
