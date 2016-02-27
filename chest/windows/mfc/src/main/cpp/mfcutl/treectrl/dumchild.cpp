#include <mfc/treectrl.h>

#include <util/string.h>

/* A dummy child is one with no text and no associated data */

/////////////////////////////////////////////////////////////////////////////

BOOL IsTreeItemDummyChild (const CTreeCtrl& treeCtrl, HTREEITEM hItem)
{
	if (NULL == hItem)
		return FALSE;

	const CString	strItemName=treeCtrl.GetItemText(hItem);
	const DWORD		dwItemData=treeCtrl.GetItemData(hItem);

	return (strItemName.IsEmpty() && ((0 == dwItemData) || ((DWORD) (-1) == dwItemData)));
}

/*---------------------------------------------------------------------------*/

// returns the handle of the dummy child item (Note: 1st fit, can have siblings)
HTREEITEM FindTreeItemDummyChild (const CTreeCtrl& treeCtrl, HTREEITEM	hParent)
{
	if (NULL == hParent)
		return NULL;

	for (HTREEITEM hChild=treeCtrl.GetChildItem(hParent);
		  hChild != NULL;
		  hChild = treeCtrl.GetNextSiblingItem(hChild))
		if (IsTreeItemDummyChild(treeCtrl, hChild))
			return hChild;

	return NULL;
}

/*---------------------------------------------------------------------------*/
