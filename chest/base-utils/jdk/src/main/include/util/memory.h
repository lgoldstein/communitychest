#ifndef _UTL_MEMORY_H_
#define _UTL_MEMORY_H_

/*
 * File
 *		memory.h
 *
 * Purpose
 *
 *			Contains declarations of useful manipulations on BYTE/WORD/DWORD
 * arrays/memory.
 *
 * Programmer
 *		LYOR	G.
 *
 * Current Revision
 *		$Revision: 561 $
 *
 * Date
 *		29-March-1993
 */

#include <_types.h>
#include <string.h>
#include <util/errors.h>

/*
 * History of changes:
 *
 * (1) Originally created by Ofer Mendelevitch as :include:rmx/withplm.h.
 *		 (Version 1.0: 22-November-92), to imitate :inc:pas286/withplm.pub
 *		 Changed to current place & format by LYOR G. after prolonged discussions
 *		 with Ofer & Yair Oren.
 */

/*--------------------------------------------------------------------------*/

extern const WORD architecture_word;
extern const BYTE *architecture_bytes;

#define LO_ENDIAN_VALUE	0xA5

#ifdef __cplusplus
inline BOOLEAN is_lo_endian ()
{
	if (architecture_bytes[0] == (BYTE) LO_ENDIAN_VALUE)
		return(TRUE);
	else
		return(FALSE);
}
#else
#define is_lo_endian()	\
	((architecture_bytes[0] == (BYTE) (LO_ENDIAN_VALUE)) ? TRUE : FALSE)
#endif

/*--------------------------------------------------------------------------*/

/*
 * These functions return the position of the first different array member
 * between the source and destination. If they are equal it returns a value
 * greater or equal to length (NOTE: there exists a more general "memcmp" fns.)
 */

extern size_t compare_bytes (const BYTE src[], const BYTE dst[], size_t length);

extern size_t compare_words (const WORD src[], const WORD dst[], size_t length);

extern size_t compare_dwords (const DWORD src[], const DWORD dst[], size_t length);

/*--------------------------------------------------------------------------*/

/*
 * set_byte/word/dword "smear" the array using the given value (NOTE: there is
 * a more general function - "memset")
 */

extern void set_byte (BYTE value, BYTE target_buf[], size_t count);

extern void set_word (WORD value, WORD target_buf[], size_t count);

extern void set_dword (DWORD	value, DWORD  target_buf[], size_t count);

/*--------------------------------------------------------------------------*/

/*
 *		These functions return the FIRST position of the requested value within
 * the target array. If value was not found, then a value greater or equal to
 * the array's length is returned.
 *		The functions ending with "r" search starting at the array's END (the
 * returned values, however, still refer to position relative to start of
 * array).
 *
 *		The "find_sorted_..." functions assume that input array is sorted in
 * ascending order (the "r" function assume descending order). The returned
 * value points to the expected place (if any) of the value. The caller must
 * first check that returned index is less than "count", and then check that
 * the array member pointed to by the returned index indeed contains the
 * sought value.
 */

extern size_t find_byte (const BYTE buf[], BYTE value, size_t count);
extern size_t find_byter (const BYTE buf[], BYTE value, size_t count);
extern size_t find_sorted_byte (const BYTE buf[], BYTE value, size_t count);
extern size_t find_sorted_byter (const BYTE buf[], BYTE value, size_t count);

extern size_t find_word (const WORD buf[], WORD value, size_t count);
extern size_t find_wordr (const WORD buf[], WORD value, size_t count);
extern size_t find_sorted_word (const WORD buf[], WORD value, size_t count);
extern size_t find_sorted_wordr (const WORD buf[], WORD value, size_t count);

extern size_t find_dword (const DWORD buf[], DWORD value, size_t count);
extern size_t find_dwordr (const DWORD buf[], DWORD value, size_t count);
extern size_t find_sorted_dword (const DWORD buf[], DWORD value, size_t count);
extern size_t find_sorted_dwordr (const DWORD buf[], DWORD value, size_t count);


/*--------------------------------------------------------------------------*/
/*								  merge_bytes/words/dwords
 *								  ------------------------
 *		Merge 2 sorted array into a new (sorted) array. Arrays are assumed to be
 * sorted in ASCENDING order (the "r" version(s) assume descending order)
 *
 * Function returns length of target array (which is the sum of the 2 merged).
 */
/*--------------------------------------------------------------------------*/

extern size_t merge_bytes (const BYTE a1[], size_t c1,
									const BYTE a2[], size_t c2,
									BYTE		  trgt[]);

