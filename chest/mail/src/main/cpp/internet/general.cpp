#include <string.h>
#include <ctype.h>

/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <util/string.h>

#include <internet/general.h>

/*---------------------------------------------------------------------------*/

/*
 *		This function is required because the library function "inet_addr" has a
 * bug - it does not handle correctly number which exceed 256 in a given member
 * (e.g. it says "OK" for "192.700.3.200").
 */

unsigned long inet_dot_str2address (const char dot_str[])
{
	const char	*tsp=NULL, *lsp=dot_str;
	EXC_TYPE	exc=EOK;
	UINT8		cnum;

	if (NULL == dot_str)
		return((unsigned long) (-1));

	for (tsp=strchr(dot_str,'.'), cnum=0;
		  (tsp != NULL) && (lsp != NULL);
		  cnum++)
	{
		arg2b(lsp, (size_t) (tsp-lsp), EXC_ARG(exc));
		if (exc != EOK)
			return((unsigned long) (-1));

		if (*tsp != '\0')
		{
			lsp = (tsp+1);
			if ((tsp=strchr(lsp, '.')) == NULL)
				tsp = strlast(lsp);
		}
		else
		{
			tsp = NULL;
			lsp = NULL;
		}
	}

	/* make sure address contains 4 elements */
	if (cnum != 4)
		return ((unsigned long) (-1));

	/*
	 *		If this point is reached then all components are ok
	 */

	 return(inet_addr(dot_str));
}

/*---------------------------------------------------------------------------*/

/*
 *		This function converts a string defining a port number to its matching
 * value. The port number string may contain either a number (e.g. "1234") or
 * the name of a service (e.g. "telnet"). If a service name is specified then
 * "getservbyname" is used to query for the port.
 *
 *	  If unsuccessful then function returns (-1)
 */

int port_string2value (const char pstr[])
{
	if (IsEmptyStr(pstr))
		return (-1);

	if (isdigit((int) pstr[0]))
	{
		EXC_TYPE	exc=EOK;
		int		s_port=(int) arg2dw(pstr, strlen(pstr), EXC_ARG(exc));

		if (exc == EOK)
			return s_port;
		else
			return (-1);
	}
	/*
	else
	{
		struct servent srvrec, *srvp=NULL;
		char				srvbuf[sizeof(struct servent)+128];

		memset(srvbuf, 0, (sizeof srvbuf));
		srvp = getservbyname_r(pstr, NULL, &srvrec, srvbuf, (sizeof srvbuf));
		if (srvp != NULL)
			return(htons(srvp->s_port));
	}
	*/
	/*
	 * This point is reached only if unable to translate correctly the string
	 */

	return(-1);
}

/*---------------------------------------------------------------------------*/

int resolve_service_port (const char	szSrvcName[],
								  const int		iCurPort,
								  const int		iDefPort)
{
	int	iConnPort=iCurPort;

	/* should be commented out if no support for "getservbyname" */
	if (iConnPort <= 0)
		iConnPort = port_string2value(szSrvcName);
	if (iConnPort <= 0)
		iConnPort = iDefPort;

	return iConnPort;
}

/*---------------------------------------------------------------------------*/
