#ifndef _UTL_STR_H_
#define _UTL_STR_H_

/*
 * File: string.h
 *
 * Contents:
 *
 *		Useful procedures for handling strings.
 *
 * Created by:
 *		Lyor Goldstein.
 *
 * History:
 *		22-Nov-1992 (Lyor G.) - strings with length byte manipulations
 *		22-Feb-1995 (Lyor G.) - "C" strings manipulations only
 *		20-Jun-1995 (Lyor G.) -	useful "str..." auxiliary functions, including
 *										handling pointer(s) to last char in string ('\0')
 * Remarks:
 *
 *		Auto-includes <_types.h>, <string.h>
 */

/*---------------------------------------------------------------------------*/

#include <_types.h>
#include <util/memory.h>

#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>

#ifndef WIN32
#	ifdef __cplusplus
		extern "C" {
#	endif
			int _sntprintf (LPTSTR dst, size_t maxsize, LPCTSTR lpszFmt, ...);
			int _stprintf (LPTSTR dst, LPCTSTR lpszFmt, ...);
#	ifdef __cplusplus
		}
#	endif

#	ifdef __cplusplus
		inline int _vstprintf (LPTSTR dst, LPCTSTR fmt, va_list ap)
		{
			return vsprintf(dst, fmt, ap);
		}

		inline int _vsntprintf (LPTSTR dst, size_t count, LPCTSTR fmt, va_list ap)
		{
			return vsnprintf(dst, count, fmt, ap);
		}

		inline size_t _tcslen (LPCTSTR s)
		{
			return strlen(s);
		}

		inline LPTSTR _tcscpy (LPTSTR dst, LPCTSTR src)
		{
			return strcpy(dst, src);
		}
		
		inline LPTSTR _tcsncpy (LPTSTR dst, LPCTSTR src, size_t n)
		{
			return strncpy(dst, src, n);
		}
		
		inline LPTSTR _tcschr (LPCTSTR s, TCHAR c)
		{
			return strchr(s, c);
		}
		
		inline LPTSTR _tcsrchr (LPCTSTR s, TCHAR c)
		{
			return strrchr(s, c);
		}
		
		inline int _tcscmp (LPCTSTR s1, LPCTSTR s2)
		{
			return strcmp(s1, s2);
		}
		
		inline int _tcsncmp (LPCTSTR s1, LPCTSTR s2, size_t n)
		{
			return strncmp(s1,s1,n);
		}
		
		inline int _tcsicmp (LPCTSTR s1, LPCTSTR s2)
		{
			return strcasecmp(s1,s2);
		}
		
		inline int _tcsnicmp (LPCTSTR s1, LPCTSTR s2, size_t n)
		{
			return strncasecmp(s1,s2, n);
		}

		inline LPTSTR _tcscat (LPTSTR s1, LPCTSTR s2)
		{
			return strcat(s1, s2);
		}

		inline LPTSTR _tcsncat (LPTSTR s1, LPCTSTR s2, size_t n)
		{
			return strncat(s1, s2, n);
		}

		inline LPTSTR _tcsdup (LPCTSTR s)
		{
			return strdup(s);
		}
#	else	/* not C++ */
#		define _vstprintf(dst,fmt,ap)				vsprintf((dst),(fmt),(ap))
#		define _vsntprintf(dst,count,fmt,ap)	vsnprintf((dst),(count),(fmt),(ap))
#		define _tcslen(s)								strlen(s)
#		define _tcscpy(d,s)							strcpy((d),(s))
#		define _tcsncpy(d,s,n)						strncpy((d),(s),(n))
#		define _tcschr(s,c)							strchr((s),(c))
#		define _tcsrchr(s,c)							strrchr((s),(c))
#		define _tcscmp(s1,s2)						strcmp((s1),(s2))
#		define _tcsncmp(s1,s2,n)					strcmp((s1),(s2),(n))
#		define _tcsicmp(s1,s2)						strcasecmp((s1),(s2))
#		define _tcsnicmp(s1,s2,n)					strncasecmp((s1),(s2),(n))
#		define _tcscat(d,s)							strcat((d),(s))
#		define _tcsncat(d,s,n)						strncat((d),(s),(n))
#		define _tcsdup(s)								strdup(s)
#	endif /* of __cplusplus */
#endif

/*
 * Useful string lengths
 */

#define NAME_LENGTH				  15
#define MEDIUM_STRING_LENGTH	  30
#define ARGUMENT_LENGTH			  80
#define LONG_STRING_LENGTH		 255

/*---------------------------------------------------------------------------*/

/*
 * Number of ASCII characters used to represent different numerical values.
 */

#define MAX_BYTE_DISPLAY_LENGTH	  3
#define MAX_WORD_DISPLAY_LENGTH	  5
#define MAX_DWORD_DISPLAY_LENGTH	 10

#define MAX_BYTE_HEX_DISPLAY_LENGTH		2
#define MAX_WORD_HEX_DISPLAY_LENGTH		4
#define MAX_DWORD_HEX_DISPLAY_LENGTH	8

/*---------------------------------------------------------------------------*/

#define UPPER_CASE_HEX_OFFSET		('A' - 10)
#define LOWER_CASE_HEX_OFFSET		('a' - 10)

/*---------------------------------------------------------------------------*/

#define GBYTE_CHAR			'G'
#define MBYTE_CHAR			'M'
#define KBYTE_CHAR			'K'

#define FALSE_CHAR	'F'
#define TRUE_CHAR		'T'

/*
 * Useful chars
 */

#define DOT_CHAR			 '.'

/*
 * Character used to separate path components
 */

#if defined(_DOS_) || defined(WIN32) || defined(_WIN32)
/*
 * DOS/WIN32 specific definitions
 */
#	define FULL_PATH_CHAR	 '\\'
#	define FULL_PATH_STR		"\\"
#	define CURDIR_STR		"."
#	define PARENTDIR_STR	".."
#endif

/*
 * UNIX specific definitions
 */
#if defined(_UNIX_) || defined(_VXWORKS_)
#	define FULL_PATH_CHAR	 '/'
#	define FULL_PATH_STR		"/"
#	define CURDIR_STR		"."
#	define PARENTDIR_STR	".."
#endif

#ifdef _RMX_
/*
 * RMX specific definitions
 */
#	define FULL_PATH_CHAR	 '/'
#	define FULL_PATH_STR		"/"
#	define CURDIR_STR		":$:"
#	define PARENTDIR_STR	"^"
#endif

#ifndef FULL_PATH_CHAR
#	error "FULL_PATH_CHAR not defined"
#endif

#ifndef FULL_PATH_STR
#	error "FULL_PATH_STR not defined"
#endif

#ifndef CURDIR_STR
#	error "CURDIR_STR not defined"
#endif

#ifndef PARENTDIR_STR
#	error "PARENTDIR_STR not defined"
#endif

/*---------------------------------------------------------------------------*/

/*
 *		Maximum length of directory & file components - these value include
 * possible terminating '\0'.
 */

#if defined(_DOS_) || defined(WIN32)
#	define MAX_DNLEN			_MAX_DIR
#	define MAX_FNLEN			_MAX_FNAME
#	define MAX_FPATH_LEN		_MAX_PATH
#else
#	define MAX_DNLEN			192
#	define MAX_FNLEN			64
#	define MAX_FPATH_LEN		((MAX_DNLEN)+(MAX_FNLEN)+2)
#endif

/*---------------------------------------------------------------------------*/

/*
 * enumerates the possible sorting order of lists/strings
 *
 * NOTE: the assigned values are according to "strcmp".
 */

typedef enum enum_so_order_case_type {
						SO_DESCENDING_ORDER_CASE	=	(-1),
						SO_ASCENDING_ORDER_CASE		=	1,
						SO_NO_ORDER_CASE				=	0,
						BAD_SO_ORDER_CASE				=	2
} SO_ORDER_CASE_TYPE;

#ifdef __cplusplus
inline BOOLEAN is_bad_so_order_case (SO_ORDER_CASE_TYPE s)
{
	if (abs((int) s) > 1)
		return(TRUE);
	else
		return(FALSE);
}
#else
#	define is_bad_so_order_case(s) (abs(s) > 1)
#endif

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
inline BOOLEAN IsEmptyStr (LPCTSTR lpszS)
{
	return ((NULL == lpszS) || (_T('\0') == *lpszS));
}

inline UINT32 GetSafeStrlen (LPCTSTR lpszS)
{
	return ((NULL == lpszS) ? 0 : _tcslen(lpszS));
}

inline LPCTSTR GetSafeStrPtr (LPCTSTR lpszS)
{
	return ((NULL == lpszS) ? _T("") : lpszS);
}

inline BOOLEAN IsSafeSpace (const TCHAR ch)
{
	return _istspace(((int) ch) & 0x000000ff);
}

inline BOOLEAN IsSafeDigit (const TCHAR ch)
{
	return _istdigit(((int) ch) & 0x000000ff);
}

inline BOOLEAN IsSafeXDigit (const TCHAR ch)
{
	return _istxdigit(((int) ch) & 0x000000ff);
}

inline BOOLEAN IsSafePrintable (const TCHAR ch)
{
	return _istprint(((int) ch) & 0x000000ff);
}

inline BOOLEAN IsSafeAlpha (const TCHAR ch)
{
	return _istalpha(((int) ch) & 0x000000ff);
}
#else
#	define IsEmptyStr(s)			((NULL == (s)) || (_T('\0') == (*(s))))
#	define GetSafeStrlen(s)		((NULL == (s)) ? 0 : _tcslen(s))
#	define GetSafeStrPtr(s)		((NULL == (s)) ? _T("") : (s))
#	define IsSafeSpace(c)		_istspace(((int) (c)) & 0x000000ff)
#	define IsSafeDigit(c)		_istdigit(((int) (c)) & 0x000000ff)
#	define IsSafeXDigit(c)		_istxdigit(((int) (c)) & 0x000000ff)
#	define IsSafePrintable(c)	_istprint(((int) ch) & 0x000000ff)
#	define IsSafeAlpha(c)		_istalpha(((int) ch) & 0x000000ff)
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

