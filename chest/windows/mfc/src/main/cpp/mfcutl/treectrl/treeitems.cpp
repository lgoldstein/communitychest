#include <mfc/treectrl.h>

#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////

// Note: if associated data is 0 or (-1) then call is skipped
static HRESULT ReleaseTreeNode (CTreeCtrl&				treeCtrl,
										  HTREEITEM					hItem,
										  TREECTL_ITEMENUM_CFN	lpfnRcfn,	// NULL == no need to release data
										  LPVOID						pArg,
										  BOOL&						fContEnum)
{
	if (NULL == hItem)
		return ERROR_BAD_ARGUMENTS;

#ifdef _DEBUG
	CString		itemName=treeCtrl.GetItemText(hItem);
#endif

	const DWORD	dwData=treeCtrl.GetItemData(hItem);
	if ((dwData != 0) && (dwData != (DWORD) (-1)) && (lpfnRcfn != NULL))
	{
		HRESULT	hr=(*lpfnRcfn)(treeCtrl, hItem, dwData, pArg, fContEnum);
		if (hr != ERROR_SUCCESS)
			return hr;
	}

	if (!treeCtrl.DeleteItem(hItem))
		return ERROR_INVALID_FUNCTION;

	return ERROR_SUCCESS;
}

HRESULT ReleaseTreeNode (CTreeCtrl&					treeCtrl,
								 HTREEITEM					hItem,
								 TREECTL_ITEMENUM_CFN	lpfnRcfn,	// NULL == no need to release data
								 LPVOID						pArg)
{
	BOOL	fContEnum=TRUE;
	return ReleaseTreeNode(treeCtrl, hItem, lpfnRcfn, pArg, fContEnum);
}

/////////////////////////////////////////////////////////////////////////////

// Deletes all items including siblings, children and the item itself
static HRESULT ReleaseAllNodes (CTreeCtrl&				treeCtrl,
										  HTREEITEM					hParent,
										  TREECTL_ITEMENUM_CFN	lpfnRcfn,	// NULL == no need to release data
										  LPVOID						pArg,
										  BOOL&						fContEnum)
{
	for (HTREEITEM	hItem=hParent; fContEnum && (hItem != NULL); )
	{
		HRESULT		hr=ERROR_SUCCESS;
		HTREEITEM	hNextItem=treeCtrl.GetNextSiblingItem(hItem);

		if (treeCtrl.ItemHasChildren(hItem))
		{
			if ((hr=ReleaseAllNodes(treeCtrl, treeCtrl.GetChildItem(hItem), lpfnRcfn, pArg, fContEnum)) != ERROR_SUCCESS)
				return hr;
		}

		if ((hr=ReleaseTreeNode(treeCtrl, hItem, lpfnRcfn, pArg, fContEnum)) != ERROR_SUCCESS)
			return hr;

		hItem = hNextItem;
	}

	return ERROR_SUCCESS;
}

HRESULT ReleaseAllNodes (CTreeCtrl&					treeCtrl,
								 HTREEITEM					hParent,
								 TREECTL_ITEMENUM_CFN	lpfnRcfn,	// NULL == no need to release data
								 LPVOID						pArg)
{
	BOOL	fContEnum=TRUE;
	return ReleaseAllNodes(treeCtrl, hParent, lpfnRcfn, pArg, fContEnum);
}

/////////////////////////////////////////////////////////////////////////////

