#include <mfc/mfcutl.h>

/////////////////////////////////////////////////////////////////////////////

BOOL CRateMeasurement::Reset ()
{
	m_ulSamplesNum = 0;
	m_ulTotal = 0;
	m_ulMaxVal = 0;
	m_ulMinVal = (ULONG) (~0);
	m_ulAvgVal = 0;

	if (m_pCtrl != NULL)
	{
		m_pCtrl->SetRange(0, (int) 100);
		m_pCtrl->SetPos(0);
	}

	if (m_pDlg != NULL)
	{
		if (m_nMinID > 0)
			m_pDlg->SetDlgItemInt(m_nMinID, 0U);
		if (m_nMaxID > 0)
			m_pDlg->SetDlgItemInt(m_nMaxID, 0U);
		if (m_nAvgID > 0)
			m_pDlg->SetDlgItemInt(m_nAvgID, 0U);
	}

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CRateMeasurement::SetParams (CDialog				*pDlg,
											 CProgressCtrl		*pCtrl,
											 const int			nMinID,
											 const int			nMaxID,
											 const int			nAvgID)
{
	m_pDlg = pDlg;
	m_pCtrl = pCtrl;
	m_nMinID = nMinID;
	m_nMaxID = nMaxID;
	m_nAvgID = nAvgID;

	Reset();

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

BOOL CRateMeasurement::AddSample (const ULONG ulVal)
{
	if ((m_lpszRateName != NULL) && (_T('\0') != *m_lpszRateName))
		TRACE(_T("\t%s - AddSample(%lu)\n"), m_lpszRateName, ulVal);

	m_ulSamplesNum++;
	m_ulTotal += ulVal;

	if (ulVal > m_ulMaxVal)
	{
		m_ulMaxVal = ulVal;
		if (m_pCtrl != NULL)
			m_pCtrl->SetRange(0, (int) m_ulMaxVal);
	}

	if (ulVal < m_ulMinVal)
		m_ulMinVal = ulVal;

	m_ulAvgVal =(m_ulTotal / m_ulSamplesNum);

	if (m_pCtrl != NULL)
		m_pCtrl->SetPos((int) m_ulAvgVal);

	if (m_pDlg != NULL)
	{
		if (m_nMinID > 0)
			m_pDlg->SetDlgItemInt(m_nMinID, (UINT) m_ulMinVal, FALSE);
		if (m_nMaxID > 0)
			m_pDlg->SetDlgItemInt(m_nMaxID, (UINT) m_ulMaxVal, FALSE);
		if (m_nAvgID > 0)
			m_pDlg->SetDlgItemInt(m_nAvgID, (UINT) m_ulAvgVal, FALSE);
	}

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////

static void SetCtrlState (CDialog& dlg, const int nID, const BOOL fEnable)
{
	if (nID > 0)
	{
		CWnd	*pWnd=dlg.GetDlgItem(nID);
		if (pWnd != NULL)
			pWnd->EnableWindow(fEnable);
	}
}

// returns "fEnable"
BOOL CRateMeasurement::EnableWindow (const BOOL fEnable)
{
	if (m_pCtrl != NULL)
		m_pCtrl->EnableWindow(fEnable);

	if (m_pDlg != NULL)
	{
		SetCtrlState(*m_pDlg, m_nMinID, fEnable);
		SetCtrlState(*m_pDlg, m_nMaxID, fEnable);
		SetCtrlState(*m_pDlg, m_nAvgID, fEnable);
	}

	return fEnable;
}

/////////////////////////////////////////////////////////////////////////////
