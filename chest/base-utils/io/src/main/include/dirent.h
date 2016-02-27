#ifndef __DIRENT_H_
#define __DIRENT_H_

/*---------------------------------------------------------------------------*/

/* dirent.h - POSIX directory handling definitions */

#ifdef WIN32
#include <wtypes.h>
#include <tchar.h>
#include <direct.h>

#ifndef getcwd
#define getcwd(s,l) _getcwd(s,l)
#endif

#endif	/* of ifdef WIN32 */

#include <limits.h>

/*---------------------------------------------------------------------------*/

#ifndef S_ISREG
#define S_ISREG(m)	(((m) & S_IFREG) == S_IFREG)
#endif

#ifndef S_ISDIR
#define S_ISDIR(m)	(((m) & S_IFDIR) == S_IFDIR)
#endif

#ifndef S_REGF
#define S_REGF(m)		((S_ISREG(m)) || (((m) & S_IFMT) == S_IFMT))
#endif

/* Directory entry */

#ifdef WIN32
#	ifndef NAME_MAX
#		define NAME_MAX	MAX_PATH
#	endif

struct dirent {
	TCHAR	d_name[NAME_MAX+2];	/* file name, null-terminated */
};

/* NOTE !!! users may not access these fields directly !! */
typedef struct {
		HANDLE				hFind;
		WIN32_FIND_DATA	fileData;
		TCHAR					szDirPath[MAX_PATH+2];
} DIR;
#endif

/*---------------------------------------------------------------------------*/

/* function declarations */

extern DIR		*opendir (LPCTSTR dirName);
extern int		closedir (DIR *pDir);
extern struct	dirent *readdir (DIR *pDir);

/* this is the "reentrant" version */
extern struct dirent *readdir_r (DIR *pDir, struct dirent *pEnt);

extern void 	rewinddir (DIR *pDir);

/*---------------------------------------------------------------------------*/

#endif /* _DIRENT_H_ */
