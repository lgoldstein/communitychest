#include <mfc/listctrl.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

// returns column info of specified index - returns NULL if not found
const LPCLISTCTRLCOLINFO GetMultiColListColByIndex (const LPCLISTCTRLCOLINFO	pCols,
																	 const int						nColIdx)
{
	if (NULL == pCols)
		return NULL;

	for (ULONG ulIdx=0; ; ulIdx++)
	{
		const LPCLISTCTRLCOLINFO	pColInfo=(pCols+ulIdx);
		
		if (NULL == pColInfo->lpszColHdr)
			break;

		if (nColIdx == pColInfo->nColIdx)
			return pColInfo;
	}

	return NULL;
}

/*---------------------------------------------------------------------------*/

// returns column info of specified user info - returns NULL if not found
const LPCLISTCTRLCOLINFO GetMultiColListColByInfo (const LPCLISTCTRLCOLINFO	pCols,
																	const ULONG						ulColInfo)
{
	if (NULL == pCols)
		return NULL;

	for (ULONG ulIdx=0; ; ulIdx++)
	{
		const LPCLISTCTRLCOLINFO	pColInfo=(pCols+ulIdx);
		
		if (NULL == pColInfo->lpszColHdr)
			break;

		if (ulColInfo == pColInfo->ulColInfo)
			return pColInfo;
	}

	return NULL;
}

/*---------------------------------------------------------------------------*/

// returns column info of specified name - returns NULL if not found
const LPCLISTCTRLCOLINFO GetMultiColListColByName (const LPCLISTCTRLCOLINFO	pCols,
																	LPCTSTR							lpszColHdr)
{
	if ((NULL == pCols) || (NULL == lpszColHdr) || (_T('\0') == *lpszColHdr))
		return NULL;

	for (ULONG ulIdx=0; ; ulIdx++)
	{
		const LPCLISTCTRLCOLINFO	pColInfo=(pCols+ulIdx);
		
		if (NULL == pColInfo->lpszColHdr)
			break;

		if (_tcsicmp(pColInfo->lpszColHdr, lpszColHdr) == 0)
			return pColInfo;
	}

	return NULL;
}

/*---------------------------------------------------------------------------*/

