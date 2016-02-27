#include <string.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

size_t word_array_to_formatted_string (const WORD		array_in[],
													const size_t	array_len,
													char				out_string[],
													const size_t	out_len,
													const char		delim,
													const BOOLEAN	fUseHex)
{
	const WORD *bp=array_in;
	char		  *outp=out_string;
	size_t	  ndx, len=0, tln=0, rlen=out_len;

	*outp = '\0';
	for (ndx = 0;	((ndx < array_len) && (len < out_len)) ; ndx++, bp++)
	{
		if (delim != '\0')
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

		tln = fUseHex ? concat_hex_word(outp, *bp, rlen) : concat_word(outp, *bp, rlen);
		len += tln;
		rlen -= tln;
		outp += tln;
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
size_t word_array_to_hex_string (const WORD		array_in[],
											const size_t	array_len,
											char				out_string[],
											const size_t	out_len,
											const char		delim)
{
	return word_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, TRUE);
}

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
size_t word_array_to_string (const WORD	array_in[],
									  const size_t	array_len,
									  char			out_string[],
									  const size_t	out_len,
									  const char	delim)
{
	return word_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, FALSE);
}
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/
