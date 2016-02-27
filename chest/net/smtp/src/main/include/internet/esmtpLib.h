#ifndef _EXT_SMTP_LIB_H_
#define _EXT_SMTP_LIB_H_

#include <internet/smtpLib.h>

/*--------------------------------------------------------------------------*/

/* extended error codes */
#define ESMTP_E_AUTH_SUCCEED		235	/* Authentication successful */

#define ESMTP_E_AUTH_DATA			334	/* Authentication client response required */

#define ESMTP_E_PASSWORD_NEEDED	432	/* A password transition is needed */
#define ESMTP_E_TEMP_AUTH_FAIL	454	/* Temporary authentication failure */

#define ESMTP_E_AUTH_REQ			530	/* Authentication required */
#define ESMTP_E_AUTH_TOO_WEAK		534	/* Authentication mechanism is too weak */
#define ESMTP_E_AUTH_FAILED		535	/* General authentication failure */
#define ESMTP_E_ENCRYPTION_REQ	538	/* Encryption required for requested authentication mechanism */

/*--------------------------------------------------------------------------*/

extern const TCHAR szESMTPEhloCmd[];
extern const TCHAR szESMTPAuthCmd[];
extern const TCHAR szESMTPEtrnCmd[];

/*--------------------------------------------------------------------------*/

extern const TCHAR szESMTLoginAuth[];
extern const TCHAR szESMTCRAMMD5Auth[];
extern const TCHAR szESMTDigestMD5Auth[];

typedef enum {
	ESMTP_LOGIN_AUTHCASE,
	ESMTP_CRAMMD5_AUTHCASE,
	ESMTP_DGSTMD5_AUTHCASE,
	ESMTP_BAD_AUTHCASE
} ESMTPAUTHCASE;

extern ESMTPAUTHCASE esmtpChars2AuthCase (LPCTSTR lpszAuthCase, const UINT32 ulACLen);
#ifdef __cplusplus
inline ESMTPAUTHCASE esmtpStr2AuthCase (LPCTSTR lpszAuthCase)
{
	return esmtpChars2AuthCase(lpszAuthCase, GetSafeStrlen(lpszAuthCase));
}
#else
#	define esmtpStr2AuthCase(ac) esmtpChars2AuthCase((ac), GetSafeStrlen(ac))
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
inline EXC_TYPE esmtpOpenSock (ISockioInterface&	ISock,
										 LPCTSTR					lpszHost,
										 const int				pnum,
										 UINT32&					rcode,
										 const UINT32			ulRspTimeout)
{
	return smtpOpenSock(ISock, lpszHost, pnum, rcode, ulRspTimeout);
}
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// callback for enumerating capabilities listed in EHLO response
typedef EXC_TYPE (*ESMTP_CAPSCFN)(ISockioInterface&	ISock,
											 LPCTSTR					lpszCapability,
											 LPVOID					pArg,
											 BOOLEAN&				fContEnum);

extern EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
										  LPCTSTR				lpszID,	// if NULL/empty then hostname is used
										  ESMTP_CAPSCFN		lpfnEcfn,	// may be NULL
										  LPVOID					pArg,
										  UINT32&				rcode,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxBufLen,
										  const UINT32			ulRspTimeout);

extern EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
										  LPCTSTR				lpszID,	// if NULL/empty then hostname is used
										  ESMTP_CAPSCFN		lpfnEcfn,	// may be NULL
										  LPVOID					pArg,
										  UINT32&				rcode,
										  const UINT32			ulRspTimeout);

extern EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
										  LPCTSTR				lpszID,	// if NULL/empty then hostname is used
										  CStr2PtrMapper&		capsMap,	// key=capability name, value=NULL
										  UINT32&				rcode,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxBufLen,
										  const UINT32			ulRspTimeout);

extern EXC_TYPE esmtpSendEHelo (ISockioInterface&	ISock,
										  LPCTSTR				lpszID,	// if NULL/empty then hostname is used
										  CStr2PtrMapper&		capsMap,	// key=capability name, value=NULL
										  UINT32&				rcode,
										  const UINT32			ulRspTimeout);
