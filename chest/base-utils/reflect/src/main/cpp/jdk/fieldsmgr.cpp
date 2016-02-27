#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

jint IJFieldsManager::GetStringField (LPCTSTR lpszName, LPTSTR lpszVal, const jint iMaxLen)
{
	if ((NULL == lpszVal) || (iMaxLen <= 0))
		return EPARAM;
	*lpszVal = _T('\0');

	jstring				jsValue=NULL;
	CJObjectRefGuard	jsg(m_envRef, (jobject &) jsValue);
	jint		err=GetStringField(lpszName, jsValue);
	if (err != EOK)
		return err;

	CJUTFString	jsuValue(m_envRef, jsValue);
	LPCTSTR		lpszValue=jsuValue.GetNativeString();
	if (IsEmptyStr(lpszValue))
		return EOK;

	const jint	vLen=::_tcslen(lpszValue);
	if (vLen > iMaxLen)
		return EOVERFLOW;

	::_tcscpy(lpszVal, lpszValue);
	return EOK;
}

/*--------------------------------------------------------------------------*/

jint IJFieldsManager::GetEnvFieldID (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean isStatic, jfieldID& fID)
{
	fID = NULL;

	if ((NULL == m_env) || (NULL == m_clazz) || IsEmptyStr(lpszName) || IsEmptyStr(lpszSig))
		return EPARAM;

	fID = isStatic ? m_envRef.GetStaticFieldID(m_clazz, lpszName, lpszSig) : m_envRef.GetFieldID(m_clazz, lpszName, lpszSig);
	if (NULL == fID)
		return ENOTLOGNAME;

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

jint CJFieldsManager::UpdateIntField (LPCTSTR lpszName, const jboolean fSetIt, jint& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIIntFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetIntField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetIntField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateByteField (LPCTSTR lpszName, const jboolean fSetIt, jbyte& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIByteFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetByteField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetByteField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateLongField (LPCTSTR lpszName, const jboolean fSetIt, jlong& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNILongFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetLongField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetLongField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateBooleanField (LPCTSTR lpszName, const jboolean fSetIt, jboolean& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIBooleanFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetBooleanField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetBooleanField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateShortField (LPCTSTR lpszName, const jboolean fSetIt, jshort& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIShortFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetShortField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetShortField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateCharField (LPCTSTR lpszName, const jboolean fSetIt, jchar& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNICharFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetCharField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetCharField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJFieldsManager::UpdateObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean fSetIt, jobject& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, lpszSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetObjectField(m_objRef, fID, iVal);
	else
		iVal = m_envRef.GetObjectField(m_objRef, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/
