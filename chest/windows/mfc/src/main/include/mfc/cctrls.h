#ifndef _MFC_CUSTOM_CONTROLS_H
#define _MFC_CUSTOM_CONTROLS_H

/*---------------------------------------------------------------------------*/

#include <mfc/mfcutlbase.h>

/*---------------------------------------------------------------------------*/

// Line(s)Chart control
//
// Written by Yuheng Zhao (yuheng@ministars.com) 
// http://www.ministars.com
// The original idea and part of the code from Ken C. Len's CHistogramCtrl
// http://www.codeguru.com/controls/histogram_control.shtml
// Enhanced by Goldstein Lyor
//
// Copyright (c) 1998.
//
// This code may be used in compiled form in any way you desire. This
// file may be redistributed unmodified by any means PROVIDING it is 
// not sold for profit without the authors written consent, and 
// providing that this notice and the authors name is included. If 
// the source code in  this file is used in any commercial application 
// then a simple email would be nice.
//
// This file is provided "as is" with no expressed or implied warranty.
// The author accepts no liability if it causes any damage whatsoever.
// It's free - so you get what you pay for.
//
//		To create such a control in the resource editor, add a custom control
// with class name "LINE_CHART_CUSTOM_CTRL", style "0x50010000" and extended style "0x0"

class CLineChartItem;
// callback template for automatic calculation of item value
typedef UINT (*LINECHARTITEMPOSCFN)(CLineChartItem *pItem);

class CLineChartItem: public CObject {
	public:
		COLORREF m_colorLine;
		UINT     m_nLower;		// lower bounds
		UINT     m_nUpper;		// upper bounds
	
		UINT     m_nPos;		// current position within bounds
		UINT     m_nOldPos;		// last position within bounds
		DWORD		m_dwItemData;
		LINECHARTITEMPOSCFN	m_lpfnPcfn;

	CLineChartItem()
		: m_nLower(0), m_nUpper(0), m_nPos(0), m_nOldPos(0), m_colorLine(RGB(255,255,255)),
		  m_dwItemData((DWORD) (-1)), m_lpfnPcfn(NULL)
	{
	}

	CLineChartItem (const COLORREF lcolor, const UINT nUpper, const UINT nLower,
						 const DWORD dwItemData=((DWORD) (-1)), LINECHARTITEMPOSCFN lpfnPcfn=NULL)
		: m_colorLine(lcolor), m_nLower(nLower), m_nUpper(nUpper), m_nPos(0), m_nOldPos(0),
		  m_dwItemData(dwItemData), m_lpfnPcfn(lpfnPcfn)
	{
	}

	virtual ~CLineChartItem ()
	{
	}
};

/////////////////////////////////////////////////////////////////////////////
// CLineChartCtrl window

#include <afxtempl.h>

class CLineChartCtrl : public CWnd {

public:
	CLineChartCtrl();
	virtual ~CLineChartCtrl();

	// sets the line at index "nIndex" to the specified position
	// Note: if callback is supplied then "SetPos" has no effect
	virtual UINT SetPos(const int nIndex, const UINT nPos);
	virtual BOOL SetRange (const int nIndex, const UINT nLower, const UINT nUpper, const BOOL fInvalidate=TRUE);

	virtual void InvalidateCtrl();

	// this function must be called to draw the new state
	virtual void Go();

	// returns index of added item (or (-1))
	virtual int Add (const COLORREF lcolor, const UINT nLower=0, const UINT nUpper=100,
						  const DWORD dwItemData=((DWORD) (-1)), LINECHARTITEMPOSCFN lpfnPcfn=NULL);

	// Note: if callback is supplied then "SetPos" has no effect
	virtual BOOL SetItemData (const int nIndex, const DWORD dwItemData, LINECHARTITEMPOSCFN lpfnPcfn=NULL);

	virtual DWORD GetItemData (const int nIndex) const;

	virtual void SetScaleBGColor (const COLORREF bgCol, const BOOL fInvalidate=TRUE)
	{
		m_ScaleBGCol = bgCol;
		if (fInvalidate)
			InvalidateCtrl();
	}

	virtual const COLORREF GetScaleBGColor () const
	{
		return m_ScaleBGCol;
	}

