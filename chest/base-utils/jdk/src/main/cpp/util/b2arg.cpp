
/*---------------------------------------------------------------------------*/

#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/

size_t byte_to_argument (BYTE in_byte, char arg[])
{
	if (NULL == arg)
		return 0;

   *arg = '\0';
   return(concat_byte(arg, in_byte, MAX_BYTE_10_POWERS+1));
}

/*---------------------------------------------------------------------------*/

size_t byte_to_hex_argument (BYTE in_byte, char   arg[])
{
	if (NULL == arg)
		return 0;

   *arg = '\0';
   return(concat_hex_byte(arg, in_byte, MAX_BYTE_HEX_DISPLAY_LENGTH+1));
}

/*---------------------------------------------------------------------------*/

size_t byte_to_fixed_argument (BYTE in_byte, char arg[], size_t len, char padChar, BOOLEAN leftPad)
{
	size_t	digitsNum=byte_to_argument(in_byte, arg);
	size_t	padLen=strpad(arg, len, padChar, leftPad);

	return (digitsNum + padLen);
}

/*---------------------------------------------------------------------------*/
