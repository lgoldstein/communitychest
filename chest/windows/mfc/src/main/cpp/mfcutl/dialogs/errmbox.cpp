#include <mfc/dialogs.h>

/*--------------------------------------------------------------------------*/

int afxMsgfVBox (LPCTSTR lpszFmt, const UINT mbStyle, va_list ap)
{
	CString	strMsg=_T("");
	strMsg.FormatV(lpszFmt, ap);

	return AfxMessageBox(strMsg, mbStyle);
}

int afxMsgfBox (LPCTSTR lpszFmt, const UINT mbStyle, ...)
{
	va_list	ap;
	va_start(ap, mbStyle);
	const int	nRes=afxMsgfVBox(lpszFmt, mbStyle, ap);
	va_end(ap);

	return nRes;
}

// Note: returns IDCANCEL if cannot load template
int afxMsgfBox (const int nTemplateID, const UINT mbStyle, ...)
{
	CString	strFmt=_T("");
	if (!strFmt.LoadString(nTemplateID))
		return IDCANCEL;

	va_list	ap;
	va_start(ap, mbStyle);
	const int	nRes=afxMsgfVBox(strFmt, mbStyle, ap);
	va_end(ap);

	return nRes;
}

/*--------------------------------------------------------------------------*/

HRESULT mfcMsgVBox (const HRESULT rhr, LPCTSTR lpszFmt, const UINT mbStyle, va_list ap)
{
	const int nRes=afxMsgfVBox(lpszFmt, mbStyle, ap);
	// NOTE: returned value from message box is ignored
	return rhr;
}

/*--------------------------------------------------------------------------*/

HRESULT mfcMsgBox (const HRESULT rhr, const int nTemplateID, const UINT mbStyle, ...)
{
	CString	strFmt=_T("");
	if (!strFmt.LoadString(nTemplateID))
		return rhr;

	va_list	ap;
	va_start(ap, mbStyle);
	HRESULT	hr=mfcMsgVBox(rhr, strFmt, mbStyle, ap);
	va_end(ap);

	return hr;
}

/*--------------------------------------------------------------------------*/

HRESULT mfcMsgBox (const HRESULT rhr, LPCTSTR lpszFmt, const UINT mbStyle, ...)
{
	va_list	ap;
	va_start(ap, mbStyle);
	HRESULT	hr=mfcMsgVBox(rhr, lpszFmt, mbStyle, ap);
	va_end(ap);

	return hr;
}

/*--------------------------------------------------------------------------*/

