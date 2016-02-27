
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/

#define MSDIGIT_MASK       0xF000
#define MSDIGIT_SHIFT      12

/*---------------------------------------------------------------------------*/

size_t concat_word (char  arg[], WORD  word_value, size_t max_arg_length)
{
   size_t   sndx=0, ndx=0;

	if (NULL == arg)
		return 0;

   /*
    *    Find greatest power of 10 contained in the number.
    */

   for (ndx=0; ndx < MAX_WORD_10_POWERS; ndx++)
      if (word_10_powers[ndx] > word_value) break;

   if (ndx == 0)
		ndx++;

   /*
    *    At this point "ndx" contains the greatest power of 10 that is
    * GREATER than the number. It is also exactly LOG10(number).
    */

   for (sndx=strlen((const char *) arg);
        (ndx > 0) && (sndx < max_arg_length);
        ndx--, sndx++)
   {
      arg[sndx] = (char) ((word_value/word_10_powers[ndx-1])+'0');
      word_value = (WORD) (word_value % word_10_powers[ndx-1]);
   }

   /*
    *    At this point "sndx" is the argument's length.
    */

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/

size_t concat_hex_word (char arg[], WORD word_value, size_t max_arg_length)
{
   size_t   sndx=0, ndx=0;

	if (NULL == arg)
		return 0;

   for (sndx = strlen((const char *) arg), ndx = 0;
        (ndx < MAX_WORD_HEX_DISPLAY_LENGTH) && (sndx < max_arg_length);
        sndx++, ndx++)
   {
      arg[sndx] =
         hex_digits_chars[((WORD) (word_value & MSDIGIT_MASK) >> MSDIGIT_SHIFT)];
      word_value = (WORD) (word_value << 4);
   }

   arg[sndx] = '\0';
   return(sndx);
}

/*---------------------------------------------------------------------------*/
