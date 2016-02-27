#ifndef _PERF_DAT_H_
#define _PERF_DAT_H_

/*
 * ----------------------------------
 * Interface for NT Performance data
 * ----------------------------------
 */

#include <wtypes.h>
#include <pdh.h>

/*--------------------------------------------------------------------------*/

/*
 * shortcut typedefs
 */
typedef HQUERY   HPERF_DAT_QUERY;
typedef HCOUNTER HPERF_DAT_COUNTER;

/*--------------------------------------------------------------------------*/

/*
 * Performance data paths:
 */

/*
 * System total processor time 
 */
#define PSZ_SYSTEM_COUNTERS_GROUP	TEXT("System")
#	define PSZ_COUNTER_SYSTEMPROCESSESNUM	TEXT("Processes")
#	define PSZ_COUNTER_SYSTEMUPTIME			TEXT("System Up Time")
#	define PSZ_COUNTER_SYSTEMTHREADSNUM		TEXT("Threads")

#define PSZ_PROCESSOR_COUNTERS_GROUP	TEXT("Processor")
#	define PSZ_PROCESSOR_TIME_COUNTER		TEXT("% Processor Time")

#define PSZ_TCP_COUNTERS_GROUP	TEXT("TCP")
#	define PSZ_TCP_CONNS_ACTIVE_COUNTER			TEXT("Connections Active")
#	define PSZ_TCP_CONNS_ESTABLISHED_COUNTER	TEXT("Connections Established")
#	define PSZ_TCP_SEGSPERSECRCV_COUNTER		TEXT("Segments Received/sec")
#	define PSZ_TCP_SEGSPERSECSND_COUNTER		TEXT("Segments Sent/sec")

/*--------------------------------------------------------------------------*/

/*
 * Creates and initalizes a performance data query.
 */
#ifdef __cplusplus
inline HRESULT PerfDatOpenQuery (HQUERY *phQuery)
{
	return PdhOpenQuery(0,0,phQuery);
}
#else
#	define PerfDatOpenQuery(phQuery)	PdhOpenQuery(0,0,phQuery)
#endif

/*
 * Closes a performance data query handle.
 */
#ifdef __cplusplus
inline HRESULT PerfDatCloseQuery (HQUERY hQuery)
{
	return PdhCloseQuery(hQuery);
}
#else
#	define PerfDatCloseQuery(hQuery)	PdhCloseQuery(hQuery)
#endif

/*
 * Adds a performance data counter to query
 */
#ifdef __cplusplus
inline HRESULT PerfDatAddCounter (HQUERY hQuery, LPCTSTR pszPerfDatPath, HCOUNTER *phCounter)
{
	return PdhAddCounter(hQuery, pszPerfDatPath, 0, phCounter);
}
#else
#	define PerfDatAddCounter(hQuery,pszPerfDatPath,phCounter) PdhAddCounter(hQuery,pszPerfDatPath,0,phCounter)
#endif

/*
 * Removes a performance data counter
 */
#ifdef __cplusplus
inline HRESULT PerfDatRemoveCounter (HCOUNTER hCounter)
{
	return PdhRemoveCounter(hCounter);
}
#else
#	define PerfDatRemoveCounter(hCounter)	PdhRemoveCounter(hCounter)
#endif

/*
 * Collects data for the specified query
 */
#ifdef __cplusplus
inline HRESULT PerfDatCollect (HQUERY hQuery)
{
	return PdhCollectQueryData(hQuery);
}
#else
#	define PerfDatCollect(hQuery)	PdhCollectQueryData(hQuery)
#endif

/*--------------------------------------------------------------------------*/

/*
 * Formats a counter as long integer
 */
extern HRESULT PerfDatFormatCounterLong (HCOUNTER hCounter, long *lpCounter);

/*--------------------------------------------------------------------------*/

/* updates the value of the specified counter */
extern HRESULT PerfDatUpdateLong (HQUERY hQuery, HCOUNTER hCounter, long *lpCounter);

/*--------------------------------------------------------------------------*/

/* retrieves value of specified counter (NOTE: slow operation !!!) */
extern HRESULT PerfDatGetLong (LPCTSTR pszPerfDatPath, long *lpCounter);

/*--------------------------------------------------------------------------*/

#define PSZ_PROCESS_COUNTERS_GROUP	TEXT("Process")
#	define PSZ_PROCESS_CPUTIME				TEXT("% Processor Time")
#	define PSZ_PROCESS_THREADCOUNT		TEXT("Thread Count")
#	define PSZ_PROCESS_WORKINGSET			TEXT("Working Set")
#	define PSZ_PROCESS_HANDLECOUNT		TEXT("Handle Count")
#	define PSZ_PROCESS_ID					TEXT("ID Process")
#	define PSZ_PROCESS_WORKINGSET_PEAK	TEXT("Working Set Peak")
#	define PSZ_PROCESS_VIRTBYTES			TEXT("Virtual Bytes")
#	define PSZ_PROCESS_VIRTBYTES_PEAK	TEXT("Virtual Bytes Peak")
#	define PSZ_PROCESS_PRIVBYTES			TEXT("Private Bytes")

/* beyond this length, all sort of formatting takes place */
#define MAX_PDH_PROCCNTR_NAME_LENGTH	15

extern HRESULT PerfDatGetProcessCounterPath (LPCTSTR		lpszMachineName,	/* NULL == localhost */
															LPCTSTR		lpszProcessName,	/* NULL == calling process */
															LPCTSTR		lpszCounterName,
															LPTSTR		lpszCounterPath,
															const DWORD	dwMaxLen);