	virtual void SetScaleFGColor (const COLORREF fgCol, const BOOL fInvalidate=TRUE)
	{
		m_ScaleFGCol = fgCol;
		if (fInvalidate)
			InvalidateCtrl();
	}

	virtual const COLORREF GetScaleFGColor () const
	{
		return m_ScaleFGCol;
	}

	virtual int GetItemsCount () const
	{
		return m_items.GetSize();
	}

	virtual const CLineChartItem *GetItem (const int nIndex) const
	{
		return ((nIndex > m_items.GetSize()) ? NULL : m_items.GetAt(nIndex));
	}

	virtual void RemoveAll ();

protected:
	static LONG	lRegisterCount;
	static BOOL RegisterWndClass(HINSTANCE hInstance);

	COLORREF												m_ScaleBGCol;	// scale background color
	COLORREF												m_ScaleFGCol;	// scale foreground color
	CDC													m_MemDC;
	CBitmap												m_Bitmap;
	CTypedPtrArray<CObArray, CLineChartItem*>	m_items;

	virtual BOOL RedrawScale (CClientDC& dc, const CRect& rcClient);
	virtual BOOL RedrawScale ();
	virtual void DrawSpike();

	afx_msg void OnPaint();
	afx_msg void OnSize(UINT nType, int cx, int cy);

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

// based on Ken C. Len's code posted on http://www.codeguru.com
//
//	In order to add such a control follow these steps:
//
//		a. in the resource editor add a picture control, and choose "rectangle" as its type
//
//		b. add a "CHistogramCtrl	m_HistogramCtrl" as a dialog member
//
//		c. in the "OnInitDialog()" add the following code:
//
//				CWnd	*pHistWnd=GetDlgItem(IDC_HISTOGRAM) - or whatever ID you used for the picture control
//
//				CRect rcHist;
//				pHistWnd->GetWindowRect(rcHist);
//				ScreenToClient(rcHist);
//
//				m_HistogramCtrl.Create(WS_VISIBLE | WS_CHILD, rect, this, IDC_SOME_NEW_RESOURCE_ID);
//
//			Note: in order to make the histogram invisible you have to call "ShowWindow(SW_SHOW/SW_HIDE)" for
//					the picture control AND the histogram
class CHistogramCtrl : public CWnd
{
public:
	// Construction
	CHistogramCtrl();
	virtual ~CHistogramCtrl();

	virtual UINT SetPos (const UINT nPos, const BOOL fRedraw=TRUE);
	virtual UINT GetPos () const
	{
		return m_nPos;
	}

	virtual BOOL SetRange (const UINT nLower, const UINT nUpper, const BOOL fInvalidate=TRUE);
	virtual void GetRange (UINT& nLower, UINT& nUpper) const
	{
		nLower = m_nLower;
		nUpper = m_nUpper;
	}

	virtual void InvalidateCtrl();

	virtual BOOL Create(DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID, CCreateContext* pContext = NULL);

	virtual void SetScaleBGColor (const COLORREF bgCol, const BOOL fInvalidate=TRUE)
	{
		m_ScaleBGCol = bgCol;
		if (fInvalidate)
			InvalidateCtrl();
	}

	virtual const COLORREF GetScaleBGColor () const
	{
		return m_ScaleBGCol;
	}

	virtual void SetScaleFGColor (const COLORREF fgCol, const BOOL fInvalidate=TRUE)
	{
		m_ScaleFGCol = fgCol;
		if (fInvalidate)
			InvalidateCtrl();
	}

	virtual const COLORREF GetScaleFGColor () const
	{
		return m_ScaleFGCol;
	}

	virtual void SetSpikeColor (const COLORREF spkCol, const BOOL fInvalidate=TRUE)
	{
		m_SpikeCol = spkCol;
		if (fInvalidate)
			InvalidateCtrl();
	}

	virtual const COLORREF GetSpikeColor () const
	{
		return m_SpikeCol;
	}

protected:
	UINT     m_nLower;	// lower bounds
	UINT     m_nUpper;	// upper bounds
	UINT     m_nPos;		// current position within bounds

	COLORREF	m_ScaleBGCol;
	COLORREF	m_ScaleFGCol;
	COLORREF	m_SpikeCol;
	CDC      m_MemDC;
	CBitmap  m_Bitmap;

