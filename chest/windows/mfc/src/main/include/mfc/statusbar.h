#ifndef _MFCUTL_STATUSBAR_H_
#define _MFCUTL_STATUSBAR_H_

#include <mfc/mfcutlbase.h>

#if _MSC_VER >= 1000
#pragma once
#endif // _MSC_VER >= 1000

/*--------------------------------------------------------------------------*/

extern HRESULT AddStatusBar (CWnd&				parentWnd,
									  CStatusBarCtrl&	stBar,
									  const int			stBarID,
									  const UINT		nWidths[],	// percentage (last entry == 0)
									  const int			nMinHeight=(-1),	// use font as height
									  const DWORD		dwStyle=(WS_CHILD | WS_VISIBLE | CCS_BOTTOM));

extern HRESULT AttachStatusBarProgress (CStatusBarCtrl&	stBar,
													 CProgressCtrl&	progCtrl,
													 const int			nPaneIndex,
													 const int			nProgID,
													 const DWORD		dwStyle=(WS_CHILD | WS_VISIBLE | PBS_SMOOTH));

/*--------------------------------------------------------------------------*/

#endif /* _MFCUTL_STATUSBAR_H_ */

