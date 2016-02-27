/////////////////////////////////////////////////////////////////////////////
// Base16.cpp
#include <util/Base16.h>
#include <util/string.h>

/////////////////////////////////////////////////////////////////////////////

LPBYTE Base16::makeBase (const LPBYTE a, const size_t aLen)
{
	const size_t	ulTotal=(aLen * 2) + 2;
	LPBYTE ret=new BYTE[ulTotal];
	if (ret == NULL)
		return NULL;

	memset(ret, 0, ulTotal);	// The last are null terminators

	const size_t	xLen=byte_array_to_hex_string(a, aLen, (char *) ret, (ulTotal+1), '\0');
	return ret;
}
    
/////////////////////////////////////////////////////////////////////////////
// getBase() allocates memory for ret pointer
// Caller should release this pointer afterwards

BOOLEAN Base16::getBase(const LPBYTE a, const size_t aLen, LPBYTE *ret)
{
	const size_t	rLen=aLen / 2;
	if (NULL == (*ret=new BYTE[rLen]))
		return FALSE;

	EXC_TYPE			exc=EOK;
	const size_t	xLen=hex_string_to_byte_array((LPCTSTR) a, aLen, *ret, rLen, '\0', EXC_ARG(exc));
	if (exc != EOK)
	{
		delete [] (*ret);
		*ret = NULL;
		return FALSE;
	}

	return TRUE;
}   

/////////////////////////////////////////////////////////////////////////////

