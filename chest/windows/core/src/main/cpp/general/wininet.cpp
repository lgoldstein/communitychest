#include <win32/general.h>

/////////////////////////////////////////////////////////////////////////////

static HRESULT EnumDomainServers (HANDLE			hEnum,
											 WNET_ENUM_CFN lpfnEcfn,
											 LPVOID			pArg,
											 const DWORD	dwESize)
{
	if ((NULL == hEnum) || (NULL == lpfnEcfn) || (0 == dwESize))
		return ERROR_BAD_ARGUMENTS;

	LPNETRESOURCE	lpnrLocal=(LPNETRESOURCE) GlobalAlloc(GPTR, dwESize);
	if (NULL == lpnrLocal)
		return ERROR_OUTOFMEMORY;

	HRESULT	hr=ERROR_SUCCESS;
	BOOL		fContEnum=TRUE;
	DWORD		dwEntries=0xFFFFFFFF;	// enumerate all possible entries

	for ( ; (ERROR_SUCCESS == hr) && fContEnum; )
	{
		DWORD	dwBuffer=dwESize;

		if ((hr=WNetEnumResource(hEnum, &dwEntries, lpnrLocal, &dwBuffer)) != NO_ERROR)
		{
			if (ERROR_NO_MORE_ITEMS == hr)
				hr = ERROR_SUCCESS;
			break;
		}

		for (DWORD i=0 ; i < dwEntries ; i++)
		{
			const NETRESOURCE& nr=lpnrLocal[i];
			if ((hr=(*lpfnEcfn)(nr, pArg, fContEnum)) != ERROR_SUCCESS)
				break;
		}
	}

	GlobalFree((HGLOBAL) lpnrLocal);
	return hr;
}

/*--------------------------------------------------------------------------*/

HRESULT EnumDomainServers (WNET_ENUM_CFN lpfnEcfn, LPVOID pArg, const DWORD dwESize)
{
	HANDLE	hEnum=NULL;
	HRESULT	hr=WNetOpenEnum(RESOURCE_CONTEXT,	// scope of enumeration
  									 RESOURCETYPE_ANY,	// resource types to list
									 0,						// resource usage to list
									 NULL,					// pointer to resource structure
									 &hEnum);				// pointer to enumeration handle buffer
	if (hr != NO_ERROR)
		return hr;

	hr = EnumDomainServers(hEnum, lpfnEcfn, pArg, dwESize);

	WNetCloseEnum(hEnum);
	return hr;
}

/////////////////////////////////////////////////////////////////////////////
