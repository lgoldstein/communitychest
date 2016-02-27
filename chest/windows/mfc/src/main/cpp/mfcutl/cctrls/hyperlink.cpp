#include <mfc/cctrls.h>
#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////
// HyperLink.cpp : implementation file
//
// HyperLink static control.
//
// Copyright (C) 1997, 1998 Giancarlo Iovino (giancarlo@saria.com)
// All rights reserved. May not be sold for profit.
//
// This code is based on CHyperlinkCtrl by Chris Maunder.
// "Default hand cursor" from Paul DiLascia's Jan 1998 MSJ article.

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

#define TOOLTIP_ID 1

#define SETBITS(dw, bits)		(dw |= bits)
#define CLEARBITS(dw, bits)	(dw &= ~(bits))
#define BITSET(dw, bit)			(((dw) & (bit)) != 0L)

/////////////////////////////////////////////////////////////////////////////
// CHyperlinkCtrl

const DWORD CHyperlinkCtrl::StyleUnderline			= 0x00000001;		// Underline bit
const DWORD CHyperlinkCtrl::StyleUseHover				= 0x00000002;		// Hand over coloring bit
const DWORD CHyperlinkCtrl::StyleAutoSize	  			= 0x00000004;		// Auto size bit
//const DWORD CHyperlinkCtrl::StyleDownClick			= 0x00000008;		// Down click mode bit
const DWORD CHyperlinkCtrl::StyleGetFocusOnClick	= 0x00000010;		// Get focus on click bit
const DWORD CHyperlinkCtrl::StyleNoHandCursor		= 0x00000020;		// No hand cursor bit
const DWORD CHyperlinkCtrl::StyleNoActiveColor		= 0x00000040;		// No active color bit

/*--------------------------------------------------------------------------*/

// default values
HYPERLINKCOLORS CHyperlinkCtrl::gm_Colors={
	/* link */		RGB(0, 0, 255),	// Blue
	/* active */	RGB(0, 128, 128),	// Dark cyan
	/* visited */	RGB(128, 0, 128),	// Purple
	/* hover */		RGB(255, 0, 0	)	// Red
};
HCURSOR	CHyperlinkCtrl::gm_hLinkCursor		= NULL;				// No cursor

/*--------------------------------------------------------------------------*/

CHyperlinkCtrl::CHyperlinkCtrl()
	: CStatic(), m_Font(), m_ToolTip(),

	  // Set default styles
	  m_dwStyle(StyleUnderline|StyleAutoSize|StyleGetFocusOnClick),

	  // Set default colors
	  m_Colors(gm_Colors),

	  m_bOverControl(FALSE),	// Cursor not yet over control
	  m_bVisited(FALSE),			// Link has not been visited yet
	  m_bLinkActive(FALSE),		// Control doesn't own the focus yet

	  m_hLinkCursor(NULL)
{
	m_strURL.Empty();				// Set URL to an empty string		

}

CHyperlinkCtrl::~CHyperlinkCtrl()
{
    m_Font.DeleteObject();
}

/*--------------------------------------------------------------------------*/

IMPLEMENT_DYNAMIC(CHyperlinkCtrl, CStatic)

BEGIN_MESSAGE_MAP(CHyperlinkCtrl, CStatic)
    //{{AFX_MSG_MAP(CHyperlinkCtrl)
    ON_WM_CTLCOLOR_REFLECT()
    ON_WM_SETCURSOR()
    ON_WM_MOUSEMOVE()
	 ON_WM_SETFOCUS()
	 ON_WM_KILLFOCUS()
	 ON_WM_NCHITTEST()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CHyperlinkCtrl message handlers

