#include <mfc/general.h>

#include <util/string.h>

/*--------------------------------------------------------------------------*/

BOOL RepostMsgToParent (const CWnd& wnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	CWnd	*pParent=wnd.GetParent();
	if (pParent != NULL)
		return pParent->PostMessage(message, wParam, lParam);
	else
		return FALSE;
}

/*--------------------------------------------------------------------------*/

// Note: if "pWnd" is NULL then it is updated
HRESULT SetWndFieldsState (const CWnd&	wnd,
									WXLTITLE		xlt[],
									const BOOL	fEnabled,	// EnableWindow
									const int	nCmdShow,	// ShowWindow (-1) == no change
									const BOOL	fIsEdit)
{
	if (NULL == xlt)
		return ERROR_SUCCESS;

	for (ULONG	Fdx=0; ; Fdx++)
	{
		WXLTITLE&	wxl=xlt[Fdx];
		if (wxl.nTitleID <= 0) break;

		CWnd*	&pWnd=wxl.pWnd;
		if (NULL == pWnd)
		{
			if (NULL == (pWnd=wnd.GetDlgItem(wxl.nTitleID)))
				return ERROR_NO_DATA;
		}

		if (fIsEdit)
		{
			CEdit	*pEdt=(CEdit *) pWnd;
			pEdt->SetReadOnly(!fEnabled);
		}
		else
			pWnd->EnableWindow(fEnabled);
		if (nCmdShow != (-1))
			pWnd->ShowWindow(nCmdShow);
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT SetWndFieldState (const CWnd&	wnd,
								  const int		nID,
								  const BOOL	fEnabled,		// EnableWindow
								  const int		nCmdShow,	// ShowWindow (-1) == no change
								  const BOOL	fIsEdit)
{
	if (nID <= 0)
		return ERROR_SECTOR_NOT_FOUND;

	CWnd	*pWnd=wnd.GetDlgItem(nID);
	if (NULL == pWnd)
		return ERROR_NO_DATA;

	if (fIsEdit)
	{
		CEdit	*pEdt=(CEdit *) pWnd;
		pEdt->SetReadOnly(!fEnabled);
	}
	else
		pWnd->EnableWindow(fEnabled);

	if (nCmdShow != (-1))
		pWnd->ShowWindow(nCmdShow);

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT SetWndFieldsState (const CWnd&	wnd,
									const int	nIDs[],
									const BOOL	fEnabled,	// EnableWindow
									const int	nCmdShow,	// ShowWindow (-1) == no change
									const BOOL	fIsEdit)
{
	if (NULL == nIDs)
		return ERROR_SUCCESS;

	for (ULONG	Fdx=0; ; Fdx++)
	{
		const int	nID=nIDs[Fdx];
		if (nID <= 0)
			break;

		HRESULT	hr=SetWndFieldState(wnd, nID, fEnabled, nCmdShow, fIsEdit);
		if (hr != S_OK)
			return hr;
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT CheckWndNonEmptyField (const CWnd& wnd, const int nID, BOOL& fNonEmpty)
{
	fNonEmpty = FALSE;

	if (nID <= 0)
		return ERROR_SECTOR_NOT_FOUND;

	CString	strVal=_T("");
	int		nLen=wnd.GetDlgItemText(nID, strVal);
	fNonEmpty = ((0 != nLen) && (!IsEmptyStr(strVal)));

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT CheckWndNonEmptyFields (const CWnd&	wnd,
										  const int		nIDs[],
										  BOOL&			fNonEmpty)
{
	fNonEmpty = FALSE;

	if (NULL == nIDs)
		return ERROR_SUCCESS;

	for (ULONG	Fdx=0; ; Fdx++)
	{
		const int	nID=nIDs[Fdx];
		if (nID <= 0)
			break;

		HRESULT	hr=CheckWndNonEmptyField(wnd, nID, fNonEmpty);
		if (hr != S_OK)
			return hr;

		if (!fNonEmpty)
			break;
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT CheckWndNonZeroField (const CWnd& wnd, const int nID, BOOL& fNonZero)
{
	fNonZero = FALSE;

	if (nID <= 0)
		return ERROR_SECTOR_NOT_FOUND;

	int	nVal=wnd.GetDlgItemInt(nID, NULL, FALSE);
	fNonZero = (0 != nVal);
	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT CheckWndNonZeroFields (const CWnd&	wnd,
										 const int		nIDs[],
										 BOOL&			fNonZero)
{
	fNonZero = FALSE;

	if (NULL == nIDs)
		return ERROR_SUCCESS;

	for (ULONG	Fdx=0; ; Fdx++)
	{
		const int	nID=nIDs[Fdx];
		if (nID <= 0)
			break;

		HRESULT	hr=CheckWndNonZeroField(wnd, nID, fNonZero);
		if (hr != S_OK)
			return hr;

		if (!fNonZero)
			break;
	}

	return ERROR_SUCCESS;
}

/*--------------------------------------------------------------------------*/

HRESULT GetCheckButtonState (const CWnd& wnd, const int nID, BOOL& fChecked)
{
	fChecked = FALSE;

	CButton	*pBtn=(CButton *) wnd.GetDlgItem(nID);
	if (NULL == pBtn)
		return ERROR_SECTOR_NOT_FOUND;

	fChecked = (1 == pBtn->GetCheck());
	return S_OK;
}

/*--------------------------------------------------------------------------*/

BOOL IsKeyDownMsg (const MSG *pMsg, const WPARAM wKey)
{
	if (NULL == pMsg)
		return FALSE;

	if (WM_KEYDOWN != pMsg->message)
		return FALSE;

	return (wKey == pMsg->wParam);
}

/*--------------------------------------------------------------------------*/

BOOL ResizeWndControl (CWnd&		wnd,
							  CWnd&		ctl,
							  const int	dx,
							  const int	dy)
{
	if ((0 == dx) && (0 == dy))
		return TRUE;

	CRect	ctlRect;
	ctl.GetWindowRect(&ctlRect);
	wnd.ScreenToClient(&ctlRect);

	ctlRect.right += dx;
	ctlRect.bottom += dy;

	ctl.MoveWindow(&ctlRect);
	return TRUE;
}

/*--------------------------------------------------------------------------*/

BOOL OccupyWndClientArea (CWnd& wndParent, CWnd& wndChild)
{
	CRect	rcClnt;
	wndParent.GetClientRect(&rcClnt);
	wndChild.MoveWindow(&rcClnt);

	return TRUE;
}

/*--------------------------------------------------------------------------*/
