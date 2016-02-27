#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/errors.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/
/* Returns pointer to exception code string, or NULL if not found
 */
/*---------------------------------------------------------------------------*/

const char *exc_code2string (const EXC_TYPE exc)
{
   DWORD idx=0;

   for (; (exception_codes[idx] <= exc) && (exception_strings[idx] != NULL); idx++);

   if (exception_codes[idx] != exc)
		return "EXC-???";
   else
		return(exception_strings[idx]);
}

/*---------------------------------------------------------------------------*/
/*                      dqdecodeexception
 *                      -----------------
 *    Returns a string explaining the value encoded in the "exc" parameter.
 * Function returns length of returned string.
 */
/*---------------------------------------------------------------------------*/

size_t dqdecodeexception (const EXC_TYPE exc, char *str)
{
	const char *exc_strp=exc_code2string(exc);

	/* not found */
   if (exc_strp == NULL)
	{
      strcpy(str, "E?");
      return(concat_hex_word(str, (WORD) exc, MEDIUM_STRING_LENGTH));
   }
	else
	{
		strcpy(str, exc_strp);
		return(strlen(str));
	}
}

/*---------------------------------------------------------------------------*/

EXC_TYPE dqencodeexception (const char *exc_str)
{
	DWORD	idx=0;

	while (exception_strings[idx][0] != '\0')
		if (strcmp(exc_str, exception_strings[idx]) == 0)
			return(exception_codes[idx]);
		else
			idx++;

	return((EXC_TYPE) 0xFFFF);
}

/*---------------------------------------------------------------------------*/
