#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 *    These functions return the FIRST position of the requested value within
 * the target array. If value was not found, then a value greater or equal to
 * the array's length is returned.
 */

/*---------------------------------------------------------------------------*/

size_t find_byte (const BYTE buf[], BYTE value, size_t count)
{
   const BYTE  *b_p=buf;
   size_t 		idx;

   for (idx = 0; idx < count; idx++, b_p++)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t find_word (const WORD buf[], WORD value, size_t count)
{
   const WORD	*b_p=buf;
   size_t 		idx;

   for (idx = 0; idx < count; idx++, b_p++)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t find_dword (const DWORD buf[], DWORD value, size_t count)
{
   const DWORD *b_p=buf;
   size_t 		idx;

   for (idx = 0; idx < count; idx++, b_p++)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/
