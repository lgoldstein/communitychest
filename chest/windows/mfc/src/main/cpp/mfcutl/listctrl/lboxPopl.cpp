#include <mfc/mfcutl.h>

/////////////////////////////////////////////////////////////////////////////

CListBoxMultiSelEnum::CListBoxMultiSelEnum (const CListBox& lBox)
 : m_nCurSel(0), m_nTotSel(lBox.GetSelCount()), m_SelItems()
{
	if (LB_ERR == m_nTotSel)
		m_nTotSel = 0;
	if (0 == m_nTotSel)
		return;

	m_SelItems.SetSize(m_nTotSel);
	const int nFetched=lBox.GetSelItems(m_nTotSel, m_SelItems.GetData());
	ASSERT(nFetched == m_nTotSel);
}

/*---------------------------------------------------------------------------*/

int CListBoxMultiSelEnum::GetNextSelectedItem ()
{
	if (m_nCurSel >= m_nTotSel)
		return (-1);

	int nItem=m_SelItems.GetAt(m_nCurSel);
	m_nCurSel++;
	return nItem;
}

/////////////////////////////////////////////////////////////////////////////

// Note: returns ERROR_FILE_NOT_FOUND if default value not found in list
HRESULT SetDefaultSel (CListBox& lb, const DWORD defVal)
{
	int	nCount=lb.GetCount();
	for (int nItem=0; nItem < nCount; nItem++)
	{
		DWORD	dwData=lb.GetItemData(nItem);
		if (defVal == dwData)
		{
			int	nRes=lb.SetCurSel(nItem);
			if (LB_ERR == nRes)
				return ERROR_OPERATION_ABORTED;
			return ERROR_SUCCESS;
		}
	}

	return ERROR_FILE_NOT_FOUND;
}

/*---------------------------------------------------------------------------*/

HRESULT PopulateChoicesList (CListBox&		lBox,
									  const STLXL	sglXl[],
									  const DWORD	dwDefData)
{
	if (NULL == sglXl)
		return ERROR_BAD_ARGUMENTS;

	for (int i=0; ; i++)
	{
		const STLXL& xlr=sglXl[i];
		if (NULL == xlr.lpszStr)
			break;

		int	nItem=lBox.AddString(xlr.lpszStr);
		if ((LB_ERR == nItem) || (LB_ERRSPACE == nItem))
		{
			TRACE(_T("\tPopulateChoicesList(%d) - cannot insert \"%s\" !!!\n"), i, xlr.lpszStr);
			continue;
		}

		int	nRes=lBox.SetItemData(nItem, xlr.dwSelData);
		if (LB_ERR == nRes)
			TRACE(_T("\tPopulateImportanceList(%d) - cannot set \"%s\" data !!!\n"), i, xlr.lpszStr);
	}

	SetDefaultSel(lBox, dwDefData);
	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateChoicesList (CListBox&		lBox,
									  const PTLXL	tglXl[],
									  const DWORD	dwDefData)
{
	if (NULL == tglXl)
		return ERROR_BAD_ARGUMENTS;

	for (int i=0; ; i++)
	{
		const PTLXL&	plx=tglXl[i];
		if (plx.nSelName <= 0)
			break;

		CString	strTgName=_T("");
		if (!strTgName.LoadString(plx.nSelName))
			strTgName.Format(_T("Choice=%lu"), plx.dwSelData);

		int	nItem=lBox.AddString(strTgName);
		if ((LB_ERR == nItem) || (LB_ERRSPACE == nItem))
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) add \"%s\" !!!\n"), nItem, (LPCTSTR) strTgName);
			continue;
		}

		int	nDErr=lBox.SetItemData(nItem, (DWORD) plx.dwSelData);
		if (LB_ERR == nDErr)
			TRACE(_T("\tPopulateChoicesList - cannot (%d) set \"%s\" data !!!\n"), nDErr, (LPCTSTR) strTgName);
	}

	SetDefaultSel(lBox, dwDefData);
	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

// NULL terminate list of strings (associated data is the string pointer itself)
HRESULT PopulateChoicesList (CListBox& lBox, LPCTSTR lpszVals[], LPCTSTR lpszDefVal)
{
	if (NULL == lpszVals)
		return ERROR_SUCCESS;

	for (ULONG	i=0; ; i++)
	{
		LPCTSTR	lpszCVal=lpszVals[i];
		if (NULL == lpszCVal)
			break;

		int	nItem=lBox.AddString(lpszCVal);
		if ((LB_ERR == nItem) || (LB_ERRSPACE == nItem))
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) add \"%s\" !!!\n"), nItem, lpszCVal);
			continue;
		}

		int	nDErr=lBox.SetItemData(nItem, (DWORD) lpszCVal);
		if (LB_ERR == nDErr)
			TRACE(_T("\tPopulateChoicesList - cannot (%d) set \"%s\" data !!!\n"), nDErr, lpszCVal);
	}

	if ((lpszDefVal != NULL) && (*lpszDefVal != _T('\0')))
	{
		int	nSel=lBox.SelectString((-1), lpszDefVal);
		if (LB_ERR == nSel)
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) select \"%s\" !!!\n"), nSel, lpszDefVal);
			return ERROR_SECTOR_NOT_FOUND;
		}
	}

	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////
