/////////////////////////////////////////////////////////////////////////////
// Base16.h
#ifndef _ENCRYPT_BASE16_H_
#define _ENCRYPT_BASE16_H_

#include <_types.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class Base16 {
	public:
		Base16()  {}
		virtual ~Base16()  {}
    
		LPBYTE	makeBase(const LPBYTE a, const size_t aLen );


		// getBase() allocates memory for ret pointer
		// Caller should release this pointer afterwards
		//
		BOOLEAN	getBase(const LPBYTE a, const size_t aLen, LPBYTE *ret);
};
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif	/* _ENCRYPT_BASE16_H */
