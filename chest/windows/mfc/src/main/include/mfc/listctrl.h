#ifndef _MFC_CLISTCTRL_H_
#define _MFC_CLISTCTRL_H_

/*---------------------------------------------------------------------------*/

#include <mfc/mfcutlbase.h>
#include <mfc/imglist.h>

#include <util/string.h>

inline UINT ListViewCheckedStateMask (const BOOL fChecked)
{
	return UINT((int(fChecked) + 1) << 12);
}

// Note: negative value means no previous state
inline LONG ListViewGetCheckedState (const UINT uState)
{
	return (((uState & LVIS_STATEIMAGEMASK)>>12)-1);
}

/*---------------------------------------------------------------------------*/

typedef HRESULT (*LCTRL_SELITEMS_CFN)(CListCtrl&	lCtrl,
												  const int		nItem,
												  LPVOID			pItemData,
												  LPVOID			pArg,
												  BOOL&			fContEnum);

// calls the enumeration callback for each selected item (if any)
extern HRESULT EnumSelectedListCtrlItems (const CListCtrl&		listCtrl,
														LCTRL_SELITEMS_CFN	lpfnEcfn,
														LPVOID					pArg);

extern HRESULT UpdateAllItemsSelection (CListCtrl& listCtrl, const BOOL fSelected);

/*---------------------------------------------------------------------------*/

typedef struct {
	LPCTSTR	lpszColHdr;
	ULONG		ulColInfo;	// user defined column info
	UINT		uColWidth;	// precent (if 0 then skipped)
	int		nFormat;		// LVCFMT_LEFT, LVCFMT_RIGHT, LVCFMT_CENTER
	BOOL		fAscending;	// sorting "direction" (if any)
	int		nColIdx;
} CLISTCTRLCOLINFO, *LPCLISTCTRLCOLINFO;

extern const LPCLISTCTRLCOLINFO GetMultiColListColByIndex (
												const LPCLISTCTRLCOLINFO	pCols,
												const int						nColIdx);

// returns column info of specified user info - returns NULL if not found
extern const LPCLISTCTRLCOLINFO GetMultiColListColByInfo (
												const LPCLISTCTRLCOLINFO	pCols,
												const ULONG						ulColInfo);

// returns column info of specified name - returns NULL if not found
extern const LPCLISTCTRLCOLINFO GetMultiColListColByName (
												const LPCLISTCTRLCOLINFO	pCols,
												LPCTSTR							lpszColHdr);

extern HRESULT BuildMultiColList (CListCtrl&				lCtrl,
											 LPCLISTCTRLCOLINFO	pCols);

// re-calculates columns widths
extern HRESULT ReadjustMultiColList (CListCtrl&				lCtrl,
												 LPCLISTCTRLCOLINFO	pCols);

/*---------------------------------------------------------------------------*/

// detects if any column clicked (not only 1st)
extern int ListCtrlHitTestEx (const CListCtrl&	lCtrl,
										const CPoint&		point,
										int					*col=NULL);


inline int AdjustListCmpRes (const int nRes, const BOOL fAscending)
{
	return (fAscending ? nRes : (0 - nRes));
}

extern int sortListStr (LPCTSTR lpszS1,	const ULONG ulL1,
								LPCTSTR lpszS2,	const ULONG ulL2,
								const BOOL			fAscending);

extern int sortListStr (LPCTSTR lpszS1,				const ULONG ulL1,
								LPCTSTR lpszS2,				const ULONG ulL2,
								const LPCLISTCTRLCOLINFO	pColInfo);

extern int sortListStr (LPCTSTR lpszS1, LPCTSTR lpszS2, const LPCLISTCTRLCOLINFO	pColInfo);

extern int sortListStr (LPCTSTR lpszS1, LPCTSTR lpszS2, const BOOL fAscending);

extern int sortListNum (const ULONG	ul1, const ULONG ul2, const BOOL fAscending);

extern int sortListNum (const ULONG	ul1, const ULONG ul2, const LPCLISTCTRLCOLINFO	pColInfo);

