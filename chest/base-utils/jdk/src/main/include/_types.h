#ifndef __BASIC_TYPES_H_
#define __BASIC_TYPES_H_

/*---------------------------------------------------------------------------*/
/*
 * _types.h
 *
 * Contains basic widely used types
 */
/*---------------------------------------------------------------------------*/

#include <stddef.h>

/*---------------------------------------------------------------------------*/

/* special definitions for different O/S */
#ifdef WIN32
#	include <wtypes.h>
#	include <tchar.h>

#	ifndef OK
#		define OK	0
#	endif

/* if ERROR defined but set as "0" then un-define it */
#	ifdef ERROR
#		if ERROR == 0
#			undef ERROR
#		endif
#	endif

#	ifndef ERROR
#		define ERROR	(-1)
#	endif

typedef int STATUS;

#	ifndef _ASMLANGUAGE
typedef DWORD EXC_TYPE;
#	endif /* of _ASMLANGUAGE */
#else

/* used by some WIN32 macros for text/character */
#	define _T(v)	v

typedef char TCHAR;
typedef TCHAR *LPTSTR;
typedef const TCHAR *LPCTSTR;

typedef char *LPSTR;
typedef const char *LPCSTR;

typedef wchar_t	WCHAR;
typedef WCHAR *LPWSTR;
typedef const WCHAR *LPCWSTR;
#endif	/* of ifdef WIN32 */

/*---------------------------------------------------------------------------*/

/*
 * Defines the CPU architecture (i.e. data-bus width)
 */

typedef unsigned int NATIVE_WORD;

/* basic types */

typedef unsigned char  		UINT8;    /* Unsigned  8 bit integer */
typedef unsigned short int	UINT16;   /* Unsigned 16 bit integer */
#ifndef _BASETSD_H_
typedef unsigned long  		UINT32;   /* Unsigned 32 bit integer */
#endif

typedef          char  SINT8;
typedef short    int   SINT16;
typedef          long  SINT32;

/* types with fixed sizes for communication applications' use. */

#ifndef WIN32
typedef UINT8  BYTE;
typedef UINT16 WORD;
typedef UINT32 DWORD;

typedef BYTE *LPBYTE;
#endif

/*---------------------------------------------------------------------------*/

/*
 * Maximum values for each basic type
 */

#ifdef __cplusplus
const UINT8  UINT8_MAX =(UINT8)  0xFF;
const UINT16 UINT16_MAX=(UINT16) 0xFFFF;
const UINT32 UINT32_MAX=(UINT32) 0xFFFFFFFF;

const SINT8 SINT8_MIN=(SINT8)	(-127);
const SINT8 SINT8_MAX=(SINT8)	0x7f;

const SINT16 SINT16_MIN=(SINT16) (-32767);
const SINT16 SIN16_MAX=(SINT16) 0x7fff;

const SINT32 SINT32_MIN=(SINT32) (-2147483647);
const SINT32 SINT32_MAX=(SINT32) 2147483648;
#else
#define UINT8_MAX    ((UINT8)  0xFF)
#define UINT16_MAX   ((UINT16) 0xFFFF)
#define UINT32_MAX   ((UINT32) 0xFFFFFFFF)

#define SINT8_MIN		((SINT8)	(-127))
#define SINT8_MAX		((SINT8)	128)

#define SINT16_MIN	((SINT16) (-32767))
#define SIN16_MAX		((SINT16)	32768)

#define SINT32_MIN   ((SINT32) (-2147483647))
#define SINT32_MAX   ((SINT32) 2147483648)
#endif /* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/*
 *	This macro returns number of bits in the "sz" (size in bytes) parameter
 */

#define SZ_BITS_NUM(sz)		((DWORD) (((DWORD)) (sz) << 3))

/*
 *	This macro returns number of bits in the "TYPE" parameter 
 */

#define TYPE_BITS_NUM(typ)	((DWORD) (((DWORD) sizeof(typ)) << 3))

/*
 *	This macro returns number of BYTES required to store "bn" bits num
 */

#define BN_BYTES_NUM(bn)	((DWORD) (((DWORD) (bn)) >> 3))

/*---------------------------------------------------------------------------*/

/*
 * Num of bits for each basic type
 */

#define INT8_BITS_NUM  		TYPE_BITS_NUM(BYTE) 
#define INT8_MSBIT_IDX  	((INT8_BITS_NUM)-1)

#define INT16_BITS_NUM  	TYPE_BITS_NUM(WORD) 
#define INT16_MSBIT_IDX  	((INT16_BITS_NUM)-1)

#define INT32_BITS_NUM 		TYPE_BITS_NUM(DWORD) 
#define INT32_MSBIT_IDX  	((INT32_BITS_NUM)-1)

#define NATIVE_WORD_BITS_NUM 	TYPE_BITS_NUM(NATIVE_WORD) 
#define NATIVE_WORD_MSBIT_IDX ((NATIVE_WORD_BITS_NUM)-1) 

/*---------------------------------------------------------------------------*/

/*
 * Exception code(s) - codes are enumerated in "util/errors.h"
 */

#ifdef __cplusplus
const EXC_TYPE EOK=0x0000;