static HRESULT HandleMultiColList (CListCtrl& lCtrl, LPCLISTCTRLCOLINFO	pCols, const BOOL fBuildIt)
{
	if (NULL == pCols)
		return ERROR_BAD_ARGUMENTS;

	// get available list width and build list accordingly
	RECT	listRect;
	lCtrl.GetClientRect(&listRect);
	CRect	wRect(&listRect);

	LPCLISTCTRLCOLINFO	pColInfo=pCols;

	for (UINT nCol=0, uAccWidth=0, uAccPercent=0; pColInfo->lpszColHdr != NULL; nCol++)
	{
		LPCLISTCTRLCOLINFO	pNextCol=(pColInfo + 1);
		int						nWidth=0;
		int&						nColIdx=pColInfo->nColIdx;

		if (0 == pColInfo->uColWidth)
		{
			pColInfo = pNextCol;
			continue;
		}

		// calculate column width so as to comply with required "weight",
		// to accomodate header and still not go beyond total list width
		if (pNextCol->lpszColHdr != NULL)
		{
			int nHdrWidth=lCtrl.GetStringWidth(pColInfo->lpszColHdr);

			nWidth = (wRect.Width() * pColInfo->uColWidth) / 100;
			nWidth = max(nHdrWidth, nWidth);
		}
		else	// make last column span entire width
		{
			nWidth = (wRect.Width() - uAccWidth);
			pColInfo->uColWidth = (100 - uAccPercent);
		}

		nWidth = min(nWidth, wRect.Width());
		uAccWidth += nWidth;
		if ((uAccPercent += pColInfo->uColWidth) > 100)
			return ERROR_MORE_DATA;

		if (fBuildIt)
		{
			if ((-1) == (nColIdx=lCtrl.InsertColumn(nCol, pColInfo->lpszColHdr, pColInfo->nFormat, nWidth, 0)))
				return ERROR_BAD_ENVIRONMENT;
		}
		else
		{
			if (!lCtrl.SetColumnWidth(nColIdx, nWidth))
				return ERROR_BAD_ENVIRONMENT;
		}

		pColInfo = pNextCol;
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

// builds multi-column list
HRESULT BuildMultiColList (CListCtrl&	lCtrl, LPCLISTCTRLCOLINFO	pCols)
{
	return HandleMultiColList(lCtrl, pCols, TRUE);
}

/*---------------------------------------------------------------------------*/

// re-calculates columns widths
HRESULT ReadjustMultiColList (CListCtrl&	lCtrl, LPCLISTCTRLCOLINFO	pCols)
{
	// disable redraw while re-adjusting so the display does not flicker
	lCtrl.SetRedraw(FALSE);
	HRESULT	hr=HandleMultiColList(lCtrl, pCols, FALSE);
	lCtrl.SetRedraw(TRUE);

	return hr;
}

/*---------------------------------------------------------------------------*/

// returns:
//
//		ERROR_BAD_LENGTH - if width is not within specified min./max. values
//		ERROR_BUFFER_OVERFLOW - if total width exceeds list control client area
HRESULT VerifyListCtrlColWidth (const CListCtrl&	lCtrl,
										  const UINT			nColIndex,
										  const UINT			nNewWidth,	// pixels
										  const UINT			nMinWidth,	// percent
										  const UINT			nMaxWidth)	// percent
{
	if (0 == nNewWidth)
		return ERROR_INVALID_SERVER_STATE;

	// get current number of columns
	const CHeaderCtrl	*plHdr=((CListCtrl &) lCtrl).GetHeaderCtrl();
	if (NULL == plHdr)
		return ERROR_BAD_ENVIRONMENT;

	const UINT	nColsNum=(UINT) plHdr->GetItemCount();
	if (nColIndex >= nColsNum)
		return ERROR_BAD_ARGUMENTS;

	// get current list client area display width - if any column exceeds this width then resizing not allowed
	CRect	rcList;
	lCtrl.GetClientRect(&rcList);
	const UINT	nTotalWidth=(UINT) rcList.Width();

	// make sure new width does not fall below min. specified width
	const UINT	nNewPercent=((nNewWidth * 100U) / nTotalWidth);
	if ((nNewPercent < nMinWidth) || (nNewPercent > nMaxWidth))
		return ERROR_BAD_LENGTH;

	// calculate new expected width
	for (UINT nColNdx=0, nAccWidth=0; nColNdx < nColsNum; nColNdx++)
	{
		// use the new column width for the changed column, and NOT the current value
		const UINT	uColWidth=((nColIndex != nColNdx) ? (UINT) lCtrl.GetColumnWidth((int) nColNdx) : nNewWidth);
		if ((nAccWidth += uColWidth) > nTotalWidth)
			return ERROR_BUFFER_OVERFLOW;
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

// calls "VerifyListCtrlColWidth" and updates the percentages only if verification successful
HRESULT UpdateListCtrlColumnWidth (const CListCtrl&	lCtrl,
											  const UINT			nColIndex,
											  const UINT			nNewWidth,	// pixels
											  const UINT			nMinWidth,	// percent
											  const UINT			nMaxWidth,	// percent (100 == no limit)
											  CLISTCTRLCOLINFO	colInfo[])
{
	HRESULT	hr=VerifyListCtrlColWidth(lCtrl, nColIndex, nNewWidth, nMinWidth, nMaxWidth);
	if (hr != S_OK)
		return hr;

	if (NULL == colInfo)
		return ERROR_BAD_ARGUMENTS;

	// get current list width so we calculate everything relative to it
	CRect	rcList;
	lCtrl.GetClientRect(&rcList);
	const UINT	nTotalWidth=(UINT) rcList.Width();

	for (UINT	nColNdx=0, nAccWidth=0; ; nColNdx++)
	{
		CLISTCTRLCOLINFO&	ci=colInfo[nColNdx];
		LPCTSTR	lpszColName=ci.lpszColHdr;
		if (IsEmptyStr(lpszColName))
			break;

		// use the new column width for the changed column, and NOT the current value
		const UINT	uColWidth=((nColIndex != nColNdx) ? (UINT) lCtrl.GetColumnWidth((int) nColNdx) : nNewWidth);
		ci.uColWidth = ((uColWidth * 100) / nTotalWidth);
		nAccWidth += uColWidth;
	}

	return S_OK;
}
