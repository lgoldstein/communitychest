#ifndef _QPENC_H_
#define _QPENC_H_

/*---------------------------------------------------------------------------*/

#include <limits.h>
#include <stdio.h>

#include <util/string.h>
#include <futils/general.h>

/*---------------------------------------------------------------------------*/

/*	For explanation of the quoted printable encoding see RFC1341, 2045 */

extern const char qpXferEncoding[];

#define MAX_QPENC_LINE_LEN	74
#define QPDELIM	'='

/*---------------------------------------------------------------------------*/

extern EXC_TYPE qp_encode_buf (const char		szIBuf[],
										 const UINT32	ulILen,	/* chars to encode */
										 UINT32			*pulInLen,	/* encoded chars */
										 char				szOBuf[],
										 const UINT32	ulOLen,	/* available space */
										 UINT32			*pulOutLen);	/* used space */

extern EXC_TYPE qp_decode_buf (const char		szIBuf[],
										 const UINT32	ulILen,	/* chars to encode */
										 UINT32			*pulInLen,	/* encoded chars */
										 char				szOBuf[],
										 const UINT32	ulOLen,	/* available space */
										 UINT32			*pulOutLen);	/* used space */

/*---------------------------------------------------------------------------*/

/* minimum size of work blocks for encode/decode */
#define QPENC_MIN_IBLKSIZE	256

/*		Implement a quoted-printable encoding session - the session consists of internally
 * allocated resources (e.g. memory). In the interest of efficiency, one can
 * create a session with some resources and then keep using it.
 */
typedef void *LPQPESESSION;

/*	Opens and initializes a quoted-printable encode/decode session handle (NULL if error) */
extern LPQPESESSION qpsess_create (void);

#define qpsess_encode_create() qpsess_create()
#define qpsess_decode_create() qpsess_create()

extern EXC_TYPE qpsess_delete (LPQPESESSION lpSess);

#define qpsess_encode_delete(pSess) qpsess_delete(pSess)
#define qpsess_decode_delete(pSess) qpsess_delete(pSess)

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new encoding session */
extern EXC_TYPE qpsess_encode_start (LPQPESESSION		lpSess,
												 IOWRITECALLBACK	lpfnWcfn,
												 void					*pFout,
						/* may be NULL */	 char					pBuf[],
												 const UINT32		iBufSize);
														 
/*	Process a new buffer for this session */
extern EXC_TYPE qpsess_encode_process (LPQPESESSION	lpSess,
													const char		pBuf[],
													const UINT32	ulBufLen);

/*	Close the encoding session */
extern EXC_TYPE qpsess_encode_end (LPQPESESSION lpSess);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to encapsulate quoted-printable encoding
class CQPEncoder {
	private:
		LPQPESESSION		m_pSess;
		IOWRITECALLBACK	m_lpfnWcfn;
		char					*m_pBuf;
		UINT32				m_ulBufLen;

	public:
		CQPEncoder ()
			: m_lpfnWcfn(NULL), m_pBuf(NULL), m_ulBufLen(0)
		{ 
			m_pSess = ::qpsess_encode_create();
		}

		EXC_TYPE Restart (void *pFout)
		{
			return ::qpsess_encode_start(m_pSess, m_lpfnWcfn, pFout, m_pBuf, m_ulBufLen);
		}

		EXC_TYPE Start (char					pBuf[],	// may be NULL
							 const UINT32		ulBufLen,
							 IOWRITECALLBACK	lpfnWcfn,
							 void					*pFout)
		{
			m_lpfnWcfn = lpfnWcfn;
			m_pBuf = pBuf;
			m_ulBufLen = ulBufLen;
			return Restart(pFout);
		}

		EXC_TYPE Process (const char pBuf[], const UINT32 ulBufLen)
		{
			return ::qpsess_encode_process(m_pSess, pBuf, ulBufLen);
		}

		EXC_TYPE End () { return ::qpsess_encode_end(m_pSess); }

		virtual ~CQPEncoder () { ::qpsess_encode_delete(m_pSess); }

};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/* encodes the contents of the "fin" stream in quoted-printable and places the output in
 * the "fout" stream. The routine allocates 2 working buffers of "blkSize" (the
 * output buffer is actually ~1.33 * blkSize), which are freed once routine is
 * completed. The I/O is performed via the supplied  callback functions.
 */