/*---------------------------------------------------------------------------*/

// extended class which detects if any column clicked (not only 1st)
class CMFCUtlListCtrl : public CListCtrl
{
	public:
		CMFCUtlListCtrl() : CListCtrl() { }
		virtual ~CMFCUtlListCtrl() { }

		int HitTestEx (CPoint &point, int *col=NULL) const
		{
			return ::ListCtrlHitTestEx(*this, point, col);
		}

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CMFCUtlListCtrl)
	//}}AFX_VIRTUAL

	protected:
	//{{AFX_MSG(CMFCUtlListCtrl)
	// mark selected row even if click not on 1st column
	afx_msg void OnLButtonDown (UINT nFlags, CPoint point);
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/*---------------------------------------------------------------------------*/

// Note: returns ERROR_FILE_NOT_FOUND if default value not found in list
extern HRESULT SetDefaultSel (CComboBox& cb, const DWORD defVal);

// returns item index (>= 0) if successful
extern int AddCBoxChoice (CComboBox& cBox, LPCTSTR lpszText, const DWORD dwVal);

extern HRESULT PopulateChoicesList (CComboBox&	cBox,
												const STLXL	sglXl[],
												const DWORD	dwDefData=(DWORD) (-1));

extern HRESULT PopulateChoicesList (CComboBox&	cBox,
												const PTLXL	tglXl[],
												const DWORD	dwDefData=(DWORD) (-1));

// NULL terminate list of strings (associated data is the string pointer itself)
extern HRESULT PopulateChoicesList (CComboBox&	cBox, LPCTSTR lpszVals[], LPCTSTR lpszDefVal=NULL);

extern HRESULT PopulateChoicesList (CComboBox& cBox, const CStr2PtrMapper& cm, LPCTSTR lpszDefVal=NULL);
extern HRESULT PopulateChoicesList (CComboBox& cBox, const CStr2StrMapper& cm, LPCTSTR lpszDefVal=NULL);

// returns (-1) if fails
extern DWORD GetSelectedItemData (const CComboBox& cBox);

/*---------------------------------------------------------------------------*/

// Note: returns ERROR_FILE_NOT_FOUND if default value not found in list
extern HRESULT SetDefaultSel (CListBox& lb, const DWORD defVal);

extern HRESULT PopulateChoicesList (CListBox&	lBox,
												const STLXL	sglXl[],
												const DWORD	dwDefData=(DWORD) (-1));

extern HRESULT PopulateChoicesList (CListBox&	lBox,
												const PTLXL	tglXl[],
												const DWORD	dwDefData=(DWORD) (-1));

// NULL terminate list of strings (associated data is the string pointer itself)
extern HRESULT PopulateChoicesList (CListBox&	lBox, LPCTSTR lpszVals[], LPCTSTR lpszDefVal=NULL);

/*---------------------------------------------------------------------------*/

#include <afxtempl.h>	// required for CArray object

class CListBoxMultiSelEnum {
	private:
		CArray<int,int>	m_SelItems;
		int					m_nTotSel;
		int					m_nCurSel;

	public:
		CListBoxMultiSelEnum (const CListBox& lBox);

		// return (-1) if no more items
		int GetFirstSelectedItem ()
		{
			m_nCurSel = 0;
			return GetNextSelectedItem();
		}

		int GetNextSelectedItem ();

		virtual ~CListBoxMultiSelEnum () { }
};

/*---------------------------------------------------------------------------*/

// places all available threads priorities in the combo-box
extern HRESULT PopulatePriosList (CComboBox& cBox, const int nDefPrio);

// returns (-1) if fails
inline int GetSelectedPriority (const CComboBox& cBox)
{
	return (int) GetSelectedItemData(cBox);
}

/*---------------------------------------------------------------------------*/

extern HRESULT UpdateListStr (LPCTSTR lpszVal,  const ULONG ulVLen,
										LPTSTR  lpszText, const ULONG cchTextMax);

