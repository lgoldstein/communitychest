
/*---------------------------------------------------------------------------*/

#include <string.h>
#include <stdio.h>
#include <sys/stat.h>
#include <ctype.h>
#include <errno.h>

#include <dirent.h>

#include <_types.h>
#include <util/errors.h>
#include <util/string.h>

#include <futils/general.h>

/*---------------------------------------------------------------------------*/

/*
 * 	This definition is required because "struct dirent" defines "d_name" as
 * a field of only 1 char in the "reentrant" version
 */

typedef union tag_sdir_dirent_type {
						struct dirent  dentry;
#ifndef WIN32
						char				dummy[sizeof(struct dirent)+MAX_FPATH_LEN];
#endif
} DIRENT_TYPE;

/*---------------------------------------------------------------------------*/
/*                         sdir_scan_directory
 *                         -------------------
 *    Scans the given directory and uses a callback function to announce the
 * user whenever a file is found.
 *
 * Parameters:
 *
 *    [IN]  dir_name_p - name of directory to be scanned.
 *    [IN]  dir_level - depth of directory - in case of sub-directories are
 *             found, they are scanned RECURIVELY. This parameter is used to
 *             inform the function which level/depth it scans. The initial
 *             caller should use "0".
 *    [IN]  params_p - parameter(s) to be passed to the callback function.
 *    [IN]  treat_file_cfn - callback function to be called for each file.
 *
 * NOTE: this procedure is equivalent to ONE CYCLE on ONE DIRECTORY of a
 *       scanning task.
 *
 *    Function returns TRUE if cycle completed successfully (i.e. user has not
 * aborted or returned non-EOK exception code).
 */
/*---------------------------------------------------------------------------*/

EXC_TYPE sdir_scan_directory (const char                 *dir_name_p,
                              const DWORD                dir_level,
                              void                       *params_p,
                              SDIR_TREAT_FILE_CFN_TYPE   treat_file_cfn)
{
	const char		*dir_np=dir_name_p;
   char           fpath[MAX_FPATH_LEN+2]="", *lfp=fpath, dirStat[2]={ 0 };
   size_t         dirname_len=0, fname_len=0;
   DIR            *dir_p=(DIR *) NULL;
   EXC_TYPE       exc=EOK;

	if (NULL == treat_file_cfn)
		return EBADADDR;

	/*
	 *	If NULL or empty dir name - assume current dir for scanning
	 */

	if (dir_np != (const char *) NULL)
	{
		if ((dirname_len=strlen(dir_np)) > MAX_DNLEN)
			return(EPATHNAMESYNTAX);
	}
	else
		dirname_len = 0;

	if ((dirname_len == 0) || (stricmp(dir_name_p, CURDIR_STR) == 0))
	{
		if (getcwd(fpath, (sizeof fpath)) == NULL)
			return EMEM;

		dir_np = fpath;
		lfp = fpath;
	}
	else
		lfp = strlcpy(fpath, dir_np);

	/* if folder does not exist, then nothing to scan */
	if ((exc=_access(dir_np, 0)) != 0)
		return EOK;
   if ((dir_p=opendir(dir_np)) == (DIR *) NULL)
      return EIFDR;

	/* signal entering scanned directory itself */
	dirStat[0] = SDIR_ENTER_DIR_DELIM;
	dirStat[1] = '\0';
	exc = (*treat_file_cfn)(dir_np, dir_level, dirStat, dir_np, params_p);

	if ((dirname_len == 0) || (stricmp(dir_name_p, CURDIR_STR) == 0))
		dir_np = CURDIR_STR;

   /*
    *    Scan source dir and process each file until either end of directory
    * or problematic file encountered.
    */

   while (EOK == exc)
   {
		struct dirent  *ent=(struct dirent *) NULL;
		DIRENT_TYPE		dentry;
		struct stat st;

		ent = readdir_r(dir_p, &dentry.dentry);
		if (NULL == ent)
			break;	/* no more entries */

      /*
       * Skip current & parent directories
       */

      if (strcmp(ent->d_name, CURDIR_STR) == 0)
         continue;

      if (strcmp(ent->d_name, PARENTDIR_STR) == 0)
         continue;

      /*
       * Build new file path
       */

		*lfp = '\0';
      fname_len = concat_fname(fpath, ent->d_name, MAX_FPATH_LEN);
		if (fname_len >= MAX_FPATH_LEN)
			return(EINVALIDFNODE);

      /*
       * Check what we have here (file, device, sub-dir...).
       */

		memset(&st, 0, (sizeof st));
		if (stat(fpath, &st) != 0)
      {
         exc = ECONTEXT;
			break;
      }

      /*
       * If ordinary file - call callback function.
       */

		if (S_REGF(st.st_mode))
			exc = (*treat_file_cfn)(dir_np,
											dir_level,
											ent->d_name,
											fpath,
											params_p);
      else if (S_ISDIR(st.st_mode))  /* Sub-directory - scan recursively */
			exc = sdir_scan_directory((const char *) fpath,
											  (dir_level+1),
											  params_p,
											  treat_file_cfn);
      else  /* if device or special file - do nothing */
         exc = EOK;
   }  /* end of WHILE loop on directory scan */

   closedir(dir_p);

	if ((EOK == exc) || (EABORTEXIT == exc))
	{
		/* signal exiting scanned directory itself */
		dirStat[0] = SDIR_EXIT_DIR_DELIM;
		dirStat[1] = '\0';
		exc = (*treat_file_cfn)(dir_np, dir_level, dirStat, dir_np, params_p);
	}

	/* propagate abort signal if not top level */
	if ((EABORTEXIT == exc) && (0 == dir_level))
		exc = EOK;

   return(exc);
}

/*---------------------------------------------------------------------------*/

static EXC_TYPE sdir_del_cfn (const char  *dir_name_p,
                              const DWORD dir_level,
                              const char  *fname_p,
                              const char  *fpath_p,
                              void        *params_p)
{
	/* skip directory entrance signal */
	if ((SDIR_ENTER_DIR_DELIM == fname_p[0]) && ('\0' == fname_p[1]))
		return EOK;

	if ((SDIR_EXIT_DIR_DELIM == fname_p[0]) && ('\0' == fname_p[1]))
	{
		const char *pDir=(const char *) params_p;

		/* do not delete the original directory (if supplied) */
		if ((NULL == pDir) || (strcmp(pDir, dir_name_p) != 0))
		{
			if (_rmdir(dir_name_p) != 0)
				return EIOHARD;
		}
	}
	else	/* normal file */
	{
		if (remove(fpath_p) != 0)
			return EIOSOFT;
	}

	return EOK;
}

EXC_TYPE sdir_delete_dir (const char *dir_name_p, const BOOLEAN fRemoveIt)
{
	return sdir_scan_directory(dir_name_p, 0, 
										((fRemoveIt) ? NULL : (void *) dir_name_p),
										sdir_del_cfn);
}

/*---------------------------------------------------------------------------*/
