#include <mfc/cctrls.h>
#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////
// CHistogramCtrl

CHistogramCtrl::CHistogramCtrl()
	: CWnd(), m_nPos(0), m_nLower(0), m_nUpper(100),
	  m_ScaleBGCol(RGB(0,0,0)), m_ScaleFGCol(RGB(0,128,0)), m_SpikeCol(RGB(0,255,0))
{
}

CHistogramCtrl::~CHistogramCtrl()
{
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CHistogramCtrl, CWnd)
	ON_WM_PAINT()
	ON_WM_SIZE()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CSpikeCtrl message handlers

BOOL CHistogramCtrl::Create(DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID, CCreateContext* pContext) 
{
	static CString className=_T("");
	if (className.IsEmpty())
	{
		className = ::AfxRegisterWndClass(CS_HREDRAW | CS_VREDRAW);
#ifdef _DEBUG
		ASSERT(!IsEmptyStr(className));
#else
		if (IsEmptyStr(className))
			return FALSE;
#endif
	}

#ifdef _DEBUG
	ASSERT(pParentWnd != NULL);
#else
	if (NULL == pParentWnd)
		return FALSE;
#endif

	return CWnd::CreateEx(WS_EX_CLIENTEDGE | WS_EX_STATICEDGE, 
								 className, NULL, dwStyle, 
								 rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top,
								 pParentWnd->GetSafeHwnd(), (HMENU) nID);
}

/////////////////////////////////////////////////////////////////////////////

BOOL CHistogramCtrl::SetRange (const UINT nLower, const UINT nUpper, const BOOL fInvalidate)
{
	if (nUpper <= nLower)
		return FALSE;

	m_nLower = nLower;
	m_nUpper = nUpper;

	if (fInvalidate)
		InvalidateCtrl();
	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CHistogramCtrl::RedrawScale (CClientDC&	dc, const CRect& rcClient)
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
	CBrush bkBrush(HS_HORIZONTAL,m_ScaleFGCol);
	m_MemDC.FillRect(rcClient,&bkBrush);

	return TRUE;
}

BOOL CHistogramCtrl::RedrawScale ()
{
	CRect rcClient;
	GetClientRect(rcClient);
	CClientDC	dc(this);
	return RedrawScale(dc, rcClient);
}

/////////////////////////////////////////////////////////////////////////////

void CHistogramCtrl::InvalidateCtrl()
{
	CRect rcClient;
	GetClientRect(rcClient);

	if (m_MemDC.GetSafeHdc() == NULL)
	{
		CClientDC dc(this);
		VERIFY(m_MemDC.CreateCompatibleDC(&dc));
		VERIFY(RedrawScale(dc, rcClient));
	}

	InvalidateRect(rcClient);
}

/////////////////////////////////////////////////////////////////////////////

UINT CHistogramCtrl::SetPos (const UINT nPos, const BOOL fRedraw)
{
	UINT	effPos=nPos;
	if (effPos > m_nUpper)
		effPos = m_nUpper;

	if (effPos < m_nLower)
		effPos = m_nLower;

	const UINT nOld=m_nPos;
	m_nPos = effPos;

	if (fRedraw)
	{
		DrawSpike();
		Invalidate();
	}

	return nOld;
}

/////////////////////////////////////////////////////////////////////////////

void CHistogramCtrl::OnPaint() 
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

void CHistogramCtrl::OnSize (UINT nType, int cx, int cy) 
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

void CHistogramCtrl::DrawSpike ()
{
	if (NULL == m_MemDC.GetSafeHdc())
		return;

	CRect rcClient;
	GetClientRect(rcClient);

	m_MemDC.BitBlt(0, 0, rcClient.Width(), rcClient.Height(), &m_MemDC, 4, 0, SRCCOPY);

	const UINT  nRange=max(1, (m_nUpper - m_nLower));
	CRect rcTop(rcClient.right - 4, 0, rcClient.right - 2, rcClient.bottom);
	rcTop.top  = (long) (((float) (m_nPos - m_nLower) / nRange) * rcClient.Height());
	rcTop.top  = rcClient.bottom - rcTop.top;

	// draw scale
	CRect rcRight = rcClient;
	rcRight.left = rcRight.right - 4;
	m_MemDC.SetBkColor(m_ScaleBGCol);

   CBrush bkBrush(HS_HORIZONTAL, m_ScaleFGCol);  
   m_MemDC.FillRect(rcRight,&bkBrush);

	// draw current spike
	CBrush brush(m_SpikeCol);
	m_MemDC.FillRect(rcTop, &brush);
}

/////////////////////////////////////////////////////////////////////////////
