#ifndef _MFCUTL_SHELL_H_
#define _MFCUTL_SHELL_H_

#include <mfc/mfcutlbase.h>

#include <win32/shell.h>

/*--------------------------------------------------------------------------*/

// Note: returns ERROR_CANCELLED if user pressed the "Cancel" button
extern HRESULT GetChosenUserFolder (HWND		hOwner,
												LPCTSTR	lpszTitle,
												CString&	curFolder /* in/out */);
												
inline HRESULT GetChosenUserFolder (CWnd&		wdOwner,
												LPCTSTR	lpszTitle,
												CString&	curFolder /* in/out */)
{
	return GetChosenUserFolder(wdOwner.GetSafeHwnd(), lpszTitle, curFolder);
}

/*--------------------------------------------------------------------------*/

#include <afxtempl.h>

class CFilesIconsAssoc {
	private:
		CImageList&								m_imgList;
		CMap<CString,LPCTSTR,int,int&>	m_iconsMap;
		int										m_nDefTypeIcon;

	public:
		CFilesIconsAssoc (CImageList& imgList)
			: m_imgList(imgList), m_nDefTypeIcon(-1)
		{
		}

		int GetSuffixIconIndex (LPCTSTR lpszFilePath);

		virtual ~CFilesIconsAssoc ()
		{
		}
};

/*--------------------------------------------------------------------------*/

#endif /* _MFCUTL_SHELL_H_ */
