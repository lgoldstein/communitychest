#ifndef _IMAP4_EXTS_H_
#define _IMAP4_EXTS_H_

/* IMAP4 protocol extensions */

/*--------------------------------------------------------------------------*/

#include <internet/imap4Lib.h>
#include <internet/md5.h>

/*--------------------------------------------------------------------------*/

/* extended capabilities */
typedef struct {
	unsigned m_fQuota			: 1;
	unsigned m_fNamespace	: 1;
	unsigned m_fLiteralPlus	: 1;
	unsigned m_fIdle			: 1;
	unsigned m_fUIDPlus		: 1;
	unsigned m_fMboxRefer	: 1;
	unsigned m_fLoginRefer	: 1;
	unsigned m_fChildren		: 1;
} IMAP4EXTCAPS, *LPIMAP4EXTCAPS;

#ifdef __cplusplus
extern EXC_TYPE imap4ExtendedCapabilitiesSync (ISockioInterface&	SBSock,
															  LPCTSTR				lpszITag,	// NULL == auto-generate
															  IMAP4EXTCAPS&		extCaps,
															  LPTSTR					lpszRspBuf,
															  const UINT32			ulMaxRspLen,
															  const UINT32			ulRspTimeout);

extern EXC_TYPE imap4ExtendedCapabilitiesSync (ISockioInterface&	SBSock,
															  LPCTSTR				lpszITag,	// NULL == auto-generate
															  IMAP4EXTCAPS&		extCaps,	
															  const UINT32			ulRspTimeout);
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

/* extensions */
extern const TCHAR szIMAP4IdleCmd[];
extern const TCHAR szIMAP4DoneCmd[];

#define IMAP4_IDLE_CAPABILITY	szIMAP4IdleCmd

/*--------------------------------------------------------------------------*/

extern const TCHAR szIMAP4GetQuotaCmd[];
extern const TCHAR szIMAP4GetQuotaRootCmd[];
extern const TCHAR szIMAP4SetQuotaCmd[];

extern const TCHAR szIMAP4QuotaRsp[];
extern const TCHAR szIMAP4QuotaRootRsp[];

#define IMAP4_QUOTA_CAPABILITY	szIMAP4QuotaRsp

/*--------------------------------------------------------------------------*/

typedef enum {
	IMAP4_QUOTASTORAGE_RESCASE,
	IMAP4_QUOTAMESSAGE_RESCASE,
	IMAP4_QUOTABAD_RESCASE
} IMAP4_QUOTARES_CASE_TYPE;

#define fIsBadIMAP4QuotaResCase(c) (((unsigned) (c)) >= ((unsigned) IMAP4_QUOTABAD_RESCASE))

extern const TCHAR szIMAP4QuotaStorageRes[];
extern const TCHAR szIMAP4QuotaMessageRes[];

extern IMAP4_QUOTARES_CASE_TYPE imap4XlateQuotaResCase (LPCTSTR lpszQuotaRes, const UINT32 ulQRLen);

#ifdef __cplusplus
inline IMAP4_QUOTARES_CASE_TYPE imap4XlateQuotaResStrCase (LPCTSTR lpszQuotaRes)
{
	return imap4XlateQuotaResCase(lpszQuotaRes, GetSafeStrlen(lpszQuotaRes));
}
#else
#define imap4XlateQuotaResStrCase(lpszQuotaRes)	\
	imap4XlateQuotaResCase((lpszQuotaRes), GetSafeStrlen(lpszQuotaRes))
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	UINT32	ulCurVal;
	UINT32	ulMaxVal;
} IMAP4QUOTARESINFO;

#ifdef __cplusplus
// callback function for quota resource information
typedef EXC_TYPE (*IMAP4_QUOTARES_ECFN_TYPE)(LPCTSTR							lpszResName,	// resource name
															const IMAP4QUOTARESINFO&	resInfo,
															LPVOID							pArg,
															BOOLEAN&							fContEnum);
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

