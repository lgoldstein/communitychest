#ifndef _UTL32_UTF8_ENC_H_
#define _UTL32_UTF8_ENC_H_

#include <util/string.h>
#include <util/errors.h>

/*
 *	0000 0000-0000 007F   0xxxxxxx
 * 0000 0080-0000 07FF   110xxxxx 10xxxxxx
 * 0000 0800-0000 FFFF   1110xxxx 10xxxxxx 10xxxxxx
 * 0001 0000-001F FFFF   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 * 0020 0000-03FF FFFF   111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
 * 0400 0000-7FFF FFFF   1111110x 10xxxxxx ... 10xxxxxx
 */

/* maximum number of bytes to be used to encode a UCS-4 character */
#define MAX_UTF8_CHAR_ENCLEN	6

/*---------------------------------------------------------------------------*/

#define MAX_UCS4_CHAR_VALUE			(UINT32) 0x7FFFFFFFUL
#define MAX_UCS4_CHAR_UTF8_ENC_LEN	MAX_UTF8_CHAR_ENCLEN

#define MAX_UCS2_CHAR_VALUE			(UINT32) 0x0000FFFFUL
#define MAX_UCS2_CHAR_UTF8_ENC_LEN	3

#define MAX_BIN_CHAR_VALUE				(UINT32) 0x000000FFUL
#define MAX_BIN_CHAR_UTF8_ENC_LEN	2

/* Note: a zero-length encoding is considered OK */
extern EXC_TYPE CheckUTF8StringEncoding (const BYTE bStr[], const DWORD dwLen);

/*---------------------------------------------------------------------------*/

/* Note: returns > MAX_UTF8_CHAR_ENCLEN for UCS-4 >= 0x80000000 */
extern BYTE GetUCS4ToUTF8CharEncodingLength (const UINT32 dwUCS4Char);

#ifdef __cplusplus
inline BYTE GetUCS2ToUTF8CharEncodingLength (const UINT16 wUCS2Char)
{
	return GetUCS4ToUTF8CharEncodingLength(((DWORD) wUCS2Char) & 0x0000FFFFUL);
}

inline BYTE GetBinToUTF8CharEncodingLength (const BYTE bChar)
{
	return ((bChar <= 0x7F) ? 1 : 2);
}
#else
#	define GetUCS2ToUTF8CharEncodingLength(wUCS2Char)	\
		GetUCS4ToUTF8CharEncodingLength(((DWORD) (wUCS2Char)) & 0x0000FFFFUL)
#	define GetBinToUTF8CharEncodingLength(bChar)	\
		(((BYTE) (bChar) > 0x7F) ? 1 : 2)
#endif	/* __cplusplus */

/*---------------------------------------------------------------------------*/

extern EXC_TYPE GetUCS4ToUTF8BufEncLen (const UINT32 wUCS4Buf[], const DWORD dwInLen, DWORD *pdwInEnc);

extern EXC_TYPE GetUCS2ToUTF8BufEncLen (const UINT16 wUCS2Buf[], const DWORD dwInLen, DWORD *pdwInEnc);

extern EXC_TYPE GetBinToUTF8BufEncLen (const BYTE binBuf[], const DWORD dwInLen, DWORD *pdwInEnc);

/*---------------------------------------------------------------------------*/

/* returns EOVREFLOW if cannot accomodate encoding */
extern EXC_TYPE EncodeUCS4CharToUTF8 (const UINT32 wUCS4Char,
												  BYTE			bUTF8[],
												  const DWORD	bLen,
												  BYTE			*pbUsedLen  /* may be NULL */);

#ifdef __cplusplus
inline EXC_TYPE EncodeUCS2CharToUTF8 (const UINT16 wUCS2Char, BYTE bUTF8[], const DWORD bLen, BYTE *pbUsedLen /* may be NULL */)
{
	return EncodeUCS4CharToUTF8((((DWORD) wUCS2Char) & 0x0000FFFFUL), bUTF8, bLen, pbUsedLen);
}

