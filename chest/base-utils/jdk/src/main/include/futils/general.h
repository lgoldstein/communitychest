#ifndef _GNRL_FUTILS_H_
#define _GNRL_FUTILS_H_

/*
 * File
 *    general.h
 *    $Source: futils/general.h $
 *
 * Purpose
 *
 *       Contains declarations of general file-utilities.
 *
 * Programmer
 *    LYOR  G.
 *
 * Date
 *    29-March-1993 - original version (RMX)
 *    19-April-1995 - adapted for UNIX & DOS (Lyor G.)
 *
 */

#include <stdio.h>
#include <time.h>

#include <sys/types.h>
#include <sys/stat.h>	/* for stat() */

	/* for access(), open(), close() */
#if defined(WIN32) || defined(_WIN32)
#	include <io.h>

#	define FBINMODE _T("b")	/* for binary files */
#else
#	include <unistd.h>
#	include <sys/types.h>
#	include <sys/stat.h>
#	include <fcntl.h>

#	define FBINMODE	/* nothing */
#endif	/* WIN32 */

#include <_types.h>
#include <util/string.h>

#ifndef WIN32
#	ifdef __cplusplus
		inline FILE *_tfopen (LPCTSTR lpszFilePath, LPCTSTR mode)
		{
			return fopen(lpszFilePath, mode);
		}

		inline int _taccess (LPCTSTR lpszFilePath, int mode)
		{
			return access(lpszFilePath, mode);
		}

		inline int _tstat (LPCTSTR lpszFilePath, struct stat *s)
		{
			return stat(lpszFilePath, s);
		}

		inline int _vftprintf (FILE *fout, LPCTSTR lpszFmt, va_list ap)
		{
			return vfprintf(fout, lpszFmt, ap);
		}

		inline int _vtprintf (LPCTSTR lpszFmt, va_list ap)
		{
			return vprintf(lpszFmt, ap);
		}
#	else
#		define _vtprintf(f,m)		vprintf((m),(a))
#		define _vftprintf(f,m,a)	vfprintf((f),(m),(a))
#		define _tfopen(f,m)			fopen((f),(m))
#		define _taccess(f,m)			access((f),(m))
#		define _tstat(f,s)			stat((f),(s))
#	endif	/* of __cplusplus */
#endif	/* of WIN32 */

/*--------------------------------------------------------------------------*/

/* creates the specified directoy - including missing components in between */
extern EXC_TYPE mkpath (LPCTSTR dirp);

/*--------------------------------------------------------------------------*/

#define SDIR_ENTER_DIR_DELIM	_T('+')
#define SDIR_EXIT_DIR_DELIM	_T('-')

/*                      SDIR_TREAT_FILE_CFN
 *                      -------------------
 *    Callback function used to announce the user that a file has been
 * discovered in the directory.
 *
 * Parameters:
 *
 *    [IN]  dir_name - string containing the directory name.
 *    [IN]  dir_level - directory "depth" (in case of recursive scan)
 *    [IN]  fname - string containing the filename (WIHOUT the directory) - if
 *					"+" or "-" then the callback has been invoked for the
 *					directory itself - '+' when directory entered, '-' when directory exited.
 *
 *    [IN]  fpath - FULL file path (dir + fname).
 *    [IN]  params_p - specific params as given when scanning was initiated.
 *
 *		If callback function returns non-EOK code, the scanning cycle is aborted, and
 * returned code is propagated (except for EABORTEXIT which is considered an OK abort).
 */

typedef EXC_TYPE (*SDIR_TREAT_FILE_CFN_TYPE)(const char  *dir_name_p,
                                             const DWORD dir_level,
                                             const char  *fname_p,
                                             const char  *fpath_p,
                                             void        *params_p);

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

extern EXC_TYPE sdir_scan_directory (const char                *dir_name_p,
                                     const DWORD               dir_level,
                                     void                      *params_p,
                                     SDIR_TREAT_FILE_CFN_TYPE  treat_file_cfn);