inline HRESULT UpdateListStr (LPCTSTR lpszVal, LPTSTR  lpszText, const ULONG cchTextMax)
{
	return UpdateListStr(lpszVal, GetSafeStrlen(lpszVal), lpszText, cchTextMax);
}

extern HRESULT UpdateListStr (const ULONG ulVal, LPTSTR  lpszText, const ULONG cchTextMax);

/*---------------------------------------------------------------------------*/

// callback for releasing associated list data
//
// Note: 0 or (-1) data is skipped
typedef HRESULT (*LISTDATA_REL_CFN)(CListCtrl&	lCtrl,
												const int	nItem,
												const DWORD	dwData,
												LPVOID		pArg);

extern HRESULT ReleaseListItem (CListCtrl&			lCtrl,
										  const int				nItem,
										  LISTDATA_REL_CFN	lpfnRcfn,	// NULL == no release necessary
										  LPVOID					pArg);

extern HRESULT ReleaseAllContents (CListCtrl&			lCtrl,
											  LISTDATA_REL_CFN	lpfnRcfn=NULL,	// NULL == no release necessary
											  LPVOID					pArg=NULL);

extern HRESULT ReleaseSelectedContents (CListCtrl&			lCtrl,
													 LISTDATA_REL_CFN	lpfnRcfn=NULL,	// NULL == no release necessary
													 LPVOID				pArg=NULL);

extern void SetAllListItemsSelectionState (CListCtrl& lCtrl, const BOOL fSelected);

/*---------------------------------------------------------------------------*/

extern HRESULT DisplayEmptyListMessage (CListCtrl& lCtrl, LPCTSTR lpszMsg);
extern HRESULT DisplayEmptyListMessage (CListCtrl& lCtrl, const int nMsgID);

/*---------------------------------------------------------------------------*/

// returns:
//
//		ERROR_BAD_LENGTH - if width is not within specified min./max. values
//		ERROR_BUFFER_OVERFLOW - if total width exceeds list control client area
extern HRESULT VerifyListCtrlColWidth (const CListCtrl&	lCtrl,
													const UINT			nColIndex,			// index of changed column
													const UINT			nNewWidth,			// pixels
													const UINT			nMinWidth,			// percent
													const UINT			nMaxWidth=100);	// percent

inline HRESULT VerifyListCtrlColWidth (const CListCtrl&	lCtrl,
													const HD_NOTIFY&	hdn,
													const UINT			nMinWidth,		// percent
													const UINT			nMaxWidth=100)	// percent
{
	if (NULL == hdn.pitem)
		return ERROR_BAD_ENVIRONMENT;
	else
		return VerifyListCtrlColWidth(lCtrl, (UINT) hdn.iItem, (UINT) hdn.pitem->cxy, nMinWidth, nMaxWidth);
}

// calls "VerifyListCtrlColWidth" and updates the percentages only if verification successful
extern HRESULT UpdateListCtrlColumnWidth (const CListCtrl&	lCtrl,
														const UINT			nColIndex,
														const UINT			nNewWidth,	// pixels
														const UINT			nMinWidth,	// percent
														const UINT			nMaxWidth,	// percent (100 == no limit)
														CLISTCTRLCOLINFO	colInfo[]);

inline HRESULT UpdateListCtrlColumnWidth (const CListCtrl&	lCtrl,
														const HD_NOTIFY&	hdn,
														const UINT			nMinWidth,	// percent
														const UINT			nMaxWidth,	// percent (100 == no limit)
														CLISTCTRLCOLINFO	colInfo[])
{
	if (NULL == hdn.pitem)
		return ERROR_BAD_ENVIRONMENT;
	else
		return UpdateListCtrlColumnWidth(lCtrl, (UINT) hdn.iItem, (UINT) hdn.pitem->cxy, nMinWidth, nMaxWidth, colInfo);
}

/*---------------------------------------------------------------------------*/

