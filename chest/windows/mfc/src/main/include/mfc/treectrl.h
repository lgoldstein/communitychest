#ifndef _MFC_CTREECTRL_H_
#define _MFC_CTREECTRL_H_

/*---------------------------------------------------------------------------*/

#include <mfc/mfcutlbase.h>
#include <mfc/imglist.h>

/*---------------------------------------------------------------------------*/

// callback used to release associated data
//
// Note: callback must check validity of passed data for release
typedef HRESULT (*TREECTL_ITEMENUM_CFN)(const CTreeCtrl&	treeCtrl,
													 HTREEITEM			hItem,
													 const DWORD		dwData,
													 LPVOID				pArg,
													 BOOL&				fContEnum);

// Note: if associated data is 0 or (-1) then call is skipped
extern HRESULT ReleaseTreeNode (CTreeCtrl&				treeCtrl,
										  HTREEITEM					hItem,
										  TREECTL_ITEMENUM_CFN	lpfnRcfn=NULL,	// NULL == no need to release data
										  LPVOID						pArg=NULL);

// Deletes all items including siblings, children and the item itself
extern HRESULT ReleaseAllNodes (CTreeCtrl&				treeCtrl,
										  HTREEITEM					hParent,
										  TREECTL_ITEMENUM_CFN	lpfnRcfn=NULL,	// NULL == no need to release data
										  LPVOID						pArg=NULL);

// deletes all children but not siblings or the item itself
extern HRESULT DeleteAllChildren (CTreeCtrl&					treeCtrl,
											 HTREEITEM					hParent,
											 TREECTL_ITEMENUM_CFN	lpfnRcfn=NULL,	// NULL == no need to release data
											 LPVOID						pArg=NULL);

inline HRESULT ReleaseTree (CTreeCtrl&					treeCtrl,
									 TREECTL_ITEMENUM_CFN	lpfnRcfn=NULL,	// NULL == no need to release data
									 LPVOID						pArg=NULL)
{
	return ReleaseAllNodes(treeCtrl, treeCtrl.GetRootItem(), lpfnRcfn, pArg);
}

/*---------------------------------------------------------------------------*/

// Note: does not set root as separator
extern HRESULT GetNodePath (const CTreeCtrl&	treeCtrl,
									 const HTREEITEM	hNode,
									 const TCHAR		chSep,
									 LPTSTR				lpszPath,
									 const UINT32		ulMaxLen);

extern HRESULT GetNodePath (const CTreeCtrl&	treeCtrl,
									 const HTREEITEM	hNode,
									 const TCHAR		chSep,
									 CString&			nodePath);

/*---------------------------------------------------------------------------*/

typedef struct tag_TreeNodeDef {
	LPCTSTR	lpszNodeName;
	int		nIconID;	// if (-1) then no associated icon
	LPARAM	pParam;
	struct tag_TreeNodeDef	*pChildren;	// NULL == no children
} TREENODEDEF;

extern HRESULT PopulateSubTree (CTreeCtrl&			treeCtrl,
										  const TREENODEDEF	nodesDefs[],	// last entry has NULL node name
										  const IMGLISTASSOC	imgs[],
										  HTREEITEM				hInsertAfter,	// can be TVI_LAST, TVI_SORT,
										  HTREEITEM				hParent);		// can be TVI_ROOT

/*---------------------------------------------------------------------------*/

extern HRESULT EnumTreeItems (const CTreeCtrl&		treeCtrl,
										HTREEITEM				hStartItem,	// can be TVI_ROOT
										TREECTL_ITEMENUM_CFN	lpfnEcfn,
										LPVOID					pArg,
										const BOOL				fEnumSiblings=TRUE,
										const BOOL				fEnumChildren=TRUE);

extern HTREEITEM FindTreeItemByData (const CTreeCtrl&	treeCtrl,
												 HTREEITEM			hStartItem,	// can be TVI_ROOT
												 const DWORD		dwData,
												 const BOOL			fCheckSiblings=TRUE,
												 const BOOL			fCheckChildren=TRUE);

/*---------------------------------------------------------------------------*/

/* A dummy child is one with no text and no associated data */

// returns handle of dummy child item (or NULL if error)
inline HTREEITEM AddDummyTreeItemChild (CTreeCtrl& treeCtrl, HTREEITEM hParent)
{
	return treeCtrl.InsertItem(TVIF_TEXT | TVIF_PARAM, _T(""), (-1), (-1), 0, 0, (LPARAM) (-1), hParent, TVI_LAST);
}

extern BOOL IsTreeItemDummyChild (const CTreeCtrl& treeCtrl, HTREEITEM hItem);

// returns the handle of the dummy child item (Note: 1st fit, can have siblings)
extern HTREEITEM FindTreeItemDummyChild (const CTreeCtrl& treeCtrl, HTREEITEM	hParent);

inline BOOL HasTreeItemDummyChild (const CTreeCtrl& treeCtrl, HTREEITEM hParent)
{
	return (FindTreeItemDummyChild(treeCtrl, hParent) != NULL);
}

// if parent has a dummy child, then it is deleted and its handle returned (otherwise does nothing)
//
// returns TRUE if successful (also if no dummy child)
inline BOOL DeleteTreeItemDummyChild (CTreeCtrl&	treeCtrl,
												  HTREEITEM		hParent,
												  HTREEITEM&	hDummyChild)
{
	if ((hDummyChild=FindTreeItemDummyChild(treeCtrl, hParent)) != NULL)
		return treeCtrl.DeleteItem(hDummyChild);
	else
		return TRUE;
}

/*---------------------------------------------------------------------------*/

#endif /* _MFC_CTREECTRL_H_ */