/* counts until NULL/empty string found (Note: returns 0 if NULL entries array) */
extern DWORD count_string_entries (const char *entries[]);

/*
 *	Scans the "entries" array for the given "str_entry" until NULL pointer entry
 * or end of entries encountered.
 *
 *	If unsuccessful returns a value which is <= "entries_num"
 */

extern DWORD find_string_entry (const char		str_entry[],
										  const char		*entries[],
										  const DWORD		entries_num,
										  const BOOLEAN	fCaseSensitive);

/*---------------------------------------------------------------------------*/
/*							ARGUMENT_TO_....
 *					-----------------------------------
 *		Returns the byte/word/dword value stored in the array of char(s).
 *
 * Parameters:
 *
 *		[IN]	arg_p - pointer to first char in array.
 *		[IN]	arg_len - number of char(s) in array.
 *		[OUT] exc - exception code - if non-EOK then returned value should be
 *						  ignored because an error occurred (e.g non-numerical char).
 */
/*-------------------------------------------------------------------*/

extern BYTE argument_to_byte (const char arg_p[],
										size_t	  arg_len,
										EXC_PARAM(exc));

extern BYTE hex_argument_to_byte (const char arg_p[],
											 size_t		arg_len,
											 EXC_PARAM(exc));

extern WORD argument_to_word (const char arg_p[],
										size_t	  arg_len,
										EXC_PARAM(exc));

extern WORD hex_argument_to_word (const char arg_p[],
											 size_t		arg_len,
											 EXC_PARAM(exc));

extern DWORD argument_to_dword (const char arg_p[],
										  size_t		 arg_len,
										  EXC_PARAM(exc));

extern DWORD hex_argument_to_dword (const char arg_p[],
												size_t	  arg_len,
												EXC_PARAM(exc));

/*---------------------------------------------------------------------------*/
/*									arg2b/w/dw
 *									----------
 *		Translate string to BYTE/WORD/DWORD. The string format (i.e. HEX/DEC) is
 * determined by looking at the string prefix: if "0x..." then HEX, otherwise
 * DEC is assumed. If string length is less (or equal) than 2 then DEC is the
 * default.
 */
/*---------------------------------------------------------------------------*/

extern BYTE arg2b (const char val[], size_t arg_len, EXC_PARAM(exc));
extern WORD arg2w (const char val[], size_t arg_len, EXC_PARAM(exc));
extern DWORD arg2dw (const char val[], size_t arg_len, EXC_PARAM(exc));

/*---------------------------------------------------------------------------*/
/*						BYTE/WORD/DWORD_TO(HEX_)_ARGUMENT
 *						---------------------------------
 *		Sets the argument parameter to the string containing the (hex) number.
 * returns number of characters used.
 */
/*---------------------------------------------------------------------------*/

extern size_t byte_to_argument (BYTE in_byte, char arg[]);

extern size_t word_to_argument (WORD in_word, char arg[]);

extern size_t dword_to_argument (DWORD in_dword, char arg[]);

/*---------------------------------------------------------------------------*/

extern size_t byte_to_hex_argument (BYTE in_byte, char arg[]);

extern size_t word_to_hex_argument (WORD in_word, char arg[]);

extern size_t dword_to_hex_argument (DWORD in_dword, char arg[]);

extern size_t pointer_to_argument (void	*p, char	 str[]);

/*---------------------------------------------------------------------------*/

/*						BYTE/WORD/DWORD_TO_FIXED_ARGUMENT
 *						---------------------------------
 *		Sets the argument parameter to the string containing the number. Left
 * pads the string with the specified filling character up to specified len.
 * Returns number of characters used (which is excatly "len").
 *
 * Note: if filling char is '\0' then result is not padded and the returned
 *			length is same as "normal" byte/word/dword_to_argument
 */
extern size_t byte_to_fixed_argument (BYTE in_byte, char arg[], size_t len, char padChar, BOOLEAN leftPad);

extern size_t word_to_fixed_argument (WORD in_word, char arg[], size_t len, char padChar, BOOLEAN leftPad);

extern size_t dword_to_fixed_argument (DWORD in_dword, char arg[], size_t len, char padChar, BOOLEAN leftPad);

/*---------------------------------------------------------------------------*/

extern BYTE hex_digit_to_byte (char xdigit, EXC_PARAM(exc));

extern char byte_to_hex_digit (BYTE byte_val, EXC_PARAM(exc));

/*---------------------------------------------------------------------------*/
/*									memory_to_argument
 *									------------------
 *		Converts a value representing a memory size to an ASCII argument. The
 * display format is "nnn.xxx" where:
 *
 *			nnn - the "FIXED" part in G/M/Kbytes.
 *			xxx - the "MANTISSA" - up to 3 digits precision.
 *
 *			(e.g. 2,100,326 bytes ~ 2.100M)
 *
 * NOTE: the procedure adds G/M/K - according to the case.
 */
/*---------------------------------------------------------------------------*/

extern size_t memory_to_argument (DWORD	memval, char arg_p[]);

extern DWORD argument_to_memory (const char arg_p[], EXC_PARAM(exc));

/*
 *		Converts a version/release number to a string. The version structure is
 * assumed to be <major>.<minor>.<release> (example: 01.03.06)) - the
 * <release> is optional.
 *
 * Returns created string length
 */

#define VERSEP_CHAR '.'

extern size_t ver2arg (UINT8 majorNum, UINT8 minorNum, UINT8 rlsNum,
							  char relCh,char arg[]);

extern EXC_TYPE arg2ver (const char arg[],
#ifdef __cplusplus
						UINT8& majorNum, UINT8& minorNum, UINT8& rlsNum,char& relCh
#else
						UINT8 *majorNum, UINT8 *minorNum, UINT8 *rlsNum,char *relCh
#endif /* of C++ */
	);

/*---------------------------------------------------------------------------*/

extern size_t boolean_to_argument (BOOLEAN	bval, char arg_p[]);

extern char boolean_to_char (BOOLEAN	bval);

extern BOOLEAN argument_to_boolean (const char arg_p[],	EXC_PARAM(exc));

extern BOOLEAN char_to_boolean (char boolch,	 EXC_PARAM(exc));

/*---------------------------------------------------------------------------*/
/*								concat_fname
 *								------------
 *		Adds a file name component to an existing path.
 *
 * Parameters:
 *
 *		[I/O] path_p - the partial path this far.
 *		[IN]	fname_p - name of file component to be added.
 *		[IN]	max_path_length - maximum length of resulting path.
 *
 * NOTES: 1. path_p/fname_p must be DISTINCT pointers !!!
 *			 2. if resulting path exceeds the allowed maximum then the procedure
 *			 has no effect.
 *			 3. the result is placed BACK in path_p.
 *			 4. if the path_p does not end in ':' or '/' then '/' is added BEFORE
 *			 the fname is added.
 */
/*---------------------------------------------------------------------------*/

extern size_t concat_fname (char			path_p[],
									 const char fname_p[],
									 size_t		max_path_length);

/*---------------------------------------------------------------------------*/
/*							CONCAT(_HEX)_BYTE/WORD/DWORD
 *							----------------------------
 *		Concatenates the byte/word/dword (hex) value to the string.
 */
/*---------------------------------------------------------------------------*/

extern size_t concat_byte (char		arg[],
									BYTE		byte_value,
									size_t	max_arg_length);

extern size_t concat_word (char		  arg[],
								  WORD		 word_value,
								  size_t		  max_arg_length);

extern size_t concat_dword (char			arg[],
									DWORD		  dword_value,
									size_t		max_arg_length);

extern size_t concat_hex_byte (char		arg[],
										BYTE	  byte_value,
										size_t	max_arg_length);

extern size_t concat_hex_word (char		arg[],
										WORD	  word_value,
										size_t	max_arg_length);

extern size_t concat_hex_dword (char	 arg[],
										 DWORD	dword_value,
										 size_t	 max_arg_length);

extern size_t concat_pointer (char	str[],
									  void  *p,
									  size_t max_arg_length);

/*---------------------------------------------------------------------------*/
/*							BYTE/WORD/DWORD_ARRAY_TO_HEX_STRING
 *		Converts a byte array, given its length, to a string of hex	characters.
 *
 *		out_len - maximum allowed length of out_string - if more needed, then
 *		  only this length is used.
 *
 *		delim - delimter to use between successive values - if '\0' then no
 *			delimiter is used.
 */
/*---------------------------------------------------------------------------*/

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER for non-HEX (otherwise parsing is impossible)
extern size_t byte_array_to_formatted_string (const BYTE		array_in[],
															 const size_t	array_len,
															 char				out_string[],
															 const size_t	out_len,
															 const char		delim,
															 const BOOLEAN	fUseHex);

extern size_t word_array_to_formatted_string (const WORD		array_in[],
															 const size_t	array_len,
															 char				out_string[],
															 const size_t	out_len,
															 const char		delim,
															 const BOOLEAN	fUseHex);

extern size_t dword_array_to_formatted_string (const DWORD		array_in[],
															  const size_t		array_len,
															  char				out_string[],
															  const size_t		out_len,
															  const char		delim,
															  const BOOLEAN	fUseHex);
#ifdef __cplusplus
inline size_t byte_array_to_hex_string (const BYTE		array_in[],
													 const size_t	array_len,
													 char				out_string[],
													 const size_t	out_len,
													 const char		delim)
{
	return byte_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, TRUE);
}

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
inline size_t byte_array_to_string (const BYTE		array_in[],
												const size_t	array_len,
												char				out_string[],
												const size_t	out_len,
												const char		delim)
{
	return byte_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, FALSE);
}

