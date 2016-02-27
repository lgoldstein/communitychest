#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 *    These functions return the index of the first value which is NOT the
 * given value (i.e they SKIP OVER the value). If ALL values equal the given
 * one, a value greater or equal to the array's length is returned.
 */

/*---------------------------------------------------------------------------*/

size_t skip_byte (const BYTE buf[], BYTE value, size_t count)
{
   const BYTE  *b_p=buf;
   size_t 		idx;

   for (idx=0; idx < count; idx++, b_p++)
      if (*b_p != value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t skip_word (const WORD buf[], WORD value, size_t count)
{
   const WORD  *b_p=buf;
   size_t 		idx;

   for (idx=0; idx < count; idx++, b_p++)
      if (*b_p != value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t skip_dword (const DWORD buf[], DWORD value, size_t count)
{
   const DWORD *b_p=buf;
   size_t 		idx;

   for (idx=0; idx < count; idx++, b_p++)
      if (*b_p != value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/
