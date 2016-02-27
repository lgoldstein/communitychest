#ifndef _MFCUTL_DIALOGS_H_
#define _MFCUTL_DIALOGS_H_

#include <mfc/mfcutlbase.h>

/*--------------------------------------------------------------------------*/

// "About..." dialog - displays version and copyright from resource (if found)
class CMFCUtlAboutDlg : public CDialog
{
	private:
		CWnd	*m_pWnd;
		int	m_nVersionID;
		int	m_nCopyrightID;

		static CString		m_VerInfo;
		static CString		m_Copyright;

	public:
		CMFCUtlAboutDlg (const int nDlgID, const int nVersionID=(-1), const int nCopyrightID=(-1), CWnd *pParent=NULL);
		
		virtual ~CMFCUtlAboutDlg () { }

// Dialog Data
	//{{AFX_DATA(CMFCUtlAboutDlg)
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMFCUtlAboutDlg)
	protected:
		virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
	protected:

	//{{AFX_MSG(CMFCUtlAboutDlg)
		virtual BOOL OnInitDialog();
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

class CRateMeasurement {
	private:
		LPCTSTR			m_lpszRateName;
		CDialog			*m_pDlg;
		CProgressCtrl	*m_pCtrl;
		int				m_nMinID;
		int				m_nMaxID;
		int				m_nAvgID;
		ULONG				m_ulSamplesNum;
		ULONG				m_ulTotal;
		ULONG				m_ulMinVal;
		ULONG				m_ulMaxVal;
		ULONG				m_ulAvgVal;

	public:
		CRateMeasurement (LPCTSTR lpszRateName=_T(""))
			: m_pCtrl(NULL), m_pDlg(NULL), m_lpszRateName(lpszRateName),
			  m_nMinID(-1), m_nMaxID(-1), m_nAvgID(-1)
		{
			Reset();
		}

		BOOL SetParams (CDialog				*pDlg=NULL,
							 CProgressCtrl		*pCtrl=NULL,
							 const int			nMinID=(-1),
							 const int			nMaxID=(-1),
							 const int			nAvgID=(-1));

		BOOL Reset ();

		BOOL AddSample (const ULONG ulVal);

		// returns "fEnable"
		BOOL EnableWindow (const BOOL fEnable);

		ULONG	GetSamplesNum () const	{ return m_ulSamplesNum;	}
		ULONG	GetMinValue () const		{ return m_ulMinVal;			}
		ULONG	GetMaxValue () const		{ return m_ulMaxVal;			}
		ULONG	GetAvgValue () const		{ return m_ulAvgVal;			}

		virtual ~CRateMeasurement () { }
};

/*--------------------------------------------------------------------------*/

extern int afxMsgfVBox (LPCTSTR lpszFmt, const UINT mbStyle, va_list ap);
extern int afxMsgfBox (LPCTSTR lpszFmt, const UINT mbStyle, ...);

// Note: returns IDCANCEL if cannot load template
extern int afxMsgfBox (const int nTemplateID, const UINT mbStyle, ...);

// returns "rhr" as its return value regardless of the message box value
extern HRESULT mfcMsgVBox (const HRESULT rhr, LPCTSTR lpszFmt, const UINT mbStyle, va_list ap);
extern HRESULT mfcMsgBox (const HRESULT rhr, const int nTemplateID, const UINT mbStyle, ...);
extern HRESULT mfcMsgBox (const HRESULT rhr, LPCTSTR lpszFmt, const UINT mbStyle, ...);

/*--------------------------------------------------------------------------*/

extern HRESULT BuildChosenFilesFilter (LPCTSTR		lpszExts[],	// last entry = NULL
													LPCTSTR		lpszDescs[],	// last entry = NULL
													CString& 	fltr);	// in out

// returns ERROR_CANCELLED if user cancelled rather than OK
extern HRESULT GetUserChosenFiles (CWnd			*pParent,
											  CString&		iDir,		// in/out
											  LPCTSTR		lpszFilter,
											  LPCTSTR		lpszDefExt,
											  const BOOL	fSaveIt,
											  CString&		filePath);

// returns ERROR_CANCELLED if user cancelled rather than OK
extern HRESULT GetUserChosenFiles (CWnd			*pParent,
											  CString&		iDir,		// in/out
											  LPCTSTR		lpszExts[],	// last entry = NULL
											  LPCTSTR		lpszDescs[],	// last entry = NULL
											  LPCTSTR		lpszDefExt,
											  const BOOL	fSaveIt,
											  CString&		filePath);

// returns ERROR_CANCELLED if user cancelled rather than OK
extern HRESULT GetUserChosenFile (CWnd			*pParent,
											 CString&	iDir,		// in/out
											 LPCTSTR		lpszExtPar,
											 LPCTSTR		lpszDesc,
											 const BOOL	fSaveIt,
											 CString&	filePath);

extern HRESULT GetUserChosenFile (CWnd			*pParent,
											 const int	nID,
											 LPCTSTR		lpszDesc,
											 const BOOL	fSaveIt,
											 CString&	filePath);

extern HRESULT UpdateUserChosenFile (CWnd			*pParent,
												 const int	nID,
												 LPCTSTR		lpszDesc);

/*--------------------------------------------------------------------------*/

extern HRESULT GetUTF8DlgItemText (const CWnd&	wnd,
											  const int		nItemID,
											  LPTSTR			lpszUTF8str,
											  const ULONG	ulMaxUTF8Len);

extern HRESULT SetUTF8DlgItemText (CWnd&			wnd,
											  const int		nItemID,
											  LPCTSTR		lpszUTF8str,
											  const ULONG	ulUTF8Len);

inline HRESULT SetUTF8DlgItemText (CWnd&		wnd,
											  const int	nItemID,
											  LPCTSTR	lpszUTF8str)
{
	return SetUTF8DlgItemText(wnd, nItemID, lpszUTF8str, ((NULL == lpszUTF8str) ? 0 : _tcslen(lpszUTF8str)));
}

inline HRESULT SetUTF8DlgItemText (CWnd&				wnd,
											  const int			nItemID,
											  const CString&	strUTF8)
{
	return SetUTF8DlgItemText(wnd, nItemID, strUTF8, strUTF8.GetLength());
}

/*--------------------------------------------------------------------------*/

/*		Updates "nDstID" with the data from "nSrcID" if "nDstID" is empty and
 * "fOverride" is FALSE. If "fOverride" is TRUE then data is copied without
 * any checking.
 */
extern HRESULT AutoUpdateNonEmptyFields (CDialog&		dlg,
													  const int		nSrcID,
													  const int		nDstID,
													  const BOOL	fOverride=FALSE);

/*--------------------------------------------------------------------------*/

extern BOOL ResizeDlgControl (CDialog&		dlg,
										const int	nCtlID,
										const int	dx,
										const int	dy);

extern BOOL OccupyDlgItemClientArea (CDialog& dlg, const int nItemID);

/*--------------------------------------------------------------------------*/

#endif /* _MFCUTL_DIALOGS_H_ */
