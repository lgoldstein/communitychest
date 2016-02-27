/****************************************************************************
 *	class		:	CKCBusyProgressCtrl
 *	author		:	Peter Mares / kinkycode.com (gui@ch.co.za)
 *	base class	:	CStatic (MFC)
 *	notes		:	Control to be used for progress indication when there are
 *					no lower and upper bounds available for a progress bar.
 *					Can also be used as a normal progress bar
 *
 *	Disclaimer	:	Its free, it feels good and its from South Africa :)
 ****************************************************************************/

#include <mfc/cctrls.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CKCBusyProgressCtrl

CKCBusyProgressCtrl::CKCBusyProgressCtrl ()
	: CStatic()
	, m_nNumSteps(10)
	, m_nStep(1)
	, m_nAbsStepSize(1)
	, m_nCurPos(0)
	, m_colBkg(::GetSysColor(COLOR_BTNFACE))
	, m_colBlockFace(RGB(160, 190, 220))
	, m_colBlockEdge(RGB(50, 90, 135))
	, m_colBlockFaceHi(RGB(190, 220, 255))
	, m_colBlockEdgeHi(RGB(50, 90, 135))
	, m_pOldBmp(NULL)
	, m_nIBPadding(1)
	, m_rect(0,0,0,0)
	, m_dBlockHeight(0.0L)
	, m_dBlockWidth(0.0L)
	, m_nMode(KCBPC_MODE_BUSY)
	, m_nLimitMode(KCBPC_LIMIT_WRAPAROUND)
	, m_nLimitDirection(1)
	, m_nLower(0)
	, m_nUpper(100)
{
}

/////////////////////////////////////////////////////////////////////////////

CKCBusyProgressCtrl::~CKCBusyProgressCtrl()
{
	if (m_pOldBmp != NULL)
	{
		m_memDC.SelectObject(m_pOldBmp);
		m_memBmp.DeleteObject();
		m_memDC.DeleteDC();
		m_pOldBmp = NULL;
	}
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CKCBusyProgressCtrl, CStatic)
	//{{AFX_MSG_MAP(CKCBusyProgressCtrl)
	ON_WM_PAINT()
	ON_WM_SIZE()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CKCBusyProgressCtrl message handlers

void CKCBusyProgressCtrl::PreSubclassWindow() 
{
	DWORD	dwStyle=GetStyle();

//	dwStyle |= SS_OWNERDRAW;
	CStatic::PreSubclassWindow();
	Recalc();
}

/////////////////////////////////////////////////////////////////////////////

BOOL CKCBusyProgressCtrl::Create(DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID, CCreateContext* pContext) 
{
	BOOL	bResult=CWnd::Create(_T("STATIC"), _T("KCBusyProgressCtrl"), dwStyle, rect, pParentWnd, nID, pContext);
	ASSERT(bResult);

	VERIFY(Recalc());

	return bResult;
}

