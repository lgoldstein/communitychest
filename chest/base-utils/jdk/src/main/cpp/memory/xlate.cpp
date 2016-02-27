#include <_types.h>
#include <util/memory.h>

/*
 *    Translates "src" into "dst" using the "tbl" conversion table.
 */

BOOLEAN translate (const BYTE src[], BYTE dst[], size_t len, const BYTE tbl[])
{
   size_t  		ndx;
   const BYTE  *s=src;
	BYTE			*d=dst;

	if ((NULL == src) || (NULL == dst) || (NULL == tbl))
		return FALSE;

   for (ndx=0; ndx < len; ndx++, s++, d++)
		*d = tbl[*s];

	return TRUE;
}
