#include <util/utf8enc.h>
#include <util/tables.h>

/*--------------------------------------------------------------------------*/

/* Note: a zero-length encoding is considered OK */
EXC_TYPE CheckUTF8StringEncoding (const BYTE bStr[], const DWORD dwLen)
{
	if (0 == dwLen)
		return EOK;
	if (NULL == bStr)
		return EPARAM;

	for (DWORD	dwIdx=0; dwIdx < dwLen; dwIdx++)
	{
		const BYTE	bChar=bStr[dwIdx];
		/* 0xFF and 0xFE are not allowed values */
		if ((0xFF == bChar) || (0xFE == bChar))
			return EINVALIDNUMERIC;

		/* number of leading "1"-s is actually the number of encoding bytes to be used */
		const BYTE	sOnes=end_ones_tbl[bChar];

		/*	0000 0000-0000 007F   0xxxxxxx */
		if (0 == sOnes)
			continue;

		/* there are at least 2 encoding bytes */
		if (1 == sOnes)
			return ESPACE;
		/* there are at most 6 encoding bytes */
		if (sOnes > MAX_UTF8_CHAR_ENCLEN)
			return EOVERFLOW;

		/* check that there is a zero immediatly following the "1"-s */
		const BYTE	bZeroMask=byte_powers_of_2_table[INT8_BITS_NUM - sOnes - 1];
		if ((bChar & bZeroMask) != 0)
			return EFLUSHING;

		/* check that remaining bytes start with "10" */
		BYTE bLen=1 /* skip first byte */;
		for (; (bLen < sOnes) && (dwIdx < dwLen); dwIdx++, bLen++)
			if ((bStr[dwIdx + 1] & 0xC0) != 0xC0)
				return EFRAGMENTATION;

		/* make sure we had enough bytes in the encoding */
		if (bLen < sOnes)
			return EEOF;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: returns > MAX_UTF8_CHAR_ENCLEN for UCS-4 >= 0x80000000 */
BYTE GetUCS4ToUTF8CharEncodingLength (const UINT32 dwUCS4Char)
{
	if (dwUCS4Char <= 0x0000007FUL)
		return 1;
	else if (dwUCS4Char <= 0x000007FFUL)
		return 2;
	else if (dwUCS4Char <= 0x0000FFFFUL)
		return 3;
	else if (dwUCS4Char <= 0x001FFFFFUL)
		return 4;
	else if (dwUCS4Char <= 0x03FFFFFFUL)
		return 5;
	else if (dwUCS4Char <= 0x03FFFFFFUL)
		return 5;
	else if (dwUCS4Char <= MAX_UCS4_CHAR_VALUE)
		return 6;

	return (MAX_UTF8_CHAR_ENCLEN + 1);
}

/*--------------------------------------------------------------------------*/

/* returns EOVREFLOW if cannot accomodate encoding */
EXC_TYPE EncodeUCS4CharToUTF8 (const UINT32 wUCS4Char, BYTE bUTF8[], const DWORD bLen, BYTE *pbUsedLen  /* may be NULL */)
{
	if (pbUsedLen != NULL)
		*pbUsedLen = 0;

	const BYTE		eLen=GetUCS4ToUTF8CharEncodingLength(wUCS4Char);
	if (eLen > MAX_UTF8_CHAR_ENCLEN)
		return ESTATE;

	// check if can accomodate encoding
	if (eLen > bLen)
		return EOVERFLOW;

	switch(eLen)
	{
		case 1	:	//	0000 0000-0000 007F   0xxxxxxx
			bUTF8[0] = (wUCS4Char & 0x000000FFUL);
			break;

		case 2	:	// 0000 0080-0000 07FF   110xxxxx 10xxxxxx
			bUTF8[0] = (/* 110xxxx */ 0x000000C0 | /* 5 MSBit(s) - out of 11 */ ((wUCS4Char >> 6) & 0x0000001FUL));
			bUTF8[1] = (/* 10xxxxx */ 0x00000080 | /* 6 LSBit(s) - out of 11 */ (wUCS4Char & 0x0000003FUL));
			break;
 
		case 3	:	// 0000 0800-0000 FFFF   1110xxxx 10xxxxxx 10xxxxxx
			bUTF8[0] = (/* 1110xxx */ 0x000000E0 | /* 4 MSBit(s)  - out of 16 */ ((wUCS4Char >> 12) & 0x0000000FUL));
			bUTF8[1] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 16 */ ((wUCS4Char >> 6) & 0x0000003FUL));
			bUTF8[2] = (/* 10xxxxx */ 0x00000080 | /* 6 LSBit(s) - out of 16 */ (wUCS4Char & 0x0000003FUL));
			break;

		case 4	:	// 0001 0000-001F FFFF   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			bUTF8[0] = (/* 11110xxx */ 0x000000F0 | /* 3 MSBit(s)  - out of 21 */ ((wUCS4Char >> 18) & 0x00000007UL));
			bUTF8[1] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 21 */ ((wUCS4Char >> 12) & 0x0000003FUL));
			bUTF8[2] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 21 */ ((wUCS4Char >>  6) & 0x0000003FUL));
			bUTF8[3] = (/* 10xxxxx */ 0x00000080 | /* 6 LSBit(s) - out of 21 */ (wUCS4Char & 0x0000003FUL));
			break;

		case 5	:	// 0020 0000-03FF FFFF   111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
			bUTF8[0] = (/* 111110xx */ 0x000000F8 | /* 2 MSBit(s)  - out of 26 */ ((wUCS4Char >> 24) & 0x00000003UL));
			bUTF8[1] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 26 */ ((wUCS4Char >> 18) & 0x0000003FUL));
			bUTF8[2] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 26 */ ((wUCS4Char >> 12) & 0x0000003FUL));
			bUTF8[3] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 26 */ ((wUCS4Char >>  6) & 0x0000003FUL));
			bUTF8[4] = (/* 10xxxxx */ 0x00000080 | /* 6 LSBit(s) - out of 26 */ (wUCS4Char & 0x0000003FUL));
			break;

		case 6	:	// 0400 0000-7FFF FFFF   1111110x 10xxxxxx ... 10xxxxxx
			bUTF8[0] = (/* 1111110x */ 0x000000FC | /* 1 MSBit(s)  - out of 31 */ ((wUCS4Char >> 30) & 0x00000001UL));
			bUTF8[1] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 31 */ ((wUCS4Char >> 24) & 0x0000003FUL));
			bUTF8[2] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 31 */ ((wUCS4Char >> 18) & 0x0000003FUL));
			bUTF8[3] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 31 */ ((wUCS4Char >> 12) & 0x0000003FUL));
			bUTF8[4] = (/* 10xxxxx */ 0x00000080 | /* 6 Middle bits  - out of 31 */ ((wUCS4Char >>  6) & 0x0000003FUL));
			bUTF8[5] = (/* 10xxxxx */ 0x00000080 | /* 6 LSBit(s) - out of 31 */ (wUCS4Char & 0x0000003FUL));
			break;

		default	:	/* should not be reached */
			return EFACCESS;
	}

	if (pbUsedLen != NULL)
		*pbUsedLen = eLen;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
