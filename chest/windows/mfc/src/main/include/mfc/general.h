#ifndef _MFCUTLGNRL_H_
#define _MFCUTLGNRL_H_

#include <mfc/mfcutlbase.h>

/*--------------------------------------------------------------------------*/

extern BOOL SetWindowText (CWnd& wnd, const int nStrID);
extern HRESULT SetUTF8WindowText (CWnd& wnd, LPCTSTR lpszUTF8str, const ULONG ulUTF8Len);
inline HRESULT SetUTF8WindowText (CWnd& wnd, LPCTSTR lpszUTF8str)
{
	return SetUTF8WindowText(wnd, lpszUTF8str, ((NULL == lpszUTF8str) ? 0 : _tcslen(lpszUTF8str)));
}

/*--------------------------------------------------------------------------*/

typedef struct {
	CWnd	*pWnd;
	int	nTitleID;
} WXLTITLE;

extern HRESULT InitFieldsTitles (WXLTITLE	xlt[]);

typedef struct {
	CWnd		*pWnd;
	LPCTSTR	lpszWndText;
} WXLTEXT;

extern HRESULT InitFieldsTexts (WXLTEXT	xlt[]);

typedef struct {
	int		nID;
	LPCTSTR	lpszWndText;
} WXLID2TXT;

extern HRESULT InitFieldsTitles (const CWnd&	wnd, const WXLID2TXT xlt[]);

/*--------------------------------------------------------------------------*/

extern HRESULT CheckWndNonEmptyField (const CWnd& wnd, const int nID, BOOL& fNonEmpty);

extern HRESULT CheckWndNonEmptyFields (const CWnd& wnd,
													const int	nIDs[],
													BOOL&			fNonEmpty);

extern HRESULT CheckWndNonZeroField (const CWnd& wnd, const int nID, BOOL& fNonZero);

extern HRESULT CheckWndNonZeroFields (const CWnd&	wnd,
												  const int		nIDs[],
												  BOOL&			fNonZero);

extern HRESULT SetWndFieldState (const CWnd&	wnd,
											const int	nID,
											const BOOL	fEnabled,		// EnableWindow
											const int	nCmdShow=(-1),	// ShowWindow (-1) == no change
											const BOOL	fIsEdit=FALSE);

// Note: if "pWnd" is NULL then it is updated
extern HRESULT SetWndFieldsState (const CWnd&	wnd,
											 WXLTITLE		xlt[],
											 const BOOL		fEnabled,		// EnableWindow
											 const int		nCmdShow=(-1),	// ShowWindow (-1) == no change
											 const BOOL		fIsEdit=FALSE);

extern HRESULT SetWndFieldsState (const CWnd&	wnd,
											 const int		nIDs[],
											 const BOOL		fEnabled,		// EnableWindow
											 const int		nCmdShow=(-1),	// ShowWindow (-1) == no change
											 const BOOL		fIsEdit=FALSE);

extern HRESULT GetCheckButtonState (const CWnd& wnd, const int nID, BOOL& fChecked);

/*--------------------------------------------------------------------------*/

extern BOOL RepostMsgToParent (const CWnd& wnd, UINT message, WPARAM wParam=0, LPARAM lParam=0);

inline BOOL RepostCmdToParent (const CWnd& wnd, UINT nCmd,  LPARAM lParam=0)
{
	return RepostMsgToParent(wnd, WM_COMMAND, (WPARAM) nCmd, lParam);
}

/*--------------------------------------------------------------------------*/

extern BOOL IsKeyDownMsg (const MSG *pMsg, const WPARAM wKey);

inline BOOL IsEscapeKeyDownMsg (const MSG *pMsg)
{
	return IsKeyDownMsg(pMsg, VK_ESCAPE);
}

#define MIN_VK_FKEY		1
#define MAX_VK_FKEY		12

inline BOOL IsFunctionKeyDownMsg (const MSG *pMsg, const WPARAM fkNum)
{
	if ((fkNum >= MIN_VK_FKEY) && (fkNum <= MAX_VK_FKEY))
		return IsKeyDownMsg(pMsg, (VK_F1+fkNum-1));
	else
		return FALSE;
}

