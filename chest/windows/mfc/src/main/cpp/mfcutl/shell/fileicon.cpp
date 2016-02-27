#include <mfc/shell.h>

#include <util/string.h>

/*--------------------------------------------------------------------------*/

int CFilesIconsAssoc::GetSuffixIconIndex (LPCTSTR lpszFilePath)
{
	if (IsEmptyStr(lpszFilePath))
		return m_nDefTypeIcon;

	LPCTSTR	lpszSuffix=strlast(lpszFilePath);
	for ( ; lpszSuffix > lpszFilePath; lpszSuffix--)
		if (_T('.') == *lpszSuffix)
			break;

		// if no suffix found use default icon
	if (_T('.') != *lpszSuffix)
		return m_nDefTypeIcon;

	int	nIdx=(-1);

	// handle special case for executables
	if (0 == _tcsicmp(lpszSuffix, _T(".exe")))
	{
		if (m_iconsMap.Lookup(lpszFilePath, nIdx))
			return nIdx;

		HICON	hSmallIcon=NULL, hLargeIcon=NULL;
		UINT	uINum=ExtractIconEx(lpszFilePath, 0, &hLargeIcon, &hSmallIcon, 1);
		if (0 == uINum)
			hLargeIcon = NULL;

		if (hSmallIcon != NULL)
			VERIFY(DestroyIcon(hSmallIcon));

		if (hLargeIcon != NULL)
		{
			nIdx = m_imgList.Add(hLargeIcon);

			if ((-1) != nIdx)
			{
				// remember association for next time
				m_iconsMap.SetAt(lpszFilePath, nIdx);
				return nIdx;
			}
		}
	}

	if (m_iconsMap.Lookup(lpszSuffix, nIdx))
		return nIdx;

	// find out associated icon image (if any)
	SHFILEINFO	sfi;
   ZeroMemory(&sfi,sizeof(sfi));
   DWORD	dwRes=SHGetFileInfo(lpszSuffix,
									  FILE_ATTRIBUTE_NORMAL,
									  &sfi, (sizeof sfi),
									  SHGFI_USEFILEATTRIBUTES|SHGFI_ICON);

   // sfi.hIcon contains the large icon for the file.
	if (NULL == sfi.hIcon)
		return m_nDefTypeIcon;

	nIdx = m_imgList.Add(sfi.hIcon);
	if ((-1) == nIdx)
		return m_nDefTypeIcon;

	// remember association for next time
	m_iconsMap.SetAt(lpszSuffix, nIdx);
	return nIdx;
}

/*--------------------------------------------------------------------------*/
