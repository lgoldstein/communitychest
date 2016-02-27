#include <mfc/dialogs.h>

#include <win32/verinfo.h>

/*--------------------------------------------------------------------------*/

CString CMFCUtlAboutDlg::m_VerInfo=_T("");
CString CMFCUtlAboutDlg::m_Copyright=_T("");

/////////////////////////////////////////////////////////////////////////////

CMFCUtlAboutDlg::CMFCUtlAboutDlg (const int nDlgID, const int nVersionID, const int nCopyrightID, CWnd *pParent)
 : CDialog(nDlgID), m_pWnd(pParent), m_nVersionID(nVersionID), m_nCopyrightID(nCopyrightID)
{
	//{{AFX_DATA_INIT(CMFCUtlAboutDlg)
	//}}AFX_DATA_INIT
}

/////////////////////////////////////////////////////////////////////////////

void CMFCUtlAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CMFCUtlAboutDlg)
	//}}AFX_DATA_MAP
}

/////////////////////////////////////////////////////////////////////////////

#define MAX_VINFO		64
#define MAX_CPYRGHT	64

BOOL CMFCUtlAboutDlg::OnInitDialog() 
{
	CDialog::OnInitDialog();

	if (m_nVersionID != (-1))
	{
		CString	strAbout=_T("");
		if (m_pWnd != NULL)
		{
			CString	dlgTitle=_T("");
			m_pWnd->GetWindowText(dlgTitle);

			strAbout += dlgTitle;
		}

		if (m_VerInfo.IsEmpty())
		{
			TCHAR		szVerInfo[MAX_VINFO+2]=_T("");
			HRESULT	vhr=GetAppVersionProductVersion(NULL, szVerInfo, MAX_VINFO);
			if (ERROR_SUCCESS == vhr)
				m_VerInfo = szVerInfo;
		}

		if (!m_VerInfo.IsEmpty())
		{
			if (!strAbout.IsEmpty())
				strAbout += _T(" ");
			strAbout += m_VerInfo;
		}

		SetDlgItemText(m_nVersionID, strAbout);
	}

	if (m_nCopyrightID != (-1))
	{
		if (m_Copyright.IsEmpty())
		{
			TCHAR		szCopyright[MAX_CPYRGHT+2]=_T("");
			HRESULT	chr=GetAppVersionLegalCopyright(NULL, szCopyright, MAX_CPYRGHT);
			if (ERROR_SUCCESS == chr)
				m_Copyright = szCopyright;
		}

		SetDlgItemText(m_nCopyrightID, m_Copyright);
	}

	CMenu	*sysMenu=GetSystemMenu(FALSE);
	if (sysMenu != NULL)
		sysMenu->ModifyMenu(SC_CLOSE, MF_BYCOMMAND | MF_GRAYED);

	return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}

/////////////////////////////////////////////////////////////////////////////

BEGIN_MESSAGE_MAP(CMFCUtlAboutDlg, CDialog)
	//{{AFX_MSG_MAP(CMFCUtlAboutDlg)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