extern EXC_TYPE sdir_delete_dir (const char *dir_name_p, const BOOLEAN fRemoveIt);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CFilePtrGuard {
	private:
		FILE*	&m_fp;

		// disable copy constructor and assignment operator
		CFilePtrGuard (const CFilePtrGuard& );
		CFilePtrGuard& operator= (const CFilePtrGuard& );

	public:
		CFilePtrGuard (FILE*	&fp)
			: m_fp(fp)
		{
		}

		int Close ()
		{
			if (m_fp != NULL)
			{
				int nRes=::fclose(m_fp);
				m_fp = NULL;
				return nRes;
			}

			return (-1);
		}

		virtual ~CFilePtrGuard ()
		{ 
			Close();
		}
};
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class CFileFdGuard {
	private:
		int&	m_fd;

		// disable copy constructor and assignment operator
		CFileFdGuard (const CFileFdGuard& );
		CFileFdGuard& operator= (const CFileFdGuard& );

	public:
		CFileFdGuard (int& fd)
			: m_fd(fd)
		{
		}

		int Close ();

		virtual ~CFileFdGuard ()
		{
			Close();
		}
};
#endif	/* of __cplusplus */

/*--------------------------------------------------------------------------*/

/* callback to read from file pointer (FILE *) */
extern UINT32 fileIOReadCfn (void *pFin, UINT8 pBuf[], const UINT32 ulBufLen);

/* callback to write to file (FILE *) */
extern UINT32 fileIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen);

/*
 * Callback that can be used to seek a stream
 *
 *		nDir - <0 means BACKWARD-RELATIVE to current position
 *				 >0 means FORWARD-RELATIVE to current position
 *				 =0 means ABSOLUTE POSITION
 *
 *		ulCount - ABSOLUTE offset to be used for seeking
 */
extern EXC_TYPE fileIOSeekCfn (void *pFp, const int nDir, const UINT32 ulCount);

/* callback to write to text file (FILE *) */
extern UINT32 textFileIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// basic interface for reading binary data
class IBinaryDataReader {
	protected:
		// running O/S endian-ness
		const bool	m_fLclBigEndian;

		IBinaryDataReader ();

	public:
		/*		nDir - <0 means BACKWARD-RELATIVE to current position
		 *				 >0 means FORWARD-RELATIVE to current position
		 *				 =0 means ABSOLUTE POSITION
		 *
		 *		ulCount - ABSOLUTE offset to be used for seeking
		 */
		virtual EXC_TYPE SeekData (const int nDir, const UINT32 ulCount) = 0;

		virtual EXC_TYPE ReadData (BYTE bData[], const DWORD dwMaxLen, DWORD& dwReadLen) = 0;

		// returns error if not read exactly specified len
		virtual EXC_TYPE ReadData (BYTE bData[], const DWORD dwMaxLen);

		virtual EXC_TYPE ReadDWORD (DWORD& dwVal, const bool isBigEndian);

		virtual EXC_TYPE ReadWORD (WORD& wVal, const bool isBigEndian);

		virtual EXC_TYPE ReadBYTE (BYTE& bVal)
		{
			return ReadData(&bVal, 1);
		}

		virtual EXC_TYPE Close () = 0;

		virtual ~IBinaryDataReader ()
		{
		}
};

/*--------------------------------------------------------------------------*/

class IBinaryIOReader : public IBinaryDataReader {
	private:
		// disable copy constructor and assignment operator
		IBinaryIOReader (const IBinaryIOReader& );
		IBinaryIOReader& operator= (const IBinaryIOReader& );

	protected:
		IOREADCALLBACK	m_lpfnRcfn;
		IOSEEKCALLBACK m_lpfnScfn;
		LPVOID			m_pFile;

	public:
		virtual EXC_TYPE ReadData (BYTE buf[], const DWORD dwMaxLen, DWORD& dwReadLen);

		/*		nDir - <0 means BACKWARD-RELATIVE to current position
		 *				 >0 means FORWARD-RELATIVE to current position
		 *				 =0 means ABSOLUTE POSITION
		 *
		 *		ulCount - ABSOLUTE offset to be used for seeking
		 */
		virtual EXC_TYPE SeekData (const int nDir, const UINT32 ulCount);

