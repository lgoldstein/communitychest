#include <mfc/menus.h>

/*--------------------------------------------------------------------------*/

void UpdateSelectedMenuItemCmdUI (CWnd& wnd, CMenu& subMenu)
{
	if (!IsMenu(subMenu.GetSafeHmenu()))
		return;

	CCmdUI cmdUI;
	cmdUI.m_nIndexMax = subMenu.GetMenuItemCount();

	for (UINT i = 0; i < cmdUI.m_nIndexMax;++i)
	{
		cmdUI.m_nIndex = i;
		cmdUI.m_nID = subMenu.GetMenuItemID(i);

		if ((-1) == cmdUI.m_nID)	// sub-menu
		{
			CMenu	*pSubMenu=subMenu.GetSubMenu(i);
			if (pSubMenu != NULL)	// go recursively
				UpdateSelectedMenuItemCmdUI(wnd, *pSubMenu);
		}
		else if (0 == cmdUI.m_nID)	// separator
		{
			continue;
		}
		else
		{
			cmdUI.m_pMenu = &subMenu;
			cmdUI.DoUpdate(&wnd, FALSE);
		}
	}
}

/*--------------------------------------------------------------------------*/

void UpdateSelectedMenuItemCmdUI (CWnd& wnd, const int nItemID)
{
	CMenu	*pMainMenu=wnd.GetMenu();
	if (NULL == pMainMenu)
		return;

	CMenu	*pSubMenu=pMainMenu->GetSubMenu(nItemID);
	if (NULL == pSubMenu)
		return;

	UpdateSelectedMenuItemCmdUI(wnd, *pSubMenu);
}

/*--------------------------------------------------------------------------*/

// based on code published by Noel Dillabough on http://www.codeproject.com
void UpdateContextMenuCmdUI (CWnd& wnd, CMenu& popupMenu)
{
	CCmdUI state;
	state.m_pMenu = &popupMenu;
	state.m_pParentMenu = &popupMenu;
	state.m_nIndexMax = popupMenu.GetMenuItemCount();

	for (state.m_nIndex = 0; state.m_nIndex < state.m_nIndexMax; state.m_nIndex++) 
	{
		state.m_nID = popupMenu.GetMenuItemID(state.m_nIndex);

		// menu separator or invalid cmd - ignore it
		if (0 == state.m_nID)
			continue; 

		// possibly a popup menu, route to child menu if so
		if ((UINT) (-1) == state.m_nID)
		{
			CMenu	*pSub=popupMenu.GetSubMenu(state.m_nIndex);
			if (pSub != NULL)
				UpdateContextMenuCmdUI(wnd, *pSub);
		}
		else // normal menu item, Auto disable if command is NOT a system command.
		{
			state.m_pSubMenu = NULL;
			state.DoUpdate(&wnd, FALSE);
		}
	}
}

/*--------------------------------------------------------------------------*/
