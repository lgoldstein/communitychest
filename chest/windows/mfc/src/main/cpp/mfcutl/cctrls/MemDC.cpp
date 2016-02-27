#include <mfc/cctrls.h>

// contributed by Matt Weagle 18-May-1998 to http://www.codeguru.com in
// the "Gradient Fill Progress Control" article source code
//////////////////////////////////////////////////////////////////////////////

CMemDC::CMemDC (CDC* pDC)
 : CDC(), m_pDC(pDC), m_pOldBitmap(NULL), m_pThis(NULL)
{
#ifdef _DEBUG
	ASSERT(m_pDC != NULL);
#else
	if (NULL == m_pDC)
		throw ERROR_BAD_ENVIRONMENT;
#endif
	m_bMemDC = !(m_pDC->IsPrinting());

	if (m_bMemDC)	// Create a Memory DC
	{
		int	nRes=m_pDC->GetClipBox(&m_rect);
		ASSERT(nRes != ERROR);

		VERIFY(CreateCompatibleDC(m_pDC));
		VERIFY(m_bitmap.CreateCompatibleBitmap(m_pDC, m_rect.Width(), m_rect.Height()));
		m_pOldBitmap = SelectObject(&m_bitmap);

		CPoint	prevPt=SetWindowOrg(m_rect.left, m_rect.top);
	}
	else		// Make a copy of the relevent parts of the current DC for printing
	{
		m_bPrinting = m_pDC->m_bPrinting;
		m_hDC		= m_pDC->m_hDC;
		m_hAttribDC = m_pDC->m_hAttribDC;
	}

	m_pThis = this;
}

//////////////////////////////////////////////////////////////////////////////

CMemDC::~CMemDC ()
{
	if (m_bMemDC)
	{
		// Copy the offscreen bitmap onto the screen.
#ifdef _DEBUG
		ASSERT(m_pDC != NULL);
#else
		if (m_pDC != NULL)
#endif
			m_pDC->BitBlt(m_rect.left, m_rect.top, m_rect.Width(), m_rect.Height(), m_pThis, m_rect.left, m_rect.top, SRCCOPY);

		//Swap back the original bitmap.
		SelectObject(m_pOldBitmap);
	}
	else
	{
		// All we need to do is replace the DC with an illegal value,
		// this keeps us from accidently deleting the handles associated with
		// the CDC that was passed to the constructor.
		m_hDC = NULL;
		m_hAttribDC = NULL;
	}
}

//////////////////////////////////////////////////////////////////////////////
