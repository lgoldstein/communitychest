#include <mfc/mfcutl.h>

/*---------------------------------------------------------------------------*/

static const STLXL priVals[]={
	{	_T("Highest"),	THREAD_PRIORITY_HIGHEST			},
	{	_T("High"),		THREAD_PRIORITY_ABOVE_NORMAL	},
	{	_T("Normal"),	THREAD_PRIORITY_NORMAL			},
	{	_T("Low"),		THREAD_PRIORITY_BELOW_NORMAL	},
	{	_T("Lowest"),	THREAD_PRIORITY_LOWEST			},
	{	NULL,				(-1)									}	// mark end of list
};

/////////////////////////////////////////////////////////////////////////////

// places all available threads priorities in the combo-box
HRESULT PopulatePriosList (CComboBox& cBox, const int nDefPrio)
{
	return PopulateChoicesList(cBox, priVals, (DWORD) nDefPrio);
}

HRESULT PopulatePriosList (CListBox& lBox, const int nDefPrio)
{
	return PopulateChoicesList(lBox, priVals, (DWORD) nDefPrio);
}

/////////////////////////////////////////////////////////////////////////////

// returns (-1) if fails
DWORD GetSelectedItemData (const CComboBox& cBox)
{
	int	nSel=cBox.GetCurSel();

	if ((CB_ERR == nSel) || (nSel < 0) || (nSel >= cBox.GetCount()))
		return ((DWORD) (-1));

	return cBox.GetItemData(nSel);
}

/////////////////////////////////////////////////////////////////////////////

// Note: returns ERROR_FILE_NOT_FOUND if default value not found in list
HRESULT SetDefaultSel (CComboBox& cb, const DWORD defVal)
{
	int	nCount=cb.GetCount();
	for (int nItem=0; nItem < nCount; nItem++)
	{
		DWORD	dwData=cb.GetItemData(nItem);
		if (defVal == dwData)
		{
			int	nRes=cb.SetCurSel(nItem);
			if (CB_ERR == nRes)
				return ERROR_OPERATION_ABORTED;
			return ERROR_SUCCESS;
		}
	}

	return ERROR_FILE_NOT_FOUND;
}

/////////////////////////////////////////////////////////////////////////////