	virtual BOOL RedrawScale (CClientDC& dc, const CRect& rcClient);
	virtual BOOL RedrawScale ();
	virtual void DrawSpike();

	afx_msg void OnPaint();
	afx_msg void OnSize(UINT nType, int cx, int cy);

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

// Written by Lyor G.
// Based on Yuheng Zhao's (yuheng@ministars.com) code for the piechart control
//
//		In order to use this control follow these steps:
//
//		a. in the resource editor, add a custom control with class name "ROUND_PROGRESS_CUSTOM_CTRL",
//			style "0x50010000" and extended style "0x0" (which are the defaults anyway).
//
//		b.	in the header file add a member
//
//				CRoundProgressCtrl	m_ProgCtrl;
//
//		c. in the "OnInitDialog()" add the following code
//
//				VERIFY(m_ProgCtrl.SubclassDlgItem(IDC_PROGRESS_CTRL, this));
//																	^^^
//									this should match the assigned resource ID in the resource editor
//
class CRoundProgressCtrl : public CWnd
{
public:
	CRoundProgressCtrl ();
	CRoundProgressCtrl (const COLORREF bgColor, const COLORREF fgColor);
	virtual ~CRoundProgressCtrl ();

	virtual BOOL SetRange (const UINT uMaxValue, const BOOL fRedraw=TRUE);
	virtual UINT GetRange () const
	{
		return m_uMaxValue;
	}

	virtual BOOL SetPos (const UINT uPos, const BOOL fRedraw=TRUE);
	virtual UINT GetPos () const
	{
		return m_uCurValue;
	}
	virtual BOOL OffsetPos (const int nPos, const BOOL fRedraw=TRUE)
	{
		return SetPos(GetPos() + nPos, fRedraw);
	}

	virtual BOOL SetStep (const UINT uStep);
	virtual UINT GetStep () const
	{
		return m_uStepValue;
	}

	// Note: stops when reaches maximum value (or more)
	virtual BOOL StepIt (const BOOL fRedraw=TRUE);

	virtual BOOL SetProgressColors (const COLORREF fgCol, const COLORREF bgCol, const BOOL fRedraw=TRUE);
	virtual void GetProgressColors (COLORREF& fgCol, COLORREF& bgCol) const
	{
		fgCol = m_fgColor;
		bgCol = m_bgColor;
	}

	virtual BOOL SetBGColor (const COLORREF bgCol, const BOOL fRedraw=TRUE)
	{
		return SetProgressColors(m_fgColor, bgCol, fRedraw);
	}
	virtual COLORREF GetBGColor () const
	{
		return m_bgColor;
	}

	virtual BOOL SetFGColor (const COLORREF fgCol, const BOOL fRedraw=TRUE)
	{
		return SetProgressColors(fgCol, m_bgColor, fRedraw);
	}
	virtual COLORREF GetFGColor () const
	{
		return m_fgColor;
	}

	virtual BOOL DrawProgress ();

	// Generated message map functions
protected:
	CRect		m_rcCtrl;
	UINT		m_uMaxValue;
	UINT		m_uCurValue;
	UINT		m_uStepValue;
	COLORREF	m_bgColor;
	COLORREF	m_fgColor;

	static LONG	m_lRefs;
	static BOOL RegisterWndClass (HINSTANCE hInstance);
	static BOOL CheckWndClassRegistration ();

	virtual void RecalcCtrlRect (const CRect& rcClient);
	virtual void RecalcCtrlRect ();

	afx_msg void OnPaint();
	afx_msg void OnSize(UINT nType, int cx, int cy);

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

// Written by matt weagle (mweagle@redrose.net) Copyright (c) 1998.
//
// This code may be used in compiled form in any way you desire. This
// file may be redistributed unmodified by any means PROVIDING it is 
// not sold for profit without the authors written consent, and 
// providing that this notice and the authors name is included. If 
// the source code in  this file is used in any commercial application 
// then a simple email would be nice.
//
// This file is provided "as is" with no expressed or implied warranty.
// The author accepts no liability if it causes any damage whatsoever.
//
// Thanks to Chris Maunder (Chris.Maunder@cbr.clw.csiro.au) for the 
// foregoing disclaimer.
class CGradientProgressCtrl : public CProgressCtrl {
	public:
		CGradientProgressCtrl();