inline size_t word_array_to_hex_string (const WORD		array_in[],
													 const size_t	array_len,
													 char				out_string[],
													 const size_t	out_len,
													 const char		delim)
{
	return word_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, TRUE);
}

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
inline size_t word_array_to_string (const WORD		array_in[],
												const size_t	array_len,
												char				out_string[],
												const size_t	out_len,
												const char		delim)
{
	return word_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, FALSE);
}

inline size_t dword_array_to_hex_string (const DWORD	array_in[],
													  const size_t	array_len,
													  char			out_string[],
													  const size_t	out_len,
													  const char	delim)
{
	return dword_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, TRUE);
}

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
inline size_t dword_array_to_string (const DWORD	array_in[],
												 const size_t	array_len,
												 char				out_string[],
												 const size_t	out_len,
												 const char		delim)
{
	return dword_array_to_formatted_string(array_in, array_len, out_string, out_len, delim, FALSE);
}
#else
extern size_t byte_array_to_hex_string (const BYTE		array_in[],
													 const size_t	array_len,
													 char				out_string[],
													 const size_t	out_len,
													 const char    delim);

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
extern size_t byte_array_to_string (const BYTE		array_in[],
												const size_t	array_len,
												char				out_string[],
												const size_t	out_len,
												const char		delim);

extern size_t word_array_to_hex_string (const WORD		array_in[],
													 const size_t	array_len,
													 char				out_string[],
													 const size_t	out_len,
													 const char		delim);

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
extern size_t word_array_to_string (const WORD		array_in[],
												const size_t	array_len,
												char				out_string[],
												const size_t	out_len,
												const char		delim);

extern size_t dword_array_to_hex_string (const DWORD	array_in[],
													  const size_t	array_len,
													  char			out_string[],
													  const size_t	out_len,
													  const char	delim);

// NOTE: HIGHLY RECOMMENDED TO USE SOME DELIMITER (otherwise parsing is impossible)
extern size_t dword_array_to_string (const DWORD	array_in[],
												 const size_t	array_len,
												 char				out_string[],
												 const size_t	out_len,
												 const char		delim);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/
/*							HEX_STRING_TO_BYTE/WORD/DWORD_ARRAY
 *		Converts a string to an array, given its length
 *
 *		out_len - maximum allowed length of out_array - if more needed, then
 *		  only this length is used.
 *
 *		delim - delimter to use between successive values - if '\0' then no
 *			delimiter is used.
 *
 * Returns actual number of bytes translated
 *
 * NOTE: if during its execution an illegal value is detected then execution
 *			is aborted and a non-EOK value is returned in "exc"
 */
/*-------------------------------------------------------------------*/

/* NOTE: returns error if no delimiter specified for non-HEX format */
extern size_t formatted_string_to_byte_array (const char		str_in[],
															 const size_t	str_len,
															 BYTE				out_array[],
															 const size_t	out_len,
															 const char		delim,
															 const BOOLEAN	isHex,
															 EXC_PARAM(exc));

extern size_t formatted_string_to_word_array (const char		str_in[],
															 const size_t	str_len,
															 WORD				out_array[],
															 const size_t	out_len,
															 const char		delim,
															 const BOOLEAN	isHex,
															 EXC_PARAM(exc));

extern size_t formatted_string_to_dword_array (const char		str_in[],
															  const size_t		str_len,
															  DWORD				out_array[],
															  const size_t		out_len,
															  const char		delim,
															  const BOOLEAN	isHex,
															  EXC_PARAM(exc));