typedef struct {
	IMAP4QUOTARESINFO	resStorage;	/* in KB(s) */
	IMAP4QUOTARESINFO	resMessage;
} GNRLIMAP4QUOTAINFO, *LPGNRLIMAP4QUOTAINFO;

#ifdef __cplusplus
// returns the general GETQUOTA/GETQUOTAROOT response - according to rfc2087
extern EXC_TYPE imap4GetGnrlQuotaCmdInfoSync (ISockioInterface&	SBSock,
															 LPCTSTR					lpszITag,	// NULL == auto-generate
															 LPCTSTR					lpszQCmd,
															 LPCTSTR					lpszRoot,	// NULL == general root
															 GNRLIMAP4QUOTAINFO&	qInfo,
															 LPTSTR					lpszRspBuf,
															 const UINT32			ulMaxRspLen,
															 const UINT32			ulRspTimeout);

extern EXC_TYPE imap4GetGnrlQuotaCmdInfoSync (ISockioInterface&	SBSock,
															 LPCTSTR					lpszITag,	// NULL == auto-generate
															 LPCTSTR					lpszQCmd,
															 LPCTSTR					lpszRoot,	// NULL == general root
															 GNRLIMAP4QUOTAINFO&	qInfo,
															 const UINT32			ulRspTimeout);

inline EXC_TYPE imap4GetRootQuotaInfoSync (ISockioInterface&	SBSock,
														 LPCTSTR					lpszITag,	// NULL == auto-generate
														 LPCTSTR					lpszRoot,	// NULL == INBOX
														 GNRLIMAP4QUOTAINFO&	qInfo,
														 const UINT32			ulRspTimeout)
{
	return imap4GetGnrlQuotaCmdInfoSync(SBSock, lpszITag, szIMAP4GetQuotaRootCmd,
													(IsEmptyStr(lpszRoot) ? IMAP4_INBOX : lpszRoot), qInfo, ulRspTimeout);
}

inline EXC_TYPE imap4GetRootTotalQuotaInfoSync (ISockioInterface&		SBSock,
																LPCTSTR					lpszITag,	// NULL == auto-generate
																GNRLIMAP4QUOTAINFO&	qInfo,
																const UINT32			ulRspTimeout)
{
	return imap4GetRootQuotaInfoSync(SBSock, lpszITag, NULL, qInfo, ulRspTimeout);
}

inline EXC_TYPE imap4GetGnrlQuotaInfoSync (ISockioInterface&	SBSock,
														 LPCTSTR					lpszITag,	// NULL == auto-generate
														 LPCTSTR					lpszRoot,	// NULL == general root
														 GNRLIMAP4QUOTAINFO&	qInfo,
														 const UINT32			ulRspTimeout)
{
	return imap4GetGnrlQuotaCmdInfoSync(SBSock, lpszITag, szIMAP4GetQuotaCmd, lpszRoot, qInfo, ulRspTimeout);
}

inline EXC_TYPE imap4GetTotalQuotaInfoSync (ISockioInterface&		SBSock,
														  LPCTSTR					lpszITag,	// NULL == auto-generate
														  GNRLIMAP4QUOTAINFO&	qInfo,
														  const UINT32				ulRspTimeout)
{
	return imap4GetGnrlQuotaInfoSync(SBSock, lpszITag, NULL, qInfo, ulRspTimeout);
}
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

extern const TCHAR szIMAP4NamespaceCmd[];
#define szIMAP4NamespaceRsp szIMAP4NamespaceCmd

#define IMAP4_NAMESPACE_CAPABILITY	szIMAP4NamespaceCmd

typedef struct {
	LPCTSTR	lpszPrefix;	// may be NULL/empty
	TCHAR		chDelim;
} IMAP4NAMESPACEDEF, *LPIMAP4NAMESPACEDEF;

