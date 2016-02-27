#include <string.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

size_t byte_array_to_formatted_string (const BYTE		array_in[],
													const size_t	array_len,
													char				out_string[],
													const size_t	out_len,
													const char		delim,
													const BOOLEAN	fUseHex)
{
	const BYTE *bp=array_in;
	char		  *outp=out_string;
	size_t	  ndx, len=0, rlen=out_len, tln=0;

	*outp = '\0';
	for (ndx = 0;((ndx < array_len) && (len < out_len) && (rlen>0)); ndx++, bp++)
	{
		if ((delim != '\0') && (ndx > 0))
		{
			outp = strladdch(outp, delim);
			len++;
			rlen--;
		}

		if (len >= out_len)
		{
			out_string[out_len] = '\0';
			return(out_len);
		}

		tln = fUseHex ? concat_hex_byte(outp, *bp, rlen) : concat_byte(outp, *bp, rlen);

		len += tln;
		outp += tln;
		rlen -= tln;

		if (len >= out_len)
		{
			out_string[out_len] = '\0';
			return(out_len);
		}
	}

	*outp = '\0';
	return(len);
}

/*---------------------------------------------------------------------------*/

#ifndef __cplusplus	/* for C++ we use inline functions */
size_t byte_array_to_hex_string (const BYTE		array_in[],
											const size_t	array_len,
											char				out_string[],
											const size_t	out_len,
											const char		delim)
{
	return byte_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, TRUE);
}

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
size_t byte_array_to_string (const BYTE	array_in[],
									  const size_t	array_len,
									  char			out_string[],
									  const size_t	out_len,
									  const char	delim)
{
	return byte_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, FALSE);
}
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/