extern size_t merge_words (const WORD a1[], size_t c1,
									const WORD a2[], size_t c2,
									WORD		  trgt[]);

extern size_t merge_dwords (const DWORD a1[], size_t c1,
									 const DWORD a2[], size_t c2,
									 DWORD		 trgt[]);


extern size_t merge_bytesr (const BYTE a1[], size_t c1,
									 const BYTE a2[], size_t c2,
									 BYTE			trgt[]);

extern size_t merge_wordsr (const WORD a1[], size_t c1,
									 const WORD a2[], size_t c2,
									 WORD			trgt[]);

extern size_t merge_dwordsr (const DWORD a1[], size_t c1,
									  const DWORD a2[], size_t c2,
									  DWORD		  trgt[]);

/*--------------------------------------------------------------------------*/

/*
 *		These functions return the index of the first value which is NOT the
 * given value (i.e they SKIP OVER the value). If ALL values equal the given
 * one, a value greater or equal to the array's length is returned.
 *		The functions ending with "r" skip starting at the array's END (the
 * returned values, however, still refer to position relative to start of
 * array).
 */

extern size_t skip_byte (const BYTE buf[], BYTE value, size_t count);

extern size_t skip_word (const WORD buf[], WORD value, size_t count);

extern size_t skip_dword (const DWORD buf[], DWORD value, size_t count);

extern size_t skip_byter (const BYTE buf[], BYTE value, size_t count);

extern size_t skip_wordr (const WORD buf[], WORD value, size_t count);

extern size_t skip_dwordr (const DWORD buf[], DWORD value, size_t count);

/*--------------------------------------------------------------------------*/
/*									move_bytes/words
 *									----------------
 *		The analog(s) of "memcpy" EXCEPT FOR 2 MAJOR DIFFERENCES :
 *
 * 1. The procedure(s) handle overlap between the "src" and "dst".
 * 2. The procedure(s) "move" the memory byte/word after byte/word WITHOUT
 *		ANY OPTIMIZATION (e.g. moving half as much words instead of bytes). This
 *		difference may matter when moving memory between processors with
 *		different data bus width (e.g. i188 (8 bits) vs. i386 (16/32 bits)).
 */
/*--------------------------------------------------------------------------*/

extern void move_bytes (const BYTE src[], BYTE dst[], size_t len);

extern void move_words (const WORD src[], WORD dst[], size_t len);

extern void move_dwords (const DWORD src[], DWORD dst[], size_t len);

/*--------------------------------------------------------------------------*/

/*								translate
 *								---------
 *		Copies each byte in the "src" to the "dst" array using an intermediate
 * translation table (i.e. dst[i] = tbl[src[i]]). TRUE if successful.
 */

extern BOOLEAN translate (const BYTE src[], BYTE dst[], size_t len, const BYTE tbl[]);

/*--------------------------------------------------------------------------*/

/*
 * reverse the byte/word/dword-s order of an array.
 */

extern void reverse_bytes (BYTE buf[], size_t buf_len);

extern void reverse_words (WORD buf[], size_t buf_len);

extern void reverse_dwords (DWORD buf[], size_t buf_len);

/*--------------------------------------------------------------------------*/

/*		Update a running CRC with the bytes buf[0..len-1]--the CRC should be
 * initialized to all 1's, and the transmitted value is the 1's complement
 * of the final running CRC (see the crc() routine below)).
 */

/*--------------------------------------------------------------------------*/

extern UINT32 calc_crc32 (const UINT32 crc, const BYTE buf[], const UINT32 len);

#ifdef __cplusplus
/* Return the CRC of the bytes buf[0..len-1]. */
inline UINT32 crc32_buf (const BYTE buf[], const UINT32 len)
{
	return (calc_crc32(0xffffffffUL, buf, len) ^ 0xffffffffUL);
}
#else
#define crc32_buf(b,len) (calc_crc32(0xffffffffUL, (buf), (len)) ^ 0xffffffffUL)
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus

// callback for enumerating data collection items
typedef EXC_TYPE (*CVSD_ENUM_CFN)(LPVOID	pItem, LPVOID pArg, BOOLEAN& fContEnum);

// collection class for variable size data
class CVSDCollection {
	private:
		UINT32	m_ulMaxItems;
		UINT32	m_ulCurItems;
		UINT32	m_ulGrwItems;	// how much to grow if necessary
		LPVOID	*m_pItems;

		// disable copy constructor and assignment operator
		CVSDCollection (const CVSDCollection& );
		CVSDCollection& operator= (const CVSDCollection& );

		EXC_TYPE AppendEntry (LPVOID pE /* actually an internal structure */);

