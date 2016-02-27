#include <mfc/listctrl.h>

/*---------------------------------------------------------------------------*/

int ListCtrlHitTestEx (const CListCtrl&	lCtrl,
							  const CPoint&		point,
							  int						*col)
{
	int colnum=0, row=lCtrl.HitTest(point, NULL);
	if (col != NULL)
		*col = 0;

	// Make sure that the ListView is in LVS_REPORT
	if ((GetWindowLong(lCtrl.m_hWnd, GWL_STYLE) & LVS_TYPEMASK) != LVS_REPORT)
		return row;

	// Get the top and bottom row visible
	row = lCtrl.GetTopIndex();
	int bottom=row + lCtrl.GetCountPerPage();
	if (bottom > lCtrl.GetItemCount())
		bottom = lCtrl.GetItemCount();

	// Get the number of columns
	CHeaderCtrl	*pHeader=(CHeaderCtrl*) lCtrl.GetDlgItem(0);
#ifdef _DEBUG
	ASSERT(pHeader != NULL);
#else
	if (NULL == pHeader)
		return (-1);
#endif

	int nColumnCount=pHeader->GetItemCount();

	// Loop through the visible rows
	for ( ; row <= bottom; row++)
	{
		// Get bounding rect of item and check whether point falls in it.
		CRect rect;
		lCtrl.GetItemRect(row, &rect, LVIR_BOUNDS);

		if (rect.PtInRect(point))
		{
			// Now find the column
			for (colnum=0; colnum < nColumnCount; colnum++ )
			{
				int colwidth=lCtrl.GetColumnWidth(colnum);

				if ((point.x >= rect.left) && (point.x <= (rect.left + colwidth)))
				{
					if (col != NULL)
						*col = colnum;
					return row;
				}

				rect.left += colwidth;
			}
		}
	}

	return (-1);
}

/*---------------------------------------------------------------------------*/