#ifdef __cplusplus	/* for C++ we use inline functions */
inline size_t hex_string_to_byte_array (const char		str_in[],
													 const size_t	str_len,
													 BYTE				out_array[],
													 const size_t	out_len,
													 const char    delim,
													 EXC_PARAM(exc))
{
	return formatted_string_to_byte_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

inline size_t string_to_byte_array (const char		str_in[],
												const size_t	str_len,
												BYTE				out_array[],
												const size_t	out_len,
												const char		delim,
												EXC_PARAM(exc))
{
	return formatted_string_to_byte_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

inline size_t hex_string_to_word_array (const char		str_in[],
													 const size_t	str_len,
													 WORD				out_array[],
													 const size_t	out_len,
													 const char    delim,
													 EXC_PARAM(exc))
{
	return formatted_string_to_word_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

inline size_t string_to_word_array (const char		str_in[],
												const size_t	str_len,
												WORD				out_array[],
												const size_t	out_len,
												const char		delim,
												EXC_PARAM(exc))
{
	return formatted_string_to_word_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

inline size_t hex_string_to_dword_array (const char	str_in[],
													  const size_t	str_len,
													  DWORD			out_array[],
													  const size_t	out_len,
													  const char   delim,
													  EXC_PARAM(exc))
{
	return formatted_string_to_dword_array(str_in, str_len, out_array, out_len, delim, TRUE, exc);
}

/* NOTE: returns error if no delimiter specified */
inline size_t string_to_dword_array (const char		str_in[],
												 const size_t	str_len,
												 DWORD			out_array[],
												 const size_t	out_len,
												 const char		delim,
												 EXC_PARAM(exc))
{
	return formatted_string_to_dword_array(str_in, str_len, out_array, out_len, delim, FALSE, exc);
}
#else
extern size_t hex_string_to_byte_array (const char		str_in[],
													 const size_t	str_len,
													 BYTE				out_array[],
													 const size_t	out_len,
													 const char    delim,
													 EXC_PARAM(exc));

extern size_t string_to_byte_array (const char		str_in[],
												const size_t	str_len,
												BYTE				out_array[],
												const size_t	out_len,
												const char		delim,
												EXC_PARAM(exc));

extern size_t hex_string_to_word_array (const char		str_in[],
													 const size_t	str_len,
													 WORD				out_array[],
													 const size_t	out_len,
													 const char    delim,
													 EXC_PARAM(exc));

extern size_t string_to_word_array (const char		str_in[],
												const size_t	str_len,
												WORD				out_array[],
												const size_t	out_len,
												const char		delim,
												EXC_PARAM(exc));

extern size_t hex_string_to_dword_array (const char	str_in[],
													  const size_t	str_len,
													  DWORD			out_array[],
													  const size_t	out_len,
													  const char   delim,
													  EXC_PARAM(exc));

/* NOTE: returns error if no delimiter specified */
extern size_t string_to_dword_array (const char		str_in[],
												 const size_t	str_len,
												 DWORD			out_array[],
												 const size_t	out_len,
												 const char		delim,
												 EXC_PARAM(exc));
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

/*		Taks a rather freely formatted string representing some address in hex
 * format, and returns it in a more strict format. The routine assumes that
 * "in_haddr" contains an address composed of hex bytes, with possibly "white
 * space" (tabs, spaces) and delimiter(s) from "in-delims". After execution,
 * the address is returned as hex bytes separated by the specified "out-delim".
 *
 * Example: IN -	39-00A231BB-42BD-800000000000-00:A2:33:42:FF:02-00
 *							in_delims="-:"
 *
 *				OUT - 39:00:A2:31:BB:42:BD:80:00:00:00:00:00:00:A2:33:42:FF:02:00
 *							out_delim=':'
 */

extern size_t hex_address_str_format (const char	in_haddr[],
												  size_t			in_len,
												  const char	in_delims[],
												  char			out_haddr[],
												  size_t			out_len,
												  char			out_delim);

/*---------------------------------------------------------------------------*/
/*		Formats the "in-addr" address string according to the "out-format"
 * pattern. The "in-addr" string is assumed to contain HEX bytes representing
 * an address, and separated by "in-delim" (if '\0' then no delimiter is used).
 * The address is re-formatted according to the "out-format" as follows:
 *
 *		- the '%' modifier is used to specify format modifier(s). Any text
 *		preceding this modifier is copied to the output string "as-is" (use
 *		"%%" for the '%' char)
 *
 *		- modifiers are of the form [delim]<len> - where:
 *
 *				len - the number of HEX bytes which form this address component
 *				delim - is an optional delimiter to separate each one of the
 *					<len> bytes for this address component.
 *
 *		Example:	in="39:00:33:A2:00:12:34:25:00:AA:00:32:25:1F:01"
 *					format="%1-%7-%:6-%1"
 *
 *					out="39-0033A200123425-00:AA:00:32:25:1F-01"
 *
 *		- if format is NULL or empty then input is copied to output "as-is"
 *
 *		- if format does not "cover" all values in "in-addr" then only the
 *		"covered" values are used. Alternatively, if more values are required
 *		in the format than available, then processing is aborted.
 *
 *	Returns number of characters in "out-addr" (i.e. len of "out-addr").
 */
/*---------------------------------------------------------------------------*/

extern size_t hex_address_format (const char	in_addr[],
											 size_t		in_len,
											 char			in_delim,
											 char			out_addr[],
											 size_t		out_len,
											 const char	out_format[]);

/*---------------------------------------------------------------------------*/
/* Returns pointer to exception code string, or NULL if not found
 */
/*---------------------------------------------------------------------------*/

extern const char *exc_code2string (const EXC_TYPE exc);

/*---------------------------------------------------------------------------*/
/*								dqdecodeexception
 *								-----------------
 *		Returns a string explaining the value encoded in the "exc" parameter.
 * Function returns length of returned string.
 */
/*---------------------------------------------------------------------------*/

extern size_t dqdecodeexception (const EXC_TYPE exc, char *str);

extern EXC_TYPE dqencodeexception (const char *exc_str);

/*---------------------------------------------------------------------------*/
/*									bdt_xlate_to_ascii
 *									------------------
 *		Translates a buffer of BAUDOT encoded characters to the equivalent ASCII
 * encoding.
 *
 * Parameters:
 *
 *		[IN]	bdt_buf - buffer containing the BAUDOT encoded characters.
 *		[IN]	bdt_buf_len - number of characters in the buffer.
 *		[IN]	last_bdt_tbl - last BAUDOT table used (see note(s)..) - if neither
 *					BDT_FS or BDT_LS, then unknown last table, then routine will
 *					"synchronize" itself (see note(s)...).
 *		[IN]	ascii_buf - buffer into which the translation is to be placed.
 *
 *					NOTE: ASCII buffer should be the same length as the BAUDOT
 *							buffer (although usually less space is needed...).
 *
 *		[OUT] ascii_buf_len - number of characters in ASCII buf.
 *
 * Returned value is last BAUDOT table used (see note(s)...).
 *
 * NOTE: the routine enables translation of SUCCESSIVE BUFFERS. This is done
 *			by following the letters/figures shitf(s) from one call to another.
 *			The routine checks the "last_bdt_tbl" to see if there is some prior
 *			knowledge as to which table to use - if no BDT_LS/FS value is provided
 *			the it will try to find the first such value by DISCARDING ALL VALUES
 *			UP TO THE BDT_LS/FS (!!). In any case, the routine returns the last
 *			synchroniztion value (BDT_LS/FS) if successful (or something else
 *			otherwise). When the routine is called with the next buffer, the
 *			last returned value should be supplied.
 *
 *			Example:
 *
 *				tbl1 = bdt_xlate_to_ascii(buf1, len1, BDT_NULL, outbuf1, &outlen1);
 *
 *					The "BDT_NULL" denotes not having any prior knowledge.
 *
 *				tbl2 = bdt_xlate_to_ascii(buf2, len2, tbl1, outbuf2, &outlen2);
 *
 *					The "tbl1" denotes the value returned by the 1st call.
 */
/*---------------------------------------------------------------------------*/

extern BYTE bdt_xlate_to_ascii (BYTE	 bdt_buf[],
										  size_t	 bdt_buf_len,
										  BYTE	 last_bdt_tbl,
										  char	 ascii_buf[],
										  size_t	 *ascii_buf_len);

/*---------------------------------------------------------------------------*/

/*		The Quoted-Printable encoding REQUIRES that encoded lines be no more
 * than 76 characters long (not including CRLF).
 */
#define MAX_QP_LINE_LENGTH	76

/* Each Quoted-Printable char is represenred by a '=' followed by a HEX value */
#define QPCHAR_DISPLAY_LENGTH		(MAX_BYTE_HEX_DISPLAY_LENGTH+1)

#define PLAIN2QPSIZE(plainSize)	((plainSize)*QPCHAR_DISPLAY_LENGTH)

#define ISOENCDELIM		'?'
#define ISOSTRDELIM		'='
#define ISOQPENCDELIM	'Q'
#define ISOB64ENCDELIM	'B'

/* TRUE if character can be transfered as-is without any encoding */
extern BOOLEAN fIsQPXferChar (const TCHAR c);

/*---------------------------------------------------------------------------*/

/* Note: caller must make sure that enough room is available (including '\0') */
extern EXC_TYPE add_QP_encoding (const char cVal, char **lppQP);

/* returns value > "qp_len" if error, converted len otherwise.
 *
 * if "fConvAll" is false then only non-printable characters are converted
 */
extern size_t ascii2quotedprintable (const char		ascii_txt[],
												 const size_t	ascii_len,
												 char				qp_txt[],
												 const size_t	qp_len,
												 const BOOLEAN	fConvAll);

/* Note: assumes enough data in QP encoding */
extern EXC_TYPE decode_QP (const char **lppQP, char *lpch);

/* returns value > "ascii_len" if error, converted len otherwise */
extern size_t quotedprintable2ascii (const char		qp_txt[],
												 const size_t	qp_len,
												 char				ascii_txt[],
												 const size_t	ascii_len);

/* if no encoding found then NULL(s) are returned */
extern EXC_TYPE extract_iso_encoding (const char	*lpszOrgStr,
												  const char	**lppszCharSet,
												  UINT32			*pulCSLen,
												  const char	**lppszEncType,
												  const char	**lppszEncTxt,
												  UINT32			*pulTxtLen);

/* looks for "=?ISO-xxxx-n?Q?.....?=" encoded string and decodes it */
extern EXC_TYPE convert_ISOQP_string (const char lpszVal[], char lpszStr[], const UINT32 ulMaxSize);

/*---------------------------------------------------------------------------*/
/*										reverse_string
 *										--------------
 *		Reverses the ORDER of the characters in the string.
 */
/*---------------------------------------------------------------------------*/

extern void reverse_chars (char	str[], size_t slen);

#define reverse_string(str) \
	reverse_chars((str), (WORD) GetSafeStrlen((const char *) (str)))

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

extern void get_fpath_components (const char	  fpath_p[],
													 char	  fname_p[],
													 char	  ext_p[]);
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

extern size_t expand_fpath (char fpath[]);

/*---------------------------------------------------------------------------*/
/*		String handling functions which use the pointer to LAST char for more
 * efficient execution.
 */
/*---------------------------------------------------------------------------*/

/*
 *		Returns pointer to end of string (i.e. '\0' char)
 */

#define strlast(s) strchr(s,'\0')
#define strcatlf(s) strcat(s,"\n")

/*---------------------------------------------------------------------------*/

/*
 *		Copies a string to another, and returns the pointer to END of result
 * string (i.e. '\0' char).
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

extern char *strlcpy (char *lps, const char *s);

/*
 *		Concatenates a string to another and returns pointer to END of string.
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

#define strlcat(lps,s) strlcpy(lps,s)

/*
 *		Copies a string to another, and returns the pointer to end of result
 * string (i.e. '\0' char). Stops when either whole string has been copied, or
 * "max" characters copied - whichever comes first
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

extern char *strlncpy (char *lps, const char *s, size_t maxlen);

/*
 *		Concatenates a string to another and returns pointer to end of string.
 *
 * NOTE: does not check that destination is indeed '\0'...
 */

#define strlncat(lps,s,maxlen) strlncpy(lps,s,maxlen)

/*
 * Adds a character to the string and returns pointer to position after it
 * (NOTE: "lps" is ASSUMED to point to last char (i.e. '\0'))
 */

extern char *strladdch (char *lps, char c);

#define strlcatlf(s) strladdch(s,'\n')

/*
 *		Returns index (starting at zero) of the character in the string. If
 * character not found, it returns a value which is greater than the string's
 * length (NOTE: trying to find '\0' with this function has unforseen results).
 */

extern size_t strndx (const char *s, char c);

extern size_t strndxr (const char *s, char c);

/*
 *		Returns pointer to 1st character in string which equals the required one.
 * It checks up to end of string or up to "slen" characters - whichever comes
 * first. Returns NULL if not successful
 */

extern char *strnchr (const char *s, const char c, size_t slen);
extern char *strnrchr (const char *s, const char c, size_t slen);

extern char *stristr (const char *s, const char *ss);

#if defined(UNICODE) || defined(_UNICODE)
#	error	"Unicode N/A"
#else
#	define _tcsnchr(s,c,l) strnchr((s),(c),(l))
#	define _tcsnrchr(s,c,l) strnrchr((s),(c),(l))
#	define _tcsistr(s,ss) stristr((s),(ss))
#endif	/* UNICODE */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE strupdatebuf (LPCTSTR lpszSrc, const UINT32 ulSLen, LPTSTR& lpszDst);

inline EXC_TYPE strupdatebuf (LPCTSTR lpszSrc, LPTSTR& lpszDst)
{
	return strupdatebuf(lpszSrc, ((NULL == lpszSrc) ? 0 : _tcslen(lpszSrc)), lpszDst);
}

inline void strreleasebuf (LPTSTR& lpszBuf)
{
	if (lpszBuf != NULL)
	{
		delete [] lpszBuf;
		lpszBuf = NULL;
	}
}
#endif /* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
//////////////////////////////////////////////////////////////////////////////
class ISearchString {
	protected:
		BOOLEAN	m_fCaseSensitive;

	public:
		ISearchString (const BOOLEAN fCaseSensitive=FALSE)
			: m_fCaseSensitive(m_fCaseSensitive)
		{
		}

		// Setting a zero length pattern actually clears everything
		virtual EXC_TYPE SetPattern (LPCTSTR lpszPattern, const UINT32 ulPatLen, const BOOLEAN fCaseSensitive=FALSE) = 0;

		// Setting a NULL pattern actually clears everything
		virtual EXC_TYPE SetPattern (LPCTSTR lpszPattern, const BOOLEAN fCaseSensitive=FALSE)
		{
			return SetPattern(lpszPattern, ::GetSafeStrlen(lpszPattern), fCaseSensitive);
		}

		virtual EXC_TYPE Clear ()
		{
			return SetPattern(NULL);
		}

		virtual LPCTSTR GetPattern () const = 0;

		virtual BOOLEAN IsCaseSensitiveSearch () const
		{
			return m_fCaseSensitive;
		}

		virtual ISearchString& operator= (LPCTSTR lpszPattern)
		{
			EXC_TYPE	exc=SetPattern(lpszPattern);
			return *this;
		}

		virtual LPCTSTR FindInSearchBuffer (LPCTSTR lpszSearchBuf, const UINT32 ulSearchLen) const = 0;

		virtual LPCTSTR FindSubstringIn (LPCTSTR lpszSearchBuf) const
		{
			return FindInSearchBuffer(lpszSearchBuf, ::GetSafeStrlen(lpszSearchBuf));
		}

		virtual BOOLEAN IsSubStringInBuffer (LPCTSTR lpszSearchBuf, const UINT32 ulSearchLen) const;

		virtual BOOLEAN IsSubStringOf (LPCTSTR lpszSearchBuf) const
		{
			return IsSubStringInBuffer(lpszSearchBuf, ::GetSafeStrlen(lpszSearchBuf));
		}

		virtual ~ISearchString () { }
};
//////////////////////////////////////////////////////////////////////////////
#endif	/* __cplusplus */

#ifdef __cplusplus
//////////////////////////////////////////////////////////////////////////////

// implements case insensitive search
class CBoyerMooreSearchString : public ISearchString {
	private:
		LPTSTR	m_lpszPattern;
		UINT32	m_ulPatLen;
		UINT8		m_PatternMask[UINT8_MAX+1];

		static const UINT8 m_AlphaTab[UINT8_MAX+1];

	public:
		// default constructor
		CBoyerMooreSearchString ()
			: ISearchString(FALSE), m_lpszPattern(NULL), m_ulPatLen(0)
		{
			SetPattern(NULL, 0, FALSE);
		}

		CBoyerMooreSearchString (LPCTSTR lpszPattern, const BOOLEAN fCaseSensitive=FALSE)
			: ISearchString(fCaseSensitive), m_lpszPattern(NULL), m_ulPatLen(0)
		{
			SetPattern(lpszPattern, ::GetSafeStrlen(lpszPattern), fCaseSensitive);
		}

		CBoyerMooreSearchString (LPCTSTR lpszPattern, const UINT32 ulPatLen, const BOOLEAN fCaseSensitive=FALSE)
			: ISearchString(fCaseSensitive), m_lpszPattern(NULL), m_ulPatLen(0)
		{
			SetPattern(lpszPattern, ulPatLen, fCaseSensitive);
		}

		// Setting a zero length pattern actually clears everything
		virtual EXC_TYPE SetPattern (LPCTSTR lpszPattern, const UINT32 ulPatLen, const BOOLEAN fCaseSensitive=FALSE);

		virtual LPCTSTR GetPattern () const
		{
			return m_lpszPattern;
		}

		virtual LPCTSTR FindInSearchBuffer (LPCTSTR lpszSearchBuf, const UINT32 ulSearchLen) const;

		virtual ~CBoyerMooreSearchString ()
		{
			::strreleasebuf(m_lpszPattern);
		}
};
//////////////////////////////////////////////////////////////////////////////
#endif	/* __cplusplus */

/*
 * Returns number of skipped characters
 */

extern size_t strskipwspace (const char str[]);
extern size_t strnskipwspace (const char str[], size_t n);

/*
 *    Returns pointer to first character which DOES NOT equal the requested one
 * or NULL if unsuccessful.
 */

extern const char *strskip (const char str[], char c);
extern const char *strnskip (const char str[], char c, size_t len);

/*
 * Skip (HEX) value - returns pointer to first non-(HEX)digit char
 *
 * NOTE: if input is NULL, then it returns NULL
 */

extern const char *strskipxnum (const char s[]);
extern const char *strskipnum (const char s[]);

/*---------------------------------------------------------------------------*/

/* Note: if padding char is '\0' or string already has requested length then
 *			nothing is done.
 *
 * Returns added padding length
 */
extern size_t strpad (char arg[], size_t len, char padChar, BOOLEAN leftPad);

#define strleftpad(s,l,p) strpad(s,l,p,TRUE)
#define strrightpad(s,l,p) strpad(s,l,p,FALSE)

/*---------------------------------------------------------------------------*/

extern size_t strbuildtime (BYTE hour, BYTE minute, BYTE second, char arg[]);

/*---------------------------------------------------------------------------*/

extern EXC_TYPE strlinschars (LPTSTR			*lppszCurPos,
										LPCTSTR			lpszChars,	/* may be NULL if 0 len */
										const UINT32	ulCLen,
										UINT32			*pulRemLen);

extern EXC_TYPE strlinsstr (LPTSTR	*lppszCurPos,
									 LPCTSTR	lpszStr,	/* may be NULL/empty */
									 UINT32	*pulRemLen);

extern EXC_TYPE strlinsch (LPTSTR		*lppszCurPos,
									const TCHAR	tch,	/* may NOT be '\0' */
									UINT32		*pulRemLen);

extern EXC_TYPE strlinsnum (LPTSTR			*lppszCurPos,
									 const UINT32	ulVal,
									 UINT32			*pulRemLen);

extern EXC_TYPE strlinspadnum (LPTSTR			*lppszCurPos,
										 const UINT32	ulVal,
										 const UINT32	ulVLen,	/* may NOT be 0 */
										 const TCHAR	padCh,	/* may NOT be '\0' */
										 const BOOLEAN	fLeftPad,
										 UINT32			*pulRemLen);

extern EXC_TYPE strlinstime (LPTSTR			*lppszCurPos,
									  const UINT8	bHour,
									  const UINT8	bMinute,
									  const UINT8	bSecond,
									  UINT32			*pulRemLen);

/*	iTimezone is the difference (in seconds) between the GMT and LOCAL time */
extern EXC_TYPE strlinsGMTOffset (LPTSTR		*lppszCurPos,
											 const int	iTimeZone,	/* (-1) == use internal */
											 UINT32		*pulRemLen);

extern EXC_TYPE strlinseos (LPTSTR	*lppszCurPos, UINT32 *pulRemLen);

extern EXC_TYPE strlinsvf (LPTSTR	*lppszCurPos, LPCTSTR lpszFmt, va_list ap, UINT32 *pulRemLen);

extern EXC_TYPE strlinsf (LPTSTR	*lppszCurPos, UINT32 *pulRemLen, LPCTSTR lpszFmt, ...);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// incremental string builder interface
class IStrlBuilder {
	protected:
		IStrlBuilder () { }

	public:
		virtual LPCTSTR GetBuffer () const = 0;

		virtual LPCTSTR GetCurPos () const = 0;

		virtual UINT32 GetCurLen () const;

		virtual void Reset () = 0;

		virtual EXC_TYPE AddChars (LPCTSTR lpszChars, const UINT32 ulCLen) = 0;

		virtual EXC_TYPE AddNonEmptyChars (LPCTSTR lpszChars, const UINT32 ulCLen);

		virtual EXC_TYPE AddStr (LPCTSTR lpszStr)
		{
			return AddChars(lpszStr, GetSafeStrlen(lpszStr));
		}

		virtual EXC_TYPE AddNonEmptyStr (LPCTSTR lpszStr)
		{
			return AddNonEmptyChars(lpszStr, GetSafeStrlen(lpszStr));
		}

		virtual EXC_TYPE AddCRLF ()
		{
			return AddStr(_T("\r\n"));
		}

		virtual EXC_TYPE AddChar (const TCHAR tch);

		// Default implementation calls "AddChar" according to number of reps
		virtual EXC_TYPE Repeat (const TCHAR tch, const UINT32 ulNumReps /* may be zero */);

		virtual EXC_TYPE AddEOS () = 0;

		virtual EXC_TYPE AddNum (const UINT32 ulVal);

		virtual EXC_TYPE AddPadNum (const UINT32	ulVal,
											 const UINT32	ulVLen=MAX_DWORD_DISPLAY_LENGTH,	/* may NOT be 0 */
											 const TCHAR	padCh=_T('0'),	/* may NOT be '\0' */
											 const BOOLEAN	fLeftPad=TRUE);

		virtual EXC_TYPE AddTime (const UINT8	bHour,
										  const UINT8	bMinute,
										  const UINT8	bSecond);

		/*	iTimezone is the difference (in seconds) between the GMT and LOCAL time */
		virtual EXC_TYPE AddGMTOffset (const int	iTimeZone=(-1));	/* (-1) == use internal */

		virtual ~IStrlBuilder ()
		{
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// string incremental builder helper class
class CStrlBuilder : public IStrlBuilder {
	private:
		LPTSTR	m_lpszIBuf;
		UINT32	m_ulILen;
		LPTSTR	m_lpszBuf;
		UINT32	m_ulMaxLen;
		LPTSTR&	m_lpszCurPos;
		UINT32&	m_ulRemLen;

		// disable copy constructor and assignment operator
		CStrlBuilder (const CStrlBuilder& );
		CStrlBuilder& operator= (const CStrlBuilder& );

	public:
		CStrlBuilder (LPTSTR& lpszCurPos, UINT32& ulRemLen, bool fDummy /* only to differentiate it from the other constructor */);

		CStrlBuilder (LPTSTR lpszBuf, const UINT32 ulMaxLen);

		virtual LPCTSTR GetBuffer () const
		{
			return m_lpszIBuf;
		}

		virtual LPCTSTR GetCurPos () const
		{
			return m_lpszCurPos;
		}

		virtual void Reset ()
		{
			if ((m_lpszBuf=m_lpszIBuf) != NULL)
				*m_lpszBuf = _T('\0');
			m_ulRemLen = m_ulILen;
		}

		virtual EXC_TYPE Repeat (const TCHAR tch, const UINT32 ulNumReps /* may be zero */);

		virtual EXC_TYPE AddChars (LPCTSTR lpszChars, const UINT32 ulCLen)
		{
			return strlinschars(&m_lpszCurPos, lpszChars, ulCLen, &m_ulRemLen);
		}

		virtual EXC_TYPE AddEOS ()
		{
			return strlinseos(&m_lpszCurPos, &m_ulRemLen);
		}

		virtual EXC_TYPE VAddf (LPCTSTR lpszFmt, va_list ap)
		{
			return strlinsvf(&m_lpszCurPos, lpszFmt, ap, &m_ulRemLen);
		}

		virtual EXC_TYPE Addf (LPCTSTR lpszFmt, ...);

		virtual ~CStrlBuilder () { }
};	// end of builder helper class
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

typedef struct {
	const char	*pszKey;
	LPVOID		pVal;
} STR2PTRASSOC, *LPSTR2PTRASSOC;

/* returns 0 if s2pa is NULL as well */
extern UINT32 CountStr2PtrAssocs (const STR2PTRASSOC s2pa[]);

/*---------------------------------------------------------------------------*/

typedef BOOLEAN (*STR2PTR_ASSOC_ENUM)(const char	pszKey[],
												  LPVOID			pVal,
												  LPVOID			pArg);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class for mapping strings to associated pointers
class CStr2PtrMapper {
	private:
		LPVOID	*m_pAssocs;			// associations map
		UINT32	m_ulAssocsNum;		// current map size
		UINT32	m_ulItemsCount;	// current items count
		BOOLEAN	m_fCase;

		// returns hash value normalized to actual table size
		EXC_TYPE GetHashIndex (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulHashIdx) const;

		EXC_TYPE FindKey (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulAdx, LPVOID& pE) const;

		// disable copy constructor and assigment operator
		CStr2PtrMapper (const CStr2PtrMapper& );
		CStr2PtrMapper& operator= (const CStr2PtrMapper& );

	public:
		// returns hash value regardless of actual table size
		static EXC_TYPE GetHashValue (const char		pszKey[],
												const UINT32	ulKeyLen,
												const BOOLEAN	fCaseSensitive,
												UINT32&			ulHashVal);

		static EXC_TYPE GetHashValue (const char		pszKey[],
												const BOOLEAN	fCaseSensitive,
												UINT32&			ulHashVal)
		{
			return GetHashValue(pszKey, GetSafeStrlen(pszKey), fCaseSensitive, ulHashVal);
		}

		// also default constructor
		CStr2PtrMapper (const UINT32 ulMapSize=0, const BOOLEAN fCaseSensitive=TRUE);

		// if map size not specified then list size is used
		CStr2PtrMapper (const STR2PTRASSOC aList[], const UINT32 ulMapSize, const BOOLEAN fCaseSensitive);

		// only up to specified number are added
		virtual EXC_TYPE Populate (const STR2PTRASSOC aList[], const UINT32 ulNum);

		// Last entry must have NULL pointers
		virtual EXC_TYPE Populate (const STR2PTRASSOC aList[])
		{
			return Populate(aList, CountStr2PtrAssocs(aList));
		}

		// if map size not specified then list size is used
		CStr2PtrMapper (const STR2PTRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize, const BOOLEAN fCaseSensitive);

		// removes all associations but not the internal hash table
		virtual void Reset ();

		// removes all associations and the internal hash table itself.
		virtual void Clear ();

		// returns available map size (NOT number of current items...)
		virtual UINT32 GetSize () const { return m_ulAssocsNum; }

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_ulItemsCount; }

		// Note: the actual map size is the 1st prime number greater or equal
		//			to the requested size.
		//
		// Re-initialization is not allowed - must "Clear()" first
		virtual EXC_TYPE InitMap (const UINT32 ulMapSize, const BOOLEAN fCaseSensitive);

		// initializes this map to be same as parameter
		virtual EXC_TYPE InitMap (const CStr2PtrMapper& m)
		{
			return InitMap(m.m_ulAssocsNum, m.m_fCase);
		}

		virtual BOOLEAN IsCaseSensitive () const { return m_fCase; }

		//		Creates an association between the (non-empty) key and the
		// supplied value. If key already exists, then only value is updated
		virtual EXC_TYPE AddKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID pVal);

		virtual EXC_TYPE AddKey (const char pszKey[], LPVOID pVal)
		{
			return AddKey(pszKey, GetSafeStrlen(pszKey), pVal);
		}

		// returns associated value before deleting
		virtual EXC_TYPE RemoveKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID& pVal);

		virtual EXC_TYPE RemoveKey (const char pszKey[], LPVOID& pVal)
		{
			return RemoveKey(pszKey, GetSafeStrlen(pszKey), pVal);
		}

		virtual EXC_TYPE RemoveKey (const char pszKey[], const UINT32 ulKeyLen)
		{
			LPVOID pVal=NULL;

			return RemoveKey(pszKey, ulKeyLen, pVal);
		}

		virtual EXC_TYPE RemoveKey (const char pszKey[])
		{
			return RemoveKey(pszKey, GetSafeStrlen(pszKey));
		}

		// if successful (EOK) then returns associated value
		virtual EXC_TYPE FindKey (const char pszKey[], const UINT32 ulKeyLen, LPVOID& pVal) const;

		virtual EXC_TYPE FindKey (const char pszKey[], LPVOID& pVal) const
		{
			return FindKey(pszKey, GetSafeStrlen(pszKey), pVal);
		}

		// if successful (EOK) then returns internal key value
		virtual EXC_TYPE GetKey (const char pszKey[], const UINT32 ulKeyLen, LPCTSTR& lpszKey) const;

		virtual EXC_TYPE GetKey (const char pszKey[], LPCTSTR& lpszKey) const
		{
			return GetKey(pszKey, GetSafeStrlen(pszKey), lpszKey);
		}

		virtual EXC_TYPE EnumKeys (STR2PTR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const;

		virtual EXC_TYPE Merge (const CStr2PtrMapper& m);

		virtual ~CStr2PtrMapper () { Clear(); }

		friend class CStr2PtrMapEnum;
};	// end of CStr2PtrMapper

typedef CAllocStructPtrGuard<CStr2PtrMapper>	CStr2PtrMapperPtrGuard;

/*---------------------------------------------------------------------------*/

class CStr2PtrMapEnum {
	private:
		const CStr2PtrMapper&	m_Mapper;
		UINT32						m_ulAdx;
		LPVOID						m_pCurE;

		// disable copy constructor and assignment operator
		CStr2PtrMapEnum (const CStr2PtrMapEnum& );
		CStr2PtrMapEnum& operator= (const CStr2PtrMapEnum& );

	public:
		CStr2PtrMapEnum (const CStr2PtrMapper& mapper);

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_Mapper.GetItemsCount(); }

		// returns EEOF if no more items
		virtual EXC_TYPE GetFirst (LPCTSTR& lpszKey, LPVOID& pVal);

		// returns EEOF if no more items
		virtual EXC_TYPE GetNext (LPCTSTR& lpszKey, LPVOID& pVal);

		virtual ~CStr2PtrMapEnum () { }
};
#endif /* of __cplusplus */

/*---------------------------------------------------------------------------*/

typedef struct {
	const char	*pszKey;
	const char	*pszVal;
} STR2STRASSOC, *LPSTR2STRASSOC;

/* returns 0 if s2pa is NULL as well */
extern UINT32 CountStr2StrAssocs (const STR2STRASSOC s2sa[]);

typedef BOOLEAN (*STR2STR_ASSOC_ENUM)(const char	pszKey[],
												  const char	pszVal[],
												  LPVOID			pArg);

#ifdef __cplusplus
// class for mapping strings to (associated) strings
class CStr2StrMapper {
	private:
		CStr2PtrMapper	m_Mapper;

		// disable copy constructor and assigment operator
		CStr2StrMapper (const CStr2StrMapper& );
		CStr2StrMapper& operator= (const CStr2StrMapper& );

	public:
		// also default constructor
		CStr2StrMapper (const UINT32 ulMapSize=0, const BOOLEAN fCaseSensitive=TRUE)
			: m_Mapper(ulMapSize,fCaseSensitive)
		{
		}

		// if map size not specified then list size is used
		CStr2StrMapper (const STR2STRASSOC aList[], const UINT32 ulMapSize=0, const BOOLEAN fCaseSensitive=TRUE);

		// only up to specified number are added
		virtual EXC_TYPE Populate (const STR2STRASSOC aList[], const UINT32 ulNum);

		// Last entry must have NULL pointers
		virtual EXC_TYPE Populate (const STR2STRASSOC aList[])
		{
			return Populate(aList, CountStr2StrAssocs(aList));
		}

		// if map size not specified then list size is used
		CStr2StrMapper (const STR2STRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize=0, const BOOLEAN fCaseSensitive=TRUE);

		// removes all associations and the internal has table itself.
		virtual void Clear ();

		// removes all associations but not the internal hash table
		virtual void Reset ();

		// returns available map size (NOT number of current items...)
		virtual UINT32 GetSize () const { return m_Mapper.GetSize(); }

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_Mapper.GetItemsCount(); }

		// Note: the actual map size is the 1st prime number greater or equal
		//			to the requested size.
		//
		// Re-initialization is not allowed - must "Clear()" first
		virtual EXC_TYPE InitMap (const UINT32 ulMapSize, const BOOLEAN fCaseSensitive)
		{
			return m_Mapper.InitMap(ulMapSize, fCaseSensitive);
		}

		virtual EXC_TYPE InitMap (const CStr2StrMapper& m)
		{
			return m_Mapper.InitMap(m.m_Mapper);
		}

		virtual BOOLEAN IsCaseSensitive () const { return m_Mapper.IsCaseSensitive(); }

		//		Creates an association between the (non-empty) key and the
		// supplied value. If key already exists, then only value is updated
		virtual EXC_TYPE AddKey (const char pszKey[], const UINT32 ulKeyLen, const char pszVal[], const UINT32 ulVLen);

		virtual EXC_TYPE AddKey (const char pszKey[], const char pszVal[], const UINT32 ulVLen)
		{
			return AddKey(pszKey, GetSafeStrlen(pszKey), pszVal, ulVLen);
		}

		virtual EXC_TYPE AddKey (const char pszKey[], const char pszVal[])
		{
			return AddKey(pszKey, GetSafeStrlen(pszKey), pszVal, GetSafeStrlen(pszVal));
		}

		virtual EXC_TYPE AddKey (const char pszKey[])
		{
			return AddKey(pszKey, GetSafeStrlen(pszKey), NULL, 0);
		}

		virtual EXC_TYPE AddKey (const char pszKey[], const UINT32 ulKeyLen)
		{
			return AddKey(pszKey, ulKeyLen, NULL, 0);
		}

		virtual EXC_TYPE AddNumKey (const char pszKey[], const UINT32 ulKeyLen, const UINT32 ulVal);

		virtual EXC_TYPE AddNumKey (const char pszKey[], const UINT32 ulVal)
		{
			return AddNumKey(pszKey, GetSafeStrlen(pszKey), ulVal);
		}

		virtual EXC_TYPE AccumulateKey (const char pszKey[], const UINT32 ulKeyLen, const char pszVal[], const UINT32 ulMaxLen);

		virtual EXC_TYPE AccumulateKey (const char pszKey[], const UINT32 ulKeyLen, const char pszVal[])
		{
			return AccumulateKey(pszKey, ulKeyLen, pszVal, GetSafeStrlen(pszVal));
		}

		virtual EXC_TYPE AccumulateKey (const char pszKey[], const char pszVal[], const UINT32 ulMaxLen)
		{
			return AccumulateKey(pszKey, GetSafeStrlen(pszKey), pszVal, ulMaxLen);
		}

		virtual EXC_TYPE AccumulateKey (const char pszKey[], const char pszVal[])
		{
			return AccumulateKey(pszKey, GetSafeStrlen(pszKey), pszVal, GetSafeStrlen(pszVal));
		}

		virtual EXC_TYPE AccumulateNumKey (const char pszKey[], const UINT32 ulKeyLen, const UINT32 ulVal);

		virtual EXC_TYPE AccumulateNumKey (const char pszKey[], const UINT32 ulVal)
		{
			return AccumulateNumKey(pszKey, GetSafeStrlen(pszKey), ulVal);
		}

		virtual EXC_TYPE RemoveKey (const char pszKey[], const UINT32 ulKeyLen);

		virtual EXC_TYPE RemoveKey (const char pszKey[])
		{
			return RemoveKey(pszKey, GetSafeStrlen(pszKey));
		}

		// if successful (EOK) then returns associated value
		virtual EXC_TYPE FindKey (const char pszKey[], const UINT32 ulKeyLen, LPCTSTR& lppVal) const;

		virtual EXC_TYPE FindKey (const char pszKey[], LPCTSTR& lppVal) const
		{
			return FindKey(pszKey, GetSafeStrlen(pszKey), lppVal);
		}

		virtual EXC_TYPE FindKey (const char pszKey[], const UINT32 ulKeyLen, char lpszVal[], const UINT32 ulMaxLen) const;

		virtual EXC_TYPE FindKey (const char pszKey[], char lpszVal[], const UINT32 ulMaxLen) const
		{
			return FindKey(pszKey, GetSafeStrlen(pszKey), lpszVal, ulMaxLen);
		}

		// Note: may return error if string found but cannot be translated to number
		virtual EXC_TYPE FindNumKey (const char pszKey[], const UINT32 ulKeyLen, UINT32& ulVal) const;

		virtual EXC_TYPE FindNumKey (const char pszKey[], UINT32& ulVal) const
		{
			return FindNumKey(pszKey, GetSafeStrlen(pszKey), ulVal);
		}

		virtual EXC_TYPE EnumKeys (STR2STR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const;

		// if successful (EOK) then returns internal key value
		virtual EXC_TYPE GetKey (const char pszKey[], const UINT32 ulKeyLen, LPCTSTR& lpszKey) const
		{
			return m_Mapper.GetKey(pszKey, ulKeyLen, lpszKey);
		}

		virtual EXC_TYPE GetKey (const char pszKey[], LPCTSTR& lpszKey) const
		{
			return GetKey(pszKey, GetSafeStrlen(pszKey), lpszKey);
		}

		virtual EXC_TYPE Merge (const CStr2StrMapper& m);

		virtual ~CStr2StrMapper () { Clear(); }

		friend class CStr2StrMapEnum;
};

typedef CAllocStructPtrGuard<CStr2StrMapper>	CStr2StrMapperPtrGuard;

class CStr2StrMapEnum {
	private:
		CStr2PtrMapEnum	m_s2pe;

		// disable copy constructor and assignment operator
		CStr2StrMapEnum (const CStr2StrMapEnum& );
		CStr2StrMapEnum& operator= (const CStr2StrMapEnum& );

	public:
		CStr2StrMapEnum (const CStr2StrMapper& mapper);

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_s2pe.GetItemsCount(); }

		// returns EEOF if no more items
		virtual EXC_TYPE GetFirst (LPCTSTR& lpszKey, LPCTSTR& lpszVal);

		// returns EEOF if no more items
		virtual EXC_TYPE GetNext (LPCTSTR& lpszKey, LPCTSTR& lpszVal);

		virtual ~CStr2StrMapEnum () { }
};
#endif /* of __cplusplus */

/*---------------------------------------------------------------------------*/

typedef struct {
	LPVOID	lpKey;
	LPVOID	lpVal;
} PTR2PTRASSOC, *LPPTR2PTRASSOC;

typedef BOOLEAN (*PTR2PTR_ASSOC_ENUM)(LPVOID lpKey, LPVOID lpVal, LPVOID pArg);

extern UINT32 CountPtr2PtrAssocs (const PTR2PTRASSOC aList[]);

#ifdef __cplusplus
// class for mapping pointers to (associated) pointers
class CPtr2PtrMapper {
	private:
		CStr2PtrMapper	m_Mapper;

		// disable copy constructor and assigment operator
		CPtr2PtrMapper (const CPtr2PtrMapper& );
		CPtr2PtrMapper& operator= (const CPtr2PtrMapper& );

	public:
		// also default constructor
		CPtr2PtrMapper (const UINT32 ulMapSize=0)
			: m_Mapper(ulMapSize,FALSE)
		{
		}

		// if map size not specified then list size is used
		CPtr2PtrMapper (const PTR2PTRASSOC aList[], const UINT32 ulMapSize=0);

		// only up to specified number are added
		virtual EXC_TYPE Populate (const PTR2PTRASSOC aList[], const UINT32 ulNum);

		// Last entry must have NULL pointers
		virtual EXC_TYPE Populate (const PTR2PTRASSOC aList[])
		{
			return Populate(aList, CountPtr2PtrAssocs(aList));
		}

		// if map size not specified then list size is used
		CPtr2PtrMapper (const PTR2PTRASSOC aList[], const UINT32 ulNum, const UINT32 ulMapSize=0);

		// removes all associations and the internal has table itself.
		virtual void Clear ()
		{
			m_Mapper.Clear();
		}

		// removes all associations but not the internal hash table
		virtual void Reset ()
		{
			m_Mapper.Reset();
		}

		// returns available map size (NOT number of current items...)
		virtual UINT32 GetSize () const { return m_Mapper.GetSize(); }

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_Mapper.GetItemsCount(); }

		// Note: the actual map size is the 1st prime number greater or equal
		//			to the requested size.
		//
		// Re-initialization is not allowed - must "Clear()" first
		virtual EXC_TYPE InitMap (const UINT32 ulMapSize)
		{
			return m_Mapper.InitMap(ulMapSize, FALSE);
		}

		virtual EXC_TYPE InitMap (const CPtr2PtrMapper& m)
		{
			return InitMap(m.GetSize());
		}

		//		Creates an association between the (non-empty) key and the
		// supplied value. If key already exists, then only value is updated
		virtual EXC_TYPE AddKey (LPVOID	lpKey, LPVOID lpVal);

		virtual EXC_TYPE RemoveKey (LPVOID lpKey);

		// if successful (EOK) then returns associated value
		virtual EXC_TYPE FindKey (LPVOID lpKey, LPVOID& lppVal) const;

		virtual EXC_TYPE EnumKeys (PTR2PTR_ASSOC_ENUM lpfnEcfn, LPVOID pArg) const;

		virtual EXC_TYPE Merge (const CPtr2PtrMapper& m);

		virtual ~CPtr2PtrMapper () { Clear(); }

		friend class CPtr2PtrMapEnum;
};

typedef CAllocStructPtrGuard<CPtr2PtrMapper>	CPtr2PtrMapperPtrGuard;

class CPtr2PtrMapEnum {
	private:
		CStr2PtrMapEnum	m_s2pe;

		// disable copy constructor and assignment operator
		CPtr2PtrMapEnum (const CPtr2PtrMapEnum& );
		CPtr2PtrMapEnum& operator= (const CPtr2PtrMapEnum& );

	public:
		CPtr2PtrMapEnum (const CPtr2PtrMapper& mapper);

		// returns number of items currently stored in map
		virtual UINT32 GetItemsCount () const { return m_s2pe.GetItemsCount(); }

		// returns EEOF if no more items
		virtual EXC_TYPE GetFirst (LPVOID& pKey, LPVOID& pVal);

		// returns EEOF if no more items
		virtual EXC_TYPE GetNext (LPVOID& pKey, LPVOID& pVal);

		virtual ~CPtr2PtrMapEnum () { }
};
#endif /* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CStr2StructMapper {
	protected:
		CVSDCollection	m_dColl;
		CStr2PtrMapper	m_dMap;

	public:
		CStr2StructMapper ();

		virtual EXC_TYPE InitMap (const UINT32		ulMapSize,
										  const BOOLEAN	fCaseSensitive,
										  const UINT32		ulIDtSize,
										  const UINT32		ulIDtGrow);

		CStr2StructMapper (const UINT32	ulMapSize,
								 const BOOLEAN	fCaseSensitive,
								 const UINT32	ulIDtSize,
								 const UINT32	ulIDtGrow);

		virtual EXC_TYPE Merge (const CStr2StructMapper& m);

		CStr2StructMapper (const CStr2StructMapper& m);

		// Note: resets the contents first
		virtual CStr2StructMapper& operator= (const CStr2StructMapper& m);

		virtual EXC_TYPE AddItem (LPCTSTR lpszItemName, const UINT32 ulINLen, const LPVOID pVal, const UINT32 ulVSize /* cannot be zero */);

		virtual EXC_TYPE AddItem (LPCTSTR lpszItemName, const LPVOID pVal, const UINT32 ulVSize /* cannot be zero */)
		{
			return AddItem(lpszItemName, GetSafeStrlen(lpszItemName), pVal, ulVSize);
		}

		virtual EXC_TYPE FindItem (LPCTSTR lpszItemName, const UINT32 ulINLen, LPVOID& pVal) const
		{
			return m_dMap.FindKey(lpszItemName, ulINLen, pVal);
		}

		virtual EXC_TYPE FindItem (LPCTSTR lpszItemName, LPVOID& pVal) const
		{
			return FindItem(lpszItemName, GetSafeStrlen(lpszItemName), pVal);
		}

		virtual EXC_TYPE RemoveItem (LPCTSTR lpszItemName, const UINT32 ulINLen);

		virtual EXC_TYPE RemoveItem (LPCTSTR lpszItemName)
		{
			return RemoveItem(lpszItemName, GetSafeStrlen(lpszItemName));
		}

		virtual UINT32 GetItemsCount () const
		{
			return m_dColl.GetSize();
		}

		virtual EXC_TYPE GetItemData (const UINT32 ulIndex, LPVOID& pData, UINT32& ulDataLen) const
		{
			return m_dColl.GetData(ulIndex, pData, ulDataLen);
		}

		virtual EXC_TYPE GetItemData (const UINT32 ulIndex, LPVOID& pData) const
		{
			return m_dColl.GetData(ulIndex, pData);
		}

		virtual LPVOID operator[] (const UINT32 ulIdx) const
		{
			return m_dColl[ulIdx];
		}

		virtual LPVOID operator[] (LPCTSTR lpszItemName) const;

		// removes contents but mapper remains initialized
		virtual EXC_TYPE Reset ();

		// Note: requires re-initialization after this call
		virtual EXC_TYPE Clear ();

		virtual ~CStr2StructMapper ()
		{
			EXC_TYPE	exc=Clear();
		}

		friend class CStr2StructMapEnum;
};

