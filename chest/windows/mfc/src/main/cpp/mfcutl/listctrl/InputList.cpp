#include <mfc/general.h>
#include <mfc/listctrl.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CInputList

CInputList::CInputList()
	: CSuperComboBox()
	, m_maxHistory(100)
	, m_valsMap()
{
	VERIFY(S_OK == m_valsMap.InitMap((UINT32) m_maxHistory, TRUE));
}

CInputList::~CInputList()
{
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CInputList, CComboBox)
	//{{AFX_MSG_MAP(CInputList)
	ON_WM_CTLCOLOR()
	ON_WM_DESTROY()
	ON_CONTROL_REFLECT(CBN_SELCHANGE, OnSelChange)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CInputList message handlers

HBRUSH CInputList::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor) 
{
	return CSuperComboBox::OnCtlColor(pDC, pWnd, nCtlColor);
}

void CInputList::OnDestroy() 
{
	CSuperComboBox::OnDestroy();
}

/////////////////////////////////////////////////////////////////////////////

void CInputList::OnSelChange () 
{
	const int	nSel=GetCurSel();
	ASSERT ((CB_ERR != nSel) && (nSel >= 0) && (nSel < GetCount()));

	CString	edtValue;
	GetLBText(nSel, edtValue);
	m_edt.SetWindowText(edtValue);
	TRACE(_T("\tCInputList::OnSelChange(%s)\n"), (LPCTSTR) edtValue);
}

/////////////////////////////////////////////////////////////////////////////

int CInputList::SetMaxHistory (int maxHistory)
{
	if (maxHistory <= 0)
		return CB_ERR;

	const int	curValue=GetMaxHistory();
	if (maxHistory > curValue)
	{
		m_valsMap.Clear();
		HRESULT	hr=m_valsMap.InitMap((UINT32) maxHistory, TRUE);
		ASSERT(S_OK == hr);
		if (hr != S_OK)
			return (hr > 0) ? (0 - hr) : hr;
	}

	m_maxHistory = maxHistory;
	return curValue;
}

/////////////////////////////////////////////////////////////////////////////

int CInputList::InsertString (int nIndex, LPCTSTR lpszItem)
{
	if (IsEmptyStr(lpszItem))
		return CB_ERR;

	int	nCount=GetCount();
	ASSERT(nIndex >= 0);
#ifdef _DEBUG
	if (nCount > 0)
		ASSERT(nIndex < nCount);
#endif

	// trim the history to up to 100 (TODO - allow to change option)
	for (const int	curHist=GetMaxHistory(); nCount > curHist; )
	{
		CString	delText;
		GetLBText(nCount-1, delText);
		LPCTSTR	lpszDelItem=delText;
		HRESULT	hr=m_valsMap.RemoveKey(lpszDelItem);

		TRACE(_T("\tCInputList::InsertString(%s) deleted (map err=0x%08x)\n"), lpszDelItem, hr);
		nCount = DeleteString(nCount - 1);
	}

	LPVOID	pVal=NULL;
	HRESULT	hr=m_valsMap.FindKey(lpszItem, pVal);
	if (S_OK == hr)
	{
		TRACE(_T("\tCInputList::InsertString(%s) skip re-insertion\n"), lpszItem);
		return FindString((-1), lpszItem);
	}

	const int	nItem=CSuperComboBox::InsertString(nIndex, lpszItem);
	if (nItem >= 0)
	{
		if ((hr=m_valsMap.AddKey(lpszItem, (LPVOID) ::time(NULL))) != S_OK)
			return (hr > 0) ? (0 - hr) : hr;
		TRACE(_T("\tCInputList::InsertString(%s) accumulated\n"), lpszItem);
	}

	return nItem;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CInputList::PreTranslateMessage(MSG* pMsg) 
{
	if (::IsKeyDownMsg(pMsg, VK_RETURN))
	{
		CString	edtText;
		m_edt.GetWindowText(edtText);
		VERIFY(UpdateEnteredText(edtText) >= 0);
		m_edt.SetWindowText(_T(""));	// remove from display (AFTER writing it)
	}
	
	return CComboBox::PreTranslateMessage(pMsg);
}

/////////////////////////////////////////////////////////////////////////////