// calls the enumeration callback for each selected item (if any)
HRESULT EnumSelectedListCtrlItems (const CListCtrl&	listCtrl,
											  LCTRL_SELITEMS_CFN	lpfnEcfn,
											  LPVOID					pArg)
{
	HRESULT	hr=ERROR_SUCCESS;
	int		nItems=listCtrl.GetItemCount();

	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	for (int	nItem=listCtrl.GetNextItem((-1), LVNI_SELECTED | LVNI_ALL); nItem >= 0; nItem=listCtrl.GetNextItem(nItem, LVNI_SELECTED | LVNI_ALL))
	{
		const DWORD		dwItemData=listCtrl.GetItemData(nItem);
		if (LB_ERR == dwItemData)
			continue;

		BOOL	fContEnum=TRUE;
		if ((hr=(*lpfnEcfn)((CListCtrl &) listCtrl, nItem, (LPVOID) dwItemData, pArg, fContEnum)) != ERROR_SUCCESS)
			return hr;

		if (!fContEnum)
			break;
	}

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT UpdateAllItemsSelection (CListCtrl& listCtrl, const BOOL fSelected)
{
	for (int	nItem=0; nItem < listCtrl.GetItemCount(); nItem++)
	{
		if (fSelected)
			listCtrl.SetItemState(nItem, LVIS_SELECTED | LVIS_FOCUSED,  LVIS_SELECTED | LVIS_FOCUSED);
		else
			listCtrl.SetItemState(nItem, 0,  0);
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT UpdateListStr (LPCTSTR lpszVal,  const ULONG ulVLen,
							  LPTSTR  lpszText, const ULONG cchTextMax)
{
	if (NULL == lpszText)
	{
		TRACE(_T("UpdateListStr - bad text output !!!\n"));
		return ERROR_BAD_ARGUMENTS;
	}

	ULONG	ulMLen=min(ulVLen, (cchTextMax-1));
	if (ulMLen > 0)
	{
		if (NULL == lpszVal)
		{
			TRACE(_T("UpdateListStr - bad text input !!!\n"));
			return ERROR_BAD_ARGUMENTS;
		}

		_tcsncpy(lpszText, lpszVal, ulMLen);
		lpszText[ulMLen] = _T('\0');
	}
	else	// nothing to copy
		*lpszText = _T('\0');

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT UpdateListStr (const ULONG ulVal, LPTSTR  lpszText, const ULONG cchTextMax)
{
	TCHAR		szVal[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	ULONG		ulVLen=dword_to_argument((DWORD) ulVal, szVal);

	return UpdateListStr(szVal, ulVLen, lpszText, cchTextMax);
}

/*---------------------------------------------------------------------------*/

HRESULT ReleaseListItem (CListCtrl&			lCtrl,
								 const int			nItem,
								 LISTDATA_REL_CFN	lpfnRcfn,	// NULL == no release necessary
								 LPVOID				pArg)
{
	if ((nItem < 0) || (nItem >= lCtrl.GetItemCount()))
		return ERROR_BAD_ARGUMENTS;

	const DWORD	dwData=lCtrl.GetItemData(nItem);
	if ((0 != dwData) && ((DWORD) (-1) != dwData))
	{
		HRESULT	hr=(*lpfnRcfn)(lCtrl, nItem, dwData, pArg);
		if (ERROR_SUCCESS != hr)
			return hr;
	}

	if (!lCtrl.DeleteItem(nItem))
		return ERROR_BAD_ENVIRONMENT;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

static HRESULT ReleaseListContents (CListCtrl&			lCtrl,
												const BOOL			fOnlySelected,
												LISTDATA_REL_CFN	lpfnRcfn,	// NULL == no release necessary
												LPVOID				pArg)

{
	if (NULL == lpfnRcfn)
		return S_OK;

	if (fOnlySelected && (0 == lCtrl.GetSelectedCount()))
		return S_OK;

	for (int	nItem=0; nItem < lCtrl.GetItemCount(); nItem++)
	{
		if (fOnlySelected)
		{
			if (lCtrl.GetItemState(nItem, LVIS_SELECTED) != LVIS_SELECTED)
				continue;
		}

		HRESULT	hr=(*lpfnRcfn)(lCtrl, nItem, lCtrl.GetItemData(nItem), pArg);
		if (ERROR_SUCCESS != hr)
			return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT ReleaseAllContents (CListCtrl&			lCtrl,
									 LISTDATA_REL_CFN	lpfnRcfn,	// NULL == no release necessary
									 LPVOID				pArg)
{
	HRESULT	hr=ReleaseListContents(lCtrl, FALSE, lpfnRcfn, pArg);
	if (hr != S_OK)
		return hr;

	if (!lCtrl.DeleteAllItems())
		return RPC_S_CALL_FAILED;

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT ReleaseSelectedContents (CListCtrl&			lCtrl,
											LISTDATA_REL_CFN	lpfnRcfn,	// NULL == no release necessary
											LPVOID				pArg)
{
	HRESULT	hr=ReleaseListContents(lCtrl, TRUE, lpfnRcfn, pArg);
	if (hr != S_OK)
		return hr;

	for (int	nItem=0, nCount=lCtrl.GetItemCount(); nItem < nCount; )
	{
		if (lCtrl.GetItemState(nItem, LVIS_SELECTED) == LVIS_SELECTED)
		{
			if (!lCtrl.DeleteItem(nItem))
				return RPC_S_CALL_FAILED;

			// if deleted item then do not increment the "nItem" index
			nCount = lCtrl.GetItemCount();
			continue;
		}
	
		nItem++;
	}

	return S_OK;
}

void SetAllListItemsSelectionState (CListCtrl& lCtrl, const BOOL fSelected)
{
	const UINT	nState=(fSelected ? LVIS_SELECTED : 0);
	for (int nItem=0; nItem < lCtrl.GetItemCount(); nItem++)
		lCtrl.SetItemState(nItem, nState, LVIS_SELECTED);
}

/////////////////////////////////////////////////////////////////////////////

int sortListStr (LPCTSTR lpszS1,	const ULONG ulL1,
					  LPCTSTR lpszS2,	const ULONG ulL2,
					  const BOOL		fAscending)
{
	int	nRes=0;
	if (IsEmptyStr(lpszS1) || (0 == ulL1))
	{
		if ((!IsEmptyStr(lpszS2)) && (ulL2 != 0))
			nRes = (-1);
	}
	else if (IsEmptyStr(lpszS2) || (0 == ulL2))
	{
		if ((!IsEmptyStr(lpszS1)) && (ulL1 != 0))
			nRes = 1;
	}
	else	// both non empty
	{
		ULONG	ulMLen=min(ulL1, ulL2);
		nRes = _tcsncmp(lpszS1, lpszS2, ulMLen);

		// if equal, check which is longer
		if (0 == nRes)
			nRes = (int) (ulL2 - ulL1);
	}

	return AdjustListCmpRes(SIGNOF(nRes), fAscending);
}

/////////////////////////////////////////////////////////////////////////////

int sortListStr (LPCTSTR lpszS1,				const ULONG ulL1,
					  LPCTSTR lpszS2,				const ULONG ulL2,
					  const LPCLISTCTRLCOLINFO pColInfo)
{
	if (NULL == pColInfo)
	{
		TRACE(_T("sortStr - no col info !!!\n"));
		return 0;
	}

	return sortListStr(lpszS1, ulL1, lpszS2, ulL2, pColInfo->fAscending);
}

int sortListStr (LPCTSTR lpszS1, LPCTSTR lpszS2, const LPCLISTCTRLCOLINFO	pColInfo)
{
	return sortListStr(lpszS1, GetSafeStrlen(lpszS1), lpszS2, GetSafeStrlen(lpszS2), pColInfo);
}

int sortListStr (LPCTSTR lpszS1, LPCTSTR lpszS2, const BOOL fAscending)
{
	return sortListStr(lpszS1, GetSafeStrlen(lpszS1), lpszS2, GetSafeStrlen(lpszS2), fAscending);
}

int sortListNum (const ULONG	ul1, const ULONG ul2, const BOOL fAscending)
{
	if (ul1 > ul2)
		return AdjustListCmpRes(1, fAscending);
	else if (ul1 < ul2)
		return AdjustListCmpRes((-1), fAscending);
	else
		return 0;
}

int sortListNum (const ULONG	ul1, const ULONG ul2, const LPCLISTCTRLCOLINFO	pColInfo)
{
	if (NULL == pColInfo)
		return 0;
	else
		return sortListNum(ul1, ul2, pColInfo->fAscending);
}

/////////////////////////////////////////////////////////////////////////////
