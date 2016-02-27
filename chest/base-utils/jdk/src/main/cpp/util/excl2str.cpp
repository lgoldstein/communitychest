
/*---------------------------------------------------------------------------*/

#include <string.h>
#include <ctype.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern
#endif
const char *exc_level_string[]={
	"FATAL",
	"ALARM",
	"INFO",
	"BAD???"
	};

/*---------------------------------------------------------------------------*/

#ifndef __cplusplus
const char *exc_level2str (EXC_LEVEL_TYPE exc_level)
{
	if (is_bad_exc_level(exc_level))
		return(exc_level_string[EXC_BAD_LEVEL]);
	else
		return(exc_level_string[exc_level]);
}
#endif

/*---------------------------------------------------------------------------*/
