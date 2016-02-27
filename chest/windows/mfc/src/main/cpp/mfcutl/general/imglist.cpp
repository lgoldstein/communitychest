#include <mfc/imglist.h>

/*---------------------------------------------------------------------------*/

HRESULT PopulateImgList (CImageList&	imgList,
								 CWinApp&		app,
								 IMGLISTASSOC	ima[])	// last entry has (-1) icon ID
{
	if (NULL == ima)
		return ERROR_BAD_ARGUMENTS;

	for (int i=0; ; i++)
	{
		IMGLISTASSOC&	fa=ima[i];
		if (fa.nIconID <= 0)
			break;

		HICON	hIcon=app.LoadIcon(fa.nIconID);
		if (NULL == hIcon)
		{
			TRACE(_T("\tPopulateImgList - cannot load icon %d !!!\n"), fa.nIconID);
			return ERROR_SECTOR_NOT_FOUND;
		}

		const int	nPrevNdx=fa.nIconNdx;
		int&	nIconNdx=fa.nIconNdx;

		if ((-1) == (nIconNdx=imgList.Add(hIcon)))
		{
			TRACE(_T("\tPopulateImgList - cannot add icon %d !!!\n"), fa.nIconID);
			return ERROR_OUTOFMEMORY;
		}

		// make sure same index returned if same association used for multiple lists
		if ((nPrevNdx != (-1)) && (nPrevNdx != nIconNdx))
		{
			TRACE(_T("\ttPopulateImgList - ID=%d icons index mismatch (%d <> %d) !!!\n"), fa.nIconID, nPrevNdx, nIconNdx);
			return ERROR_ARENA_TRASHED;
		}
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

// Note: returns (-1) for NULL associations
static int CountImgsAssoc (const IMGLISTASSOC	ima[])
{
	if (NULL == ima)
		return (-1);

	for (int i=0; ; i++)
	{
		const IMGLISTASSOC&	fa=ima[i];
		if (fa.nIconID <= 0)
			return i;
	}

	return 0;
}

/*---------------------------------------------------------------------------*/

HRESULT CreateImgList (CImageList&	imgList,
							  CWinApp&		app,
							  const BOOL	fLargeIcons,
							  IMGLISTASSOC	ima[],	// last entry has (-1) icon ID
							  const UINT	uFlags,
							  const int		nInitial,	// (-1) means use list assocs size
							  const int		nGrow)
{
	int	ulLrgCX=::GetSystemMetrics(SM_CXICON), ulLrgCY=::GetSystemMetrics(SM_CYICON);
	int	ulSmlCX=(ulLrgCX / 2), ulSmlCY=(ulLrgCY / 2);
	const int	nInitialSize=(((-1) == nInitial) ? CountImgsAssoc(ima) : nInitial);
	BOOL	fSuccess=(fLargeIcons ? imgList.Create(ulLrgCX, ulLrgCY, uFlags, nInitialSize, nGrow) :
											imgList.Create(ulSmlCX, ulSmlCY, uFlags, nInitialSize, nGrow));
	if (!fSuccess)
		return ERROR_INVALID_FUNCTION;

	if (NULL == ima)
		return ERROR_SUCCESS;

	return PopulateImgList(imgList, app, ima);
}

/*---------------------------------------------------------------------------*/

int GetAssocImageIndex (const int nIconID, const IMGLISTASSOC	ima[])
{
	if ((nIconID <= 0) || (NULL == ima))
		return (-1);

	for (int i=0; ; i++)
	{
		const IMGLISTASSOC&	fa=ima[i];
		if (fa.nIconID <= 0)
			break;

		if (fa.nIconID == nIconID)
			return fa.nIconNdx;
	}

	return (-1);
}

/*---------------------------------------------------------------------------*/
