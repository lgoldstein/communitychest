#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <util/string.h>

/*---------------------------------------------------------------------------*/

/*		Taks a rather freely formatted string representing some address in hex
 * format, and returns it in a more strict format. The routine assumes that
 * "in_haddr" contains an address composed of hex bytes, with possibly "white
 * space" (tabs, spaces) and delimiter(s) from "in-delims". After execution,
 * the address is returned as hex bytes separated by the specified "out-delim".
 *
 * Example: IN -	39-00A231BB-42BD-800000000000-00:A2:33:42:FF:02-00
 *							in_delims="-:"
 *
 *				OUT - 39:00:A2:31:BB:42:BD:80:00:00:00:00:00:00:A2:33:42:FF:02:00
 *							out_delim=':'
 */

#define is_delim(dlm,dsetp)	\
	((dsetp == NULL) ? 0 : (strchr((dsetp),(dlm)) != NULL))

size_t hex_address_str_format (const char in_haddr[],
										 size_t		in_len,
										 const char in_delims[],
										 char			out_haddr[],
										 size_t		out_len,
										 char			out_delim)
{
	size_t ilen=0, olen=0, t;
	const char *ip=in_haddr;
	char *op=out_haddr, last_delim='\0';

	if ((in_haddr == NULL) || (out_haddr == NULL))
		return 0;
	out_haddr[0] = '\0';

	while ((ilen < in_len) && (*ip != '\0') && (olen < out_len))
	{
		/* skip white space */
		for ( ; (isspace((int) *ip)) && (*ip != '\0') && (ilen < in_len); ip++, ilen++);
		if ((*ip == '\0') || (ilen >= in_len))
			break;

		if (is_delim((*ip), in_delims))
		{
			/* make sure that we don't have 2 successive delimiters */
			if (last_delim != '\0')
			{
				out_haddr[0] = '\0';
				return 0;
			}

			last_delim = (*ip);
			ip++;
			ilen++;
			continue;
		}

		/* add delimiter if such specified and not 1st value */
		if ((olen > 0) && (out_delim != '\0'))
		{
			*op = out_delim;
			op++;
			olen++;
			if (olen >= out_len)
				break;
		}

		/* copy 2 successive HEX digits */
		for (t = 0; (t < 2) && (olen < out_len); olen++, op++, ip++, t++)
		{
			/* this must be a HEX digit */
			if (!isxdigit((int) (*ip)))
			{
				out_haddr[0] = '\0';
				return 0;
			}

			*op = *ip;
		}

		if (olen >= out_len)
			break;

		last_delim = '\0';
	}

	*op = '\0';
	return olen;
}

/*---------------------------------------------------------------------------*/

#define ADDR_FORMAT_MODIFIER '%'
#define VAL_LEN 2

static int add_addr_component (const char **inp2p,
										 size_t		*ilenp,
										 size_t		in_len,
										 char			in_delim,
										 size_t		vnum,
										 char			**outp2p,
										 size_t		*olenp,
										 size_t		out_len,
										 char			idlm)
{
	const char *inp=*inp2p;
	char *outp=*outp2p;
	size_t vdx, ddx;

	for (vdx = 0;
		  (vdx < vnum) && ((*ilenp) < in_len) && ((*olenp) < out_len);
		  vdx++)
	{
		for (ddx = 0;
			  (ddx < VAL_LEN) && ((*ilenp) < in_len) && ((*olenp) < out_len);
			  ddx++, inp++, (*ilenp)++, outp++, (*olenp)++)
			if (!isxdigit((int) *inp))
				return (-1);
			else
				*outp = *inp;

			/* make sure expected delimiter is there */
		if ((in_delim != '\0') && (*inp != in_delim) && (*inp != '\0'))
			return (-1);

		inp++;
		(*ilenp)++;

		if (idlm == '\0') continue;

		/* add internal delimiter (if any) provided this is not the last value */
		if (vdx < (vnum-1))
		{
			if ((*olenp) >= out_len)
				return (-1);

			*outp = idlm;
			outp++;
			(*olenp)++;
		}
	}

	if (vdx < vnum)
		return (-1);

	*inp2p = inp;
	*outp2p = (outp-1);
	(*olenp)--;
	return 0;
}

