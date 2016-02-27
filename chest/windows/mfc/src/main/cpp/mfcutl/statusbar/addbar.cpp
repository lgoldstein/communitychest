#include <mfc/statusbar.h>

/*--------------------------------------------------------------------------*/

HRESULT AddStatusBar (CWnd&				parentWnd,
							 CStatusBarCtrl&	stBar,
							 const int			stBarID,
							 const UINT			nWidths[],	// percentage (last entry == 0)
							 const int			nMinHeight,	// use font as height
							 const DWORD		dwStyle)
{
	if ((NULL == nWidths) || (stBarID <= 0))
		return ERROR_BAD_ARGUMENTS;

	// find number of panes
	const int	MAX_STBAR_PANES=32;
	UINT	nWNum=0, totWidthPercent=0;
	for (nWNum=0; nWidths[nWNum] != 0; nWNum++)
		totWidthPercent += nWidths[nWNum];

	// make sure we have some panes and no more than 100%
	if ((0 == nWNum) || (nWNum > MAX_STBAR_PANES) ||
		 (0 == totWidthPercent) || (totWidthPercent > 100))
		return ERROR_BAD_ARGUMENTS;

	CRect rWin;
	parentWnd.GetWindowRect(&rWin);

	int	nStBarHeight=0;
	if (nMinHeight <= 0)
	{
		CFont	*pFont=parentWnd.GetFont();
		if (NULL == pFont)
			return ERROR_BAD_ENVIRONMENT;

		LOGFONT	lfi;
		memset(&lfi, 0, (sizeof lfi));
		if (!pFont->GetLogFont(&lfi))
			return ERROR_INVALID_FUNCTION;

		const LONG	lfHeight=lfi.lfHeight;

		return (-7);
	}
	else
	{
		nStBarHeight = nMinHeight;
	}

	rWin.bottom += nStBarHeight;
	parentWnd.MoveWindow(&rWin, FALSE);

	// create a status bar to display relay progress
	CRect rect(rWin);
	rect.top = rect.bottom - nStBarHeight;
	if (!stBar.Create(dwStyle, rect, &parentWnd, stBarID))
		return ERROR_INVALID_FUNCTION;

	stBar.SetMinHeight(nStBarHeight);

	// now define the panes
	const	int	nTotWidth=(rWin.right - rWin.left);
	int	nPanesW[MAX_STBAR_PANES], nRemWidth=nTotWidth;
	memset(nPanesW, 0, (sizeof nPanesW));

	for (UINT	nPdx=0; nPdx < nWNum; nPdx++)
	{
		int	nPaneWidth=((nTotWidth * nWidths[nPdx]) / 100);

		if (nPaneWidth > nRemWidth)
		{
			if (nPdx >= (nWNum-1))
				return ERROR_BUFFER_OVERFLOW;
		}

		if (nPdx < (nWNum-1))
		{
			nPanesW[nPdx] = nPaneWidth;
			nRemWidth -= nPaneWidth;
		}
		else
			nPanesW[nPdx] = (-1);	// take remaining space
	}

	if (!stBar.SetParts(nWNum, nPanesW))
		return ERROR_INVALID_FUNCTION;

	return S_OK;
}

/*--------------------------------------------------------------------------*/

HRESULT AttachStatusBarProgress (CStatusBarCtrl&	stBar,
											CProgressCtrl&		progCtrl,
											const int			nPaneIndex,
											const int			nProgID,
											const DWORD			dwStyle)
{
	CRect	progRect;
	if (!stBar.GetRect(nPaneIndex, &progRect))
		return ERROR_INVALID_FUNCTION;

	if (!progCtrl.Create(dwStyle, progRect, &stBar, nProgID))
		return ERROR_INVALID_FUNCTION;

	return S_OK;
}

/*--------------------------------------------------------------------------*/
