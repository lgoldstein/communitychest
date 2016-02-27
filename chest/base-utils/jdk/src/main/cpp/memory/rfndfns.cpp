#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 *    These functions return the FIRST position of the requested value within
 * the target array. If value was not found, then a value greater or equal to
 * the array's length is returned.
 *    The functions ending with "r" search starting at the array's END (the
 * returned values, however, still refer to position relative to start of
 * array).
 */

/*---------------------------------------------------------------------------*/

size_t find_byter (const BYTE buf[], BYTE value, size_t count)
{
   const BYTE  *b_p;
   size_t 		idx;

   for (idx=count, b_p=(buf+count-1); idx > 0; idx--, b_p--)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t find_wordr (const WORD buf[], WORD value, size_t count)
{
   const WORD  *b_p;
   size_t 		idx;

   for (idx=count, b_p=(buf+count-1); idx > 0; idx--, b_p--)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/

size_t find_dwordr (const DWORD buf[], DWORD value, size_t count)
{
   const DWORD *b_p;
   size_t 		idx;

   for (idx=count, b_p=(buf+count-1); idx > 0; idx--, b_p--)
      if (*b_p == value) return(idx);

   return(count);
}

/*---------------------------------------------------------------------------*/
