// GradientProgressCtrl.cpp : implementation file
//
//
//
// Written by matt weagle (mweagle@redrose.net)
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
// Consider yourself lucky if it works, unlucky if it doesn't.
//
// Thanks to Chris Maunder (Chris.Maunder@cbr.clw.csiro.au) for the 
// foregoing disclaimer.
// 
// Please use and enjoy. Please let me know of any bugs/mods/improvements 
// that you have found/implemented and I will fix/incorporate them into this
// file. 

#include <mfc/cctrls.h>
#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////
// CGradientProgressCtrl

CGradientProgressCtrl::CGradientProgressCtrl()
	:	CProgressCtrl(),

		// initial positions
		m_nLower(0), m_nUpper(100),
		m_nCurrentPosition(0), m_nStep(1),

		// initial colors
		m_clrStart(RGB(255, 0,0)),
		m_clrEnd(RGB(0,255,0)),
		m_clrBkGround(::GetSysColor(COLOR_3DFACE)),
		m_clrText(RGB(255, 255, 255)),

		// Initial show percent
		m_efCase(PBS_SHOW_NONE)
{
}

CGradientProgressCtrl::~CGradientProgressCtrl()
{
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CGradientProgressCtrl, CProgressCtrl)
	ON_WM_PAINT()
	ON_WM_ERASEBKGND()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CGradientProgressCtrl message handlers

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
HRESULT CGradientProgressCtrl::SetTextFormat (LPCTSTR lpszFmt, const PBSTEXTFORMATCASE efCase)
{
	// the only time the format is NULL/empty is when we hide the text
	if (IsEmptyStr(lpszFmt) && (efCase != PBS_SHOW_NONE))
		return ERROR_BAD_ARGUMENTS;
	if ((unsigned) efCase >= (unsigned) PBS_BAD_FORMAT)
		return ERROR_BAD_ARGUMENTS;

	// cannot do anything if no window available
	if (!::IsWindow(m_hWnd))
		return ERROR_BAD_ENVIRONMENT;

	// make sure requested value appears only once
	ULONG	ulModifiers=0;
	for (LPCTSTR	lpszCurPos=lpszFmt; *lpszCurPos != _T('\0'); lpszCurPos++)
		if (_T('%') == *lpszCurPos)
		{
			// skip '%%'
			if (_T('%') == *(lpszCurPos+1))
			{
				lpszCurPos++;
				continue;
			}

			ulModifiers++;
		}

	// check if we have a correct number of modifiers
	switch(efCase)
	{
		case PBS_SHOW_NONE	:	// don't care what the format is
			break;

		case PBS_SHOW_PERCENT:	// at most one modifier allowed
			if (ulModifiers > 1)
				return ERROR_BAD_FORMAT;
			break;

		case PBS_SHOW_POSITION:	// at most 2 modifiers allowed
			if (ulModifiers > 2)
				return ERROR_BAD_FORMAT;
			break;

		default					:
			return ERROR_INVALID_SERVER_STATE;
	}

	SetWindowText((PBS_SHOW_NONE == efCase) ? _T("") : GetSafeStrPtr(lpszFmt));
	m_efCase = efCase;
	return S_OK;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT CGradientProgressCtrl::GetTextFormat (CString& strFmt, PBSTEXTFORMATCASE& efCase) const
{
	if (!::IsWindow(m_hWnd))
	{
		strFmt = _T("");
		efCase = PBS_BAD_FORMAT;
		return ERROR_BAD_ENVIRONMENT;
	}

	GetWindowText(strFmt);
	efCase = m_efCase;
	return S_OK;
}

/************************************************************************/
// The main drawing routine.  Consists of two parts
// (1) Call the DrawGradient routine to draw the visible part of the progress gradient
// (2) If needed, show the percentage text
/************************************************************************/

void CGradientProgressCtrl::OnPaint () 
{
	CPaintDC dc(this); // device context for painting

	// TODO: Add your message handler code here

	// If the current positionis  invalid then we should fade into the  background
	if ((m_nCurrentPosition <= m_nLower) || (m_nCurrentPosition >= m_nUpper))
	{
		CRect rect;
		GetClientRect(rect);
		CBrush brush;
		brush.CreateSolidBrush(::GetSysColor(COLOR_3DFACE));
		dc.FillRect(&rect, &brush);
		VERIFY(brush.DeleteObject());
		return;
	}

	// Figure out what part should be visible so we can stop the gradient when needed
	CRect rectClient;
	GetClientRect(&rectClient);
	float maxWidth=((float)m_nCurrentPosition/(float)m_nUpper * (float)rectClient.right);

	// Draw the gradient
	DrawGradient(&dc, rectClient, (int)maxWidth);

	// Show text if needed
	if ((m_efCase != PBS_SHOW_NONE) && ((unsigned) m_efCase < (unsigned) PBS_BAD_FORMAT))
	{
		CString strFmt=_T("");
		GetWindowText(strFmt);

		// should not happen, but what the heck
		if (!strFmt.IsEmpty())
		{
			CString	strDisplay=_T("");

			switch(m_efCase)
			{
				case PBS_SHOW_PERCENT	:
					{
						const ULONG ulPercent=((m_nCurrentPosition-m_nLower) * 100) / max(1,(m_nUpper-m_nLower));
						strDisplay.Format((LPCTSTR) strFmt, ulPercent);
					}
					break;

				case PBS_SHOW_POSITION	:
					strDisplay.Format((LPCTSTR) strFmt, m_nCurrentPosition, m_nUpper);
					break;

				default					:
					strDisplay = strFmt;
					ASSERT(FALSE);
			}

			dc.SetTextColor(m_clrText);
			dc.SetBkMode(TRANSPARENT);
			dc.DrawText(strDisplay, &rectClient, DT_VCENTER |  DT_CENTER | DT_SINGLELINE);
		}
	}

	// Do not call CProgressCtrl::OnPaint() for painting messages
}

/*************************************************************************/
// Need to keep track of wher the indicator thinks it is.
/*************************************************************************/
void CGradientProgressCtrl:: SetRange32 (int nLower, int nUpper)
{
	m_nLower = nLower;
	m_nUpper = nUpper;
	ASSERT(m_nLower < m_nUpper);
	m_nCurrentPosition = nLower;

	CProgressCtrl::SetRange32(nLower, nUpper);
}

/*************************************************************************/
// Need to keep track of wher the indicator thinks it is.
/*************************************************************************/
int CGradientProgressCtrl::SetPos (int nPos)
{
	m_nCurrentPosition = nPos;

	int nRes = CProgressCtrl::SetPos(nPos);
	if (m_efCase != PBS_SHOW_NONE)	// redraw the text
		Invalidate();
	return nRes;
}

// returns the previous (!) position of the progress bar control.
int CGradientProgressCtrl::OffsetPos (int nPos)
{
	int	nRes=GetPos(), newPos=(nRes + nPos);
	SetPos(newPos);
	return nRes;
}

/*************************************************************************/
// Need to keep track of wher the indicator thinks it is.
/*************************************************************************/
int CGradientProgressCtrl::SetStep(int nStep)
{
	m_nStep = nStep;

	return (CProgressCtrl::SetStep(nStep));
}

/*************************************************************************/
// Need to keep track of wher the indicator thinks it is.
/*************************************************************************/
int CGradientProgressCtrl::StepIt(void)
{
	m_nCurrentPosition += m_nStep;

	return (CProgressCtrl::StepIt());
}

/*************************************************************************/
// Where most of the actual work is done.  The general version would fill the entire rectangle with
// a gradient, but we want to truncate the drawing to reflect the actual progress control position.
/*************************************************************************/
void CGradientProgressCtrl::DrawGradient(CPaintDC *pDC, const RECT &rectClient, const int &nMaxWidth)
{
	CMemDC memDC(pDC);

	// First find out the largest color distance between the start and end colors.  This distance
	// will determine how many steps we use to carve up the client region and the size of each
	// gradient rect.
	int	r=(GetRValue(m_clrEnd) - GetRValue(m_clrStart));
	int	g=(GetGValue(m_clrEnd) - GetGValue(m_clrStart));
	int	b=(GetBValue(m_clrEnd) - GetBValue(m_clrStart));

	// Make the number of steps equal to the greatest distance
	int nSteps=max(abs(r), max(abs(g), abs(b)));

	// Determine how large each band should be in order to cover the
	// client with nSteps bands (one for every color intensity level)
	float fStep = (float)rectClient.right / (float)nSteps;

	// Calculate the step size for each color
	float	rStep = r/(float)nSteps;
	float gStep = g/(float)nSteps;
	float bStep = b/(float)nSteps;

	// Reset the colors to the starting position
	r = GetRValue(m_clrStart);
	g = GetGValue(m_clrStart);
	b = GetBValue(m_clrStart);


	// Start filling bands
	for (int iOnBand = 0; iOnBand < nSteps; iOnBand++) 
	{
		RECT rectFill;	   // Rectangle for filling band
		VERIFY(::SetRect(&rectFill,
							  (int)(iOnBand * fStep),       // Upper left X
							  0,									 // Upper left Y
							  (int)((iOnBand+1) * fStep),          // Lower right X
							  rectClient.bottom+1));			// Lower right Y
	
		// CDC::FillSolidRect is faster, but it does not handle 8-bit color depth
		CBrush brush;			// Brush to fill in the bar	
		VERIFY(brush.CreateSolidBrush(RGB(r+rStep*iOnBand, g + gStep*iOnBand, b + bStep *iOnBand)));
		memDC.FillRect(&rectFill,&brush);
		VERIFY(brush.DeleteObject());

		// If we are past the maximum for the current position we need to get out of the loop.
		// Before we leave, we repaint the remainder of the client area with the background color.
		if (rectFill.right > nMaxWidth)
		{
			::SetRect(&rectFill, rectFill.right, 0, rectClient.right, rectClient.bottom);
			VERIFY(brush.CreateSolidBrush(m_clrBkGround));
			memDC.FillRect(&rectFill, &brush);
			VERIFY(brush.DeleteObject());
			return;
		}
	}
}

/*************************************************************************/
// All drawing is done in the OnPaint function
/*************************************************************************/
BOOL CGradientProgressCtrl::OnEraseBkgnd(CDC* pDC) 
{
	// TODO: Add your message handler code here and/or call default
	return TRUE;
}
