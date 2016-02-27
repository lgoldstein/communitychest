#include <stdlib.h>
#include <string.h>
#include <limits.h>

/*---------------------------------------------------------------------------*/

#include <dirent.h>

/*---------------------------------------------------------------------------*/

DIR *opendir (LPCTSTR dirName)
{
	DIR	*pDir=NULL;

	if (NULL == dirName)
		return NULL;
#ifdef __cplusplus
	pDir = new DIR;
#else
	pDir = (DIR *) malloc(sizeof *pDir);
#endif

	if (NULL == pDir)
		return NULL;
	memset(pDir, 0, (sizeof *pDir));

	_tcscpy(pDir->szDirPath, dirName);
	_tcscat(pDir->szDirPath, _T("\\*.*"));
	pDir->hFind = FindFirstFile(pDir->szDirPath, &(pDir->fileData));
	if (INVALID_HANDLE_VALUE == pDir->hFind)
	{
#ifdef __cplusplus
		delete pDir;
#else
		free((LPVOID) pDir);
#endif
		return NULL;
	}

	return pDir;
}

/*---------------------------------------------------------------------------*/

int closedir (DIR *pDir)
{
	if (NULL == pDir) return (-1);
	if (INVALID_HANDLE_VALUE != pDir->hFind)
		FindClose(pDir->hFind);
#ifdef __cplusplus
	delete pDir;
#else
	free((LPVOID) pDir);
#endif

	return 0;
}

/*---------------------------------------------------------------------------*/

/* this is the "reentrant" version */
struct dirent *readdir_r (DIR *pDir, struct dirent *pEnt)
{
	if ((NULL == pDir) || (NULL == pEnt))
		return NULL;
	if ((INVALID_HANDLE_VALUE == pDir->hFind) || (NULL == pDir->hFind))
		return NULL;

	strncpy(pEnt->d_name, pDir->fileData.cFileName, NAME_MAX);
	pEnt->d_name[NAME_MAX] = '\0';

	if (!FindNextFile(pDir->hFind, &(pDir->fileData)))
	{
		FindClose(pDir->hFind);
		pDir->hFind = INVALID_HANDLE_VALUE;
		memset(&(pDir->fileData), 0, (sizeof pDir->fileData));
	}

	return pEnt;
}

struct dirent *readdir (DIR *pDir)
{
	static struct dirent ent;

	return readdir_r(pDir, &ent);
}

/*---------------------------------------------------------------------------*/

void rewinddir (DIR *pDir)
{
	if (NULL == pDir) return;

	if (pDir->hFind != INVALID_HANDLE_VALUE)
		FindClose(pDir->hFind);
	pDir->hFind = FindFirstFile(pDir->szDirPath, &(pDir->fileData));
	if (INVALID_HANDLE_VALUE == pDir->hFind)
	{
		memset(&(pDir->fileData), 0, (sizeof pDir->fileData));
		return;
	}
}

/*---------------------------------------------------------------------------*/
