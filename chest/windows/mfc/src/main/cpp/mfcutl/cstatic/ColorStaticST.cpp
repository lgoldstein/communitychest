// ColorStaticST.cpp : implementation file
//

#include <mfc/cstatic.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CMFCUtlColorStatic

CMFCUtlColorStatic::CMFCUtlColorStatic ()
{
	// Set default foreground text
	m_crTextColor = ::GetSysColor(COLOR_BTNTEXT);

	// Set default background text
	m_crBkColor = ::GetSysColor(COLOR_BTNFACE);

	// Set default background brush
	m_brBkgnd.CreateSolidBrush(m_crBkColor);
}

/////////////////////////////////////////////////////////////////////////////

CMFCUtlColorStatic::~CMFCUtlColorStatic()
{
} // End of ~CMFCUtlColorStatic

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CMFCUtlColorStatic, CStatic)
	//{{AFX_MSG_MAP(CMFCUtlColorStatic)
	ON_WM_CTLCOLOR_REFLECT()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CMFCUtlColorStatic message handlers

HBRUSH CMFCUtlColorStatic::CtlColor (CDC* pDC, UINT nCtlColor) 
{
#ifdef _DEBUG
	ASSERT(pDC != NULL);
#else
	if (NULL == pDC)
		return NULL;
#endif

	// Set foreground color
	pDC->SetTextColor(m_crTextColor);

	// Set background color & brush
	pDC->SetBkColor(m_crBkColor);

	// Return a non-NULL brush if the parent's handler should not be called
	return (HBRUSH)m_brBkgnd;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CMFCUtlColorStatic::SetTextColor (const COLORREF crTextColor)
{
	// Set new foreground color
	if (crTextColor != 0xffffffff)
		m_crTextColor = crTextColor;
	else // Set default foreground color
		m_crTextColor = ::GetSysColor(COLOR_BTNTEXT);

	// Repaint control
	Invalidate();
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CMFCUtlColorStatic::SetBkColor (const COLORREF crBkColor)
{
	// Set new background color
	if (crBkColor != 0xffffffff)
		m_crBkColor = crBkColor;
	else // Set default background color
		m_crBkColor = ::GetSysColor(COLOR_BTNFACE);

   if (!m_brBkgnd.DeleteObject())
		return FALSE;

   if (!m_brBkgnd.CreateSolidBrush(m_crBkColor))
		return FALSE;

	// Repaint control
	Invalidate();

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////
