#include <mfc/mfcutl.h>

/////////////////////////////////////////////////////////////////////////////

/*		Updates "nDstID" with the data from "nSrcID" if "nDstID" is empty and
 * "fOverride" is FALSE. If "fOverride" is TRUE then data is copied without
 * any checking.
 */
HRESULT AutoUpdateNonEmptyFields (CDialog&	dlg,
											 const int	nSrcID,
											 const int	nDstID,
											 const BOOL	fOverride)
{
	if ((nSrcID <= 0) || (nDstID <=0))
		return ERROR_BAD_ARGUMENTS;

	CString		srcStr=_T("");
	const int	nSrcLen=dlg.GetDlgItemText(nSrcID, srcStr);

	if (!fOverride)
	{
		CString		dstStr=_T("");
		const int	nDstLen=dlg.GetDlgItemText(nDstID, dstStr);

		if (dstStr.IsEmpty())
			dlg.SetDlgItemText(nDstID, srcStr);
	}
	else
	{
		dlg.SetDlgItemText(nDstID, srcStr);
	}

	return S_OK;
}

/*--------------------------------------------------------------------------*/

BOOL ResizeDlgControl (CDialog&	dlg,
							  const int	nCtlID,
							  const int	dx,
							  const int	dy)
{
	CWnd	*pCtl=dlg.GetDlgItem(nCtlID);
	if (NULL == pCtl)
		return FALSE;
	else
		return ResizeWndControl(dlg, *pCtl, dx, dy);
}

/*--------------------------------------------------------------------------*/

BOOL OccupyDlgItemClientArea (CDialog& dlg, const int nItemID)
{
	CWnd	*pCtl=dlg.GetDlgItem(nItemID);
	if (NULL == pCtl)
		return FALSE;

	return OccupyWndClientArea(dlg, *pCtl);
}

/*--------------------------------------------------------------------------*/
