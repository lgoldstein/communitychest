
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/errors.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

char boolean_to_char (BOOLEAN   bval)
{
   if (bval)
      return(TRUE_CHAR);
   else
      return(FALSE_CHAR);
}

/*---------------------------------------------------------------------------*/

BOOLEAN char_to_boolean (char boolch, EXC_PARAM(exc))
{
	EXC_VAL(exc,EOK);

   if (boolch == FALSE_CHAR)
		return(FALSE);
   if (boolch == TRUE_CHAR)
		return(TRUE);

	EXC_VAL(exc,EPARAM);
   return((BOOLEAN) 0xAA);
}

/*---------------------------------------------------------------------------*/