		virtual void SetRange32 (int nLower, int nUpper);
		virtual void SetRange (short nLower, short nUpper)
		{
			SetRange32((int) nLower, (int) nUpper);
		}

		virtual int SetPos(int nPos);
		virtual int SetStep(int nStep);
		virtual int StepIt(void);
		// returns the previous (!) position of the progress bar control.
		virtual int OffsetPos(int nPos);

		// Set Functions
		virtual void SetTextColor (const COLORREF color)	{ m_clrText = color; }
		virtual void SetBkColor (const COLORREF color)		{ m_clrBkGround = color; }
		virtual void SetStartColor (const COLORREF color)	{ m_clrStart = color; }
		virtual void SetEndColor (const COLORREF color)		{ m_clrEnd = color; }

		virtual const COLORREF GetTextColor () const	{ return m_clrText; }
		virtual const COLORREF GetBkColor () const		{ return m_clrBkGround; }
		virtual const COLORREF GetStartColor ()	const	{ return m_clrStart; }
		virtual const COLORREF GetEndColor () const		{ return m_clrEnd; }

		virtual void GetGradientColors (COLORREF& clrStart, COLORREF& clrEnd) const
		{
			clrStart = m_clrStart;
			clrEnd = m_clrEnd;
		}

		virtual void SetGradientColors (const COLORREF clrStart, const COLORREF clrEnd)
		{
			m_clrStart = clrStart;
			m_clrEnd = clrEnd;
		}

		typedef enum {
			PBS_SHOW_NONE=0,
			PBS_SHOW_PERCENT,
			PBS_SHOW_POSITION,
			PBS_BAD_FORMAT
		} PBSTEXTFORMATCASE;

		/*
		 * The text format can as follows, given the formatting case
		 *
		 *		PBS_SHOW_NONE (default) - no text is shown (the "lpszFmt" is ignored and can be NULL/empty)
		 *
		 *		PBS_SHOW_PERCENT - the text may contain a SINGLE '%XXX' formatting value that will be used
		 *				to display the current percentage (e.g. "Completed %d%%"). Note: the caller must add
		 *				the percent sign (represented by '%%')
		 *
		 *		PBS_SHOW_POSITION - the text may contain up to '%XXX' formatting values that will be used
		 *				to display the current value(s) - where the first one is assumed to be the current
		 *				position, and the (optional) second is the maximum value (e.g. "Completed %lu out of %lu").
		 */
		virtual HRESULT SetTextFormat (LPCTSTR lpszFmt, const PBSTEXTFORMATCASE efCase);

		virtual HRESULT HideText ()
		{
			return SetTextFormat(NULL, PBS_SHOW_NONE);
		}

		virtual HRESULT GetTextFormat (CString& strFmt, PBSTEXTFORMATCASE& efCase) const;

		virtual ~CGradientProgressCtrl ();

protected:
	virtual void DrawGradient (CPaintDC *pDC, const RECT &rectClient, const int &nMaxWidth);	

	int		m_nLower, m_nUpper, m_nStep, m_nCurrentPosition;
	COLORREF	m_clrStart, m_clrEnd, m_clrBkGround, m_clrText;
	PBSTEXTFORMATCASE	m_efCase;

	afx_msg void OnPaint();
	afx_msg BOOL OnEraseBkgnd(CDC* pDC);

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////
// CMemDC - memory DC
//
// Author: Keith Rule
// Email:  keithr@europa.com
// Copyright 1996-1997, Keith Rule
//
// You may freely use or modify this code provided this
// Copyright is included in all derived versions.
//
// History - 10/3/97 Fixed scrolling bug.
//                   Added print support.
//           25 feb 98 - fixed minor assertion bug
//
// This class implements a memory Device Context
class CMemDC : public CDC {
	public:
		// constructor sets up the memory DC
		CMemDC (CDC* pDC);

		// Destructor copies the contents of the mem DC to the original DC
		virtual ~CMemDC ();
	
		// Allow usage as a pointer
		virtual CMemDC* operator->() { return this; }
	
		// Allow usage as a pointer
		virtual operator CMemDC*() { return this; }

