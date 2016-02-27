
/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <util/string.h>
#include <util/tables.h>

/*---------------------------------------------------------------------------*/
/*                         bdt_xlate_to_ascii
 *                         ------------------
 *    Translates a buffer of BAUDOT encoded characters to the equivalent ASCII
 * encoding.
 *
 * Parameters:
 *
 *    [IN]  bdt_buf - buffer containing the BAUDOT encoded characters.
 *    [IN]  bdt_buf_len - number of characters in the buffer.
 *    [IN]  last_bdt_tbl - last BAUDOT table used (see note(s)..) - if neither
 *             BDT_FS or BDT_LS, then unknown last table, then routine will
 *             "synchronize" itself (see note(s)...).
 *    [IN]  ascii_buf - buffer into which the translation is to be placed.
 *
 *             NOTE: ASCII buffer should be the same length as the BAUDOT
 *                   buffer (although usually less space is needed...).
 *
 *    [OUT] ascii_buf_len - number of characters in ASCII buf.
 *
 * Returned value is last BAUDOT table used (see note(s)...).
 *
 * NOTE: the routine enables translation of SUCCESSIVE BUFFERS. This is done
 *       by following the letters/figures shitf(s) from one call to another.
 *       The routine checks the "last_bdt_tbl" to see if there is some prior
 *       knowledge as to which table to use - if no BDT_LS/FS value is provided
 *       the it will try to find the first such value by DISCARDING ALL VALUES
 *       UP TO THE BDT_LS/FS (!!). In any case, the routine returns the last
 *       synchroniztion value (BDT_LS/FS) if successful (or something else
 *       otherwise). When the routine is called with the next buffer, the
 *       last returned value should be supplied.
 *
 *       Example:
 *
 *          tbl1 = bdt_xlate_to_ascii(buf1, len1, BDT_NULL, outbuf1, &outlen1);
 *
 *             The "BDT_NULL" denotes not having any prior knowledge.
 *
 *          tbl2 = bdt_xlate_to_ascii(buf2, len2, tbl1, outbuf2, &outlen2);
 *
 *             The "tbl1" denotes the value returned by the 1st call.
 */
/*---------------------------------------------------------------------------*/

BYTE bdt_xlate_to_ascii (BYTE    bdt_buf[],
                         size_t  bdt_buf_len,
                         BYTE    last_bdt_tbl,
                         char    ascii_buf[],
                         size_t  *ascii_buf_len)
{
   size_t  	bndx, outlen=0;
   BYTE  	*bdt_p=bdt_buf, tbl_sign;
   char  	*asc_p=ascii_buf;

   if ((last_bdt_tbl != (BYTE) BDT_LS) && (last_bdt_tbl != (BYTE) BDT_FS))
   {
      /*
       *    If unknown last table, try to synchronize by finding first
       * occurance of and LS/FS.
       */

      for (bndx = 0; (bndx < bdt_buf_len); bndx++, bdt_p++)
         if ((*bdt_p == (BYTE) BDT_LS) || (*bdt_p == (BYTE) BDT_FS)) break;

      tbl_sign = *bdt_p;
   }
   else
   {
      bndx = 0;
      tbl_sign = last_bdt_tbl;
   }

   for ( ; (bndx < bdt_buf_len) ; bndx++, bdt_p++)
      switch(*bdt_p)
      {
         case BDT_LS    :
         case BDT_FS    :
            tbl_sign = *bdt_p;

            /* fall through to the "continue" stmt. */

         case BDT_NULL  :
            continue;

         default        :
            if (*bdt_p <= (BYTE) MAX_BAUDOT_CHAR)
               if (tbl_sign == (BYTE) BDT_LS)
                  *asc_p = bdt2ascii_letters_table[*bdt_p];
               else
                  *asc_p = bdt2ascii_figures_table[*bdt_p];
            else
               *asc_p = (char) BDT_UNKNOWN;

            asc_p++;
            outlen++;
      }

   *ascii_buf_len = outlen;
   return(tbl_sign);
}

/*---------------------------------------------------------------------------*/
