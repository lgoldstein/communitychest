#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>
#include <util/errors.h>

static size_t v2a (UINT8 val, char **arg_p2p, size_t *tot)
{
	char *arg_p=*arg_p2p;
	size_t l=0, tln;

	if (val < 10)
	{
		arg_p = strladdch(arg_p, '0');
		l++;
	}

	tln = byte_to_argument(val, arg_p);
	*arg_p2p = (arg_p+tln);
	*tot += (l+tln);
	return (l+tln);
}

/*
 *		Converts a version/release number to a string. The version structure is
 * assumed to be <major>.<minor>.<release> (example: 01.03.06)) - the
 * <release> is optional.
 *
 * Returns created string length
 */
size_t ver2arg (UINT8 majorNum, UINT8 minorNum, UINT8 rlsNum,
					 char relCh,char arg[])
{
	size_t l=0, tln;
	char *arg_p=arg;

	*arg_p = '\0';

	tln = v2a(majorNum, &arg_p, &l);
	arg_p = strladdch(arg_p, VERSEP_CHAR);
	l++;

	tln = v2a(minorNum, &arg_p, &l);
	arg_p = strladdch(arg_p, VERSEP_CHAR);
	l++;

	tln = v2a(rlsNum, &arg_p, &l);

	return l;
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE a2v (const char **tsp_p2p,
#ifdef __cplusplus
							UINT8&		val
#else
							UINT8			*val
#endif
	)
{
	EXC_TYPE exc=EOK;
	const char *tsp, *arg=*tsp_p2p;
	size_t l=0;

	if ((arg == NULL) || (*arg == '\0'))
		return EEMPTYENTRY;

	if ((tsp=strchr(arg, VERSEP_CHAR)) != NULL)
	{
		l = (tsp - arg);
		tsp++;
	}
	else
		l = strlen(arg);

#ifdef __cplusplus
	val = argument_to_byte(arg, l, exc);
#else
	*val = argument_to_byte(arg, l, &exc);
#endif
	*tsp_p2p = tsp;
	return exc;
}

/*---------------------------------------------------------------------------*/

EXC_TYPE arg2ver (const char arg[],
#ifdef __cplusplus
						UINT8& majorNum, UINT8& minorNum, UINT8& rlsNum,char& relCh
#else
						UINT8 *majorNum, UINT8 *minorNum, UINT8 *rlsNum,char *relCh
#endif /* of C++ */
)
{
	EXC_TYPE exc=EOK;
	const char *tsp=arg;

	if (arg == NULL)
		return EEMPTYENTRY;

#ifndef __cplusplus
	if ((majorNum == NULL) || (minorNum == NULL) ||
		 (rlsNum == NULL) || (relCh == NULL))
		return EEMPTYENTRY;
	*majorNum = 0;
	*minorNum = 0;
	*rlsNum = 0;
	*relCh = '\0';
#else
	majorNum = 0;
	minorNum = 0;
	rlsNum = 0;
	relCh = '\0';
#endif

	if (((exc=a2v(&tsp, majorNum)) != EOK) || (tsp == NULL) || (*tsp == '\0'))
		return exc;
	if (((exc=a2v(&tsp, minorNum)) != EOK) || (tsp == NULL) || (*tsp == '\0'))
		return exc;
	if (((exc=a2v(&tsp, rlsNum)) != EOK) || (tsp == NULL) || (*tsp == '\0'))
		return exc;

	return EOK;
}