/*--------------------------------------------------------------------------*/

extern BOOL ResizeWndControl (CWnd&			wnd,
										CWnd&			ctl,
										const int	dx,
										const int	dy);

extern BOOL OccupyWndClientArea (CWnd& wndParent, CWnd& wndChild);

/*--------------------------------------------------------------------------*/

/* based on "Naming common colors" by Ales Krajnc from www.codeproject.com */
typedef enum {
	colAliceBlue=0,
	colAntiqueWhite,
	colAqua,
	colAquamarine,
	colAzure,
	colBeige,
	colBisque,
	colBlack,
	colBlanchedAlmond,
	colBlue,
	colBlueViolet,
	colBrown,
	colBurlywood,
	colCadetBlue,
	colChartreuse,
	colChocolate,
	colCoral,
	colCornflowerBlue,
	colCornsilk,
	colCrimson,
	colCyan,
	colDarkBlue,
	colDarkCyan,
	colDarkGoldenRod,
	colDarkGray,
	colDarkGreen,
	colDarkKhaki,
	colDarkMagenta,
	colDarkOliveGreen,
	colDarkOrange,
	colDarkOrchid,
	colDarkRed,
	colDarkSalmon,
	colDarkSeaGreen,
	colDarkSlateBlue,
	colDarkSlateGray,
	colDarkTurquoise,
	colDarkViolet,
	colDeepPink,
	colDeepSkyBlue,
	colDimGray,
	colDodgerBlue,
	colFireBrick,
	colFloralWhite,
	colForestGreen,
	colFuchsia,
	colGainsboro,
	colGhostWhite,
	colGold,
	colGoldenRod,
	colGray,
	colGreen,
	colGreenYellow,
	colHoneyDew,
	colHotPink,
	colIndianRed,
	colIndigo,
	colIvory,
	colKhaki,
	colLavender,
	colLavenderBlush,
	colLawngreen,
	colLemonChiffon,
	colLightBlue,
	colLightCoral,
	colLightCyan,
	colLightGoldenRodYellow,
	colLightGreen,
	colLightGrey,
	colLightPink,
	colLightSalmon,
	colLightSeaGreen,
	colLightSkyBlue,
	colLightSlateGray,
	colLightSteelBlue,
	colLightYellow,
	colLime,
	colLimeGreen,
	colLinen,
	colMagenta,
	colMaroon,
	colMediumAquamarine,
	colMediumBlue,
	colMediumOrchid,
	colMediumPurple,
	colMediumSeaGreen,
	colMediumSlateBlue,
	colMediumSpringGreen,
	colMediumTurquoise,
	colMediumVioletRed,
	colMidnightBlue,
	colMintCream,
	colMistyRose,
	colMoccasin,
	colNavajoWhite,
	colNavy,
	colNavyblue,
	colOldLace,
	colOlive,
	colOliveDrab,
	colOrange,
	colOrangeRed,
	colOrchid,
	colPaleGoldenRod,
	colPaleGreen,
	colPaleTurquoise,
	colPaleVioletRed,
	colPapayaWhip,
	colPeachPuff,
	colPeru,
	colPink,
	colPlum,
	colPowderBlue,
	colPurple,
	colRed,
	colRosyBrown,
	colRoyalBlue,
	colSaddleBrown,
	colSalmon,
	colSandyBrown,
	colSeaGreen,
	colSeaShell,
	colSienna,
	colSilver,
	colSkyBlue,
	colSlateBlue,
	colSlateGray,
	colSnow,
	colSpringGreen,
	colSteelBlue,
	colTan,
	colTeal,
	colThistle,
	colTomato,
	colTurquoise,
	colViolet,
	colWheat,
	colWhite,
	colWhiteSmoke,
	colYellow,
	colYellowGreen,
	colBadColor
} WKREFCOLORCASE;

#define fIsBadKnownColorCase(c)	(((unsigned) (c)) >= ((unsigned) colBadColor))

extern const COLORREF wellKnownColors[];
extern LPCTSTR lpszWellKnownColorsNames[];