		IBinaryIOReader (IOREADCALLBACK lpfnRcfn=NULL, IOSEEKCALLBACK lpfnScfn=NULL, LPVOID pFile=NULL);

		// returns error if alread set, also if both callback functions are NULL
		virtual EXC_TYPE SetIOEnvironment (IOREADCALLBACK lpfnRcfn /* may be NULL */, IOSEEKCALLBACK lpfnScfn /* may be NULL */, LPVOID pFile /* may be NULL */);

		virtual EXC_TYPE Close ();

		virtual ~IBinaryIOReader ()
		{
			Close();
		}
};

/*--------------------------------------------------------------------------*/

class IBinaryFileReader : public IBinaryIOReader {
	private:
		// disable copy constructor and assignment operator
		IBinaryFileReader (const IBinaryFileReader& );
		IBinaryFileReader& operator= (const IBinaryFileReader& );

	protected:
		FILE	*m_fp;
		bool	m_fAutoClose;

	public:
		IBinaryFileReader ();

		// NOTE: returns error if already set
		virtual EXC_TYPE SetFile (LPCTSTR lpszFilePath, const bool fAutoClose);

		IBinaryFileReader (LPCTSTR lpszFilePath, const bool fAutoClose);

		// NOTE: returns error if already set
		virtual EXC_TYPE SetFile (FILE *fp, const bool fAutoClose);

		// NOTE: assumed to be opened for BINARY read
		IBinaryFileReader (FILE *fp, const bool fAutoClose);

		// NOTE: assumes file pointer is closed by someone else
		virtual EXC_TYPE Detach ();

		virtual EXC_TYPE Close ();