	protected:
		CMemDC	*m_pThis;
		CBitmap  m_bitmap;		// Offscreen bitmap
		CBitmap* m_pOldBitmap;	// bitmap originally found in CMemDC
		CDC*     m_pDC;			// Saves CDC passed in constructor
		CRect    m_rect;		// Rectangle of drawing area.
		BOOL     m_bMemDC;		// TRUE if CDC really is a Memory DC.
};

/////////////////////////////////////////////////////////////////////////////

//
// based on the code written by Yuheng Zhao (yuheng@ministars.com)
// Copyright (c) 1998 as published in http://www.codeguru.com
//
//	Usage:
//
//		1. Use the MS Visual C++ dialog editor to place a custom control on
//		the dialog, and enter "PIE_CHART_CTRL" as the Class name
//
//		2. Add a CPieChartCtrl class member in the dialog header file - e.g.:
//
//				CPieChartCtrl m_wndChart;
//
//		3. Subclass the class member with the control in OnInitDialog() - e.g.
//
//				// IDC_PIECHART1 is the control ID used when defined the custom control
//				m_wndChart.SubclassDlgItem(IDC_PIECHART1, this);

class CPieChartPiece : public CObject {
	private:
		COLORREF m_colorBack;
		COLORREF m_colorText;
		CBrush	m_brushBack;	
		int		m_nSize;
		CString	m_strInfo;
		DWORD		m_dwData;

	public:
		CPieChartPiece ();

		CPieChartPiece (COLORREF		colorBack,
							 COLORREF		colorText,
							 const int		nSize,
							 LPCTSTR			lpszStrInfo=NULL,
							 const DWORD	dwData=0);

		virtual BOOL SetInfo (COLORREF		colorBack,
									 COLORREF		colorText,
									 const int		nSize,
									 LPCTSTR			lpszStrInfo=NULL,
									 const DWORD	dwData=0);

		virtual void SetSize (const int nSize)
		{
			m_nSize = nSize;
		}

		virtual void SetData (const DWORD dwData)
		{
			m_dwData = dwData;
		}

		virtual const DWORD GetData () const
		{
			return m_dwData;
		}

		virtual const int GetSize () const
		{
			return m_nSize;
		}

		virtual BOOL SetColors (COLORREF clrBack, COLORREF clrText);

		virtual void GetColors (COLORREF& clrBack, COLORREF& clrText) const;

		virtual void SetName (LPCTSTR lpszName /* can be NULL */);

		virtual LPCTSTR GetName () const;

		virtual CBrush& GetBrush ()
		{
			return m_brushBack;
		}

		virtual ~CPieChartPiece ()
		{
			if (m_brushBack.m_hObject != NULL)
				m_brushBack.DeleteObject();
		}
};

typedef CTypedPtrArray <CObArray, CPieChartPiece*> CChartPieceArray;

class CPieChartCtrl : public CWnd {
	private:
		int		m_nLower;	// default = 0
		int		m_nUpper;	// default = 359
		CFont		m_fontInfo;
		COLORREF m_clrDefault;
		COLORREF m_clrLine;
		CRect		m_rectChart;
		BOOL		m_fDrawPercent;	// default = TRUE

		CChartPieceArray m_chartPieces;

		static BOOL RegisterWndClass (HINSTANCE hInstance);

		void RecalcRect();

		void CountPoint (const int nAngle, CPoint& pt, const BOOL bPercent=FALSE);

	public:
		CPieChartCtrl();

		virtual void ShowPercent (const BOOL fShowIt, const BOOL fRedraw=FALSE)
		{
			m_fDrawPercent = fShowIt;
			if (fRedraw)
				InvalidateRect(NULL, FALSE);
		}

		virtual const BOOL IsShowingPercent () const
		{
			return m_fDrawPercent;
		}

		// 1. default is 0-359
		// 2. ensures that "nLower" < "nUpper"
		virtual BOOL SetRange (const int nLower, const int nUpper, const BOOL fRedraw=FALSE);

		virtual void GetRange (int& nLower, int& nUpper) const
		{
			nLower = m_nLower;
			nUpper = m_nUpper;
		}

		virtual void SetColors (COLORREF clrDefault, COLORREF clrLine, const BOOL fRedraw=FALSE);

		virtual void GetColors (COLORREF& clrDefault, COLORREF& clrLine) const
		{
			clrDefault = m_clrDefault;
			clrLine = m_clrLine;
		}

		virtual void Reset(const BOOL fRedraw=TRUE);