int AddCBoxChoice (CComboBox& cBox, LPCTSTR lpszText, const DWORD dwVal)
{
	int	nItem=cBox.AddString(lpszText);
	if ((CB_ERR == nItem) || (CB_ERRSPACE == nItem))
	{
		TRACE(_T("\tAddCBoxChoice(%s/0x%08x) - cannot insert !!!\n"), lpszText, dwVal);
		return nItem;
	}

	int	nRes=cBox.SetItemData(nItem, dwVal);
	if (CB_ERR == nRes)
	{
		TRACE(_T("\tAddCBoxChoice(%s/0x%08x) - cannot set data !!!\n"), lpszText, dwVal);
		return nRes;
	}

	return nItem;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateChoicesList (CComboBox&	cBox,
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

		const int	nItem=AddCBoxChoice(cBox, xlr.lpszStr, xlr.dwSelData);
	}

	SetDefaultSel(cBox, dwDefData);
	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateChoicesList (CComboBox&	cBox,
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

		const int	nItem=AddCBoxChoice(cBox, strTgName, plx.dwSelData);
	}

	SetDefaultSel(cBox, dwDefData);
	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

// NULL terminate list of strings (associated data is the string pointer itself)
HRESULT PopulateChoicesList (CComboBox& cBox, LPCTSTR lpszVals[], LPCTSTR lpszDefVal)
{
	if (NULL == lpszVals)
		return ERROR_SUCCESS;

	for (ULONG	i=0; ; i++)
	{
		LPCTSTR	lpszCVal=lpszVals[i];
		if (NULL == lpszCVal)
			break;

		const int	nItem=AddCBoxChoice(cBox, lpszCVal, (DWORD) lpszCVal);
	}

	if (!IsEmptyStr(lpszDefVal))
	{
		int	nSel=cBox.SelectString((-1), lpszDefVal);
		if (CB_ERR == nSel)
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) select \"%s\" !!!\n"), nSel, lpszDefVal);
			return ERROR_SECTOR_NOT_FOUND;
		}
	}

	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateChoicesList (CComboBox& cBox, const CStr2PtrMapper& cm, LPCTSTR lpszDefVal)
{
	CStr2PtrMapEnum	cme(cm);
	LPCTSTR				lpszKey=NULL;
	LPVOID				pVal=NULL;
	for (HRESULT	hr=cme.GetFirst(lpszKey, pVal); S_OK == hr; hr=cme.GetNext(lpszKey, pVal))
	{
		const int	nItem=AddCBoxChoice(cBox, lpszKey, (DWORD) pVal);
	}

	if (!IsEmptyStr(lpszDefVal))
	{
		int	nSel=cBox.SelectString((-1), lpszDefVal);
		if (CB_ERR == nSel)
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) select \"%s\" !!!\n"), nSel, lpszDefVal);
			return ERROR_SECTOR_NOT_FOUND;
		}
	}

	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateChoicesList (CComboBox& cBox, const CStr2StrMapper& cm, LPCTSTR lpszDefVal)
{
	CStr2StrMapEnum	cme(cm);
	LPCTSTR				lpszKey=NULL, lpszVal=NULL;
	for (HRESULT	hr=cme.GetFirst(lpszKey, lpszVal); S_OK == hr; hr=cme.GetNext(lpszKey, lpszVal))
	{
		const int	nItem=AddCBoxChoice(cBox, lpszKey, (DWORD) lpszVal);
	}

	if (!IsEmptyStr(lpszDefVal))
	{
		int	nSel=cBox.SelectString((-1), lpszDefVal);
		if (CB_ERR == nSel)
		{
			TRACE(_T("\tPopulateChoicesList - cannot (%d) select \"%s\" !!!\n"), nSel, lpszDefVal);
			return ERROR_SECTOR_NOT_FOUND;
		}
	}

	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////

// returns item number (or < 0 if failed)
int AddExCBoxChoice (CComboBoxEx&			cBox,
							LPCTSTR					lpszValue,	// may be NULL/empty if icon specified
							const int				nIconID,		// can be (-1) if no icon
							const IMGLISTASSOC	imga[],		// must be initialized via "CreateImgList" (if nIconID != (-1))
							const DWORD				dwData)
{
	COMBOBOXEXITEM	cbi={ 0 };
	cbi.mask = CBEIF_LPARAM;
	cbi.iItem = cBox.GetCount();
	cbi.lParam = (LPARAM) dwData;

	if (!IsEmptyStr(lpszValue))
	{
		cbi.mask |= CBEIF_TEXT;
		cbi.pszText = (LPTSTR) lpszValue;
	}
	else
	{
		if (nIconID <= 0)
		{
			TRACE(_T("\tAddExCBoxChoice - no icon and no text either !!!\n"));
			return (-1);
		}
	}

	if (nIconID > 0)
	{
		if ((cbi.iImage=GetAssocImageIndex(nIconID, imga)) >= 0)
		{
			cbi.iSelectedImage = cbi.iImage;
			cbi.mask |= (CBEIF_IMAGE | CBEIF_SELECTEDIMAGE);
		}
		else
			TRACE(_T("\tAddExCBoxChoice(%s) - icon ID=%d not found !!!\n"), GetSafeStrPtr(lpszValue), nIconID);
	}

	return cBox.InsertItem(&cbi);
}

/////////////////////////////////////////////////////////////////////////////

HRESULT PopulateExtendedChoicesList (CComboBoxEx&			cBox,		// must have the matching image list set
												 const IMGLISTASSOC	imga[],	// must be initialized via "CreateImgList"
												 const XTACL			xtas[],	// last item must have a NULL string value AND (-1) icon ID
												 const DWORD			dwDefData)
{
	if (NULL == xtas)
		return ERROR_BAD_ARGUMENTS;

	for (ULONG	i=0; ; i++)
	{
		const XTACL&	xa=xtas[i];
		if (IsEmptyStr(xa.lpszValue) && (xa.nIconID <= 0))
			break;

		const int	nItem=AddExCBoxChoice(cBox, xa, imga);
		if (nItem < 0)
			TRACE(_T("\tPopulateExtendedChoicesList - cannot (%d) add value=%s !!!\n"), nItem, xa.lpszValue);
	}

	SetDefaultSel(cBox, dwDefData);
	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////

HBRUSH CSuperComboBox::OnCtlColor (CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	if (nCtlColor == CTLCOLOR_EDIT)
   {
		CEdit&	edt=GetEditControl();
      //[ASCII 160][ASCII 160][ASCII 160]Edit control
      if (edt.GetSafeHwnd() == NULL)
		{
			ASSERT(pWnd != NULL);
         VERIFY(edt.SubclassWindow(pWnd->GetSafeHwnd()));
		}
   }
   else if (nCtlColor == CTLCOLOR_LISTBOX)
   {
      CListBox&	lb=GetListBoxControl();
      if (lb.GetSafeHwnd() == NULL)
		{
			ASSERT(pWnd != NULL);
         VERIFY(lb.SubclassWindow(pWnd->GetSafeHwnd()));
		}
   }

   return CComboBox::OnCtlColor(pDC, pWnd, nCtlColor);
}

/*--------------------------------------------------------------------------*/

void CSuperComboBox::OnDestroy ()
{
	CEdit&	edt=GetEditControl();
   if (edt.GetSafeHwnd() != NULL)
      edt.UnsubclassWindow();

   CListBox&	lb=GetListBoxControl();
   if (lb.GetSafeHwnd() != NULL)
      lb.UnsubclassWindow();

   CComboBox::OnDestroy();
}

/////////////////////////////////////////////////////////////////////////////
