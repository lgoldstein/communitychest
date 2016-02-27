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

#include <mfc/cctrls.h>
#include <math.h>

#include <util/string.h>

//////////////////////////////////////////////////////////////////////////////

CPieChartPiece::CPieChartPiece ()
	: m_colorBack(::GetSysColor(COLOR_WINDOW)), m_colorText(::GetSysColor(COLOR_WINDOWTEXT)), m_nSize(0)
{
	VERIFY(m_brushBack.CreateSolidBrush(m_colorBack));
}

CPieChartPiece::CPieChartPiece (COLORREF		colorBack,
										  COLORREF		colorText,
										  const int		nSize,
										  LPCTSTR		lpszStrInfo,
										  const DWORD	dwData)
	: m_colorBack(colorBack), m_colorText(colorText), m_nSize(nSize), m_strInfo(GetSafeStrPtr(lpszStrInfo)), m_dwData(dwData)
{
	VERIFY(m_brushBack.CreateSolidBrush(m_colorBack));
}

BOOL CPieChartPiece::SetInfo (COLORREF		colorBack,
										COLORREF		colorText,
										const int	nSize,
										LPCTSTR		lpszStrInfo,
										const DWORD	dwData)
{
	if (!SetColors(colorBack, colorText))
		return FALSE;

	m_nSize = nSize;
	m_strInfo = GetSafeStrPtr(lpszStrInfo);
	m_dwData = dwData;

	return TRUE;
}

void CPieChartPiece::GetColors (COLORREF& clrBack, COLORREF& clrText) const
{
	clrBack = m_colorBack;
	clrText = m_colorText;
}

BOOL CPieChartPiece::SetColors (COLORREF clrBack, COLORREF clrText)
{
	m_colorBack = clrBack;
	m_colorText = clrText;

	if (m_brushBack.m_hObject != NULL)
		m_brushBack.DeleteObject();
	return m_brushBack.CreateSolidBrush(m_colorBack);
}

LPCTSTR CPieChartPiece::GetName () const
{
	return GetSafeStrPtr(m_strInfo);
}

void CPieChartPiece::SetName (LPCTSTR lpszName /* can be NULL */)
{
	m_strInfo = GetSafeStrPtr(lpszName);
}

/////////////////////////////////////////////////////////////////////////////

static const double piValue=3.1415926535;

/////////////////////////////////////////////////////////////////////////////

CPieChartCtrl::CPieChartCtrl ()
	: m_nLower(0), m_nUpper(359), m_fDrawPercent(TRUE),
	  m_clrLine(RGB(0,0,0)), m_clrDefault(RGB(0,0,255))
{
	this->RegisterWndClass(AfxGetInstanceHandle());

	
	m_rectChart.SetRect(0,0,0,0);

	m_fontInfo.CreateFont(13, 0,0,0,FW_NORMAL, 0,0,0,
		DEFAULT_CHARSET, OUT_CHARACTER_PRECIS, CLIP_CHARACTER_PRECIS,
		DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, "Arial");
}

CPieChartCtrl::~CPieChartCtrl()
{
	Reset(FALSE);
}

/*--------------------------------------------------------------------------*/

BEGIN_MESSAGE_MAP(CPieChartCtrl, CWnd)
	//{{AFX_MSG_MAP(CPieChartCtrl)
	ON_WM_PAINT()
	ON_WM_SIZE()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/*--------------------------------------------------------------------------*/

// ensures that "nLower" < "nUpper"
BOOL CPieChartCtrl::SetRange (const int nLower, const int nUpper, const BOOL fRedraw)
{
	if (nLower >= nUpper)
		return FALSE;

	m_nLower = nLower;
	m_nUpper = nUpper;

	if (fRedraw)
		InvalidateRect(NULL, FALSE);

	return TRUE;
}

/*--------------------------------------------------------------------------*/

