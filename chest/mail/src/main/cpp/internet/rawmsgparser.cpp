#include <internet/rawmsgparser.h>

//////////////////////////////////////////////////////////////////////////////

static EXC_TYPE AddHdrParserValue (CRFC822HdrsTbl& htbl, CRFC822HdrParser& hdrParser)
{
	EXC_TYPE	exc=EOK;
	LPCTSTR	lpszHdrName=hdrParser.GetHdrName(), lpszHdrValue=hdrParser.GetHdrValue();
	if (hdrParser.IsContHdr())
		exc = htbl.AddHdrData(lpszHdrName, lpszHdrValue);
	else
		exc = htbl.SetHdrData(lpszHdrName, lpszHdrValue);
	if (exc != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE UpdatePartName (CRawRFC2046MsgPart& mpPart, LPCTSTR lpszHdrName, const CRFC822HdrsTbl& mpHdrs)
{
	// if already have a name, then do nothing
	if (!IsEmptyStr(mpPart.GetName()))
		return EOK;

	// if this is not a header known to contain names, then do nothing
	if ((_tcsicmp(lpszHdrName, pszStdContentTypeHdr) != 0) && (_tcsicmp(lpszHdrName, pszStdContentDisposition) != 0))
		return EOK;

	LPCRFC822HDRDATA	pHdrData=NULL;
	EXC_TYPE				exc=mpHdrs.GetHdrData(lpszHdrName, pHdrData);
	// the header data MUST exist, since it is called AFTER having been inserted into the headers table
	if (exc != EOK)
		return exc;

	// OK if not found a name
	LPCTSTR	lpszHdrValue=pHdrData->GetHdrValue(), lpszNamePos=NULL;
	UINT32	ulNameLen=0;
	if ((exc=RFC822FindAttachName(lpszHdrValue, &lpszNamePos, &ulNameLen)) != EOK)
		return EOK;

	if ((exc=mpPart.SetName(lpszNamePos, ulNameLen)) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

CRawRFC2046MsgDescBuilder::CRawRFC2046MsgDescBuilder (const CStr2PtrMapper& hdrsMap, CRawRFC2046MsgPartsCollection& msgParts)
	: m_hdrsMap(hdrsMap), m_msgParts(msgParts), m_lpszMIMEBoundary(NULL), m_ulMBLen(0)
{
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::GetCurPart (const UINT32 ulMaxHdrsNum, const UINT32 ulPartID, CRawRFC2046MsgPart* &pPart)
{
	EXC_TYPE	exc=m_msgParts.AddPart(ulMaxHdrsNum, pPart);
	if (exc != EOK)
		return exc;

	pPart->m_ulPartID = ulPartID;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessMsgContentType (LPCTSTR lpszHdrValue)
{
	// means already have the boundary
	if ((!IsEmptyStr(m_lpszMIMEBoundary)) || (m_ulMBLen != 0))
		return EOK;

	LPCTSTR	lpszMBPos=NULL;
	EXC_TYPE	exc=RFC822FindMIMEBoundary(lpszHdrValue, &lpszMBPos, &m_ulMBLen);
	if (exc != EOK)	// if error returned, then assume this line does not contain the boundary
		return EOK;

	if ((exc=::strupdatebuf(lpszMBPos, m_ulMBLen, m_lpszMIMEBoundary)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessMsgEnvelope (IRawRFC2046MsgInputStream&	ims,
																		  const UINT32						ulMaxEnvHdrsNum,
																		  LPTSTR								lpszWorkBuf,
																		  const UINT32						ulMaxBufLen)
{
	CRawRFC2046MsgPart	*pEnvPart=NULL;
	EXC_TYPE					exc=GetCurPart(ulMaxEnvHdrsNum, RFC2046_ENVELOPE_RAW_MSG_PART_ID, pEnvPart);
	if (exc != EOK)
		return exc;
	CRawRFC2046MsgPart&	envPart=(*pEnvPart);
	CRFC822HdrsTbl&		envHdrs=(CRFC822HdrsTbl &) envPart.GetPartHdrs();

	CRFC822HdrParser	hdrParser;
	UINT32				ulReadLen=0, ulCurOffset=0;
	for (exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen); EOK == exc; exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen))
	{
		// in case, the line we read is the blank line signalling the end of the envelope
		envPart.m_ulHdrsEndOffset = ulCurOffset;

		if ((exc=ims.GetCurReadOffset(ulCurOffset)) != EOK)
			return exc;

		if (0 == ulReadLen)
			break;

		if ((exc=hdrParser.ProcessHdr(lpszWorkBuf)) != EOK)
			return exc;

		// check if this is the "Content-Type:" header and use it to extract the MIME boundary (if any)
		LPCTSTR	lpszHdrName=hdrParser.GetHdrName();
		if (0 == ::_tcsicmp(lpszHdrName, pszStdContentTypeHdr))
		{
			if ((exc=ProcessMsgContentType(hdrParser.GetHdrValue())) != EOK)
				return exc;
		}

		// check if this header is required
		LPVOID	pVal=NULL;
		if (EOK == (exc=m_hdrsMap.FindKey(lpszHdrName, pVal)))
		{
			if ((exc=::AddHdrParserValue(envHdrs, hdrParser)) != EOK)
				return exc;
		}
	}

	// we do not expect EEOF at this stage
	if (exc != EOK)
		return exc;

	// envelope headers always start at zero position
	envPart.m_ulHdrsStartOffset = 0;

	// signal the fact that this "part" has no data
	envPart.m_ulDataStartOffset = envPart.m_ulHdrsEndOffset;
	envPart.m_ulDataEndOffset = envPart.m_ulDataStartOffset;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessDirectAttachMsg (IRawRFC2046MsgInputStream&	ims,
																				LPTSTR							lpszWorkBuf,
																				const UINT32					ulMaxBufLen)
{
	CRawRFC2046MsgPart	*pEnvPart=m_msgParts.GetPart(RFC2046_ENVELOPE_RAW_MSG_PART_ID);
#ifdef _DEBUG
	// should not happen
	if (NULL == pEnvPart)
		return ECHECKEXCEPTION;
#endif
	const CRFC822HdrsTbl&	envHdrs=pEnvPart->GetPartHdrs();
	CRawRFC2046MsgPart		*pdaPart=NULL;
	EXC_TYPE						exc=GetCurPart(envHdrs.GetSize(), (RFC2046_ENVELOPE_RAW_MSG_PART_ID+1), pdaPart);
	if (exc != EOK)
		return exc;
	CRawRFC2046MsgPart&	daPart=(*pdaPart);

	daPart.m_ulHdrsStartOffset = pEnvPart->m_ulHdrsStartOffset;
	daPart.m_ulHdrsEndOffset = pEnvPart->m_ulHdrsEndOffset;
	if ((exc=ims.GetCurReadOffset(daPart.m_ulDataStartOffset)) != EOK)
		return exc;

	// skip data till end of stream
	UINT32	ulReadLen=0;
	for (exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen); EOK == exc; exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen))
	{
		if ((exc=ims.GetCurReadOffset(daPart.m_ulDataEndOffset)) != EOK)
			return exc;
	}

	// make sure we stopped because end of data
	if (exc != EEOF)
		return exc;

	if ((exc=daPart.UpdatePartHdrs(*pEnvPart)) != EOK)
		return exc;

	return EEOF;	// signal end of data stream
}

/*---------------------------------------------------------------------------*/

// Note: returns EEOF for last boundary
EXC_TYPE CRawRFC2046MsgDescBuilder::IsMIMEBoundary (LPCTSTR lpszWorkBuf, const UINT32 ulReadLen, BOOLEAN& fIsBoundary) const
{
	fIsBoundary = FALSE;

	// check if length exactly like the boundary (or like the signal of boundary end)
	if ((ulReadLen != (m_ulMBLen + RFC822_MIME_BOUNDARY_DELIMS_LEN)) &&
		 (ulReadLen != (m_ulMBLen + RFC822_MIME_BOUNDARY_DELIMS_LEN + RFC822_MIME_BOUNDARY_DELIMS_LEN)))
		return EOK;

	// make sure it starts with a boundary delimiter
	if (::_tcsncmp(lpszWorkBuf, pszMIMEBoundaryDelims, RFC822_MIME_BOUNDARY_DELIMS_LEN) != 0)
		return EOK;

	// check that it continues like the boundary
	if (::_tcsncmp(m_lpszMIMEBoundary, (lpszWorkBuf + RFC822_MIME_BOUNDARY_DELIMS_LEN), m_ulMBLen) != 0)
		return EOK;
	fIsBoundary = TRUE;

	// if found end of message boundary, then simulate an EOF
	if ((m_ulMBLen + RFC822_MIME_BOUNDARY_DELIMS_LEN + RFC822_MIME_BOUNDARY_DELIMS_LEN) == ulReadLen)
	{
		if (0 == ::_tcscmp(pszMIMEBoundaryDelims, (lpszWorkBuf + m_ulMBLen + RFC822_MIME_BOUNDARY_DELIMS_LEN)))
			return EEOF;

		// if this not a correct boundary end, then it is not a boundary at all
		fIsBoundary = FALSE;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::SkipToNextMIMEBoundary (IRawRFC2046MsgInputStream& ims,
																				LPTSTR							lpszWorkBuf,
																				const UINT32					ulMaxBufLen)
{
#ifdef _DEBUG
	// should not happen
	if (IsEmptyStr(m_lpszMIMEBoundary) || (0 == m_ulMBLen))
		return ESTATE;
#endif

	UINT32		ulReadLen=0;
	EXC_TYPE	exc=EOK;
	for (exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen); EOK == exc; exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen))
	{
		BOOLEAN	fIsBoundary=FALSE;
		if ((exc=IsMIMEBoundary(lpszWorkBuf, ulReadLen, fIsBoundary)) != EOK)
			return exc;

		if (fIsBoundary)
			return EOK;
	}

	return exc;
}

/*---------------------------------------------------------------------------*/

// Note: assumes stream is positioned on first line AFTER the MIME boundary
EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessMsgPart (const UINT32						ulPartID,
																	 IRawRFC2046MsgInputStream&	ims,
																	 const UINT32						ulMaxPartHdrsNum,
																	 LPTSTR								lpszWorkBuf,
																	 const UINT32						ulMaxBufLen)
{
	CRawRFC2046MsgPart	*pmpPart=NULL;
	EXC_TYPE					exc=GetCurPart(ulMaxPartHdrsNum, ulPartID, pmpPart);
	if (exc != EOK)
		return exc;
	CRawRFC2046MsgPart&	mpPart=(*pmpPart);
	CRFC822HdrsTbl&		mpHdrs=(CRFC822HdrsTbl &) mpPart.GetPartHdrs();

	UINT32	ulPrvOffset=0, ulReadLen=0, ulLdx=0;
	if ((exc=ims.GetCurReadOffset(ulPrvOffset)) != EOK)
		return exc;

	CRFC822HdrParser	hdrParser;
	BOOLEAN				fParsingHdrs=FALSE;
	for (exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen); EOK == exc; ulLdx++, exc=ims.ReadLine(lpszWorkBuf, ulMaxBufLen, ulReadLen))
	{
		UINT32	ulCurOffset=0;
		if ((exc=ims.GetCurReadOffset(ulCurOffset)) != EOK)
			return exc;

		if (0 == ulLdx)
		{
			// if first line is a header line, then assume parsing headers
			if (EOK == (exc=hdrParser.ProcessHdr(lpszWorkBuf)))
			{
				fParsingHdrs = TRUE;
				mpPart.m_ulHdrsStartOffset = ulPrvOffset;
			}
			else
			{
				// if first line is empty, then assume this is the separator between the boundary and the data
				if (0 == ulReadLen)
					mpPart.m_ulDataStartOffset = ulCurOffset;
				else
					mpPart.m_ulDataStartOffset = ulPrvOffset;

				// mark the fact that there are no headers
				mpPart.m_ulHdrsStartOffset = mpPart.m_ulDataStartOffset;
				mpPart.m_ulHdrsEndOffset = mpPart.m_ulHdrsStartOffset;
			}
		}

		// handle blank line
		if (0 == ulReadLen)
		{
			if (fParsingHdrs)
			{
				// first blank line signals end of headers
				fParsingHdrs = FALSE;

				// the end of the headers is the offset BEFORE the blank line
				mpPart.m_ulHdrsEndOffset = ulPrvOffset;

				// the start of the data is the offset AFTER the blank line
				mpPart.m_ulDataStartOffset = ulCurOffset;
				mpPart.m_ulDataEndOffset = ulCurOffset;
			}
			else	// we assign this value in case the next non-blank line is a MIME boundary
				mpPart.m_ulDataEndOffset = ulPrvOffset;
		}
		else // non-blank line
		{
			if (fParsingHdrs)
			{
				if ((exc=hdrParser.ProcessHdr(lpszWorkBuf)) != EOK)
					return exc;
				if ((exc=::AddHdrParserValue(mpHdrs, hdrParser)) != EOK)
					return exc;

				if ((exc=::UpdatePartName(mpPart, hdrParser.GetHdrName(), mpHdrs)) != EOK)
					return exc;

				// track the end of the headers (which is at least AFTER the currently parsed one)
				mpPart.m_ulHdrsEndOffset = ulCurOffset;
			}
			else	// check if this is another MIME boundary
			{
				BOOLEAN	fIsBoundary=FALSE;

				if ((exc=IsMIMEBoundary(lpszWorkBuf, ulReadLen, fIsBoundary)) != EOK)
					return exc;

				if (fIsBoundary)
					break;

				// since this is NOT a MIME boundary, track it as a possible end of data (at least AFTER this line)
				mpPart.m_ulDataEndOffset = ulCurOffset;
			}
		}

		ulPrvOffset = ulCurOffset;
	}

	if (exc != EEOF)
		return exc;

	// we expect to stop because of last MIME boundary and not because of running out of data
	return EJOBPARAM;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessMsgParts (IRawRFC2046MsgInputStream&	ims,
																	  const UINT32						ulMaxPartHdrsNum,
																	  LPTSTR								lpszWorkBuf,
																	  const UINT32						ulMaxBufLen)
{
	if (IsEmptyStr(m_lpszMIMEBoundary))
		return ProcessDirectAttachMsg(ims, lpszWorkBuf, ulMaxBufLen);

	EXC_TYPE	exc=SkipToNextMIMEBoundary(ims, lpszWorkBuf, ulMaxBufLen);
	if (exc != EOK)
		return exc;

	for (UINT32	ulPartID=(RFC2046_ENVELOPE_RAW_MSG_PART_ID+1); ; ulPartID++)
	{
		if ((exc=ProcessMsgPart(ulPartID, ims, ulMaxPartHdrsNum, lpszWorkBuf, ulMaxBufLen)) != EOK)
			return exc;
	}

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CRawRFC2046MsgDescBuilder::ProcessInputStream (IRawRFC2046MsgInputStream&	ims,
																		  const UINT32						ulMaxEnvHdrsNum,
																		  const UINT32						ulMaxPartHdrsNum,
																		  LPTSTR								lpszWorkBuf,
																		  const UINT32						ulMaxBufLen)
{
	const UINT32	ulEnvHdrsNum=((0 == ulMaxEnvHdrsNum) ? m_hdrsMap.GetSize() : ulMaxEnvHdrsNum);
	if ((0 == ulEnvHdrsNum) || (0 == ulMaxPartHdrsNum) || (NULL == lpszWorkBuf) || (ulMaxBufLen < (BASE64_MAX_LINE_LEN+2)))
		return EPARAM;

	// we expect an empty collection of message parts
	if (m_msgParts.GetNumOfParts() != 0)
		return ESTATE;

	EXC_TYPE	exc=ProcessMsgEnvelope(ims, ulEnvHdrsNum, lpszWorkBuf, ulMaxBufLen);
	if (exc != EOK)
		return exc;

	// we expect to stop only due to end of data
	if ((exc=ProcessMsgParts(ims, ulMaxPartHdrsNum, lpszWorkBuf, ulMaxBufLen)) != EEOF)
	{
		if (EOK == exc)
			return ENDPERROR;
		else
			return exc;
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE BuildRFC2046MsgDescription (IRawRFC2046MsgInputStream&		ims,
												 CRawRFC2046MsgPartsCollection&	msgParts,
												 const CStr2PtrMapper&				hdrsMap, // keys are names of envelope headers to be cached
												 const UINT32							ulMaxEnvHdrsNum, // 0 == same as size of hdrsMap
												 const UINT32							ulMaxPartHdrsNum,
												 const UINT32							ulWorkBufLen,
												 LPTSTR									lpszWorkBuf)
{
	LPTSTR	lpszLineBuf=lpszWorkBuf;

	if (ulWorkBufLen < (BASE64_MAX_LINE_LEN+2))	// according to RFC2822
		return EBADBUFF;

	LPTSTR			lpszAllocBuf=NULL;
	CStrBufGuard	abg(lpszAllocBuf);
	if (NULL == lpszLineBuf)
	{
		if (NULL == (lpszAllocBuf=new TCHAR[ulWorkBufLen+2]))
			return EMEM;

		lpszLineBuf = lpszAllocBuf;
	}

	CRawRFC2046MsgDescBuilder	mdb(hdrsMap, msgParts);
	EXC_TYPE							exc=mdb.ProcessInputStream(ims, ulMaxEnvHdrsNum, ulMaxPartHdrsNum, lpszLineBuf, ulWorkBufLen);
	if (exc != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
