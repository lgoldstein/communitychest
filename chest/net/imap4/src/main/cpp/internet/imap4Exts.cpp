#include <internet/imap4Exts.h>

#ifdef __cplusplus
#define SZXTRN extern
#else
#define SZXTRN /* nothing */
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

// callback used to list server capabilities
static EXC_TYPE extCapsCfn (ISockioInterface&	SBSock,
									 LPCTSTR					lpszCapability,
									 LPVOID					pArg,
									 BOOLEAN&				fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;
	if (IsEmptyStr(lpszCapability))
		return EBADHEADER;

	IMAP4EXTCAPS&	extCaps=*((IMAP4EXTCAPS *) pArg);
	if (_tcsicmp(lpszCapability, IMAP4_IDLE_CAPABILITY) == 0)
		extCaps.m_fIdle = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_QUOTA_CAPABILITY) == 0)
		extCaps.m_fQuota = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_NAMESPACE_CAPABILITY) == 0)
		extCaps.m_fNamespace = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_UIDPLUS_CAPABILITY) == 0)
		extCaps.m_fUIDPlus = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_MBOXREFERRAL_CAPABILITY) == 0)
		extCaps.m_fMboxRefer = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_LOGINREFERRAL_CAPABILITY) == 0)
		extCaps.m_fLoginRefer = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_LITERALPLUS_CAPABILITY) == 0)
		extCaps.m_fLiteralPlus = 1;
	else if (_tcsicmp(lpszCapability, IMAP4_CHILDREN_CAPABILITY) == 0)
		extCaps.m_fChildren = 1;

	fContEnum = TRUE;
	return S_OK;
}

