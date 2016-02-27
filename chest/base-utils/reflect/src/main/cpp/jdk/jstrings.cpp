#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

void CJUTFString::Release ()
{
	if ((m_lpszStr != NULL) && (JNI_TRUE == m_isCopy))
		m_Env.ReleaseStringUTFChars(m_jstr, m_lpszStr);

	m_isCopy = JNI_FALSE;
	m_lpszStr = NULL;
	m_jstr = NULL;
	m_jSize = 0;
}

/*--------------------------------------------------------------------------*/

CJUTFString& CJUTFString::operator= (jstring jstr)
{
	Release();

	if ((m_jstr=jstr) != NULL)
	{
		m_lpszStr = m_Env.GetStringUTFChars(m_jstr, m_isCopy);
		m_jSize = m_Env.GetStringUTFLength(m_jstr);
	}

	return *this;
}

//////////////////////////////////////////////////////////////////////////////

void CJString::Release ()
{
	if ((m_jcStr != NULL) && (JNI_TRUE == m_isCopy))
		m_Env.ReleaseStringChars(m_jstr, m_jcStr);

	m_jcStr = NULL;
	m_jstr = NULL;
	m_jSize = 0;
	m_isCopy = JNI_FALSE;
}

/*--------------------------------------------------------------------------*/

CJString& CJString::operator= (jstring jstr)
{
	Release();

	if ((m_jstr=jstr) != NULL)
	{
		m_jcStr = m_Env.GetStringChars(m_jstr, m_isCopy);
		m_jSize = m_Env.GetStringLength(m_jstr);
	}

	return *this;
}

//////////////////////////////////////////////////////////////////////////////

// Note !!! marks Java string as auto-release (returns EOK if successful)
jint CPureJString::SetValue (LPCTSTR lpszVal, const jboolean fAutoRelease)
{
	Release();

	if (lpszVal != NULL)
	{
		CUTF8StrValue	utf8Value;
		jint				exc=utf8Value.SetValue(lpszVal, FALSE);
		if (exc != EOK)
			return exc;

		m_jstr = m_env.NewStringUTF(utf8Value.GetValue());
	}

	SetAutoRelease(fAutoRelease);
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