void CPieChartCtrl::SetColors (COLORREF clrDefault, COLORREF clrLine, const BOOL fRedraw)
{
	m_clrDefault = clrDefault;
	m_clrLine = clrLine;

	if (fRedraw)
		InvalidateRect(NULL, FALSE);
}

/*--------------------------------------------------------------------------*/

BOOL CPieChartCtrl::RegisterWndClass (HINSTANCE hInstance)
{
	WNDCLASS wc={ 0 };
	wc.lpszClassName = "PIE_CHART_CTRL"; // matches class name in client
	wc.hInstance = hInstance;
	wc.lpfnWndProc = ::DefWindowProc;
	wc.hCursor = ::LoadCursor(NULL, IDC_CROSS);
	wc.hIcon = 0;
	wc.lpszMenuName = NULL;
	wc.hbrBackground = (HBRUSH) ::GetStockObject(LTGRAY_BRUSH);
	wc.style = CS_GLOBALCLASS; // To be modified
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;

	return (::RegisterClass(&wc) != 0);
}

/*--------------------------------------------------------------------------*/

void CPieChartCtrl::OnPaint() 
{
	if (m_rectChart.IsRectEmpty()) // First Time
		RecalcRect();

	CPaintDC dc(this); // device context for painting
	CRect clientRect;
	GetClientRect(&clientRect);
	
	CDC memDC;
	VERIFY(memDC.CreateCompatibleDC(&dc));

	CBitmap bitmap;
	VERIFY(bitmap.CreateCompatibleBitmap(&dc, clientRect.Width(), clientRect.Height()));

	CBitmap *pOldmemDCBitmap=(CBitmap *) memDC.SelectObject(&bitmap);
	memDC.FillSolidRect(clientRect, ::GetSysColor(COLOR_3DFACE));

	CPen pen;
	VERIFY(pen.CreatePen(PS_SOLID, 1, m_clrLine));
	CPen	*pOldPen=(CPen *) memDC.SelectStockObject(NULL_PEN);

	CBrush brush;
	VERIFY(brush.CreateSolidBrush(m_clrDefault));

	CBrush	*pOldBrush=memDC.SelectObject(&brush);
	VERIFY(memDC.Ellipse(m_rectChart));

	const int	nCount=m_chartPieces.GetSize();
	const int	nTotalSize=(m_nUpper - m_nLower);

	CPoint pt1;
	int nCurrectAngle = 0;
	CountPoint(nCurrectAngle, pt1);

	for (int i=0; i < nCount; i++)
	{
		CPieChartPiece	*pItem=m_chartPieces.GetAt(i);
		ASSERT(pItem != NULL);

		const LPCTSTR	lpszItemName=pItem->GetName();
		const int		nItemSize=pItem->GetSize();
		// skip "empty" items
		if (0 == nItemSize)
			continue;

		const int	nItemAngle=((360 * nItemSize) / nTotalSize);
		nCurrectAngle += nItemAngle;

		CPoint pt2;
		CountPoint(nCurrectAngle, pt2);

		memDC.SelectStockObject(NULL_PEN);
		CBrush&	itemBrush=pItem->GetBrush();
		memDC.SelectObject(&itemBrush);

		if (pt2 != pt1)
			VERIFY(memDC.Pie(m_rectChart, pt2, pt1));
		
		// Draw separation line
		memDC.SelectObject(&pen);
		memDC.MoveTo(pt1);
		memDC.LineTo(m_rectChart.CenterPoint());
		memDC.LineTo(pt2);

		//Draw info
		CFont	*pOldFont=memDC.SelectObject(&m_fontInfo);
		memDC.SetBkMode(TRANSPARENT);

		// Draw percent
		if (m_fDrawPercent && (nItemAngle > 25))
		{
			const int nPctAngle=(nCurrectAngle - (nItemAngle / 2));

			CPoint pa;
			CountPoint(nPctAngle, pa, TRUE);

			CString str;
			str.Format("%.0f%%", (double)(nItemAngle)*100.0/360.0);

			const CSize sz=memDC.GetTextExtent(str);
			COLORREF		clrBack, clrText;
			pItem->GetColors(clrBack, clrText);

			memDC.SetTextColor(clrText);
			memDC.TextOut(pa.x-sz.cx/2, pa.y-sz.cy/2, str);
		}

		memDC.SelectObject(pOldFont);

		pt1 = pt2;
	}
	
	// Draw Line for the out circle
	memDC.SelectObject(&pen);
	memDC.SelectStockObject(NULL_BRUSH);
	memDC.Ellipse(m_rectChart);

	dc.BitBlt(0, 0, clientRect.Width(), clientRect.Height(), &memDC, 0, 0, SRCCOPY);

	memDC.SelectObject(pOldPen);
	memDC.SelectObject(&pOldBrush);
	memDC.SelectObject(pOldmemDCBitmap);
}

