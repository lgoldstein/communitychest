
/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <string.h>
#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/

size_t word_to_argument (WORD in_word, char   arg[])
{
   *arg = '\0';
   return(concat_word(arg, in_word, MAX_WORD_10_POWERS+1));
}

/*---------------------------------------------------------------------------*/

size_t word_to_hex_argument (WORD in_word, char   arg[])
{
   *arg = '\0';
   return(concat_hex_word(arg, in_word, MAX_WORD_HEX_DISPLAY_LENGTH+1));
}

/*---------------------------------------------------------------------------*/

size_t word_to_fixed_argument (WORD in_word, char arg[], size_t len, char padChar, BOOLEAN leftPad)
{
	size_t	digitsNum=word_to_argument(in_word, arg);
	size_t	padLen=strpad(arg, len, padChar, leftPad);

	return (digitsNum + padLen);
}

/*---------------------------------------------------------------------------*/
