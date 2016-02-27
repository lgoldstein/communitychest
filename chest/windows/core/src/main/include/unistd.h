#ifndef _UNISTD_H_
#define _UNISTD_H_

/* this file is supplied for UNIX compatibility */

#include <io.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

/*--------------------------------------------------------------------------*/

#ifndef F_OK
#define F_OK	0
#endif

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern "C" {
#endif

/*--------------------------------------------------------------------------*/

extern int truncate (const char *pszFPath, off_t fLen);
extern int ftruncate (int fDes, off_t fLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
}
#endif

#endif