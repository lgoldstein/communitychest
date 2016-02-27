#include <win32/general.h>

/*---------------------------------------------------------------------------*/

CBaseUnknown::CBaseUnknown () : IBaseUnknown()
{
	InitializeCriticalSection(&m_cs);
	m_ulRefCount = 1;
}

CBaseUnknown::~CBaseUnknown ()
{
	DeleteCriticalSection(&m_cs);
}

/*---------------------------------------------------------------------------*/

HRESULT CBaseUnknown::Lock ()
{
	EnterCriticalSection(&m_cs);
	return ERROR_SUCCESS;
}

HRESULT CBaseUnknown::Unlock ()
{
	LeaveCriticalSection(&m_cs);
	return ERROR_SUCCESS;
}

/*---------------------------------------------------------------------------*/

ULONG CBaseUnknown::AddRef ()
{
	ULONG	ulRefCount=0UL;

	Lock();

	m_ulRefCount++;
	ulRefCount = m_ulRefCount;

	Unlock();

	return ulRefCount;
}

/*---------------------------------------------------------------------------*/

ULONG CBaseUnknown::Release ()
{
	ULONG	ulRefCount=0UL;

	Lock();

	m_ulRefCount--;
	ulRefCount = m_ulRefCount;

	Unlock();

	if (0 == ulRefCount)
		delete this;

	return ulRefCount;
}

//////////////////////////////////////////////////////////////////////////////

ULONG CBUKGuard::Release ()
{
	if (NULL == m_ppBUK)
		return ((ULONG) (-1));

	ULONG	ulRefCount=m_ppBUK->Release();
	m_ppBUK = NULL;

	return ulRefCount;
}

/*---------------------------------------------------------------------------*/

CBUKGuard::~CBUKGuard ()
{
	const ULONG	ulRefCount=Release();
	UNREFERENCED_PARAMETER(ulRefCount);
}

/*---------------------------------------------------------------------------*/