BOOL CHyperlinkCtrl::PreTranslateMessage(MSG* pMsg) 
{
    m_ToolTip.RelayEvent(pMsg);
    return CStatic::PreTranslateMessage(pMsg);
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::PreSubclassWindow() 
{		    
	// If the URL string is empty try to set it to the window text
	if (m_strURL.IsEmpty())
		GetWindowText(m_strURL);

	// Check that the window text isn't empty.
	// If it is, set it as URL string.
	CString strWndText;
	GetWindowText(strWndText);

	// Set the URL string as the window text
	if (strWndText.IsEmpty() && (!m_strURL.IsEmpty()))
		CStatic::SetWindowText(m_strURL);

	// Get the current window font	
	CFont	*pFont = GetFont();	
	if (pFont != NULL)
	{
		LOGFONT lf={ 0 };
		pFont->GetLogFont(&lf);
		lf.lfUnderline = BITSET(m_dwStyle, StyleUnderline);
		if (m_Font.CreateFontIndirect(&lf))
			CStatic::SetFont(&m_Font);
		else
			ASSERT(FALSE);

		// Adjust window size to fit URL if necessary
		AdjustWindow();
	}
	else
	{
		// if GetFont() returns NULL then probably the static
		// control is not of a text type: it's better to set
		// auto-resizing off
		CLEARBITS(m_dwStyle,StyleAutoSize);
	}
	
	if ((NULL == m_hLinkCursor) && (!BITSET(m_dwStyle,StyleNoHandCursor)))
	{
		VERIFY(S_OK == RetrieveDefaultCursor());
		m_hLinkCursor = gm_hLinkCursor;
	}

    // Create the tooltip
    CRect rect; 
    GetClientRect(rect);
    VERIFY(m_ToolTip.Create(this));
    VERIFY(m_ToolTip.AddTool(this, m_strURL, rect, TOOLTIP_ID));

    CStatic::PreSubclassWindow();
}

/*--------------------------------------------------------------------------*/

// Handler for WM_CTLCOLOR reflected message (see message map)
HBRUSH CHyperlinkCtrl::CtlColor(CDC* pDC, UINT nCtlColor) 
{		
	ASSERT(nCtlColor == CTLCOLOR_STATIC);
	ASSERT(pDC != NULL);

	COLORREF	prevColor=(-1);

	if (m_bOverControl && BITSET(m_dwStyle,StyleUseHover))
		prevColor = pDC->SetTextColor(m_Colors.crHover);
	else if (!BITSET(m_dwStyle,StyleNoActiveColor) && m_bLinkActive)
		prevColor = pDC->SetTextColor(m_Colors.crActive);
	else if (m_bVisited)
		prevColor = pDC->SetTextColor(m_Colors.crVisited);
	else
		prevColor = pDC->SetTextColor(m_Colors.crLink);

	// Set transparent drawing mode
	const int	nPrevMode=pDC->SetBkMode(TRANSPARENT);

	return (HBRUSH) GetStockObject(NULL_BRUSH);
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::OnMouseMove (UINT nFlags, CPoint point) 
{

	if (m_bOverControl)        // Cursor currently over control
	{
		CRect rect;
		GetClientRect(rect);

		if (!rect.PtInRect(point))
		{
			m_bOverControl = FALSE;
			ReleaseCapture();
			Invalidate();						
			return;
		}			
	}
	else                      // Cursor has left control area
	{
		m_bOverControl = TRUE;
		Invalidate();		
		SetCapture();		
	}
}

//////////////////////////////////////////////////////////////////////////
// "Normally, a static control does not get mouse events unless it has
// SS_NOTIFY. This achieves the same effect as SS_NOTIFY, but it's fewer
// lines of code and more reliable than turning on SS_NOTIFY in OnCtlColor
// because Windows doesn't send WM_CTLCOLOR to bitmap static controls."
// (Paul DiLascia)
UINT CHyperlinkCtrl::OnNcHitTest (CPoint /*point*/) 
{
	return HTCLIENT;	
}

/*--------------------------------------------------------------------------*/

BOOL CHyperlinkCtrl::OnSetCursor (CWnd* /*pWnd*/, UINT /*nHitTest*/, UINT /*message*/) 
{	
	if (m_hLinkCursor != NULL)
	{
		::SetCursor(m_hLinkCursor);
		return TRUE;
	}

	return FALSE;
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::OnSetFocus (CWnd* /*pOldWnd*/) 
{
	m_bLinkActive = TRUE;
	Invalidate();							// Repaint to set the focus
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::OnKillFocus(CWnd* /*pNewWnd*/) 
{	
	// Assume that control lost focus = mouse out
	// this avoid troubles with the Hover color
	m_bOverControl = FALSE;
	m_bLinkActive = FALSE;
	Invalidate();							// Repaint to unset the focus
}

/////////////////////////////////////////////////////////////////////////////
// CHyperlinkCtrl operations

void CHyperlinkCtrl::SetColors (const COLORREF crLinkColor,
										  const COLORREF crActiveColor,
										  const COLORREF crVisitedColor,
										  const COLORREF crHoverColor /* = -1 */) 
{
	HYPERLINKCOLORS	lColors={ 0 };

	lColors.crLink = crLinkColor;
	lColors.crActive = crActiveColor;
	lColors.crVisited = crVisitedColor;	

	if (crHoverColor == -1)
		lColors.crHover = ::GetSysColor(COLOR_HIGHLIGHT);
	else
		lColors.crHover = crHoverColor;

	SetColors(lColors);
}

void CHyperlinkCtrl::SetDefaultColors (const COLORREF crLinkColor,
													const COLORREF crActiveColor,
													const COLORREF crVisitedColor,
													const COLORREF crHoverColor /* = -1 */) 
{
	HYPERLINKCOLORS	lColors={ 0 };

	lColors.crLink = crLinkColor;
	lColors.crActive = crActiveColor;
	lColors.crVisited = crVisitedColor;	

	if (crHoverColor == -1)
		lColors.crHover = ::GetSysColor(COLOR_HIGHLIGHT);
	else
		lColors.crHover = crHoverColor;

	SetDefaultColors(lColors);
}

/*--------------------------------------------------------------------------*/

BOOL CHyperlinkCtrl:: ModifyLinkStyle (const DWORD dwRemove, const DWORD dwAdd, const BOOL bApply /* =TRUE */)
{
	// Remove old styles and set the new ones
	CLEARBITS(m_dwStyle, dwRemove);
	SETBITS(m_dwStyle, dwAdd);
		
	if (bApply && ::IsWindow(GetSafeHwnd()))
	{
		// If possible, APPLY the new styles on the fly
		if (BITSET(dwAdd,StyleUnderline) || BITSET(dwRemove,StyleUnderline))
			SwitchUnderline();		
		if (BITSET(dwAdd,StyleAutoSize))
			AdjustWindow();		
		if (BITSET(dwRemove,StyleUseHover))
			Invalidate();
	}

	return TRUE;
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::SetURL (LPCTSTR lpszURL)
{
	m_strURL = GetSafeStrPtr(lpszURL);

	if (::IsWindow(GetSafeHwnd()))
	{
		ShowWindow(SW_HIDE);
		AdjustWindow();
		m_ToolTip.UpdateTipText(m_strURL, this, TOOLTIP_ID);
		ShowWindow(SW_SHOW);
	}
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::SetWindowText (LPCTSTR lpszText)
{
	if (::IsWindow(GetSafeHwnd()))
	{
		// Set the window text and adjust its size while the window
		// is kept hidden in order to allow dynamic modification
		ShowWindow(SW_HIDE);				// Hide window

		// Call the base class SetWindowText()
		CStatic::SetWindowText(GetSafeStrPtr(lpszText));

		// Resize the control if necessary
		AdjustWindow();
		ShowWindow(SW_SHOW);				// Show window
	}
}

/*--------------------------------------------------------------------------*/

void CHyperlinkCtrl::SetFont(CFont* pFont)
{
	ASSERT(::IsWindow(GetSafeHwnd()));
	ASSERT(pFont != NULL);

	// Set the window font and adjust its size while the window
	// is kept hidden in order to allow dynamic modification
	ShowWindow(SW_HIDE);

	// Create the new font
	LOGFONT lf={ 0 };
	pFont->GetLogFont(&lf);
	m_Font.DeleteObject();
	VERIFY(m_Font.CreateFontIndirect(&lf));

	// Call the base class SetFont()
	CStatic::SetFont(&m_Font);

	// Resize the control if necessary
	AdjustWindow();
	ShowWindow(SW_SHOW);
}

/*--------------------------------------------------------------------------*/

// Function to set underline on/off
void CHyperlinkCtrl::SwitchUnderline()
{	
	CFont	*pFont=GetFont();
	if (pFont != NULL)
	{
		LOGFONT lf={ 0 };
		pFont->GetLogFont(&lf);		
		lf.lfUnderline = BITSET(m_dwStyle,StyleUnderline);

		m_Font.DeleteObject();
		m_Font.CreateFontIndirect(&lf);

		SetFont(&m_Font);					
	}
}

/*--------------------------------------------------------------------------*/

// Move and resize the window so that its client area has the same size
// as the hyperlink text. This prevents the hyperlink cursor being active
// when it is not over the text.
void CHyperlinkCtrl::AdjustWindow()
{	
	ASSERT(::IsWindow(GetSafeHwnd()));
    
	if (!BITSET(m_dwStyle,StyleAutoSize)) 
		return;

    // Get the current window rect
    CRect rcWnd;
    GetWindowRect(rcWnd);

	 // For a child CWnd object, window rect is relative to the 
	 // upper-left corner of the parent window’s client area. 
    CWnd	*pParent=GetParent();
    if (pParent != NULL)
		 pParent->ScreenToClient(rcWnd);
	
	// Get the current client rect
	CRect rcClient;
	GetClientRect(rcClient);

	// Calc border size based on window and client rects
	int borderWidth = rcWnd.Width() - rcClient.Width();
	int borderHeight = rcWnd.Height() - rcClient.Height();

    // Get the extent of window text 
    CString strWndText;
    GetWindowText(strWndText);
	
    CDC	*pDC=GetDC();	
	 ASSERT(pDC != NULL);

    CFont	*pOldFont=pDC->SelectObject(&m_Font);
    CSize	Extent=pDC->GetTextExtent(strWndText);
    pDC->SelectObject(pOldFont);
    ReleaseDC(pDC);

    // Get the text justification style
    DWORD dwStyle=GetStyle();

    // Recalc window size and position based on text justification
    if (BITSET(dwStyle, SS_CENTERIMAGE))
		rcWnd.DeflateRect(0, (rcWnd.Height() - Extent.cy) / 2);
    else
		rcWnd.bottom = rcWnd.top + Extent.cy;

    if (BITSET(dwStyle, SS_CENTER))
		rcWnd.DeflateRect((rcWnd.Width() - Extent.cx) / 2, 0);
    else if (BITSET(dwStyle,SS_RIGHT))
		rcWnd.left  = rcWnd.right - Extent.cx;
	else // SS_LEFT
		rcWnd.right = rcWnd.left + Extent.cx;

	// Move and resize the window
	MoveWindow(rcWnd.left, rcWnd.top, rcWnd.Width() + borderWidth, rcWnd.Height() + borderHeight);
}

/////////////////////////////////////////////////////////////////////////////
// CHyperlinkCtrl implementation

// The following function appeared in Paul DiLascia's Jan 1998 
// MSJ articles. It loads a "hand" cursor from "winhlp32.exe"
// resources
HRESULT CHyperlinkCtrl::RetrieveDefaultCursor()
{
	HRESULT	hr=S_OK;

	if (gm_hLinkCursor != NULL)
		return S_OK;

	// Get the windows directory
	TCHAR			szWndDir[MAX_PATH+4]=_T("");
	const UINT	nLen=::GetWindowsDirectory(szWndDir, MAX_PATH);
	if (0 == nLen)
		return (hr=::GetLastError());
	if (nLen >= MAX_PATH)
		return ERROR_BUFFER_OVERFLOW;

	static const TCHAR	szDefCursorAppName[]=_T("winhlp32.exe");
	if ((nLen+::_tcslen(szDefCursorAppName)) > MAX_PATH)
		return ERROR_OUTOFMEMORY;
	::_tcscat(szWndDir, _T("\\"));
	::_tcscat(szWndDir, szDefCursorAppName);

	// This retrieves cursor #106 from winhlp32.exe, which is a hand pointer
	HMODULE hModule=::LoadLibrary(szWndDir);
	if (NULL == hModule)
		return (hr=::GetLastError());

	HCURSOR hHandCursor=::LoadCursor(hModule, MAKEINTRESOURCE(106));
	if (hHandCursor != NULL)
	{
		if (NULL == (gm_hLinkCursor=CopyCursor(hHandCursor)))
			hr = ::GetLastError();
	}
	else
		hr = ::GetLastError();

	if (!::FreeLibrary(hModule))
		hr = ::GetLastError();

	return hr;
}

/////////////////////////////////////////////////////////////////////////////
