#include <mfc/listctrl.h>

/////////////////////////////////////////////////////////////////////////////
  
// mark selected row even if click not on 1st column
void CMFCUtlListCtrl::OnLButtonDown (UINT nFlags, CPoint point) 
{
	CListCtrl::OnLButtonDown(nFlags, point);

	// Before calling HitTest(), the x coordinate is changed to 2.
	// This forces the point being tested to fall on the first column.
	// A value of x below 2 fails (it is presumably occupied by the border ). 
	//
	// Note: this code fails if 1st column is not visible
	point.x = 2;
   int nItem=HitTestEx(point);
	if (nItem != (-1))
	{
		if (!SetItemState(nItem, LVIS_SELECTED | LVIS_FOCUSED,  LVIS_SELECTED | LVIS_FOCUSED))
			TRACE(_T("\tCMFCUtlListCtrl::OnLButtonDown() - cannot mark item %d !!!\n"));
	}
}

/////////////////////////////////////////////////////////////////////////////
