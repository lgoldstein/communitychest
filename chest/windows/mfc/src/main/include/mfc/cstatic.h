#ifndef _MFC_CSTATIC__H
#define _MFC_CSTATIC__H

/*---------------------------------------------------------------------------*/

#include <mfc/mfcutlbase.h>

/*---------------------------------------------------------------------------*/

class CMFCUtlColorStatic : public CStatic
{
private:
	COLORREF m_crTextColor;
	COLORREF m_crBkColor;
	CBrush	m_brBkgnd;

// Construction
public:
	CMFCUtlColorStatic();

// Attributes
public:

// Operations
public:

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMFCUtlColorStatic)
	//}}AFX_VIRTUAL

// Implementation
public:
	virtual ~CMFCUtlColorStatic();

	BOOL SetTextColor (const COLORREF crTextColor = 0xffffffff);
	COLORREF GetTextColor() const { return m_crTextColor; }

	BOOL SetBkColor (const COLORREF crBkColor = 0xffffffff);
	COLORREF GetBkColor() const { return m_crBkColor; }
	HBRUSH GetBkBrush () const { return m_brBkgnd; }

	// Generated message map functions
protected:
	//{{AFX_MSG(CMFCUtlColorStatic)
	afx_msg HBRUSH CtlColor(CDC* pDC, UINT nCtlColor);
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Developer Studio will insert additional declarations immediately before the previous line.

#endif 
