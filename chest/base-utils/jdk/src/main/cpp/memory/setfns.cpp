#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 * set_byte/word/dword "smear" the array using the given value (NOTE: there is
 * a more general function - "memset")
 */

/*---------------------------------------------------------------------------*/

void set_byte (BYTE value, BYTE target_buf[], size_t count)
{
   BYTE  *b_p=target_buf;
   size_t idx;

   for (idx=0; idx < count; idx++, b_p++) *b_p = value;
}

/*---------------------------------------------------------------------------*/

void set_word (WORD value, WORD target_buf[], size_t count)
{
   WORD  *b_p=target_buf;
   size_t idx;

   for (idx=0; idx < count; idx++, b_p++) *b_p = value;
}

/*---------------------------------------------------------------------------*/

void set_dword (DWORD  value, DWORD  target_buf[], size_t count)
{
   DWORD *b_p=target_buf;
   size_t idx;

   for (idx=0; idx < count; idx++, b_p++) *b_p = value;
}

/*---------------------------------------------------------------------------*/