#ifdef __cplusplus
typedef EXC_TYPE (*IMAP4_NSENUM_CFN)(const UINT32					ulItemNdx,
												 const IMAP4NAMESPACEDEF&	nsDef,
												 LPVOID							pArg,
												 BOOLEAN&						fContEnum);

class CIMAP4NamespaceGroup {
	private:
		CVSDCollection	m_nsc;

		// disable copy constructor and assignment operator
		CIMAP4NamespaceGroup (const CIMAP4NamespaceGroup& );
		CIMAP4NamespaceGroup& operator= (const CIMAP4NamespaceGroup& );

	public:
		// also default constructor
		CIMAP4NamespaceGroup (const UINT32 ulMaxItems=0, const UINT32 ulGrow=0)
			: m_nsc(ulMaxItems, ulGrow)
		{
		}

		// Note(s):
		//
		//		a. grow factor may be zero - i.e. when limit is reached no more items
		//			are added.
		//
		//		b. cannot be re-initialized !!!
		EXC_TYPE SetParams (const UINT32 ulMaxItems, const UINT32 ulGrow)
		{
			return m_nsc.SetParams(ulMaxItems, ulGrow);
		}

		// prefix and delimiter may be empty/NULL
		EXC_TYPE AddItem (LPCTSTR lpszPrefix, const TCHAR chDelim);

		EXC_TYPE AddItem (const IMAP4NAMESPACEDEF& nsDef)
		{
			return AddItem(nsDef.lpszPrefix, nsDef.chDelim);
		}

		UINT32 GetSize () const
		{
			return m_nsc.GetSize();
		}

		// if NULL returned then bad/illegal index
		LPIMAP4NAMESPACEDEF operator[] (const UINT32 ulIdx) const;

		// Note: pointer becomes invalid if group changes (grows, decreases, destroyed)
		EXC_TYPE GetNamespaceDefinition (const UINT32 ulIdx, LPIMAP4NAMESPACEDEF& pDef) const;

		// removes all data items
		EXC_TYPE Reset ();

		EXC_TYPE EnumItems (IMAP4_NSENUM_CFN lpfnEcfn, LPVOID pArg) const;

		EXC_TYPE Merge (const CIMAP4NamespaceGroup& nsg);