EXC_TYPE imap4ExtendedCapabilitiesSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 IMAP4EXTCAPS&			extCaps,
													 LPTSTR					lpszRspBuf,
													 const UINT32			ulMaxRspLen,
													 const UINT32			ulRspTimeout)
{
	memset(&extCaps, 0, (sizeof extCaps));
	return imap4CapabilitiesSync(SBSock, lpszITag, extCapsCfn, (LPVOID) &extCaps, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
}

EXC_TYPE imap4ExtendedCapabilitiesSync (ISockioInterface&	SBSock,
													 LPCTSTR					lpszITag,	// NULL == auto-generate
													 IMAP4EXTCAPS&			extCaps,	
													 const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4ExtendedCapabilitiesSync(SBSock, lpszITag, extCaps, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////

SZXTRN const TCHAR szIMAP4GetQuotaCmd[]=_T("GETQUOTA");
SZXTRN const TCHAR szIMAP4GetQuotaRootCmd[]=_T("GETQUOTAROOT");
SZXTRN const TCHAR szIMAP4SetQuotaCmd[]=_T("SETQUOTA");

SZXTRN const TCHAR szIMAP4QuotaRsp[]=_T("QUOTA");
SZXTRN const TCHAR szIMAP4QuotaRootRsp[]=_T("QUOTAROOT");

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szIMAP4QuotaStorageRes[]=_T("STORAGE");
SZXTRN const TCHAR szIMAP4QuotaMessageRes[]=_T("MESSAGE");

typedef struct {
	LPCTSTR							lpszQRes;
	IMAP4_QUOTARES_CASE_TYPE	eQRes;
} XL_QRS;

static const XL_QRS xlQrs[]={
	{	szIMAP4QuotaStorageRes,	IMAP4_QUOTASTORAGE_RESCASE	},
	{	szIMAP4QuotaMessageRes,	IMAP4_QUOTAMESSAGE_RESCASE	},
	{	NULL,							IMAP4_QUOTABAD_RESCASE		}	/* mark end */
};

IMAP4_QUOTARES_CASE_TYPE imap4XlateQuotaResCase (LPCTSTR lpszQuotaRes, const UINT32 ulQRLen)
{
	UINT32	i=0;

	if ((NULL == lpszQuotaRes) || (0 == ulQRLen))
		return IMAP4_QUOTABAD_RESCASE;

	for (i=0; ; i++)
	{
		const XL_QRS	*pXL=&xlQrs[i];
		LPCTSTR			lpszXQR=pXL->lpszQRes;
		UINT32			ulXQLen=GetSafeStrlen(lpszXQR);
		if (IsEmptyStr(lpszXQR))
			break;

		if ((ulXQLen != ulQRLen) || (_tcsnicmp(lpszQuotaRes, lpszXQR, ulQRLen) != 0))
			continue;

		return pXL->eQRes;
	}

	return IMAP4_QUOTABAD_RESCASE;
}

/*--------------------------------------------------------------------------*/

static EXC_TYPE SendIMAP4QuotaFolder (ISockioInterface&	SBSock,
												  IStrlBuilder&		strb,
												  LPTSTR					lpszCmd,
												  LPCTSTR				lpszQRFolder,
												  const UINT32			ulRspTimeout)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszQRFolder))
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;
		if ((exc=strb.AddChar(IMAP4_QUOTE_DELIM)) != EOK)
			return exc;

		return EOK;
	}

	if (EOK == (exc=SendIMAP4Folder(SBSock, lpszCmd, lpszQRFolder, ulRspTimeout)))
	{
		if ((exc=strb.AddChar(_T(' '))) != EOK)
			return exc;
		
		if ((exc=AddIMAP4Folder(strb, lpszQRFolder)) != EOK)
			return exc;
	}
	else	// have special characters in folder name
	{
		if (exc != ELITERAL)
			return exc;

		strb.Reset();
	}

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4QuotaResRspParser : public CIMAP4RspParser {
	private:
		// disable copy constructor and assignment operator
		CIMAP4QuotaResRspParser (const CIMAP4QuotaResRspParser& );
		CIMAP4QuotaResRspParser& operator= (const CIMAP4QuotaResRspParser& );

	protected:
		IMAP4_QUOTARES_ECFN_TYPE	m_lpfnQcfn;
		LPVOID							m_pArg;
		BOOLEAN							m_fContEnum;

		CIMAP4QuotaResRspParser (ISockioInterface&			SBSock,
										 LPCTSTR							lpszTag,
										 IMAP4_QUOTARES_ECFN_TYPE	lpfnQcfn,	// can be NULL
										 LPVOID							pArg,
										 LPTSTR							lpszRspBuf,
										 const UINT32					ulMaxRspLen,
										 const UINT32					ulRspTimeout)
			:	CIMAP4RspParser(SBSock, lpszTag, lpszRspBuf, ulMaxRspLen, ulRspTimeout),
				m_lpfnQcfn(lpfnQcfn), m_pArg(pArg), m_fContEnum(TRUE)
		{
		}

		virtual EXC_TYPE HandleQuotaResourcesList ();

		virtual ~CIMAP4QuotaResRspParser () { }
};

/*--------------------------------------------------------------------------*/

