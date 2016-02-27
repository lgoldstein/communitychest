#ifndef _RFC2046_RAWMSGPARSER_H
#define _RFC2046_RAWMSGPARSER_H

#include <internet/rfc822msg.h>

/*---------------------------------------------------------------------------*/

#define MAX_RAW_RFC2046_MSG_LINE_LEN	1022	/* actually according to RFC2822 it is less */

#define RFC2046_ENVELOPE_RAW_MSG_PART_ID	0

/* only if indeed exists, otherwise, this part ID is assigned to an attachment */
#define RFC2046_BODY_RAW_MSG_PART_ID	(RFC2046_ENVELOPE_RAW_MSG_PART_ID+1)

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRawRFC2046MsgPart {
	private:
		CRawRFC2046MsgPart (const CRawRFC2046MsgPart& );
		CRawRFC2046MsgPart& operator= (const CRawRFC2046MsgPart& );

	protected:
		// part headers
		CRFC822HdrsTbl	m_partHdrs;

		// Note !!! we protect this since it is dynamically allocated
		LPTSTR	m_lpszPartName;		// empty/NULL for body and envelope

	public:
		// Note !!! we make this public only due to efficiency reasons...
		UINT32	m_ulPartID;	// 0 == envelope
		UINT32	m_ulHdrsStartOffset;	// same as m_ulDataStartOffset if no headers
		UINT32	m_ulHdrsEndOffset;	// same as m_ulHdrsStartOffset if no headers
		UINT32	m_ulDataStartOffset;	// for envelope same as m_ulHdrsEndOffset
		UINT32	m_ulDataEndOffset;	// for envelope same as m_ulDataStartOffset (i.e. zero length data)

		CRawRFC2046MsgPart (const UINT32	ulMaxHdrs=0);

		virtual EXC_TYPE Init (const UINT32 ulMaxHdrs);

		virtual EXC_TYPE UpdatePartHdrs (const CRFC822HdrsTbl& ht)
		{
			return m_partHdrs.UpdateHdrsTbl(ht);
		}

		virtual EXC_TYPE UpdatePartHdrs (const CRawRFC2046MsgPart& mp)
		{
			return UpdatePartHdrs(mp.GetPartHdrs());
		}

		// Note: does NOTE copy the headers !!!
		virtual EXC_TYPE UpdatePartDescriptor (const CRawRFC2046MsgPart& mp);

		// Note: resets and updates all information
		virtual EXC_TYPE	UpdateRawMsgPart (const CRawRFC2046MsgPart& mp);

		virtual EXC_TYPE SetName (LPCTSTR lpszPartName, const UINT32 ulNameLen)
		{
			return ::strupdatebuf(lpszPartName, ulNameLen, m_lpszPartName);
		}

		virtual EXC_TYPE SetName (LPCTSTR lpszPartName)
		{
			return ::strupdatebuf(lpszPartName, m_lpszPartName);
		}

		virtual LPCTSTR GetName () const
		{
			return m_lpszPartName;
		}

		virtual const CRFC822HdrsTbl& GetPartHdrs () const
		{
			return m_partHdrs;
		}

		virtual void Reset ();

		virtual ~CRawRFC2046MsgPart ()
		{
			Reset();
		}
};

typedef CAllocStructPtrGuard<CRawRFC2046MsgPart>	CRawRFC2046MPGuard;
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRawRFC2046MsgPartsCollection {
	private:
		// disable copy constructor and assignment operator
		CRawRFC2046MsgPartsCollection (const CRawRFC2046MsgPartsCollection& );
		CRawRFC2046MsgPartsCollection& operator= (const CRawRFC2046MsgPartsCollection& );

	protected:
		CVSDCollection	m_MsgParts;	// each member is a pointer to a CRawRFC2046MsgPart

	public:
		CRawRFC2046MsgPartsCollection (const UINT32 ulInitialSize=0, const UINT32 ulGrowSize=0)
			: m_MsgParts(ulInitialSize, ulGrowSize)
		{
		}

		// Note: cannot be re-initialized unless cleared first !!!
		EXC_TYPE Init (const UINT32 ulInitialSize, const UINT32 ulGrowSize)
		{
			return m_MsgParts.SetParams(ulInitialSize, ulGrowSize);
		}

		// Note: the part is added as a POINTER (!!!) and will be released upon destruction
		EXC_TYPE AddPart (const UINT32 ulMaxHdrs, CRawRFC2046MsgPart*	&mp);

		const UINT32 GetNumOfParts () const
		{
			return m_MsgParts.GetSize();
		}

		CRawRFC2046MsgPart *GetPart (const UINT32 ulMPdx) const
		{
			return (CRawRFC2046MsgPart *) m_MsgParts[ulMPdx];
		}

		CRawRFC2046MsgPart *operator[] (const UINT32 ulMPdx) const
		{
			return (CRawRFC2046MsgPart *) m_MsgParts[ulMPdx];
		}

		// Must be re-initialized to be used
		void Clear ();

		~CRawRFC2046MsgPartsCollection ()
		{
			Clear();
		}
};