inline EXC_TYPE EncodeBinCharToUTF8 (const BYTE bChar, BYTE bUTF8[], const DWORD bLen, BYTE *pbUsedLen /* may be NULL */)
{
	return EncodeUCS4CharToUTF8((((DWORD) bChar) & 0x000000FFUL), bUTF8, bLen, pbUsedLen);
}
#else
#	define EncodeUCS2CharToUTF8(wUCS2Char,bUTF8,bLen,pbUsedLen)	\
		EncodeUCS4CharToUTF8((((DWORD) (wUCS2Char)) & 0x0000FFFFUL), bUTF8, bLen, pbUsedLen)
#	define EncodeBinCharToUTF8(bChar,bUTF8,bLen,pbUsedLen)	\
		EncodeUCS4CharToUTF8((((DWORD) (bChar)) & 0x000000FFUL), bUTF8, bLen, pbUsedLen)
#endif

/*---------------------------------------------------------------------------*/

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
extern EXC_TYPE EncodeBinToUtf8 (const BYTE binBuf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
											BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */);

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
extern EXC_TYPE EncodeUCS2ToUtf8 (const UINT16 wUCS2Buf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
											 BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */);

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
extern EXC_TYPE EncodeUCS4ToUtf8 (const UINT32 wUCS4Buf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
											 BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
//		This code assumes TCHAR == char
#	if defined(UNICODE) || defined(_UNICODE)
#		error "CUTF8StrValue implementation N/A for WIDE chars"
#	endif

// this class converts its input to a UTF-8 string (if necessary)
class CUTF8StrValue {
	private:
		LPCTSTR	m_lpszRetVal;
		DWORD		m_ulRetLen;
		LPTSTR	m_lpszTmpVal;

	public:
		/*		Unless specified otherwise, the original value is NOT copied if
		 * it needs no UTF-8 encoding. If a value is not copied, then the
		 * supplied pointer MUST remain valid throughout the usage of this class
		 */
		CUTF8StrValue ()
			: m_lpszRetVal(NULL)
			, m_lpszTmpVal(NULL)
			, m_ulRetLen(0)
		{
		}

		CUTF8StrValue (LPCTSTR lpszValue, const BOOLEAN fCopyIt)
			: m_lpszRetVal(NULL)
			, m_lpszTmpVal(NULL)
			, m_ulRetLen(0)
		{
			SetValue(lpszValue, fCopyIt);
		}

		CUTF8StrValue (LPCTSTR lpszValue, const UINT32 ulVLen, const BOOLEAN fCopyIt)
			: m_lpszRetVal(NULL)
			, m_lpszTmpVal(NULL)
			, m_ulRetLen(0)
		{
			SetValue(lpszValue, ulVLen, fCopyIt);
		}
		/*		Unless specified otherwise, the original value is NOT copied if
		 * it needs no UTF-8 encoding. If a value is not copied, then the
		 * supplied pointer MUST remain valid throughout the usage of this class
		 */
		virtual EXC_TYPE SetValue (LPCTSTR lpszValue, const UINT32 ulVLen, const BOOLEAN fCopyIt);

		virtual EXC_TYPE SetValue (LPCTSTR lpszValue, const BOOLEAN fCopyIt)
		{
			return SetValue(lpszValue, GetSafeStrlen(lpszValue), fCopyIt);
		}

		virtual void Reset ()
		{
			::strreleasebuf(m_lpszTmpVal);
			m_lpszRetVal = NULL;
			m_ulRetLen = 0;
		}

		virtual LPCTSTR GetValue () const
		{
			return m_lpszRetVal;
		}

		virtual UINT32 GetLength () const
		{
			return m_ulRetLen;
		}

		virtual ~CUTF8StrValue ()
		{
			Reset();
		}
};
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* _UTL32_UTF8_ENC_H_ */
