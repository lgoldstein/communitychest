#ifndef _MFCUTLBASE_H_
#define _MFCUTLBASE_H_

/*---------------------------------------------------------------------------*/

#ifndef __cplusplus
#error "This file requires a C++ compiler !!!"
#endif

#include <afxwin.h>
#include <afxext.h>         // MFC extensions
#include <afxdtctl.h>		// MFC support for Internet Explorer 4 Common Controls
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>			// MFC support for Windows Common Controls
#endif // _AFX_NO_AFXCMN_SUPPORT

#include <afxsock.h>		// MFC socket extensions

/*---------------------------------------------------------------------------*/

#define WMMFCUTLBASE					(WM_APP+100)

#define WMMFCUTL_SPLITTER_MOVED	(WMMFCUTLBASE+1)

/*---------------------------------------------------------------------------*/

// association structures
typedef struct {
	LPCTSTR	lpszStr;
	DWORD		dwSelData;
} STLXL;

typedef struct {
	int	nSelName;
	DWORD	dwSelData;
} PTLXL;

/*--------------------------------------------------------------------------*/

// clipboard reverse engineered formats !!!
#define REVCF_FILENAME					49158	// same as RegisterClipboardFormat(CFSTR_FILENAMEA) - see <shlobj.h>
#define REVCF_FILENAMEW					49159	// same as RegisterClipboardFormat(CFSTR_FILENAMEW) - see <shlobj.h>
#define REVCF_SELLIDLISTARRAY			49227	// same as RegisterClipboardFormat(CFSTR_SHELLIDLIST) - see <shlobj.h>
#define REVCF_SHELLOBJECTOFFSETS		49262	// same as RegisterClipboardFormat(CFSTR_SHELLIDLISTOFFSET) - see <shlobj.h>

/*--------------------------------------------------------------------------*/

#endif