		EXC_TYPE AddEntry (LPVOID pDE, const UINT32 ulDataSize);
		EXC_TYPE DelEntry (LPVOID pDE, const UINT32 ulDataSize);
		EXC_TYPE FindEntry (LPVOID pDE, const UINT32 ulDataSize, UINT32& ulIdx) const;
		EXC_TYPE SetEntry (const UINT32 ulItemIndex, LPVOID pData, const UINT32 ulDataLen);

	public:
		// also default constructor
		CVSDCollection (const UINT32 ulMaxItems=0, const UINT32 ulGrow=0);

		// Note(s):
		//
		//		a. grow factor may be zero - i.e. when limit is reached no more items
		//			are added.
		//
		//		b. cannot be re-initialized unless cleared first
		virtual EXC_TYPE SetParams (const UINT32 ulMaxItems, const UINT32 ulGrow);

		virtual EXC_TYPE SetParams (const CVSDCollection& dColl)
		{
			return SetParams(dColl.m_ulMaxItems, dColl.m_ulGrwItems);
		}

		virtual EXC_TYPE AddItem (LPVOID pData)
		{
			return AddEntry(pData, 0UL);
		}

		// auto allocates data item and copies into it
		virtual EXC_TYPE AddItem (LPVOID pData, const UINT32 ulDataSize)
		{
			return AddEntry(pData, ulDataSize);
		}

		// auto allocates item data and copies into it - adds '\0' at end of copied characters (may specify zero size)
		virtual EXC_TYPE AddChars (LPCTSTR s, const UINT32 ulNumChars)
		{
			if (0 == ulNumChars)
				return AddItem((LPVOID) _T(""), sizeof(TCHAR));
			else
				return AddItem((LPVOID) s, ulNumChars * sizeof(TCHAR));
		}

		// auto allocates item data and copies into - may add null/empty string
		virtual EXC_TYPE AddStr (LPCTSTR s)
		{
			return AddChars(s, (NULL == s) ? 0 : ::_tcslen(s));
		}

		// last string must be NULL (Note: empty strings ("") ARE not
		// considered NULL)
		// may be NULL array - in which case, an empty entry is created (same
		// as if all items were "")
		virtual EXC_TYPE ConcatStringsItem (LPCTSTR s[]);

		// NULL entries ARE skipped - up to specified number of strings (may be ZERO)
		// if zero number of strings then an empty entry is created  (same as if
		// all items were "")
		virtual EXC_TYPE ConcatStringsItem (LPCTSTR s[], const UINT32 ulNumStrs);

		// Note: returns error if index out of range
		virtual EXC_TYPE SetItem (const UINT32 ulIndex, LPVOID pData)
		{
			return SetEntry(ulIndex, pData, 0);
		}

		virtual EXC_TYPE SetItem (const UINT32 ulIndex, LPVOID pData, const UINT32 ulDataLen)
		{
			return SetEntry(ulIndex, pData, ulDataLen);
		}

		// Note: returns error if index out of range
		virtual EXC_TYPE ClearItem (const UINT32 ulIndex)
		{
			return SetItem(ulIndex, NULL);
		}

		// Note: compares only pointer
		virtual BOOL FindItem (LPVOID pData) const
		{
			UINT32	ulIdx=(UINT32) (-1);
			return (FindEntry(pData, 0, ulIdx) == EOK);
		}

		virtual EXC_TYPE GetItemIndex (LPVOID pData, UINT32& ulIdx) const
		{
			return FindEntry(pData, 0, ulIdx);
		}

		// performs "memcmp" on data
		virtual BOOL FindItem (LPVOID pData, const UINT32 ulDataSize) const
		{
			UINT32	ulIdx=(UINT32) (-1);
			return (FindEntry(pData, ulDataSize, ulIdx) == EOK);
		}

		virtual EXC_TYPE GetItemIndex (LPVOID pData, const UINT32 ulDataSize, UINT32& ulIdx) const
		{
			return FindEntry(pData, ulDataSize, ulIdx);
		}

		virtual EXC_TYPE RemoveItem (LPVOID pData)
		{
			return DelEntry(pData, 0UL);
		}

		virtual EXC_TYPE RemoveItem (const UINT32 ulItemIndex);

		virtual EXC_TYPE RemoveItem (LPVOID pData, const UINT32 ulDataSize)
		{
			return DelEntry(pData, ulDataSize);
		}

		virtual UINT32 GetSize () const { return m_ulCurItems; }
		virtual UINT32 GetGrowFactor () const { return m_ulGrwItems; }