// edit control sub-classed object for editing list control items
class CListItemEdit : public CEdit {
	protected:
		int		m_x;
		int		m_y;
		CString	m_orgText;	// original text (restored on ESCAPE)

		// returns TRUE if this is an ESC key (and was handled)
		virtual BOOL HandleEscapeKey (const UINT nChar);

	public:
		CListItemEdit ()
			: CEdit(), m_x(-1), m_y(-1), m_orgText(_T(""))
		{
		}

		virtual BOOL HaveItemPosition () const
		{
			return ((m_x >= 0) && (m_y >= 0));
		}

		virtual void GetItemPos (int& xPos, int& yPos) const
		{
			xPos = m_x;
			yPos = m_y;
		}

		virtual LPCTSTR GetOriginalText () const
		{
			return m_orgText;
		}

		virtual BOOL SetItemPos (const int xPos, const int yPos);

		virtual BOOL SetItemPos (const CListCtrl& lCtrl, const int nRow, const int nCol);

		virtual BOOL AttachItem (const CListCtrl& lCtrl, const int nRow, const int nCol);

		virtual BOOL DetachItem (const CListCtrl& lCtrl, const int nRow, const int nCol);

		virtual ~CListItemEdit()
		{
		}

		virtual BOOL PreTranslateMessage (MSG* pMsg);

	// Generated message map functions
	protected:
		// we need to override this member in order to adjust the relative position
		afx_msg void OnWindowPosChanging(WINDOWPOS FAR* lpwndpos);

		// we need this event in order to restore the original text
		afx_msg void OnKillFocus (CWnd* pNewWnd);

		// we need this in order to process ESC, UP/DOWN, PAGEUP/DOWN
		afx_msg void OnChar(UINT nChar, UINT nRepCnt, UINT nFlags);
		afx_msg void OnKeyUp(UINT nChar, UINT nRepCnt, UINT nFlags);

		DECLARE_MESSAGE_MAP()
};

/*---------------------------------------------------------------------------*/

class CListItemEditHandler {
	private:
		// row/column of currently edited item (or (-1) if none)
		int				m_nRow;
		int				m_nCol;

		CListItemEdit	m_ItemEdit;

	public:
		CListItemEditHandler ()
			: m_ItemEdit(), m_nRow(-1), m_nCol(-1)
		{
		}

		virtual BOOL IsEditing () const
		{
			return ((m_nRow >= 0) && (m_nCol >= 0));
		}

		virtual void GetEditedItem (int& nRow, int& nCol) const
		{
			nRow = m_nRow;
			nCol = m_nCol;
		}

		// if returns FALSE, then "*pResult" of the LVN_BEGINLABELEDIT handler must be set to TRUE (and viceversa)
		virtual BOOL BeginEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo, const CPoint& cursorPos);
		virtual BOOL BeginEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo);

		// does not set the new item text
		virtual BOOL EndEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo, CString& strNewValue);

		// also sets the edited item to the new text
		virtual BOOL EndEdit (CListCtrl& lCtrl, const LV_DISPINFO& dispInfo);

		~CListItemEditHandler ()
		{
		}
};

/*---------------------------------------------------------------------------*/

class CListCtrlRedrawGuard {
	private:
		CListCtrlRedrawGuard (const CListCtrlRedrawGuard& );
		CListCtrlRedrawGuard& operator= (const CListCtrlRedrawGuard& );

	protected:
		CListCtrl&	m_lCtrl;
		BOOL			m_fIsEnabled;

	public:
		// NOTE !!! is using this guard, then all redraw changes MUST be made through it
		CListCtrlRedrawGuard (CListCtrl& lCtrl, const BOOL fInitialState)
			: m_lCtrl(lCtrl), m_fIsEnabled(fInitialState)
		{
		}

		virtual void SetState (const BOOL fIsEnabled)
		{
			m_lCtrl.SetRedraw(fIsEnabled);
			m_fIsEnabled = fIsEnabled;
		}

		virtual BOOL IsEnabled () const
		{
			return m_fIsEnabled;
		}

