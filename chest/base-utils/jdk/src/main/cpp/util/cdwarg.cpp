
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/tables.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

size_t concat_dword (char arg[], DWORD dword_value, size_t max_arg_length)
{
   size_t sndx=0, ndx=0;

	if (NULL == arg)
		return 0;

   /*
    *    Find greatest power of 10 contained in the number.
    */

   for (ndx=0; ndx < MAX_DWORD_10_POWERS; ndx++)
      if (dword_10_powers[ndx] > dword_value) break;

   if (ndx== 0) ndx++;

   /*
    *    At this point "ndx" contains the greatest power of 10 that is
    * GREATER than the number. It is also exactly LOG10(number).
    */

   for (sndx=strlen((const char *) arg);
        (ndx > 0) && (sndx < max_arg_length);
        ndx--, sndx++)
   {
      arg[sndx] = (char) ((dword_value/dword_10_powers[ndx-1])+'0');
      dword_value = (DWORD) (dword_value % dword_10_powers[ndx-1]);
   }

   /*
    *    At this point "sndx" is the argument's length.
    */

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/

#define MSDIGIT_MASK 	0xF0000000
#define MSDIGIT_SHIFT   28

size_t concat_hex_dword (char arg[], DWORD dword_value, size_t max_arg_length)
{
   size_t   sndx=0, ndx=0;

	if (NULL == arg)
		return 0;

   for (sndx = strlen((const char *) arg), ndx = 0;
        (sndx < max_arg_length) && (ndx < MAX_DWORD_HEX_DISPLAY_LENGTH);
        sndx++, ndx++)
   {
      arg[sndx] =
         hex_digits_chars[((DWORD) (dword_value & (DWORD) MSDIGIT_MASK) >> MSDIGIT_SHIFT)];
      dword_value = (DWORD) (dword_value << 4);
   }

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/
