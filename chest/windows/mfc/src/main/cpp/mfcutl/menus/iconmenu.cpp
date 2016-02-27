#include <mfc/menus.h>
#include <util/math.h>

//////////////////////////////////////////////////////////////////////////////

CIconizedMenu::CIconizedMenu (const IMLXL ilx[])
	: CMenu(), m_imgList(), m_iconsMap(), m_pApp(NULL)
{
	VERIFY((m_iconX=::GetSystemMetrics(SM_CXMENUCHECK)) != 0);
	VERIFY((m_iconY=::GetSystemMetrics(SM_CYMENUCHECK)) != 0);

	if (ilx != NULL)
	{
		HRESULT	hr=SetIconsMap(ilx);
		ASSERT(S_OK == hr);
	}
}

/*--------------------------------------------------------------------------*/

static ULONG CountILXs (const IMLXL ilx[])
{
	if (NULL == ilx)
		return 0;

	ULONG	ulCount=0;
	for (; ((LONG) ilx[ulCount].nMenuCmd) > 0; ulCount++);
	return ulCount;
}

/*--------------------------------------------------------------------------*/

HRESULT CIconizedMenu::SetIconsMap (const IMLXL ilx[])
{
	const ULONG	ulXCount=CountILXs(ilx);
	if (NULL == ulXCount)
		return ERROR_BAD_ARGUMENTS;

	// use first call to create the icons map and images list
	if (NULL == m_pApp)
	{
		if (NULL == (m_pApp=AfxGetApp()))
			return ERROR_BAD_ENVIRONMENT;

		const int	ulLrgCX=::GetSystemMetrics(SM_CXICON), ulLrgCY=::GetSystemMetrics(SM_CYICON);
		const int	ulSmlCX=(ulLrgCX / 2), ulSmlCY=(ulLrgCY / 2);

		if (!m_imgList.Create(ulSmlCX, ulSmlCY, (ILC_COLOR | ILC_MASK), (int) ulXCount, max((int) (ulXCount / 2), 5)))
			return ERROR_INVALID_FUNCTION;

		m_iconsMap.InitHashTable((UINT) FindClosestPrime((UINT32) ulXCount));
	}

	for (ULONG	ulXdx=0; ulXdx < ulXCount; ulXdx++)
	{
		const IMLXL&	xl=ilx[ulXdx];
		if (xl.nIconID <= 0)	// skip commands without icons
			continue;

		HICON	hIcon=m_pApp->LoadIcon(xl.nIconID);
		if (NULL == hIcon)
			return ERROR_SECTOR_NOT_FOUND;

		const int	nIconNdx=m_imgList.Add(hIcon);
		if ((-1) == nIconNdx)
			return ERROR_OUTOFMEMORY;

		VERIFY(ModifyMenu(xl.nMenuCmd, MF_BYCOMMAND | MF_OWNERDRAW, xl.nMenuCmd));

		m_iconsMap.SetAt(xl.nMenuCmd, nIconNdx);
	}

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

void CIconizedMenu::MeasureItem (LPMEASUREITEMSTRUCT lpMIS)
{
#ifdef _DEBUG
	ASSERT(lpMIS != NULL);
#else
	if (NULL == lpMIS)
		return;
#endif

/*
	ASSERT(m_pApp != NULL);
	CWnd	*pMainWnd=m_pApp->m_pMainWnd;

	ASSERT(pMainWnd != NULL);
	CDC	*pDC=pMainWnd->GetDC();

	ASSERT(pDC != NULL);
	CFont	*pMainFont=pMainWnd->GetFont();

	ASSERT(pMainFont != NULL);
	CFont	*pFont=pDC->SelectObject(pMainFont);

	TEXTMETRIC tm;
	pDC->GetTextMetrics (&tm);
	pDC->SelectObject(pFont);
	pMainWnd->ReleaseDC(pDC);

	lpMIS->itemWidth = m_iconX + tm.tmAveCharWidth *  lstrlen(((MENUDATA*)(lpMIS->itemData))->menuText) + 10;
	lpMIS->itemHeight = (m_iconY > (m_iMenuHeight+1)) ? m_iconY : (m_iMenuHeight + 1);
	*/

	lpMIS->itemWidth = m_iconX;
	lpMIS->itemHeight = m_iconY;
}

//////////////////////////////////////////////////////////////////////////////

void CIconizedMenu::DrawItem (LPDRAWITEMSTRUCT lpDIS)
{
	ASSERT(lpDIS != NULL);
	CRect rect(&lpDIS->rcItem);

	CDC	*pDC=CDC::FromHandle(lpDIS->hDC);
	ASSERT(pDC != NULL);

	int	nIconNdx=(-1);
	if (!m_iconsMap.Lookup(lpDIS->itemID, nIconNdx))
	{
		ASSERT(nIconNdx >= 0);

		HICON	hI=m_imgList.ExtractIcon(nIconNdx);
		ASSERT(hI != NULL);

		VERIFY(DrawIconEx(pDC->GetSafeHdc(), rect.left, rect.top, hI, m_iconX, m_iconY, 0, NULL, DI_NORMAL));
	}
}

//////////////////////////////////////////////////////////////////////////////

CIconizedMenu::~CIconizedMenu ()
{
}

//////////////////////////////////////////////////////////////////////////////
