#ifndef _NTSERVICE_H_
#define _NTSERVICE_H_

/*---------------------------------------------------------------------------*/
/*		Implements interface for creating & handling NT services.
 */
/*---------------------------------------------------------------------------*/

#ifndef WIN32
#error "This can be compiler only for WIN32"
#endif

#include <wtypes.h>
#include <tchar.h>
#include <winerror.h>
#include <winsvc.h>

#ifndef SERVICE_CONTROL_START
#define SERVICE_CONTROL_START	((DWORD) (-2))
#endif

#ifndef SERVICE_CONTROL_RESTART
#define SERVICE_CONTROL_RESTART	((DWORD) (-3))
#endif

/*---------------------------------------------------------------------------*/

/*		Callback used when enumerating services - if returns FALSE then
 * enumeration is aborted.
 */
typedef BOOL (*HSRVCS_ENUM_CFN)(SC_HANDLE							hSCM,
										  const LPENUM_SERVICE_STATUS	pSvcEnumStatus,
										  LPVOID								pArg);

/*	Enumerates available services according to supplied options:
 *
 *		eSvcType - type of services to enumerate
 *		eSvcState - state of services to enumerate
 *
 * Note: see "EnumServicesStatus" for available values.
 */
extern HRESULT EnumSCMServices (SC_HANDLE			hSCM,
										  const DWORD		eSvcType,
										  const DWORD		eSvcState,
										  HSRVCS_ENUM_CFN	lpfnEcfn,
										  LPVOID				pArg);

/* Enumerate services on specified host - if NULL or empty then local host */
extern HRESULT EnumHostServices (LPCTSTR				lpszHostName,
											const DWORD			eSvcType,
											const DWORD			eSvcState,
											HSRVCS_ENUM_CFN	lpfnEcfn,
											LPVOID				pArg);

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
extern HRESULT StartSCMServices (SC_HANDLE	hSCM,
											LPCTSTR		lpszSrvcs[],	/* may be NULL */
											LPDWORD		pdwSrvcsNum);	/* out - may be NULL */

extern HRESULT StopSCMServices (SC_HANDLE	hSCM,
										  LPCTSTR	lpszSrvcs[],	/* may be NULL */
										  LPDWORD	pdwSrvcsNum);	/* out - may be NULL */

/*---------------------------------------------------------------------------*/

extern HRESULT StartHostServices (LPCTSTR	lpszHostName,	/* NULL == localhost */
											 LPCTSTR	lpszSrvcs[],	/* may be NULL */
											 LPDWORD	pdwSrvcsNum);	/* out - may be NULL */

extern HRESULT StopHostServices (LPCTSTR	lpszHostName,	/* NULL == localhost */
										   LPCTSTR	lpszSrvcs[],		/* may be NULL */
										   LPDWORD	pdwSrvcsNum);		/* out - may be NULL */

/*---------------------------------------------------------------------------*/

extern HRESULT RestartSCMServices (SC_HANDLE		hSCM,
											  const BOOL	fReverseStart,
											  LPCTSTR		lpszSrvcs[],		/* may be NULL */
											  LPDWORD		pdwSrvcsNum);		/* out - may be NULL */

extern HRESULT RestartHostServices (LPCTSTR		lpszHostName,	/* NULL == local */
												const BOOL	fReverseStart,
												LPCTSTR		lpszSrvcs[],	/* may be NULL */
												LPDWORD		pdwSrvcsNum);	/* out - may be NULL */

/*---------------------------------------------------------------------------*/

/* Enumerates the Exchange services (if any) */
extern HRESULT EnumXcgSCMServices (SC_HANDLE			hSCM,
											  const DWORD		eSvcType,
											  const DWORD		eSvcState,
											  HSRVCS_ENUM_CFN	lpfnEcfn,
											  LPVOID				pArg);