/*---------------------------------------------------------------------------*/
/*		Formats the "in-addr" address string according to the "out-format"
 * pattern. The "in-addr" string is assumed to contain HEX bytes representing
 * an address, and separated by "in-delim" (if '\0' then no delimiter is used).
 * The address is re-formatted according to the "out-format" as follows:
 *
 *		- the '%' modifier is used to specify format modifier(s). Any text
 *		preceding this modifier is copied to the output string "as-is" (use
 *		"%%" for the '%' char)
 *
 *		- modifiers are of the form [delim]<len> - where:
 *
 *				len - the number of HEX bytes which form this address component
 *				delim - is an optional delimiter to separate each one of the
 *					<len> bytes for this address component.
 *
 *		Example:	in="39:00:33:A2:00:12:34:25:00:AA:00:32:25:1F:01"
 *					format="%1-%7-%:6-%1"
 *
 *					out="39-0033A200123425-00:AA:00:32:25:1F-01"
 *
 *		- if format is NULL or empty then input is copied to output "as-is"
 *
 *		- if format does not "cover" all values in "in-addr" then only the
 *		"covered" values are used. Alternatively, if more values are required
 *		in the format than available, then processing is aborted.
 *
 *	Returns number of characters in "out-addr" (i.e. len of "out-addr").
 */
/*---------------------------------------------------------------------------*/

size_t hex_address_format (const char	in_addr[],
									size_t		in_len,
									char			in_delim,
									char			out_addr[],
									size_t		out_len,
									const char	out_format[])
{
	size_t ilen=0, olen=0, tln;
	const char *inp=in_addr, *fp=out_format;
	char *outp=out_addr, idlm='\0', slen[32], *sp=slen;

	if ((out_addr == NULL) || (out_format == NULL))
		return 0;
	out_addr[0] = '\0';

	if (in_addr == NULL)
		return 0;

	/* if empty/NULL format copy input to output */
	if ((out_format == NULL) || (out_format[0] == '\0'))
	{
		if (in_len > out_len)
			return 0;
		strcpy(out_addr, in_addr);
		out_addr[in_len] = '\0';
		return in_len;
	}

	for (olen=0; olen < out_len; olen++, outp++)
	{
		*outp = '\0';

		/* no more formatting */
		if (*fp == '\0')
			return olen;

		if (*fp != ADDR_FORMAT_MODIFIER)
		{
			*outp = *fp;
			fp++;
			continue;
		}

		fp++;
		/* special case - "%%" */
		if (*fp == ADDR_FORMAT_MODIFIER)
		{
			*outp = ADDR_FORMAT_MODIFIER;
			fp++;
			continue;
		}

		/* if delimiter preceding the length then store it */
		if (!isdigit((int) *fp))
		{
			idlm = *fp;
			fp++;
		}
		else
			idlm = '\0';

		/* length must be specified */
		if (!isdigit((int) *fp))
		{
			out_addr[0] = '\0';
			return 0;
		}

		/* retrieve num of HEX bytes to copy */
		memset(slen, 0, (sizeof slen));
		for (tln = 0, sp = slen;
			  (tln < (sizeof slen)) && (isdigit((int) *fp)) && (*fp != '\0');
			  tln++, sp++, fp++)
			*sp = *fp;
		slen[(sizeof slen)-1] = '\0';

		tln = (size_t) strtoul(slen, &sp, 10);
		if (*sp != '\0')
		{
			out_addr[0] = '\0';
			return 0;
		}

		if (add_addr_component(&inp, &ilen, in_len, in_delim, tln, &outp, &olen, out_len, idlm) == (-1))
		{
			out_addr[0] = '\0';
			return 0;
		}
	}

	out_addr[olen] = '\0';
	return olen;
}

/*---------------------------------------------------------------------------*/
