
/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <string.h>
#include <ctype.h>
#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

/*
 * Holds the maximum digits allowed at each position of the maximum value
 */

static const char max_value_digits[]={ '2', '5', '5' , '\0' };

/*
 *    This functions checks if maximum value exceeded in case the number of
 * digits is the maximum allowed (e.g. for BYTE arguments we allow up to 3
 * digits, however a value such as "267" is not a legal BYTE value).
 *
 *    Returns FALSE if maximum value exceeded
 */

static BOOLEAN check_max_value (const char arg_p[], size_t arg_len)
{
   size_t idx;

   /*
    * Zero length or too many digits
    */

   if ((NULL == arg_p) || (0 == arg_len) || (arg_len > MAX_BYTE_10_POWERS))
      return(FALSE);

   /*
    *    If number of digits less than maximum allowed - no need to check
    * because the maximum allowed value cannot be exceeded anyway.
    */

   if (arg_len < MAX_BYTE_10_POWERS)
      return(TRUE);

   for (idx=0; idx < MAX_BYTE_10_POWERS; idx++)
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

BYTE argument_to_byte (const char arg_p[], size_t arg_len, EXC_PARAM(exc))
{
   char     ch;
   BYTE     byte_val;
	size_t	cndx, pndx;
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

   byte_val = 0;
   for (cndx=0, pndx=arg_len-1; cndx < arg_len; cndx++, pndx--)
   {
      ch = arg_p[cndx];
      if (ch == '\0') break;

      if (!isdigit((int) ch))
      {
         *exc_p = EPARAM;
         return(0);
      }

      byte_val += (BYTE) ((ch - '0') * byte_10_powers[pndx]);
   }

   return(byte_val);
}

/*---------------------------------------------------------------------------*/

BYTE hex_argument_to_byte (const char arg_p[],  size_t arg_len, EXC_PARAM(exc))
{
   BYTE        byte_val=0;
	size_t 		cndx=0, pndx=0;
   char        ch='\0';
	EXC_TYPE    err=EOK, *exc_p=&err;

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

   if ((0 == arg_len) || (arg_len > MAX_BYTE_HEX_DISPLAY_LENGTH))
   {
      *exc_p = EOVERFLOW;
      return(0);
   }

   /*
    *    cndx - points to current digit to be translated.
    *    pndx - points to current power of 16 that the digit represents.
    */

   byte_val = 0;
   for (cndx=0, pndx=(size_t) (arg_len-1) << 2;
        (cndx < arg_len);
        cndx++, pndx-=4)
   {
      ch = arg_p[cndx];
      if (ch == '\0')
         return(byte_val);

      if (!isxdigit((int)ch))
      {
         *exc_p = EPARAM;
         return(0);
      }

      if (isdigit((int)ch))
      {
         byte_val += (BYTE) ((BYTE) (ch - '0') << pndx);
         continue;
      }

      /*
       *	Reaching this point means that it is a character 'a'/'A' - 'f'/'F'
       */
      byte_val += (BYTE) ((BYTE) ((BYTE) (tolower((int) ch) - 'a')+10) << pndx);
   }

   return(byte_val);
}

/*---------------------------------------------------------------------------*/

BYTE arg2b (const char val[], size_t arg_len, EXC_PARAM(exc))
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
		return(argument_to_byte(val, arg_len, exc));
	if (tolower((int) val[1]) == 'x')
		return(hex_argument_to_byte(&val[2], arg_len-2, exc));
	return(argument_to_byte(val, arg_len, exc));
}

/*---------------------------------------------------------------------------*/
