#ifndef _BASE64_H_
#define _BASE64_H_

/*---------------------------------------------------------------------------*/

#include <limits.h>

#include <_types.h>
#include <futils/general.h>

/*---------------------------------------------------------------------------*/

/*	The base64 algorithm is rather simple:
 *
 * -	every 3 octets are viewed as a 24 bit value. This value is
 *		then "decomposed" into 4 6-bit values. Each 6-bit value is
 *		an index into a translation table (below) which yields a 7-bit
 *		ASCII character.
 *
 * -	the output "stream" is broken into lines of length of up to 76
 *		characters at the most, terminated by CRLF.
 */

#define BASE64_MAX_LINE_LEN		76
#define BASE64_BITS_PER_OCTET		6
#define BASE64_INPUT_BLOCK_LEN	3
#define BASE64_OUTPUT_BLOCK_LEN	4
#define BASE64_ENCODED_BITS_NUM	((BASE64_INPUT_BLOCK_LEN) * (CHAR_BIT))

/* maximum buffer required to hold encoding of a given size */
#define BASE64_MAX_OUTBUF_LEN(iBufSize)	\
	((((iBufSize) * BASE64_OUTPUT_BLOCK_LEN) / BASE64_INPUT_BLOCK_LEN) + BASE64_OUTPUT_BLOCK_LEN)
/* maximum buffer required to hold decoding of a given size */
#define BASE64_MAX_INBUF_LEN(oBufSize)	\
	((((oBufSize) * BASE64_INPUT_BLOCK_LEN) / BASE64_OUTPUT_BLOCK_LEN) + BASE64_INPUT_BLOCK_LEN)

#define BASE64_MAX_VALUE		((unsigned)(1<<BASE64_BITS_PER_OCTET))
#define BASE64_MASK_VALUE		((unsigned) (BASE64_MAX_VALUE) - 1)
#define FIsBadBase64Value(v)	(((unsigned) (v)) >= BASE64_MAX_VALUE)

#define BASE64_PAD_CHAR	'='

/* string to be used when declaring a BASE64 encoding (e.g. RFC822) */
extern const char b64XferEncoding[];

/*---------------------------------------------------------------------------*/

#define FIsBadBase64Char(c) \
	(((c) != BASE64_PAD_CHAR) && (FIsBadBase64Value(base64_decode_tbl[(char) (c)])))

extern UINT32 B64IOWrite (IOWRITECALLBACK lpfnWcfn, void *pFout, const char pBuf[], const UINT32 ulBufLen);

/*---------------------------------------------------------------------------*/
/* the following is a pseudo-code for how to use the base74 encoding routines:
 *
 *			do
 *			{
 *				rLen = read(iBuf, IBUF_SIZE);
 *				b64_encode_buf(iBuf, IBUF_SIZE, &iLen, outBuf, OBUF_SIZE, &oLen);
 *				output_buf(outBuf, oLen);
 *
 *				...take care of cases in which (iLen < rLen) and/or not enough
 *					data in outbuf to warrant a line.
 *			} while (IBUF_SIZE == rLen);
 *
 *			b64_finish_buf(iBuf, remLen, oBuf, oLen...);
 *
 *			flush_output(obuf);
 */
/*---------------------------------------------------------------------------*/

/* Note: outMaxLen should be > (4/3) * inBufLen because the algorithm
 *			encodes every 3 octets into 4.
 */
extern EXC_TYPE b64_encode_buf (const UINT8	inBuf[],
										  const UINT32	inBufLen,	/* bytes to encode */
										  UINT32			*inLen,		/* encoded bytes */
										  char			outBuf[],
										  const UINT32	outBufLen,	/* available space */
										  UINT32			*outLen);	/* used space */

/*
 * build the last block encoding (special treatment)
 *
 *	Note: if entire buffer is contained in one call, then "b64_encode_finish"
 *			may be called directly (since if calls "b64_encode_buf" anyway
 */
extern EXC_TYPE b64_encode_finish (const UINT8	inRemBuf[],	/* un-encoded */
											  const UINT32	inRemLen,
											  char			outBuf[],
											  const UINT32	outRemLen,	/* at least 4 */
											  UINT32			*outLen);

/*---------------------------------------------------------------------------*/

/* returns:
 *		EOK - data processed
 *		EEOF - no more data
 *		EPREPOSITION - some illegal values were found (and ignored)
 *		E??? - other internal errors
 */
extern EXC_TYPE b64_decode_buf (const char	inBuf[],
										  const UINT32	inBufLen,
										  UINT32			*inLen,	/* actual decode size */
										  UINT8			outBuf[],
										  const UINT32	outBufLen,
										  UINT32			*outLen);

