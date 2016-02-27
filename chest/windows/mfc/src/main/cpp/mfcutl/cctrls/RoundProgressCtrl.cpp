// RoundProgressCtrl.cpp : implementation file
//
#include <mfc/cctrls.h>

#include <math.h>

static const double PIValue=3.1415926535;

/////////////////////////////////////////////////////////////////////////////
// RoundProgressCtrl

LONG CRoundProgressCtrl::m_lRefs=0;

BOOL CRoundProgressCtrl::RegisterWndClass(HINSTANCE hInstance)
{
	WNDCLASS wc;
	memset(&wc, 0, (sizeof wc));

	wc.lpszClassName = _T("ROUND_PROGRESS_CUSTOM_CTRL"); // matches class name in client
	wc.hInstance = hInstance;
	wc.lpfnWndProc = ::DefWindowProc;
	wc.style = CS_GLOBALCLASS; // To be modified

	return (::RegisterClass(&wc) != 0);
}

BOOL CRoundProgressCtrl::CheckWndClassRegistration ()
{
	LONG	lPrev=::InterlockedIncrement(&m_lRefs);
	if (lPrev > 1)
		return TRUE;

	if (!CRoundProgressCtrl::RegisterWndClass(AfxGetInstanceHandle()))
		return FALSE;

	lPrev = ::InterlockedIncrement(&m_lRefs);	// add one more ref to compensate for the destructor
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

CRoundProgressCtrl::CRoundProgressCtrl()
	: CWnd(), m_uCurValue(0), m_uMaxValue(100), m_uStepValue(1), m_rcCtrl(0, 0, 0, 0)
{
	VERIFY(this->CheckWndClassRegistration());

	m_bgColor = ::GetSysColor(COLOR_WINDOW);
	m_fgColor = ::GetSysColor(COLOR_ACTIVECAPTION);
}

CRoundProgressCtrl::CRoundProgressCtrl (const COLORREF bgColor, const COLORREF fgColor)
	: CWnd(), m_bgColor(bgColor), m_fgColor(fgColor), m_uStepValue(1),
	  m_uCurValue(0), m_uMaxValue(100), m_rcCtrl(0, 0, 0, 0)
{
	VERIFY(this->CheckWndClassRegistration());
}

CRoundProgressCtrl::~CRoundProgressCtrl()
{
	LONG	lPrev=::InterlockedDecrement(&m_lRefs);
	ASSERT(lPrev > 0);
}

/////////////////////////////////////////////////////////////////////////////

void CRoundProgressCtrl::RecalcCtrlRect (const CRect& rcClient)
{
	// the available size for drawing is limited by the shortest dimension
	const int nSize=min(rcClient.Height(),rcClient.Width());
	m_rcCtrl = CRect(CPoint(rcClient.left + ((rcClient.Width() - nSize) / 2), rcClient.top + (rcClient.Height() - nSize)), CSize(nSize, nSize));
}

void CRoundProgressCtrl::RecalcCtrlRect ()
{
	CRect rcClient;
	GetClientRect(&rcClient);

	RecalcCtrlRect(rcClient);
}

/////////////////////////////////////////////////////////////////////////////

BOOL CRoundProgressCtrl::DrawProgress ()
{
	InvalidateRect(NULL, FALSE);
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CRoundProgressCtrl::SetPos (const UINT uPos, const BOOL fRedraw)
{
	m_uCurValue = min(uPos, m_uMaxValue);
	return (fRedraw ? DrawProgress() : TRUE);
}

BOOL CRoundProgressCtrl::SetRange (const UINT uMaxValue, const BOOL fRedraw)
{
	if (0 == uMaxValue)
		return FALSE;

	m_uMaxValue = uMaxValue;

	return SetPos(m_uCurValue, fRedraw);
}

/////////////////////////////////////////////////////////////////////////////

BOOL CRoundProgressCtrl::SetStep (const UINT uStep)
{
	if (0 == uStep)
		return FALSE;

	m_uStepValue = uStep;
	return TRUE;
}

// Note: stops when reaches maximum value (or more)
BOOL CRoundProgressCtrl::StepIt (const BOOL fRedraw)
{
	if (m_uCurValue < m_uMaxValue)
		m_uCurValue = min((m_uCurValue+m_uStepValue), m_uMaxValue);

	return SetPos(m_uCurValue, fRedraw);
}	

/////////////////////////////////////////////////////////////////////////////

BOOL CRoundProgressCtrl::SetProgressColors (const COLORREF fgCol, const COLORREF bgCol, const BOOL fRedraw)
{
	m_fgColor = fgCol;
	m_bgColor = bgCol;

	return (fRedraw ? DrawProgress() : TRUE);
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CRoundProgressCtrl, CWnd)
	ON_WM_PAINT()
	ON_WM_SIZE()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// RoundProgressCtrl message handlers

void CRoundProgressCtrl::OnPaint() 
{
	CPaintDC dc(this); // device context for painting

	CRect rcClient;
	GetClientRect(&rcClient);

	if (m_rcCtrl.IsRectEmpty()) // First Time
		RecalcCtrlRect(rcClient);

	CDC	memDC;
	VERIFY(memDC.CreateCompatibleDC(&dc));

	CBitmap	bmp;
	VERIFY(bmp.CreateCompatibleBitmap(&dc, rcClient.Width(), rcClient.Height()));
	CBitmap *pOldmemDCBitmap=(CBitmap *) memDC.SelectObject(&bmp);

	memDC.FillSolidRect(rcClient, m_bgColor);

	// TODO: use shortcuts for well known angles: 0, 90, 180, 270
	if ((m_uCurValue=min(m_uCurValue, m_uMaxValue)) > 0)
	{
		ASSERT(m_uMaxValue > 0);

		int	nXPos=(-1), nYPos(-1);
		if (m_uCurValue < m_uMaxValue)
		{
			const double	dValue=(((double) m_uCurValue) * 360.0) / (double) max(m_uMaxValue,1);
			const double	dAngle=(dValue * PIValue) / (double) 180.0;
			const double	dRadius=((double) m_rcCtrl.Height())/2.0;
			const double	dOffX=(dRadius * sin(dAngle));
			const double	dOffY=0.0 - (dRadius * cos(dAngle));
			const double	dX=((double) (m_rcCtrl.right+m_rcCtrl.left))/2.0;
			const double	dY=((double) (m_rcCtrl.top+m_rcCtrl.bottom))/2.0;
		
			nXPos = (int) (dX + dOffX);
			nYPos = (int) (dY + dOffY);
		}
		else	// if maximum value then draw a solid circle
		{
			nXPos = (m_rcCtrl.right + m_rcCtrl.left) / 2;
			nYPos = (m_rcCtrl.top + m_rcCtrl.bottom) / 2;
		}

		CBrush	indBrush;
		VERIFY(indBrush.CreateSolidBrush(m_fgColor));
		CBrush	*pPrevIndBrush = memDC.SelectObject(&indBrush);
		VERIFY(memDC.Pie(m_rcCtrl, CPoint(0, 0), CPoint(nXPos, nYPos)));

		dc.BitBlt(0, 0, rcClient.Width(), rcClient.Height(), &memDC, 0, 0, SRCCOPY);
		memDC.SelectObject(pPrevIndBrush);
	}
	else
	{
		dc.BitBlt(0, 0, rcClient.Width(), rcClient.Height(), &memDC, 0, 0, SRCCOPY);
	}

	memDC.SelectObject(pOldmemDCBitmap);
}

/////////////////////////////////////////////////////////////////////////////

void CRoundProgressCtrl::OnSize(UINT nType, int cx, int cy) 
{
	CWnd::OnSize(nType, cx, cy);
	
	RecalcCtrlRect();
}

/////////////////////////////////////////////////////////////////////////////
