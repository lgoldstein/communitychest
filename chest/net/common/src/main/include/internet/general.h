#ifndef _INET_GENERAL_H_
#define _INET_GENERAL_H_

#include <_types.h>

/*---------------------------------------------------------------------------*/
/* this header files defines several useful constants, types and utilities for
 * dealing with Internet protocols (e.g. reading ASCII based commands from a
 * connection).
 */
/*---------------------------------------------------------------------------*/

#define MAX_DNS_DOMAIN_LEN 64
#define MAX_USER_PART_LEN	64

/* e-mail address length - including "<>" delimiters */
#define MAX_EMAIL_ADDR_LEN				(MAX_USER_PART_LEN + MAX_DNS_DOMAIN_LEN + 1 + 2)

/* max personal/display name length - including quote(s) */
#define MAX_PERSONAL_EMAIL_NAMELEN	(MAX_EMAIL_ADDR_LEN+2)

/* maximum full address pair length - including separating space(s) */
#define MAX_ADDRPAIR_EMAIL_LEN	(MAX_EMAIL_ADDR_LEN+MAX_PERSONAL_EMAIL_NAMELEN+2)

/* separator between user and domain */
#define INET_DOMAIN_SEP		_T('@')

/* separator between IP address components */
#define INET_ADDRESS_SEP	_T('.')

#define MAX_RCVR_NAME_LEN	MAX_EMAIL_ADDR_LEN
#define MAX_SNDR_NAME_LEN	MAX_RCVR_NAME_LEN

/*---------------------------------------------------------------------------*/

/* delimiters used for email addresses */
#define EMAIL_PATH_SDELIM	_T('<')
#define EMAIL_PATH_EDELIM	_T('>')

/*---------------------------------------------------------------------------*/

/* processing time allowed for server before retrieving data */
#define MAX_ISRV_DPROC_TIME(ulRspTimeout) ((ulRspTimeout) * 4)

/*
 *		This function is required because the library function "inet_addr" has a
 * bug - it does not handle correctly number which exceed 256 in a given member
 * (e.g. it says "OK" for "192.700.3.200").
 */

extern unsigned long inet_dot_str2address (const char dot_str[]);

/*
 *		This function converts a string defining a port number to its matching
 * value. The port number string may contain either a number (e.g. "1234") or
 * the name of a service (e.g. "telnet"). If a service name is specified then
 * "getservbyname" is used to query for the port.
 *
 *	  If unsuccessful then function returns (-1)
 */

extern int port_string2value (const char pstr[]);

/*		Resolves named service port. If provided port is zero, then query is
 * executed using the service name. If that fails, then the default port is returned.
 */
extern int resolve_service_port (const char	szSrvcName[],
										   const int	iCurPort,
											const int	iDefPort);

/*---------------------------------------------------------------------------*/

#define INET_WPAT_MODIFIER		_T('%')
#define INET_WPAT_TYPE			_T('T')
#define INET_WPAT_VERSION		_T('V')
#define INET_WPAT_IGNORE		_T('I')
#define INET_WPAT_RFC822DATE	_T('D')
#define INET_WPAT_SKIPTODELIM	_T('*')	/* must be followed by a delimiter: e.g. "%*(" */

/*	Welcome patterns definitions:
 *
 *	a. all spaces are ignore as far as comparison goes
 *
 *	b. a "string" is defined as any sequence of non-space characters
 *
 *	c. modifiers are:
 *
 *		%T - server type name
 *		%V - server version string
 *
 *			Note: if %T or %V appears more than once than the values are
 *					concatenated with a SPACE between them.
 *
 *		%I - ignore string
 *
 *			Note: if this is the last modifier, then rest of welcome is
 *					ignored.	Otherwise, this is a placeholder for a single
 *					string argument.
 *
 *		%D - match an RFC822 date/time
 *		%% - '%' itself
 */
extern EXC_TYPE inetAnalyzeWelcomePattern (LPCTSTR			lpszWelcome,
														 LPCTSTR			lpszWPattern,
														 LPTSTR			lpszType,
														 const UINT32	ulMaxTypeLen,
														 LPTSTR			lpszVersion,
														 const UINT32	ulMaxVerLen);

typedef EXC_TYPE (*INETPROTO_WELCOME_ANCFN)(LPCTSTR		lpszWelcome,
														  LPCTSTR		lpszWPattern,
														  LPTSTR			lpszType,
														  const UINT32	ulMaxTypeLen,
														  LPTSTR			lpszVersion,
														  const UINT32	ulMaxVerLen);

/* go over known patterns - returns EIOUNCLASS if no match found */
extern EXC_TYPE inetAnalyzeWelcome (LPCTSTR						lpszWelcome,
												LPCTSTR						lpszPatterns[],
												INETPROTO_WELCOME_ANCFN	lpfnAcfn,
												LPTSTR						lpszType,
												const UINT32				ulMaxTypeLen,
												LPTSTR						lpszVersion,
												const UINT32				ulMaxVerLen);

/* go over known patterns - returns EIOUNCLASS if no match found */
typedef EXC_TYPE (*INETPROTO_WPATS_ANCFN)(LPCTSTR					lpszWelcome,
														LPCTSTR					lpszPatterns[],
														LPTSTR					lpszType,
														const UINT32			ulMaxTypeLen,
														LPTSTR					lpszVersion,
														const UINT32			ulMaxVerLen);

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _INET_GENERAL_H_ */