typedef CAllocStructPtrGuard<CRawRFC2046MsgPartsCollection>	CRawRFC2046MsgPartsCollGuard;
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class IRawRFC2046MsgInputStream {
	private:
		IRawRFC2046MsgInputStream (const IRawRFC2046MsgInputStream& );
		IRawRFC2046MsgInputStream& operator= (const IRawRFC2046MsgInputStream& );

	public:
		IRawRFC2046MsgInputStream ()
		{
		}

		// returns current stream position (starting at zero)
		virtual EXC_TYPE GetCurReadOffset (UINT32& ulOffset) const = 0;

		// Note: returns ECONTINUED if entire line buffer filled (including terminating '\0'),
		//			but no end-of-line found. In any case, the '\n' is NOT included in the line buffer.
		virtual EXC_TYPE ReadLine (LPTSTR lpszLine, const UINT32 ulMaxLen, UINT32& ulCurLen) = 0;

		virtual ~IRawRFC2046MsgInputStream ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRawRFC2046MsgDescBuilder {
	private:
		const CStr2PtrMapper&				m_hdrsMap;	// keys are names of envelope headers to be cached
		CRawRFC2046MsgPartsCollection&	m_msgParts;
		UINT32									m_ulMBLen;
		LPTSTR									m_lpszMIMEBoundary;

		CRawRFC2046MsgDescBuilder (const CRawRFC2046MsgDescBuilder& );
		CRawRFC2046MsgDescBuilder& operator= (const CRawRFC2046MsgDescBuilder& );

		EXC_TYPE GetCurPart (const UINT32 ulMaxHdrsNum, const UINT32 ulPartID, CRawRFC2046MsgPart* &pPart);

		EXC_TYPE ProcessMsgContentType (LPCTSTR lpszHdrValue);

		EXC_TYPE ProcessMsgEnvelope (IRawRFC2046MsgInputStream&	ims,
											  const UINT32						ulMaxEnvHdrsNum,
											  LPTSTR								lpszWorkBuf,
											  const UINT32						ulMaxBufLen);

		EXC_TYPE IsMIMEBoundary (LPCTSTR lpszWorkBuf, const UINT32 ulReadLen, BOOLEAN& fIsBoundary) const;

		EXC_TYPE SkipToNextMIMEBoundary (IRawRFC2046MsgInputStream& ims,
													LPTSTR							lpszWorkBuf,
													const UINT32					ulMaxBufLen);

		EXC_TYPE ProcessMsgPart (const UINT32						ulPartID,
										 IRawRFC2046MsgInputStream&	ims,
										 const UINT32						ulMaxPartHdrsNum,
										 LPTSTR								lpszWorkBuf,
										 const UINT32						ulMaxBufLen);

		EXC_TYPE ProcessDirectAttachMsg (IRawRFC2046MsgInputStream&	ims,
													LPTSTR							lpszWorkBuf,
													const UINT32					ulMaxBufLen);

		EXC_TYPE ProcessMsgParts (IRawRFC2046MsgInputStream&	ims,
										  const UINT32						ulMaxPartHdrsNum,
										  LPTSTR								lpszWorkBuf,
										  const UINT32						ulMaxBufLen);

	public:
		CRawRFC2046MsgDescBuilder (const CStr2PtrMapper& hdrsMap, CRawRFC2046MsgPartsCollection& msgParts);

		EXC_TYPE ProcessInputStream (IRawRFC2046MsgInputStream&	ims,
											  const UINT32						ulMaxEnvHdrsNum,
											  const UINT32						ulMaxPartHdrsNum,
											  LPTSTR								lpszWorkBuf,
											  const UINT32						ulMaxBufLen);

		~CRawRFC2046MsgDescBuilder ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE BuildRFC2046MsgDescription (IRawRFC2046MsgInputStream&		ims,
														  CRawRFC2046MsgPartsCollection&	msgParts,
														  const CStr2PtrMapper&				hdrsMap, // keys are names of envelope headers to be cached
														  const UINT32							ulMaxEnvHdrsNum, // 0 == same as size of hdrsMap
														  const UINT32							ulMaxPartHdrsNum,
														  const UINT32							ulWorkBufLen=MAX_RAW_RFC2046_MSG_LINE_LEN,
														  LPTSTR									lpszWorkBuf=NULL /* auto-allocate */);
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CRawRFC2046MsgFileInputStream : public IRawRFC2046MsgInputStream {
	private:
		CRawRFC2046MsgFileInputStream (const CRawRFC2046MsgFileInputStream &);
		CRawRFC2046MsgFileInputStream& operator= (const CRawRFC2046MsgFileInputStream &);

	protected:
		FILE		*m_fp;	// assumed to be opened on a TEXT file (not binary !!!)
		BOOLEAN	m_fAutoClose;

	public:
		CRawRFC2046MsgFileInputStream (FILE *fp=NULL, const BOOLEAN fAutoClose=FALSE)
			: IRawRFC2046MsgInputStream(), m_fp(fp), m_fAutoClose(fAutoClose)
		{
		}

		// Note: removes any previous file object
		EXC_TYPE SetFile (FILE *fp, const BOOLEAN fAutoClose);

		// Note: implies auto-close
		EXC_TYPE SetFile (LPCTSTR lpszFilePath)
		{
			return SetFile(::_tfopen(GetSafeStrPtr(lpszFilePath), _T("r")), TRUE);
		}

		CRawRFC2046MsgFileInputStream (LPCTSTR lpszFilePath)
			: IRawRFC2046MsgInputStream(), m_fp(NULL), m_fAutoClose(FALSE)
		{
			EXC_TYPE exc=SetFile(lpszFilePath);
		}

		FILE *GetFile () const
		{
			return m_fp;
		}

		BOOLEAN IsAutoClose () const
		{
			return m_fAutoClose;
		}

		void SetAutoClose (const BOOLEAN fAutoClose)
		{
			m_fAutoClose = fAutoClose;
		}

		// no effect if already closed
		void Close ();

		// returns current stream position (starting at zero)
		virtual EXC_TYPE GetCurReadOffset (UINT32& ulOffset) const;

		// Note: returns ECONTINUED if entire line buffer filled (including terminating '\0') but no end-of-line found
		virtual EXC_TYPE ReadLine (LPTSTR lpszLine, const UINT32 ulMaxLen, UINT32& ulCurLen);

		virtual ~CRawRFC2046MsgFileInputStream ()
		{
			Close();
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to look for MIME boundary, and then use it
class CMIMEBoundaryHunter {
	private:
		TCHAR	m_szMIMEBoundary[MAX_RFC822_MIME_BOUNDARY_LEN+2];

	public:
		CMIMEBoundaryHunter ()
		{
			m_szMIMEBoundary[0] = _T('\0');
		}

		/*		Ignores non-"Content-Type:" headers or non-continuation data - i.e.,
		 * assumes the boundary property (and for that matter ANY property) resides
		 * on a separate line of data.
		 */
		EXC_TYPE ProcessHdr (LPCTSTR lpszHdrName, const BOOLEAN fIsContHdr, LPCTSTR lpszHdrValue);

		EXC_TYPE ProcessHdr (const CRFC822HdrParser& hdrParser)
		{
			return ProcessHdr(hdrParser.GetHdrName(), hdrParser.IsContHdr(), hdrParser.GetHdrValue());
		}

		LPCTSTR GetBoundary () const
		{
			return m_szMIMEBoundary;
		}

		BOOLEAN HaveBoundary () const
		{
			return (m_szMIMEBoundary[0] != _T('\0'));
		}

		EXC_TYPE IsMIMEBoundary (LPCTSTR lpszLine, BOOLEAN& fIsBoundary, BOOLEAN& fIsLast) const
		{
			return ::IsRFC822MIMEBoundary(lpszLine, m_szMIMEBoundary, &fIsBoundary, &fIsLast);
		}

		void Reset ()
		{
			m_szMIMEBoundary[0] = _T('\0');
		}

		~CMIMEBoundaryHunter ()
		{
		}
};
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* _RFC2046_RAWMSGPARSER_H */
