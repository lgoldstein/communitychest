
/*---------------------------------------------------------------------------*/

#include <string.h>
#include <stdlib.h>

#include <_types.h>
#include <util/string.h>

/*---------------------------------------------------------------------------*/
/*                      concat_fname
 *                      ------------
 *    Adds a file name component to an existing path.
 *
 * Parameters:
 *
 *    [I/O] path_p - the partial path this far.
 *    [IN]  fname_p - name of file component to be added.
 *    [IN]  max_path_length - maximum length of resulting path.
 *
 * NOTES: 1. path_p/fname_p must be DISTINCT pointers !!!
 *        2. if resulting path exceeds the allowed maximum then the procedure
 *        has no effect.
 *        3. the result is placed BACK in path_p.
 *        4. if the path_p does not end in ':' or '/' then '/' is added BEFORE
 *        the fname is added.
 */
/*---------------------------------------------------------------------------*/

size_t concat_fname (char path_p[], const char fname_p[], size_t max_path_length)
{
   size_t	final_len=0, flen=0;
   char     *last_ch_p=(char *) NULL;

	if (NULL == path_p)
		return 0;

	final_len = strlen((const char *) path_p);
	flen = ((NULL == fname_p) ? 0 : strlen(fname_p));
   if (0 == flen)
      return(final_len);

   if (final_len != 0)
   {
      last_ch_p = strlast(path_p);

      if (*(last_ch_p-1) != FULL_PATH_CHAR)
      {
         final_len++;
         if (final_len >= max_path_length)
            return(max_path_length);

         last_ch_p = strladdch(last_ch_p, FULL_PATH_CHAR);
      }
   }
   else
      last_ch_p = path_p;

   final_len += flen;
	if (final_len > max_path_length)
	{
		strncat(last_ch_p, fname_p, max_path_length - (final_len-flen));
		final_len = max_path_length;
	}
	else
		strncat(last_ch_p, fname_p, flen);

   return(final_len);
}

/*---------------------------------------------------------------------------*/
