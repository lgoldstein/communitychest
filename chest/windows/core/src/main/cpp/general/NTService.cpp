/*---------------------------------------------------------------------------*/


#ifndef __cplusplus
#error "This file requires a C++ compiler !!!"
#endif

// NTService.cpp
//
// Implementation of CNTService

/*---------------------------------------------------------------------------*/

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <limits.h>

#include <util/string.h>

#include <win32/NTService.h>

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::Close ()
{
	if (NULL == m_hSCM)
		return S_OK;

	HRESULT	hr=S_OK;
	if (!::CloseServiceHandle(m_hSCM))
	{
		hr = ::GetLastError();
		if (S_OK == hr)
			hr = ERROR_INVALID_HANDLE;
	}

	m_hSCM = NULL;

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::Attach (SC_HANDLE hScm)
{
	if (m_hSCM != NULL)
		return ERROR_ALREADY_EXISTS;
	if (NULL == (m_hSCM=hScm))
		return ERROR_INVALID_HANDLE;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::Open (LPCTSTR		lpMachineName,   // pointer to machine name string
										  LPCTSTR		lpDatabaseName,  // pointer to database name string
										  const DWORD	dwDesiredAccess)	// type of access
{
	if (m_hSCM != NULL)
		return ERROR_ALREADY_INITIALIZED;

	if (NULL == (m_hSCM=::OpenSCManager(lpMachineName, lpDatabaseName, dwDesiredAccess)))
	{
		HRESULT	hr=::GetLastError();
		return (S_OK == hr) ?  ERROR_INVALID_HANDLE : hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::OpenService (LPCTSTR		lpServiceName, // pointer to name of service to start
													const DWORD	dwDesiredAccess,  // type of access to service
													SC_HANDLE&	hSvc) const
{
	hSvc = NULL;

	if (NULL == m_hSCM)
		return ERROR_INVALID_HANDLE;

	if (NULL == (hSvc=::OpenService(m_hSCM, lpServiceName, dwDesiredAccess)))
	{
		HRESULT	hr=::GetLastError();
		return (S_OK == hr) ?  ERROR_INVALID_HANDLE : hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::CreateService (LPCTSTR		lpServiceName,      // name of service to start
													  LPCTSTR		lpDisplayName,      // display name
													  const DWORD	dwDesiredAccess,      // type of access to service
													  const DWORD	dwServiceType,        // type of service
													  const DWORD	dwStartType,          // when to start service
													  const DWORD	dwErrorControl,       // severity of service failure
													  LPCTSTR		lpBinaryPathName,   // name of binary file
													  LPCTSTR		lpLoadOrderGroup,   // name of load ordering group
													  LPDWORD		lpdwTagId,          // receives tag identifier
													  LPCTSTR		lpDependencies,     // array of dependency names
													  LPCTSTR		lpServiceStartName, // service account name 
													  LPCTSTR		lpPassword,         // account password
													  SC_HANDLE&	hSVC) const
{
	hSVC = NULL;

	if (NULL == m_hSCM)
		return ERROR_INVALID_HANDLE;
	if (IsEmptyStr(lpServiceName))
		return ERROR_SOURCE_ELEMENT_EMPTY;
	if (IsEmptyStr(lpBinaryPathName))
		return ERROR_ILLEGAL_ELEMENT_ADDRESS;

	if (NULL == (hSVC=::CreateService(m_hSCM, lpServiceName, lpDisplayName, dwDesiredAccess, dwServiceType, dwStartType, dwErrorControl,
												 lpBinaryPathName, lpLoadOrderGroup, lpdwTagId, lpDependencies, lpServiceStartName, lpPassword)))
	{
		HRESULT hr=::GetLastError();
		return (S_OK == hr) ?  ERROR_INVALID_HANDLE : hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::HandleService (LPCTSTR lpszSrvcName, const BOOL fStart)
{
	if (IsEmptyStr(lpszSrvcName))
		return ERROR_BAD_ARGUMENTS;

	LPCTSTR	lpszSrvcs[]={ lpszSrvcName, NULL };
	DWORD		dwSrvcsNum=0;
	HRESULT	hr=HandleServices(lpszSrvcs, fStart, dwSrvcsNum);
	if (hr != S_OK)
		return hr;

	if (dwSrvcsNum != 1)
		return ERROR_ARENA_TRASHED;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CServicesManager::DeleteService (LPCTSTR lpszSrvcName) const
{
	if (IsEmptyStr(lpszSrvcName))
		return ERROR_SOURCE_ELEMENT_EMPTY;

	if (NULL == m_hSCM)
		return ERROR_INVALID_HANDLE;

	SC_HANDLE	hService=NULL;
	HRESULT		hr=OpenService(lpszSrvcName, DELETE, hService);
	if (hr != S_OK)
		return hr;

	if (!::DeleteService(hService))
	{
		hr = ::GetLastError();
		if (S_OK == hr)
			hr =ERROR_INVALID_HANDLE;
	}

	::CloseServiceHandle(hService);

	return hr;
}

/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

class CSvcStatusArg {
	private:
		const LPCTSTR		m_lpszSvcName;
		SERVICE_STATUS&	m_svcStatus;
		HRESULT				m_hr;

		HRESULT CheckStatus (const ENUM_SERVICE_STATUS& eSvcStatus);

	public:
		CSvcStatusArg (LPCTSTR lpszSvcName, SERVICE_STATUS& svcStatus)
			: m_lpszSvcName(GetSafeStrPtr(lpszSvcName))
			, m_svcStatus(svcStatus)
			, m_hr(ERROR_SECTOR_NOT_FOUND)
		{
		}

		static BOOL svcsEcfn (SC_HANDLE							hSCM,
									 const LPENUM_SERVICE_STATUS	pSvcEnumStatus,
									 LPVOID								pArg)
		{
			if ((pArg != NULL) && (pSvcEnumStatus != NULL))
				return (((CSvcStatusArg *) pArg)->CheckStatus(*pSvcEnumStatus) != S_OK);

			return TRUE;
		}

		HRESULT VerifyStatus () const
		{
			return m_hr;
		}
};

/*---------------------------------------------------------------------------*/

HRESULT CSvcStatusArg::CheckStatus (const ENUM_SERVICE_STATUS& eSvcStatus)
{
	if (S_OK == m_hr)	// make sure not called more than once
		m_hr = ERROR_ALREADY_EXISTS;

	if (m_hr != ERROR_SECTOR_NOT_FOUND)
		return m_hr;

	if ((eSvcStatus.lpServiceName != NULL) && (0 == ::_tcsicmp(eSvcStatus.lpServiceName, m_lpszSvcName)))
	{
		m_svcStatus = eSvcStatus.ServiceStatus;
		m_hr = S_OK;
	}

	return m_hr;
}

/*+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-*/

HRESULT CServicesManager::GetServiceStatus (LPCTSTR lpszSvcName, SERVICE_STATUS& svcStatus) const
{
	::memset(&svcStatus, 0, (sizeof svcStatus));

	if (IsEmptyStr(lpszSvcName))
		return ERROR_SOURCE_ELEMENT_EMPTY;

	CSvcStatusArg	ssa(lpszSvcName, svcStatus);
	HRESULT	hr=EnumServices(SERVICE_WIN32, SERVICE_ACTIVE, CSvcStatusArg::svcsEcfn, (LPVOID) &ssa);
	if (hr != S_OK)
		return hr;

	if ((hr=ssa.VerifyStatus()) != S_OK)
		return hr;

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

CNTService *CNTService::m_pThis=NULL;

CNTService::CNTService (const ULONG ulMajorVersion, const ULONG ulMinorVersion)
	: m_ulMajorVersion(ulMajorVersion), m_ulMinorVersion(ulMinorVersion), m_hServiceStatus(NULL), m_bIsRunning(FALSE)
{
	memset(m_szServiceName, 0, (sizeof m_szServiceName));
	memset(m_szServiceDisplayName, 0, (sizeof m_szServiceDisplayName));

	memset(&m_Status, 0, (sizeof m_Status));
	m_Status.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
	m_Status.dwCurrentState = SERVICE_STOPPED;
	m_Status.dwControlsAccepted = SERVICE_ACCEPT_STOP;
}

/*---------------------------------------------------------------------------*/

HRESULT CNTService::Init (LPCTSTR lpszSrvcName, LPCTSTR lpszSrvcDispName)
{
	if (IsEmptyStr(lpszSrvcName))
		return ERROR_SOURCE_ELEMENT_EMPTY;

	if ((m_szServiceName[0] != _T('\0')) || (m_pThis != NULL))
		return ERROR_ALREADY_INITIALIZED;
	if (_tcslen(lpszSrvcName) > MAX_SERVICE_NAME_LEN)
		return ERROR_BUFFER_OVERFLOW;
	_tcscpy(m_szServiceName, lpszSrvcName);

	if (!IsEmptyStr(lpszSrvcDispName))
	{
		if (_tcslen(lpszSrvcDispName) > MAX_SERVICE_DISPLY_NAME_LEN)
			return ERROR_BUFFER_OVERFLOW;
		_tcscpy(m_szServiceDisplayName, lpszSrvcDispName);
	}
	else	// don't have a display name - use service name
	{
		if (_tcslen(lpszSrvcName) > MAX_SERVICE_DISPLY_NAME_LEN)
			return ERROR_BUFFER_OVERFLOW;
		_tcscpy(m_szServiceDisplayName, lpszSrvcName);
	}

	m_pThis = this;
	return S_OK;
}

/*---------------------------------------------------------------------------*/

CNTService::~CNTService()
{
	m_pThis = NULL;
}

/*---------------------------------------------------------------------------*/

// Test if the service is currently installed
BOOL CNTService::IsInstalled (void) const
{
	CServicesManager	scm;
	HRESULT				hr=scm.Open();
	if (hr != S_OK)
		return FALSE;

	// Try to open the service
	SC_HANDLE hService=NULL;
	if ((hr=scm.OpenService(m_szServiceName, SERVICE_QUERY_CONFIG, hService)) != S_OK)
		return FALSE;

	::CloseServiceHandle(hService);
	return TRUE;
}

/*---------------------------------------------------------------------------*/

// Install the service
HRESULT CNTService::Install (const BOOL fAutoStart)
{
	CServicesManager	scm;
	HRESULT				hr=scm.Open();
	if (hr != S_OK)
		return hr;

	// Get the executable file path
	TCHAR szFilePath[MAX_PATH+2]=_T("");
	DWORD	dwFLen=::GetModuleFileName(NULL, szFilePath, MAX_PATH);
	if ((0 == dwFLen) || (dwFLen >= MAX_PATH))
	{
		hr = GetLastError();
		return ((S_OK == hr) ? ERROR_MAGAZINE_NOT_PRESENT : hr);
	}

    // Create the service
	SC_HANDLE	hService=NULL;
	if ((hr=scm.CreateSimpleService(m_szServiceName, m_szServiceDisplayName, szFilePath, fAutoStart, hService)) != S_OK)
		return hr;
	::CloseServiceHandle(hService);

	return S_OK;
}

/*---------------------------------------------------------------------------*/

HRESULT CNTService::Uninstall (void)
{
	CServicesManager	scm;
	HRESULT				hr=scm.Open();
	if (hr != S_OK)
		return hr;

	if ((hr=scm.DeleteService(m_szServiceName)) != S_OK)
		return hr;

	return S_OK;
}

/*---------------------------------------------------------------------------*/

// Control request handlers

// static member function (callback) to handle commands from the
// service control manager
void CNTService::Handler (DWORD dwOpcode)
{
	// Get a pointer to the object
	CNTService *pService=m_pThis;
	if (NULL == pService)
		return;
	CNTService&	svc=(*pService);

	switch (dwOpcode)
	{
		case SERVICE_CONTROL_STOP: // 1
			svc.SetStatus(SERVICE_STOP_PENDING);
			svc.OnStop();
			svc.SetRunningState(FALSE);
			break;

		case SERVICE_CONTROL_PAUSE: // 2
			svc.OnPause();
			break;

		case SERVICE_CONTROL_CONTINUE: // 3
			svc.OnContinue();
			break;

		case SERVICE_CONTROL_INTERROGATE: // 4
			svc.OnInterrogate();
			break;

		case SERVICE_CONTROL_SHUTDOWN: // 5
			svc.OnShutdown();
			break;

		default:
			if (dwOpcode >= SERVICE_CONTROL_USER)
			{
				HRESULT	hr=svc.OnUserControl(dwOpcode);
			}
	}

    // Report current status
	SERVICE_STATUS&	st=svc.GetStatus();
	::SetServiceStatus(svc.GetStatusHandle(), &st);
}

/*---------------------------------------------------------------------------*/

// static member function (callback)
void WINAPI CNTService::ServiceMain (const DWORD dwArgc, LPTSTR *lpszArgv)
{
	CNTService *pService=m_pThis;
	if (NULL == pService)
		return;
	CNTService&	svc=(*pService);

	SERVICE_STATUS_HANDLE hStatus=RegisterServiceCtrlHandler(svc.GetName(), CNTService::Handler);
	if (NULL == hStatus)
		return;
	svc.SetStatusHandle(hStatus);

	SERVICE_STATUS& st=svc.GetStatus();
	st.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
	st.dwServiceSpecificExitCode = S_OK;

	// Start the initialization
	HRESULT	hr=svc.Initialize();
	if (S_OK == hr)
	{
		// Do the real work - when the Run function returns, the service has stopped.
		st.dwWin32ExitCode = S_OK;
		st.dwCheckPoint = 0;
		st.dwWaitHint = 0;

		svc.SetStatus(SERVICE_RUNNING);
		svc.SetRunningState(TRUE);
		hr = svc.Run();
	}

	// Tell the service manager we are stopped
	svc.SetStatus(SERVICE_STOPPED);
}

/*---------------------------------------------------------------------------*/
// status functions

HRESULT CNTService::SetStatus (const DWORD	dwState,
										 const DWORD	dwErr,
										 const DWORD	dwHintWait)
{
	if (NULL == m_hServiceStatus)
		return ERROR_INVALID_HANDLE;

	m_Status.dwCurrentState = dwState;
	m_Status.dwWaitHint = dwHintWait;

	::SetServiceStatus(m_hServiceStatus, &m_Status);
	return S_OK;
}

/*---------------------------------------------------------------------------*/

// Service startup and registration
HRESULT CNTService::StartService (void)
{
	SERVICE_TABLE_ENTRY st[]={
		{ m_szServiceName,	CNTService::ServiceMain },
		{ NULL,					NULL							}
	};

	if (_T('\0') == m_szServiceName[0])
		return ERROR_NO_TRACKING_SERVICE;
	if (m_pThis != this)
		return ERROR_NO_MATCH;

	if (!::StartServiceCtrlDispatcher(st))
	{
		HRESULT	hr=GetLastError();
		return hr;
	}

	return S_OK;
}

/*---------------------------------------------------------------------------*/

// Service initialization

HRESULT CNTService::Initialize (void)
{
	SetStatus(SERVICE_START_PENDING);
    
	// Perform the actual initialization
	HRESULT	hr=OnInit();
   
	// Set final state
	m_Status.dwWin32ExitCode = hr;
	m_Status.dwCheckPoint = 0;
	m_Status.dwWaitHint = 0;

	if (hr != S_OK)
	{
		SetStatus(SERVICE_STOPPED);
		return hr;
	}

	return S_OK;
}

//////////////////////////////////////////////////////////////////////////////

/*	Enumerates available services according to supplied options:
 *
 *		eSvcType - type of services to enumerate
 *		eSvcState - state of services to enumerate
 *
 * Note: see "EnumServicesStatus" for available values.
 */
HRESULT EnumSCMServices (SC_HANDLE			hSCM,
								 const DWORD		eSvcType,
								 const DWORD		eSvcState,
								 HSRVCS_ENUM_CFN	lpfnEcfn,
								 LPVOID				pArg)
{
	static const DWORD MIN_SRVC_STBUF_SIZE=(MAX_SERVICE_NAME_LEN+MAX_PATH+2+sizeof(ENUM_SERVICE_STATUS)+sizeof(ENUM_SERVICE_STATUS_PROCESS));

	if ((NULL == hSCM) | (NULL == lpfnEcfn))
		return ERROR_BAD_ARGUMENTS;

	for (DWORD dwSdx=0, dwRH=0; ; dwSdx++)
	{
		BYTE							buf[MIN_SRVC_STBUF_SIZE]={ 0 };
		DWORD							cbNeeded=0, dwSrvcNum=0;
		LPENUM_SERVICE_STATUS	pe=(LPENUM_SERVICE_STATUS) buf;
		BOOL							fSuccess=EnumServicesStatus(hSCM,		// handle to service control manager database
																			 eSvcType,	// type of services to enumerate
																			 eSvcState,	// state of services to enumerate
																			 pe,			// status buffer
																			 (sizeof buf),	// size of available buffer
																			 &cbNeeded,
																			 &dwSrvcNum,	// num of returned status(es)
																			 &dwRH);			// management handle
		HRESULT						hr=GetLastError();

		if ((ERROR_MORE_DATA == hr) || (ERROR_IO_PENDING == hr))
		{
			hr = ERROR_SUCCESS;
		
			if (!fSuccess)
				fSuccess = TRUE;
		}

		if ((!fSuccess) || (hr != ERROR_SUCCESS))
			return hr;

		for (DWORD	i=0; i < dwSrvcNum; i++, pe++)
			if (!(*lpfnEcfn)(hSCM, pe, pArg))
				return ERROR_SUCCESS;

		if (0 == dwRH)
			break;
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

/* Enumerate services on specified host - if NULL or empty then local host */
HRESULT EnumHostServices (LPCTSTR			lpszHostName,
								  const DWORD		eSvcType,
								  const DWORD		eSvcState,
								  HSRVCS_ENUM_CFN	lpfnEcfn,
								  LPVOID				pArg)
{
	HRESULT		hr=ERROR_SUCCESS;
	SC_HANDLE	hSCM=NULL;

	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	hSCM = OpenSCManager(lpszHostName, NULL, SC_MANAGER_ENUMERATE_SERVICE);
	if (NULL == hSCM)
	{
		hr = GetLastError();
		return hr;
	}

	hr = EnumSCMServices(hSCM, eSvcType, eSvcState, lpfnEcfn, pArg);
	CloseServiceHandle(hSCM);
	return hr;
}

/*---------------------------------------------------------------------------*/

static HRESULT ControlSCMServices (SC_HANDLE		hSCM,
											  const DWORD	dwCtrl,
											  LPCTSTR		lpszSrvcs[],		/* may be NULL */
											  LPDWORD		pdwSrvcsNum)		/* out - may be NULL */
{
	DWORD		dwDummy=0;
	DWORD&	dwCount=((NULL == pdwSrvcsNum) ? dwDummy : *pdwSrvcsNum);

	if (NULL == hSCM)
		return ERROR_BAD_ARGUMENTS;
	dwCount = 0;

	if (NULL == lpszSrvcs)
		return ERROR_SUCCESS;

	for (DWORD	idx=0; lpszSrvcs[idx] != NULL; idx++, dwCount++)
	{
		LPCTSTR	lpszSrvcName=lpszSrvcs[idx];
		if (IsEmptyStr(lpszSrvcName))
			break;

		SC_HANDLE				hSrvc=NULL;
		CServiceHandleGuard	svg(hSrvc);
		switch(dwCtrl)
		{
			case SERVICE_CONTROL_START	:
				hSrvc = OpenService(hSCM, lpszSrvcName, SERVICE_START);
				break;

			case SERVICE_CONTROL_STOP	:
				hSrvc = OpenService(hSCM, lpszSrvcName, SERVICE_STOP);
				break;

			default							:
				return ERROR_BAD_ARGUMENTS;
		}

		if (NULL == hSrvc)
			return GetLastError();

		SERVICE_STATUS	st={ 0 };
		BOOL				fSuccess=FALSE;
		switch(dwCtrl)
		{
			case SERVICE_CONTROL_START	:
				fSuccess = StartService(hSrvc, 0, NULL);
				break;

			case SERVICE_CONTROL_STOP	:
				fSuccess = ControlService(hSrvc, SERVICE_CONTROL_STOP, &st);
				break;

			default							:
				return ERROR_BAD_ARGUMENTS;
		}

		if (!fSuccess)
			return GetLastError();

		HRESULT	hr=svg.Release();
		if (hr != ERROR_SUCCESS)
			return hr;
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

/*		Start/Stop the specified service(s) (without any arguments !!!).
 *
 *		hSCM - handle to the services manager
 *
 *		lpszSrvcs - a NULL terminated list of service names (!) to be started
 *
 *		pdwSrvcsNum - if non-NULL contains upon return the number of started
 *			services. Since the routine stops the at the 1st service which fails
 *			to start, this parameter will hold the index of the failed service
 *			in this case.
 */
HRESULT StartSCMServices (SC_HANDLE	hSCM,
								  LPCTSTR	lpszSrvcs[],		/* may be NULL */
								  LPDWORD	pdwSrvcsNum)		/* out - may be NULL */
{
	return ControlSCMServices(hSCM, SERVICE_CONTROL_START, lpszSrvcs, pdwSrvcsNum);
}

HRESULT StopSCMServices (SC_HANDLE	hSCM,
								 LPCTSTR		lpszSrvcs[],		/* may be NULL */
								 LPDWORD		pdwSrvcsNum)		/* out - may be NULL */
{
	return ControlSCMServices(hSCM, SERVICE_CONTROL_STOP, lpszSrvcs, pdwSrvcsNum);
}

/*---------------------------------------------------------------------------*/

static HRESULT ControlHostServices (LPCTSTR		lpszHostName,		/* NULL == local */
												const DWORD	dwCtrl,
												LPCTSTR		lpszSrvcs[],		/* may be NULL */
												LPDWORD		pdwSrvcsNum)		/* out - may be NULL */
{
	HRESULT		hr=ERROR_SUCCESS;
	SC_HANDLE	hSCM=NULL;

	hSCM = OpenSCManager(lpszHostName, NULL, SC_MANAGER_ALL_ACCESS);
	if (NULL == hSCM)
	{
		hr = GetLastError();
		return hr;
	}

	hr = ControlSCMServices(hSCM, dwCtrl, lpszSrvcs, pdwSrvcsNum);
	CloseServiceHandle(hSCM);
	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT StartHostServices (LPCTSTR	lpszHostName,	/* NULL == localhost */
									LPCTSTR	lpszSrvcs[],	/* may be NULL */
									LPDWORD	pdwSrvcsNum)	/* out - may be NULL */
{
	return ControlHostServices(lpszHostName, SERVICE_CONTROL_START, lpszSrvcs, pdwSrvcsNum);
}

HRESULT StopHostServices (LPCTSTR	lpszHostName,	/* NULL == localhost */
								  LPCTSTR	lpszSrvcs[],		/* may be NULL */
								  LPDWORD	pdwSrvcsNum)		/* out - may be NULL */
{
	return ControlHostServices(lpszHostName, SERVICE_CONTROL_STOP, lpszSrvcs, pdwSrvcsNum);
}

/*---------------------------------------------------------------------------*/

HRESULT RestartSCMServices (SC_HANDLE	hSCM,
									 const BOOL	fReverseStart,
									 LPCTSTR		lpszSrvcs[],		/* may be NULL */
									 LPDWORD		pdwSrvcsNum)		/* out - may be NULL */
{
	HRESULT	hr=ERROR_SUCCESS;
	DWORD		dwDummy=0, idx=0, dwNum=0;
	LPDWORD	pdwCount=((NULL == pdwSrvcsNum) ? &dwDummy : pdwSrvcsNum);

	if ((hr=StopSCMServices(hSCM, lpszSrvcs, pdwCount)) != ERROR_SUCCESS)
		return hr;

	if (!fReverseStart)
		return StartSCMServices(hSCM, lpszSrvcs, pdwCount);

	/* at this point, since we were successful, "pdwCount" holds number of services */
	for (idx=(*pdwCount), *pdwCount=0; idx > 0; idx--, (*pdwCount)++)
	{
		LPCTSTR	lpszSrvcName=lpszSrvcs[idx-1], lpszDummy[2]={ lpszSrvcName, NULL };
		DWORD		dwStam=0;

		if ((hr=StartSCMServices(hSCM, lpszDummy, &dwStam)) != ERROR_SUCCESS)
			return hr;
	}

	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

HRESULT RestartHostServices (LPCTSTR		lpszHostName,	/* NULL == local */
									  const BOOL	fReverseStart,
									  LPCTSTR		lpszSrvcs[],	/* may be NULL */
									  LPDWORD		pdwSrvcsNum)	/* out - may be NULL */
{
	HRESULT		hr=ERROR_SUCCESS;
	SC_HANDLE	hSCM=NULL;

	hSCM = OpenSCManager(lpszHostName, NULL, SC_MANAGER_ALL_ACCESS);
	if (NULL == hSCM)
	{
		hr = GetLastError();
		return hr;
	}

	hr = RestartSCMServices(hSCM, fReverseStart, lpszSrvcs, pdwSrvcsNum);
	CloseServiceHandle(hSCM);
	return hr;
}

/*---------------------------------------------------------------------------*/

typedef struct {
	HRESULT				hr;
	HSRVCS_ENUM_CFN	lpfnEcfn;
	LPVOID				pArg;
} XCGSRVCS;

/*		Callback used when enumerating services - if returns FALSE then
 * enumeration is aborted.
 */
static BOOL xcg_srvcs_cfn (SC_HANDLE							hSCM,
									const LPENUM_SERVICE_STATUS	pSvcEnumStatus,
									LPVOID								pArg)
{
	static LPCTSTR lpszMSExchangeSvcPrefix=_T("MSExchange");

	HRESULT				hr=ERROR_SUCCESS;
	XCGSRVCS				*pSrvcs=(XCGSRVCS *) pArg;
	HSRVCS_ENUM_CFN	lpfnEcfn=NULL;
	BOOL					fRetVal=FALSE;

	if ((NULL == hSCM) || (NULL == pSvcEnumStatus) || (NULL == pArg))
	{
		hr = ERROR_BAD_ENVIRONMENT;
		goto Quit;
	}

	if (NULL == (lpfnEcfn=pSrvcs->lpfnEcfn))
	{
		hr = ERROR_INVALID_FUNCTION;
		goto Quit;
	}

	if (_tcsstr(pSvcEnumStatus->lpServiceName, lpszMSExchangeSvcPrefix) != NULL)
		fRetVal = (*lpfnEcfn)(hSCM, pSvcEnumStatus, pSrvcs->pArg);
	else
		fRetVal = TRUE;

Quit:
	if ((hr != ERROR_SUCCESS) && (pSrvcs != NULL))
		pSrvcs->hr = hr;

	return fRetVal;
}

/*---------------------------------------------------------------------------*/

HRESULT EnumXcgSCMServices (SC_HANDLE			hSCM,
									 const DWORD		eSvcType,
									 const DWORD		eSvcState,
									 HSRVCS_ENUM_CFN	lpfnEcfn,
									 LPVOID				pArg)
{
	HRESULT	hr=ERROR_SUCCESS;
	XCGSRVCS	xcgSrvcs={ ERROR_SUCCESS, lpfnEcfn, pArg };

	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	hr = EnumSCMServices(hSCM, eSvcType, eSvcState, xcg_srvcs_cfn, (LPVOID) &xcgSrvcs);
	if (ERROR_SUCCESS == hr)
		hr = xcgSrvcs.hr;

	return hr;
}

/*---------------------------------------------------------------------------*/

HRESULT EnumXcgHostServices (LPCTSTR			lpszHostName,
									  const DWORD		eSvcType,
									  const DWORD		eSvcState,
									  HSRVCS_ENUM_CFN	lpfnEcfn,
									  LPVOID				pArg)
{
	HRESULT		hr=ERROR_SUCCESS;
	SC_HANDLE	hSCM=NULL;

	if (NULL == lpfnEcfn)
		return ERROR_BAD_ARGUMENTS;

	hSCM = OpenSCManager(lpszHostName, NULL, SC_MANAGER_ALL_ACCESS);
	if (NULL == hSCM)
	{
		hr = GetLastError();
		return hr;
	}

	hr = EnumXcgSCMServices(hSCM, eSvcType, eSvcState, lpfnEcfn, pArg);
	CloseServiceHandle(hSCM);
	return hr;
}

/*---------------------------------------------------------------------------*/
