
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/tables.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

void xlate_to_lowercase (BYTE  buf[], WORD buf_len)
{
   if (buf_len == 0) return;

   translate(buf, buf, buf_len, (BYTE *) &lower_case_chars_tbl[0]);
}

/*---------------------------------------------------------------------------*/

void xlate_to_uppercase (BYTE  buf[], WORD buf_len)
{
   if (buf_len == 0) return;

   translate(buf, buf, buf_len, (BYTE *) &upper_case_chars_tbl[0]);
}

/*---------------------------------------------------------------------------*/