		// returns index of added piece (or (-1) if error)
		virtual int AddPiece (COLORREF colorBack, COLORREF colorText, const int nSize, LPCTSTR lpszName=NULL, const DWORD dwData=0, const BOOL fRedraw=TRUE);

		virtual const int GetNumOfPieces () const
		{
			return m_chartPieces.GetSize();
		}

		// returns NULL if illegal index
		virtual CPieChartPiece *GetPiece (const int nIndex) const
		{
			return m_chartPieces.GetAt(nIndex);
		}

		virtual ~CPieChartCtrl();

	protected:
		//{{AFX_MSG(CPieChartCtrl)
		afx_msg void OnPaint();
		afx_msg void OnSize(UINT nType, int cx, int cy);
		//}}AFX_MSG

		DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

// HyperLink static control.
//
// Copyright Giancarlo Iovino, 1997 (giancarlo@saria.com)
// This code is based on CHyperlinkCtrl by Chris Maunder.

// Structure used to get/set hyperlink colors
typedef struct tagHYPERLINKCOLORS {
	COLORREF	crLink;		// Link normal color
	COLORREF	crActive;	// Link active color
	COLORREF	crVisited;	// Link visited color
	COLORREF	crHover;		// Hover color
} HYPERLINKCOLORS;

/*		To use the hyperlink control, just create a static control
 * (e.g. IDC_HYPERLINK) and attach it to a member variable of type CHyperlinkCtrl
 * in the class wizard (or by editing the header file and changing the
 *	definition of the member variable from CStatic to CHyperlinkCtrl).
 *
 *		The default URL for the link is taken as the caption of the static
 * control unless otherwise specified. If no caption for the control is specified
 * then it is set as the URL.
 */
class CHyperlinkCtrl : public CStatic
{
	DECLARE_DYNAMIC(CHyperlinkCtrl)

public:
// Link styles
	static const DWORD StyleUnderline;
	static const DWORD StyleUseHover;
	static const DWORD StyleAutoSize;
//	static const DWORD StyleDownClick;
	static const DWORD StyleGetFocusOnClick;
	static const DWORD StyleNoHandCursor;
	static const DWORD StyleNoActiveColor;

// Construction/destruction
	CHyperlinkCtrl();
	virtual ~CHyperlinkCtrl();

// Attributes
public:

// Operations
public:	
	virtual void GetColors (HYPERLINKCOLORS& linkColors) const
	{
		linkColors = m_Colors;
	}

	static void GetDefaultColors (HYPERLINKCOLORS& linkColors)
	{
		linkColors = gm_Colors;
	}

	virtual void SetColors (const HYPERLINKCOLORS& colors)
	{
		m_Colors = colors;
	}

	virtual void SetColors (const COLORREF crLinkColor, const COLORREF crActiveColor, const COLORREF crVisitedColor, const COLORREF crHoverColor=-1);

	static void SetDefaultColors (const HYPERLINKCOLORS& colors)
	{
		gm_Colors = colors;
	}

	static void SetDefaultColors (const COLORREF crLinkColor, const COLORREF crActiveColor, const COLORREF crVisitedColor, const COLORREF crHoverColor=-1);

	virtual HCURSOR GetLinkCursor () const
	{
		return m_hLinkCursor;
	}

	static HCURSOR GetDefaultLinkCursor ()
	{
		return gm_hLinkCursor;
	}

	virtual BOOL SetLinkCursor (HCURSOR hCursor)
	{
    	if (NULL == hCursor)
			return FALSE;

		m_hLinkCursor = hCursor;
		return TRUE;
	}

	static BOOL SetDefaultLinkCursor (HCURSOR hCursor)
	{
    	if (NULL == hCursor)
			return FALSE;

		gm_hLinkCursor = hCursor;
		return TRUE;
	}

	virtual void SetURL (LPCTSTR lpszURL);

   virtual const LPCTSTR GetURL () const
	{
		return m_strURL;
	}

	virtual const DWORD GetLinkStyle () const
	{
		return m_dwStyle;
	}

	virtual BOOL ModifyLinkStyle (const DWORD dwRemove, const DWORD dwAdd, const BOOL bApply=TRUE);	
    
	virtual void SetWindowText (LPCTSTR lpszText);
	virtual void SetFont (CFont *pFont);
	
