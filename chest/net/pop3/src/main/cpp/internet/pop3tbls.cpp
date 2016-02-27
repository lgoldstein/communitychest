#include <util/errors.h>
#include <util/string.h>

#include <internet/pop3Lib.h>

#ifndef __cplusplus
#error "This file requires a C++ compiler !!!"
#endif

/*---------------------------------------------------------------------------*/

CPOP3MsgInfo::CPOP3MsgInfo (const UINT32	ulMsgNum,
									 const char		*lpszUIDL,
									 const UINT32	ulMsgSize)
	: m_lpszUIDL(NULL), m_ulUIDLLen(0), m_ulMsgNum(0), m_ulMsgSize(0)
{
	SetInfo(ulMsgNum, lpszUIDL, ulMsgSize);
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3MsgInfo::UpdateInfo (const CPOP3MsgInfo& p3mi)
{
	Clear();

	if (p3mi.m_ulMsgNum != 0)
		return SetInfo(p3mi.m_ulMsgNum, p3mi.m_lpszUIDL, p3mi.m_ulMsgSize);

	return EOK;
}

/*---------------------------------------------------------------------------*/

// copy constructor
CPOP3MsgInfo::CPOP3MsgInfo (const CPOP3MsgInfo& p3mi)
	: m_lpszUIDL(NULL), m_ulUIDLLen(0), m_ulMsgNum(0), m_ulMsgSize(0)
{
	UpdateInfo(p3mi);
}

/*---------------------------------------------------------------------------*/

void CPOP3MsgInfo::Clear ()
{
	if (m_lpszUIDL != NULL)
		*m_lpszUIDL = '\0';
	m_ulMsgNum = 0;
	m_ulMsgSize = 0;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3MsgInfo::SetUIDL (const UINT32 ulMsgNum, const char *lpszUIDL)
{
	if ((0 == ulMsgNum) || IsEmptyStr(lpszUIDL))
		return EPARAM;

	UINT32	ulULen=strlen(lpszUIDL);
	if (ulULen > m_ulUIDLLen)
	{
		if (m_lpszUIDL != NULL)
		{
			delete [] m_lpszUIDL;
			m_ulUIDLLen = 0;
			m_lpszUIDL = NULL;
		}

		if (NULL == (m_lpszUIDL=new char[ulULen+2]))
			return EMEM;

		m_ulUIDLLen = ulULen;
	}

	strcpy(m_lpszUIDL, lpszUIDL);
	m_ulMsgNum = ulMsgNum;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3MsgInfo::SetSize (const UINT32 ulMsgNum, const UINT32 ulMsgSize)
{
	if ((0 == ulMsgNum) || (0 == ulMsgSize))
		return EPARAM;

	m_ulMsgNum = ulMsgNum;
	m_ulMsgSize = ulMsgSize;

	return EOK;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3MsgInfo::SetInfo (const UINT32	ulMsgNum,
										  const char	*lpszUIDL,
										  const UINT32	ulMsgSize)
{
	EXC_TYPE	exc=EOK;

	if (IsEmptyStr(lpszUIDL) || (0 == ulMsgSize))
		return EPARAM;

	if ((exc=SetUIDL(ulMsgNum, lpszUIDL)) != EOK)
		return exc;
	if ((exc=SetSize(ulMsgNum, ulMsgSize)) != EOK)
		return exc;

	return EOK;
}

/*---------------------------------------------------------------------------*/

CPOP3InfoTbl::CPOP3InfoTbl (const UINT32 ulMsgsNum)
	: m_ulMsgsNum(0), m_pInfo(NULL), m_Mapper()
{
	if (ulMsgsNum != 0)
	{
		SetSize(ulMsgsNum);
	}
}

/*---------------------------------------------------------------------------*/

void CPOP3InfoTbl::Clear ()
{
	if (m_pInfo != NULL)
	{
		delete [] m_pInfo;
		m_pInfo = NULL;
	}

	m_ulMsgsNum = 0;
	m_Mapper.Clear();
}

/*---------------------------------------------------------------------------*/

// does not allow re-initialization
EXC_TYPE CPOP3InfoTbl::SetSize (const UINT32 ulMsgsNum)
{
	if (m_ulMsgsNum != 0)
		return EEXIST;

	if (NULL == (m_pInfo=new CPOP3MsgInfo[ulMsgsNum]))
		return EMEM;

	m_ulMsgsNum = ulMsgsNum;
	return m_Mapper.InitMap(m_ulMsgsNum, TRUE);
}

/*---------------------------------------------------------------------------*/

CPOP3MsgInfo *CPOP3InfoTbl::FindInfo (const UINT32 ulMsgNum) const
{
	// check that requested msg info is within allowed range
	if ((0 == ulMsgNum) || (ulMsgNum > m_ulMsgsNum))
		return NULL;

	// check that we have info records allocated
	if (NULL == m_pInfo)
		return NULL;

	// POP3 message numbers start at 1
	UINT32			ulIdx=(ulMsgNum - 1);
	CPOP3MsgInfo	*pInfo=(m_pInfo + ulIdx);
	UINT32			ulINum=pInfo->GetMsgNum();

	// if number already assigned, then check that it matches requested one
	if (ulINum != 0)
	{
		if (ulINum != ulMsgNum)
			return NULL;
	}

	return pInfo;
}

/*---------------------------------------------------------------------------*/
		
EXC_TYPE CPOP3InfoTbl::AddInfo (const UINT32	ulMsgNum,
										  const char	*lpszUIDL,
										  const UINT32	ulMsgSize)
{
	CPOP3MsgInfo *pInfo=FindInfo(ulMsgNum);
	if (NULL == pInfo)
		return EFNEXIST;
	
	EXC_TYPE	exc=pInfo->SetInfo(ulMsgNum, lpszUIDL, ulMsgSize);
	if (EOK == exc)
		exc = m_Mapper.AddKey(lpszUIDL, (LPVOID) pInfo);
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3InfoTbl::AddUIDL (const UINT32 ulMsgNum, const char *lpszUIDL)
{
	CPOP3MsgInfo *pInfo=FindInfo(ulMsgNum);
	if (NULL == pInfo)
		return EFNEXIST;
	
	EXC_TYPE	exc=pInfo->SetUIDL(ulMsgNum, lpszUIDL);
	if (EOK == exc)
		exc = m_Mapper.AddKey(lpszUIDL, (LPVOID) pInfo);
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3InfoTbl::AddSize (const UINT32 ulMsgNum, const UINT32 ulMsgSize)
{
	CPOP3MsgInfo *pInfo=FindInfo(ulMsgNum);
	if (NULL == pInfo)
		return EFNEXIST;
	return pInfo->SetSize(ulMsgNum, ulMsgSize);
}

/*---------------------------------------------------------------------------*/

const CPOP3MsgInfo *CPOP3InfoTbl::GetInfo (const UINT32 ulIdx) const
{
	if (ulIdx >= m_ulMsgsNum)
		return NULL;

	return (m_pInfo + ulIdx);
}

const CPOP3MsgInfo *CPOP3InfoTbl::GetInfo (const char *lpszUIDL) const
{
	LPVOID	pV=NULL;
	EXC_TYPE	exc=m_Mapper.FindKey(lpszUIDL, pV);
	return ((EOK == exc) ? (const CPOP3MsgInfo *) pV : NULL);
}

/*---------------------------------------------------------------------------*/

typedef struct {
	LPCPOP3INFOTBL	pTbl;
	EXC_TYPE			exc;
	BOOLEAN			fIsMsgSize;
} POPLARGS, *LPPOPLARGS;

static BOOLEAN poplUIDLtblCfn (const UINT32	msgNum,		/* requested ID */
										 const SINT32	/* linesNum */,	/* requested num */
										 const char		iBuf[],		/* read buffer */
										 const UINT32	iLen,			/* valid datalen */
										 void				*pArg)		/* caller arg */
{
	LPPOPLARGS	pPopl=(LPPOPLARGS) pArg;

	if ((0 != msgNum) && (!IsEmptyStr(iBuf)) && (0 != iLen) && (NULL != pPopl))
	{
		LPCPOP3INFOTBL	pTbl=pPopl->pTbl;
		if (pTbl != NULL)
		{
			if (pPopl->fIsMsgSize)
			{
				UINT32	ulMsgSize=argument_to_dword(iBuf, iLen, EXC_ARG(pPopl->exc));
				if (EOK == pPopl->exc)
					pPopl->exc = pTbl->AddSize(msgNum, ulMsgSize);
			}
			else	// this is UIDL
				pPopl->exc = pTbl->AddUIDL(msgNum, iBuf);

			if (pPopl->exc != EOK)
				return FALSE;
		}
		else	// missing table param
		{
			pPopl->exc = ENOPARAMETERS;
			return FALSE;
		}
	}
	else	// some bad arguments
	{
		if (pPopl != NULL)
			pPopl->exc = ECONTEXT;
		return FALSE;
	}

	return TRUE;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE CPOP3InfoTbl::Populate (ISockioInterface&	CBSock,
											const BOOLEAN		fGetMsgSize,
											const UINT32		uRspTimeout)
{
	EXC_TYPE	exc=EOK;

	if (0 == m_ulMsgsNum)
	{
		UINT32	ulMsgsNum=0, ulMboxSize=0;
		if ((exc=pop3_clnt_stat(CBSock, &ulMsgsNum, &ulMboxSize, uRspTimeout)) != EOK)
			return exc;

		if (0 == ulMsgsNum)
			return EOK;

		if ((exc=SetSize(ulMsgsNum)) != EOK)
			return exc;
	}

	POPLARGS	poplArgs;
	memset(&poplArgs, 0, (sizeof poplArgs));
	poplArgs.pTbl = this;

	// 1st (and foremost !!!) populate with UIDL(s)
	if ((exc=pop3_clnt_uidl(CBSock, POP3_ALL_MSGS, poplUIDLtblCfn, (void *) &poplArgs, uRspTimeout)) != EOK)
		return exc;

	if ((exc=poplArgs.exc) != EOK)
		return exc;

	poplArgs.fIsMsgSize = fGetMsgSize;
	if (poplArgs.fIsMsgSize)
	{
		if ((exc=pop3_clnt_list(CBSock, POP3_ALL_MSGS, poplUIDLtblCfn, (void *) &poplArgs, uRspTimeout)) != EOK)
			return exc;

		if ((exc=poplArgs.exc) != EOK)
			return exc;
	}

	return EOK;
}