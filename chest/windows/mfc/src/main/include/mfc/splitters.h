// CMFCUtlSplitterBar.h : header file
//
/////////////////////////////////////////////////////////////////////////////
#ifndef __SPLITTER_BAR_H__
#define __SPLITTER_BAR_H__

#include <mfc/mfcutlbase.h>

#if _MSC_VER >= 1000
#pragma once
#endif // _MSC_VER >= 1000

/////////////////////////////////////////////////////////////////////////////
// CMFCUtlSplitterBar

class CMFCUtlSplitterBar : public CWnd
{
	DECLARE_DYNAMIC(CMFCUtlSplitterBar)

private:
	CRect	m_rectSplitter;
	CRect	m_rectDragPrev;
	CRect	m_rectDragCurt;
	BOOL	m_bDragging;

	CWnd	*m_pwndLeftPane;	//left pane window
	CWnd	*m_pwndRightPane;	//right pane window

	int		m_cxLeftMost;		//left most, relative to parent window
	int		m_cxRightMost;		//right most, relative to parent window

	BOOL	m_bHorizontal;

	void MovePanes ();

	enum DRAGFLAG { DRAG_ENTER=0, DRAG_EXIT=1, DRAGGING=2 };

	void DrawDraggingBar (CPoint point,DRAGFLAG df=DRAGGING);

// Construction
public:
	CMFCUtlSplitterBar();
	BOOL Create (CWnd		*pParentWnd,
					 DWORD	dwStyle=WS_CHILD|WS_BORDER|WS_DLGFRAME|WS_VISIBLE,
					 LPRECT	pRect=NULL,
					 UINT		nID=999,
					 BOOL		bHorizontal=FALSE);

// Attributes
public:
	BOOL IsCursorOverSplitter( const CPoint& ptCursor );

// Operations
public:
	void SetPanes (CWnd& wndLeftPane, CWnd& wndRightPane);

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMFCUtlSplitterBar)
	//}}AFX_VIRTUAL

// Implementation
public:
	virtual ~CMFCUtlSplitterBar();

	// Generated message map functions
protected:
	//{{AFX_MSG(CMFCUtlSplitterBar)
	afx_msg void OnPaint();
	afx_msg UINT OnNcHitTest(CPoint point);
	afx_msg int  OnCreate(LPCREATESTRUCT lpCreateStruct);
	afx_msg BOOL OnSetCursor(CWnd* pWnd, UINT nHitTest, UINT message);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Developer Studio will insert additional declarations immediately before the previous line.

#endif 
