#ifndef _UTL_TABLES_H_
#define _UTL_TABLES_H_

/*
 * File
 *    tables.h
 *
 * Purpose
 *
 *       Contains declarations of useful tables.
 *
 * Programmer
 *    LYOR  G.
 *
 * Current Revision
 *    $Revision: 561 $
 *
 * Date
 *    28-March-1993
 *
 * Remarks:
 */

#include <_types.h>

/*
 * History of changes:
 *
 *    1. added BAUDOT - ASCII conversion table(s) & routine(s) (24/07/94 - LY)
 */

/*---------------------------------------------------------------------------*/
/*                   bit_set/clear_byte/word/dword_mask_table(s)
 *                   -------------------------------------------
 *    Tables of values which may be used to set/clear specific bits :
 *
 *       - Setting is done by OR-ing the value with the proper mask.
 *       - Clearing is done by AND-ing the value with the proper mask.
 *
 *    Each table contains at cell N the mask for setting/clearing the N-th
 * bit (LSBit is always #0).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE bit_set_byte_mask_table[];
extern const BYTE bit_clear_byte_mask_table[];

extern const WORD bit_set_word_mask_table[];
extern const WORD bit_clear_word_mask_table[];

extern const DWORD bit_set_dword_mask_table[];
extern const DWORD bit_clear_dword_mask_table[];

/*---------------------------------------------------------------------------*/
/*                      complemented_bytes_table
 *                      ------------------------
 *       Contains the 1's complement for each BYTE value (i.e. tbl[i]=0xFF-i)
 */
/*---------------------------------------------------------------------------*/

extern const BYTE complemented_bytes_table[];

/*---------------------------------------------------------------------------*/
/*                         reversed_bytes_table
 *                         --------------------
 *    Contains the BYTE with REVERSED BITS ORDERING (LSBit <-> MSBit).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE reversed_bytes_table[];

/*---------------------------------------------------------------------------*/
/*                byte/word/dword_powers_of_2_tables
 *                ----------------------------------
 *    Contains all possible powers of 2 which can be represented using a
 * byte/word/dword (respectively) - i.e. tbl[i] = 2^i (i=0,1,2,...)
 */
/*---------------------------------------------------------------------------*/

#define MAX_BYTE_2_POWERS   8
#define MAX_WORD_2_POWERS  16
#define MAX_DWORD_2_POWERS 32

extern const BYTE    byte_powers_of_2_table[];
extern const WORD    word_powers_of_2_table[];
extern const DWORD   dword_powers_of_2_table[];

/*---------------------------------------------------------------------------*/
/*                byte/word/dword_powers_of_10_tables
 *                ----------------------------------
 *    Contains all possible powers of 10 which can be represented using a
 * byte/word/dword (respectively) - i.e. tbl[i] = 10^i (i=0,1,2,...)
 */
/*---------------------------------------------------------------------------*/

#define MAX_BYTE_10_POWERS    3
#define MAX_WORD_10_POWERS    5
#define MAX_DWORD_10_POWERS  10

extern const BYTE  byte_10_powers[];
extern const WORD  word_10_powers[];
extern const DWORD dword_10_powers[];

/*---------------------------------------------------------------------------*/
/*                byte/word/dword_powers_of_16_tables
 *                ----------------------------------
 *    Contains all possible powers of 16 which can be represented using a
 * byte/word/dword (respectively) - i.e. tbl[i] = 16^i (i=0,1,2,...)
 */
/*---------------------------------------------------------------------------*/

#define MAX_BYTE_16_POWERS    2
#define MAX_WORD_16_POWERS    4
#define MAX_DWORD_16_POWERS   8

extern const BYTE  byte_16_powers[];
extern const WORD  word_16_powers[];
extern const DWORD dword_16_powers[];

/*---------------------------------------------------------------------------*/
/*                      hex_digits_chars
 *                      ----------------
 *    Contains the characters for representing a number in HEX format (e.g.
 * tbl[14] == 'E').
 */
/*---------------------------------------------------------------------------*/

#define HEX_DIGITS_CHARS_NUM     16

extern const char hex_digits_chars[];

/*---------------------------------------------------------------------------*/
/*                         lower/upper_case_chars_tbl
 *                         --------------------------
 *       Contains a conersion table to lower/upper case letters (i.e.
 * tbl[ch] = upper/lower(ch)).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE upper_case_chars_tbl[];
extern const BYTE lower_case_chars_tbl[];

/*---------------------------------------------------------------------------*/
/*                         set_bits_count_table
 *                         --------------------
 *    Holds in each byte the number of set bits (i.e. "1"s) in the binary
 * representation of that byte (e.g. set_bits_count_table[7]=3).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE set_bits_count_table[];

/*
 *	Returns total number of bits set in the "bits-array".
 */