	virtual const BOOL IsVisited() const
	{
		return m_bVisited;
	}

	virtual void SetVisited (const BOOL bVisited = TRUE)
	{
		m_bVisited = bVisited;
	}
	
	// Use this if you want to subclass and also set different URL
	virtual BOOL SubclassDlgItem (const UINT nID, CWnd	*pParent, LPCTSTR lpszURL=NULL)
	{
		m_strURL = ((NULL == lpszURL) ? _T("") : lpszURL);

		return CStatic::SubclassDlgItem(nID, pParent);
	}

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CHyperlinkCtrl)
	public:
		virtual BOOL PreTranslateMessage(MSG* pMsg);	
	protected:
		virtual void PreSubclassWindow();	
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Note: if already initialized then does nothing
	static HRESULT RetrieveDefaultCursor();

	virtual void AdjustWindow();	
	virtual void SwitchUnderline();
	
// Protected attributes
protected:
	// default values
	static HYPERLINKCOLORS	gm_Colors;
	static HCURSOR				gm_hLinkCursor;		// Hyperlink mouse cursor

	HYPERLINKCOLORS	m_Colors;

	HCURSOR  m_hLinkCursor;		// Hyperlink mouse cursor

	BOOL		m_bLinkActive;				// Is the link active?
	BOOL		m_bOverControl;			// Is cursor over control?
	BOOL		m_bVisited;				// Has link been visited?
	DWORD		m_dwStyle;					// Link styles

	CString			m_strURL;					// Hyperlink URL string
	CFont				m_Font;					// Underlined font (if required)	
	CToolTipCtrl	m_ToolTip;				// The link tooltip	

	// Generated message map functions
protected:
	//{{AFX_MSG(CHyperlinkCtrl)
	afx_msg HBRUSH CtlColor(CDC* pDC, UINT nCtlColor);
	afx_msg BOOL OnSetCursor(CWnd* pWnd, UINT nHitTest, UINT message);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnSetFocus(CWnd* pOldWnd);
	afx_msg void OnKillFocus(CWnd* pNewWnd);
	afx_msg UINT OnNcHitTest(CPoint point);
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

/*
 *	Based on a CodeGuru article by Peter Mares.
 *
 *		This control displays a series of blocks as progress (with various
 * controllable parameters) where the current value is "highlighted". It can
 * be used either as a simple progress or a "busy" display - i.e. where range
 * is unknown and we simply display a running progress.
 *
 * In order to use this control in a dialog, do the following:
 *
 *	1. Add a static control to your dialog resource (e.g. IDC_KCPROGRESS)
 *	2. Add a member variable for the static control (e.g. m_kcBProgress). 
 *	3. Change the variable declaration from: 
 *
 *			CStatic m_kxBProgress;
 *		to: 
 *			CKCBusyProgress m_kxBProgress;
 */
class CKCBusyProgressCtrl : public CStatic {
	public:
		// progress control available modes
		enum { KCBPC_MODE_BUSY=0, KCBPC_MODE_PROGRESS, KCBPC_BAD_MODE } KCBPCMODECASE;

		CKCBusyProgressCtrl ();
		virtual ~CKCBusyProgressCtrl();

		// recalculate the block sizes and optionally get the current client area
		virtual BOOL Recalc ();

		virtual void Reset (const BOOL fRedraw=TRUE);

		virtual const int GetStep () const
		{
			return m_nStep;
		}

		// Note: a NEGATIVE step means a decreasing progress
		virtual BOOL SetStep (const int nStep);

		// Note: stops when reaches limit value (if not BUSY mode)
		virtual BOOL StepIt ();

		// Set/Get the number of visible blocks in the control 
		virtual BOOL SetNumSteps (const unsigned nNumSteps);

		virtual const unsigned GetNumSteps () const
		{
			return m_nNumSteps;
		}

		// returns the block number corresponding with the current position
		virtual const unsigned GetCurrentStep () const;

		// returns FALSE if new position not within range
		virtual BOOL SetCurPos (const int nCurPos, const BOOL fRedraw=TRUE);
		virtual const int	GetCurPos () const
		{ 
			return m_nCurPos;
		}

		// Get/Set the number of pixels between each block 
		virtual void SetInterBlockPadding (const int nPadding)
		{
			m_nIBPadding = nPadding;
			Recalc();
		}

