#ifndef _WIN32_GENERAL_H_
#define _WIN32_GENERAL_H_

/*---------------------------------------------------------------------------*/
/* general utilities (for lack of better holder file)
 *
 * Other Win32 built-in of interest:
 *
 *		GetVersionEx - find out the platform (95/NT), version and build number
 */

#include <_types.h>
#include <winerror.h>
#include <winbase.h>
#include <unknwn.h>
#include <stdio.h>

#include <util/string.h>
#include <util/time.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
inline BOOL IsBadHandle (const HANDLE h)
{
	return ((NULL == h) || (INVALID_HANDLE_VALUE == h));
}

inline HRESULT ResolveWIN32LastError (const HRESULT rhr)
{
	return ((S_OK == rhr) ? ERROR_INVALID_HANDLE : rhr);
}

class CHandleGuard {
	private:
		HANDLE&	m_h;

		// disable copy constructor and assignment operator
		CHandleGuard (const CHandleGuard& );
		CHandleGuard& operator= (const CHandleGuard& );
	public:
		CHandleGuard (HANDLE& h) : m_h(h) { }

		void Close ()
		{
			if (!IsBadHandle(m_h))
			{
				CloseHandle(m_h);
				m_h = NULL;
			}
		}

		virtual ~CHandleGuard () { Close(); }
};
#else
#	define IsBadHandle(h)	((NULL == (h)) || (INVALID_HANDLE_VALUE == (h)))
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CDLLHandleGuard {
	private:
		HINSTANCE&	m_hDLL;

	public:
		CDLLHandleGuard (HINSTANCE& hDLL)
			: m_hDLL(hDLL)
		{
		}

		virtual ~CDLLHandleGuard ()
		{
			if (m_hDLL != NULL)
			{
				::FreeLibrary((HMODULE) m_hDLL);
				m_hDLL = NULL;
			}
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* MakePtr is a macro that allows you to easily add to values (including
 * pointers) together without dealing with "C"-s pointer arithmetic. The first
 * parameter is used to typecast the result to the appropriate pointer type.
 */

#define MakePtr(cast, ptr, addValue) (cast)((char *)(ptr) + (DWORD)(addValue))

/*---------------------------------------------------------------------------*/

/* sets console echo according to ON/OFF flag - return TRUE if successful
 * (useful for input of passwords...)
 */
extern BOOL set_console_echo (BOOL fOnOff);
extern BOOL get_console_echo (BOOL *pfOnOff);

/*---------------------------------------------------------------------------*/

/* returns NULL if unsuccessful */
extern HWND GetConsoleWindowHandle (void);

/* changes the console window to minimzied/maximized */
extern HRESULT SetConsoleDisplayMode (const BOOL fMaximized);
extern HRESULT GetConsoleDisplayMode (BOOL *pfMaximized);

/*---------------------------------------------------------------------------*/

/*		To be used for getting a handle for the current thread if needs to be
 * accessed from another thread.
 *
 *	Note: GetCurrentThread() returns a PSEUDO-handle which cannot be used
 *			by another thread (e.g. to suspend/terminate)
 */
#ifdef __cplusplus
extern HRESULT GetSafeCurrentThreadHandle (HANDLE& hSafe);
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Callback used to enumerate currently running processes. If return code
 * is other than ERROR_SUCCESS then enumeration is aborted and return code is
 * PROPAGATED (!!!).
 */
typedef HRESULT (*PROC_E_CFN)(const char	pszProcName[],
										const DWORD	dwProcId,
										LPVOID		pArg);

extern HRESULT EnumRunningProcesses (const char	*pszRegServer,
												 PROC_E_CFN	lpfnEcfn,
												 LPVOID		pArg);

/* determines if the ".exe" module is currently running (if ERROR_SUCCESS) */
extern HRESULT IsRunningProcess (const char pszModulePath[], BOOL *pfRunning);

/*---------------------------------------------------------------------------*/

/* returns 0 if unsuccessful */
extern time_t SystemTimeToTime (const SYSTEMTIME *pst);

extern HRESULT TmStructToSystemTime (const struct tm *ptm, SYSTEMTIME *pst);
extern HRESULT SystemTimeToTmStruct (const SYSTEMTIME *pst,  struct tm *ptm);

#ifdef __cplusplus
extern bool IsDSTActive (const DWORD dwRes, const TIME_ZONE_INFORMATION& tzi);

extern bool IsDSTActive ();

/* Compares only the DATE part - returns
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
extern int CompareSystemTimeDates (const SYSTEMTIME& v1, const SYSTEMTIME& v2);
/* Compares only the TIME part - returns
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
extern int CompareSystemTimeTimes (const SYSTEMTIME& v1, const SYSTEMTIME& v2);
/* Returns:
 *		>0 - if v1 < v2
 *		=0 - if v1 == v2
 *		<0 - if v1 > v2
 */
extern int CompareSystemTimesValues (const SYSTEMTIME& v1, const SYSTEMTIME& v2);

extern HRESULT TmValToSystemTime (const time_t tmVal, SYSTEMTIME& syst);

// useful class encapsulation for SYSTEMTIME
class CSystemTime : public SYSTEMTIME {
	public:
		virtual void Reset ()
		{
			wYear = 0;
			wMonth = 0;
			wDay = 0;
			wDayOfWeek = 0;
			wHour = 0;
			wMinute = 0;
			wSecond = 0;
			wMilliseconds = 0;
		}

		/* Returns:
		 *		>0 - if THIS < v2
		 *		=0 - if THIS == v2
		 *		<0 - if THIS > v2
		 */
		virtual int CompareTo (const SYSTEMTIME& v2)
		{
			return ::CompareSystemTimesValues(*this, v2);
		}

		// using default copy constructor and assignment operator
		CSystemTime ()
		{
			Reset();
		}

		virtual HRESULT SetTimeValue (const time_t tmVal)
		{
			return ::TmValToSystemTime(tmVal, *this);
		}

		CSystemTime (const time_t tmVal)
		{
			SetTimeValue(tmVal);
		}

		virtual const time_t GetTimeValue () const
		{
			return ::SystemTimeToTime(this);
		}

		virtual HRESULT SetTmStructValue (const struct tm& tmv)
		{
			return ::TmStructToSystemTime(&tmv, this);
		}

		CSystemTime (const struct tm& tmv)
		{
			SetTmStructValue(tmv);
		}

		virtual HRESULT GetTmStructValue (struct tm& tmv) const
		{
			return ::SystemTimeToTmStruct(this, &tmv);
		}

		virtual HRESULT SetTime (const SYSTEMTIME& sysTime);
};

class CSystemTimeUpdater : public CSystemTime {
	private:
		time_t	m_tmVal;	// the matching time_t value

	public:
		virtual HRESULT SetTimeValue (const time_t tmVal);

		CSystemTimeUpdater ()
			: CSystemTime()
		{
			SetTimeValue(::time(NULL));
		}

		virtual const time_t GetTimeValue () const
		{
			return m_tmVal;
		}

		virtual HRESULT SetTime (const SYSTEMTIME& sysTime);

		// assignment operator
		virtual CSystemTimeUpdater& operator= (const SYSTEMTIME& sysTime)
		{
			SetTime(sysTime);
			return *this;
		}

		virtual HRESULT AddSeconds (const LONG lSecs);

		// copy constructor
		CSystemTimeUpdater (const SYSTEMTIME& sysTime)
			: CSystemTime()
		{
			SetTime(sysTime);
		}

		virtual HRESULT SetTmStructValue (const struct tm& tmv);

		CSystemTimeUpdater (const struct tm& tmv)
			: CSystemTime()
		{
			SetTmStructValue(tmv);
		}

		virtual ~CSystemTimeUpdater ()
		{
		}
};
#endif

extern time_t FileTimeToTime (const FILETIME *pft, const BOOL fIsLocal);
extern HRESULT TimeToFileTime (const time_t tVal, FILETIME *ft);

/* offset in sec. from GMT of local system - GMT+0200 => -7200, GMT-0200 => +7200 */
extern int GetLocalTimezoneOffset ();

extern HRESULT GetAdjustedLocalSysTime (const int newTmZone, SYSTEMTIME *ost);

extern HRESULT RecalculateSystemTimeTimezone (const SYSTEMTIME	*pst,
														    const int			orgTmZone,
															 const int			newTmZone,
															 SYSTEMTIME			*ost);

extern  HRESULT AdjustSystemTimeTimezone (const SYSTEMTIME	*pst,
														const int			tmZone,
														SYSTEMTIME			*ost);

/*---------------------------------------------------------------------------*/

extern HRESULT UnicodeToAnsi (LPCWSTR pszW, LPSTR pszA, const ULONG ulASize);
extern HRESULT AnsiToUnicode (LPCSTR pszA, LPWSTR pszW, const ULONG ulWLen);

/*---------------------------------------------------------------------------*/

/* returns type of drive (e.g. CD-ROM, network, etc.) */
extern const char *XlateDriveType (const UINT uDrvType);

/*---------------------------------------------------------------------------*/

/*		Callback used to enumerate available drives on local system. If return
 * value is other than TRUE then enumeration is aborted.
 */
typedef BOOL (*SYSDRV_E_CFN)(const char	pszDrvName[],
									  const UINT	uDrvType,	/* see GetDriveType() */
									  const char	pszVolumeName[],
									  const DWORD	dwVolumeNumber,
									  const DWORD	dwFSystemFlags,
									  const char	pszFSystemName[],
									  LPVOID			pArg);

/*		Enumerates current drives. Note: removable drives (e.g. floppy, CD) may
 * be enumerated without their volume information if they are not present.
 */
extern HRESULT EnumSystemDrives (SYSDRV_E_CFN	lpfnEcfn, LPVOID	pArg);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// base class for implementing the IUnknown-like interfaces
class IBaseUnknown {
	public:
		IBaseUnknown () { }

		virtual ULONG AddRef () = 0;
		virtual ULONG Release () = 0;

		virtual ULONG GetRefCount () const = 0;

		virtual ~IBaseUnknown () { }
};	// end of IUnknown-like base class

typedef IBaseUnknown *LPIBUNKWN;
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CBaseUnknown : public IBaseUnknown {
	private:
		CRITICAL_SECTION	m_cs;
		ULONG					m_ulRefCount;

		// disable copy constructor and assignment operator
		CBaseUnknown (const CBaseUnknown& cbu);
		CBaseUnknown& operator= (const CBaseUnknown& cbu);

	protected:
		virtual HRESULT Lock ();
		virtual HRESULT Unlock ();

	public:
		CBaseUnknown ();
		virtual ~CBaseUnknown ();

		virtual ULONG AddRef ();
		virtual ULONG Release ();

		virtual ULONG GetRefCount () const
		{
			return m_ulRefCount;
		}
};	// end of base IUnknown implementation class

typedef CBaseUnknown *LPCBASEUNKNWN;
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// "guard" class - destructor releases guarded object
class CBUKGuard {
	private:
		// disable copy constructor and assignment operator
		CBUKGuard& operator = (const CBUKGuard& ukg);
		CBUKGuard (const CBUKGuard& ukg);

	protected:
		LPIBUNKWN&	m_ppBUK;

	public:
		CBUKGuard (LPIBUNKWN& pBUK) : m_ppBUK(pBUK)
		{
			
		}

		virtual ULONG AddRef ()
		{
			return ((NULL == (m_ppBUK)) ? 0UL : (m_ppBUK)->AddRef());
		}

		virtual ULONG Release ();

		virtual ULONG GetRefCount () const
		{
			return ((NULL == (m_ppBUK)) ? 0UL : (m_ppBUK)->GetRefCount());
		}

		virtual ~CBUKGuard ();
};	// end of guard class
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// template for wrapping base unknown derived objects so they auto-delete themselves
// when out of scope
template<class CBUKDerived> class LPCBUKDerived {
	private:
		CBUKDerived	*mp;

		void Reset ()
		{
			if (mp != NULL)
			{
				ULONG ulRefCount=mp->Release();
				mp = NULL;
			}
		}

	public:
		// also default constructor
		LPCBUKDerived (CBUKDerived *p=NULL) : mp(NULL)
		{
			*this = p;
		}

		LPCBUKDerived (const LPCBUKDerived& hx) : mp(NULL)
		{ 
			*this = hx.mp;
		}

		LPCBUKDerived& operator= (CBUKDerived *p)
		{
			Reset();
			if ((mp=p) != NULL)
			{
				ULONG	ulRefCount=mp->AddRef();
			}

			return *this;
		}

		LPCBUKDerived& operator= (const LPCBUKDerived& hx)
		{
			return (*this=hx.mp);
		}

		CBUKDerived *CreateObject ()
		{
			Reset();
			return (mp=new CBUKDerived);
		}

		CBUKDerived *operator-> () const { return mp; }

		CBUKDerived& operator* () { return *mp; }

		virtual ~LPCBUKDerived ()
		{
			Reset();
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

extern HRESULT GetUserAndDomainName (LPTSTR			lpszUserName,
												 const DWORD	dwMaxUserName,
												 LPTSTR			lpszDomainName,
												 const DWORD	dwMaxDomainName);

#define DEFAULT_WNET_BUFSIZE (16 * 1024)

#ifdef __cplusplus
typedef HRESULT (*WNET_ENUM_CFN)(const NETRESOURCE& nr, LPVOID pArg, BOOL& fContEnum);

extern HRESULT EnumDomainServers (WNET_ENUM_CFN lpfnEcfn, LPVOID pArg=NULL, const DWORD dwESize=DEFAULT_WNET_BUFSIZE);
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// base class for "reentrant" lock - i.e. one which can be locked
// repeatedly by same thread
class IReentrantLock {
	private:
		CRITICAL_SECTION	m_cs;
		DWORD					m_dwOwnerId;	// id of current owner thread (0 == no owner)
		HANDLE				m_hOwner;		// handle of current owner thread (NULL == no owner)
		time_t				m_tLockTime;	// time when lock started
		DWORD					m_dwRefCount;	// count of "locked" instances of owner thread

		// disable copy constructor and assignment operator
		IReentrantLock (const IReentrantLock& );
		IReentrantLock& operator= (const IReentrantLock& );

		void Clear ();

		void ILock ()
		{
			EnterCriticalSection(&m_cs);
		}

		void IUnlock ()
		{
			LeaveCriticalSection(&m_cs);
		}

	protected:
		virtual HRESULT Lock (const ULONG ulTimeout) = 0;
		virtual HRESULT Unlock () = 0;

		HRESULT IAddRef (const DWORD dwTid, const HANDLE hThread);
		HRESULT IRelease (const DWORD dwTid);

	public:
		IReentrantLock ()
		{
			Clear();
			InitializeCriticalSection(&m_cs);
		}

		virtual HANDLE GetOwner () const { return m_hOwner; }
		virtual DWORD GetOwnerId () const { return m_dwOwnerId; }
		virtual time_t GetLockTime () const { return m_tLockTime; }
		virtual DWORD GetRefCount () const { return m_dwRefCount; }

		virtual HRESULT AddRef (const DWORD dwTid,
										const HANDLE hThread,
										const ULONG ulTimeout);

		virtual HRESULT AddRef (const ULONG ulTimeout)
		{
			return AddRef(GetCurrentThreadId(), GetCurrentThread(), ulTimeout);
		}

		virtual HRESULT Wait ()
		{
			return AddRef(INFINITE);
		}

		virtual HRESULT Release (const DWORD dwTid);
		virtual HRESULT Release () { return Release(GetCurrentThreadId()); }

		virtual BOOL IsLocked () const { return (m_dwRefCount != 0); }

		// forcefully release lock
		virtual HRESULT Reset ();

		virtual ~IReentrantLock ()
		{
			DeleteCriticalSection(&m_cs);
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// "reentrant" mutex lock
class CReentrantMutex : public IReentrantLock {
	private:
		HANDLE	m_hMtx;

		// disable copy constructor and assignment operator
		CReentrantMutex (const CReentrantMutex& );
		CReentrantMutex& operator= (const CReentrantMutex& );

		virtual HRESULT Lock (const ULONG ulTimeout);
		virtual HRESULT Unlock ();

	public:
		CReentrantMutex () : IReentrantLock(), m_hMtx(NULL) { }
 
		// Note: may return ERROR_ALREADY_EXISTS for named mutex
		HRESULT Create (LPSECURITY_ATTRIBUTES	lpMutexAttributes=NULL,
							 const BOOL					bInitialOwner=FALSE,
							 LPCTSTR						lpszName=NULL);

		HANDLE GetHandle () const { return m_hMtx; }

		HRESULT Destroy ();

		virtual ~CReentrantMutex () { Destroy(); }
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// base class which takes care of unlocking when destroyed
class ILockGuard {
	private:
		BOOL	m_fIsLocked;
	
		// disable copy constructor and assignment operator
		ILockGuard (const ILockGuard& );
		ILockGuard& operator= (const ILockGuard& );

	public:
		ILockGuard () : m_fIsLocked(FALSE) { }

		virtual HRESULT Lock (const ULONG ulTimeout) = 0;
		virtual HRESULT Unlock () = 0;

		virtual ~ILockGuard () { if (m_fIsLocked) Unlock(); }
};	// end of ILockGuard interface
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CMtxLockGuard : public ILockGuard {
	private:
		HANDLE	m_hMtx;

		// disable copy constructor and assignment operator
		CMtxLockGuard (const CMtxLockGuard& );
		CMtxLockGuard& operator= (const CMtxLockGuard& );

	public:
		CMtxLockGuard (HANDLE hMtx=NULL) : m_hMtx(hMtx) { }

		virtual HRESULT SetLock (HANDLE hMtx)
		{
			return ((NULL == (m_hMtx=hMtx)) ? ERROR_BAD_ARGUMENTS : ERROR_SUCCESS);
		}

		virtual HRESULT Lock (const ULONG ulTimeout);

		virtual HRESULT Unlock ();

		virtual ~CMtxLockGuard () { }
};	// end of mutex lock guard
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CCSLockGuard : public ILockGuard {
	private:
		CRITICAL_SECTION	*m_pcs;

		// disable copy constructor and assignment operator
		CCSLockGuard (const CCSLockGuard& );
		CCSLockGuard& operator= (const CCSLockGuard& );

	public:
		CCSLockGuard (CRITICAL_SECTION *pcs=NULL) : m_pcs(pcs) { }

		virtual HRESULT SetLock (CRITICAL_SECTION *pcs)
		{
			return ((NULL == (m_pcs=pcs)) ? ERROR_BAD_ARGUMENTS : ERROR_SUCCESS);
		}

		virtual HRESULT Lock (const ULONG ulTimeout);

		virtual HRESULT Unlock ();

		virtual ~CCSLockGuard () { }
};	// end of critical section lock guard
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern HRESULT GetCurrentAppName (LPCTSTR& lpszAppName);
#endif	/* of ifdef __cplusplus */

extern HRESULT GetModuleDirectory (HMODULE hInst, LPTSTR lpszDir, const ULONG ulMaxLen);
#ifdef __cplusplus
inline HRESULT GetAppDirectory (LPTSTR lpszDir, const ULONG ulMaxLen)
{
	return GetModuleDirectory(NULL, lpszDir, ulMaxLen);
}
#else
#define GetAppDirectory(d,l) GetModuleDirectory(NULL, (d), (l))
#endif

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to measure difference in ticks between start/stop
//
// Note: the ticks counter wraps around every 49 days or so, thus
//			this calculation may yield large or even untrue numbers
//			(e.g. if several wrap-arounds took place between start/stop)
class CTicksMeasure : public IDurationMeasurer {
	protected:
		virtual DWORD GetCurrentValue () const
		{
			return ::GetTickCount();
		}

	public:
		CTicksMeasure (const BOOL fStartNow=FALSE)
			: IDurationMeasurer()
		{
			if (fStartNow)
				Start();
		}

		virtual ~CTicksMeasure ()
		{
		}
};

class CHiResPerfMeasure : public IDurationMeasurer {
	protected:
		LARGE_INTEGER	m_freq;
		LARGE_INTEGER	m_cStart;
		LARGE_INTEGER	m_cEnd;
		const DWORD		m_dwSecTicks;	// measurement resolution

		virtual void GetCurrentValue (LARGE_INTEGER& cv) const;

		virtual DWORD GetCurrentValue () const;

	public:
		// resolution may be 1 (seconds), 1000 (msec.), 1000000 (microsec), 1000000000 (nanosec.)
		CHiResPerfMeasure (const DWORD dwSecTicks=1000000UL, const BOOLEAN fStartNow=FALSE);

		virtual const DWORD GetResolutionFrequency () const
		{
			return m_dwSecTicks;
		}

		virtual const LARGE_INTEGER GetMeasurementFrequency () const
		{
			return m_freq;
		}

		virtual DWORD Start ();

		virtual DWORD Stop ();

		// returns duration not normalized to the ticks measure
		virtual const LARGE_INTEGER RawDuration () const;

		// returns duration normalized to the ticks measure
		virtual const LARGE_INTEGER FullDuration () const;

		// Note: returns ZERO if duration cannot be contained in a single DWORD
		virtual DWORD Duration () const;

		virtual ~CHiResPerfMeasure ()
		{
		}
};

class CMiliSecHiResPerfMeasure : public CHiResPerfMeasure {
	public:
		CMiliSecHiResPerfMeasure (const BOOLEAN fStartNow=FALSE)
			: CHiResPerfMeasure(1000UL, fStartNow)
		{
		}

		virtual ~CMiliSecHiResPerfMeasure ()
		{
		}
};

class CMicroSecHiResPerfMeasure : public CHiResPerfMeasure {
	public:
		CMicroSecHiResPerfMeasure (const BOOLEAN fStartNow=FALSE)
			: CHiResPerfMeasure(1000000UL, fStartNow)
		{
		}

		virtual ~CMicroSecHiResPerfMeasure ()
		{
		}
};

class CNanoSecHiResPerfMeasure : public CHiResPerfMeasure {
	public:
		CNanoSecHiResPerfMeasure (const BOOLEAN fStartNow=FALSE)
			: CHiResPerfMeasure(1000000000UL, fStartNow)
		{
		}

		virtual ~CNanoSecHiResPerfMeasure ()
		{
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

extern HRESULT GetSysErrorText (const HRESULT	rhr,
										  LPTSTR				lpszErrStr,
										  const ULONG		ulMaxLen);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CMappedFileViewGuard {
	private:
		CMappedFileViewGuard (const CMappedFileViewGuard& );
		CMappedFileViewGuard& operator= (const CMappedFileViewGuard& );

	protected:
		LPVOID&	m_pBuf;

	public:
		CMappedFileViewGuard (LPVOID& pBuf)
			: m_pBuf(pBuf)
		{
		}

		virtual HRESULT Release ()
		{
			HRESULT	hr=S_OK;

			if (m_pBuf != NULL)
			{
				if (!::UnmapViewOfFile(m_pBuf))
					hr = ::GetLastError();

				m_pBuf = NULL;
			}

			return hr;
		}

		virtual ~CMappedFileViewGuard ()
		{
			Release();
		}
};
#endif	/* of ifdef __cplusplus */

extern HRESULT mmap_fhmap (HANDLE hfMap, const BOOL fReadOnly, LPVOID *ppBuf);
extern HRESULT mmap_fhndl (HANDLE hFile, const BOOL fReadOnly, LPVOID *ppBuf);
extern HRESULT mmap_file (LPCTSTR lpszFile, const BOOL fReadOnly, LPVOID *ppBuf);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// shared memory map of string to data
class CSharedMemMap {	// not MT-safe !!!
	protected:
		// shared memory structure
		typedef struct {
			DWORD	dwMapSize;
			DWORD	dwMaxValues;
			DWORD	dwMaxKeyLen;
			DWORD	dwMaxValSize;
			DWORD	dwEntrySize;
			DWORD	dwFreeIndex;	// "heap" used to allocate new entries
			DWORD	dwNumItems;		// current number of items in the map
			BOOL	fCaseSensitive;
			BYTE	bData[sizeof(DWORD)];	// actual size is auto-allocated
		} SMMDESC, *LPSMMDESC;

		static DWORD GetMapMemorySize (const DWORD	dwMapSize,
												 const DWORD	dwMaxKeyLen,
												 const DWORD	dwMaxValues,
												 const DWORD	dwMaxValSize);

		static HRESULT GetAttachedMappingSize (LPCTSTR lpszMapName, DWORD& ulMapSize);

		HANDLE		m_hDesc;	// handle of shared memory object
		LPSMMDESC	m_pDesc;

		static HRESULT AttachSharedMap (LPCTSTR lpszMapName, HANDLE& hDesc, LPSMMDESC& pDesc);

		static HRESULT CreateSharedMap (LPCTSTR		lpszMapName,
												  const DWORD	dwMapSize,
												  const DWORD	dwMaxKeyLen,
												  const DWORD	dwMaxValues,
												  const DWORD	dwMaxValSize,
												  const BOOL	fCaseSensitive,
												  HANDLE&		hDesc,
												  LPSMMDESC&	pDesc);

		// returns hash value normalized to actual table size
		virtual HRESULT GetHashIndex (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwHashIdx) const;

		LPDWORD		m_pMapItems;
		LPBYTE		m_pMapValues;

		// helper structure for handling map entries
		typedef struct {
			DWORD		nextIndex;
			DWORD		prevIndex;
			DWORD		keyLen;	// not including the EOS
			DWORD		dataLen;
			LPCTSTR	lpszKey;
			LPBYTE	pData;
		} SMESTRUCT, *LPSMESTRUCT;

		virtual HRESULT GetValueStruct (const DWORD dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const;

		virtual HRESULT GetEntryStruct (const DWORD dwMapIndex, DWORD& dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const;

		virtual HRESULT GetEntryStruct (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwValIndex, LPBYTE& pEntry, SMESTRUCT& sme) const;

		static HRESULT InitSharedMapEntryStruct (LPBYTE	pEntry, const DWORD ulMaxEntrySize, SMESTRUCT& sme);

		static HRESULT UpdateSharedMapEntryStruct (const SMESTRUCT& sme, LPBYTE pEntry, const DWORD ulMaxEntrySize);

	public:
		CSharedMemMap ();

		// returns ERROR_ALREADY_EXISTS if map already created - in which case, provided size/len values are ignored
		virtual HRESULT Create (LPCTSTR		lpszMapName,
										const DWORD	dwMapSize,
										const DWORD	dwMaxKeyLen,
										const DWORD	dwMaxValues,
										const DWORD	dwMaxValSize,
										const BOOL	fCaseSensitive);

		/* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
		 *		If a map is attached while it is being created, then undefined
		 * behavior may occur when trying to find/add/remove key/value
		 * (reminder - the object is NOT MT-safe...).
		 */
		virtual HRESULT Attach (LPCTSTR lpszMapName);

		virtual BOOL IsInitialized () const;

		virtual DWORD GetNumItems () const
		{
			return (IsInitialized() ? m_pDesc->dwNumItems : 0);
		}

		virtual DWORD GetMapSize () const
		{
			return (IsInitialized() ? m_pDesc->dwMapSize : 0);
		}

		virtual DWORD GetMaxValues () const
		{
			return (IsInitialized() ? m_pDesc->dwMaxValues : 0);
		}

		// returns S_OK if key exists in the map, ERROR_SECTOR_NOT_FOUND if not (and other errors otherwise)
		virtual HRESULT FindValue (LPCTSTR lpszKey, const DWORD dwKeyLen, LPVOID& pVal, DWORD& vLen) const;

		// returns S_OK if key exists in the map, ERROR_SECTOR_NOT_FOUND if not (and other errors otherwise)
		virtual HRESULT FindValue (LPCTSTR lpszKey, LPVOID& pVal, DWORD& vLen) const
		{
			return FindValue(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), pVal, vLen);
		}

		// returns S_OK if key exists in the map, ERROR_SECTOR_NOT_FOUND if not (and other errors otherwise)
		virtual HRESULT FindKey (LPCTSTR lpszKey, const DWORD dwKeyLen) const
		{
			// dummy, don't care about returned value
			LPVOID	pVal=0;
			DWORD		vLen=0;

			return FindValue(lpszKey, dwKeyLen, pVal, vLen);
		}

		// returns S_OK if key exists in the map, ERROR_SECTOR_NOT_FOUND if not (and other errors otherwise)
		virtual HRESULT FindKey (LPCTSTR lpszKey) const
		{
			return FindKey(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey));
		}

		virtual HRESULT FindNumValue (LPCTSTR lpszKey, const DWORD dwKeyLen, DWORD& dwValue) const;

		virtual HRESULT FindNumValue (LPCTSTR lpszKey, DWORD& dwValue) const
		{
			return FindNumValue(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), dwValue);
		}

		virtual HRESULT AddValue (LPCTSTR lpszKey, const DWORD dwKeyLen /* may be 0 */, LPCVOID pVal, const DWORD vLen /* may be 0 */);

		virtual HRESULT AddValue (LPCTSTR lpszKey /* may be NULL/empty */, LPCVOID pVal, const DWORD vLen /* may be 0 */)
		{
			return AddValue(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), pVal, vLen);
		}

		virtual HRESULT AddNumValue (LPCTSTR lpszKey, const DWORD dwKeyLen /* may be 0 */, const DWORD dwValue)
		{
			return AddValue(lpszKey, dwKeyLen, &dwValue, (sizeof dwValue));
		}

		virtual HRESULT AddNumValue (LPCTSTR lpszKey /* may be NULL/empty */, const DWORD dwValue)
		{
			return AddNumValue(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey), dwValue);
		}

		virtual HRESULT AddStrValue (LPCTSTR lpszKey, const DWORD dwKeyLen, LPCTSTR lpszVal, const DWORD dwVLen)
		{
			return AddValue(lpszKey, dwKeyLen, lpszVal, dwVLen * sizeof(TCHAR));
		}

		virtual HRESULT AddStrValue (LPCTSTR lpszKey, LPCTSTR lpszVal, const DWORD dwVLen)
		{
			return AddStrValue(lpszKey, GetSafeStrlen(lpszKey), lpszVal, dwVLen);
		}

		virtual HRESULT AddStrValue (LPCTSTR lpszKey, const DWORD dwKeyLen, LPCTSTR lpszVal)
		{
			return AddStrValue(lpszKey, dwKeyLen, lpszVal, GetSafeStrlen(lpszVal));
		}

		virtual HRESULT AddStrValue (LPCTSTR lpszKey, LPCTSTR lpszVal)
		{
			return AddStrValue(lpszKey, GetSafeStrlen(lpszKey), lpszVal, GetSafeStrlen(lpszVal));
		}

		virtual HRESULT AddValue (LPCTSTR lpszKey, const DWORD dwKeyLen /* may be 0 */)
		{
			return AddValue(lpszKey, dwKeyLen, NULL, 0);
		}

		virtual HRESULT AddValue (LPCTSTR lpszKey /* may be NULL/empty */)
		{
			return AddValue(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey));
		}

		// may return ERROR_SECTOR_NOT_FOUND if the key is not there
		virtual HRESULT DeleteKey (LPCTSTR lpszKey, const DWORD dwKeyLen);

		virtual HRESULT DeleteKey (LPCTSTR lpszKey)
		{
			return DeleteKey(GetSafeStrPtr(lpszKey), GetSafeStrlen(lpszKey));
		}

		// dumps as CSV
		virtual HRESULT Export (FILE *fout);

		virtual HRESULT Export (LPCTSTR lpszFilePath);

		// removes all entries
		virtual HRESULT Clear ();

		virtual HRESULT Detach ();

		virtual ~CSharedMemMap ()
		{
			Detach();
		}

		friend class CSharedMemMapEnum;
};

class CSharedMemMapEnum {
	protected:
		const CSharedMemMap&	m_smm;
		const DWORD				m_dwMapSize;
		const DWORD				m_dwMaxVals;
		DWORD						m_dwMapIndex;
		DWORD						m_dwValIndex;
		DWORD						m_dwItemNum;

	public:
		CSharedMemMapEnum (const CSharedMemMap& smm);

		virtual DWORD GetNumItems () const
		{
			return m_smm.GetNumItems();
		}

		// returns ERROR_HANDLE_EOF if no more entries
		virtual HRESULT GetFirst (LPCTSTR& lpszKey, LPVOID& pVal, DWORD& vLen);

		// returns ERROR_HANDLE_EOF if no more entries
		virtual HRESULT GetNext (LPCTSTR& lpszKey, LPVOID& pVal, DWORD& vLen);

		virtual ~CSharedMemMapEnum ()
		{
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CUTF8ToWideCharString {
	private:
		LPWSTR					m_pW;
		CWideCharStrBufGuard	m_wsg;

		CUTF8ToWideCharString (const CUTF8ToWideCharString& );
		CUTF8ToWideCharString& operator= (const CUTF8ToWideCharString& );

	public:
		CUTF8ToWideCharString ()
			: m_pW(NULL), m_wsg(m_pW)
		{
		}

		// Note: setting a new string deletes the old one
		HRESULT SetString (LPCSTR lpszUTF8Str, const ULONG ulUTF8Len);

		HRESULT SetString (LPCSTR lpszUTF8Str)
		{
			return SetString(lpszUTF8Str, ((NULL == lpszUTF8Str) ? 0 : strlen(lpszUTF8Str)));
		}

		operator LPCWSTR () const
		{
			return m_pW;
		}

		virtual ~CUTF8ToWideCharString ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CWideCharToUTF8String {
	private:
		LPTSTR			m_pS;
		CStrBufGuard	m_ssg;

		CWideCharToUTF8String (const CWideCharToUTF8String& );
		CWideCharToUTF8String& operator= (const CWideCharToUTF8String& );

	public:
		CWideCharToUTF8String ()
			: m_pS(NULL), m_ssg(m_pS)
		{
		}

		// Note: for Asian characters the average multi-byte encoding may be greater (ZERO is illegal)

		// Note: setting a new string deletes the old one
		HRESULT SetString (LPCWSTR lpwStr, const ULONG ulWLen, const size_t sAvgMBEncLen=2);

		HRESULT SetString (LPCWSTR lpwStr, const size_t sAvgMBEncLen=2)
		{
			return SetString(lpwStr, ((NULL == lpwStr) ? 0 : wcslen(lpwStr)), sAvgMBEncLen);
		}

		operator LPCTSTR () const
		{
			return m_pS;
		}

		virtual ~CWideCharToUTF8String ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
typedef HRESULT (*MCENUMCALLBACK)(LPCTSTR			lpszModName,
											 const HMODULE	hModInst,
											 LPVOID			pArg,
											 BOOL&			fContEnum);

class CModulesCache {
	protected:
		CStr2PtrMapper	m_ModsMap;
		CStr2PtrMapper	m_FuncsMap;
		HANDLE			m_hAccMtx;
		LPCTSTR			m_lpszPrefix;
		LPCTSTR			m_lpszSuffix;
		LPCTSTR			m_lpszPathPrefix;

		// returns ERROR_FILE_NOT_FOUND if module not loaded in the first place
		virtual HRESULT RemoveModule (LPCTSTR lpszModName);

		virtual HRESULT InsertModule (LPCTSTR lpszModName, HMODULE& hModInst, const ULONG ulFlags);

		virtual HRESULT UpdateModuleInstance (LPCTSTR		lpszModName,
														  HMODULE&		hModInst,
														  BOOL&			fFirstLoad,
														  const ULONG ulFlags);

		virtual HRESULT UpdateModuleFunction (LPCTSTR		lpszModName,
														  LPCTSTR		lpszFuncName,
														  FARPROC&		lpfnProc,
														  BOOL&			fFirstLoad,
														  const ULONG	ulFlags);

	public:
		// Note: supplied strings must persist !!!
		CModulesCache (const UINT32	ulAvgModsNum,
							const UINT32	ulAvgFuncsNum=0,	// if zero then same as avg. modules num
							LPCTSTR			lpszPrefix=NULL,
							LPCTSTR			lpszSuffix=NULL,
							LPCTSTR			lpszPathPrefix=NULL);

		// module instance loading flags
		enum {
			MCFDLLMODULE=0x00000001,
			MCFEXEMODULE=0x00000002,
			MCFNOPREFIX=0x00000004,
			MCFNOSUFFIX=0x00000008,
			MCFNOPATHPREFIX=0x00000010,
			MCFAUTLOAD=0x00000020
		};

		virtual HRESULT Lock (const ULONG ulMaxWait) const;

		virtual HRESULT Unlock () const;

		// If module not in cache then it is loaded
		virtual HRESULT GetModuleInstance (LPCTSTR		lpszModName,
													  HMODULE&		hModInst,
													  BOOL&			fFirstLoad,
													  const ULONG	ulMaxWait=INFINITE,
													  const ULONG	ulFlags=MCFDLLMODULE);

		virtual HRESULT GetModuleFunction (LPCTSTR		lpszModName,
													  LPCTSTR		lpszFuncName,
													  FARPROC&		lpfnProc,
													  BOOL&			fFirstLoad,
													  const ULONG	ulMaxWait=INFINITE,
													  const ULONG	ulFlags=MCFAUTLOAD | MCFDLLMODULE);

		// Note: locks the cache !!!
		virtual HRESULT EnumModules (MCENUMCALLBACK lpfnEcfn, LPVOID pArg) const;

		// returns ERROR_FILE_NOT_FOUND if module not loaded in the first place
		virtual HRESULT UnloadModule (LPCTSTR lpszModName, const ULONG ulMaxWait=INFINITE);

		// Note: unloads all loaded modules and cannot be re-initialized.
		//			if non-OK return code then not all modules have been unloaded
		virtual HRESULT Clear ();

		virtual ~CModulesCache ()
		{
			Clear();
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// guards pointer retrieved via "GetEnvironmentStrings" call
class CEnvStringsGuard {
	private:
		LPVOID&	m_pEnv;

	public:
		CEnvStringsGuard (LPVOID& pEnv)
			: m_pEnv(pEnv)
		{
		}

		/*
		 *	Format:
		 *
		 *		- variable strings are separated by '\0'
		 *		- each variable is a "name=value" pair (e.g., "FOO=bar")
		 *		- the block is terminated by an extra '\0' (e.g., "LAST=123\0\0")
		 * NOTE: may be NULL/empty
		 */
		virtual LPCTSTR GetEnvironmentBlock () const
		{
			return (LPCTSTR) m_pEnv;
		}

		virtual BOOL Release ()
		{
			if (m_pEnv != NULL)
			{
				const BOOL	fRes=::FreeEnvironmentStrings((LPTSTR) m_pEnv);
				m_pEnv = NULL;
				return fRes;
			}
			else
			{
				return TRUE;
			}
		}

		virtual ~CEnvStringsGuard ()
		{
			Release();
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _WIN32_GENERAL_H_ */
