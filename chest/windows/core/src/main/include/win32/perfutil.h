#ifndef _PERFUTIL_H_
#define _PERFUTIL_H_

/*---------------------------------------------------------------------------*/
/*		Useful utilities (classes) for handling performance monitoring counters
 */
/*---------------------------------------------------------------------------*/

#include <winperf.h>

#include <win32/general.h>

/*---------------------------------------------------------------------------*/

enum PERFORMANCE_TITLE_TYPE {
	PERF_TITLE_COUNTER,
	PERF_TITLE_EXPLAIN,
	PERF_TITLE_BAD
};

/*---------------------------------------------------------------------------*/

// impmenets a container for counters titles
class CPerfTitleDatabase {
	private:
		ULONG	m_nLastIndex;
		PTSTR *m_TitleStrings;
		PTSTR m_pszRawStrings;

   public:
		CPerfTitleDatabase ()	// must call InitTitleDatabase
		{
			m_nLastIndex = 0;
			m_pszRawStrings = NULL;
			m_TitleStrings = NULL;
		}

		CPerfTitleDatabase (PERFORMANCE_TITLE_TYPE titleType)
		{
			m_nLastIndex = 0;
			m_pszRawStrings = NULL;
			m_TitleStrings = NULL;

			InitTitleDatabase(titleType);
		}

		virtual ~CPerfTitleDatabase ()
		{
			DisposeTitleDatabase();
		}

		BOOL	InitTitleDatabase (PERFORMANCE_TITLE_TYPE titleType);
		void	DisposeTitleDatabase ();

		ULONG GetLastTitleIndex (void) const { return m_nLastIndex; }

		const PTSTR GetTitleStringFromIndex (const ULONG index) const
		{
			if ((NULL == m_TitleStrings) || (index > m_nLastIndex)) // Is index within range?
				return NULL;
			else
				return m_TitleStrings[index];
		}

		// returns 0 if unsuccessful
		ULONG GetIndexFromTitleString (const PTSTR pszTitleString) const;
}; // end of class CPerfTitleDatabase

typedef CPerfTitleDatabase *PCPerfTitleDatabase;

/*---------------------------------------------------------------------------*/

// Implements a class which takes a snapshot of current performance counters
class CPerfSnapshot {
	private:
		PPERF_DATA_BLOCK		m_pPerfDataHeader;  // Points to snapshot data
		PCPerfTitleDatabase  m_pCounterTitles;  // The title conversion object

		// Private function to convert the ASCII strings passedto TakeSnapshot()
		// into a suitable form for the RegQueryValue call
		BOOL ConvertSnapshotItemName (const PTSTR pszIn,
												PTSTR			pszOut,
												const DWORD nSize);

    public:
		CPerfSnapshot (PCPerfTitleDatabase pCounterTitles)
		{
			m_pPerfDataHeader = NULL;
			m_pCounterTitles = pCounterTitles;
		}

		virtual ~CPerfSnapshot (void) {	DisposeSnapshot(); }

		// Items must be delimted by ','
		BOOL TakeSnapshot (const PTSTR pszSnapshotItems);

		void DisposeSnapshot(void);	// invalidate current snapshot

		DWORD GetNumObjectTypes (void) const // # of objects in the snapshot
		{
			return ((m_pPerfDataHeader != NULL) ? m_pPerfDataHeader->NumObjectTypes: 0);
		}

		BOOL GetSystemName (PTSTR pszSystemName, const DWORD nSize) const;

		// Returns a header to the first byte following the PERF_DATA_BLOCK
		// (including the variable length system name string at the end)
		PVOID GetPostHeaderPointer (void)
		{
			if (m_pPerfDataHeader != NULL)
				return MakePtr(PVOID,m_pPerfDataHeader,m_pPerfDataHeader->HeaderLength);
			else
				return NULL;
		}
}; // end of class CPerfSnapshot

typedef CPerfSnapshot *PCPerfSnapshot;

/*---------------------------------------------------------------------------*/

class CPerfCounter
{
	public:
		CPerfCounter ()	// must call InitCounter subsequently
		{
			m_pszName = NULL;
			m_pData = NULL;
		}

		CPerfCounter (PTSTR const pszName, const DWORD type,
                    PBYTE const pData, const DWORD cbData);
		virtual ~CPerfCounter () { CleanupCounter(); }

		BOOL InitCounter (PTSTR const pszName, const DWORD type,
								PBYTE const pData, const DWORD cbData);
		void CleanupCounter (void);

		const PTSTR GetName (void) const { return m_pszName; }
		DWORD GetType (void) const { return m_type; }
		DWORD GetSize (void) const { return m_cbData; }

		// if "pType" NULL then caller does not require data type (e.g. use "GetType" method)
		BOOL GetData (PBYTE pBuffer, const DWORD cbBuffer, DWORD *pType) const;

		// If less than 64 bits then stored in "LowPart"
		BOOL GetValue (ULARGE_INTEGER	*puliValue);

		// If less than 32 bits then value returned
		BOOL GetValue (PDWORD pdwValue);

		BOOL Format (PTSTR pszBuffer, const DWORD nSize, const BOOL fHex=FALSE) const;

	protected:
		PTSTR m_pszName;
		DWORD m_type;
		PBYTE m_pData;
		DWORD m_cbData;

}; // end of class CPerfCounter;

typedef CPerfCounter *PCPerfCounter;

/*---------------------------------------------------------------------------*/

