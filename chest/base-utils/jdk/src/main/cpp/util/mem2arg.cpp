
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

#define GBYTE_THRESHOLD    0x40000000
#define MBYTE_THRESHOLD    0x00100000
#define KBYTE_THRESHOLD    0x00000400

/*---------------------------------------------------------------------------*/

static size_t build_memarg (char   arg_p[], WORD pref, WORD mant, char valchar)
{
   size_t   len=0, tln;
	char		*str=arg_p;

   if (mant >= (WORD) 1000)
   {
      pref++;
      mant -= (WORD) 1000;
   }

   len = word_to_argument(pref, arg_p);
	str += len;
   str = strladdch(str, '.'); 
	len++;
   tln = concat_word(str, mant, NAME_LENGTH);
	str += tln;
	len += tln;
   str = strladdch(str, valchar);
	len++;

   return(len);
}

/*---------------------------------------------------------------------------*/
/*                         memory_to_argument
 *                         ------------------
 *    Converts a value representing a memory size to an ASCII argument. The
 * display format is "nnn.xxx" where:
 *
 *       nnn - the "FIXED" part in G/M/Kbytes.
 *       xxx - the "MANTISSA" - up to 3 digits precision.
 *
 *       (e.g. 2,100,326 bytes ~ 2.100M)
 *
 * NOTE: the procedure adds G/M/K - according to the case.
 */
/*---------------------------------------------------------------------------*/

size_t memory_to_argument (DWORD   memval,  char arg_p[])
{
   if (memval >= (DWORD) GBYTE_THRESHOLD)
      return(build_memarg(arg_p,
                         (WORD) (memval >> 30),
                         (WORD) ((memval & (GBYTE_THRESHOLD-1)) >> 20),
                         (char) GBYTE_CHAR));

   if (memval >= (DWORD) MBYTE_THRESHOLD)
      return(build_memarg(arg_p,
                          (WORD) (memval >> 20),
                          (WORD) ((memval & (MBYTE_THRESHOLD-1)) >> 10),
                          (char) MBYTE_CHAR));

   return(build_memarg(arg_p,
                       (WORD) (memval >> 10),
                       (WORD) (memval & (KBYTE_THRESHOLD-1)),
                       (char) KBYTE_CHAR));
}

/*---------------------------------------------------------------------------*/