extern HRESULT PerfDatAddProcessCounter (HQUERY		hQuery,
													  LPCTSTR	lpszMachineName,	/* NULL == localhost */
													  LPCTSTR	lpszProcessName,	/* NULL == this */
													  LPCTSTR	lpszCounterName,
													  HCOUNTER	*phCounter);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPerfDatQueryGuard {
	private:
		HPERF_DAT_QUERY&	m_hQry;

	public:
		CPerfDatQueryGuard (HPERF_DAT_QUERY& hQry)
			: m_hQry(hQry)
		{
		}

		void Release ()
		{
			if (m_hQry != NULL)
			{
				::PerfDatCloseQuery(m_hQry);
				m_hQry = NULL;
			}
		}

		~CPerfDatQueryGuard ()
		{
			Release();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPerfDatCounterGuard {
	private:
		HPERF_DAT_COUNTER&	m_hCntr;

	public:
		CPerfDatCounterGuard (HPERF_DAT_COUNTER& hCntr)
			: m_hCntr(hCntr)
		{
		}

		void Release ()
		{
			if (m_hCntr != NULL)
			{
				::PerfDatRemoveCounter(m_hCntr);
				m_hCntr = NULL;
			}
		}

		~CPerfDatCounterGuard ()
		{
			Release();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	long	lProcessID;
	long	lCPUTime;
	long	lThreadCount;
	long	lWorkSet;
	long	lWorkSetPeak;
	long	lHandleCount;
	long	lVirtBytes;
	long	lVirtBytesPeak;
	long	lPrivBytes;
} PROCESS_PERFDAT_INFO;

#define MAX_PROCESS_PERFDAT_CNTRS_NUM	(sizeof(PROCESS_PERFDAT_INFO)/sizeof(long))

#ifdef __cplusplus
class CProcessPerfDatInfoQuery {
	private:
		HPERF_DAT_QUERY		m_hQuery;
		PROCESS_PERFDAT_INFO	m_datInfo;

		typedef struct {
			LPCTSTR	lpszCntrName;
			HCOUNTER	hCntr;
			long		*lpValue;
		} PROCDATINFODESC;

		PROCDATINFODESC	m_infoDesc[MAX_PROCESS_PERFDAT_CNTRS_NUM+2];

		// disable copy constructor and assignment operator
		CProcessPerfDatInfoQuery (const CProcessPerfDatInfoQuery& );
		CProcessPerfDatInfoQuery& operator= (const CProcessPerfDatInfoQuery& );

		HRESULT AddCounters (LPCTSTR lpszMachineName, LPCTSTR lpszProcName);

	public:
		CProcessPerfDatInfoQuery ();

		// may not be re-initialized unless "Close" called first
		HRESULT Init (LPCTSTR lpszMachineName, // NULL/empty == localhost
						  LPCTSTR lpszProcName);	// NULL/empty == calling process

		HRESULT QueryInfo (PROCESS_PERFDAT_INFO& datInfo);

		// may be called more than once
		HRESULT Close ();

		~CProcessPerfDatInfoQuery ()
		{
			HRESULT	hr=Close();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CPerfDatCountersGroup {
	private:
		LPTSTR			m_lpszMachineName;	// may be NULL
		LPTSTR			m_lpszGroupName;
		HQUERY			m_hQuery;
		CStr2PtrMapper	m_cntrsMap;	// key=name, value=hCntr

	public:
		CPerfDatCountersGroup ();

		// may not be re-initialized unless "Close" called first
		HRESULT Init (LPCTSTR lpszMachineName, LPCTSTR lpszGroupName, const ULONG ulMaxCounters);

		// see "PdhMakeCounterPath" for specifications
		HRESULT AddCounter (LPCTSTR		lpszCntrName,
								  LPCTSTR		lpszParentInstance=NULL,
								  LPCTSTR		lpszInstanceName=NULL,
								  const DWORD	dwInstance=(DWORD) (-1));

		// if (-1) specified as number of counters, then last entry must be empty/NULL
		HRESULT AddCounters (LPCTSTR lpszCntrs[], const ULONG ulCNum=(ULONG) (-1));

		HRESULT UpdateData ();

		// Note: "UpdateData" must be called in order for a value (of the same counter) to be updated
		HRESULT GetCounterValue (LPCTSTR lpszCntrName, long& lValue) const;

		// may be called several times even if already closed
		HRESULT Close ();

		const LPCTSTR GetMachineName () const
		{
			return m_lpszMachineName;
		}

		const LPCTSTR GetGroupName () const
		{
			return m_lpszGroupName;
		}

		const BOOL IsCounterInGroup (LPCTSTR lpszCounterName) const
		{
			LPVOID	pVal=NULL;
			return (S_OK == m_cntrsMap.FindKey(lpszCounterName, pVal));
		}

		~CPerfDatCountersGroup ()
		{
			Close();
		}

		friend class CPerfDatCountersGroupEnum;
};

/*--------------------------------------------------------------------------*/

class CPerfDatCountersGroupEnum {
	private:
		const CPerfDatCountersGroup&	m_dtc;
		CStr2PtrMapEnum					m_cme;

	public:
		CPerfDatCountersGroupEnum (const CPerfDatCountersGroup& dtc)
			: m_dtc(dtc), m_cme(dtc.m_cntrsMap)
		{
		}

		/*
		 *		These methods can be used to enumerate values
		 *
		 * Note: "UpdateData" must be called before starting the enumeration
		 *			if you want to have the values up-to-date	
		 */
		HRESULT GetFirstCounterValue (LPCTSTR& lpszCntrName, long& lValue);
		HRESULT GetNextCounterValue (LPCTSTR& lpszCntrName, long& lValue);

		~CPerfDatCountersGroupEnum ()
		{
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#endif /* PERF_DAT_H */