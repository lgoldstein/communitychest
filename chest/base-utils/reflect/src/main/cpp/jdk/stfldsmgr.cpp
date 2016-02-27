#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

jint CJStaticFieldsManager::UpdateIntField (LPCTSTR lpszName, const jboolean fSetIt, jint& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIIntFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticIntField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticIntField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateByteField (LPCTSTR lpszName, const jboolean fSetIt, jbyte& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIByteFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticByteField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticByteField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateLongField (LPCTSTR lpszName, const jboolean fSetIt, jlong& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNILongFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticLongField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticLongField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateBooleanField (LPCTSTR lpszName, const jboolean fSetIt, jboolean& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIBooleanFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticBooleanField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticBooleanField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateShortField (LPCTSTR lpszName, const jboolean fSetIt, jshort& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNIShortFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticShortField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticShortField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateCharField (LPCTSTR lpszName, const jboolean fSetIt, jchar& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, szJNICharFieldSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticCharField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticCharField(m_clazz, fID);

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJStaticFieldsManager::UpdateObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean fSetIt, jobject& iVal)
{
	jfieldID	fID=NULL;
	jint		err=GetObjectFieldID(lpszName, lpszSig, fID);
	if (err != EOK)
		return err;

	if (fSetIt)
		m_envRef.SetStaticObjectField(m_clazz, fID, iVal);
	else
		iVal = m_envRef.GetStaticObjectField(m_clazz, fID);

	return EOK;
}

//////////////////////////////////////////////////////////////////////////////
