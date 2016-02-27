#include <mfc/listctrl.h>
#include <mfc/general.h>
#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CListItemEdit, CEdit)
   ON_WM_CHAR()
	ON_WM_KEYUP()
   ON_WM_KILLFOCUS()
	ON_WM_WINDOWPOSCHANGING()
END_MESSAGE_MAP()

/*--------------------------------------------------------------------------*/

void CListItemEdit::OnKillFocus (CWnd* pNewWnd)
{
	// we need this event in order to restore the original text
	SetWindowText(m_orgText);
	CEdit::OnKillFocus(pNewWnd);
}

/*--------------------------------------------------------------------------*/

// returns TRUE if this is an ESC key (and was handled)
BOOL CListItemEdit::HandleEscapeKey (const UINT nChar)
{
	if (VK_ESCAPE != nChar)
		return FALSE;

	SetWindowText(m_orgText);

	CWnd	*pParent=GetParent();
	if (pParent != NULL)
		pParent->SetFocus();

	return TRUE;
}

/*--------------------------------------------------------------------------*/

void CListItemEdit::OnChar (UINT nChar, UINT nRepCnt, UINT nFlags) 
{
	if (HandleEscapeKey(nChar))
		return;

	CEdit::OnChar (nChar, nRepCnt, nFlags);
}

/*--------------------------------------------------------------------------*/

void CListItemEdit::OnKeyUp (UINT nChar, UINT nRepCnt, UINT nFlags)
{
	if (HandleEscapeKey(nChar))
		return;

	CEdit::OnKeyUp (nChar, nRepCnt, nFlags);
}

/*--------------------------------------------------------------------------*/

void CListItemEdit::OnWindowPosChanging (WINDOWPOS FAR* lpwndpos) 
{
	if (lpwndpos != NULL)
	{
		lpwndpos->x = m_x;
		lpwndpos->y = m_y;
	}

	CEdit::OnWindowPosChanging(lpwndpos);
}

/*--------------------------------------------------------------------------*/

BOOL CListItemEdit::PreTranslateMessage (MSG* pMsg) 
{
	if (IsEscapeKeyDownMsg(pMsg))
	{
		::TranslateMessage (pMsg);
		::DispatchMessage (pMsg);
		return TRUE;		    	// DO NOT process further
	}

	return CEdit::PreTranslateMessage (pMsg);
}

/*--------------------------------------------------------------------------*/

BOOL CListItemEdit::SetItemPos (const int xPos, const int yPos)
{
	if (((m_x=xPos) < 0) || ((m_y=yPos) < 0))
		return FALSE;
	else
		return TRUE;
}

/*--------------------------------------------------------------------------*/

BOOL CListItemEdit::SetItemPos (const CListCtrl& lCtrl, const int nRow, const int nCol)
{
	CRect rcItem;
	if (!((CListCtrl &) lCtrl).GetSubItemRect(nRow, nCol, LVIR_LABEL, rcItem))
		return FALSE;

	return SetItemPos(rcItem.left, rcItem.top-1);
}

/*--------------------------------------------------------------------------*/

BOOL CListItemEdit::AttachItem (const CListCtrl& lCtrl, const int nRow, const int nCol)
{
	// make sure edited item is visible
	if (!(((CListCtrl &) lCtrl).EnsureVisible(nRow, TRUE)))
		return FALSE;

	// get edit item window
	CEdit	*pEdt=lCtrl.GetEditControl();
#ifdef _DEBUG
	ASSERT(pEdt != NULL);
#else
	if (NULL == pEdt)
		return FALSE;
#endif

	HWND hWnd=pEdt->GetSafeHwnd();
#ifdef _DEBUG
	ASSERT(hWnd != NULL);
#else
	if (NULL == hWnd)
		return FALSE;
#endif

	// attach it to the edit item control
	if (!SubclassWindow(hWnd))
		return FALSE;

	// set position to overlap the item
	if (!SetItemPos(lCtrl, nRow, nCol))
		return FALSE;

	// set initial text to be same as item data
	m_orgText = lCtrl.GetItemText(nRow, nCol);
	SetWindowText(m_orgText);

	return TRUE;
}

/*--------------------------------------------------------------------------*/

BOOL CListItemEdit::DetachItem (const CListCtrl& lCtrl, const int nRow, const int nCol)
{
	if (NULL == UnsubclassWindow())
		return FALSE;

	m_x = (-1);
	m_y = (-1);
	m_orgText = _T("");

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CListItemEditHandler::BeginEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo, const CPoint& cursorPos)
{
	// check if have previous editing in progress
	if (m_ItemEdit.HaveItemPosition() || IsEditing())
		return FALSE;

	CPoint	posMouse(cursorPos);
   lCtrl.ScreenToClient(&posMouse);
	int	nCol=(-1), nRow=::ListCtrlHitTestEx(lCtrl, posMouse, &nCol);
	if (nRow < 0)
		return FALSE;

	if (!m_ItemEdit.AttachItem(lCtrl, nRow, nCol))
		return FALSE;

	m_nRow = nRow;
	m_nCol = nCol;
	return TRUE;
}

/*--------------------------------------------------------------------------*/

// if returns FALSE, then "*pResult" of the LVN_BEGINLABELEDIT handler must be set to TRUE (and viceversa)
BOOL CListItemEditHandler::BeginEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo)
{
	CPoint posMouse;
   if (!::GetCursorPos(&posMouse))
		return FALSE;

	return BeginEdit(lCtrl, dispInfo, posMouse);
}

/*--------------------------------------------------------------------------*/

// does not set the new item text
BOOL CListItemEditHandler::EndEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo, CString& strNewValue)
{
	strNewValue = _T("");

	// make sure we have editing in progress
	if ((!IsEditing()) || (!m_ItemEdit.HaveItemPosition()))
		return FALSE;

	const LV_ITEM&	lvItem=dispInfo.item;
	const int		nItem=lvItem.iItem;
	// ensure we are talking about the same row
	if (nItem != m_nRow)
		return FALSE;

	m_ItemEdit.GetWindowText(strNewValue);

	if (!m_ItemEdit.DetachItem(lCtrl, m_nRow, m_nCol))
		return FALSE;

	m_nRow = (-1);
	m_nCol = (-1);

	return TRUE;
}

/*--------------------------------------------------------------------------*/

// also sets the edited item to the new text
BOOL CListItemEditHandler::EndEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo)
{
	// have to remember them since the call to the other "EndEdit" resets them
	const int	nRow=m_nRow, nCol=m_nCol;
	CString		strValue;
	if (!EndEdit(lCtrl, dispInfo, strValue))
		return FALSE;

	LPCTSTR	lpszNewText=dispInfo.item.pszText;
	if (!IsEmptyStr(lpszNewText))
		lCtrl.SetItemText(nRow, nCol, lpszNewText);

	lCtrl.SetItemState(nRow, 0, LVNI_FOCUSED | LVNI_SELECTED);

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////
