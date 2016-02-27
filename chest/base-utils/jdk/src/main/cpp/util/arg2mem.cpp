
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/errors.h>
#include <util/string.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

DWORD argument_to_memory (const char arg_p[], EXC_PARAM(exc))
{
   DWORD       pref, mant;
	size_t 		arg_len, ndx;
	const char	*pp=NULL;
	EXC_TYPE    err=EOK, *exc_p=&err;

#ifdef __cplusplus
	exc_p = &exc;
#else
	exc_p = (exc != (EXC_TYPE *) NULL) ? exc : &err;
#endif
	*exc_p = EOK;

	if (NULL == arg_p)
	{
		*exc_p = EBADBUFF;
		return 0;
	}

	if (NULL == (pp=strchr(arg_p, '.')))
	{
		*exc_p = ETYPE;
		return(0);
	}

   *exc_p = EOK;
   arg_len = strlen(arg_p);
   ndx = (pp - arg_p); 

   /*
    *    The '.' must be followed by at least one digit + one char.
    */

   if (ndx >= (arg_len-2))
   {
      *exc_p = ETYPE;
      return((DWORD) 0);
   }

   pref = argument_to_word(arg_p, ndx, exc);
   if (*exc_p != EOK) return((DWORD) 0);

   mant = argument_to_word(&arg_p[ndx+1], (arg_len-ndx-2), exc);
   if (*exc_p != EOK) return((DWORD) 0);

   switch(arg_p[arg_len-1])
   {
      case KBYTE_CHAR   :
         return(((DWORD) pref << 10) + (DWORD) mant);

      case MBYTE_CHAR   :
         return(((DWORD) pref << 20) + ((DWORD) mant << 10));

      case GBYTE_CHAR   :
         return(((DWORD) pref << 30) + ((DWORD) mant << 20));

      default           :
         *exc_p = EPARAM;
         return((DWORD) 0);
   }
}

/*---------------------------------------------------------------------------*/
