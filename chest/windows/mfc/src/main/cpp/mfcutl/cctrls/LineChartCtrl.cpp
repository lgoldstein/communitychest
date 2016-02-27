#include <mfc/cctrls.h>

// LineChartCtrl.cpp : implementation file
//
// Written by Yuheng Zhao (yuheng@ministars.com) 
// http://www.ministars.com
// The original idea and part of the code from Ken C. Len's CHistogramCtrl
// http://www.codeguru.com/controls/histogram_control.shtml
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

/////////////////////////////////////////////////////////////////////////////
// CLineChartCtrl

LONG	CLineChartCtrl::lRegisterCount=0;

CLineChartCtrl::CLineChartCtrl()
	: CWnd(), m_ScaleBGCol(RGB(0,0,0)), m_ScaleFGCol(RGB(0,128,0))
{
	LONG	lPrev=::InterlockedIncrement(&lRegisterCount);
	if (1 == lPrev)
	{
		VERIFY(CLineChartCtrl::RegisterWndClass(AfxGetInstanceHandle()));

		// add one more since we decrement it upon destruction
		lPrev = ::InterlockedIncrement(&lRegisterCount);
	}
}

void CLineChartCtrl::RemoveAll ()
{
	const int nCount=m_items.GetSize();
	
	for (int i = 0; i < nCount; i++)
	{
		CLineChartItem	*pItem=m_items.GetAt(i);
#ifdef _DEBUG
		ASSERT(pItem != NULL);
#else
		if (pItem != NULL)
#endif
			delete pItem;
	}

	m_items.RemoveAll();
}

CLineChartCtrl::~CLineChartCtrl()
{
	RemoveAll();

	LONG	lPrev=::InterlockedDecrement(&lRegisterCount);
	ASSERT(lPrev != 0);
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CLineChartCtrl, CWnd)
	ON_WM_PAINT()
	ON_WM_SIZE()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////

BOOL CLineChartCtrl::RegisterWndClass(HINSTANCE hInstance)
{
	WNDCLASS wc;
	memset(&wc, 0, (sizeof wc));

	wc.lpszClassName = _T("LINE_CHART_CUSTOM_CTRL"); // matches class name in custom control
	wc.hInstance = hInstance;
	wc.lpfnWndProc = ::DefWindowProc;
	wc.hCursor = ::LoadCursor(NULL, IDC_ARROW);
	wc.hIcon = 0;
	wc.lpszMenuName = NULL;
	wc.hbrBackground = (HBRUSH) ::GetStockObject(LTGRAY_BRUSH);
	wc.style = CS_GLOBALCLASS; // To be modified
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;

	return (::RegisterClass(&wc) != 0);
}

/////////////////////////////////////////////////////////////////////////////

BOOL CLineChartCtrl::RedrawScale (CClientDC&	dc, const CRect& rcClient)
{
	if (m_Bitmap.GetSafeHandle() != NULL)
	{
		if (!m_Bitmap.DeleteObject())
			return FALSE;
	}

	if (!m_Bitmap.CreateCompatibleBitmap(&dc, rcClient.Width(), rcClient.Height()))
		return FALSE;

	if (NULL == m_MemDC.SelectObject(m_Bitmap))
		return FALSE;
		
	// draw scale
	m_MemDC.SetBkColor(m_ScaleBGCol);
	CBrush bkBrush(HS_CROSS,m_ScaleFGCol);
	m_MemDC.FillRect(rcClient,&bkBrush);

	return TRUE;
}

BOOL CLineChartCtrl::RedrawScale ()
{
	CRect rcClient;
	GetClientRect(rcClient);
	CClientDC	dc(this);
	return RedrawScale(dc, rcClient);
}

/////////////////////////////////////////////////////////////////////////////

void CLineChartCtrl::InvalidateCtrl()
{
	CRect rcClient;
	GetClientRect(rcClient);

	if (m_MemDC.GetSafeHdc() == NULL)
	{
		CClientDC dc(this);
		VERIFY(m_MemDC.CreateCompatibleDC(&dc));
		VERIFY(RedrawScale(dc, rcClient));
	}

	InvalidateRect(rcClient, FALSE);
}