/*---------------------------------------------------------------------------*/

/* minimum size of work blocks for encode/decode */
#define BASE64_MIN_IBLKSIZE	256

/*		Implement a base64 encoding session - the session consists of internally
 * allocated resources (e.g. memory). In the interest of efficiency, one can
 * create a session with some resources and then keep using it.
 */
typedef void	*LPB64ESESSION;

/*	Opens and initializes a base64 encode/decode session handle (NULL if error) */
extern LPB64ESESSION b64sess_create (void);

#define b64sess_encode_create() b64sess_create()
#define b64sess_decode_create() b64sess_create()

extern EXC_TYPE b64sess_delete (LPB64ESESSION lpSess);

#define b64sess_encode_delete(pSess) b64sess_delete(pSess)
#define b64sess_decode_delete(pSess) b64sess_delete(pSess)

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new encoding session */
extern EXC_TYPE b64sess_encode_start (LPB64ESESSION	lpSess,
												  IOWRITECALLBACK	lpfnWcfn,
												  void				*pFout);
														 
/*	Process a new buffer for this session */
extern EXC_TYPE b64sess_encode_process (LPB64ESESSION	lpSess,
													 const UINT8	pBuf[],
													 const UINT32	ulBufLen);

/*		Finish whatever "leftovers" there are (Note: callback function for
 * writing may still be called.
 */
extern EXC_TYPE b64sess_encode_finish (LPB64ESESSION lpSess);

/*		Close the encoding session ("b64sess_encode_finish" is called first !!)
 */
extern EXC_TYPE b64sess_encode_end (LPB64ESESSION lpSess);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to encapsulate base64 encoding
class CB64Encoder {
	private:
		LPB64ESESSION		m_pSess;
		IOWRITECALLBACK	m_lpfnWcfn;

	public:
		CB64Encoder () : m_lpfnWcfn(NULL) { m_pSess = ::b64sess_encode_create(); }

		EXC_TYPE Restart (void *pFout)
		{
			return ::b64sess_encode_start(m_pSess, m_lpfnWcfn, pFout);
		}

		EXC_TYPE Start (IOWRITECALLBACK lpfnWcfn, void *pFout)
		{
			m_lpfnWcfn = lpfnWcfn;
			return Restart(pFout);
		}

		EXC_TYPE Process (const UINT8 pBuf[], const UINT32 ulBufLen)
		{
			return ::b64sess_encode_process(m_pSess, pBuf, ulBufLen);
		}

		EXC_TYPE Finish () { return ::b64sess_encode_finish(m_pSess); }

		EXC_TYPE End () { return ::b64sess_encode_end(m_pSess); }

		virtual ~CB64Encoder () { ::b64sess_encode_delete(m_pSess); }

};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* encodes the contents of the "fin" stream in base64 and places the output in
 * the "fout" stream. The routine allocates 2 working buffers of "blkSize" (the
 * output buffer is actually ~1.33 * blkSize), which are freed once routine is
 * completed. The I/O is performed via the supplied  callback functions.
 */

extern EXC_TYPE b64_encode_stream (IOREADCALLBACK	lpfnRcfn,
											  void				*pFin,
											  IOWRITECALLBACK	lpfnWcfn,
											  void				*pFout,
											  const UINT32		iBlkSize);

#ifdef __cplusplus
inline EXC_TYPE b64_encode_file (FILE					*fin,
											IOWRITECALLBACK	lpfnWcfn,
											void					*pFout,
											const UINT32		iBlkSize)
{
	return b64_encode_stream(fileIOReadCfn, (void *) fin, lpfnWcfn, pFout, iBlkSize);
}
#else
#define b64_encode_file(fin,lpfnWcfn,pFout,iSugBlkSize)	\
	b64_encode_stream(fileIOReadCfn, (void *) (fin), lpfnWcfn, pFout, iSugBlkSize)
#endif

extern EXC_TYPE b64_encode_named_file (const char			pszFPath[],
													IOWRITECALLBACK	lpfnWcfn,
													void					*pFout,
													const UINT32		iSugBlkSize);

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new decoding session */
extern EXC_TYPE b64sess_decode_start (LPB64ESESSION	lpSess,
												  UINT8				pBuf[],
												  const UINT32		iMaxLen,
												  IOWRITECALLBACK	lpfnWcfn,
												  void				*pFout);

extern EXC_TYPE b64sess_decode_flush (LPB64ESESSION	lpSess);