		virtual ~CIMAP4NamespaceGroup ()
		{
			Reset();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

/* IMAP4 namespaces types - Note: order is important (see rfc2342) */
typedef enum {
	IMAP4_NSPERSONAL_CASE,
	IMAP4_NSOTHER_CASE,
	IMAP4_NSSHARED_CASE,
	IMAP4_NSBAD_CASE
} IMAP4NAMESPACECASE;

#define fIsBadIMAP4NamespaceCase(c) (((unsigned) (c)) >= ((unsigned) IMAP4_NSBAD_CASE))

#ifdef __cplusplus
class CIMAP4Namespaces {
	private:
		CIMAP4NamespaceGroup	m_nsPersonal;
		CIMAP4NamespaceGroup	m_nsShared;
		CIMAP4NamespaceGroup	m_nsOther;

	public:
		CIMAP4Namespaces (const UINT32 ulMaxItems=0, const UINT32 ulGrow=0)
			: m_nsPersonal(ulMaxItems, ulGrow), m_nsShared(ulMaxItems, ulGrow), m_nsOther(ulMaxItems, ulGrow)
		{
		}

		const CIMAP4NamespaceGroup& GetPersonalNamespace () const
		{
			return m_nsPersonal;
		}

		const CIMAP4NamespaceGroup& GetSharedNamespace () const
		{
			return m_nsShared;
		}

		const CIMAP4NamespaceGroup& GetOtherNamespace () const
		{
			return m_nsOther;
		}

		// returns NULL if bad/illegal namespace case requested
		const CIMAP4NamespaceGroup *GetNamespace (const IMAP4NAMESPACECASE nsCase) const;

		// Note(s):
		//
		//		a. grow factor may be zero - i.e. when limit is reached no more items
		//			are added.
		//
		//		b. cannot be re-initialized !!!
		EXC_TYPE SetParams (const UINT32 ulMaxItems, const UINT32 ulGrow);

		// prefix and delimiter may be empty/NULL
		EXC_TYPE AddItem (const IMAP4NAMESPACECASE nsCase, LPCTSTR lpszPrefix, const TCHAR chDelim);

		EXC_TYPE AddItem (const IMAP4NAMESPACECASE nsCase, const IMAP4NAMESPACEDEF& nsDef)
		{
			return AddItem(nsCase, nsDef.lpszPrefix, nsDef.chDelim);
		}

		EXC_TYPE EnumItems (const IMAP4NAMESPACECASE nsCase, IMAP4_NSENUM_CFN lpfnEcfn, LPVOID pArg) const;

		// removes all data items
		EXC_TYPE Reset ();

		virtual ~CIMAP4Namespaces ()
		{
			Reset();
		}
};

extern EXC_TYPE imap4GetNamespaceSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,
													CIMAP4Namespaces&	ns,
													LPTSTR				lpszRspBuf,
													const UINT32		ulMaxLen,
													const UINT32		ulRspTimeout);

extern EXC_TYPE imap4GetNamespaceSync (ISockioInterface&	SBSock,
													LPCTSTR				lpszITag,
													CIMAP4Namespaces&	ns,
													const UINT32		ulRspTimeout);
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

extern const TCHAR IMAP4_LITERALPLUS_CAPABILITY[];
extern const TCHAR IMAP4_UIDPLUS_CAPABILITY[];
extern const TCHAR IMAP4_MBOXREFERRAL_CAPABILITY[];
extern const TCHAR IMAP4_LOGINREFERRAL_CAPABILITY[];
extern const TCHAR IMAP4_CHILDREN_CAPABILITY[];

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class IIMAP4Authenticator {
	protected:
		IIMAP4Authenticator ()
		{
		}

	public:
		virtual LPCTSTR GetMechanismName () const = 0;

		virtual EXC_TYPE GetChallengeResponse (LPCTSTR lpszChallenge, LPTSTR lpszRspBuf, /* IN/OUT */ UINT32& ulMaxLen) = 0;

		virtual ~IIMAP4Authenticator ()
		{
		}
};

extern EXC_TYPE imap4AuthenticateUserSync (ISockioInterface&		SBSock,
														 LPCTSTR						lpszITag,	// NULL==auto-generated
														 IIMAP4Authenticator&	auth,
														 LPTSTR						lpszRspBuf,
														 const UINT32				ulMaxRspLen,
														 const UINT32				ulRspTimeout);

extern EXC_TYPE imap4AuthenticateUserSync (ISockioInterface&		SBSock,
														 LPCTSTR						lpszITag,	// NULL==auto-generated
														 IIMAP4Authenticator&	auth,
														 const UINT32				ulRspTimeout);

/*--------------------------------------------------------------------------*/

class IMAP4CRAMMD5Authenticator : public IIMAP4Authenticator {
	private:
		const LPCTSTR	m_lpszUID, m_lpszPass;

	public:
		IMAP4CRAMMD5Authenticator (LPCTSTR lpszUID, LPCTSTR lpszPass)
			: IIMAP4Authenticator()
			, m_lpszUID(lpszUID)
			, m_lpszPass(lpszPass)
		{
		}

		virtual LPCTSTR GetMechanismName () const
		{
			return _T("CRAM-MD5");
		}

		virtual EXC_TYPE GetChallengeResponse (LPCTSTR lpszChallenge, LPTSTR lpszRspBuf, UINT32& ulMaxLen);

		virtual ~IMAP4CRAMMD5Authenticator ()
		{
		}
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#endif	/* _IMAP4_EXTS_H_ */