// deletes all children but not siblings or the item itself
HRESULT DeleteAllChildren (CTreeCtrl&				treeCtrl,
									HTREEITEM				hParent,
									TREECTL_ITEMENUM_CFN	lpfnRcfn,	// NULL == no need to release data
									LPVOID					pArg)
{
	if (NULL == hParent)
		return ERROR_BAD_ARGUMENTS;

	for (HTREEITEM	hItem=treeCtrl.GetChildItem(hParent); hItem != NULL; )
	{
		HTREEITEM	hNextItem=treeCtrl.GetNextItem(hItem, TVGN_NEXT);
		HRESULT		hr=DeleteAllChildren(treeCtrl, hItem, lpfnRcfn, pArg);
		if (hr != ERROR_SUCCESS)
			return hr;

		// now that all other children have been deleted, we can release this item
		if ((hr=ReleaseTreeNode(treeCtrl, hItem, lpfnRcfn, pArg)) != ERROR_SUCCESS)
			return hr;

		hItem = hNextItem;
	}

	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

static HRESULT EnumTreeItems (const CTreeCtrl&		treeCtrl,
										HTREEITEM				hStartItem,	// can be TVI_ROOT
										TREECTL_ITEMENUM_CFN	lpfnEcfn,
										LPVOID					pArg,
										const BOOL				fEnumSiblings,
										const BOOL				fEnumChildren,
										BOOL&						fContEnum)
{
	if (NULL == lpfnEcfn)
		return ERROR_INVALID_FUNCTION;

	for (HTREEITEM	hItem=hStartItem; fContEnum && (hItem != NULL); )
	{
		HRESULT		hr=ERROR_SUCCESS;
		const BOOL	fIsRoot=(TVI_ROOT == hItem);
		HTREEITEM	hNextItem=((fEnumSiblings && (!fIsRoot)) ? treeCtrl.GetNextSiblingItem(hItem) : NULL);
		const BOOL	fHasChildren=(fIsRoot ? TRUE : treeCtrl.ItemHasChildren(hItem));

		if (fHasChildren && fEnumChildren)
		{
			if ((hr=EnumTreeItems(treeCtrl, treeCtrl.GetChildItem(hItem), lpfnEcfn, pArg, TRUE, TRUE, fContEnum)) != ERROR_SUCCESS)
				return hr;
		}

		if (fContEnum)
		{
			if ((hr=(*lpfnEcfn)(treeCtrl, hItem, treeCtrl.GetItemData(hItem), pArg, fContEnum)) != ERROR_SUCCESS)
				return hr;
		}

		hItem = hNextItem;
	}

	return ERROR_SUCCESS;
}

HRESULT EnumTreeItems (const CTreeCtrl&		treeCtrl,
							  HTREEITEM					hStartItem,	// can be TVI_ROOT
							  TREECTL_ITEMENUM_CFN	lpfnEcfn,
							  LPVOID						pArg,
							  const BOOL				fEnumSiblings,
							  const BOOL				fEnumChildren)
{
	BOOL	fContEnum=TRUE;
	return EnumTreeItems(treeCtrl, hStartItem, lpfnEcfn, pArg, fEnumSiblings, fEnumChildren, fContEnum);
}

/////////////////////////////////////////////////////////////////////////////

typedef struct {
	HTREEITEM	hFound;
	DWORD			dwData;
} FTIBARGS;

static HRESULT ftibdCfn (const CTreeCtrl&	treeCtrl,
								 HTREEITEM			hItem,
								 const DWORD		dwData,
								 LPVOID				pArg,
								 BOOL&				fContEnum)
{
	if (NULL == pArg)
		return ERROR_BAD_ENVIRONMENT;
	FTIBARGS&	ftiba=*((FTIBARGS *) pArg);
	if (dwData == ftiba.dwData)
		ftiba.hFound = hItem;

	fContEnum = (NULL == ftiba.hFound);
	return S_OK;
}

HTREEITEM FindTreeItemByData (const CTreeCtrl&	treeCtrl,
										HTREEITEM			hStartItem,	// can be TVI_ROOT
										const DWORD			dwData,
										const BOOL			fCheckSiblings,
										const BOOL			fCheckChildren)
{
	FTIBARGS	ftiba={	NULL, dwData };	
	HRESULT	hr=EnumTreeItems(treeCtrl, hStartItem, ftibdCfn, (LPVOID) &ftiba, fCheckSiblings, fCheckChildren);
	if (S_OK != hr)
		return NULL;
	else
		return ftiba.hFound;
}

/////////////////////////////////////////////////////////////////////////////
