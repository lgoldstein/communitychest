#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 * These functions return the position of the first different array member
 * between the source and destination. If they are equal it returns a value
 * greater or equal to length (NOTE: there exists a more general "memcmp" fns.)
 */

/*---------------------------------------------------------------------------*/

size_t compare_bytes (const BYTE src[], const BYTE dst[], size_t length)
{
   const BYTE 	*s_p=src, *d_p=dst;
   size_t 		idx;

   for (idx=0; idx < length; idx++, s_p++, d_p++)
      if (*s_p != *d_p) return(idx);

   return(length);
}

/*---------------------------------------------------------------------------*/

size_t compare_words (const WORD src[], const WORD dst[], size_t length)
{
   const WORD  *s_p=src, *d_p=dst;
   size_t 		idx;

   for (idx=0; idx < length; idx++, s_p++, d_p++)
      if (*s_p != *d_p) return(idx);

   return(length);
}

/*---------------------------------------------------------------------------*/

size_t compare_dwords (const DWORD src[], const DWORD dst[], size_t length)
{
   const DWORD *s_p=src, *d_p=dst;
   size_t 		idx;

   for (idx=0; idx < length; idx++, s_p++, d_p++)
      if (*s_p != *d_p) return(idx);

   return(length);
}

/*---------------------------------------------------------------------------*/
