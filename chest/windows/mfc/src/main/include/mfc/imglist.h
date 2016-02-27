#ifndef _MFUTLIMGLIST_H_
#define _MFUTLIMGLIST_H_

#include <mfc/mfcutlbase.h>

/*---------------------------------------------------------------------------*/

typedef struct {
	int	nIconID;
	int	nIconNdx;
} IMGLISTASSOC;

/*---------------------------------------------------------------------------*/

extern HRESULT PopulateImgList (CImageList&	imgList,
										  CWinApp&		app,
										  IMGLISTASSOC	ima[]);	// last entry has (-1) icon ID

extern HRESULT CreateImgList (CImageList&		imgList,
										CWinApp&			app,
										const BOOL		fLargeIcons,
										IMGLISTASSOC	ima[],	// last entry has (-1) icon ID
										const UINT		uFlags=(ILC_COLOR | ILC_MASK),
										const int		nInitial=(-1),	// (-1) means use list assocs size
										const int		nGrow=5);

// returns (-1) if not found
extern int GetAssocImageIndex (const int nIconID, const IMGLISTASSOC	ima[]);

/*---------------------------------------------------------------------------*/

#endif /* _MFUTLIMGLIST_H_ */
