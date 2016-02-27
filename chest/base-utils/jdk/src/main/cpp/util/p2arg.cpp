#include <string.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

size_t pointer_to_argument (void *p, char str[])
{
	char *l=strlcpy(str,"0x");

	l += dword_to_hex_argument((DWORD) p, l);
	return(strlen(str));
}
