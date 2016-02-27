#include <mfc/listctrl.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

HRESULT DisplayEmptyListMessage (CListCtrl& lCtrl, LPCTSTR lpszMsg)
{
	if (IsEmptyStr(lpszMsg))
		return ERROR_SECTOR_NOT_FOUND;

	if (lCtrl.GetItemCount() > 0)
		return S_OK;

	CDC		*pDC=lCtrl.GetDC();
#ifdef _DEBUG
	ASSERT(pDC != NULL);
#else
	if (NULL == pDC)
		return ERROR_BAD_ENVIRONMENT;
#endif

	// Save dc state
	int nSavedDC = pDC->SaveDC();

	CRect rc;
	lCtrl.GetWindowRect(&rc);
	lCtrl.ScreenToClient(&rc);

	CHeaderCtrl	*pHC=lCtrl.GetHeaderCtrl();
	if (pHC != NULL)
	{
		CRect rcH;
		pHC->GetItemRect(0, &rcH);
		rc.top += rcH.bottom;
	}
	rc.top += 10;

	COLORREF clrText = GetSysColor(COLOR_WINDOWTEXT);
	pDC->SetTextColor(clrText);
	COLORREF clrTextBk = GetSysColor(COLOR_WINDOW);
	pDC->SetBkColor(clrTextBk);
	pDC->FillRect(rc, &CBrush(clrTextBk));

	pDC->SelectStockObject(ANSI_VAR_FONT);
	pDC->DrawText(lpszMsg, _tcslen(lpszMsg), rc, DT_CENTER | DT_WORDBREAK | DT_NOPREFIX | DT_NOCLIP);

   // Restore dc
   pDC->RestoreDC(nSavedDC);
	lCtrl.ReleaseDC(pDC);

	return ERROR_SUCCESS;
}

HRESULT DisplayEmptyListMessage (CListCtrl& lCtrl, const int nMsgID)
{
	CString	strMsg=_T("");
	if (!strMsg.LoadString(nMsgID))
		return ERROR_FILE_NOT_FOUND;

	return DisplayEmptyListMessage(lCtrl, strMsg);
}

/////////////////////////////////////////////////////////////////////////////
