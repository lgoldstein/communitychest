// CMFCUtlSplitterBar.cpp : implementation file
//
/////////////////////////////////////////////////////////////////////////////

#include <mfc/splitters.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CMFCUtlSplitterBar

IMPLEMENT_DYNAMIC(CMFCUtlSplitterBar, CWnd)

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CMFCUtlSplitterBar, CWnd)
	//{{AFX_MSG_MAP(CMFCUtlSplitterBar)
	ON_WM_PAINT()
	ON_WM_NCHITTEST()
	ON_WM_CREATE()
	ON_WM_SETCURSOR()
	ON_WM_MOUSEMOVE()
	ON_WM_LBUTTONDOWN()
	ON_WM_LBUTTONUP()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////

CMFCUtlSplitterBar::CMFCUtlSplitterBar()
{
	m_cxLeftMost = (-1);
	m_cxRightMost = (-1);
	m_bDragging = FALSE;
	m_pwndLeftPane = NULL;
	m_pwndRightPane = NULL;
}

/////////////////////////////////////////////////////////////////////////////

CMFCUtlSplitterBar::~CMFCUtlSplitterBar()
{
}

/////////////////////////////////////////////////////////////////////////////

BOOL CMFCUtlSplitterBar::Create (CWnd* pParentWnd, DWORD dwStyle, LPRECT pRect, UINT nID, BOOL bHorizontal)
{
	m_bHorizontal=bHorizontal;

	CRect		rect(0,0,0,0);
	LPRECT	lpRct=((NULL == pRect) ? &rect : pRect);
	CWnd		*pWnd=this;

	return pWnd->Create(NULL, _T(""), dwStyle, *lpRct, pParentWnd, nID);
}

/////////////////////////////////////////////////////////////////////////////
// CMFCUtlSplitterBar message handlers

void CMFCUtlSplitterBar::OnPaint() 
{
	RECT rc;
	if (!GetUpdateRect(&rc))
		return;

	PAINTSTRUCT paint;
	CDC *pDC=BeginPaint(&paint);
#ifdef _DEBUG
	ASSERT(pDC != NULL);
#else
	if (NULL == pDC)
		return;
#endif

	CRect rect;
	GetClientRect(rect);
	pDC->Draw3dRect(&rect,
					    ::GetSysColor(COLOR_BTNHIGHLIGHT),
					    ::GetSysColor(COLOR_BTNSHADOW));
	EndPaint(&paint);
}

/////////////////////////////////////////////////////////////////////////////

UINT CMFCUtlSplitterBar::OnNcHitTest(CPoint point) 
{	
	return HTCLIENT;
}

/////////////////////////////////////////////////////////////////////////////