		// data len is ZERO for items that have only the PTR available		
		virtual EXC_TYPE GetData (const UINT32 ulIdx, LPVOID& pData, UINT32& ulDataLen) const;
		virtual EXC_TYPE GetData (const UINT32 ulIdx, LPVOID& pData) const
		{
			UINT32	ulDataLen=0;
			return GetData(ulIdx, pData, ulDataLen);
		}

		virtual LPVOID operator[] (const UINT32 ulIdx) const;

		virtual EXC_TYPE EnumItems (CVSD_ENUM_CFN lpfnEcfn, LPVOID pArg) const;

		virtual EXC_TYPE Merge (const CVSDCollection& dc);

		// removes all data items
		virtual EXC_TYPE Reset ();

		// must be re-initialized afterwards
		virtual EXC_TYPE Clear ();

		virtual ~CVSDCollection () { Clear(); }

		friend class CVSDCollEnum;
};

typedef CAllocStructPtrGuard<CVSDCollection>	CVSDCGuard;
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
// enumerator for the CVSDCollection class
class CVSDCollEnum {
	private:
		const CVSDCollection&	m_dc;
		UINT32						m_ulCurItem;

		// disable copy constructor and assignment operator
		CVSDCollEnum (const CVSDCollEnum& );
		CVSDCollEnum& operator= (const CVSDCollEnum& );

	public:
		CVSDCollEnum (const CVSDCollection&	dc) : m_dc(dc), m_ulCurItem(0) { }

		virtual UINT32 GetSize () const { return m_dc.GetSize(); }

		// returns EEOF if no more items
		virtual EXC_TYPE GetFirstItem (LPVOID& pItem)
		{
			m_ulCurItem = 0;
			return GetNextItem(pItem);
		}

		// returns EEOF if no more items
		virtual EXC_TYPE GetNextItem (LPVOID& pItem);

		virtual ~CVSDCollEnum () { }
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
class IBufBuilder {
	protected:
		LPBYTE	m_pBuf;
		UINT32	m_ulCurLen;
		UINT32	m_ulMaxLen;

		// disable copy constructor and assignment operator
		IBufBuilder (const IBufBuilder& );
		IBufBuilder& operator= (const IBufBuilder& );

		// called whenever there is not enough space to add data
		virtual EXC_TYPE ReadjustBuffer (const UINT32 ulMinGrow)
		{
			return ((EXC_TYPE) (-1));
		}

	public:
		IBufBuilder ()
			: m_pBuf(NULL), m_ulCurLen(0), m_ulMaxLen(0)
		{
		}

		IBufBuilder (LPBYTE pBuf, const UINT32 ulMaxLen)
			: m_pBuf(pBuf), m_ulCurLen(0), m_ulMaxLen(ulMaxLen)
		{
		}

		IBufBuilder (LPBYTE pBuf, const UINT32 ulCurLen, const UINT32 ulMaxLen)
			: m_pBuf(pBuf), m_ulCurLen(ulCurLen), m_ulMaxLen(ulMaxLen)
		{
		}

		virtual void ResetData ()
		{
			m_ulCurLen = 0;
		}

		virtual const LPBYTE GetBuf () const
		{
			return m_pBuf;
		}

		virtual const LPBYTE GetCurPos () const
		{
			if (NULL == m_pBuf)
				return NULL;
			else
				return (m_pBuf + m_ulCurLen);
		}

		virtual const UINT32 GetCurLen () const
		{
			return m_ulCurLen;
		}

		virtual const UINT32 GetMaxLen () const
		{
			return m_ulMaxLen;
		}

		virtual EXC_TYPE AddData (const BYTE bData[], const UINT32 ulDataLen);

		virtual EXC_TYPE AddByte (const BYTE bVal)
		{
			return AddData(&bVal, (sizeof bVal));
		}

		virtual EXC_TYPE AddWord (const WORD bVal, const bool fLittleEndian);

		virtual EXC_TYPE AddDword (const DWORD bVal, const bool fLittleEndian);

		virtual ~IBufBuilder ()
		{
		}
};

class CIncBufBuilder : public IBufBuilder {
	private:
		UINT32	m_ulGrowSize;
		BOOLEAN	m_fAutoRelease;

		// disable copy constructor and assignment operator
		CIncBufBuilder (CIncBufBuilder& );
		CIncBufBuilder& operator= (CIncBufBuilder& );

	protected:
		// called whenever there is not enough space to add data
		virtual EXC_TYPE ReadjustBuffer (const UINT32 ulMinGrow);

	public:
		// Note: can be called only once !!!
		virtual EXC_TYPE Init (const UINT32 ulInitialSize, const UINT32 ulGrowSize, const BOOLEAN fAutoRelease);

