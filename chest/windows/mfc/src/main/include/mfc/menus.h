#ifndef _MFCUTL_MENUS_H_
#define _MFCUTL_MENUS_H_

#include <mfc/mfcutlbase.h>

/*--------------------------------------------------------------------------*/

inline UINT GetMenuItemStateMask (const BOOL fEnabled)
{
	return (fEnabled ? MF_ENABLED : (MF_DISABLED | MF_GRAYED));
}

/*--------------------------------------------------------------------------*/

typedef struct {
	UINT	nMenuCmd;
	int	nIconID;	// (-1) means none
} IMLXL, *LPIMLXL;

#include <afxtempl.h>

class CIconizedMenu : public CMenu {
	private:
		int							m_iconX;
		int							m_iconY;
		CImageList					m_imgList;
		CMap<UINT,UINT,int,int>	m_iconsMap;
		CWinApp						*m_pApp;

	public:
		CIconizedMenu (const IMLXL ilx[]=NULL);
		virtual ~CIconizedMenu();

		virtual HRESULT SetIconsMap (const IMLXL ilx[]);
		virtual void DrawItem (LPDRAWITEMSTRUCT lpDIS);
		virtual void MeasureItem (LPMEASUREITEMSTRUCT lpMIS);

	protected:
//		DECLARE_MESSAGE_MAP()

};

/*--------------------------------------------------------------------------*/

extern void UpdateSelectedMenuItemCmdUI (CWnd& wnd, const int nItemID);
extern void UpdateSelectedMenuItemCmdUI (CWnd& wnd, CMenu& subMenu);
extern void UpdateContextMenuCmdUI (CWnd& wnd, CMenu& popupMenu);

/*--------------------------------------------------------------------------*/

extern int CompareMenuItemNames (LPCTSTR lpszN1, const ULONG ulN1Len,
											LPCTSTR lpszN2, const ULONG ulN2Len,
											const BOOL		fCaseSensitive);
extern int CompareMenuItemNames (LPCTSTR lpszN1, LPCTSTR lpszN2, const BOOL fCaseSensitive);
extern CMenu *FindSubMenuByName (const CMenu& parentMenu, LPCTSTR lpszSubName, const BOOL fCaseSensitive);

/*--------------------------------------------------------------------------*/

#endif /* _MFCUTL_MENUS_H_ */
