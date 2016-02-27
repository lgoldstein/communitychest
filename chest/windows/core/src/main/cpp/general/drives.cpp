#include <string.h>
#include <limits.h>

#include <wtypes.h>
#include <winbase.h>
#include <winerror.h>

#ifndef WIN32
#error "This file is intended for WIN32 !!!"
#endif

#include <win32/general.h>

/*---------------------------------------------------------------------------*/

typedef struct {
	UINT			uDrvType;
	const char	*pszDrvType;
} DRVTYPEXLATE;

#define DRVUNKNOWN_IDX	0

/*---------------------------------------------------------------------------*/

static const DRVTYPEXLATE drvTypeXlator[]={
	{	DRIVE_UNKNOWN,			"UNKNOWN"	},
	{	DRIVE_NO_ROOT_DIR,	"NO-ROOT"	},
	{	DRIVE_REMOVABLE,		"REMOVABLE"	},
	{	DRIVE_FIXED,			"FIXED"		},
	{	DRIVE_REMOTE,			"REMOTE"		},
	{	DRIVE_CDROM,			"CD-ROM"		},
	{	DRIVE_RAMDISK,			"RAM-DISK"	},
	{		0				,		NULL			}	/* mark end of list */
};

/*---------------------------------------------------------------------------*/

/* returns type of drive (e.g. CD-ROM, network, etc.) */
const char *XlateDriveType (const UINT uDrvType)
{
	const DRVTYPEXLATE	*pXlate=&drvTypeXlator[0];

	while (pXlate->pszDrvType != NULL)
		if (uDrvType == pXlate->uDrvType)
			return pXlate->pszDrvType;
		else
			pXlate++;

	return drvTypeXlator[DRVUNKNOWN_IDX].pszDrvType;
}

/*---------------------------------------------------------------------------*/

/*		Enumerates current drives. Note: removable drives (e.g. floppy, CD) may
 * be enumerated without their volume information if they are not present.
 */
HRESULT EnumSystemDrives (SYSDRV_E_CFN	lpfnEcfn, LPVOID	pArg)
{
	char			szDrives[MAX_PATH];
	const char	*pszDriveName=szDrives;
	ULONG			ulCount=0;
	HRESULT		hr=ERROR_SUCCESS;

	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	/* get drives list: "A:\.B:\...." */
	memset(szDrives, 0, (sizeof szDrives));
	ulCount = GetLogicalDriveStrings((sizeof szDrives), szDrives);
	if ((0 == ulCount) || (ulCount >= (sizeof szDrives)))
	{
		hr = GetLastError();
		goto Quit;
	}

	while (*pszDriveName != '\0')
	{
		UINT		uDrvType=DRIVE_UNKNOWN;
		DWORD		dwSerialNumber=0, dwMaxCompLen=0, dwFsysFlags=0;
		char		szVolName[MAX_PATH], szFsysName[MAX_PATH];

		if (DRIVE_UNKNOWN == (uDrvType=GetDriveType(pszDriveName)))
		{
			hr = GetLastError();
			goto Quit;
		}

		if (!GetVolumeInformation(pszDriveName,
										  szVolName, (sizeof szVolName),
										  &dwSerialNumber,
										  &dwMaxCompLen,
										  &dwFsysFlags,
										  szFsysName, (sizeof szFsysName)))
		{
			/* 
			 *		If this is not a fixed drive then allow for no volume info,
			 * e.g. the floppy/CD may not be in the drive at the moment.
			 */

			if (DRIVE_FIXED != uDrvType)
			{
				szVolName[0] = '\0';
				dwSerialNumber = 0;
				szFsysName[0] = '\0';
				dwFsysFlags = 0;
			}
			else
			{
				hr = GetLastError();
				goto Quit;
			}
		}

		/* inform caller */
		if (!(*lpfnEcfn)(pszDriveName, uDrvType, szVolName, dwSerialNumber,
							  dwFsysFlags, szFsysName, pArg))
			break;

		/* skip to next drive */
		while (*pszDriveName != '\0') pszDriveName++;
		pszDriveName++;
	}

	hr = ERROR_SUCCESS;

Quit:
	return hr;
}

/*---------------------------------------------------------------------------*/