		CIncBufBuilder (const UINT32 ulInitialSize=0, const UINT32 ulGrowSize=0, const BOOLEAN fAutoRelease=TRUE)
			: IBufBuilder(), m_fAutoRelease(fAutoRelease), m_ulGrowSize(ulGrowSize)
		{
			if (ulInitialSize != 0)
				Init(ulInitialSize, ulGrowSize, fAutoRelease);
		}

		virtual BOOLEAN IsAutoRelease () const
		{
			return m_fAutoRelease;
		}

		virtual void SetAutoRelease (const BOOLEAN fAutoRelease)
		{
			m_fAutoRelease = fAutoRelease;
		}

		virtual ~CIncBufBuilder ();
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

/*
 * sdbm - This algorithm was created for sdbm (a reimplementation of ndbm) database library. 
 * it does well in scrambling bits, causing better distribution of the keys and fewer splits. 
 * it also happens to be a good general hashing function with good distribution. the actual function 
 * is hash(i) = hash(i - 1) * 65599 + str[i]; what is included below is the faster version 
 * used in gawk. [there is even a faster, duff-device version] the magic constant 65599 was 
 * picked out of thin air while experimenting with different constants, and turns out to be a prime. 
 * this is one of the algorithms used in berkeley db (see sleepycat) and elsewhere. 
 */
extern UINT32 sdbm_hash_buffer (const BYTE buf[], const UINT32 ulBufLen);

/* 
 * djb2 this algorithm (k=33) was first reported by dan bernstein many years ago in 
 * comp.lang.c. another version of this algorithm (now favored by bernstein) uses xor: 
 * hash(i) = hash(i - 1) * 33 ^ str[i]; 
 * the magic of number 33 (why it works better than many other constants, prime or not) has never been 
 * adequately explained. 
 */
extern UINT32 djb2_hash_buffer (const BYTE buf[], const UINT32 ulBufLen);

/*--------------------------------------------------------------------------*/

#ifdef __cplusplus
extern UINT32 extractBufferDataValue (const BYTE buf[], const BYTE len, const bool fLittleEndian);

// class for handling data from a buffer
class IBufConsumer {
	protected:
		LPBYTE	m_pBuf;
		DWORD		m_dwStartPos;
		DWORD		m_dwCurPos;
		DWORD		m_dwMaxLen;

	public:
		IBufConsumer ()
		{
			Clear();
		}

		IBufConsumer (LPBYTE pBuf, const DWORD dwStartPos, const DWORD dwMaxLen)
			: m_pBuf(pBuf)
			, m_dwStartPos(dwStartPos)
			, m_dwCurPos(dwStartPos)
			, m_dwMaxLen(dwMaxLen)
		{
		}

		IBufConsumer (LPBYTE pBuf, const DWORD dwMaxLen)
			: m_pBuf(pBuf)
			, m_dwStartPos(0)
			, m_dwCurPos(0)
			, m_dwMaxLen(dwMaxLen)
		{
		}

		virtual EXC_TYPE SetBuffer (LPBYTE pBuf, const DWORD dwStartPos, const DWORD dwMaxLen);

		virtual EXC_TYPE SetBuffer (LPBYTE pBuf, const DWORD dwMaxLen)
		{
			return SetBuffer(pBuf, 0, dwMaxLen);
		}

		virtual LPBYTE GetBuffer () const
		{
			return m_pBuf;
		}

		virtual DWORD GetCurPos () const
		{
			return m_dwCurPos;
		}

		virtual DWORD GetMaxLen () const
		{
			return m_dwMaxLen;
		}

		virtual DWORD GetRemainLen () const
		{
			return (m_dwMaxLen - m_dwCurPos);
		}

		virtual DWORD GetConsumedLen () const
		{
			return (m_dwCurPos - m_dwStartPos);
		}

		virtual EXC_TYPE GetWord (WORD& v, const bool fLittleEndian);

		virtual EXC_TYPE GetDword (DWORD& v, const bool fLittleEndian);

		// Note: returns error if not enough data remaining
		virtual EXC_TYPE GetData (BYTE v[], const DWORD dwLen);

		virtual EXC_TYPE GetByte (BYTE& v)
		{
			return GetData(&v, (sizeof v));
		}

		// Note: returns error if asked to seek beyond start/end
		virtual EXC_TYPE Seek (const SINT32 sLen);

		virtual void Clear ();

		virtual void Rewind ()
		{
			m_dwCurPos = m_dwStartPos;
		}

		virtual ~IBufConsumer ()
		{
		}
};
#endif /* __cplusplus */

/*--------------------------------------------------------------------------*/

#endif