// container class for performance counters instances
class CPerfObjectInstance
{
	public:
		CPerfObjectInstance (PPERF_INSTANCE_DEFINITION const pPerfInstDef,
									PPERF_COUNTER_DEFINITION const pPerfCntrDef,
									const DWORD nCounters,
									PCPerfTitleDatabase const pPerfTitleDatabase,
									BOOL fDummy=FALSE)
		{
			InitObjectInstance(pPerfInstDef, pPerfCntrDef, nCounters, pPerfTitleDatabase, fDummy);
		}

		// Must call InitObjectInstance subsequently
		CPerfObjectInstance () { DisposeObjectInstance(); }

		virtual ~CPerfObjectInstance () { DisposeObjectInstance(); }

		BOOL InitObjectInstance (PPERF_INSTANCE_DEFINITION const pPerfInstDef,
										 PPERF_COUNTER_DEFINITION const pPerfCntrDef,
										 const DWORD nCounters,
										 PCPerfTitleDatabase const pPerfTitleDatabase,
										 BOOL fDummy=FALSE);
		void DisposeObjectInstance (void);

		BOOL GetObjectInstanceName (const PTSTR pszObjInstName,
											 const DWORD nSize) const;

		// Functions take an external CPerfCounter pointer. If NULL, a NEW one
		// is created and the caller is responsible for deleting in when done.
		PCPerfCounter GetFirstCounter (PCPerfCounter pPerfCntr);
		PCPerfCounter GetNextCounter (PCPerfCounter pPerfCntr);
		PCPerfCounter GetCounterByName (PTSTR const		pszName,
												  PCPerfCounter	pPerfCntr) const;

	protected:
		PPERF_INSTANCE_DEFINITION	m_pPerfInstDef;
		PPERF_COUNTER_DEFINITION	m_pPerfCntrDef;

		ULONG m_nCounters;
		ULONG m_currentCounter;

		PCPerfTitleDatabase m_pPerfCounterTitles;
		PCPerfTitleDatabase m_pCounterTitleDatabase;

		BOOL m_fDummy;  // FALSE normally, TRUE when an object with no instances

		// Caller must delete counter once no longer needed
		PCPerfCounter MakeCounter (PPERF_COUNTER_DEFINITION const pCounter,
											PCPerfCounter				 pPerfCntr) const;

		// Caller must delete counter once no longer needed
		PCPerfCounter GetCounterByIndex (const DWORD		index,
													PCPerfCounter	pPerfCntr) const;
};

typedef CPerfObjectInstance *PCPerfObjectInstance;

/*---------------------------------------------------------------------------*/

class CPerfObject
{
	public:
		CPerfObject ()	// Must call InitPerfObject subsequently
		{
			m_pObjectList = NULL;
			m_pPerfCounterTitles = NULL;
		}

		CPerfObject (PPERF_OBJECT_TYPE const pObjectList,
						 PCPerfTitleDatabase const pPerfCounterTitles)
		{
			InitPerfObject(pObjectList, pPerfCounterTitles);
		}
		virtual ~CPerfObject () { DisposePerfObject(); }

		void DisposePerfObject (void)
		{
			m_pObjectList = NULL;
			m_pPerfCounterTitles = NULL;
		}

		BOOL InitPerfObject (PPERF_OBJECT_TYPE const pObjectList,
								   PCPerfTitleDatabase const pPerfCounterTitles);

		// Functions take an external CPerfObjectInstance pointer. If NULL, a NEW
		// one is created, and caller is responsible for deleting it when done.
		PCPerfObjectInstance GetFirstObjectInstance (PCPerfObjectInstance pInst);
		PCPerfObjectInstance GetNextObjectInstance (PCPerfObjectInstance pInst);

		ULONG GetObjectInstanceCount (void) const
		{
			return (m_pObjectList != NULL) ? m_pObjectList->NumInstances : 0;
		}

		BOOL GetObjectTypeName (PTSTR pszObjTypeName, const DWORD nSize) const;

	protected:
		PPERF_OBJECT_TYPE				m_pObjectList;
		PPERF_INSTANCE_DEFINITION	m_pCurrentObjectInstanceDefinition;
		PCPerfTitleDatabase			m_pPerfCounterTitles;
		ULONG								m_currentObjectInstance;
};

typedef CPerfObject *PCPerfObject;

/*---------------------------------------------------------------------------*/

class CPerfObjectList
{
	public:
		CPerfObjectList (PCPerfSnapshot const pPerfSnapshot,
							  PCPerfTitleDatabase const pPerfCounterTitles)
		{
			m_pPerfSnapshot = pPerfSnapshot;
			m_pPerfCounterTitles = pPerfCounterTitles;
			m_currentObjectListIndex = 0;
			m_pCurrObjectType = NULL;
		}
		virtual ~CPerfObjectList () { }

		// Functions that return CPerfObject pointers have an external
		// CPerfObject parameter. If caller did not supply a CPerfObject pointer,
		// then a new one is created and caller is responsible for deleting it
		// once done with it.
		PCPerfObject GetFirstPerfObject (PCPerfObject pPerfObj);
		PCPerfObject GetNextPerfObject (PCPerfObject pPerfObj);
    
		PCPerfObject GetPerfObject (PTSTR const	pszObjListName,
											 PCPerfObject	pPerfObj) const;

	protected:
		PCPerfSnapshot			m_pPerfSnapshot;
		PCPerfTitleDatabase	m_pPerfCounterTitles;
		ULONG						m_currentObjectListIndex;
		PPERF_OBJECT_TYPE		m_pCurrObjectType; // current first/next object ptr
};

typedef CPerfObjectList *PCPerfObjectList;

/*---------------------------------------------------------------------------*/

#endif /* of _PERFUTIL_H_ */