extern DWORD get_set_bits_num (const BYTE bits_array[], DWORD alen);

/*---------------------------------------------------------------------------*/
/*                         start/end_zeros_tbl
 *                         -------------------
 *    Holds the number of successive "0"s starting from the L/M-SBit (!!!) for
 * each byte in its binary representation (e.g. start_zeros_tbl[20] = 2,
 * end_zeros_tbl[20] = 3).
 *
 * NOTE: start_zeros_tbl[0] = 8 ==> end_zeros_tbl[0] = 0 !!!!!
 */
/*---------------------------------------------------------------------------*/

extern const BYTE start_zeros_tbl[];
extern const BYTE end_zeros_tbl[];

/*---------------------------------------------------------------------------*/
/*                         start/end_ones_tbl
 *                         ------------------
 *    Holds the number of successive "1"s starting from the L/M-SBit (!!!) for
 * each byte in its binary representation (e.g. start_ones_tbl[233] = 1,
 * end_ones_tbl[233] = 3).
 *
 * NOTE: start_ones_tbl[0xFF] = 8 ==> end_ones_tbl[0xFF] = 0 !!!!!
 */
/*---------------------------------------------------------------------------*/

extern const BYTE start_ones_tbl[];
extern const BYTE end_ones_tbl[];

/*---------------------------------------------------------------------------*/
/*                      ebcdic<->ascii tables
 *                      ---------------------
 *    Conversion table(s) between EBCDIC and ASCII character code(s).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE ebcdic_to_ascii_tbl[];
extern const BYTE ascii_to_ebcdic_tbl[];

/*---------------------------------------------------------------------------*/
/*								heb7to8bit_chars_tbl
 *								--------------------
 *		Conversion between 7bit <-> 8 bit Hebrew
 */
/*---------------------------------------------------------------------------*/

extern const char heb7to8bit_chars_tbl[];

/*---------------------------------------------------------------------------*/
/*								eng2heb_keystrokes_tbl
 *								----------------------
 *		Conversion between keystorkes on keyboard to 7-bit Hebrew
 */
/*---------------------------------------------------------------------------*/

extern const char eng2heb_keystrokes_tbl[];

/*---------------------------------------------------------------------------*/

/*		Days of week (0=Sun.). Last (extra) element is NULL so it can be used
 * as a scanning stop-point).
 */
#define ABBREV_DOW_LEN	3
extern const char *day_of_week[];
extern const char *full_day_of_week[];

/* returns 0-6 if found (or greater if not found) */
extern UINT8 str2dow (const char szDOW[], const UINT32 cLen);
extern UINT8 str2fulldow (const char szDOW[], const UINT32 cLen);

/*		Months of year (0=January). Last (extra) element is NULL so it can be
 * used as a scanning stop-point).
 */
#define ABBREV_MOY_LEN	3
extern const char *month_of_year[];
extern const char *full_month_of_year[];

/* returns 0-11 (or greater if fails) */
extern UINT8 str2moy (const char szMOY[], const UINT32 cLen);
extern UINT8 str2fullmoy (const char szMOY[], const UINT32 cLen);

/*---------------------------------------------------------------------------*/

#define MAX_BAUDOT_CHAR 0x1F
#define BDT_CHARS_NUM   ((MAX_BAUDOT)CHAR) + 1)

#define BDT_NULL        0x00
#define BDT_WRU         '#'      /* marks the "who are you"     */
#define BDT_SPROCKET    '|'
#define BDT_FS          0x1B
#define BDT_LS          0x1F
#define BDT_UNKNOWN     '~'      /* marks non-baudot characters */

/*---------------------------------------------------------------------------*/
/*                   bdt2ascii_letters/figures_table(s)
 *                   ----------------------------------
 *    Contain the ASCII equivalent of the BAUDOT figure & letters tables.
 *
 * NOTE: BDT_LS/FS have no equivalent so they are translated to same values.
 */
/*---------------------------------------------------------------------------*/

extern const char bdt2ascii_letters_table[];
extern const char bdt2ascii_figures_table[];

/*---------------------------------------------------------------------------*/
/*                         reverse_bdt_bits_table
 *                         ----------------------
 *    Contains the BAUDOT code(s) with bits in reverse order (e.g. 0x01->0x10).
 */