#endif	/* of ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

extern EXC_TYPE esmtpAuthLogin (SOCKET			sock,
										  LPCTSTR		lpszUID,
										  LPCTSTR		lpszPassword,
										  UINT32			*rcode,
										  const UINT32	ulRspTimeout);

#ifdef __cplusplus
extern EXC_TYPE esmtpAuthLogin (ISockioInterface&	ISock,
										  LPCTSTR				lpszUID,
										  LPCTSTR				lpszPassword,
										  UINT32&				rcode,
										  LPTSTR					lpszRspBuf,
										  const UINT32			ulMaxBufLen,
										  const UINT32			ulRspTimeout);

extern EXC_TYPE esmtpAuthLogin (ISockioInterface&	ISock,
										  LPCTSTR				lpszUID,
										  LPCTSTR				lpszPassword,
										  UINT32&				rcode,
										  const UINT32			ulRspTimeout);
#endif

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
inline EXC_TYPE esmtpSendEtrn (ISockioInterface&	ISock,
										 LPCTSTR					lpszDomain,
										 UINT32&					rcode,
										 const UINT32			ulRspTimeout)
{
	return smtpSendCmdSync(ISock, szESMTPEtrnCmd, lpszDomain, rcode, ulRspTimeout);
}
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
/*
 *		Each option in mapper is an "attribute=value" pair where the key is
 * attribute and the mapped item is the value. If the mapped item is NULL/empty,
 * then the key is assumed to be a predicate rather than an attribute. If no
 * attributes are specified, then the "smtp" version is called.
 *
 *	Note: it is assumed that the order of the attributes is not important
 */
extern EXC_TYPE esmtpSetSender (ISockioInterface&		ISock,
										  LPCTSTR					lpszSndr,	// may be NULL/empty
										  const CStr2StrMapper&	opts,
										  UINT32&					rcode,
										  LPTSTR						lpszRspBuf,
										  const UINT32				ulMaxBufLen,
										  const UINT32				ulRspTimeout);

extern EXC_TYPE esmtpSetSender (ISockioInterface&		ISock,
										  LPCTSTR					lpszSndr,	// may be NULL/empty
										  const CStr2StrMapper&	opts,
										  UINT32&					rcode,
										  const UINT32				ulRspTimeout);

extern EXC_TYPE esmtpAddRecipient (ISockioInterface&		ISock,
											  LPCTSTR					lpszRecip,
											  const CStr2StrMapper&	opts,
											  UINT32&					rcode,
											  LPTSTR						lpszRspBuf,
											  const UINT32				ulMaxBufLen,
											  const UINT32				ulRspTimeout);

extern EXC_TYPE esmtpAddRecipient (ISockioInterface&		ISock,
											  LPCTSTR					lpszRecip,
											  const CStr2StrMapper&	opts,
											  UINT32&					rcode,
											  const UINT32				ulRspTimeout);
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

/* string used to report the DSN capability on EHLO response */
extern const TCHAR szESMTPDSNOption[];

/* as per RFC1891 - DSN option and limitations */
extern const TCHAR szESMTPDSNENVIDKwd[];
#define ESMTP_DSN_MAX_ENVID_LEN 100

extern const TCHAR szESMTPDSNNotifyKwd[];
extern const TCHAR	szESMTPDSNNotifyNeverOpt[];
extern const TCHAR	szESMTPDSNNotifySuccessOpt[];
extern const TCHAR	szESMTPDSNNotifyFailureOpt[];
extern const TCHAR	szESMTPDSNNotifyDelayOpt[];
#define ESMTP_DSN_MAX_NOTIFY_LEN	28

typedef struct {
	unsigned m_fNever		:	1;
	unsigned m_fSuccess	:	1;
	unsigned m_fFailure	:	1;
	unsigned m_fDelay		:	1;
} ESMTPDSNNOTIFYOPTS;

#ifdef __cplusplus
// Note: if no option set, then nothing is done (and EOK returned)
extern EXC_TYPE esmtpBuildDSNNotifyOptions (const ESMTPDSNNOTIFYOPTS& opts, IStrlBuilder& strb);

