#include <mfc/treectrl.h>

/*---------------------------------------------------------------------------*/

HRESULT PopulateSubTree (CTreeCtrl&				treeCtrl,
								 const TREENODEDEF	nodesDefs[],	// last entry has NULL node name
								 const IMGLISTASSOC	imgs[],
								 HTREEITEM				hInsertAfter,	// can be TVI_LAST, TVI_SORT,
								 HTREEITEM				hParent)			// can be TVI_ROOT
{
	if (NULL == nodesDefs)
		return S_OK;

	for (ULONG	ulNdx=0; ; ulNdx++)
	{
		const TREENODEDEF&	nDef=nodesDefs[ulNdx];
		LPCTSTR					lpszNodeName=nDef.lpszNodeName;
		if (NULL == lpszNodeName)
			break;

		UINT	iMask=(TVIF_HANDLE | TVIF_PARAM | TVIF_TEXT);
		int	nImgNdx=(-1);
		if (nDef.nIconID != (-1))
		{
			if (NULL == imgs)
				return ERROR_BAD_ARGUMENTS;

			if ((-1) == (nImgNdx=GetAssocImageIndex(nDef.nIconID, imgs)))
				return ERROR_SECTOR_NOT_FOUND;

			iMask |= (TVIF_IMAGE | TVIF_SELECTEDIMAGE);
		}

		HTREEITEM	hItem=treeCtrl.InsertItem(iMask, lpszNodeName, nImgNdx, nImgNdx, 0, 0, nDef.pParam, hParent, hInsertAfter);
		if (NULL == hItem)
			return ERROR_INVALID_FUNCTION;

		if (nDef.pChildren != NULL)
		{
			HRESULT	hr=PopulateSubTree(treeCtrl, nDef.pChildren, imgs, hInsertAfter, hItem);
			if (hr != S_OK)
				return hr;
		}
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/
