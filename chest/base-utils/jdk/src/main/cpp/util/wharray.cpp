#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

size_t formatted_string_to_word_array (const char		str_in[],
													const size_t	str_len,
													WORD				out_array[],
													const size_t	out_len,
													const char     delim,
													const BOOLEAN	isHex,
													EXC_PARAM(exc))
{
	WORD		  *bp=out_array;
	const char *strp=str_in, *tsp=NULL;
	size_t	  len=0, sdx=0, tLen=0;

	EXC_VAL(exc, EOK);
	if (0 == str_len)
		return 0;

	if ((out_array == NULL) || (str_in == NULL))
	{
		EXC_VAL(exc, EEMPTYENTRY);
		return(0);
	}

	if ((!isHex) && ('\0' == delim))
	{
		EXC_VAL(exc,EPARAM);
		return 0;
	}

	for ( ; (len < out_len) && (sdx < str_len); bp++, len++)
	{
		if (delim != '\0')
		{
			if (isHex)
				tsp = strskipxnum(strp);
			else
				tsp = strskipnum(strp);
		}
		else	// this can occur ONLY for HEX format
			tsp = (strp + MAX_WORD_HEX_DISPLAY_LENGTH);

		tLen = (size_t) (tsp-strp);
		if ((sdx + tLen) > str_len)
			break;

		*bp = isHex ? hex_argument_to_word(strp, (size_t) (tsp-strp), exc) : argument_to_word(strp, (size_t) (tsp-strp), exc);
		if (EXC_LVAL(exc) != EOK)
			return(len);

		if (delim != '\0')
		{
			strp = strchr(strp, delim);
			if (strp != NULL)
				strp += (1+strskipwspace((strp+1)));
		}
		else
			strp = (tsp+1);

		if (NULL == strp)
			sdx = str_len;	/* force loop exit */
		else if ('\0' == *strp)
			sdx = str_len;	/* force loop exit */
		else
			sdx = (size_t) (strp - str_in);
	}

	return(len);
}

/*---------------------------------------------------------------------------*/

#ifndef __cplusplus	/* for C++ we use inline functions */
size_t hex_string_to_word_array (const char		str_in[],
											const size_t	str_len,
											WORD				out_array[],
											const size_t	out_len,
											const char		delim,
											EXC_PARAM(exc))
{
	return formatted_string_to_word_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

size_t string_to_word_array (const char	str_in[],
									  const size_t	str_len,
									  WORD			out_array[],
									  const size_t	out_len,
									  const char	delim,
									  EXC_PARAM(exc))
{
	return formatted_string_to_word_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/