/////////////////////////////////////////////////////////////////////////////

// Note: if callback is supplied then "SetPos" has no effect
UINT CLineChartCtrl::SetPos(const int nIndex, const UINT nPos)
{
	if (nIndex >= m_items.GetSize())
		return 0;

	CLineChartItem	*pItem=m_items.GetAt(nIndex);
#ifdef _DEBUG
	ASSERT(pItem != NULL);
#else
	if (NULL == pItem)
		return 0;
#endif

	LINECHARTITEMPOSCFN lpfnPcfn=pItem->m_lpfnPcfn;
	if (NULL == lpfnPcfn)
	{
		pItem->m_nOldPos = pItem->m_nPos;
		pItem->m_nPos = nPos;
		return pItem->m_nOldPos;
	}

	return 0;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CLineChartCtrl::SetRange (const int nIndex, const UINT nLower, const UINT nUpper, const BOOL fInvalidate)
{
	if (nIndex >= m_items.GetSize())
		return FALSE;

	ASSERT(nLower < nUpper);
	CLineChartItem	*pItem=m_items.GetAt(nIndex);
#ifdef _DEBUG
	ASSERT(pItem != NULL);
#else
	if (NULL == pItem)
		return FALSE;
#endif

	pItem->m_nLower = nLower;
	pItem->m_nUpper = nUpper;

	if (fInvalidate)
		InvalidateCtrl();

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

void CLineChartCtrl::OnPaint() 
{
	if (m_MemDC.GetSafeHdc() != NULL)
	{
		CPaintDC dc(this); // device context for painting
		CRect rcClient;
		GetClientRect(rcClient);
		dc.BitBlt(0, 0, rcClient.Width(), rcClient.Height(), &m_MemDC, 0, 0, SRCCOPY);
	}
}

/////////////////////////////////////////////////////////////////////////////

void CLineChartCtrl::OnSize (UINT nType, int cx, int cy) 
{
	CWnd::OnSize(nType, cx, cy);

	if (m_MemDC.GetSafeHdc() != NULL)
	{
		VERIFY(RedrawScale());

		Invalidate();
		UpdateWindow();
	}
}

/////////////////////////////////////////////////////////////////////////////

void CLineChartCtrl::DrawSpike()
{
	if (NULL == m_MemDC.GetSafeHdc())
		return;

	CRect rcClient;
	GetClientRect(rcClient);
	m_MemDC.BitBlt(0, 0, rcClient.Width(), rcClient.Height(), &m_MemDC, 4, 0, SRCCOPY);

	// draw scale
	CRect rcRight = rcClient;
	rcRight.left = rcRight.right - 4;
	m_MemDC.SetBkColor(m_ScaleBGCol);

	CBrush bkBrush(HS_HORIZONTAL,m_ScaleFGCol);  
	m_MemDC.FillRect(rcRight,&bkBrush);

	static BOOL bDrawVerticle = FALSE;
	bDrawVerticle = !bDrawVerticle;

	// Draw Verticle lines only every two steps
	if (bDrawVerticle)
	{
		CPen pen(PS_SOLID, 1, m_ScaleFGCol);
		CPen* pOldPen = m_MemDC.SelectObject(&pen);
		m_MemDC.MoveTo(CPoint(rcClient.right-2, rcClient.top));
		m_MemDC.LineTo(CPoint(rcClient.right-2, rcClient.bottom));
		m_MemDC.SelectObject(pOldPen);
	}

	int nCount = m_items.GetSize();
	CPoint ptOld, ptNew;
	const UINT	uRcHeight=rcRight.Height();
	for (int i=0; i < nCount; i++)
	{
		CLineChartItem	*pItem=m_items.GetAt(i);
#ifdef _DEBUG
		ASSERT(pItem != NULL);
#else
		if (NULL == pItem)
			continue;
#endif

		LINECHARTITEMPOSCFN lpfnPcfn=pItem->m_lpfnPcfn;
		if (lpfnPcfn != NULL)
		{
			pItem->m_nOldPos = pItem->m_nPos;
			pItem->m_nPos = (*lpfnPcfn)(pItem);
		}

		if (pItem->m_nOldPos > pItem->m_nUpper)
			pItem->m_nOldPos = pItem->m_nUpper;
		if (pItem->m_nOldPos < pItem->m_nLower)
			pItem->m_nOldPos = pItem->m_nLower;
		if (pItem->m_nPos > pItem->m_nUpper)
			pItem->m_nPos = pItem->m_nUpper;
		if (pItem->m_nPos < pItem->m_nLower)
			pItem->m_nPos = pItem->m_nLower;

		const UINT  nRange=(pItem->m_nUpper - pItem->m_nLower);
		ptOld.x = rcRight.left-1; // Minus one to make sure to draw inside the area
		ptNew.x = rcRight.right-1;
		const UINT	uOldDiff=(pItem->m_nUpper - pItem->m_nOldPos);
		ptOld.y = (int) (((float) uOldDiff * (float) uRcHeight) / (float) nRange);
		const UINT	uNewDiff=(pItem->m_nUpper - pItem->m_nPos);
		ptNew.y = (int) (((float) uNewDiff * (float) uRcHeight) / (float) nRange);

		CPen pen(PS_SOLID, 1, pItem->m_colorLine);
		CPen	*pOldPen=m_MemDC.SelectObject(&pen);
		ASSERT(pOldPen != NULL);
		m_MemDC.MoveTo(ptOld);
		m_MemDC.LineTo(ptNew);
		m_MemDC.SelectObject(pOldPen);
	}
}

/////////////////////////////////////////////////////////////////////////////

// returns index of added item (or (-1))
int CLineChartCtrl::Add(const COLORREF lcolor, const UINT nLower, const UINT nUpper,
								const DWORD dwItemData, LINECHARTITEMPOSCFN lpfnPcfn)
{
	ASSERT(nLower < nUpper);
	CLineChartItem	*pItem=new CLineChartItem(lcolor, nUpper, nLower, dwItemData, lpfnPcfn);
#ifdef _DEBUG
	ASSERT(pItem != NULL);
#else
	if (NULL == pItem)
		return (-1);
#endif

	try 
	{
		const int	nIndex=m_items.Add(pItem);

		InvalidateCtrl();
		return nIndex;
	}
	catch (CMemoryException* e)
	{
		if (pItem !=NULL) 
			delete pItem;
		e->Delete();
		return (-1);
	}
}

/////////////////////////////////////////////////////////////////////////////

BOOL CLineChartCtrl::SetItemData (const int nIndex, const DWORD dwItemData, LINECHARTITEMPOSCFN lpfnPcfn)
{
	if (nIndex >= m_items.GetSize())
		return FALSE;

	CLineChartItem	*pItem=m_items.GetAt(nIndex);
#ifdef _DEBUG
	ASSERT(pItem != NULL);
#else
	if (NULL == pItem)
		return FALSE;
#endif

	pItem->m_dwItemData = dwItemData;
	pItem->m_lpfnPcfn = lpfnPcfn;

	return TRUE;
}

DWORD CLineChartCtrl::GetItemData (const int nIndex) const
{
	if (nIndex >= m_items.GetSize())
		return ((DWORD) (-1));

	CLineChartItem	*pItem=m_items.GetAt(nIndex);
#ifdef _DEBUG
	ASSERT(pItem != NULL);
#else
	if (NULL == pItem)
		return ((DWORD) (-1));
#endif

	return pItem->m_dwItemData;
}

/////////////////////////////////////////////////////////////////////////////

void CLineChartCtrl::Go()
{
	DrawSpike();

	Invalidate(FALSE);
}

/////////////////////////////////////////////////////////////////////////////
