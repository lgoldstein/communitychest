#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

/*
 *	The following definitions are used in order to determine the CPU architecture
 * during RUNTIME - LO/HI endian (i.e. LO value 1st or high value first)
 */

#ifdef __cplusplus
extern
#endif
const WORD architecture_word=(WORD) (0x5A00+(LO_ENDIAN_VALUE));

#ifdef __cplusplus
extern
#endif
const BYTE *architecture_bytes=(BYTE *) &architecture_word;

/*---------------------------------------------------------------------------*/
