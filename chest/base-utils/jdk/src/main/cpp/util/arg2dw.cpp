
/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <ctype.h>
#include <string.h>
#include <util/errors.h>
#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/

/*
 * Holds the maximum digits allowed at each position of the maximum value
 */

static const char max_value_digits[]={ '4','2','9','4','9','6','7','2','9','6','\0' };

/*    This functions checks if maximum value exceeded in case the number of
 * digits is the maximum allowed (e.g. for WORD arguments we allow up to 5
 * digits, however a value such as "72,346" is not a legal WORD value).
 *
 *    Returns FALSE if maximum value exceeded
 */

static BOOLEAN check_max_value (const char arg_p[], size_t arg_len)
{
   size_t idx;

   /*
    * Zero length or too many digits
    */

   if ((NULL == arg_p) || (arg_len == 0) || (arg_len > MAX_DWORD_10_POWERS))
      return(FALSE);

   /*
    *    If number of digits less than maximum allowed - no need to check
    * because the maximum allowed value cannot be exceeded anyway.
    */

   if (arg_len < MAX_DWORD_10_POWERS)
      return(TRUE);

   for (idx=0; idx < MAX_DWORD_10_POWERS; idx++)
   {
      /*
       *    Since the digits are checked from MSB to LSB, reaching a digit
       * which is GREATER means that value is exceeded, while a LESSER value
       * means that value is below allowed maximum.
       */

      if (arg_p[idx] > max_value_digits[idx])
         return(FALSE);
      if (arg_p[idx] < max_value_digits[idx])
         return(TRUE);
   }

   /*
    * Reaching this point means that everything checks out O.K.
    */

   return(TRUE);
}

/*---------------------------------------------------------------------------*/

DWORD argument_to_dword (const char arg_p[], size_t arg_len, EXC_PARAM(exc))
{
   char     ch;
   size_t   cndx, pndx;
   DWORD    dword_val;
	EXC_TYPE err=EOK, *exc_p=&err;

#ifdef __cplusplus
	exc_p = &exc;
#else
   exc_p = (exc != (EXC_TYPE *) NULL) ? exc : &err;
#endif
   *exc_p = EOK;

	if (NULL == arg_p)
	{
		*exc_p = EBADBUFF;
		return 0;
	}

   if (!check_max_value(arg_p, arg_len))
   {
      *exc_p = EOVERFLOW;
      return(0);
   }

   /*
    *    cndx - points to current digit to be translated.
    *    pndx - points to current power of 10 that the digit represents.
    */

   dword_val = 0;
   for (cndx=0, pndx=arg_len-1; cndx < arg_len; cndx++, pndx--)
   {
      ch = arg_p[cndx];
      if (ch == '\0')
         return(dword_val);

      if (!isdigit((int) ch))
      {
         *exc_p = EPARAM;
         return(0);
      }

      dword_val += (DWORD) ((DWORD) (ch - '0') * (dword_10_powers[pndx]));
   }

   return(dword_val);
}

/*---------------------------------------------------------------------------*/

DWORD hex_argument_to_dword (const char arg_p[], size_t arg_len, EXC_PARAM(exc))
{
   size_t   cndx, pndx;
   DWORD    dword_val;
	char		ch;
	EXC_TYPE err=EOK, *exc_p=&err;

#ifdef __cplusplus
	exc_p = &exc;
#else
   exc_p = (exc != (EXC_TYPE *) NULL) ? exc : &err;
#endif
   *exc_p = EOK;

	if (NULL == arg_p)
	{
		*exc_p = EBADBUFF;
		return 0;
	}

   if ((arg_len == 0) || (arg_len > MAX_DWORD_HEX_DISPLAY_LENGTH))
   {
      *exc_p = EOVERFLOW;
      return(0);
   }

   /*
    *    cndx - points to current digit to be translated.
    *    pndx - points to current power of 16 that the digit represents.
    */

   dword_val = 0;
   for (cndx=0, pndx=(arg_len-1) << 2;
        cndx < arg_len;
        cndx++, pndx-=4)
   {
      ch = arg_p[cndx];
      if (ch == '\0')
         return(dword_val);

      if (!isxdigit((int) ch))
      {
         *exc_p = EPARAM;
         return(0);
      }

      if (isdigit((int) ch))
      {
         dword_val += (DWORD) ((DWORD) (ch - '0') << pndx);
         continue;
      }

      dword_val += (DWORD) ((DWORD) ((tolower((int) ch) - 'a')+10) << pndx);
   }

   return(dword_val);
}

/*---------------------------------------------------------------------------*/

DWORD arg2dw (const char val[], size_t arg_len, EXC_PARAM(exc))
{
	if (NULL == val)
	{
#ifdef __cplusplus
		exc = EBADBUFF;
#else
		if (exc != NULL)
			*exc = EBADBUFF;
#endif
		return 0;
	}

	if (arg_len <= 2)
		return(argument_to_dword(val, arg_len, exc));
	if (tolower((int) val[1]) == 'x')
		return(hex_argument_to_dword(&val[2], arg_len-2, exc));
	return(argument_to_dword(val, arg_len, exc));
}

/*---------------------------------------------------------------------------*/
