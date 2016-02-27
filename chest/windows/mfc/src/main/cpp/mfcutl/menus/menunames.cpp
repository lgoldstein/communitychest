#include <mfc/menus.h>
#include <util/string.h>

/*--------------------------------------------------------------------------*/

int CompareMenuItemNames (LPCTSTR lpszN1, const ULONG ulN1Len,
								  LPCTSTR lpszN2, const ULONG ulN2Len,
								  const BOOL		fCaseSensitive)
{
	LPCTSTR	lpszN1Pos=lpszN1, lpszN2Pos=lpszN2;

	for (ULONG	ul1dx=0, ul2dx=0; (ul1dx < ulN1Len) && (ul2dx < ulN2Len); )
	{
		if (_T('&') == *lpszN1Pos)
		{
			lpszN1Pos++;
			ul1dx++;
			continue;
		}

		if (_T('&') == *lpszN2Pos)
		{
			lpszN2Pos++;
			ul2dx++;
			continue;
		}

		if (fCaseSensitive)
		{
			if (*lpszN1Pos != *lpszN2Pos)
				return SIGNOF((int) (*lpszN2Pos) - (int) (*lpszN1Pos));
		}
		else
		{
			if (_totlower(*lpszN1Pos) != _totlower(*lpszN2Pos))
				return SIGNOF((int) _totlower(*lpszN2Pos) - (int) _totlower(*lpszN1Pos));
		}

		// this point is reached if same character
		lpszN1Pos++;
		lpszN2Pos++;
		ul1dx++;
		ul2dx++;
	}

	if ((ul1dx < ulN1Len) || (ul2dx < ulN2Len))
		return SIGNOF((int) ulN2Len - (int) ulN1Len);

	return 0;
}

int CompareMenuItemNames (LPCTSTR lpszN1, LPCTSTR lpszN2, const BOOL fCaseSensitive)
{
	return CompareMenuItemNames(GetSafeStrPtr(lpszN1), GetSafeStrlen(lpszN1), GetSafeStrPtr(lpszN2), GetSafeStrlen(lpszN2), fCaseSensitive);
}

/*--------------------------------------------------------------------------*/

CMenu *FindSubMenuByName (const CMenu& parentMenu, LPCTSTR lpszSubName, const BOOL fCaseSensitive)
{
	const	UINT	nCount=parentMenu.GetMenuItemCount();
	if ((int) nCount < 0)
		return NULL;
	if (IsEmptyStr(lpszSubName))
		return NULL;

	const ULONG	ulSNLen=_tcslen(lpszSubName);
	for (UINT	nItem=0; nItem < nCount; nItem++)
	{
		CString		strName=_T("");
		const int	nLen=parentMenu.GetMenuString(nItem, strName, MF_BYPOSITION);

		if (0 == CompareMenuItemNames(lpszSubName, ulSNLen, strName, (ULONG) nLen, fCaseSensitive))
			return parentMenu.GetSubMenu(nItem);
	}

	return NULL;
}

/*--------------------------------------------------------------------------*/
