
/*---------------------------------------------------------------------------*/

#include <string.h>

#include <_types.h>
#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/

#define MSDIGIT_MASK       0xF0000000
#define MSDIGIT_SHIFT      28

/*---------------------------------------------------------------------------*/

size_t dword_to_argument (DWORD in_dword, char   arg[])
{
	if (NULL == arg)
		return 0;

   *arg = '\0';
   return(concat_dword(arg, in_dword, MAX_DWORD_10_POWERS+1));
}

/*---------------------------------------------------------------------------*/

size_t dword_to_hex_argument (DWORD in_dword, char  arg[])
{
	if (NULL == arg)
		return 0;

   *arg = '\0';
   return(concat_hex_dword(arg, in_dword, MAX_DWORD_HEX_DISPLAY_LENGTH+1));
}

/*---------------------------------------------------------------------------*/

size_t dword_to_fixed_argument (DWORD in_dword, char arg[], size_t len, char padChar, BOOLEAN leftPad)
{
	size_t	digitsNum=dword_to_argument(in_dword, arg);
	size_t	padLen=strpad(arg, len, padChar, leftPad);

	return (digitsNum + padLen);
}

/*---------------------------------------------------------------------------*/