/////////////////////////////////////////////////////////////////////////////
//	function		:	Recalc()
//	description		:	Function used to recalculate the block sizes and
//						optionally get the current client area
/////////////////////////////////////////////////////////////////////////////
BOOL CKCBusyProgressCtrl::Recalc()
{
	if (0 == m_nNumSteps)
		return FALSE;

	if (m_rect.IsRectEmpty())
		GetClientRect(&m_rect);

	CRect	tRect=m_rect;
	tRect.right -= (m_nNumSteps * m_nIBPadding);

	m_dBlockWidth = ((double)tRect.Width() / (double) m_nNumSteps);
	m_dBlockHeight = tRect.Height();
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CKCBusyProgressCtrl::SetNumSteps (const unsigned nNumSteps)
{ 
	if (0 == nNumSteps)
		return FALSE;

	m_nNumSteps = nNumSteps;
	return Recalc();
}

/////////////////////////////////////////////////////////////////////////////

BOOL CKCBusyProgressCtrl::SetRange (const int nLower, const int nUpper)
{
	if (nLower >= nUpper)
		return FALSE;

	m_nLower = nLower;
	m_nUpper = nUpper;
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

// returns FALSE if new position not within range
BOOL CKCBusyProgressCtrl::SetCurPos (const int nCurPos, const BOOL fRedraw)
{
	if ((nCurPos < m_nLower) || (nCurPos > m_nUpper))
		return FALSE;

	m_nCurPos = nCurPos;
	if (fRedraw)
		Invalidate();

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::OnPaint() 
{
	CPaintDC	dc(this); // device context for painting

	// create a memory dc if needs be
	if (NULL == m_memDC.m_hDC)
	{
		VERIFY(m_memDC.CreateCompatibleDC(&dc));
		VERIFY(m_memBmp.CreateCompatibleBitmap(&dc, m_rect.Width(), m_rect.Height()));
		VERIFY((m_pOldBmp=m_memDC.SelectObject(&m_memBmp)) != NULL);
	}

	DrawBackground(m_memDC, m_rect);
	DrawBlocks(m_memDC, m_rect);

	// render the final image
	dc.BitBlt(0, 0, m_rect.Width(), m_rect.Height(), &m_memDC, 0, 0, SRCCOPY);
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::DrawBackground (CDC& dc, CRect& rect)
{
	dc.FillSolidRect(&rect, m_colBkg);
}

/////////////////////////////////////////////////////////////////////////////

// returns the block number corresponding with the current position
const unsigned CKCBusyProgressCtrl::GetCurrentStep () const
{
	ASSERT(m_nNumSteps != 0);
	ASSERT((m_nLower <= m_nCurPos) && (m_nCurPos <= m_nUpper));

	const unsigned nRange=(1 + (m_nUpper - m_nLower));
	const unsigned	nPosUsage=(1 + (m_nCurPos - m_nLower));
	const double	dUsagePct=((double) nPosUsage) / ((double) nRange);
	const unsigned	nCurStep=(unsigned) ((double) m_nNumSteps * dUsagePct);

	return min(nCurStep, (m_nNumSteps-1));
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::DrawBlocks (CDC& dc, CRect& rect)
{
	// create some drawing tools
	CPen	nPen;
	VERIFY(nPen.CreatePen(PS_SOLID, 0, m_colBlockEdge));
	CPen	*pOldPen=dc.SelectObject(&nPen);

	CBrush	nBrush;
	VERIFY(nBrush.CreateSolidBrush(m_colBlockFace));
	CBrush	*pOldBrush=dc.SelectObject(&nBrush);

	// create the initial rectangle
	CRect	bRect;
	bRect.top = 0; bRect.bottom = (int) m_dBlockHeight;
	bRect.left = 0; bRect.right = (int) m_dBlockWidth;

	const unsigned	nCurStep=GetCurrentStep();
	const double	dPerc=(double) (1 + (m_nCurPos - m_nLower)) / (double) (1 + (m_nUpper - m_nLower));
	const unsigned	nFull=(unsigned) (dPerc * (double) m_nNumSteps)-1;
	const double	dPerc2=(((dPerc * (double) m_nNumSteps)-1) - nFull);
	const int		nPartial=(int) (m_dBlockWidth * dPerc2);

	double	dXOffset=0;
	for (unsigned i = 0; i < m_nNumSteps; i++)
	{
		switch(m_nMode)
		{
			case KCBPC_MODE_BUSY	:
				if (nCurStep == i)
					DrawHiliteBlock(dc, bRect);
				else
					VERIFY(DrawBlock(dc, bRect));
				break;

			case KCBPC_MODE_PROGRESS	:
				if (i <= nFull)
					DrawHiliteBlock(dc, bRect);
				else
				{
					VERIFY(DrawBlock(dc, bRect));
					if ((i == (nFull + 1)) && (nPartial != 0))
						DrawPartialBlock(dc, bRect, nPartial);
				}
				break;

			default						: ASSERT(FALSE);
		}

		// offset the rectangle a bit
		dXOffset += m_dBlockWidth + (double) m_nIBPadding;
		bRect.left = (int) dXOffset;
		bRect.right = (int) (dXOffset + m_dBlockWidth);
	}

	// cleanup after ourselves...
	dc.SelectObject(pOldPen);
	VERIFY(nPen.DeleteObject());

	dc.SelectObject(pOldBrush);
	VERIFY(nBrush.DeleteObject());
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::DrawHiliteBlock (CDC& dc, CRect& rect)
{
	// use the correct tools ;)
	CPen	nPen;
	VERIFY(nPen.CreatePen(PS_SOLID, 0, m_colBlockEdgeHi));
	CPen	*pOldPen=dc.SelectObject(&nPen);

	CBrush	nBrush;
	VERIFY(nBrush.CreateSolidBrush(m_colBlockFaceHi));
	CBrush	*pOldBrush=dc.SelectObject(&nBrush);

	VERIFY(dc.Rectangle(&rect));

	// cleanup
	dc.SelectObject(pOldPen);
	VERIFY(nPen.DeleteObject());

	dc.SelectObject(pOldBrush);
	VERIFY(nBrush.DeleteObject());
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::DrawPartialBlock (CDC& dc, CRect& rect, const int nPartial)
{
	CRect	pRect = rect;

	pRect.DeflateRect(1, 1);
	pRect.right = pRect.left + nPartial;
	if (pRect.right >= rect.right)
		pRect.right = rect.right - 1;

	dc.FillSolidRect(&pRect, m_colBlockFaceHi);
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::OnSize (UINT nType, int cx, int cy) 
{
	CStatic::OnSize(nType, cx, cy);
	
	GetClientRect(&m_rect);
	if (m_memDC.m_hDC != NULL)
	{
		// delete the dc to allow OnPaint to recreate the DC for the new size
		m_memDC.SelectObject(m_pOldBmp);
		VERIFY(m_memBmp.DeleteObject());
		VERIFY(m_memDC.DeleteDC());
	}
}

/////////////////////////////////////////////////////////////////////////////

// Note: a NEGATIVE step means a decreasing progress
BOOL CKCBusyProgressCtrl::SetStep (const int nStep)
{
	// do not allow zero or above-range step size
	if ((0 == nStep) || (abs(nStep) > (1 + (m_nUpper - m_nLower))))
		return FALSE;

	m_nStep = nStep;
	m_nAbsStepSize = abs(nStep);
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CKCBusyProgressCtrl::SetLimitMode (const unsigned nMode)
{
	if (nMode >= KCBPC_LIMIT_BAD_MODE)
		return FALSE;

	m_nLimitMode = nMode;
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

// Note: stops when reaches limit value (if not BUSY mode)
BOOL CKCBusyProgressCtrl::StepIt ()
{
	switch(m_nMode)
	{
		case KCBPC_MODE_BUSY	:
			{
				if (m_nLimitDirection > 0)	// going left-to-right
				{
					if (m_nCurPos >= m_nUpper)
					{
						switch(m_nLimitMode)
						{
							case KCBPC_LIMIT_WRAPAROUND		:
								m_nCurPos = m_nLower;
								break;

							case KCBPCS_LIMIT_BACKANDFORTH	:
								m_nLimitDirection = (-1);	// change direction
								m_nCurPos = (m_nUpper - m_nAbsStepSize);
								break;

							default	: ASSERT(FALSE);
						}
					}
					else	// keep going to the right
						m_nCurPos = min(m_nUpper, (int) (m_nCurPos + m_nAbsStepSize));
				}
				else	// going right-to-left
				{
					if (m_nCurPos <= m_nLower)
					{
						switch(m_nLimitMode)
						{
							case KCBPC_LIMIT_WRAPAROUND		:
								m_nCurPos = m_nUpper;
								break;

							case KCBPCS_LIMIT_BACKANDFORTH	:
								m_nLimitDirection = 1;	// change direction
								m_nCurPos = (m_nLower + m_nAbsStepSize);
								break;

							default	: ASSERT(FALSE);
						}
					}
					else
						m_nCurPos = max(m_nLower, (int) (m_nCurPos - m_nAbsStepSize));
				}
			}
			break;

		case KCBPC_MODE_PROGRESS	:
			{
				const int	nNewPos=(m_nCurPos + m_nStep);
				if (m_nStep > 0)
				{
					if (m_nCurPos >= m_nUpper)
						return FALSE;

					m_nCurPos = min(m_nUpper, nNewPos);
				}
				else	// for negative step, check lower limit
				{
					if (m_nCurPos <= m_nLower)
						return FALSE;

					m_nCurPos = max(m_nLower, nNewPos);
				}
			}
			break;

		default		: ASSERT(FALSE);
	}

	Invalidate();
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

// returns FALSE if bad mode supplied
BOOL CKCBusyProgressCtrl::SetMode (const unsigned nMode)
{
	if (nMode >= KCBPC_BAD_MODE)
		return FALSE;

	m_nMode = nMode;
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

void CKCBusyProgressCtrl::Reset(const BOOL fRedraw)
{
	m_nCurPos = ((m_nStep > 0) ? m_nLower : m_nUpper);
	m_nLimitDirection = ((m_nStep > 0) ? 1 : (-1));

	if (fRedraw)
		Invalidate();
}

/////////////////////////////////////////////////////////////////////////////