class CStr2StructMapEnum : public CStr2PtrMapEnum {
	public:
		CStr2StructMapEnum (const CStr2StructMapper& m)
			: CStr2PtrMapEnum(m.m_dMap)
		{
		}

		virtual ~CStr2StructMapEnum ()
		{
		}
};

/*		This class can be used to enforce string typing on mapper - e.g.
 *
 *	typedef CStr2StructMapTemplate<MYSTRUCT> CStr2MyStructMapper;
 *
 *	MYSTRUCT	ms;
 *	......
 *	CStr2MyStructMapper	mm;	<= this class will accept now "AddItem("xxx", ms);
 */
template<class STST> class CStr2StructMapTemplate : public CStr2StructMapper {
	public:
		CStr2StructMapTemplate ()
			: CStr2StructMapper()
		{
		}

		CStr2StructMapTemplate (const UINT32	ulMapSize,
										const BOOLEAN	fCaseSensitive,
										const UINT32	ulIDtSize,
										const UINT32	ulIDtGrow)
			: CStr2StructMapper(ulMapSize, fCaseSensitive, ulIDtSize, ulIDtGrow)
		{
		}

		virtual EXC_TYPE AddItem (LPCTSTR lpszItemName, const UINT32 ulINLen, const STST& itemVal)
		{
			return CStr2StructMapper::AddItem(lpszItemName, ulINLen, (LPVOID) &itemVal, (sizeof itemVal));
		}

		virtual EXC_TYPE AddItem (LPCTSTR lpszItemName, const STST& itemVal)
		{
			return AddItem(lpszItemName, GetSafeStrlen(lpszItemName), itemVal);
		}

		virtual EXC_TYPE FindItem (LPCTSTR lpszItemName, const UINT32 ulINLen, STST* &pItem) const
		{
			return CStr2StructMapper::FindItem(lpszItemName, ulINLen, (LPVOID &) pItem);
		}

		virtual EXC_TYPE FindItem (LPCTSTR lpszItemName, STST* &pItem) const
		{
			return FindItem(lpszItemName, GetSafeStrlen(lpszItemName), pItem);
		}

		virtual ~CStr2StructMapTemplate ()
		{
		}
};
#endif /* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// "greedy" string buffer
class CGreedyString {
	private:
		LPTSTR	m_lpszBuf;
		UINT32	m_ulMaxLen;
		UINT32	m_ulGrowLen;
		UINT32	m_ulCurLen;