#define EXC_PARAM(exc)	EXC_TYPE& ##exc
#define EXC_LVAL(exc)	(exc)
#define EXC_ARG(exc)		exc
#else
#define EOK    ((EXC_TYPE) 0x0000)

#define EXC_PARAM(exc)	EXC_TYPE ##*exc
#define EXC_LVAL(exc)	*(exc)
#define EXC_ARG(exc)		&##exc
#endif /* of ifdef __cplusplus */

#define EXC_VAL(exc,v) (EXC_LVAL(exc)) = v

/*---------------------------------------------------------------------------*/

/* useful macros */

/*
 * Returns:
 *
 *    +1 - if val > 0
 *    -1 - if val < 0
 *     0 - if val = 0
 */

#ifndef SIGNOF
#define SIGNOF(val) (((val) > 0) ? 1 : (((val) == 0) ? 0 : (-1)))
#endif

#ifndef ODD
#define ODD(val) (((val) & 1) != 0)
#endif

#ifndef EVEN
#define EVEN(val) (((val) & 1) == 0)
#endif

/*
 * Computes the ABSOLUTE difference between 2 NUMERICAL values of ANY type.
 */

#define absdiff(v1,v2) (((v1) >= (v2)) ? ((v1)-(v2)) : ((v2)-(v1)))

/*
 * some (non-ANSI) "stdlib.h" files do not contain these definitions
 */

#ifndef max
#define max(a,b)   (((a) > (b)) ? (a) : (b))
#endif

#ifndef min
#define min(a,b)   (((a) < (b)) ? (a) : (b))
#endif

#ifndef abs
#define abs(a)		(((a) > 0) ? (a) : (0 - (a)))
#endif

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
template<class ALBT> class CAllocBufPtrGuard {
	private:
		// disable copy constructor and assignment operator
		CAllocBufPtrGuard (const CAllocBufPtrGuard& );
		CAllocBufPtrGuard& operator= (const CAllocBufPtrGuard& );

	protected:
		ALBT&	m_pBuf;

	public:
		CAllocBufPtrGuard (ALBT& pBuf) : m_pBuf(pBuf) { }

		virtual void Release ()
		{
			if (m_pBuf != NULL)
			{
				delete [] m_pBuf;
				m_pBuf = NULL;
			}
		}

		virtual ~CAllocBufPtrGuard ()
		{
			Release();
		}
};	// end of allocated buffer guard class

typedef CAllocBufPtrGuard<LPTSTR>	CStrBufGuard;
typedef CAllocBufPtrGuard<LPSTR>		CAsciiStrBufGuard;
typedef CAllocBufPtrGuard<LPWSTR>	CWideCharStrBufGuard;
typedef CAllocBufPtrGuard<LPBYTE>	CBytesBufGuard;
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
template<class ASTPG> class CAllocStructPtrGuard {
	private:
		// disable copy constructor and assignment operator
		CAllocStructPtrGuard (const CAllocStructPtrGuard& );
		CAllocStructPtrGuard& operator= (const CAllocStructPtrGuard& );

	protected:
		ASTPG*	&m_pStruct;

	public:
		CAllocStructPtrGuard (ASTPG*	&pStruct)
			: m_pStruct(pStruct)
		{
		}

		virtual void Release ()
		{
			if (m_pStruct != NULL)
			{
				delete m_pStruct;
				m_pStruct = NULL;
			}
		}

		virtual ~CAllocStructPtrGuard ()
		{
			Release();
		}
};	// end of allocated structure guard class
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define IOCFN_BAD_LEN	((UINT32) (~0))

/*		Callback(s) used for I/O stream(s) for the data buf to be encoded. The
 * functions should return the actual read/write len (or B64_BAD_LEN if I/O
 * error occurred).
 *
 * Due to simplicity considerations, the EOF state is assumed to exist whenever
 * the number of actually read bytes is LESS than the requested number. This
 * means that if an implementation cannot guarantee achieving the required buffer
 * size in a single I/O operation (e.g. sockets...), then it must use repeated
 * input I/O until EOF or requested buffer length is achieved.
 *
 * The same applies to writing - if a number of bytes LESS than requested has
 * been written, then an I/O error is assumed (!!!)
 *
 * The writing callback must not assume a NULL terminated string !!!
 */
typedef UINT32 (*IOREADCALLBACK)(void *pFin,UINT8 pBuf[],const UINT32 ulBufLen);
typedef UINT32 (*IOWRITECALLBACK)(void *pFout,const char pBuf[],const UINT32 ulBufLen);

/*
 * Callback that can be used to seek a stream
 *
 *		nDir - <0 means BACKWARD-RELATIVE to current position
 *				 >0 means FORWARD-RELATIVE to current position
 *				 =0 means ABSOLUTE POSITION
 *
 *		ulCount - ABSOLUTE offset to be used for seeking
 */
typedef EXC_TYPE (*IOSEEKCALLBACK)(void *pFp, const int nDir, const UINT32 ulCount);

/* reads a "line" (including the '\n') from the input (up to max buf len) */
extern UINT32 sIOReadLine (IOREADCALLBACK	lpfnRcfn,
									void				*pFin,
									UINT8				pBuf[],
									const UINT32	ulBufLen);

/*---------------------------------------------------------------------------*/

#endif	/* of ifndef __BASIC_TYPES_H */