extern HRESULT EnumXcgHostServices (LPCTSTR				lpszHostName,
											   const DWORD			eSvcType,
												const DWORD			eSvcState,
												HSRVCS_ENUM_CFN	lpfnEcfn,
												LPVOID				pArg);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CServicesManager {
	private:
		// disable copy constructor and assignment operator
		CServicesManager (const CServicesManager& );
		CServicesManager& operator= (const CServicesManager& );

	protected:
		SC_HANDLE	m_hSCM;

	public:
		CServicesManager ()
			: m_hSCM(NULL)
		{
		}

		// returns error if already have a valid handle
		virtual HRESULT Attach (SC_HANDLE hScm);

		virtual HRESULT Open (LPCTSTR			lpMachineName=NULL,   // pointer to machine name string
									 LPCTSTR			lpDatabaseName=NULL,  // pointer to database name string
									 const DWORD	dwDesiredAccess=SC_MANAGER_ALL_ACCESS);	// type of access

		virtual HRESULT OpenService (LPCTSTR		lpServiceName, // pointer to name of service to start
											  const DWORD	dwDesiredAccess,  // type of access to service
											  SC_HANDLE&	hSvc) const;

		virtual HRESULT CreateService (LPCTSTR			lpServiceName,			// name of service to start
												 LPCTSTR			lpDisplayName,			// display name
												 const DWORD	dwDesiredAccess,		// type of access to service
												 const DWORD	dwServiceType,			// type of service
												 const DWORD	dwStartType,			// when to start service
												 const DWORD	dwErrorControl,		// severity of service failure
												 LPCTSTR			lpBinaryPathName,		// name of binary file
												 LPCTSTR			lpLoadOrderGroup,		// name of load ordering group
												 LPDWORD			lpdwTagId,				// receives tag identifier
												 LPCTSTR			lpDependencies,		// array of dependency names
												 LPCTSTR			lpServiceStartName,	// service account name 
												 LPCTSTR			lpPassword,				// account password
												 SC_HANDLE&		hSVC) const;

		virtual HRESULT CreateSimpleService (LPCTSTR		lpszSrvcName,
														 LPCTSTR		lpszSrvcDispName,
														 LPCTSTR		lpszSrvcPath,
														 const BOOL	fAutoStart,
														 SC_HANDLE&	hSvc) const
		{
			return CreateService(lpszSrvcName,
										lpszSrvcDispName,
										SERVICE_ALL_ACCESS,
										SERVICE_WIN32_OWN_PROCESS | SERVICE_INTERACTIVE_PROCESS,
										((fAutoStart) ? SERVICE_AUTO_START : SERVICE_DEMAND_START),
										SERVICE_ERROR_NORMAL,
										lpszSrvcPath,
										NULL,
										NULL,
										_T(""),
										NULL,
										NULL,
										hSvc);
		}

		/*	Enumerates available services according to supplied options:
		 *
		 *		eSvcType - type of services to enumerate
	    *		eSvcState - state of services to enumerate
		 *
		 * Note: see "EnumServicesStatus" for available values.
		 */
		virtual HRESULT EnumServices (const DWORD			eSvcType,
												const DWORD			eSvcState,
												HSRVCS_ENUM_CFN	lpfnEcfn,
												LPVOID				pArg) const
		{
			return ::EnumSCMServices(m_hSCM, eSvcType, eSvcState, lpfnEcfn, pArg);
		}

		virtual HRESULT HandleServices (LPCTSTR lpszSrvcs[] /* may be NULL */, const BOOL fStart, DWORD& dwSrvcsNum)
		{
			return (fStart ? ::StartSCMServices(m_hSCM, lpszSrvcs, &dwSrvcsNum) : ::StopSCMServices(m_hSCM, lpszSrvcs, &dwSrvcsNum));
		}

		virtual HRESULT HandleService (LPCTSTR lpszSrvcName, const BOOL fStart);

		/*		Start/Stop the specified service(s) (without any arguments !!!).
		 *
		 *
		 *		lpszSrvcs - a NULL terminated list of service names (!) to be started
		 *
		 *		dwSrvcsNum - contains upon return the number of started
		 *			services. Since the routine stops the at the 1st service which fails
		 *			to start, this parameter will hold the index of the failed service
		 *			in this case.
		 */
		virtual HRESULT StartServices (LPCTSTR lpszSrvcs[] /* may be NULL */, DWORD& dwSrvcsNum)
		{
			return HandleServices(lpszSrvcs, TRUE, dwSrvcsNum);
		}

		virtual HRESULT StartService (LPCTSTR lpszSrvcName)
		{
			return HandleService(lpszSrvcName, TRUE);
		}

		virtual HRESULT StopServices (LPCTSTR	lpszSrvcs[] /* may be NULL */, DWORD& dwSrvcsNum)
		{
			return HandleServices(lpszSrvcs, FALSE, dwSrvcsNum);
		}

		virtual HRESULT StopService (LPCTSTR lpszSrvcName)
		{
			return HandleService(lpszSrvcName, FALSE);
		}

		virtual HRESULT DeleteService (LPCTSTR lpszSrvcName) const;

		virtual HRESULT GetServiceStatus (LPCTSTR lpszSvcName, SERVICE_STATUS& svcStatus) const;

		// NOTE: Make sure handle is eventually closed, otherwise leak occurs
		virtual HRESULT Detach ()
		{
			m_hSCM = NULL;
			return S_OK;
		}

		virtual HRESULT Close ();

		virtual SC_HANDLE GetManagerHandle () const
		{ 
			return m_hSCM;
		}

		virtual ~CServicesManager ()
		{ 
			Close();
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CServiceHandleGuard {
	protected:
		SC_HANDLE&	m_hSvc;

	public:
		CServiceHandleGuard (SC_HANDLE& hSvc)
			: m_hSvc(hSvc)
		{
		}

		HRESULT Release ()
		{
			if (m_hSvc != NULL)
			{
				HRESULT	hr=S_OK;
				if (!::CloseServiceHandle(m_hSvc))
					hr = ::GetLastError();
				m_hSvc = NULL;

				return hr;
			}

			return S_OK;
		}

		~CServiceHandleGuard ()
		{
			Release();
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/*
 * MessageId: EVMSG_INSTALLED
 *
 * MessageText:
 *
 *  The %1 service was installed.
 */
#define EVMSG_INSTALLED                  0x00000064L

/*
 * MessageId: EVMSG_REMOVED
 *
 * MessageText:
 *
 *  The %1 service was removed.
 */

#define EVMSG_REMOVED                    0x00000065L

/*
 * MessageId: EVMSG_NOTREMOVED
 *
 * MessageText:
 *
 *  The %1 service could not be removed.
 */
#define EVMSG_NOTREMOVED                 0x00000066L

/*
 * MessageId: EVMSG_CTRLHANDLERNOTINSTALLED
 *
 * MessageText:
 *
 *  The control handler could not be installed.
 */
#define EVMSG_CTRLHANDLERNOTINSTALLED    0x00000067L

/*
 * MessageId: EVMSG_FAILEDINIT
 *
 * MessageText:
 *
 *  The initialization process failed.
 */
#define EVMSG_FAILEDINIT                 0x00000068L

/*
 * MessageId: EVMSG_STARTED
 *
 * MessageText:
 *
 *  The service was started.
 */
#define EVMSG_STARTED                    0x00000069L

/*
 * MessageId: EVMSG_BADREQUEST
 *
 * MessageText:
 *
 *  The service received an unsupported request.
 */
#define EVMSG_BADREQUEST                 0x0000006AL

/*
 * MessageId: EVMSG_DEBUG
 *
 * MessageText:
 *
 *  Debug: %1
 */
#define EVMSG_DEBUG                      0x0000006BL

/*
 * MessageId: EVMSG_STOPPED
 *
 * MessageText:
 *
 *  The service was stopped.
 */
#define EVMSG_STOPPED                    0x0000006CL

/*
 * Definitions for CNTService
 */

#define SERVICE_CONTROL_USER 128

/*---------------------------------------------------------------------------*/

#define MAX_SERVICE_NAME_LEN			256
#define MAX_SERVICE_DISPLY_NAME_LEN	MAX_SERVICE_NAME_LEN

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// Calls to encompass the NT services functionality
class CNTService {
	private:
		// disable copy constructor and assignment operator
		CNTService (const CNTService& );
		CNTService& operator= (const CNTService& );

	protected:
		// data members
		TCHAR							m_szServiceName[MAX_SERVICE_NAME_LEN+2];
		TCHAR							m_szServiceDisplayName[MAX_SERVICE_DISPLY_NAME_LEN+2];
		ULONG							m_ulMajorVersion;
		ULONG							m_ulMinorVersion;
		SERVICE_STATUS_HANDLE	m_hServiceStatus;
		SERVICE_STATUS				m_Status;
		BOOL							m_bIsRunning;
		
		// static members
		static CNTService *m_pThis;
		static void WINAPI ServiceMain (DWORD dwArgc, LPTSTR *lpszArgv);
		static void WINAPI Handler (DWORD dwOpcode);

	public:
		// also default constructor
		CNTService (const ULONG ulMajorVersion=0, const ULONG ulMinorVersion=0);

		// if display name is NULL/empty then using same as service name
		virtual HRESULT Init (LPCTSTR lpszSrvcName, LPCTSTR lpszSrvcDispName=NULL);

		virtual ~CNTService();

		virtual BOOL IsInstalled (void) const;
		virtual HRESULT Install (const BOOL fAutoStart);
		virtual HRESULT Uninstall (void);

		virtual SERVICE_STATUS& GetStatus (void)
		{
			return m_Status;
		}

		virtual SERVICE_STATUS_HANDLE GetStatusHandle (void) const
		{
			return m_hServiceStatus;
		}

		virtual void SetStatusHandle (const SERVICE_STATUS_HANDLE hStatus)
		{
			m_hServiceStatus = hStatus;
		}

		virtual void SetRunningState (const BOOL fIsRunning)
		{
			m_bIsRunning = fIsRunning;
		}

		virtual LPCTSTR GetName (void) const
		{
			return m_szServiceName;
		}

		virtual LPCTSTR GetDisplayName (void) const
		{
			return m_szServiceDisplayName;
		}

		virtual HRESULT StartService (void);
		virtual HRESULT SetStatus (const DWORD dwState,
											const DWORD dwErr=S_OK,
											const DWORD	dwHintWait=5000);

		virtual HRESULT Initialize (void);

		// This function performs the main work of the service. 
		// When this function returns the service has stopped.
		virtual HRESULT Run (void) = 0;

		// Called when the service is first initialized
		virtual HRESULT OnInit (void)
		{
			return S_OK;
		}

		/* Called when the service control manager wants to stop the service
		 *
		 * Note: 
		 *	If a Stop procedure is going to take longer than 3 seconds to execute,
		 * it should spawn a thread to execute the stop code, and return.
		 * Otherwise, the ServiceControlManager (SCM) will believe that the service
		 * has stopped responding.
		 */
		virtual HRESULT OnStop (void)
		{
			return S_OK;
		}

		// called when the service is interrogated
		virtual HRESULT OnInterrogate (void)
		{
			return S_OK;
		}

		// called when the service is paused
		virtual HRESULT OnPause (void)
		{
			return S_OK;
		}

		// called when the service is continued
		virtual HRESULT OnContinue (void)
		{
			return S_OK;
		}

		// called when the service is shut down
		virtual HRESULT OnShutdown (void)
		{
			return S_OK;
		}

		// called when the service gets a user control message
		virtual HRESULT OnUserControl (const DWORD dwOpcode)
		{
			return S_OK;
		}
};	// end of CNTService class definition
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _NTSERVICE_H_ */