		virtual ~IBinaryFileReader ()
		{
			Close();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// deletes the file on destruction
class CFileLocationGuard {
	private:
		CFileLocationGuard (const CFileLocationGuard& );
		CFileLocationGuard& operator= (const CFileLocationGuard& );

	protected:
		LPCTSTR	m_lpszFilePath;

	public:
		CFileLocationGuard (LPCTSTR lpszFilePath)
			: m_lpszFilePath(lpszFilePath)
		{
		}

		virtual int Release ()
		{
			int	nErr=0;

			if (!IsEmptyStr(m_lpszFilePath))
				nErr = ::_tremove(m_lpszFilePath);

			m_lpszFilePath = NULL;
			return nErr;
		}

		virtual ~CFileLocationGuard ()
		{
			int	nErr=Release();
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

/*
 *		Replaces "dangerous" characters that cannot appear in a path component
 * with ones that are allowed (Note: '/' characters are translated to path separators).
 */
extern void AdjustFileComponentCharacters (/* In/Out */ LPTSTR lpszSubPathStr /* may be NULL/empty */);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
typedef EXC_TYPE (*CSV_ENUM_CFN)(LPCTSTR			lpszLine,	// original line
											const UINT32	ulColNdx,	// starts at zero
											LPCTSTR			lpszVal,		// Note: value may contain double quotes ("") as SINGLE quotes sign (")
											const UINT32	ulVLen,
											LPVOID			pArg,
											BOOLEAN&			fContEnum);

extern EXC_TYPE EnumCSValuesLine (LPCTSTR lpszLine, CSV_ENUM_CFN lpfnEcfn, LPVOID pArg);

//////////////////////////////////////////////////////////////////////////////

class CCSVParser {
	private:
		static EXC_TYPE HandleCSVValue (LPCTSTR		lpszLine,	// original line
												  const UINT32	ulColNdx,	// starts at zero
												  LPCTSTR		lpszVal,
												  const UINT32	ulVLen,
												  LPVOID			pArg,
												  BOOLEAN&		fContEnum);

	protected:
		// if returns EOK then parsing is continued
		virtual EXC_TYPE HandleParseError (const UINT32 ulLineNum, const EXC_TYPE err)
		{
			return err;
		}

		// called BEFORE parsing specified line number
		virtual EXC_TYPE HandleLineStart (const UINT32	ulLineNum, BOOLEAN& fSkipLine, BOOLEAN& fContEnum)
		{
			fSkipLine = FALSE;	// just making sure
			fContEnum = TRUE;	// just making sure
			return EOK;
		}

		// called to handle a specific value
		virtual EXC_TYPE HandleValue (const UINT32	ulLineNum,	// starts at zero
												LPCTSTR			lpszLine,	// original line
												const UINT32	ulColNdx,	// starts at zero
												LPCTSTR			lpszVal,		// Note: value may contain double quotes ("") as SINGLE quotes sign (")
												const UINT32	ulVLen,
												BOOLEAN&			fContEnum) = 0;	// if FALSE, then current line parsing is stopped

		// called AFTER parsing specified line number
		virtual EXC_TYPE HandleLineEnd (const UINT32	ulLineNum, BOOLEAN& fContEnum)
		{
			fContEnum = TRUE;	// just making sure
			return EOK;
		}

		// called to read a line - EOF should be signalled either by returning EEOF
		//
		// Note: the returned line should not contain any terminating CR/LF
		virtual EXC_TYPE ReadLine (const UINT32	ulLineNum,	// starts at zero
											LPTSTR			lpszLine,
											const UINT32	ulMaxLen,	// zero length line is ignored
											UINT32&			ulReadLen) = 0;
	public:
		CCSVParser ()
		{
		}

		// uses the specified buffer for parsing
		virtual EXC_TYPE ParseStream (LPTSTR lpszLine, const UINT32 ulMaxLineLen);

		// automatically allocates/frees a buffer of specified length and uses it
		virtual EXC_TYPE ParseStream (const UINT32 ulMaxLineLen);

		virtual ~CCSVParser ()
		{
		}
};

//////////////////////////////////////////////////////////////////////////////

class CCSVStreamParser : public CCSVParser {
	private:
		IOREADCALLBACK	m_lpfnRcfn;
		LPVOID			m_pArg;

	protected:
		virtual EXC_TYPE ReadLine (const UINT32	ulLineNum,	// starts at zero
											LPTSTR			lpszLine,
											const UINT32	ulMaxLen,
											UINT32&			ulReadLen);

	public:
		// also default constructor
		CCSVStreamParser (IOREADCALLBACK lpfnRcfn=NULL, LPVOID pArg=NULL)
			: CCSVParser(), m_lpfnRcfn(lpfnRcfn), m_pArg(pArg)
		{
		}

		virtual EXC_TYPE SetStream (IOREADCALLBACK lpfnRcfn, LPVOID pArg);

		virtual ~CCSVStreamParser ()
		{
		}
};

//////////////////////////////////////////////////////////////////////////////

class CCSVFileParser : public CCSVParser {
	private:
		FILE	*m_fp;

	protected:
		virtual EXC_TYPE ReadLine (const UINT32	ulLineNum,	// starts at zero
											LPTSTR			lpszLine,
											const UINT32	ulMaxLen,
											UINT32&			ulReadLen);

	public:
		CCSVFileParser ()
			: CCSVParser(), m_fp(NULL)
		{
		}

		// Note: does not close the file after parsing
		virtual EXC_TYPE ParseFile (FILE *fp, LPTSTR lpszLine, const UINT32 ulMaxLineLen);

		// Note: does not close the file after parsing
		virtual EXC_TYPE ParseFile (FILE *fp, const UINT32 ulMaxLineLen);

		virtual EXC_TYPE ParseFile (LPCTSTR lpszFilePath, LPTSTR lpszLine, const UINT32 ulMaxLineLen);

		virtual EXC_TYPE ParseFile (LPCTSTR lpszFilePath, const UINT32 ulMaxLineLen);

		virtual ~CCSVFileParser ()
		{
			if (m_fp != NULL)
				::fclose(m_fp);
		}
};
#endif	/* __cplusplus */

/*--------------------------------------------------------------------------*/

#endif
