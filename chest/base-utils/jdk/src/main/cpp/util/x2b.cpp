
/*---------------------------------------------------------------------------*/

#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>
#include <util/tables.h>
#include <util/errors.h>

/*---------------------------------------------------------------------------*/

BYTE hex_digit_to_byte (char xdigit, EXC_PARAM(exc))
{
	BYTE	byte_val=0;

	EXC_VAL(exc,EOK);

	if (!isxdigit((int) xdigit))
	{
		EXC_VAL(exc,EPARAM);
		return(0);
	}

	if (isdigit((int) xdigit))
		byte_val = (BYTE) (xdigit - '0');
	else
		byte_val = (BYTE) (10 + (tolower((int) xdigit) - 'a'));

	return(byte_val);
}

/*---------------------------------------------------------------------------*/

char byte_to_hex_digit (BYTE byte_val, EXC_PARAM(exc))
{
	EXC_VAL(exc,EOK);

	if (byte_val >= (BYTE) HEX_DIGITS_CHARS_NUM)
	{
		EXC_VAL(exc,EOVERFLOW);
		return('\0');
	}
	else
		return(hex_digits_chars[byte_val]);
}

/*---------------------------------------------------------------------------*/