EXC_TYPE CIMAP4QuotaResRspParser::HandleQuotaResourcesList ()
{
	while (*m_lpszCurPos != IMAP4_PARLIST_EDELIM)
	{
		// get option name
		LPCTSTR	lpszOption=NULL;
		UINT32	ulOptLen=0;
		EXC_TYPE	exc=ExtractStringHdrVal(lpszOption, ulOptLen, FALSE, FALSE);
		if (exc != EOK)
			return exc;

		IMAP4QUOTARESINFO	resInfo;
		memset(&resInfo, 0, (sizeof resInfo));

		if ((exc=ExtractNumVal(resInfo.ulCurVal)) != EOK)
			return exc;

		if ((exc=ExtractNumVal(resInfo.ulMaxVal)) != EOK)
			return exc;

		if ((m_lpfnQcfn != NULL) && m_fContEnum)
		{
			LPCTSTR		lpszOptEnd=(lpszOption + ulOptLen);
			const TCHAR	chOE=(*lpszOptEnd);

			*((LPTSTR) lpszOptEnd) = _T('\0');
			exc = (*m_lpfnQcfn)(lpszOption, resInfo, m_pArg, m_fContEnum);
			*((LPTSTR) lpszOptEnd) = chOE;
		}
	}

	m_lpszCurPos++;	// skip list end delimiter
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

class CIMAP4GnrlQuotaRspParser : public CIMAP4QuotaResRspParser {
	private:
		// disable copy constructor and assignment operator
		CIMAP4GnrlQuotaRspParser (const CIMAP4GnrlQuotaRspParser& );
		CIMAP4GnrlQuotaRspParser& operator= (const CIMAP4GnrlQuotaRspParser& );

	public:
		CIMAP4GnrlQuotaRspParser (ISockioInterface&			SBSock,
										  LPCTSTR						lpszTag,
										  IMAP4_QUOTARES_ECFN_TYPE	lpfnQcfn,	// can be NULL
										  LPVOID							pArg,
										  LPTSTR							lpszRspBuf,
										  const UINT32					ulMaxRspLen,
										  const UINT32					ulRspTimeout)
			: CIMAP4QuotaResRspParser(SBSock, lpszTag, lpfnQcfn, pArg, lpszRspBuf, ulMaxRspLen, ulRspTimeout)
		{
		}

		virtual EXC_TYPE HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp);

		virtual ~CIMAP4GnrlQuotaRspParser () { }
};

/*--------------------------------------------------------------------------*/

