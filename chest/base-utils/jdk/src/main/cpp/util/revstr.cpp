
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/
/*                            reverse_chars
 *                            -------------
 *    Reverses the ORDER of the characters in the array.
 */
/*---------------------------------------------------------------------------*/

void reverse_chars (char  str[], size_t slen)
{
   char  *s, *d, tch;
   size_t sndx, dndx;

   s = str;
   d = (str + slen -1);

   for (sndx = 0, dndx = (slen-1);
        (sndx < dndx) && (s != d);
        sndx++, dndx--, s++, d--)
   {
      tch = *s;
      *s = *d;
      *d = tch;
   }
}

/*---------------------------------------------------------------------------*/