		virtual const int	GetInterBlockPadding ()
		{
			return m_nIBPadding;
		}

		// returns FALSE if bad mode supplied
		virtual BOOL SetMode (const unsigned nMode);

		virtual const unsigned GetMode () const
		{
			return m_nMode;
		}

		/* Upper/lower limit mode behavior when required to step beyond them:
		 *
		 *		Wrap-around - when progress reaches the limit, it starts again
		 *
		 *		Back-and-forth - when progress reaches the limit it reverses direction
		 */
		enum { KCBPC_LIMIT_WRAPAROUND=0, KCBPCS_LIMIT_BACKANDFORTH, KCBPC_LIMIT_BAD_MODE };
		virtual BOOL SetLimitMode (const unsigned nMode);

		virtual const unsigned GetLimitMode () const
		{
			return m_nLimitMode;
		}

		virtual BOOL SetRange (const int nLower, const int nUpper);
		virtual void GetRange (int& nLower, int& nUpper) const
		{ 
			nLower = m_nLower;
			nUpper = m_nUpper;
		}

		// Get/Set the colour of the background
		virtual COLORREF GetColBkg () const
		{
			return m_colBkg;
		}

		virtual void SetColBkg (const COLORREF col)
		{
			m_colBkg = col;
		}

		// Get/Set the fill colour of the face of normal blocks 
		virtual COLORREF GetColBlockFace ()	const
		{
			return m_colBlockFace;
		}

		virtual void SetColBlockFace (const COLORREF col)
		{
			m_colBlockFace = col;
		}

		// Get/Set the pen colour of the border of each normal block 
		virtual COLORREF GetColBlockEdge () const
		{
			return m_colBlockEdge;
		}

		virtual void SetColBlockEdge (const COLORREF col)
		{ 
			m_colBlockEdge = col;
		}

		// Get/Set the fill colour of the face of highlighted blocks
		virtual COLORREF GetColBlockFaceHi () const
		{ 
			return m_colBlockFaceHi;
		}

		virtual void SetColBlockFaceHi (const COLORREF col)
		{ 
			m_colBlockFaceHi = col;
		}

		// Get/Set the pen colour of the border of each highlighted block 
		virtual COLORREF GetColBlockEdgeHi () const
		{
			return m_colBlockEdgeHi;
		}

		virtual void SetColBlockEdgeHi (const COLORREF col)
		{ 
			m_colBlockEdgeHi = col;
		}

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CKCBusyProgressCtrl)
	public:
		virtual BOOL Create(DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID, CCreateContext* pContext = NULL);
	protected:
		virtual void PreSubclassWindow();
	//}}AFX_VIRTUAL

protected:
	//
	// Methods
	virtual void DrawBackground (CDC& dc, CRect& rect);
	virtual void DrawBlocks (CDC& dc, CRect& rect);

	virtual BOOL DrawBlock (CDC& dc, CRect& rect)
	{
		return dc.Rectangle(&rect);
	}

	virtual void DrawHiliteBlock (CDC& dc, CRect& rect);
	virtual void DrawPartialBlock (CDC& dc, CRect& rect, const int nPartial);

	//
	// Attributes
	unsigned		m_nNumSteps;
	int			m_nStep;
	unsigned		m_nAbsStepSize;
	unsigned		m_nLimitMode;
	int			m_nLimitDirection;	// (+1) - left-to-right, (-1), right-to-left
	int			m_nCurPos;
	CRect			m_rect;
	int			m_nIBPadding;
	double		m_dBlockHeight;
	double		m_dBlockWidth;
	unsigned		m_nMode;
	int			m_nLower;
	int			m_nUpper;

	// colours
	COLORREF		m_colBkg;
	COLORREF		m_colBlockFace;
	COLORREF		m_colBlockEdge;
	COLORREF		m_colBlockFaceHi;
	COLORREF		m_colBlockEdgeHi;

	// drawing stuff
	CDC			m_memDC;
	CBitmap		m_memBmp;
	CBitmap*		m_pOldBmp;

	// Generated message map functions
	//{{AFX_MSG(CKCBusyProgressCtrl)
	afx_msg void OnPaint();
	afx_msg void OnSize(UINT nType, int cx, int cy);
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

#endif /* _MFC_CUSTOM_CONTROLS_H */