		// disable copy constructor and assignment operator
		CGreedyString (const CGreedyString& cgs);
		CGreedyString& operator= (const CGreedyString& cgs);

	public:
		// Note: if grow size is zero then string is grown exactly as required
		CGreedyString (const UINT32 ulGrowLen=0);

		virtual operator LPCTSTR () const
		{
			return m_lpszBuf;
		}

		// can be NULL/empty
		virtual EXC_TYPE Set (LPCTSTR lpszVal, const UINT32 ulVLen);

		virtual EXC_TYPE Set (LPCTSTR lpszVal)
		{
			return Set(lpszVal, GetSafeStrlen(lpszVal));
		}

		virtual UINT32 GetLength () const
		{
			return m_ulCurLen;
		}

		virtual void Reset ()
		{
			EXC_TYPE	exc=Set(NULL);
		}

		virtual ~CGreedyString ()
		{
			strreleasebuf(m_lpszBuf);
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// "greedy" incremental string
class CIncStrlBuilder : public IStrlBuilder {
	private:
		LPTSTR	m_lpszBuf;
		UINT32	m_ulMaxLen;
		UINT32	m_ulGrowLen;
		LPTSTR	m_lpszCurPos;
		UINT32	m_ulCurLen;

		// disable copy constructor and assignment operator
		CIncStrlBuilder (const CIncStrlBuilder& );
		CIncStrlBuilder& operator= (const CIncStrlBuilder& );

		// re-allocates if necessary
		EXC_TYPE CheckAvailability (const UINT32 ulCLen);

	public:
		// Note: if grow size is zero then cannot grow string
		CIncStrlBuilder (const UINT32 ulInitialSize, const UINT32 ulGrowSize=NAME_LENGTH);
		CIncStrlBuilder (LPCTSTR lpszInitialValue=NULL, const UINT32 ulGrowSize=NAME_LENGTH);

		virtual LPCTSTR GetBuffer () const
		{
			return m_lpszBuf;
		}

		virtual UINT32 GetCurLen () const
		{
			return m_ulCurLen;
		}

		virtual operator LPCTSTR () const
		{
			return m_lpszBuf;
		}

		virtual LPCTSTR GetCurPos () const
		{
			return m_lpszCurPos;
		}

		virtual EXC_TYPE Repeat (const TCHAR tch, const UINT32 ulNumReps /* may be zero */);

		virtual EXC_TYPE AddChars (LPCTSTR lpszChars, const UINT32 ulCLen);

		virtual EXC_TYPE AddEOS ();

		virtual void Reset ();

		virtual ~CIncStrlBuilder ()
		{
			strreleasebuf(m_lpszBuf);
		}
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#define MIN_ARACRYPT_SEED_LEN	32

#ifdef __cplusplus
// Note: A Special Thanks to Mr. Warren Ward for his Sept. 1998 CUJ article:
// "Stream Encryption" Copyright (c) 1998 by Warren Ward
//
// Based on "CryptIt" code published by Daniel Madden on http://www.codeproject.com
class ARACrypt {
	protected:
		TCHAR	m_szKey[MIN_ARACRYPT_SEED_LEN+2];

		unsigned long           m_LFSR_A;
		unsigned long           m_LFSR_B;
		unsigned long           m_LFSR_C;
		const unsigned long     m_Mask_A;
		const unsigned long     m_Mask_B;
		const unsigned long     m_Mask_C;
		const unsigned long     m_Rot0_A;
		const unsigned long     m_Rot0_B;
		const unsigned long     m_Rot0_C;
		const unsigned long     m_Rot1_A;
		const unsigned long     m_Rot1_B;
		const unsigned long     m_Rot1_C;

	public:
		// also default constructor
		ARACrypt (LPCTSTR lpszKey=NULL, const UINT32 ulKeyLen=(UINT32) (-1));

		// Note(s): can be set only ONCE to a non-empty value
		virtual EXC_TYPE SetKey (LPCTSTR lpszKey, const UINT32 ulKeyLen);
		virtual EXC_TYPE SetKey (LPCTSTR lpszKey)
		{
			return SetKey(lpszKey, GetSafeStrlen(lpszKey));
		}

		// Note: return value may be NULL/empty
		virtual LPCTSTR GetKey () const
		{
			return m_szKey;
		}

		/*
		 *		Transforms a single character. If it is plaintext, it will be encrypted.
		 *	If it is encrypted, and if the LFSRs are in the same state as when it was
		 *	encrypted (i.e., the same key loaded into them and the same number of calls
		 *	to TransformChar after the keys were loaded), the character will be decrypted
		 *	to its original value.
		 *
		 *	DEVELOPER'S NOTE:
		 *
		 *		This code contains corrections to the LFSR operations that supercede the
		 * code examples in Applied Cryptography (first edition, up to at least the 4th
		 * printing, and second edition, up to at least the 6th printing). More recent
		 * errata sheets may show the corrections.
		 */
		virtual EXC_TYPE TransformChar (const TCHAR bIn, TCHAR& bOut);

		virtual EXC_TYPE TransformString (LPCTSTR lpszInput, const UINT32 ulILen, IStrlBuilder& istrb);

		// Note: output string must be at least as large as the input (since the
		//			encrypted/decrypted result has exactly the same size).
		// ==========================================
		// DO NOT TREAT THE OUTPUT STRING AS A NULL-
		// TERMINATED STRING. 
		// ==========================================
		virtual EXC_TYPE TransformString (LPCTSTR lpszInput, const UINT32 ulILen,
													 LPTSTR lpszOutput, const UINT32 ulOLen,
													 /* OUT */ UINT32& ulUsedLen);

		// ==========================================
		// DO NOT TREAT THE OUTPUT STRING AS A NULL-
		// TERMINATED STRING. 
		// ==========================================
		virtual EXC_TYPE TransformString (LPCTSTR lpszInput, LPTSTR lpszOutput, const UINT32 ulOLen, UINT32& ulUsedLen)
		{
			return TransformString(lpszInput, GetSafeStrlen(lpszInput), lpszOutput, ulOLen, ulUsedLen);
		}

		virtual ~ARACrypt() { }
};
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#endif			/* of IF _utl_str_h_ */