int CMFCUtlSplitterBar::OnCreate(LPCREATESTRUCT lpCreateStruct) 
{
	if (CWnd::OnCreate(lpCreateStruct) == (-1))
		return (-1);
	
	GetWindowRect(&m_rectSplitter);
	SetWindowPos(&CWnd::wndTop,0,0,0,0,SWP_NOMOVE|SWP_NOSIZE);

	//Initialize left most and right most coordinator
	CWnd	*pParent=GetParent();
#ifdef _DEBUG
	ASSERT(pParent != NULL);
#else
	if (NULL == pParent)
		return (-1);
#endif

	CRect rectParent;
	pParent->GetClientRect(rectParent);
	if(m_bHorizontal)
	{
		m_cxLeftMost=rectParent.top;
		m_cxRightMost=rectParent.bottom;
	}
	else
	{
		m_cxLeftMost=rectParent.left;
		m_cxRightMost=rectParent.right;
	}

	return 0;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CMFCUtlSplitterBar::OnSetCursor(CWnd* pWnd, UINT nHitTest, UINT message) 
{
	CPoint ptCursor=GetMessagePos();		
	if(IsCursorOverSplitter(ptCursor))
	{
		::SetCursor(AfxGetApp()->LoadCursor(m_bHorizontal?AFX_IDC_VSPLITBAR:AFX_IDC_HSPLITBAR));	
		return TRUE;
	}

	return CWnd::OnSetCursor(pWnd, nHitTest, message);
}

/////////////////////////////////////////////////////////////////////////////

BOOL CMFCUtlSplitterBar::IsCursorOverSplitter( const CPoint& ptCursor )
{
	CRect rectSplitter;
	GetWindowRect(rectSplitter);
	return rectSplitter.PtInRect( ptCursor );
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::OnMouseMove(UINT nFlags, CPoint point) 
{
	if (((nFlags & MK_LBUTTON) != 0) && m_bDragging)
	{
		DrawDraggingBar(point);
		return;
	}

	CWnd::OnMouseMove(nFlags, point);
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::OnLButtonDown(UINT nFlags, CPoint point) 
{	
	ClientToScreen(&point);

	if (IsCursorOverSplitter(point))
	{
		SetCapture();
		m_bDragging=TRUE;
		GetWindowRect(&m_rectSplitter);		
		ScreenToClient(&point);
		DrawDraggingBar(point,DRAG_ENTER);
		return;
	}
	
	CWnd::OnLButtonDown(nFlags, point);
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::OnLButtonUp(UINT nFlags, CPoint point) 
{
	CWnd	*pParent=GetParent();
	ASSERT(pParent != NULL);

	if (m_bDragging && (pParent != NULL))
	{
		DrawDraggingBar(point,DRAG_EXIT);
		//Move the splitter here
		ClientToScreen(&point);

		//with in client area?
		if(m_bHorizontal)
		{
			CPoint pointLeftMost;
			pointLeftMost.y=m_cxLeftMost;
			pParent->ClientToScreen(&pointLeftMost);
			CPoint pointRightMost;
			pointRightMost.y=m_cxRightMost;
			pParent->ClientToScreen(&pointRightMost);

			if(point.y < pointLeftMost.y)
				point.y=pointLeftMost.y;
			if(point.y > pointRightMost.y)
				point.y=pointRightMost.y;

			m_rectDragCurt=m_rectSplitter;
			m_rectDragCurt.top=point.y;
			m_rectDragCurt.bottom=point.y+m_rectSplitter.Height();
		}
		else
		{
			CPoint pointLeftMost;
			pointLeftMost.x=m_cxLeftMost;
			pParent->ClientToScreen(&pointLeftMost);
			CPoint pointRightMost;
			pointRightMost.x=m_cxRightMost;
			pParent->ClientToScreen(&pointRightMost);

			if(point.x < pointLeftMost.x)
				point.x=pointLeftMost.x;
			if(point.x > pointRightMost.x)
				point.x=pointRightMost.x;

			m_rectDragCurt=m_rectSplitter;
			m_rectDragCurt.left=point.x;
			m_rectDragCurt.right=point.x+m_rectSplitter.Width();
		}

		pParent->ScreenToClient(m_rectDragCurt);
		MoveWindow(m_rectDragCurt,TRUE);
		OnPaint();

		ReleaseCapture();
		m_bDragging=FALSE;
		MovePanes();
		pParent->SendMessage(WMMFCUTL_SPLITTER_MOVED,0,0L);
	}

	CWnd::OnLButtonUp(nFlags, point);
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::DrawDraggingBar(CPoint point,DRAGFLAG df)
{
	ClientToScreen(&point);
	m_rectDragCurt=m_rectSplitter;
	if(m_bHorizontal)
	{
		m_rectDragCurt.top=point.y;
		m_rectDragCurt.bottom=point.y+m_rectSplitter.Height();
	}
	else
	{
		m_rectDragCurt.left=point.x;
		m_rectDragCurt.right=point.x+m_rectSplitter.Width();
	}

	CSize size(m_rectDragCurt.Width(),m_rectDragCurt.Height());

	CWnd *pParent=GetParent();
#ifdef _DEBUG
	ASSERT(pParent != NULL);
#else
	if (NULL == pParent)
		return;
#endif

	CDC *pDC=pParent->GetDC();
#ifdef _DEBUG
	ASSERT(pDC != NULL);
#else
	if (NULL == pDC)
		return;
#endif

	pParent->ScreenToClient(m_rectDragCurt);
	switch(df)
	{
		case DRAG_ENTER:
			 pDC->DrawDragRect(m_rectDragCurt,size,NULL,size);
			 break;
		case DRAG_EXIT:	//fall through
		default:
			pDC->DrawDragRect(m_rectDragCurt,size,m_rectDragPrev,size);
			break;
	}

	pParent->ReleaseDC(pDC);
	m_rectDragPrev=m_rectDragCurt;
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::SetPanes (CWnd& wndLeftPane, CWnd& wndRightPane)
{
	CWnd	*pParent=GetParent();
#ifdef _DEBUG
	ASSERT(pParent != NULL);
#else
	if (NULL == pParent)
		return;
#endif

	m_pwndLeftPane = &wndLeftPane;
	m_pwndRightPane = &wndRightPane;

	//Initialize splitter bar position & size
	CRect rectBar;
	m_pwndLeftPane->GetWindowRect(rectBar);
	pParent->ScreenToClient(rectBar);

	int nBarWidth=0;
	if (m_bHorizontal)
	{
		nBarWidth=GetSystemMetrics(SM_CXFRAME);
		rectBar.top=rectBar.bottom;
		rectBar.top-=nBarWidth/2;
		rectBar.bottom+=nBarWidth/2;
	}
	else
	{
		nBarWidth=GetSystemMetrics(SM_CYFRAME);
		rectBar.left=rectBar.right;
		rectBar.left-=nBarWidth/2;
		rectBar.right+=nBarWidth/2;
	}

	//repostion top/left & bottom/right panes
	MoveWindow(rectBar);
	MovePanes();

	//calculate top/left most and bottom/right most coordinator
	CRect rectLeft;
	m_pwndLeftPane->GetWindowRect(rectLeft);
	pParent->ScreenToClient(rectLeft);

	CRect rectRight;
	m_pwndRightPane->GetWindowRect(rectRight);
	pParent->ScreenToClient(rectRight);

	if (m_bHorizontal)
	{
		m_cxLeftMost=rectLeft.top;
		m_cxRightMost=rectRight.bottom;
	}
	else
	{
		m_cxLeftMost=rectLeft.left;
		m_cxRightMost=rectRight.right;
	}
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlSplitterBar::MovePanes()
{
#ifdef _DEBUG
	ASSERT(m_pwndLeftPane != NULL);
	ASSERT(m_pwndRightPane != NULL);
#else
	if ((NULL == m_pwndLeftPane) || (NULL == m_pwndRightPane))
		return;
#endif

	CWnd	*pParent=GetParent();
#ifdef _DEBUG
	ASSERT(pParent != NULL);
#else
	if (NULL == pParent)
		return;
#endif

	//Get position of the splitter bar
	CRect rectBar;
	GetWindowRect(rectBar);
	pParent->ScreenToClient(rectBar);

	CRect rectLeft;
	m_pwndLeftPane->GetWindowRect(rectLeft);
	pParent->ScreenToClient(rectLeft);

	CRect rectRight;
	m_pwndRightPane->GetWindowRect(rectRight);
	pParent->ScreenToClient(rectRight);

	if (m_bHorizontal)
	{
		//reposition top pane
		rectLeft.bottom=rectBar.top+GetSystemMetrics(SM_CXBORDER);

		//reposition bottom pane
		rectRight.top=rectBar.bottom-GetSystemMetrics(SM_CXBORDER);;
	}
	else
	{
		//reposition left pane
		rectLeft.right=rectBar.left+GetSystemMetrics(SM_CYBORDER);

		//reposition right pane
		rectRight.left=rectBar.right-GetSystemMetrics(SM_CYBORDER);;
	}

	m_pwndLeftPane->MoveWindow(rectLeft);
	m_pwndRightPane->MoveWindow(rectRight);

	//repaint client area
	pParent->Invalidate();
}

/////////////////////////////////////////////////////////////////////////////