inline EXC_TYPE esmtpBuildDSNNotifyOptions (const ESMTPDSNNOTIFYOPTS& opts, LPTSTR lpszBuf, const UINT32 ulBufLen)
{
	return esmtpBuildDSNNotifyOptions(opts, CStrlBuilder(lpszBuf, ulBufLen));
}
#endif	/* of __cplusplus */

extern const TCHAR szESMTPDSNORCPTKwd[];
#define ESMTP_DSN_MAX_ORCPT_LEN 500

extern const TCHAR szESMTPDSNRETKwd[];
extern const TCHAR	szESMTPDSNRETHdrsOpt[];
extern const TCHAR	szESMTPDSNRETFullOpt[];
#define ESMTP_DSN_MAX_RET_LEN	8

/* do not change order !!! */
typedef enum {
	ESMTP_DSN_RETNONE=0,
	ESMTP_DSN_RETHDRS,
	ESMTP_DSN_RETFULL,
	ESMTP_DSN_RETBAD
} ESMTPDSNRETCASE;

#define fIsBadESMTPDSNRetOpt(e)	(((unsigned) (e)) >= ((unsigned) ESMTP_DSN_RETBAD))

/* index is a VALID enumeration value (BAD => NULL, NONE => "") */
extern const LPCTSTR szESMTPDSNOptions[];

/* Note: empty/NULL <=> ESMTP_DSN_RETNONE */
extern const ESMTPDSNRETCASE esmtpXlateDSNReturnOption (LPCTSTR lpszOpt, const UINT32 ulOptLen);

#ifdef __cplusplus
inline const ESMTPDSNRETCASE esmtpXlateDSNReturnOption (LPCTSTR lpszOpt)
{
	return esmtpXlateDSNReturnOption(GetSafeStrPtr(lpszOpt), GetSafeStrlen(lpszOpt));
}
#endif	/* of __cplusplus */

/* Note: returns NULL if error, _T("") if ESMTP_DSN_RETNONE */
extern const LPCTSTR esmtpGetDSNReturnOption (const ESMTPDSNRETCASE eRetOp);

extern EXC_TYPE esmtpGetDSNReturnOptionString (const ESMTPDSNRETCASE eRetOp, LPTSTR lpszOpt, const UINT32 ulOptLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE esmtpSetDSNSender (ISockioInterface&		ISock,
											  LPCTSTR					lpszSndr,	// may be NULL/empty
											  LPCTSTR					lpszENVID,	// may be NULL/empty
											  const ESMTPDSNRETCASE	eRetCase,	// may be NONE
											  UINT32&					rcode,
											  LPTSTR						lpszRspBuf,
											  const UINT32				ulMaxBufLen,
											  const UINT32				ulRspTimeout);

extern EXC_TYPE esmtpSetDSNSender (ISockioInterface&		ISock,
											  LPCTSTR					lpszSndr,	// may be NULL/empty
											  LPCTSTR					lpszENVID,	// may be NULL/empty
											  const ESMTPDSNRETCASE	eRetCase,	// may be NONE
											  UINT32&					rcode,
											  const UINT32				ulRspTimeout);

extern EXC_TYPE esmtpAddDSNRecipient (ISockioInterface&				ISock,
												  LPCTSTR							lpszRecip,	// may be NULL/empty
												  const ESMTPDSNNOTIFYOPTS&	ntfyOpts,
												  LPCTSTR							lpszORCPT,	// may be NULL/empty
												  UINT32&							rcode,
												  LPTSTR								lpszRspBuf,
												  const UINT32						ulMaxBufLen,
												  const UINT32						ulRspTimeout);

extern EXC_TYPE esmtpAddDSNRecipient (ISockioInterface&				ISock,
												  LPCTSTR							lpszRecip,	// may be NULL/empty
												  const ESMTPDSNNOTIFYOPTS&	ntfyOpts,
												  LPCTSTR							lpszORCPT,	// may be NULL/empty
												  UINT32&							rcode,
												  const UINT32						ulRspTimeout);
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#endif	/* of _EXT_SMTP_LIB_H_ */