		virtual void Release ()
		{
			if (!m_fIsEnabled)
				SetState(TRUE);
		}

		virtual ~CListCtrlRedrawGuard ()
		{
			Release();
		}
};

/*---------------------------------------------------------------------------*/

// returns item number (or < 0 if failed)
extern int AddExCBoxChoice (CComboBoxEx&			cBox,
									 LPCTSTR					lpszValue,	// may be NULL/empty if icon specified
									 const int				nIconID,		// can be (-1) if no icon
									 const IMGLISTASSOC	imga[],		// must be initialized via "CreateImgList" (if nIconID != (-1))
									 const DWORD			dwData=(DWORD) (-1));

typedef struct {
	LPCTSTR	lpszValue;
	int		nIconID;	// if (-1) then no icon is associated
	DWORD		dwData;
} XTACL;

inline int AddExCBoxChoice (CComboBoxEx& cBox, const XTACL& xa, const IMGLISTASSOC imga[])
{
	return AddExCBoxChoice(cBox, xa.lpszValue, xa.nIconID, imga, xa.dwData);
}

extern HRESULT PopulateExtendedChoicesList (CComboBoxEx&			cBox,		// must have the matching image list set
														  const IMGLISTASSOC	imga[],	// must be initialized via "CreateImgList"
														  const XTACL			xtas[],	// last item must have a NULL string value AND (-1) icon ID
														  const DWORD			dwDefData=(DWORD) (-1));

/*---------------------------------------------------------------------------*/

/* Inspired by MSDN KB174667 "How to subclass CListBox and CEdit inside of CComboBox"
 *
 * NOTE: for subclassing to occur, the dialog box must be painted at least
 *			ONCE. There are cases when the dialog box doesn't paint at all (for
 *			example, closing the dialog box before it is displayed, hidden dialog
 *			boxes). This method may not be suitable when access to the subclassed
 *			windows are needed in these cases).
 */
class CSuperComboBox : public CComboBox {
	protected:
		// Called to supply the edit control which will be subclassed for the combobox's control
		virtual CEdit& GetEditControl () = 0;
		// Called to supply the list-box control which will be subclassed for the combobox's control
		virtual CListBox& GetListBoxControl () = 0;

		// call these methods from their matching sub-classed handlers
		HBRUSH OnCtlColor (CDC* pDC, CWnd* pWnd, UINT nCtlColor);
		void OnDestroy ();
};

/////////////////////////////////////////////////////////////////////////////

// keeps a history of everything typed in the edit box and ENTER-ed
class CInputList : public CSuperComboBox {
private:
	// key=input string (case sensitive), value=time value when it was inserted
	CStr2PtrMapper	m_valsMap;
	CEdit				m_edt;
	CListBox			m_lst;
	int				m_maxHistory;

protected:
		// Called to supply the edit control which will be subclassed for the combobox's edit control
		virtual CEdit& GetEditControl ()
		{
			return m_edt;
		}

		virtual CListBox& GetListBoxControl ()
		{
			return m_lst;
		}

public:
	CInputList();
	virtual ~CInputList();

public:
	virtual int InsertString (int nIndex, LPCTSTR lpszItem);

	// called when ENTER is pressed on the edit control
	// returns index of inserted item in history (<0 if error)
	virtual int UpdateEnteredText (LPCTSTR lpszItem)
	{
		return InsertString(0, lpszItem);
	}

	virtual int GetMaxHistory () const
	{
		return m_maxHistory;
	}

	// returns previous value (<0 if illegal argument)
	// NOTE: mapped history is LOST if max history greater than current
	virtual int SetMaxHistory (int maxHistory);

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CInputList)
	public:
		virtual BOOL PreTranslateMessage(MSG* pMsg);
	//}}AFX_VIRTUAL

		// Generated message map functions
protected:
	//{{AFX_MSG(CInputList)
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
	afx_msg void OnDestroy();
	afx_msg void OnSelChange();
	//}}AFX_MSG

	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

#endif /* _MFC_CLISTCTRL_H_ */