extern EXC_TYPE qp_encode_stream (IOREADCALLBACK	lpfnRcfn,
											 void					*pFin,
											 IOWRITECALLBACK	lpfnWcfn,
											 void					*pFout,
											 const UINT32		iBlkSize);

#ifdef __cplusplus
inline EXC_TYPE qp_encode_file (FILE				*fin,
										  IOWRITECALLBACK	lpfnWcfn,
										  void				*pFout,
										  const UINT32		iBlkSize)
{
	return qp_encode_stream(fileIOReadCfn, (void *) fin, lpfnWcfn, pFout, iBlkSize);
}
#else
#define qp_encode_file(fin,lpfnWcfn,pFout,iSugBlkSize)	\
	qp_encode_stream(fileIOReadCfn,(void *) (fin),lpfnWcfn,pFout,iSugBlkSize)
#endif

extern EXC_TYPE qp_encode_named_file (LPCTSTR			lpszFPath,
												  IOWRITECALLBACK	lpfnWcfn,
												  void				*pFout,
												  const UINT32		iSugBlkSize);

/*---------------------------------------------------------------------------*/

/*	Initialize and prepare for a new decoding session */
extern EXC_TYPE qpsess_decode_start (LPQPESESSION		lpSess,
						/* may be NULL */	 char					pBuf[],
												 const UINT32		iMaxLen,
												 IOWRITECALLBACK	lpfnWcfn,
												 void					*pFout);

extern EXC_TYPE qpsess_decode_process (LPQPESESSION	lpSess,
													const char		lpszBuf[],
													const UINT32	ulBufLen);

extern EXC_TYPE qpsess_decode_flush (LPQPESESSION lpSess);

extern EXC_TYPE qpsess_decode_end (LPQPESESSION	lpSess);

extern EXC_TYPE qp_decode_stream (IOREADCALLBACK	lpfnRcfn,
											 void					*pFin,
											 IOWRITECALLBACK	lpfnWcfn,
											 void					*pFout,
											 const UINT32		iBlkSize);

#ifdef __cplusplus
inline EXC_TYPE qp_decode_to_file (IOREADCALLBACK	lpfnRcfn,
											  void				*pFin,
											  FILE				*fout,
											  const UINT32		iBlkSize)
{
	return qp_decode_stream(lpfnRcfn, pFin, fileIOWriteCfn, (LPVOID) fout, iBlkSize);
}
#else
#define qp_decode_to_file(lpfnRcfn,pFin,fout,iBlkSize)	\
	qp_decode_stream(lpfnRcfn, pFin, fileIOWriteCfn, (LPVOID) fout, iBlkSize)
#endif

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to encapsulate quoted-printable decoding
class CQPDecoder {
	private:
		LPQPESESSION		m_pSess;
		IOWRITECALLBACK	m_lpfnWcfn;
		char					*m_pBuf;
		UINT32				m_iMaxLen;

	public:

		CQPDecoder () : m_lpfnWcfn(NULL), m_pBuf(NULL), m_iMaxLen(0)
		{
			m_pSess = ::qpsess_decode_create();
		}

		EXC_TYPE Restart (void *pFout)
		{
			return ::qpsess_decode_start(m_pSess, m_pBuf, m_iMaxLen, m_lpfnWcfn, pFout);
		}

		EXC_TYPE Start (char					pBuf[],
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
			return ::qpsess_decode_process(m_pSess, lpszBuf, ulBufLen);
		}

		EXC_TYPE Flush () { return ::qpsess_decode_flush(m_pSess); }

		EXC_TYPE End () { return ::qpsess_decode_end(m_pSess); }

		virtual ~CQPDecoder () { ::qpsess_decode_delete(m_pSess); }

};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE qp_decode_to_named_file (IOREADCALLBACK	lpfnRcfn,
													  LPVOID				pFin,
													  LPCTSTR			lpszOutput,
													  const UINT32		iBlkSize);

/*---------------------------------------------------------------------------*/

#endif /* of _QPENC_ */
