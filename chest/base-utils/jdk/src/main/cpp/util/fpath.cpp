#include <string.h>
#include <stdlib.h>

#include <_types.h>
#include <util/string.h>

#ifdef _UNIX_
#include <pwd.h>
#endif

/*---------------------------------------------------------------------------*/
/*								get_fpath_components
 *								--------------------
 *		Breaks up a file path into its last component + extension (if found).
 *
 * e.g.	:kuku:app/new/foo.35 -> fname = foo, extension=35
 *
 * NOTE: 1. only LAST component of a path is processed.
 *			2. if no extension exists then a zero length string is returned as the
 *			extension string.
 */
/*---------------------------------------------------------------------------*/

void get_fpath_components (const char	 fpath_p[],
											char	 fname_p[],
											char	 ext_p[])
{
	size_t sndx, endx, fplen, fnlen;
	char	 *ch_p, *e_p;

	*fname_p = '\0';
	*ext_p = '\0';
	fplen = strlen(fpath_p);

	ch_p = (char *) (fpath_p+fplen);
	for (sndx = (fplen-1); (sndx > 0) && (*ch_p != FULL_PATH_CHAR) ; sndx--)
		ch_p--;

	/*
	 *		At the end of the loop "ch_p" points one character BEFORE the last
	 * component of the path. "sndx" points to first character of the path's
	 * last component.
	 *
	 *	If "ch_p" does not point to a directory separator, then the path
	 * contains no directory references.
	 */

	if (*ch_p == FULL_PATH_CHAR)
	{
		ch_p++;
		sndx+= 2;
	}
	else
	{
		ch_p = (char *) fpath_p;
		sndx = 0;
	}

	for (e_p = ch_p,endx=sndx; (endx < fplen) && (*e_p != DOT_CHAR); e_p++,endx++);

	/*
	 *		At the end of the loop either "e_p" points to '.' or it points BEYOND
	 * the end of the string- either way "endx"+"e_p" point to first character
	 * BEYOND the file name component (regardless of whether there is an
	 * extension or not).
	 */

	fnlen = endx-sndx;
	memcpy((void *) fname_p, (const void *) ch_p, (size_t) fnlen);
	fname_p[fnlen] = '\0';

	fnlen = fplen - endx;

	/*
	 *		The '\0' at the end of the path is copied to the extension (it is
	 * included in the value of "fnlen").
	 */
 
	if (fnlen > 0)
		memcpy((void *) ext_p, (const void *) (e_p+1), (size_t) fnlen);
}

/*---------------------------------------------------------------------------*/

static size_t expand_env_var (char fpath[])
{
	size_t l=strlen(fpath), tln=0, vlen=0, mlen=0, flen=l, idx;
	char	 *tsp=strchr(fpath, '/'), tch='\0', *val_p=NULL;

	if (tsp == NULL)
		tsp = strlast(fpath);

	tch = *tsp;
	*tsp = '\0';  /* insert a temporary terminator */
	vlen = strlen(fpath);
	val_p = getenv(fpath);
	*tsp = tch;	  /* restore original terminator */
	if (val_p == NULL)
		return(l);

	tln = strlen(val_p);


	/*
	 *		Make room for "tln" characters in order to insert the environmental
	 * variable value.
	 */

	if (tln >= vlen)
	{
		mlen = tln-vlen;
		flen = l + mlen;
		for (idx = flen; idx > mlen; idx--)
			fpath[idx] = fpath[idx-mlen];
	}
	else
	{
		mlen = vlen - tln;
		for (idx = 0; idx < l; idx++)
			fpath[idx] = fpath[idx + mlen];
		flen = l - mlen;
	}

	/*
	 *	  store last character which will be temporarily overwitten by the
	 * "val_p" terminator after "strcpy" execution.
	 */

	tch = fpath[tln];
	strcpy(fpath, val_p);
	fpath[tln] = tch; /* restore overwritten character */
	return(flen);
}

/*---------------------------------------------------------------------------*/

static size_t expand_home_dir (char fpath[])
{
	size_t	   l=strlen(fpath), tln, idx, tp, d;
	char			*tsp=NULL, tch;
#ifdef _UNIX_
	struct passwd *pwdp=NULL;
#endif

	if (fpath[1] == FULL_PATH_CHAR)
	{
		/*
		 * No user name to follow '~'
		 */

		tsp = getenv("HOME");
		if (tsp == NULL)
			return(l);
	}
	else
	{
#ifdef _UNIX_
		/*
		 *		Find out the extent of the user's name - if no '/' found, then
		 * assume whole string is user's name.
		 */

		if ((tsp=strchr(fpath, FULL_PATH_CHAR)) == NULL)
			tsp = strlast(fpath);

		tch = *tsp;
		*tsp = '\0';		/* insert temporary terminator */
		tp = strlen(fpath);
		*tsp = tch;			/* restore original char */

		if (pwdp == NULL)
			return(l);
		
		tsp = pwdp->pw_dir;
#else
		return(l);
#endif
	}

	tln = strlen(tsp);

	/*
	 * Make room for "tln" characters to precede the first '/' (if any)
	 */

	if ((tsp=strchr(fpath, FULL_PATH_CHAR)) == NULL)
		tsp = strlast(fpath);
	tch = *tsp;
	*tsp = '\0';		/* insert temporary terminator */
	tp = strlen(fpath);
	*tsp = tch;

	if (tln >= tp)
	{
		/*
		 *	User path length is greater than available
		 */

		d = tln - tp;
		for (idx = l + d; idx >= tp; idx--)
			fpath[idx] = fpath[idx-d];
		l += d;
	}
	else
	{
		/*
		 *	User path length is less than user's name.
		 */

		d = tp-tln;
		for (idx=tln; idx <= (l-d); idx++)
			fpath[idx] = fpath[idx+d];

		l -= d;
	}

	/*
	 * Store char in last position since it is overwritten by '\0' by "strcpy"
	 */

	tch = fpath[tln];
	strcpy(fpath, tsp);
	fpath[tln] = tch;
	return(l);
}

/*---------------------------------------------------------------------------*/
/*									  expand_fpath
 *									  ------------
 * Expands paths with '~' (e.g. ~kuku/what). If path does not start with '~'
 * then it has no effect. Expansion is performed as follows
 *
 *		a. if '~' is followed by '/' then the '$HOME' environmental variable is
 *			used (using "getenv") to supply the required prefix.
 *
 *		b. if '~' is followed by a user name, then the prefix "/home/user-name"
 *			is used.
 */
/*---------------------------------------------------------------------------*/

size_t expand_fpath (char fpath[])
{
	switch(fpath[0])
	{
		case '~' :
			return(expand_home_dir(fpath));

		case '$' :
			return(expand_env_var(fpath));
			
		default :
			return(strlen(fpath));
	}
}

/*---------------------------------------------------------------------------*/