extern EXC_TYPE b64sess_decode_process (LPB64ESESSION	lpSess,
													 const char		lpszBuf[],
													 const UINT32	ulBufLen);

extern EXC_TYPE b64sess_decode_end (LPB64ESESSION	lpSess);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to encapsulate base64 encoding
class CB64Decoder {
	private:
		LPB64ESESSION		m_pSess;
		IOWRITECALLBACK	m_lpfnWcfn;
		UINT8					*m_pBuf;
		UINT32				m_iMaxLen;

	public:

		CB64Decoder () : m_lpfnWcfn(NULL), m_pBuf(NULL), m_iMaxLen(0)
		{
			m_pSess = ::b64sess_decode_create();
		}

		EXC_TYPE Restart (void *pFout)
		{
			return ::b64sess_decode_start(m_pSess, m_pBuf, m_iMaxLen, m_lpfnWcfn, pFout);
		}

		EXC_TYPE Start (UINT8				pBuf[],
							 const UINT32		iMaxLen,
							 IOWRITECALLBACK	lpfnWcfn,
							 void					*pFout)
		{
			m_pBuf = pBuf;
			m_iMaxLen = iMaxLen;
			m_lpfnWcfn = lpfnWcfn;

			return Restart(pFout);
		}

		EXC_TYPE Process (const char lpszBuf[], const UINT32 ulBufLen)
		{
			return ::b64sess_decode_process(m_pSess, lpszBuf, ulBufLen);
		}

		EXC_TYPE Flush () { return ::b64sess_decode_flush(m_pSess); }

		EXC_TYPE End () { return ::b64sess_decode_end(m_pSess); }

		virtual ~CB64Decoder () { ::b64sess_decode_delete(m_pSess); }

};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Decodes the incoming stream into the output stream. If a MIME boundary is
 * supplied, then decoding stops once boundary is encountered. In this case,
 * the function returns an indication if this is the last boundary. At the same
 * time, the input stream should be positioned at the first char of the base64
 * encoding.
 *
 * Note: if boundary used, then upon return the input stream is positioned on
 *			first char of first line AFTER the boundary.
 *
 * If no boundary supplied, then entire input stream is decoded.
 */
extern EXC_TYPE b64_decode_stream (IOREADCALLBACK	lpfnRcfn,
											  void				*pFin,
											  IOWRITECALLBACK	lpfnWcfn,
											  void				*pFout,
											  const char		MIMEBoundary[],
											  const UINT32		iBlkSize,
											  BOOLEAN			*pfIsLast);

#ifdef __cplusplus
inline EXC_TYPE b64_decode_to_file (IOREADCALLBACK	lpfnRcfn,
												void				*pFin,
												FILE				*fout,
												const char		MIMEBoundary[],
												const UINT32	iBlkSize,
												BOOLEAN			*pfIsLast)
{
	return b64_decode_stream(lpfnRcfn, pFin, fileIOWriteCfn, (void *) fout, MIMEBoundary, iBlkSize, pfIsLast);
}
#else
#define b64_decode_to_file(lpfnRcfn,pFin,fout,MIMEb,iBlkSize,pfIsLast)	\
	b64_decode_stream(lpfnRcfn, pFin, fileIOWriteCfn, (void *) (fout), MIMEb, iBlkSize, pfIsLast)
#endif

extern EXC_TYPE b64_decode_to_named_file (IOREADCALLBACK	lpfnRcfn,
														void				*pFin,
														const char		pszFilePath[],
														const char		MIMEBoundary[],
														const UINT32	iBlkSize,
														BOOLEAN			*pfIsLast);

#define b64_decode_file_to_named_file(fin,of,mmb,isz,pfl)	\
	b64_decode_to_named_file(fileIOReadCfn, (void *) (fin), (of), (mmb), (isz), (pfl))

extern EXC_TYPE b64_decode_named_to_named_file (const char		pszInputFilePath[],
																const char		pszOutputFilePath[],
																const char		MIMEBoundary[],
																const UINT32	iBlkSize,
																BOOLEAN			*pfIsLast);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE b64_calc_encode_size (const UINT32		ulFSize,
												  const BOOLEAN	fAddLinesSeps,
												  UINT32				*pulB64Size);

extern EXC_TYPE b64_calc_decode_size (const UINT32	ulEncSize,
												  const UINT32	ulLineLen,
												  UINT32			*pulBinSize);

extern EXC_TYPE b64_get_file_encode_size (const char		lpszFilePath[],
														const BOOLEAN	fAddLinesSep,
														UINT32			*pulB64Size);

/*---------------------------------------------------------------------------*/

#endif /* of ifdef BASE64_H */
