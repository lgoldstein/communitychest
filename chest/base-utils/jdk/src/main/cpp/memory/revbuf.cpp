#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 * reverse the byte/word/dword-s order of an array.
 */

/*---------------------------------------------------------------------------*/

void reverse_bytes (BYTE buf[], size_t buf_len)
{
   size_t hi, lo;
   BYTE   aux, *l_p, *h_p;

   if (buf_len == 0) return;

   for (lo=0, hi=(buf_len-1), l_p=buf, h_p=(buf+buf_len-1);
        lo < hi;
        lo++, hi--, l_p++, h_p--)
   {
      aux = *l_p;
      *l_p = *h_p;
      *h_p = aux;
   }
}

/*---------------------------------------------------------------------------*/

void reverse_words (WORD buf[], size_t buf_len)
{
   size_t hi, lo;
   WORD  aux, *l_p, *h_p;

   if (buf_len == 0) return;

   for (lo=0, hi=(buf_len-1), l_p=buf, h_p=(buf+buf_len-1);
        lo < hi;
        lo++, hi--, l_p++, h_p--)
   {
      aux = *l_p;
      *l_p = *h_p;
      *h_p = aux;
   }
}

/*---------------------------------------------------------------------------*/

void reverse_dwords (DWORD  buf[], size_t buf_len)
{
   size_t hi, lo;
   DWORD  aux, *l_p, *h_p;

   if (buf_len == 0) return;

   for (lo=0, hi=(buf_len-1), l_p=buf, h_p=(buf+buf_len-1);
        lo < hi;
        lo++, hi--, l_p++, h_p--)
   {
      aux = *l_p;
      *l_p = *h_p;
      *h_p = aux;
   }
}

/*---------------------------------------------------------------------------*/
