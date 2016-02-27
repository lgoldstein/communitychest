
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/tables.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

#define MSDIGIT_MASK       0xF0
#define MSDIGIT_SHIFT      4

/*---------------------------------------------------------------------------*/

size_t concat_byte (char arg[], BYTE byte_value, size_t max_arg_length)
{
   size_t  ndx=0, sndx=0;

	if (NULL == arg)
		return 0;

   /*
    *    Find greatest power of 10 contained in the number.
    */

   for (ndx=0; ndx < MAX_BYTE_10_POWERS ; ndx++)
      if (byte_10_powers[ndx] > byte_value) break;

   if (ndx==0) ndx++;

   /*
    *    At this point "ndx" contains the greatest power of 10 that is
    * GREATER than the number. It is also the number of digits required
    * to represent the number.
    */

   for (sndx=strlen((const char *) arg);
        (ndx > 0) && (sndx < max_arg_length);
        ndx--, sndx++)
   {
      arg[sndx] = (char) ((byte_value/byte_10_powers[ndx-1])+'0');
      byte_value = (BYTE) (byte_value % byte_10_powers[ndx-1]);
   }

   /*
    *    At this point "sndx" is the argument's length.
    */

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/

size_t concat_hex_byte (char arg[], BYTE byte_value, size_t max_arg_length)
{
   size_t  sndx=0, ndx=0;

	if (NULL == arg)
		return 0;

   for (sndx = strlen((const char *) arg), ndx =0;
        (ndx < MAX_BYTE_HEX_DISPLAY_LENGTH) && (sndx < max_arg_length);
        sndx++, ndx++)
   {
      arg[sndx] =
         hex_digits_chars[((BYTE) (byte_value & MSDIGIT_MASK) >> MSDIGIT_SHIFT)];
      byte_value = (BYTE) (byte_value << MSDIGIT_SHIFT);
   }

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/