EXC_TYPE EncodeBinToUtf8 (const BYTE binBuf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
								  BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */)
{
	if (pdwInEnc != NULL)
		*pdwInEnc = 0;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = 0;

	if (0 == dwInLen)
		return EOK;
	if ((NULL == binBuf) || (NULL == bUTF8))
		return EPARAM;

	DWORD dwEncLen=0, dwUsedLen=0;
	for (; (dwEncLen < dwInLen) && (dwUsedLen < dwOutLen); dwEncLen++)
	{
		BYTE		eLen=0;
		EXC_TYPE	exc=EncodeBinCharToUTF8(binBuf[dwEncLen], (bUTF8 + dwUsedLen), (dwOutLen - dwUsedLen), &eLen);
		if (exc != EOK)
		{
			// if cannot accomodate encoding, then stop (no error)
			if (EOVERFLOW == exc)
				break;
			else
				return exc;
		}

		dwUsedLen += eLen;
	}

	if (pdwInEnc != NULL)
		*pdwInEnc = dwEncLen;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = dwUsedLen;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
EXC_TYPE EncodeUCS2ToUtf8 (const UINT16 wUCS2Buf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
									BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */)
{
	if (pdwInEnc != NULL)
		*pdwInEnc = 0;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = 0;

	if (0 == dwInLen)
		return EOK;
	if ((NULL == wUCS2Buf) || (NULL == bUTF8))
		return EPARAM;

	DWORD dwEncLen=0, dwUsedLen=0;
	for (; (dwEncLen < dwInLen) && (dwUsedLen < dwOutLen); dwEncLen++)
	{
		BYTE		eLen=0;
		EXC_TYPE	exc=EncodeUCS2CharToUTF8(wUCS2Buf[dwEncLen], (bUTF8 + dwUsedLen), (dwOutLen - dwUsedLen), &eLen);
		if (exc != EOK)
		{
			// if cannot accomodate encoding, then stop (no error)
			if (EOVERFLOW == exc)
				break;
			else
				return exc;
		}

		dwUsedLen += eLen;
	}

	if (pdwInEnc != NULL)
		*pdwInEnc = dwEncLen;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = dwUsedLen;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/* Note: returns EOK even if not entire input buffer used - check the encoded vs. the input length */
EXC_TYPE EncodeUCS4ToUtf8 (const UINT32 wUCS4Buf[], const DWORD dwInLen, DWORD *pdwInEnc  /* may be NULL */,
									BYTE bUTF8[], const DWORD dwOutLen, DWORD *pdwOutUsed  /* may be NULL */)
{
	if (pdwInEnc != NULL)
		*pdwInEnc = 0;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = 0;

	if (0 == dwInLen)
		return EOK;
	if ((NULL == wUCS4Buf) || (NULL == bUTF8))
		return EPARAM;

	DWORD dwEncLen=0, dwUsedLen=0;
	for (; (dwEncLen < dwInLen) && (dwUsedLen < dwOutLen); dwEncLen++)
	{
		BYTE		eLen=0;
		EXC_TYPE	exc=EncodeUCS4CharToUTF8(wUCS4Buf[dwEncLen], (bUTF8 + dwUsedLen), (dwOutLen - dwUsedLen), &eLen);
		if (exc != EOK)
		{
			// if cannot accomodate encoding, then stop (no error)
			if (EOVERFLOW == exc)
				break;
			else
				return exc;
		}

		dwUsedLen += eLen;
	}

	if (pdwInEnc != NULL)
		*pdwInEnc = dwEncLen;
	if (pdwOutUsed != NULL)
		*pdwOutUsed = dwUsedLen;

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE GetUCS4ToUTF8BufEncLen (const UINT32 wUCS4Buf[], const DWORD dwInLen, DWORD *pdwInEnc)
{
	if (NULL == pdwInEnc)
		return EPARAM;
	*pdwInEnc = 0;

	if (0 == dwInLen)
		return EOK;

	if (NULL == wUCS4Buf)
		return EBADBUFF;

	for (DWORD dwEncLen=0; dwEncLen < dwInLen; dwEncLen++)
	{
		const BYTE	eLen=GetUCS4ToUTF8CharEncodingLength(wUCS4Buf[dwEncLen]);
		if (eLen > MAX_UCS4_CHAR_UTF8_ENC_LEN)
			return ESTATE;

		*pdwInEnc += eLen;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE GetUCS2ToUTF8BufEncLen (const UINT16 wUCS2Buf[], const DWORD dwInLen, DWORD *pdwInEnc)
{
	if (NULL == pdwInEnc)
		return EPARAM;
	*pdwInEnc = 0;

	if (0 == dwInLen)
		return EOK;

	if (NULL == wUCS2Buf)
		return EBADBUFF;

	for (DWORD dwEncLen=0; dwEncLen < dwInLen; dwEncLen++)
	{
		const BYTE	eLen=GetUCS2ToUTF8CharEncodingLength(wUCS2Buf[dwEncLen]);
		if (eLen > MAX_UCS2_CHAR_UTF8_ENC_LEN)
			return ESTATE;

		*pdwInEnc += eLen;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

EXC_TYPE GetBinToUTF8BufEncLen (const BYTE binBuf[], const DWORD dwInLen, DWORD *pdwInEnc)
{
	if (NULL == pdwInEnc)
		return EPARAM;
	*pdwInEnc = 0;

	if (0 == dwInLen)
		return EOK;

	if (NULL == binBuf)
		return EBADBUFF;

	for (DWORD dwEncLen=0; dwEncLen < dwInLen; dwEncLen++)
	{
		const BYTE	eLen=GetBinToUTF8CharEncodingLength(binBuf[dwEncLen]);
		if (eLen > MAX_BIN_CHAR_UTF8_ENC_LEN)
			return ESTATE;

		*pdwInEnc += eLen;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
//		This code assumes TCHAR == char
#	if defined(UNICODE) || defined(_UNICODE)
#		error "CUTF8StrValue implementation N/A for WIDE chars"
#	endif

/*		Unless specified otherwise, the original value is NOT copied if
 * it needs no UTF-8 encoding. If a value is not copied, then the
 * supplied pointer MUST remain valid throughout the usage of this class
 */
EXC_TYPE CUTF8StrValue::SetValue (LPCTSTR lpszValue, const UINT32 ulVLen, const BOOLEAN fCopyIt)
{
	Reset();

	if (0 == ulVLen)
	{
		m_lpszRetVal = (NULL == lpszValue) ? NULL : _T("");
		return EOK;
	}

	DWORD		dwEncLen=0;
	EXC_TYPE	exc=::GetBinToUTF8BufEncLen((BYTE *) lpszValue, ulVLen, &dwEncLen);
	if (exc != EOK)
		return exc;

	if (dwEncLen != ulVLen)
	{
		if (NULL == (m_lpszTmpVal=new TCHAR[dwEncLen+sizeof(NATIVE_WORD)]))
			return EMEM;

		DWORD	dwInEnc=0, dwOutUsed=0;
		if ((exc=::EncodeBinToUtf8((BYTE *) lpszValue, ulVLen, &dwInEnc, (BYTE *) m_lpszTmpVal, dwEncLen+1, &dwOutUsed)) != EOK)
			return exc;

		// make sure entire data has been encoded
		if (dwInEnc != ulVLen)
			return ESTATE;

		m_lpszTmpVal[dwOutUsed] = _T('\0');	// make sure this is a string
		m_lpszRetVal = m_lpszTmpVal;
	}
	else	// if same length, assume no changes required
	{
		if (fCopyIt)
		{
			if ((exc=::strupdatebuf(lpszValue, ulVLen, m_lpszTmpVal)) != EOK)
				return exc;

			m_lpszRetVal = m_lpszTmpVal;
		}
		else
		{
			m_lpszRetVal = lpszValue;
		}
	}

	m_ulRetLen = dwEncLen;
	return EOK;
}
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/