/*--------------------------------------------------------------------------*/

void CPieChartCtrl::OnSize(UINT nType, int cx, int cy) 
{
	CWnd::OnSize(nType, cx, cy);

	RecalcRect();
}

/*--------------------------------------------------------------------------*/

void CPieChartCtrl::RecalcRect ()
{
	CRect rect;
	GetClientRect(&rect);

	const int nSize=(rect.Width()>rect.Height())?rect.Height():rect.Width();
	m_rectChart = CRect(
		CPoint(rect.left + (rect.Width()-nSize)/2, rect.top + (rect.Height()-nSize)), 
		CSize(nSize, nSize)
	);
}

/*--------------------------------------------------------------------------*/

// returns index of added piece (or (-1) if error)
int CPieChartCtrl::AddPiece (COLORREF colorBack, COLORREF colorText, const int nSize, LPCTSTR lpszName, const DWORD dwData, const BOOL fRedraw)
{
	CPieChartPiece	*pItem=new CPieChartPiece(colorBack, colorText, nSize, lpszName, dwData);
	if (NULL == pItem)
		return FALSE;

	try
	{
		const int	nIndex=m_chartPieces.Add(pItem);

		if (fRedraw)
			InvalidateRect(NULL, FALSE);

		return nIndex;
	}
	catch (CMemoryException* e)
	{
		if (pItem !=NULL) 
			delete pItem;
		e->Delete();
	}

	return (-1);
}

/*--------------------------------------------------------------------------*/

// bPercent is TRUE when counting the position for the percent info
void CPieChartCtrl::CountPoint (const int nAngle, CPoint & pt, const BOOL bPercent)
{
	int	nRealAngle=nAngle;
	while (nRealAngle < 0)
		nRealAngle += 360;

	while (nRealAngle > 359)
		nRealAngle -= 360;

	const double dAngle=((double)nRealAngle) * piValue/ (double) 180;
	double r=((double)m_rectChart.Height()) / 2.0;
	if (bPercent)
		r = r * 3.0/5.0;

	const double dOffX=(r * sin(dAngle)), dOffY=(0.0 - (r * cos(dAngle)));
	const double dX = ((double)(m_rectChart.right+m_rectChart.left))/2.0;
	const double dY = ((double)(m_rectChart.top+m_rectChart.bottom))/2.0;
	
	pt.x = (int)(dX + dOffX);
	pt.y = (int)(dY + dOffY);
}

/*--------------------------------------------------------------------------*/

void CPieChartCtrl::Reset (const BOOL fRedraw)
{
	const int nCount=m_chartPieces.GetSize();

	for (int i = 0; i < nCount; i++)
	{
		CPieChartPiece	*pItem=m_chartPieces.GetAt(i);
		delete pItem;
	}

	m_chartPieces.RemoveAll();

	if (fRedraw)
		InvalidateRect(NULL, FALSE);
}

//////////////////////////////////////////////////////////////////////////////