inline COLORREF GetWellKnownColorValue (const WKREFCOLORCASE wkr)
{
	return (fIsBadKnownColorCase(wkr) ? wellKnownColors[colBlack] : wellKnownColors[wkr]);
}

// returns NULL if illegal name
inline LPCTSTR GetWellKnownColorName (const WKREFCOLORCASE wkr)
{
	return (fIsBadKnownColorCase(wkr) ? NULL : lpszWellKnownColorsNames[wkr]);
}

extern WKREFCOLORCASE GetWellKnownColorCaseByName (LPCTSTR lpszColName);

inline COLORREF GetWellKnownColorValueByName (LPCTSTR lpszColName)
{
	return GetWellKnownColorValue(GetWellKnownColorCaseByName(lpszColName));
}

/*--------------------------------------------------------------------------*/

class CMFCUTLCmdLineParser : public CCommandLineInfo {
	protected:
		HRESULT&	m_hr;

		CMFCUTLCmdLineParser (HRESULT& hr)
			: CCommandLineInfo(), m_hr(hr)
		{
			m_hr = S_OK;
		}

		virtual void ShowUsage () = 0;

		virtual HRESULT ShowUsageError (const HRESULT rhr)
		{
			ShowUsage();
			return rhr;
		}

		virtual HRESULT ParseFlag (LPCTSTR lpszFlag) = 0;

		virtual HRESULT ParseValue (LPCTSTR lpszVal) = 0;

	public:
		virtual void ParseParam (LPCTSTR lpszParam, BOOL bFlag, BOOL bLast)
		{
			if (m_hr != S_OK)
				return;

			if (bFlag)
				m_hr = ParseFlag(lpszParam);
			else
				m_hr = ParseValue(lpszParam);
		}

		virtual ~CMFCUTLCmdLineParser ()
		{
		}
};

/*--------------------------------------------------------------------------*/

// class to guard a DC pointer
class CCDCGuard {
	private:
		CWnd&	m_wnd;
		CDC*	m_pTmpDC;
		CDC*	&m_pDC;

	public:
		CCDCGuard (CWnd& wnd, CDC* &pDC)
			: m_wnd(wnd)
			, m_pDC(pDC)
			, m_pTmpDC(NULL)
		{
		}

		CCDCGuard (CWnd& wnd, CDC& dc)
			: m_wnd(wnd)
			, m_pTmpDC(&dc)
			, m_pDC(m_pTmpDC)
		{
		}

		virtual int Release ()
		{
			if (m_pDC != NULL)
			{
				int	nRes=m_wnd.ReleaseDC(m_pDC);
				m_pDC = NULL;
				return nRes;
			}

			return TRUE;
		}

		virtual ~CCDCGuard ()
		{
			Release();
		}
};

/*--------------------------------------------------------------------------*/

template<class DCOBJ>class CDCSelObjectGuard {
	protected:
		CDC&	m_dc;
		DCOBJ	*m_pOld;
		bool	m_fSelected;

	public:
		CDCSelObjectGuard (CDC& dc, DCOBJ& obj)
			: m_dc(dc)
			, m_pOld(dc.SelectObject(&obj))
			, m_fSelected(true)
		{
		}

		virtual DCOBJ *Release ()
		{
			if (m_fSelected)
			{
				DCOBJ	*retVal=m_dc.SelectObject(m_pOld);
				m_pOld = NULL;
				m_fSelected = false;
				return retVal;
			}

			return NULL;
		}

		virtual ~CDCSelObjectGuard ()
		{
			Release();
		}
};

typedef CDCSelObjectGuard<CBrush> CDCSelBrushGuard;
typedef CDCSelObjectGuard<CPen> CDCSelPenGuard;
typedef CDCSelObjectGuard<CFont> CDCSelFontGuard;
typedef CDCSelObjectGuard<CBitmap> CDCSelBitmapGuard;

/*--------------------------------------------------------------------------*/

extern void DateTimePickersToSystemTime (const CDateTimeCtrl& srcDate, const CDateTimeCtrl& srcTime, SYSTEMTIME& sysTime);

/*--------------------------------------------------------------------------*/

#endif	/* of _MFCUTLGNRL_H_ */