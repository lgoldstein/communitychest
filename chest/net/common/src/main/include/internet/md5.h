#ifndef _MD5_H_
#define _MD5_H_

#include <util/string.h>

/* Copyright (C) 1991-2, RSA Data Security, Inc. Created 1991. All
 * rights reserved.
 *
 * License to copy and use this software is granted provided that it
 * is identified as the "RSA Data Security, Inc. MD5 Message-Digest
 * Algorithm" in all material mentioning or referencing this software
 * or this function.
 *
 * License is also granted to make and use derivative works provided
 * that such works are identified as "derived from the RSA Data
 * Security, Inc. MD5 Message-Digest Algorithm" in all material
 * mentioning or referencing the derived work.
 *
 * RSA Data Security, Inc. makes no representations concerning either
 * the merchantability of this software or the suitability of this
 * software for any particular purpose. It is provided "as is"
 * without express or implied warranty of any kind.
 *
 * These notices must be retained in any copies of any part of this
 * documentation and/or software.
 */
/*--------------------------------------------------------------------------*/

#define MD5_STATE_LEN	4
#define MD5_COUNT_BITS	2
#define MD5_BLOCK_LEN	64
#define MD5_DIGEST_LEN	16

typedef UINT8 MD5_DIGEST_TYPE[MD5_DIGEST_LEN];

#define HEXMD5DIGEST_DISPLAY_LEN (MD5_DIGEST_LEN * MAX_BYTE_HEX_DISPLAY_LENGTH)

/* MD5 context */
typedef struct {
  UINT32 state[MD5_STATE_LEN];     /* state (ABCD) */
  UINT32 count[MD5_COUNT_BITS]; /* number of bits, modulo 2^64 (lsb first) */
  UINT8	buffer[MD5_BLOCK_LEN];   /* input buffer */
} MD5_CTX;

/*--------------------------------------------------------------------------*/

/* MD5 initialization. Begins an MD5 operation, writing a new context. */
extern EXC_TYPE MD5Init (MD5_CTX *pContext);

/* MD5 block update operation. Continues an MD5 message-digest
 * operation, processing another message block, and updating the
 * context.
 */
extern EXC_TYPE MD5Update (MD5_CTX *pContext, const UINT8 iBuf[], const UINT32 bLen);

/* MD5 finalization. Ends an MD5 message-digest operation, writing the
 * the message digest and zeroizing the context.
 *
 * Note: digest buffer must be at least MD5_DIGEST_LEN size
 */
extern EXC_TYPE MD5Final (MD5_CTX *pContext, UINT8 pDigest[], const UINT32 ulDLen);

/*--------------------------------------------------------------------------*/

/* Calculates the MD5 signature of the supplied buffer/string.
 *
 * Note: digest buffer must be at least MD5_DIGEST_LEN size
 */
extern EXC_TYPE MD5Calc (const UINT8 iBuf[], const UINT32 bLen, UINT8 pDigest[], const UINT32 ulDLen);

#ifdef __cplusplus
inline EXC_TYPE MD5StrCalc (LPCTSTR lpszStr, UINT8 pDigest[], const UINT32 ulDLen)
{
	return MD5Calc((const UINT8 *) lpszStr, GetSafeStrlen(lpszStr), pDigest, ulDLen);
}
#else
#	define MD5StrCalc(s,d,l)	MD5Calc((const UINT8 *) (s), GetSafeStrlen(s), (d), (l))
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

/* Calculates the MD5 signature of the supplied file.
 *
 * Note: digest buffer must be at least MD5_DIGEST_LEN size
 */

extern EXC_TYPE MD5File (const char		*lpszFilePath,
								 const UINT32	ulReadSize,
								 UINT8			pDigest[],
								 const UINT32	ulDLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CMD5Digester {
	private:
		MD5_CTX	m_ctx;

	public:
		virtual EXC_TYPE Reset ()
		{
			return ::MD5Init(&m_ctx);
		}

		virtual EXC_TYPE Update (const UINT8 iBuf[], const UINT32 bLen)
		{
			return ::MD5Update (&m_ctx, iBuf, bLen);
		}

		// NOTE: resets the context after successful calculation
		virtual EXC_TYPE Digest (UINT8 pDigest[], const UINT32 ulDLen)
		{
			return ::MD5Final(&m_ctx, pDigest, ulDLen);
		}

		// NOTE: calls Update and then resets the context after successful calculation
		virtual EXC_TYPE Digest (const UINT8 iBuf[], const UINT32 bLen, UINT8 pDigest[], const UINT32 ulDLen);

		CMD5Digester ()
		{
			Reset();
		}

		virtual ~CMD5Digester ()
		{
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#endif /* of _MD5_H_ */