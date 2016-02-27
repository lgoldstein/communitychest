#include <mfc/treectrl.h>

#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////

HRESULT GetNodePath (const CTreeCtrl&	treeCtrl,
							const HTREEITEM	hNode,
							const TCHAR			chSep,
							LPTSTR				lpszPath,
							const UINT32		ulMaxLen)
{
	if ((NULL == hNode) || (_T('\0') == chSep) || (NULL == lpszPath) || (0 == ulMaxLen))
		return ERROR_BAD_ARGUMENTS;

	CString		nodeName=treeCtrl.GetItemText(hNode);
	HTREEITEM	hRoot=treeCtrl.GetRootItem();
	if (hNode == hRoot)
	{
		*lpszPath = _T('\0');
		if ((UINT32) nodeName.GetLength() >= ulMaxLen)
			return ERROR_BUFFER_OVERFLOW;

		_tcscpy(lpszPath, nodeName);
		return ERROR_SUCCESS;
	}

	// OK if root is empty but not other nodes
	if (nodeName.IsEmpty())
		return ERROR_BAD_FORMAT;

	HTREEITEM	hParent=treeCtrl.GetParentItem(hNode);
	HRESULT		hr=GetNodePath(treeCtrl, hParent, chSep, lpszPath, ulMaxLen);
	if (hr != ERROR_SUCCESS)
		return hr;

	UINT32	ulCurLen=_tcslen(lpszPath), ulRemLen=(ulMaxLen - ulCurLen);
	LPTSTR	lsp=(lpszPath + ulCurLen);
	if (ulCurLen > 0)
	{
		if ((hr=strlinsch(&lsp, chSep, &ulRemLen)) != ERROR_SUCCESS)
			return hr;
	}

	if ((hr=strlinsstr(&lsp, nodeName, &ulRemLen)) != ERROR_SUCCESS)
		return hr;

	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////

HRESULT GetNodePath (const CTreeCtrl&	treeCtrl,
							const HTREEITEM	hNode,
							const TCHAR			chSep,
							CString&				nodePath)
{
	if ((NULL == hNode) || (_T('\0') == chSep))
		return ERROR_BAD_ARGUMENTS;

	CString		nodeName=treeCtrl.GetItemText(hNode);
	HTREEITEM	hRoot=treeCtrl.GetRootItem();
	if (hNode == hRoot)
	{
		nodePath = nodeName;
		return ERROR_SUCCESS;
	}

	// OK if root is empty but not other nodes
	if (nodeName.IsEmpty())
		return ERROR_BAD_FORMAT;

	HTREEITEM	hParent=treeCtrl.GetParentItem(hNode);
	HRESULT		hr=GetNodePath(treeCtrl, hParent, chSep, nodePath);
	if (hr != ERROR_SUCCESS)
		return hr;

	if (!nodePath.IsEmpty())
		nodePath += chSep;
	nodePath += nodeName;

	return ERROR_SUCCESS;
}

/////////////////////////////////////////////////////////////////////////////