/*---------------------------------------------------------------------------*/

extern const BYTE reverse_bdt_bits_table[];

/*---------------------------------------------------------------------------*/

/* precompiled CRC32 values for 8-bit input values which can be used as index
 * of the table.
 */

extern const UINT32 crc32_table[];

/*---------------------------------------------------------------------------*/

/*
 * Signals end of codes in array - must be GREATER than any legal code
 */

#define END_OF_EXC_CODES   0xFFFF

/*
 * contains all possible exception codes. The "strings" array contains ASCII
 * strings for displaying the code (the order matches the "codes" array).
 *
 * NOTE !!! the exception codes are SORTED in order of INCREASING value
 */

extern const EXC_TYPE exception_codes[];
extern const char *exception_strings[];

/*---------------------------------------------------------------------------*/

/*		This table contains the base64 encoding translation - by indexing it with
 * a 6-bit (!) value, one receives the encoding character
 */
extern const char base64_encode_tbl[];

/*		This table contains the base64 decoding translation - by indexing it with
 * a character encoding (!) value, one receives the 6-bit value (in lower 6
 * bits of the 8-bit value). An invalid index is signaled by a value which is
 * greater or equal to BASE64_MAX_VALUE (i.e. FIsBadBase64Value(v) is TRUE).
 */
extern const UINT8 base64_decode_tbl[];

/*===========================================================================*/

/* entry in a G3 run-length(s) table */
typedef struct {
		UINT16	code;	/* code word (placed in low bits) */
		SINT16	len;	/* code len (in bits) */
} RLE_ENTRY;

/*---------------------------------------------------------------------------*/

extern const RLE_ENTRY G3_EOL_RLE;

extern const RLE_ENTRY G3_PRE_EOL_RLE;

/*---------------------------------------------------------------------------*/

/* terminating codes for white run lengths */
extern const RLE_ENTRY G31D_TermWhite[];

/* make up codes for white run-length */
extern const RLE_ENTRY G31D_MakeupWhite[];

extern const RLE_ENTRY *G31D_ExtendedWhite;

/* terminating codes for black run lengths */
extern const RLE_ENTRY G31D_TermBlack[];

/* make up codes for black run-length */
extern const RLE_ENTRY G31D_MakeupBlack[];

extern const RLE_ENTRY *G31D_ExtendedBlack;

/*---------------------------------------------------------------------------*/

#define GetEncodeTermCodes(c) \
	((G3_WHITE == (c)) ? &G31D_TermWhite[0] : &G31D_TermBlack[0])
#define GetEncodeMakeupCodes(c) \
	((G3_WHITE == (c)) ? &G31D_MakeupWhite[0] : &G31D_MakeupBlack[0])
#define GetEncodeExtendedCodes(c) \
	((G3_WHITE == (c)) ? G31D_ExtendedWhite : G31D_ExtendedBlack)

/*---------------------------------------------------------------------------*/

/* a G3 encoding table */
typedef struct {
	const RLE_ENTRY *pTermCodes;
	const RLE_ENTRY *pMakeupCodes;
	const RLE_ENTRY *pExtendedCodes;
} RLE_TABLE;

extern const RLE_TABLE G31D_EncodeWhiteTbl;
extern const RLE_TABLE G31D_EncodeBlackTbl;

/*---------------------------------------------------------------------------*/

/* structure for decoding a run-length */
typedef struct {
		UINT16	rl;			/* run-length */
		SINT16	len;			/* bits used to encode it */
} RLD_ENTRY;

/*		These are "reverse" tables which enables quick lookup of a run-length
 * given the encoding (1st 13 bits...).
 */

extern const RLD_ENTRY G31D_DecodeWhiteTbl[];

extern const RLD_ENTRY G31D_DecodeBlackTbl[];

/*---------------------------------------------------------------------------*/

#define	MIN_GMT_OFFSET_YEAR	1970
#define	MAX_GMT_OFFSET_YEAR	2038
#define	NUM_GMT_OFFSET_YEARS	(MAX_GMT_OFFSET_YEAR - MIN_GMT_OFFSET_YEAR)

/* offset (sec.) of 1/1/yyyy 00:00:00 since MIN_GMT_YEAR 1/1 00:00:00 */
extern const DWORD year_GMT_offsets[/* index=year-MIN_GMT_OFFSET_YEAR */];
/* offset (sec.) of 1st of the month (non-leap year) since 1/1 of same year */
extern const DWORD month_GMT_offsets[/* 0=Jan. */];

/*---------------------------------------------------------------------------*/

#endif
