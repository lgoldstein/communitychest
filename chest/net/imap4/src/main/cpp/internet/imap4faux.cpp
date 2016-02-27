#include <internet/imap4Lib.h>
#include <futils/general.h>

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgSync (ISockioInterface&	SBSock,
									 LPCTSTR					lpszITag,			// NULL == auto-generate
									 const BOOLEAN			fIsUIDFetch,
									 const UINT32			ulMsgID,
									 LPCTSTR					lpszModifiers[],	// last member must be NULL
									 IMAP4_FETCHRSP_CFN	lpfnHcfn,
									 LPVOID					pArg,
									 const UINT32			ulRspTimeout)
{
	TCHAR	szMsgID[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	dword_to_argument(ulMsgID, szMsgID);
	return imap4FetchMsgRangeSync(SBSock, lpszITag, fIsUIDFetch, szMsgID, lpszModifiers, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgBodySync (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,			// NULL == auto-generate
										  const BOOLEAN		fIsUIDFetch,
										  const UINT32			ulMsgID,
										  IMAP4_FETCHRSP_CFN	lpfnHcfn,
										  LPVOID					pArg,
										  const UINT32			ulRspTimeout)
{
	static LPCTSTR lpszBodyFetchModifier[]={ _T("BODY.PEEK[]"), NULL };
	return imap4FetchMsgSync(SBSock, lpszITag, fIsUIDFetch, ulMsgID, lpszBodyFetchModifier, lpfnHcfn, pArg, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	LPIMAP4FASTMSGINFO	pFI;
	UINT32					ulMaxFI;
	UINT32					ulCurFI;
	UINT32					ulLastID;
	BOOL						fFetchedSomething;
} FIFARGS, *LPFIFARGS;

static EXC_TYPE fastInfoCfn (const CIMAP4FetchCfnData&	ftchData,
									  LPCTSTR							lpszFetchRsp,
									  LPCTSTR							lpszSubHdr,		// valid only for complex structures
									  LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
									  LPVOID								lpModVal)		// actual type depends on modifier
{
	LPFIFARGS	pFIFA=(LPFIFARGS) ftchData.GetCallbackArg();
	if (NULL == pFIFA)
		return EPARAM;
	UINT32&		ulCurFI=pFIFA->ulCurFI;
	UINT32&		ulLastID=pFIFA->ulLastID;

	LPIMAP4FASTMSGINFO	pFI=pFIFA->pFI;
	if ((NULL == pFI) || (0 == pFIFA->ulMaxFI))
		return ERANGE;

	UINT32	ulCurSeq=ftchData.GetMsgSeqNo();
	if ((ulCurSeq != ulLastID) && (ulLastID != 0))
	{
		// make sure have a valid UID for previous message before going to next one
		IMAP4FASTMSGINFO&	prevInfo=pFI[ulCurFI];
		if (0 == prevInfo.ulMsgUID)
			return ESTATE;

		ulCurFI++;
		if (ulCurFI >= pFIFA->ulMaxFI)
			return EOVERFLOW;
	}

	IMAP4FASTMSGINFO&	fmInfo=pFI[ulCurFI];
	fmInfo.ulMsgSeqNo = ulCurSeq;

	if (0 == _tcsicmp(lpszFetchRsp, IMAP4_UID))
		fmInfo.ulMsgUID = (UINT32) lpModVal;
	else if (0 == _tcsicmp(lpszFetchRsp, IMAP4_RFC822SIZE))
		fmInfo.ulMsgSize = (UINT32) lpModVal;
	else if (0 == _tcsicmp(lpszFetchRsp, IMAP4_INTERNALDATE))
	{
		EXC_TYPE	exc=DecodeIMAP4InternalDate((LPCTSTR) lpModVal, &fmInfo.iDate, &fmInfo.tmZone);
		if (exc != EOK)
			return exc;
	}
	else if (0 == _tcsicmp(lpszFetchRsp, IMAP4_FLAGS))
	{
		const LPIMAP4MSGFLAGS	pFlags=(LPIMAP4MSGFLAGS) lpModVal;
		if (NULL == pFlags)
			return EEMPTYENTRY;
		fmInfo.flags = *pFlags;
	}
	else if (0 == _tcsicmp(lpszFetchRsp, IMAP4_SILENT))
	{
		// ignore the value - only check its validity
		LPCTSTR			lpszFlag=GetSafeStrPtr(lpszSubHdr);
		const UINT32	ulFlgLen=(UINT32) lpModVal;
		if (0 == ulFlgLen)
			return EEMPTYENTRY;
	}
	else
		return EILLEGALOPCODE;

	pFIFA->fFetchedSomething = TRUE;
	ulLastID = ulCurSeq;
	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgRangeFastInfo (ISockioInterface&	SBSock,
												 LPCTSTR					lpszITag,			// NULL == auto-generate
												 const BOOLEAN			fIsUIDFetch,
												 LPCTSTR					lpszMsgRange,		// NULL == "1:*"
												 IMAP4FASTMSGINFO		fmInfo[],
												 const UINT32			ulMaxInfos,
												 UINT32&					ulInfosNum,			// (OUT) actually fetched
												 const UINT32			ulRspTimeout)
{
	static LPCTSTR lpszFastFetchModifiers[]={
		IMAP4_UID,
		IMAP4_FLAGS,
		IMAP4_INTERNALDATE,
		IMAP4_RFC822SIZE,
		NULL	// mark end
	};

	ulInfosNum = 0;

	if ((NULL == fmInfo) || (0 == ulMaxInfos))
		return ERANGE;
	memset(fmInfo, 0, (ulMaxInfos * (sizeof fmInfo[0])));

	FIFARGS	fifa={ fmInfo, ulMaxInfos, 0, 0, FALSE };
	EXC_TYPE	exc=imap4FetchMsgRangeSync(SBSock, lpszITag, fIsUIDFetch, lpszMsgRange, lpszFastFetchModifiers,
													fastInfoCfn, (LPVOID) &fifa, ulRspTimeout);
	if (exc != EOK)
		return exc;

	// need to add 1 since last message does not cause "current FI" increment
	if (fifa.fFetchedSomething)
		ulInfosNum = (fifa.ulCurFI + 1);

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgFastInfo (ISockioInterface&	SBSock,
										  LPCTSTR				lpszITag,			// NULL == auto-generate
										  const BOOLEAN		fIsUIDFetch,
										  const UINT32			ulMsgId,
										  IMAP4FASTMSGINFO&	fmInfo,
										  const UINT32			ulRspTimeout)
{
	TCHAR		szMsgID[MAX_DWORD_DISPLAY_LENGTH+2];
	dword_to_argument(ulMsgId, szMsgID);
	UINT32	ulFMNum=0;

	return imap4FetchMsgRangeFastInfo(SBSock, lpszITag, fIsUIDFetch, szMsgID, &fmInfo, 1, ulFMNum, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4ClearMsgsInfo (CVSDCollection&	msgsInfo)	// each member is a LPIMAP4FULLMSGINFO
{
	for (UINT32	ulIdx=0; ulIdx < msgsInfo.GetSize(); ulIdx++)
	{
		LPIMAP4FULLMSGINFO	pInfo=(LPIMAP4FULLMSGINFO) msgsInfo[ulIdx];
		if (pInfo != NULL)
			delete pInfo;
	}

	msgsInfo.Reset();
	return EOK;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE fmriCfn (const CIMAP4FetchCfnData&	ftchData,
								 LPCTSTR							lpszFetchRsp,
								 LPCTSTR							lpszSubHdr,		// valid only for complex structures
								 LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
								 LPVOID							lpModVal)		// actual type depends on modifier
{
	LPVOID	pArg=ftchData.GetCallbackArg();
	if (NULL == pArg)
		return ECONTEXT;
	CVSDCollection&	msgsInfo=*((CVSDCollection *) pArg);

	LPIMAP4FULLMSGINFO	pInfo=NULL;
	UINT32					ulMsgsNum=msgsInfo.GetSize();
	// check if switched to a new message
	if (ulMsgsNum > 0)
	{
		// should not happen
		if (NULL == (pInfo=(LPIMAP4FULLMSGINFO) msgsInfo[ulMsgsNum-1]))
			return EFATALEXIT;

		if (pInfo->GetMsgSeqNo() != ftchData.GetMsgSeqNo())
			pInfo = NULL;	// force a new allocation
	}

	if (NULL == pInfo)
	{
		if (NULL == (pInfo=new CIMAP4FullMsgInfo))
			return EMEM;

		EXC_TYPE	exc=msgsInfo.AddItem((LPVOID) pInfo);
		if (exc != EOK)
		{
			delete pInfo;
			return exc;
		}
	}

	return pInfo->HandleFetchResponse(ftchData, lpszFetchRsp, lpszSubHdr, lpszKeyword, lpModVal);
}

/*---------------------------------------------------------------------------*/

static LPCTSTR fmriMods[]={
	IMAP4_UID,	// Must be 1st !!!
	IMAP4_FLAGS,
	IMAP4_INTERNALDATE,
	IMAP4_RFC822SIZE,
	IMAP4_ENVELOPE,
	IMAP4_BODY,
	NULL
};	// mark end

/*---------------------------------------------------------------------------*/

static EXC_TYPE fmsCfn (const CIMAP4FetchCfnData&	ftchData,
								LPCTSTR							lpszFetchRsp,
								LPCTSTR							lpszSubHdr,		// valid only for complex structures
								LPCTSTR							lpszKeyword,	// non-NULL for "keyword=value" pair(s)
								LPVOID							lpModVal)		// actual type depends on modifier
{
	LPIMAP4FULLMSGINFO	pMI=(LPIMAP4FULLMSGINFO) ftchData.GetCallbackArg();
	if (NULL == pMI)
		return ECONTEXT;

	return pMI->HandleFetchResponse(ftchData, lpszFetchRsp, lpszSubHdr, lpszKeyword, lpModVal);
}

EXC_TYPE imap4FetchMsgFullInfoSync (ISockioInterface&		SBSock,
												LPCTSTR					lpszITag,			// NULL == auto-generate
												const BOOLEAN			fIsUIDFetch,
												const UINT32			ulMsgID,
												CIMAP4FullMsgInfo&	msgInfo,
												const UINT32			ulRspTimeout)
{
	msgInfo.Reset();

	LPCTSTR	*lppMods=(fIsUIDFetch ? &fmriMods[1] : fmriMods);
	return imap4FetchMsgSync(SBSock, lpszITag, fIsUIDFetch, ulMsgID, lppMods, fmsCfn, (LPVOID) &msgInfo, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

// Note(s):
//
//	a. clears the data collection before fetching the items
// b. caller must delete allocated msgs info in data collection
EXC_TYPE imap4FetchMsgRangeInfo (ISockioInterface&	SBSock,
											LPCTSTR				lpszITag,			// NULL == auto-generate
											const BOOLEAN		fIsUIDFetch,
											LPCTSTR				lpszMsgRange,
											LPCTSTR				lpszModifiers[],	// (NULL == full info) last member must be NULL
											CVSDCollection&	msgsInfo,			// each member is a LPIMAP4FULLMSGINFO
											const UINT32		ulRspTimeout)
{
	LPCTSTR	*lppMods=lpszModifiers;
	if (NULL == lppMods)
		lppMods = (fIsUIDFetch ? &fmriMods[1] : fmriMods);

	EXC_TYPE	exc=imap4ClearMsgsInfo(msgsInfo);
	if (exc != EOK)
		return exc;

	return imap4FetchMsgRangeSync(SBSock, lpszITag, fIsUIDFetch, lpszMsgRange,	lppMods, fmriCfn, (LPVOID) &msgsInfo, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

static const IMAP4_MSGFLAGS	delFlags={ 0, 0, 0, 1, 0, 0, 0 };

EXC_TYPE imap4DeleteMsgsSync (ISockioInterface&	SBSock,
										LPCTSTR				lpszITag,	// NULL == auto-generate
										LPCTSTR				lpszMsgSet,
										const BOOLEAN		fIsUID,
										const BOOLEAN		fSilent,
										IMAP4_STORE_HCFN	lpfnHcfn,	// may be NULL
										LPVOID				pArg,
										const UINT32		ulRspTimeout)
{
	IMAP4STORECMDFLAGS	delMode={ IMAP4STOREADDMODE, fIsUID, fSilent };

	return imap4StoreMsgsSync(SBSock, lpszITag, lpszMsgSet, delMode, delFlags, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4DeleteRangeSync (ISockioInterface&	SBSock,
										 LPCTSTR					lpszITag,	// NULL == auto-generate
										 const UINT32			ulStartID,
										 const UINT32			ulEndID,
										 const BOOLEAN			fIsUID,
										 const BOOLEAN			fSilent,
										 IMAP4_STORE_HCFN		lpfnHcfn,	// may be NULL
										 LPVOID					pArg,
										 const UINT32			ulRspTimeout)
{
	TCHAR		szMsgRange[(2*MAX_DWORD_DISPLAY_LENGTH)+4]=_T("");
	UINT32	rLen=dword_to_argument(ulStartID, szMsgRange);

	if (ulStartID != ulEndID)
	{
		szMsgRange[rLen] = IMAP4_MSGRANGE_DELIM;
		dword_to_argument(ulEndID, &szMsgRange[rLen+1]);
	}

	return imap4DeleteMsgsSync(SBSock, lpszITag, szMsgRange, fIsUID, fSilent, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4CopyMsgSync (ISockioInterface&	SBSock,
									LPCTSTR				lpszITag,	// NULL == auto-generate
									const BOOLEAN		fIsUID,
									const UINT32		ulMsgID,
									LPCTSTR				lpszDstFolder,
									const UINT32		ulRspTimeout)
{
	TCHAR	szMsgID[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	dword_to_argument(ulMsgID, szMsgID);

	return imap4CopyMsgsSync(SBSock, lpszITag, fIsUID, szMsgID, lpszDstFolder, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4CopyRangeSync (ISockioInterface&	SBSock,
									  LPCTSTR				lpszITag,	// NULL == auto-generate
									  const BOOLEAN		fIsUID,
									  const UINT32			ulStartID,
									  const UINT32			ulEndID,
									  LPCTSTR				lpszDstFolder,
									  const UINT32			ulRspTimeout)
{
	UINT32	ulEncLen=0UL;
	TCHAR		szMsgSet[MAX_MSGSET_RANGE_ENCLEN+2]=_T("");
	EXC_TYPE	exc=imap4CreateMsgRange(ulStartID, ulEndID, szMsgSet, MAX_MSGSET_RANGE_ENCLEN, &ulEncLen);
	if (EOK == exc)
		exc = imap4CopyMsgsSync(SBSock, lpszITag, fIsUID, szMsgSet, lpszDstFolder, ulRspTimeout);

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4StoreMsgSync (ISockioInterface&			SBSock,
									 LPCTSTR							lpszITag,	// NULL == auto-generate
									 const UINT32					ulMsgID,
									 const IMAP4STORECMDFLAGS&	cmdMode,
									 const IMAP4_MSGFLAGS&		flags,
									 IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
									 LPVOID							pArg,
									 const UINT32					ulRspTimeout)
{
	TCHAR	szMsgID[MAX_DWORD_DISPLAY_LENGTH+2]=_T("");
	dword_to_argument(ulMsgID, szMsgID);
	return imap4StoreMsgsSync(SBSock, lpszITag, szMsgID, cmdMode, flags, lpfnHcfn, pArg, ulRspTimeout);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4StoreRangeSync (ISockioInterface&				SBSock,
										LPCTSTR							lpszITag,	// NULL == auto-generate
										const UINT32					ulStartID,
										const UINT32					ulEndID,
										const IMAP4STORECMDFLAGS&	cmdMode,
										const IMAP4_MSGFLAGS&		flags,
										IMAP4_STORE_HCFN				lpfnHcfn,	// may be NULL
										LPVOID							pArg,
										const UINT32					ulRspTimeout)
{
	UINT32	ulEncLen=0UL;
	TCHAR		szMsgSet[MAX_MSGSET_RANGE_ENCLEN+2]=_T("");
	EXC_TYPE	exc=imap4CreateMsgRange(ulStartID, ulEndID, szMsgSet, MAX_MSGSET_RANGE_ENCLEN, &ulEncLen);
	if (EOK == exc)
		exc = imap4StoreMsgsSync(SBSock, lpszITag, szMsgSet, cmdMode, flags, lpfnHcfn, pArg, ulRspTimeout);

	return exc;
}

//////////////////////////////////////////////////////////////////////////////

// also default constructor
CIMAP4FoldersPropsSet::CIMAP4FoldersPropsSet  (const UINT32 ulInitialSize, const UINT32 ulGrowSize)
	: m_FoldersMap((ulInitialSize + ulGrowSize), TRUE), m_FoldersProps(ulInitialSize, ulGrowSize)
{
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FoldersPropsSet::FindFolderProps (LPCTSTR lpszFolder, LPIMAP4FOLDERPROPS& pProps) const
{
	pProps = NULL;

	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_FoldersMap.FindKey(lpszFolder, pV);
	if (EOK == exc)
		pProps = (LPIMAP4FOLDERPROPS) pV;

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FoldersPropsSet::AddFolder (LPCTSTR						lpszFolder,
														 const TCHAR				chSep,
														 const IMAP4_FLDRFLAGS&	flags)
{
	IMAP4_FOLDERPROPS	fprops;
	memset(&fprops, 0, (sizeof fprops));
	fprops.lpszFolder = lpszFolder;
	fprops.chSep = chSep;
	fprops.flags = flags;

	return AddFolder(fprops);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FoldersPropsSet::AddFolder (const IMAP4_FOLDERPROPS& fprops)
{
	EXC_TYPE	exc=EOK;
	UINT32	ulFdx=m_FoldersProps.GetSize();
	if ((exc=m_FoldersProps.AddItem((LPVOID) &fprops, (sizeof fprops))) != EOK)
		return exc;

	// update the data
	LPVOID	pV=NULL;
	if ((exc=m_FoldersProps.GetData(ulFdx, pV)) != EOK)
		return exc;

	LPCTSTR	lpszFolder=fprops.lpszFolder;
	if ((exc=m_FoldersMap.AddKey(lpszFolder, pV)) != EOK)
		return exc;

	IMAP4_FOLDERPROPS&	uprops=*((LPIMAP4FOLDERPROPS) pV);
	if ((exc=m_FoldersMap.GetKey(lpszFolder, uprops.lpszFolder)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CIMAP4FoldersPropsSet::EnumFoldersProps (IMAP4_FPSET_ENUM_CFN lpfnEcfn, LPVOID pArg) const
{
	if (NULL == lpfnEcfn)
		return EBADADDR;

	BOOLEAN	fContEnum=TRUE;
	for (UINT32	ulIdx=0; fContEnum && (ulIdx < m_FoldersProps.GetSize()); ulIdx++)
	{
		LPIMAP4FOLDERPROPS	pProps=(LPIMAP4FOLDERPROPS) m_FoldersProps[ulIdx];
		if (NULL == pProps)
			return ECONTEXT;

		EXC_TYPE	exc=(*lpfnEcfn)(*pProps, pArg, fContEnum);
		if (exc != EOK)
			return exc;
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

static EXC_TYPE gfpCfn (ISockioInterface&			SBSock,
								LPCTSTR						lpszFolder,
								const TCHAR					chSep,
								const IMAP4_FLDRFLAGS&	flags,
								LPVOID						pArg,
								BOOLEAN&						fContEnum)
{
	fContEnum = TRUE;

	if (NULL == pArg)
		return ECONTEXT;

	CIMAP4FoldersPropsSet&	fpSet=*((CIMAP4FoldersPropsSet *) pArg);
	return fpSet.AddFolder(lpszFolder, chSep, flags);
}

EXC_TYPE imap4GetFoldersProps (ISockioInterface&		SBSock,
										 LPCTSTR						lpszITag,	// NULL == auto generate
										 LPCTSTR						lpszRef,
										 LPCTSTR						lpszMbox,
										 CIMAP4FoldersPropsSet&	fpSet,
										 const UINT32				ulRspTimeout)
{
	fpSet.Reset();

	return imap4ListSync(SBSock, lpszITag, lpszRef, lpszMbox, gfpCfn, (LPVOID) &fpSet, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

typedef struct {
	UINT32							ulMsgID;
	BOOLEAN							fIsUIDFetch;
	IMAP4_MSGPART_FETCH_CFN		lpfnFcfn;
	LPVOID							pArg;
} IMAP4MPFARGS;

static EXC_TYPE mpfCfn (const CIMAP4FetchCfnData&	ftchData,
								LPCTSTR							lpszModifier,
								LPCTSTR							lpszPartID,
								LPCTSTR							lpszKeyword,
								LPVOID							lpModVal)
{
	LPVOID	pArg=ftchData.GetCallbackArg();
	if (NULL == pArg)
		return ECONTEXT;
	IMAP4MPFARGS&	mpfa=*((IMAP4MPFARGS *) pArg);

	// skip non "BODY[xxx]" responses
	LPCTSTR	lpszFetchRsp=ftchData.GetModifier();
	if (_tcsicmp(lpszModifier, IMAP4_BODY) != 0)
		return EOK;

	const IMAP4_BODYPART_CASE	ePart=imap4XlateBodyPart(lpszPartID);
	LPCTSTR	lpszPartEnd=NULL;
	if (!fIsBadIMAP4BodyPart(ePart))
	{
		if (NULL == _tcsrchr(lpszPartID, IMAP4_BODYPART_DELIM))
			return EILLVOL;
	}

	LPCTSTR	lpszData=(LPCTSTR) lpModVal;
	if (IsEmptyStr(lpszData))
		return EOK;

	IMAP4_MSGPART_FETCH_CFN	lpfnFcfn=mpfa.lpfnFcfn;
	if (NULL == lpfnFcfn)
		return EBADADDR;

	if (lpszPartEnd != NULL)
		*((LPTSTR) lpszPartEnd) = _T('\0');
	EXC_TYPE	exc=(*lpfnFcfn)(mpfa.ulMsgID, mpfa.fIsUIDFetch, ePart, lpszPartID, lpszData, _tcslen(lpszData), mpfa.pArg);
	if (lpszPartEnd != NULL)
		*((LPTSTR) lpszPartEnd) = IMAP4_BODYPART_DELIM;

	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE imap4FetchMsgPartsData (ISockioInterface&				SBSock,
											LPCTSTR							lpszITag,			// NULL == auto-generate
											const UINT32					ulMsgID,
											const BOOLEAN					fIsUIDFetch,
											const IMAP4_BODYPART_CASE	eBodyPart,
											LPCTSTR							lpszMsgParts[],	// last == NULL
											IMAP4_MSGPART_FETCH_CFN		lpfnFcfn,
											LPVOID							pArg,
											const UINT32					ulRspTimeout)
{
	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+2]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag, lpszBodyPart=imap4GetBodyPart(eBodyPart);
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4FetchCmd, fIsUIDFetch, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	if ((exc=strb.AddNum(ulMsgID)) != EOK)
		return exc;
	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	if ((exc=strb.AddChar(IMAP4_PARLIST_SDELIM)) != EOK)
		return exc;

	if ((NULL == lpszMsgParts) || (NULL == lpfnFcfn) ||
		 (0 == ulMsgID) || fIsBadIMAP4BodyPart(eBodyPart))
		return EPARAM;
	if ((NULL == lpszMsgParts[0]) || IsEmptyStr(lpszBodyPart))
		return EEMPTYENTRY;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	for (UINT32 ulPdx=0; ; ulPdx++)
	{
		LPCTSTR	lpszPartID=lpszMsgParts[ulPdx];
		if (IsEmptyStr(lpszPartID))
			break;

		if (0 == ulPdx)
			wLen = SBSock.WriteCmdf(_T("%s[%s.%s]"), IMAP4_BODYPEEK, lpszPartID, lpszBodyPart);
		else
			wLen = SBSock.WriteCmdf(_T(" %s[%s.%s]"), IMAP4_BODYPEEK, lpszPartID, lpszBodyPart);
		if (wLen <= 0)
			return ENOTCONNECTION;
	}

	if ((wLen=SBSock.WriteCmdf(_T("%c\r\n"), IMAP4_PARLIST_EDELIM)) <= 0)
		return ENOTCONNECTION;

	IMAP4MPFARGS	mpfa={ ulMsgID, fIsUIDFetch, lpfnFcfn, pArg };
	if ((exc=imap4HandleFetchRspSync(SBSock, lpszTag, mpfCfn, (LPVOID) &mpfa, ulRspTimeout)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/