EXC_TYPE CIMAP4GnrlQuotaRspParser::HandleUntaggedResponse (LPCTSTR lpszTag, LPCTSTR lpszOp)
{
	// ignore non-QUOTA responses
	if (_tcsicmp(lpszTag, szIMAP4QuotaRsp) != 0)
		return EOK;

	EXC_TYPE	exc=SkipFolderUpToParamsList(lpszOp);
	if (exc != EOK)
		return exc;

	if ((exc=HandleQuotaResourcesList()) != EOK)
		return exc;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

// callback function for quota resource information
static EXC_TYPE gqrCfn (LPCTSTR							lpszResName,	// resource name
								const IMAP4QUOTARESINFO&	resInfo,
								LPVOID							pArg,
								BOOLEAN&							fContEnum)
{
	if (NULL == pArg)
		return ECONTEXT;

	GNRLIMAP4QUOTAINFO&	qInfo=*((GNRLIMAP4QUOTAINFO *) pArg);
	switch (imap4XlateQuotaResStrCase(lpszResName))
	{
		case IMAP4_QUOTASTORAGE_RESCASE	: qInfo.resStorage = resInfo;	break;
		case IMAP4_QUOTAMESSAGE_RESCASE	: qInfo.resMessage = resInfo;	break;
		default									:	/* ignore */;
	}

	fContEnum = TRUE;
	return EOK;
}

/*--------------------------------------------------------------------------*/

// returns the GETQUOTA/GETQUOTAROOT response - according to rfc2087 - example:
//
//		C: A003 GETQUOTA ""
//		S: * QUOTA "" (STORAGE 10 512 MESSAGE 10 2000)
//		S: A003 OK Getquota completed

EXC_TYPE imap4GetGnrlQuotaCmdInfoSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													LPCTSTR					lpszQCmd,
													LPCTSTR					lpszRoot,	// NULL == general root
													GNRLIMAP4QUOTAINFO&	qInfo,
													LPTSTR					lpszRspBuf,
													const UINT32			ulMaxRspLen,
													const UINT32			ulRspTimeout)
{
	memset(&qInfo, 0, (sizeof qInfo));

	if ((NULL == lpszRspBuf) || (ulMaxRspLen < MAX_IMAP4_CMD_LEN))
		return EBADADDR;

	TCHAR				szCmd[MAX_IMAP4_CMD_LEN+4]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_CMD_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szTag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, lpszQCmd, FALSE, szTag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	// check how to send IMAP4 folder
	if ((exc=SendIMAP4QuotaFolder(SBSock, strb, szCmd, lpszRoot, ulRspTimeout)) != EOK)
		return exc;

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	CIMAP4GnrlQuotaRspParser	gqrsp(SBSock, lpszTag, gqrCfn, (LPVOID) &qInfo, lpszRspBuf, ulMaxRspLen, ulRspTimeout);
	if ((exc=gqrsp.ParseResponse()) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4GetGnrlQuotaCmdInfoSync (ISockioInterface&		SBSock,
													LPCTSTR					lpszITag,	// NULL == auto-generate
													LPCTSTR					lpszQCmd,
													LPCTSTR					lpszRoot,	// NULL == general root
													GNRLIMAP4QUOTAINFO&	qInfo,
													const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4GetGnrlQuotaCmdInfoSync(SBSock, lpszITag, lpszQCmd, lpszRoot, qInfo, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR IMAP4_UIDPLUS_CAPABILITY[]=_T("UIDPLUS");
SZXTRN const TCHAR IMAP4_LITERALPLUS_CAPABILITY[]=_T("LITERAL+");
SZXTRN const TCHAR IMAP4_MBOXREFERRAL_CAPABILITY[]=_T("MAILBOX-REFERRALS");
SZXTRN const TCHAR IMAP4_LOGINREFERRAL_CAPABILITY[]=_T("LOGIN-REFERRALS");
SZXTRN const TCHAR IMAP4_CHILDREN_CAPABILITY[]=_T("CHILDREN");

/*--------------------------------------------------------------------------*/

SZXTRN const TCHAR szIMAP4IdleCmd[]=_T("IDLE");
SZXTRN const TCHAR szIMAP4DoneCmd[]=_T("DONE");

/*--------------------------------------------------------------------------*/

#if defined(UNICODE) || defined(_UNICODE)
#	error "HMAC algorithm requires UNICODE adjustments"
#endif

static EXC_TYPE HMAC (LPCTSTR lpszText, LPCTSTR lpszKey, UINT8 buf[], /* IN/OUT */ UINT32& ulBufLen)
{
	if (IsEmptyStr(lpszText) || IsEmptyStr(lpszKey) || (NULL == buf) || (ulBufLen < MD5_DIGEST_LEN))
		return EPARAM;

	static const UINT32	MAX_PAD_LEN=64;
	EXC_TYPE			exc=EOK;
	CMD5Digester	md5Digester;
	BYTE				xorIPAD[MAX_PAD_LEN]={ 0 }, xorOPAD[MAX_PAD_LEN]={ 0 }, tmpKey[MD5_DIGEST_LEN]={ 0 };
	LPBYTE			pKey=(LPBYTE) lpszKey;
	UINT32			keyLen=_tcslen(lpszKey);
	// if password greater than max. length then use its digest as input key
	if (keyLen > MAX_PAD_LEN)
	{
		if ((exc=md5Digester.Digest(pKey, keyLen, tmpKey, (sizeof tmpKey))) != EOK)
			return exc;

		pKey = tmpKey;
		keyLen = MD5_DIGEST_LEN;
	}

	for (UINT32	kIndex=0; kIndex < keyLen; kIndex++)
	{
		xorIPAD[kIndex] = pKey[kIndex] ^ 0x36;
		xorOPAD[kIndex] = pKey[kIndex] ^ 0x5C;
	}

	// padd with zero remaining key bytes => X xor 0=X
	for (UINT32	pIndex=keyLen; pIndex < MAX_PAD_LEN; pIndex++)
	{
		xorIPAD[pIndex] = 0x36;
		xorOPAD[pIndex] = 0x5C;
	}

	// perform inner digest
	if ((exc=md5Digester.Update(xorIPAD, MAX_PAD_LEN)) != EOK)
		return exc;
	if ((exc=md5Digester.Update((BYTE *) lpszText, _tcslen(lpszText) * sizeof(TCHAR))) != EOK)
		return exc;

	BYTE	imedDigest[MD5_DIGEST_LEN]={ 0 };
	if ((exc=md5Digester.Digest(imedDigest, (sizeof imedDigest))) != EOK)
		return exc;

	if ((exc=md5Digester.Update(xorOPAD, MAX_PAD_LEN)) != EOK)
		return exc;
	if ((exc=md5Digester.Update(imedDigest, MD5_DIGEST_LEN)) != EOK)
		return exc;
	if ((exc=md5Digester.Digest(buf, ulBufLen)) != EOK)
		return exc;

	ulBufLen = MD5_DIGEST_LEN;
	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE IMAP4CRAMMD5Authenticator::GetChallengeResponse (LPCTSTR lpszChallenge, LPTSTR lpszRspBuf, UINT32& ulMaxLen)
{
	if (IsEmptyStr(lpszChallenge) || (NULL == lpszRspBuf) || (ulMaxLen <= MAX_IMAP4_TAG_LEN))
		return EPARAM;
	*lpszRspBuf = _T('\0');

	if (IsEmptyStr(m_lpszUID) || IsEmptyStr(m_lpszPass))
		return ESTATE;

	BYTE		hmVal[MD5_DIGEST_LEN]={ 0 };
	UINT32	ulHMLen=(sizeof hmVal);
	EXC_TYPE	exc=HMAC(lpszChallenge, m_lpszPass, hmVal, ulHMLen);
	if (exc != EOK)
		return exc;

	const UINT32	UIDLen=::_tcslen(m_lpszUID);
	if ((UIDLen+1+(ulHMLen * MAX_BYTE_HEX_DISPLAY_LENGTH)) >= ulMaxLen)
		return EOVERFLOW;

	::_tcscpy(lpszRspBuf, m_lpszUID);
	lpszRspBuf[UIDLen] = _T(' ');

	::byte_array_to_hex_string(hmVal, ulHMLen, lpszRspBuf + UIDLen + 1, ulMaxLen - UIDLen - 1, _T('\0'));
	lpszRspBuf[ulMaxLen - 1] = _T('\0');	// just making sure

	::_tcslwr(lpszRspBuf + UIDLen + 1);
	ulMaxLen = ::_tcslen(lpszRspBuf);

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

EXC_TYPE imap4AuthenticateUserSync (ISockioInterface&		SBSock,
												LPCTSTR					lpszITag,	// NULL==auto-generated
												IIMAP4Authenticator&	auth,
												LPTSTR					lpszRspBuf,
												const UINT32			ulMaxRspLen,
												const UINT32			ulRspTimeout)
{
	if ((NULL == lpszRspBuf) || (ulMaxRspLen <= MAX_IMAP4_TAG_LEN))
		return EPARAM;

	TCHAR				szCmd[MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN+2]=_T("");
	CStrlBuilder	strb(szCmd, MAX_IMAP4_DATA_LEN+MAX_IMAP4_TAG_LEN);
	LPCTSTR			lpszTag=lpszITag;
	TCHAR				szITag[MAX_IMAP4_TAG_LEN+2]=_T("");
	EXC_TYPE			exc=InitIMAP4Cmd(lpszTag, szIMAP4AuthCmd, FALSE, szITag, MAX_IMAP4_TAG_LEN, strb);
	if (exc != EOK)
		return exc;

	if ((exc=strb.AddChar(_T(' '))) != EOK)
		return exc;

	LPCTSTR	lpszMechanism=auth.GetMechanismName();
	if (IsEmptyStr(lpszMechanism))
		return EILLEGALOPCODE;
	if ((exc=strb.AddStr(lpszMechanism)) != EOK)
		return exc;

	if ((exc=strb.AddCRLF()) != EOK)
		return exc;

	int	wLen=SBSock.Write(szCmd);
	if (wLen <= 0)
		return ENOTCONNECTION;

	for (UINT32 ulLdx=1; ; ulLdx++)
	{
		BOOLEAN	fStripCRLF=FALSE;
		int		rLen=SBSock.ReadCmd(lpszRspBuf, ulMaxRspLen, ulRspTimeout, &fStripCRLF);
		if (rLen <= 0)
			return ENOTCONNECTION;

		// do not handle response unless contained entirely in response buffer
		if (!fStripCRLF)
			return EOVERFLOW;

		TCHAR		szTag[MAX_IMAP4_TAG_LEN+2]=_T(""), szOp[MAX_IMAP4_OPCODE_LEN+2]=_T("");
		LPCTSTR	lpszArgs=NULL;
		EXC_TYPE	exc=imap4ExtractRsp(lpszRspBuf, szTag, MAX_IMAP4_TAG_LEN, szOp, MAX_IMAP4_OPCODE_LEN, &lpszArgs);
		if (EOK == exc)
		{
			// if tag matches then translate operation code (OK/BAD/NO)
			if (_tcsicmp(lpszTag, szTag) == 0)
				return imap4XlateRspCode(szOp);

			// tag does not match the expected one
			return EILLOGICALRENAME;
		}
		else if (EDATACHAIN == exc)
			continue;	// skip unsolicited responses
		else if (exc != ECONTINUED)
			return exc;	// allow only '+' response

		// make sure this is indeed a continuation
		if ((NULL == lpszArgs) || (*lpszArgs != IMAP4_CONTINUE_RSP))
			return EUDFFORMAT;

		// find BASE64 challenge start
		for (lpszArgs++; (*lpszArgs != _T('\0')); lpszArgs++)
			if (*lpszArgs != _T(' '))
				break;

		if ((rLen=GetSafeStrlen(lpszArgs)) > 0)
		{
			UINT32	iLen=0, oLen=0;
			if ((exc=b64_decode_buf(lpszArgs, (UINT32) rLen, &iLen, (UINT8 *) szCmd, MAX_IMAP4_DATA_LEN * sizeof(TCHAR), &oLen)) != EOK)
			{
				if (exc != EEOF)
					return exc;
			}

			if (rLen != (int) iLen)
				return EBADBUFF;

			szCmd[oLen / sizeof(TCHAR)] = _T('\0');
		}
		else
			szCmd[0] = _T('\0');

		UINT32	ulMaxLen=ulMaxRspLen;
		if ((exc=auth.GetChallengeResponse(szCmd, lpszRspBuf, ulMaxLen)) != EOK)
			return exc;

		if (ulMaxLen > 0)
		{
			UINT32	oLen=0;
			if ((exc=b64_encode_finish((UINT8 *) lpszRspBuf, ulMaxLen * sizeof(TCHAR), szCmd, MAX_IMAP4_DATA_LEN, &oLen)) != EOK)
			{
				if (exc != EEOF)
					return exc;
			}

			szCmd[oLen] = _T('\0');	// just making sure
			ulMaxLen = oLen;
		}

		if ((wLen=SBSock.Write(szCmd, ulMaxLen)) != (int) ulMaxLen)
			return ENOTCONNECTION;
		if ((wLen=SBSock.Writeln()) != 2)
			return ENOTCONNECTION;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE imap4AuthenticateUserSync (ISockioInterface&		SBSock,
												LPCTSTR					lpszITag,	// NULL==auto-generated
												IIMAP4Authenticator&	auth,
												const UINT32			ulRspTimeout)
{
	TCHAR	szRsp[MAX_IMAP4_DATA_LEN+2]=_T("");
	return imap4AuthenticateUserSync(SBSock, lpszITag, auth, szRsp, MAX_IMAP4_DATA_LEN, ulRspTimeout);
}

//////////////////////////////////////////////////////////////////////////////
