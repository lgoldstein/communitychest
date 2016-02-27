#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

void CJIntArray::Release ()
{
	if ((m_cInts != NULL) && (JNI_TRUE == m_fIsCopy))
		m_Env.ReleaseIntArrayElements(m_jInts, m_cInts);

	m_cInts = NULL;
	m_jInts = NULL;
	m_arLen = 0;
	m_fIsCopy = JNI_FALSE;
}

/*--------------------------------------------------------------------------*/

CJIntArray& CJIntArray::operator= (jintArray jInts)
{
	Release();

	if ((m_jInts=jInts) != NULL)
	{
		m_cInts = m_Env.GetIntArrayElements(m_jInts, m_fIsCopy);
		m_arLen = ((NULL == m_cInts) ? 0 : m_Env.GetArrayLength(m_jInts));
	}

	return *this;
}

//////////////////////////////////////////////////////////////////////////////

void CJByteArray::Release ()
{
	if ((m_cBytes != NULL) && (JNI_TRUE == m_fIsCopy))
		m_Env.ReleaseByteArrayElements(m_jBytes, m_cBytes);

	m_cBytes = NULL;
	m_jBytes = NULL;
	m_arLen = 0;
	m_fIsCopy = JNI_FALSE;
}

/*--------------------------------------------------------------------------*/

CJByteArray& CJByteArray::operator= (jbyteArray jBytes)
{
	Release();

	if ((m_jBytes=jBytes) != NULL)
	{
		m_cBytes = m_Env.GetByteArrayElements(m_jBytes, m_fIsCopy);
		m_arLen = ((NULL == m_cBytes) ? 0 : m_Env.GetArrayLength(m_jBytes));
	}

	return *this;
}

//////////////////////////////////////////////////////////////////////////////

// copies specified region char array to specified buffer
// NOTE: does not put a terminating '\0' - if non-ASCII character found, then error returned
jint CJCharArray::copyASCIIChars (const jint startIndex /* inclusive */, const jint endIndex /* exclusive */, LPTSTR lpszDst, const jint dstLen)
{
	LPCJSTR	jsSrc=m_cChars;
	jint		cpyLen=(endIndex - startIndex);
	if ((endIndex > m_arLen) || (startIndex > endIndex) || (NULL == lpszDst) || (cpyLen > dstLen) || (cpyLen > m_arLen))
		return EPARAM;

	LPTSTR	lpszDstPos=lpszDst;
	for (jint	curPos=startIndex; curPos < endIndex; curPos++, jsSrc++, lpszDstPos++)
	{
		jchar	jc=(*jsSrc);
		if (jc > 0x00FF)
			return ELOGNAMESYNTAX;
		else
			*lpszDstPos = (TCHAR) (jc & 0x00FF);
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

void CJCharArray::Release ()
{
	if ((m_cChars != NULL) && (JNI_TRUE == m_fIsCopy))
		m_Env.ReleaseCharArrayElements(m_jChars, m_cChars);

	m_cChars = NULL;
	m_jChars = NULL;
	m_arLen = 0;
	m_fIsCopy = JNI_FALSE;
}

/*--------------------------------------------------------------------------*/

CJCharArray& CJCharArray::operator= (jcharArray jChars)
{
	Release();

	if ((m_jChars=jChars) != NULL)
	{
		m_cChars = m_Env.GetCharArrayElements(m_jChars, m_fIsCopy);
		m_arLen = ((NULL == m_cChars) ? 0 : m_Env.GetArrayLength(m_jChars));
	}

	return *this;
}

//////////////////////////////////////////////////////////////////////////////
