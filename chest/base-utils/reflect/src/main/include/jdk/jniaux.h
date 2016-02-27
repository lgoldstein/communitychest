#ifndef _JNIAUX_H_
#define _JNIAUX_H_

#include <jni.h>

typedef JNIEnv			*LPJNIEnv;
typedef jbyte			*LPJBYTE;
typedef jshort			*LPJSHORT;
typedef jint			*LPJINT;
typedef jlong			*LPJLONG;
typedef jdouble		*LPJDOUBLE;
typedef jfloat			*LPJFLOAT;
typedef jboolean		*LPJBOOL;

typedef jchar			*LPJSTR;
typedef const jchar	*LPCJSTR;

typedef jvalue			*LPJVALUE;

#include <time.h>

#include <util/errors.h>
#include <util/utf8enc.h>

#define szJNIByteFieldSig		_T("B")
#define szJNICharFieldSig		_T("C")
#define szJNIIntFieldSig		_T("I")
#define szJNILongFieldSig		_T("J")
#define szJNIShortFieldSig		_T("S")
#define szJNIBooleanFieldSig	_T("Z")

/*--------------------------------------------------------------------------*/

#define JavaArrayOfTypeSignature(t)	_T("[") t
#	define JavaArrayOfByteSignature		JavaArrayOfTypeSignature(szJNIByteFieldSig)
#	define JavaArrayOfCharSignature		JavaArrayOfTypeSignature(szJNICharFieldSig)
#	define JavaArrayOfIntSignature		JavaArrayOfTypeSignature(szJNIIntFieldSig)
#	define JavaArrayOfShortSignature		JavaArrayOfTypeSignature(szJNIShortFieldSig)
#	define JavaArrayOfLongSignature		JavaArrayOfTypeSignature(szJNILongFieldSig)
#	define JavaArrayOfBooleanSignature	JavaArrayOfTypeSignature(szJNIBooleanFieldSig)

/*--------------------------------------------------------------------------*/

/* some known classes names */
#define szJNIStringObjectClass			_T("java/lang/String")
#define szJNIEnumerationObjectClass		_T("java/util/Enumeration")
#define szJNISimpleTimeZoneObjectClass _T("java/util/SimpleTimeZone")
#define szJNIIOExceptionObjectClass		_T("java/io/IOException")

#define JAVA_TIMETICKS_PER_SECOND	1000

extern jint ThrowJNIException (JNIEnv *env, LPCTSTR lpszClassName, LPCTSTR lpszMsg /* may be NULL */);

/* generates a full class path of a class */
#define JavaFullClassPath(pkg,nm)					pkg _T("/") nm

/*  generates an inner class name */
#define JavaInnerClassName(className,innerName)	className _T("$") innerName

/* useful "field-of-class-XXX" signature definition */
#define JavaFullClassPathSignature(classPath)	_T("L") classPath _T(";")
#	define szJNIStringFieldSig			JavaFullClassPathSignature(szJNIStringObjectClass)

/* generates an "array-of-XXX" method signature */
#define JavaFullClassPathArraySignature(classPath)		JavaArrayOfTypeSignature(JavaFullClassPathSignature(classPath))

/* generates the signature of an array of objects of specified class */
#define JavaClassArraySignature(pkg,nm)			 JavaFullClassPathArraySignature(JavaFullClassPath(pkg,nm))

/* useful "array-of-XXX" signature definitions */
#define JavaStringsArraySignature					JavaFullClassPathArraySignature(szJNIStringObjectClass)

#ifdef __cplusplus
inline jint ThrowJNIException (JNIEnv *env, LPCTSTR lpszClassName)
{
	return ThrowJNIException(env, lpszClassName, NULL);
}

// C++ interface for reference management
/*--------------------------------------------------------------------------*/

// class used to throw exceptions from "void" functions that encounter errors
class CJNIAuxException {
	private:
		const EXC_TYPE	m_hr;

	public:
		CJNIAuxException (const EXC_TYPE hr)
			: m_hr(hr)
		{
		}

		virtual EXC_TYPE GetLastError () const
		{
			return m_hr;
		}

		virtual ~CJNIAuxException ()
		{
		}
};

/*--------------------------------------------------------------------------*/

inline void CheckObj (jobject obj, const EXC_TYPE nVal=(EXC_TYPE) (-1))
{
	if (NULL == obj)
		throw CJNIAuxException(nVal);
}

/*--------------------------------------------------------------------------*/

inline jboolean BOOL2JNIBOOL (const bool fVal)
{
	return (fVal ? JNI_TRUE : JNI_FALSE);
}

/*--------------------------------------------------------------------------*/

// forward declaration(s)
class	CJNIEnvRef;
class CJUTFString;
class CJString;

/*--------------------------------------------------------------------------*/

// JNI environment wrapper class
class CJNIEnvRef {
	protected:
		LPJNIEnv	m_pEnv;

	public:
		CJNIEnvRef (LPJNIEnv env)
			: m_pEnv(env)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
		}

		virtual CJNIEnvRef& operator= (LPJNIEnv env)
		{
			if (NULL == (m_pEnv=env))
				throw CJNIAuxException(ENOTCONNECTION);
			return *this;
		}

		CJNIEnvRef (const CJNIEnvRef& jr)
			: m_pEnv(jr.m_pEnv)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
		}

		virtual CJNIEnvRef& operator= (const CJNIEnvRef& jr)
		{
			if (NULL == (m_pEnv=jr.m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			return *this;
		}

		virtual operator LPJNIEnv() const
		{
			return m_pEnv;
		}

		virtual JNIEnv *GetEnvironment () const
		{
			return (JNIEnv *) m_pEnv;
		}

		virtual void DeleteLocalRef (jobject lref)
		{
			if (lref != NULL)
				m_pEnv->DeleteLocalRef(lref);
		}

		virtual void DeleteGlobalRef (jobject gref)
		{
			if (gref != NULL)
				m_pEnv->DeleteGlobalRef(gref);
		}

		virtual jclass FindClass (LPCTSTR name)
		{
			if ((NULL == m_pEnv) || IsEmptyStr(name))
				return NULL;
			else
				return m_pEnv->FindClass(name);
		}

		virtual jobject NewGlobalRef (jobject lobj)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewGlobalRef(lobj);
		}

		virtual jboolean IsSameObject (jobject obj1, jobject obj2) const
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->IsSameObject(obj1, obj2);
		}

		virtual jobject AllocObject (jclass clazz)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->AllocObject(clazz);
		}

		virtual jclass GetObjectClass (jobject obj)
		{
			if ((NULL == m_pEnv) || (NULL == obj))
				return NULL;
			else
				return m_pEnv->GetObjectClass(obj);
		}

		virtual jboolean IsInstanceOf (jobject obj, jclass clazz)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->IsInstanceOf(obj, clazz);
		}

		virtual jmethodID GetMethodID (jclass clazz, LPCTSTR name, LPCTSTR sig)
		{
			if ((NULL == clazz) || (NULL == m_pEnv) || IsEmptyStr(name) || IsEmptyStr(sig))
				return NULL;
			else
				return m_pEnv->GetMethodID(clazz, name, sig);
		}

		virtual jmethodID GetObjMethodID (jobject objRef, LPCTSTR name, LPCTSTR sig)
		{
			return GetMethodID(GetObjectClass(objRef), name, sig);
		}

		virtual jobject CallObjectMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->CallObjectMethodV(obj, methodID, args);
		}

		virtual jobject CallObjectMethod (jobject obj, jmethodID methodID, ...);

		virtual jobject CallObjectMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->CallObjectMethodA(obj, methodID, args);
		}

		virtual jboolean CallBooleanMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallBooleanMethodV(obj,methodID,args);
		}

		virtual jboolean CallBooleanMethod (jobject obj, jmethodID methodID, ...);

		virtual jboolean CallBooleanMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallBooleanMethodA(obj,methodID, args);
		}

		virtual jbyte CallByteMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallByteMethodV(obj,methodID,args);
		}

		virtual jbyte CallByteMethod (jobject obj, jmethodID methodID, ...);

		virtual jbyte CallByteMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallByteMethodA(obj,methodID,args);
		}

		virtual jchar CallCharMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallCharMethodV(obj,methodID,args);
		}

		virtual jchar CallCharMethod (jobject obj, jmethodID methodID, ...);

		virtual jchar CallCharMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallCharMethodA(obj,methodID,args);
		}

		virtual jshort CallShortMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallShortMethodV(obj,methodID,args);
		}

		virtual jshort CallShortMethod (jobject obj, jmethodID methodID, ...);

		virtual jshort CallShortMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallShortMethodA(obj,methodID,args);
		}

		virtual jint CallIntMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallIntMethodV(obj,methodID,args);
		}

		virtual jint CallIntMethod (jobject obj, jmethodID methodID, ...);

		virtual jint CallIntMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallIntMethodA(obj,methodID,args);
		}

		virtual jlong CallLongMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallLongMethodV(obj,methodID,args);
		}

		virtual jlong CallLongMethod (jobject obj, jmethodID methodID, ...);

		virtual jlong CallLongMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallLongMethodA(obj,methodID,args);
		}

		virtual jfloat CallFloatMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallFloatMethodV(obj,methodID,args);
		}

		virtual jfloat CallFloatMethod (jobject obj, jmethodID methodID, ...);

		virtual jfloat CallFloatMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallFloatMethodA(obj,methodID,args);
		}

		virtual jdouble CallDoubleMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallDoubleMethodV(obj,methodID,args);
		}

		virtual jdouble CallDoubleMethod (jobject obj, jmethodID methodID, ...);

		virtual jdouble CallDoubleMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->CallDoubleMethodA(obj,methodID,args);
		}

		virtual void CallVoidMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->CallVoidMethodV(obj,methodID,args);
		}

		virtual void CallVoidMethod (jobject obj, jmethodID methodID, ...);

		virtual void CallVoidMethodA (jobject obj, jmethodID methodID, LPJVALUE args)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->CallVoidMethodA(obj,methodID,args);
		}

		virtual jfieldID GetFieldID (jclass clazz, LPCTSTR name, LPCTSTR sig)
		{
			if ((NULL == clazz) || IsEmptyStr(name) || IsEmptyStr(sig) || (NULL == m_pEnv))
				return NULL;
			else
				return m_pEnv->GetFieldID(clazz,name,sig);
		}

		virtual jfieldID GetClassFieldID (LPCTSTR lpszClassPath, LPCTSTR name, LPCTSTR sig)
		{
			return GetFieldID(FindClass(lpszClassPath),name,sig);
		}

		virtual jfieldID GetObjFieldID (jobject objRef, LPCTSTR name, LPCTSTR sig)
		{
			return GetFieldID(GetObjectClass(objRef), name, sig);
		}

		virtual jobject GetObjectField (jobject obj, jfieldID fieldID)
		{
			if ((NULL == obj) || (NULL == fieldID) || (NULL == m_pEnv))
				return NULL;
			else
				return m_pEnv->GetObjectField(obj, fieldID);
		}

		virtual jboolean GetBooleanField (jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetBooleanField(obj,fieldID);
		}

		virtual jbyte GetByteField (jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetByteField(obj,fieldID);
		}

		virtual jchar GetCharField (jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetCharField(obj,fieldID);
		}

		virtual jshort GetShortField(jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetShortField(obj,fieldID);
		}

		virtual jint GetIntField(jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetIntField(obj,fieldID);
		}

		virtual jlong GetLongField(jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetLongField(obj,fieldID);
		}

		virtual jfloat GetFloatField(jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetFloatField(obj,fieldID);
		}

		virtual jdouble GetDoubleField(jobject obj, jfieldID fieldID)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetDoubleField(obj,fieldID);
		}

		virtual void SetObjectField (jobject obj, jfieldID fieldID, jobject val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetObjectField(obj,fieldID,val);
		}

		virtual void SetStringField (jobject obj, jfieldID fieldID, jstring val)
		{
			SetObjectField(obj, fieldID, (jobject) val);
		}

		virtual void SetBooleanField(jobject obj, jfieldID fieldID, jboolean val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetBooleanField(obj,fieldID,val);
		}

		virtual void SetByteField(jobject obj, jfieldID fieldID, jbyte val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetByteField(obj,fieldID,val);
		}

		virtual void SetCharField(jobject obj, jfieldID fieldID, jchar val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetCharField(obj,fieldID,val);
		}

		virtual void SetShortField(jobject obj, jfieldID fieldID, jshort val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetShortField(obj,fieldID,val);
		}

		virtual void SetIntField(jobject obj, jfieldID fieldID, jint val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetIntField(obj,fieldID,val);
		}

		virtual void SetLongField(jobject obj, jfieldID fieldID, jlong val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetLongField(obj,fieldID,val);
		}

		virtual void SetFloatField(jobject obj, jfieldID fieldID, jfloat val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetFloatField(obj,fieldID,val);
		}

		virtual void SetDoubleField(jobject obj, jfieldID fieldID, jdouble val)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetDoubleField(obj,fieldID,val);
		}

		virtual jfieldID GetStaticFieldID (jclass clazz, LPCTSTR name, LPCTSTR sig)
		{
			if ((NULL == clazz) || IsEmptyStr(name) || IsEmptyStr(sig) || (NULL == m_pEnv))
				return NULL;
			else
				return m_pEnv->GetStaticFieldID(clazz, name, sig);
		}

		virtual jfieldID GetClassStaticFieldID (LPCTSTR lpszClassPath, LPCTSTR name, LPCTSTR sig)
		{
			return GetStaticFieldID(FindClass(lpszClassPath), name, sig);
		}

		virtual jfieldID GetObjectStaticFieldID (jobject objRef, LPCTSTR name, LPCTSTR sig)
		{
			return GetStaticFieldID(GetObjectClass(objRef), name, sig);
		}

		virtual jobject GetStaticObjectField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticObjectField(clazz, fieldID);
		}

		virtual jboolean GetStaticBooleanField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticBooleanField(clazz, fieldID);
		}

		virtual jbyte GetStaticByteField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticByteField(clazz, fieldID);
		}

		virtual jchar GetStaticCharField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticCharField(clazz, fieldID);
		}

		virtual jshort GetStaticShortField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticShortField(clazz, fieldID);
		}

		virtual jint GetStaticIntField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticIntField(clazz, fieldID);
		}

		virtual jlong GetStaticLongField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticLongField(clazz, fieldID);
		}

		virtual jfloat GetStaticFloatField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticFloatField(clazz, fieldID);
		}

		virtual jdouble GetStaticDoubleField (jclass clazz, jfieldID fieldID)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStaticDoubleField(clazz, fieldID);
		}

		virtual void SetStaticObjectField (jclass clazz, jfieldID fieldID, jobject value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticObjectField(clazz, fieldID, value);
		}

		virtual void SetStaticBooleanField (jclass clazz, jfieldID fieldID, jboolean value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticBooleanField(clazz, fieldID, value);
		}

		virtual void SetStaticByteField (jclass clazz, jfieldID fieldID, jbyte value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticByteField(clazz, fieldID, value);
		}

		virtual void SetStaticCharField (jclass clazz, jfieldID fieldID, jchar value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticCharField(clazz, fieldID, value);
		}

		virtual void SetStaticShortField (jclass clazz, jfieldID fieldID, jshort value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticShortField(clazz, fieldID, value);
		}

		virtual void SetStaticIntField (jclass clazz, jfieldID fieldID, jint value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticIntField(clazz, fieldID, value);
		}

		virtual void SetStaticLongField (jclass clazz, jfieldID fieldID, jlong value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticLongField(clazz, fieldID, value);
		}

		virtual void SetStaticFloatField (jclass clazz, jfieldID fieldID, jfloat value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticFloatField(clazz, fieldID, value);
		}

		virtual void SetStaticDoubleField (jclass clazz, jfieldID fieldID, jdouble value)
		{
			if ((NULL == clazz) || (NULL == fieldID) || (NULL == m_pEnv))
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetStaticDoubleField(clazz, fieldID, value);
		}

		virtual jstring NewString (LPCJSTR unicode, jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewString(unicode,len);
		}

		virtual jsize GetStringLength (jstring str) const
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStringLength(str);
		}

		virtual LPCJSTR GetStringChars (jstring str, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetStringChars(str, &isCopy);
		}

		virtual void ReleaseStringChars (jstring str, LPCJSTR chars)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseStringChars(str,chars);
		}

		virtual jstring NewStringUTF (LPCTSTR utf)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewStringUTF(utf);
		}

		virtual jsize GetStringUTFLength (jstring str) const
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetStringUTFLength(str);
		}

		virtual LPCTSTR GetStringUTFChars (jstring str, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetStringUTFChars(str, &isCopy);
		}

		virtual void ReleaseStringUTFChars (jstring str, LPCTSTR chars)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseStringUTFChars(str, chars);
		}

		virtual jsize GetArrayLength (jarray array) const
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetArrayLength(array);
		}

		virtual jobject NewObjectV (jclass clazz, jmethodID methodID, va_list args)
		{
			if ((NULL == clazz) || (NULL == methodID) || (NULL == m_pEnv))
				return NULL;
			else
				return m_pEnv->NewObjectV(clazz, methodID, args);
		}

		virtual jobject NewObject (jclass clazz, jmethodID methodID, ...);

		virtual jmethodID GetDefaultClassCtor (jclass clazz)
		{
			return GetMethodID(clazz, _T("<init>"), _T("()V"));
		}

		virtual jobject CreateDefaultObject (jclass clazz)
		{
			jmethodID	jsCtor=GetDefaultClassCtor(clazz);
			if (NULL == jsCtor)
				return NULL;
			else
				return m_pEnv->NewObject(clazz, jsCtor);
		}

		virtual jobject CreateDefaultObject (LPCTSTR lpszClassName)
		{
			return CreateDefaultObject(FindClass(lpszClassName));
		}

		virtual jstring CreateStringObject ()
		{
			return (jstring) CreateDefaultObject(szJNIStringObjectClass);
		}

		virtual jobject CreateDefaultObject (jobject objRef)
		{
			return CreateDefaultObject(m_pEnv->GetObjectClass(objRef));
		}

		virtual jobjectArray NewObjectArray (jsize len, jclass clazz, jobject init)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewObjectArray(len,clazz,init);
		}

		virtual jobjectArray NewObjectArray (jsize len, LPCTSTR lpszClassName, jobject init)
		{
			return NewObjectArray(len, FindClass(lpszClassName), init);
		}

		virtual jobjectArray NewObjectArray (jsize len, LPCTSTR lpszClassName)
		{
			return NewObjectArray(len, lpszClassName, (jobject) NULL);
		}

		virtual jobjectArray NewStringArray (jsize len)
		{
			return NewObjectArray(len, FindClass(szJNIStringObjectClass), NULL);
		}

		virtual jobject GetObjectArrayElement (jobjectArray array, jsize index)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				return m_pEnv->GetObjectArrayElement(array,index);
		}

		virtual jboolean SetObjectArrayElement (jobjectArray array, jsize index, jobject val)
		{
			if (NULL == m_pEnv)
				return JNI_FALSE;

			m_pEnv->SetObjectArrayElement(array,index,val);
			return JNI_TRUE;
		}

		virtual jbooleanArray NewBooleanArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewBooleanArray(len);
		}

		virtual jbyteArray NewByteArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewByteArray(len);
		}

		virtual jcharArray NewCharArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewCharArray(len);
		}

		virtual jshortArray NewShortArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewShortArray(len);
		}

		virtual jintArray NewIntArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewIntArray(len);
		}

		virtual jlongArray NewLongArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewLongArray(len);
		}

		virtual jfloatArray NewFloatArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewFloatArray(len);
		}

		virtual jdoubleArray NewDoubleArray (jsize len)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->NewDoubleArray(len);
		}

		virtual LPJBOOL GetBooleanArrayElements (jbooleanArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetBooleanArrayElements(array, &isCopy);
		}

		virtual LPJBYTE GetByteArrayElements (jbyteArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetByteArrayElements(array, &isCopy);
		}

		virtual LPJSTR GetCharArrayElements (jcharArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetCharArrayElements(array, &isCopy);
		}

		virtual LPJSHORT GetShortArrayElements(jshortArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetShortArrayElements(array, &isCopy);
		}

		virtual LPJINT GetIntArrayElements (jintArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetIntArrayElements(array, &isCopy);
		}

		virtual LPJLONG GetLongArrayElements (jlongArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetLongArrayElements(array, &isCopy);
		}

		virtual LPJFLOAT GetFloatArrayElements (jfloatArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetFloatArrayElements(array, &isCopy);
		}

		virtual LPJDOUBLE GetDoubleArrayElements (jdoubleArray array, jboolean& isCopy)
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->GetDoubleArrayElements(array, &isCopy);
		}
		
		virtual void ReleaseBooleanArrayElements (jbooleanArray array, LPJBOOL elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseBooleanArrayElements(array,elems,mode);
		}

		virtual void ReleaseByteArrayElements (jbyteArray array, LPJBYTE elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseByteArrayElements(array,elems,mode);
		}

		virtual void ReleaseCharArrayElements (jcharArray array, LPJSTR elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseCharArrayElements(array,elems,mode);
		}

		virtual void ReleaseShortArrayElements (jshortArray array, LPJSHORT elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseShortArrayElements(array,elems,mode);
		}

		virtual void ReleaseIntArrayElements (jintArray array, LPJINT elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseIntArrayElements(array,elems,mode);
		}

		virtual void ReleaseLongArrayElements (jlongArray array, LPJLONG elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseLongArrayElements(array,elems,mode);
		}

		virtual void ReleaseFloatArrayElements (jfloatArray array, LPJFLOAT elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseFloatArrayElements(array,elems,mode);
		}

		virtual void ReleaseDoubleArrayElements (jdoubleArray array, LPJDOUBLE elems, jint mode=0)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ReleaseDoubleArrayElements(array,elems,mode);
		}
		
		virtual void GetBooleanArrayRegion (jbooleanArray array, jsize start, jsize len, LPJBOOL buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetBooleanArrayRegion(array,start,len,buf);
		}

		virtual void GetByteArrayRegion (jbyteArray array, jsize start, jsize len, LPJBYTE buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetByteArrayRegion(array,start,len,buf);
		}

		virtual void GetCharArrayRegion (jcharArray array, jsize start, jsize len, LPJSTR buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetCharArrayRegion(array,start,len,buf);
		}

		virtual void GetShortArrayRegion (jshortArray array, jsize start, jsize len, LPJSHORT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetShortArrayRegion(array,start,len,buf);
		}

		virtual void GetIntArrayRegion (jintArray array, jsize start, jsize len, LPJINT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetIntArrayRegion(array,start,len,buf);
		}

		virtual void GetLongArrayRegion (jlongArray array, jsize start, jsize len, LPJLONG buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetLongArrayRegion(array,start,len,buf);
		}

		virtual void GetFloatArrayRegion (jfloatArray array, jsize start, jsize len, LPJFLOAT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetFloatArrayRegion(array,start,len,buf);
		}

		virtual void GetDoubleArrayRegion (jdoubleArray array, jsize start, jsize len, LPJDOUBLE buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->GetDoubleArrayRegion(array,start,len,buf);
		}
		
		virtual void SetBooleanArrayRegion (jbooleanArray array, jsize start, jsize len, LPJBOOL buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetBooleanArrayRegion(array,start,len,buf);
		}

		virtual void SetByteArrayRegion (jbyteArray array, jsize start, jsize len, LPJBYTE buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetByteArrayRegion(array,start,len,buf);
		}

		virtual void SetCharArrayRegion (jcharArray array, jsize start, jsize len, LPJSTR buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetCharArrayRegion(array,start,len,buf);
		}

		virtual void SetShortArrayRegion (jshortArray array, jsize start, jsize len, LPJSHORT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetShortArrayRegion(array,start,len,buf);
		}

		virtual void SetIntArrayRegion (jintArray array, jsize start, jsize len, LPJINT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetIntArrayRegion(array,start,len,buf);
		}

		virtual void SetLongArrayRegion (jlongArray array, jsize start, jsize len, LPJLONG buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetLongArrayRegion(array,start,len,buf);
		}

		virtual void SetFloatArrayRegion (jfloatArray array, jsize start, jsize len, LPJFLOAT buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetFloatArrayRegion(array,start,len,buf);
		}

		virtual void SetDoubleArrayRegion (jdoubleArray array, jsize start, jsize len, LPJDOUBLE buf)
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->SetDoubleArrayRegion(array,start,len,buf);
		}

		virtual jint Throw (jthrowable obj)
		{
			if (NULL == m_pEnv)
				return ENOTCONNECTION;
			else if (NULL == obj)
				return EPARAM;
			else
				return m_pEnv->Throw(obj);
		}

		virtual jint Throw (LPCTSTR lpszClassName, LPCTSTR lpszMsg /* may be NULL */)
		{
			return ::ThrowJNIException(m_pEnv, lpszClassName, lpszMsg);
		}

		virtual jint ThrowClass (LPCTSTR lpszClassName)
		{
			return Throw(lpszClassName, NULL);
		}

		virtual jthrowable ExceptionOccurred () const
		{
			if (NULL == m_pEnv)
				return NULL;
			else
				return m_pEnv->ExceptionOccurred();
		}

		virtual void ExceptionDescribe ()
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ExceptionDescribe();
		}

		virtual void ExceptionClear ()
		{
			if (NULL == m_pEnv)
				throw CJNIAuxException(ENOTCONNECTION);
			else
				m_pEnv->ExceptionClear();
		}

		virtual ~CJNIEnvRef ()
		{ 
			m_pEnv = NULL;
		}
};	// end of JNI environment wrapper

/*--------------------------------------------------------------------------*/

class CJObjectRefGuard {
	protected:
		CJNIEnvRef	m_envRef;
		jobject&		m_obj;
		jboolean		m_fIsLocal;

		void SetMode (const jboolean fIsLocal)
		{
			m_fIsLocal = fIsLocal;
		}

	public:
		CJObjectRefGuard (LPJNIEnv env, jobject& obj, const jboolean fIsLocal=JNI_TRUE)
			: m_envRef(env), m_obj(obj), m_fIsLocal(fIsLocal)
		{
		}

		virtual void SetLocal ()
		{
			SetMode(JNI_TRUE);
		}

		virtual void SetGlobal ()
		{
			SetMode(JNI_FALSE);
		}

		virtual jboolean IsLocal () const
		{
			return m_fIsLocal;
		}

		virtual void Release ()
		{
			if (m_obj != NULL)
			{
				if (m_fIsLocal)
					m_envRef.DeleteLocalRef(m_obj);
				else
					m_envRef.DeleteGlobalRef(m_obj);
			}

			m_obj = NULL;
			m_fIsLocal = JNI_TRUE;
		}

		virtual jobject GetObjectRef () const
		{
			return m_obj;
		}

		virtual operator jobject() const
		{
			return m_obj;
		}
};	// end of object reference wrapper

/*--------------------------------------------------------------------------*/

class CJStringBase {
	protected:
		CJNIEnvRef	m_Env;
		jstring		m_jstr;
		jsize			m_jSize;
		jboolean		m_isCopy;

		union
		{
				LPCTSTR	m_lpszStr;
				LPCJSTR	m_jcStr;
		} m_pStr;

		CJStringBase (LPJNIEnv pEnv, jstring jstr=NULL)
			: m_Env(pEnv), m_jstr(jstr), m_jSize(0), m_isCopy(JNI_FALSE)
		{
			m_pStr.m_lpszStr = NULL;
			m_pStr.m_jcStr = NULL;
		}

		virtual ~CJStringBase () { }

	public:
		virtual jstring GetJavaString () const { return m_jstr; }

		virtual operator jstring() const { return m_jstr; }

		virtual jsize GetLength () const { return m_jSize; }

		virtual void Release () = 0;
};	// end of CJStringBase

/*--------------------------------------------------------------------------*/

class CJUTFString : public CJStringBase {
	private:
		LPCTSTR&	m_lpszStr;

	public:
		CJUTFString (LPJNIEnv pEnv, jstring jstr=NULL)
			: CJStringBase(pEnv), m_lpszStr(m_pStr.m_lpszStr)
		{
			*this = jstr;
		}

		virtual LPCTSTR GetNativeString () const
		{ 
			return m_lpszStr;
		}

		virtual operator LPCTSTR() const
		{ 
			return m_lpszStr;
		}

		virtual void Release ();

		virtual CJUTFString& operator= (jstring jstr);

		virtual ~CJUTFString () { Release(); }
};	// end of UTF string class

/*--------------------------------------------------------------------------*/

class CJString : public CJStringBase {
	private:
		LPCJSTR&	m_jcStr;

	public:
		CJString (LPJNIEnv pEnv, jstring jstr=NULL)
			: CJStringBase(pEnv), m_jcStr(m_pStr.m_jcStr)
		{
			*this = jstr;
		}

		virtual operator LPCJSTR() const { return m_jcStr; }

		virtual void Release ();
    
		virtual CJString& operator= (jstring jstr);

		virtual ~CJString () { Release(); }
};	// end of (normal) string class

/*--------------------------------------------------------------------------*/

class CPureJString {
	protected:
		CJNIEnvRef	m_env;
		jstring		m_jstr;
		jboolean		m_jAutoRelease;

	public:
		CPureJString (LPJNIEnv pEnv, LPCTSTR lpszVal=NULL, const jboolean fAutoRelease=JNI_TRUE)
			: m_env(pEnv), m_jstr(NULL), m_jAutoRelease(JNI_TRUE)
		{
			*this = lpszVal;

			SetAutoRelease(fAutoRelease);
		}

		// returns EOK if successful
		virtual jint SetValue (LPCTSTR lpszVal, const jboolean fAutoRelease=JNI_TRUE);

		// throws CJNIAuxException if cannot set value AND marks value as auto-release
		virtual CPureJString& operator= (LPCTSTR lpszVal)
		{
			jint	err=SetValue(lpszVal, JNI_TRUE);
			if (err != EOK)
				throw CJNIAuxException(err);

			return *this;
		}

		virtual jstring GetPureJString () const
		{
			return m_jstr;
		}

		virtual operator jstring() const
		{
			return m_jstr;
		}

		// CAVEAT EMPTOR !!!
		virtual void SetAutoRelease (const jboolean fAutoRelease)
		{
			m_jAutoRelease = fAutoRelease;
		}

		virtual jboolean IsAutoRelease () const
		{
			return m_jAutoRelease;
		}

		virtual void Release ()
		{
			if ((m_jstr != NULL) && (JNI_TRUE == m_jAutoRelease))
				m_env.DeleteLocalRef(m_jstr);

			m_jstr = NULL;
			m_jAutoRelease = JNI_TRUE;
		}

		virtual ~CPureJString ()
		{
			Release();
		}
};

/*--------------------------------------------------------------------------*/

class CJArrayBase {
	protected:
		CJNIEnvRef	m_Env;
		union {
			jarray			m_jArr;
			jintArray		m_jInts;
			jbyteArray		m_jBytes;
			jobjectArray	m_jObjs;
			jcharArray		m_jChars;
		}				m_jArr;
		union {
			LPJINT		m_cInts;
			LPJBYTE		m_cBytes;
			LPJSTR		m_cChars;
			LPVOID		m_cArr;
		}					m_cArr;
		jboolean		m_fIsCopy;
		jsize			m_arLen;

		CJArrayBase (LPJNIEnv pEnv)
			: m_Env(pEnv), m_arLen(0), m_fIsCopy(JNI_FALSE)
		{
			m_jArr.m_jArr = NULL;
			m_cArr.m_cArr = NULL;
		}

		virtual ~CJArrayBase () { }

	public:
		virtual jsize GetLength () const { return m_arLen; }

		virtual void Release () = 0;
};	// end of array(s) base class

/*--------------------------------------------------------------------------*/

class CJIntArray : public CJArrayBase {
	private:
		jintArray&	m_jInts;
		LPJINT&		m_cInts;

	public:
		CJIntArray (LPJNIEnv pEnv, jintArray jInts=NULL)
			: CJArrayBase(pEnv), m_jInts(m_jArr.m_jInts), m_cInts(m_cArr.m_cInts)
		{
			*this = jInts;
		}

		virtual void Release ();

		virtual CJIntArray& operator= (jintArray jInts);

		virtual LPJINT GetNativeIntArray () const
		{
			return m_cInts;
		}

		virtual jint& operator[] (const jint ndx)
		{
			if (ndx >= m_arLen)
				throw CJNIAuxException(EARRAYBOUNDS);
			return m_cInts[ndx];
		}

		virtual operator jintArray() const { return m_jInts; }

		virtual ~CJIntArray () { Release(); }
};	// end of INT array class

/*--------------------------------------------------------------------------*/

class CJByteArray : public CJArrayBase {
	private:
		jbyteArray&	m_jBytes;
		LPJBYTE&		m_cBytes;

	public:
		CJByteArray (LPJNIEnv pEnv, jbyteArray jBytes=NULL)
			: CJArrayBase(pEnv), m_jBytes(m_jArr.m_jBytes), m_cBytes(m_cArr.m_cBytes)
		{
			*this = jBytes;
		}

		virtual void Release ();

		virtual CJByteArray& operator= (jbyteArray jBytes);

		virtual LPJBYTE GetNativeByteArray () const
		{
			return m_cBytes;
		}

		virtual jbyte& operator[] (const jint ndx)
		{
			if (ndx >= m_arLen)
				throw CJNIAuxException(EARRAYBOUNDS);
			return m_cBytes[ndx];
		}

		virtual operator jbyteArray() const { return m_jBytes; }

		virtual void GetByteArrayRegion (jsize start, jsize len, LPJBYTE buf)
		{
			m_Env.GetByteArrayRegion(m_jBytes, start, len, buf);
		}

		virtual ~CJByteArray () { Release(); }
};	// end of INT array class

/*--------------------------------------------------------------------------*/

class CJCharArray : public CJArrayBase {
	private:
		jcharArray&	m_jChars;
		LPJSTR&		m_cChars;

	public:
		CJCharArray (LPJNIEnv pEnv, jcharArray jChars=NULL)
			: CJArrayBase(pEnv), m_jChars(m_jArr.m_jChars), m_cChars(m_cArr.m_cChars)
		{
			*this = jChars;
		}

		virtual void Release ();

		virtual CJCharArray& operator= (jcharArray jChars);

		virtual LPJSTR GetNativeCharArray () const
		{
			return m_cChars;
		}

		// copies specified region char array to specified buffer
		// NOTE: does not put a terminating '\0' - if non-ASCII character found, then error returned
		virtual jint copyASCIIChars (const jint startIndex /* inclusive */, const jint endIndex /* exclusive */, LPTSTR lpszDst, const jint dstLen);

		// copies specified size char array to specified buffer
		virtual jint copyASCIIChars (const jint srcLen, LPTSTR lpszDst, const jint dstLen)
		{
			return copyASCIIChars(0, srcLen, lpszDst, dstLen);
		}

		// copies ENTIRE char array to specified buffer
		virtual jint copyASCIIChars (LPTSTR lpszDst, const jint dstLen)
		{
			return copyASCIIChars(0, m_arLen, lpszDst, dstLen);
		}
		
		virtual jchar& operator[] (const jint ndx)
		{
			if (ndx >= m_arLen)
				throw CJNIAuxException(EARRAYBOUNDS);
			return m_cChars[ndx];
		}

		virtual operator jcharArray() const { return m_jChars; }

		virtual void GetCharArrayRegion (jsize start, jsize len, LPJSTR buf)
		{
			m_Env.GetCharArrayRegion(m_jChars, start, len, buf);
		}

		virtual ~CJCharArray () { Release(); }
};	// end of INT array class

/*--------------------------------------------------------------------------*/

class CJavaPureObjectBase {
	private:
		// disable copy constructor and assignment operator
		CJavaPureObjectBase (const CJavaPureObjectBase& );
		CJavaPureObjectBase& operator= (const CJavaPureObjectBase& );

	protected:
		CJNIEnvRef	m_envRef;
		jobject		m_objRef;
		jclass		m_objClass;

	public:
		CJavaPureObjectBase (LPJNIEnv pEnv, jobject objRef, jclass objClass=NULL)
			: m_envRef(pEnv), m_objRef(objRef), m_objClass(objClass)
		{
			if ((m_objRef != NULL) && (NULL == m_objClass))
				m_objClass = m_envRef.GetObjectClass(objRef);
		}

		CJavaPureObjectBase (LPJNIEnv pEnv, jobject objRef, LPCTSTR lpszClassName)
			: m_envRef(pEnv), m_objRef(objRef), m_objClass(NULL)
		{
			if (!IsEmptyStr(lpszClassName))
				m_objClass = m_envRef.FindClass(lpszClassName);
		}

		virtual CJNIEnvRef& GetEnvRef ()	const { return	(CJNIEnvRef &) m_envRef; }

		virtual jobject GetObjRef () const { return (jobject) m_objRef; }

		virtual jclass	 GetObjClass () const { return (jclass) m_objClass; }

		// returns non-NULL if successful
		virtual jmethodID GetMethodID (LPCTSTR lpszName, LPCTSTR lpszSig)
		{
			if (m_objClass != NULL)
				return m_envRef.GetMethodID(m_objClass, lpszName, lpszSig);
			else
				return NULL;
		}

		virtual jshort CallShortMethodV (jmethodID methodID, va_list args)
		{
			return m_envRef.CallShortMethodV(m_objRef, methodID, args);
		}

		virtual jshort CallShortMethod (jmethodID methodID, ...);

		virtual jint CallIntMethodV (jmethodID methodID, va_list args)
		{
			return m_envRef.CallIntMethodV(m_objRef, methodID, args);
		}

		virtual jint CallIntMethod (jmethodID methodID, ...);

		virtual jboolean CallBooleanMethodV (jmethodID methodID, va_list args)
		{
			return m_envRef.CallBooleanMethodV(m_objRef, methodID, args);
		}

		virtual jboolean CallBooleanMethod (jmethodID methodID, ...);

		virtual jchar CallCharMethodV (jmethodID methodID, va_list args)
		{
			return m_envRef.CallCharMethodV(m_objRef, methodID, args);
		}

		virtual jchar CallCharMethod (jmethodID methodID, ...);

		virtual void CallVoidMethodV (jmethodID methodID, va_list args)
		{
			m_envRef.CallVoidMethodV(m_objRef, methodID, args);
		}

		virtual void CallVoidMethod (jmethodID methodID, ...);

		virtual jobject CallObjectMethodV (jobject obj, jmethodID methodID, va_list args)
		{
			return m_envRef.CallObjectMethodV(m_objRef, methodID, args);
		}

		virtual jobject CallObjectMethod (jmethodID methodID, ...);

		virtual ~CJavaPureObjectBase ()
		{
		}
};

/*--------------------------------------------------------------------------*/

class IJFieldsManager {
	private:
		// disable copy constructor and assignment operator
		IJFieldsManager (const IJFieldsManager& );
		IJFieldsManager& operator= (const IJFieldsManager& );

	protected:
		const LPJNIEnv	m_env;
		CJNIEnvRef		m_envRef;
		jclass			m_clazz;

		IJFieldsManager (LPJNIEnv pEnv, jclass clazz)
			: m_env(pEnv)
			, m_envRef(pEnv)
			, m_clazz(clazz)
		{
		}

		IJFieldsManager (LPJNIEnv pEnv, LPCTSTR lpszClassPath)
			: m_env(pEnv)
			, m_envRef(pEnv)
			, m_clazz(pEnv->FindClass(lpszClassPath))
		{
		}

		virtual jint GetEnvFieldID (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean isStatic, jfieldID& fID);

	public:
		virtual jint GetObjectFieldID (LPCTSTR lpszName, LPCTSTR lpszSig, jfieldID& fID) = 0;

		virtual jint UpdateIntField (LPCTSTR lpszName, const jboolean fSetIt, jint& iVal) = 0;

		virtual jint GetIntField (LPCTSTR lpszName, jint& iVal)
		{
			return UpdateIntField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetIntField (LPCTSTR lpszName, const jint iVal)
		{
			jint	tiVal=iVal;
			return UpdateIntField(lpszName, JNI_TRUE, tiVal);
		}

		virtual jint UpdateByteField (LPCTSTR lpszName, const jboolean fSetIt, jbyte& iVal) = 0;

		virtual jint GetByteField (LPCTSTR lpszName, jbyte& iVal)
		{
			return UpdateByteField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetByteField (LPCTSTR lpszName, const jbyte bVal)
		{
			jbyte	tbVal=bVal;
			return UpdateByteField(lpszName, JNI_TRUE, tbVal);
		}

		virtual jint UpdateLongField (LPCTSTR lpszName, const jboolean fSetIt, jlong& iVal) = 0;

		virtual jint GetLongField (LPCTSTR lpszName, jlong& iVal)
		{
			return UpdateLongField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetLongField (LPCTSTR lpszName, const jlong iVal)
		{
			jlong	tiVal=iVal;
			return UpdateLongField(lpszName, JNI_TRUE, tiVal);
		}

		virtual jint UpdateBooleanField (LPCTSTR lpszName, const jboolean fSetIt, jboolean& iVal) = 0;

		virtual jint GetBooleanField (LPCTSTR lpszName, jboolean& iVal)
		{
			return UpdateBooleanField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetBooleanField (LPCTSTR lpszName, const jboolean iVal)
		{
			jboolean	tiVal=iVal;
			return UpdateBooleanField(lpszName, JNI_TRUE, tiVal);
		}

		virtual jint UpdateShortField (LPCTSTR lpszName, const jboolean fSetIt, jshort& iVal) = 0;

		virtual jint GetShortField (LPCTSTR lpszName, jshort& iVal)
		{
			return UpdateShortField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetShortField (LPCTSTR lpszName, const jshort iVal)
		{
			jshort	tiVal=iVal;
			return UpdateShortField(lpszName, JNI_TRUE, tiVal);
		}

		virtual jint UpdateCharField (LPCTSTR lpszName, const jboolean fSetIt, jchar& iVal) = 0;

		virtual jint GetCharField (LPCTSTR lpszName, jchar& iVal)
		{
			return UpdateCharField(lpszName, JNI_FALSE, iVal);
		}

		virtual jint SetCharField (LPCTSTR lpszName, const jchar iVal)
		{
			jchar	tiVal=iVal;
			return UpdateCharField(lpszName, JNI_TRUE, tiVal);
		}

		virtual jint UpdateObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean fSetIt, jobject& iVal) = 0;

		virtual jint SetObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jobject iVal)
		{
			jobject	tObj=iVal;
			return UpdateObjectField(lpszName, lpszSig, JNI_TRUE, tObj);
		}

		virtual jint GetObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, jobject& iVal)
		{
			return UpdateObjectField(lpszName, lpszSig, JNI_FALSE, iVal);
		}

		virtual jint GetStringField (LPCTSTR lpszName, LPTSTR lpszVal, const jint iMaxLen);

		virtual jint GetStringField (LPCTSTR lpszName, jstring& jsVal)
		{
			return UpdateObjectField(lpszName, szJNIStringFieldSig, JNI_FALSE, (jobject &) jsVal);
		}

		virtual jint SetStringField (LPCTSTR lpszName, jstring sVal)
		{
			jobject	jsVal=(jobject) sVal;
			return UpdateObjectField(lpszName, szJNIStringFieldSig, JNI_TRUE, jsVal);
		}

		virtual jint SetStringField (LPCTSTR lpszName, LPCTSTR lpszStr)
		{
			CPureJString	jsStr(m_envRef, lpszStr);
			return SetStringField(lpszName, jsStr.GetPureJString());
		}

		virtual ~IJFieldsManager ()
		{
		}
};

/*--------------------------------------------------------------------------*/

class CJFieldsManager : public IJFieldsManager {
	private:
		// disable copy constructor and assignment operator
		CJFieldsManager (const CJFieldsManager& );
		CJFieldsManager& operator= (const CJFieldsManager& );

	protected:
		jobject	m_objRef;

	public:
		// if NULL class, then object is queried for its class
		CJFieldsManager (LPJNIEnv pEnv, jobject objRef, jclass objClass=NULL)
			: IJFieldsManager(pEnv, ((NULL == objClass) ? pEnv->GetObjectClass(objRef) : objClass))
			, m_objRef(objRef)
		{
		}

		CJFieldsManager (LPJNIEnv pEnv, jobject objRef, LPCTSTR lpszClassName)
			: IJFieldsManager(pEnv, lpszClassName)
			, m_objRef(objRef)
		{
		}

		virtual jint GetObjectFieldID (LPCTSTR lpszName, LPCTSTR lpszSig, jfieldID& fID)
		{
			return GetEnvFieldID(lpszName, lpszSig, JNI_FALSE, fID);
		}

		virtual jint UpdateIntField (LPCTSTR lpszName, const jboolean fSetIt, jint& iVal);

		virtual jint UpdateByteField (LPCTSTR lpszName, const jboolean fSetIt, jbyte& iVal);

		virtual jint UpdateLongField (LPCTSTR lpszName, const jboolean fSetIt, jlong& iVal);

		virtual jint UpdateBooleanField (LPCTSTR lpszName, const jboolean fSetIt, jboolean& iVal);

		virtual jint UpdateShortField (LPCTSTR lpszName, const jboolean fSetIt, jshort& iVal);

		virtual jint UpdateCharField (LPCTSTR lpszName, const jboolean fSetIt, jchar& iVal);

		virtual jint UpdateObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean fSetIt, jobject& iVal);

		virtual ~CJFieldsManager ()
		{
		}
};

/*--------------------------------------------------------------------------*/

class CJStaticFieldsManager : public IJFieldsManager {
	private:
		// disable copy constructor and assignment operator
		CJStaticFieldsManager (const CJStaticFieldsManager& );
		CJStaticFieldsManager& operator= (const CJStaticFieldsManager& );

	public:
		CJStaticFieldsManager (LPJNIEnv pEnv, jobject objRef, const bool dummy /* just so we have different constructors */)
			: IJFieldsManager(pEnv, pEnv->GetObjectClass(objRef))
		{
		}

		CJStaticFieldsManager (LPJNIEnv pEnv, jclass clazz)
			: IJFieldsManager(pEnv, clazz)
		{
		}

		CJStaticFieldsManager (LPJNIEnv pEnv, LPCTSTR lpszClassPath)
			: IJFieldsManager(pEnv, lpszClassPath)
		{
		}

		virtual jint GetObjectFieldID (LPCTSTR lpszName, LPCTSTR lpszSig, jfieldID& fID)
		{
			return GetEnvFieldID(lpszName, lpszSig, JNI_TRUE, fID);
		}

		virtual jint UpdateIntField (LPCTSTR lpszName, const jboolean fSetIt, jint& iVal);

		virtual jint UpdateByteField (LPCTSTR lpszName, const jboolean fSetIt, jbyte& iVal);

		virtual jint UpdateLongField (LPCTSTR lpszName, const jboolean fSetIt, jlong& iVal);

		virtual jint UpdateBooleanField (LPCTSTR lpszName, const jboolean fSetIt, jboolean& iVal);

		virtual jint UpdateShortField (LPCTSTR lpszName, const jboolean fSetIt, jshort& iVal);

		virtual jint UpdateCharField (LPCTSTR lpszName, const jboolean fSetIt, jchar& iVal);

		virtual jint UpdateObjectField (LPCTSTR lpszName, LPCTSTR lpszSig, const jboolean fSetIt, jobject& iVal);

		virtual ~CJStaticFieldsManager ()
		{
		}
};

/*--------------------------------------------------------------------------*/

inline time_t Java2NativeTimeVal (const jlong jsTime)
{
	return ((time_t) (jsTime / JAVA_TIMETICKS_PER_SECOND));
}

inline jlong Native2JavaTimeVal (const time_t tmVal)
{
	return (((jlong) tmVal) * JAVA_TIMETICKS_PER_SECOND);
}

extern jint Java2NativeTmVal (const jlong jsTime, struct tm& dtVal);

// Note: returns 0 if error
inline jlong Native2JavaTmVal (const struct tm& tmVal)
{
	return Native2JavaTimeVal(mktime((struct tm *) &tmVal));
}

/*--------------------------------------------------------------------------*/

class CJavaCalendarManager : public CJFieldsManager {
	private:
		CJavaCalendarManager (const CJavaCalendarManager& );
		CJavaCalendarManager& operator= (const CJavaCalendarManager& );

	public:
		CJavaCalendarManager (LPJNIEnv pEnv, jobject objRef, jclass objClass=NULL)
			: CJFieldsManager(pEnv, objRef, objClass)
		{
		}

		CJavaCalendarManager (LPJNIEnv pEnv, jobject objRef, LPCTSTR lpszClassName)
			: CJFieldsManager(pEnv, objRef, lpszClassName)
		{
		}

		typedef enum {
			CAL_DAY_OF_MONTH=0,
			CAL_MONTH,
			CAL_YEAR,
			CAL_HOUR_OF_DAY,
			CAL_MINUTE,
			CAL_SECOND,
			CAL_ZONE_OFFSET,
			CAL_BAD_FIELD
		} JAVACALENDARFIELDCASE;

		static jboolean IsBadCalendarField (const JAVACALENDARFIELDCASE efCase)
		{
			if (((unsigned) efCase) >= ((unsigned) CAL_BAD_FIELD))
				return JNI_TRUE;
			else
				return JNI_FALSE;
		}

		virtual jint SetCalendarField (const JAVACALENDARFIELDCASE efCase, const jint iVal)
		{
			jint	tVal=iVal;
			return UpdateCalendarField(efCase, JNI_TRUE, tVal);
		}

		virtual jint GetCalendarField (const JAVACALENDARFIELDCASE efCase, jint& iVal)
		{
			return UpdateCalendarField(efCase, JNI_FALSE, iVal);
		}

		virtual jint SetDate (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear);
		virtual jint SetTime (const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond);

		virtual jint SetDateTime (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear,
										  const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond);

		virtual jint SetDateTime (const struct tm& dtVal)
		{
			return SetDateTime((jint) dtVal.tm_mday, (jint) (dtVal.tm_mon+1), (jint) (dtVal.tm_year+1900),
									 (jint) dtVal.tm_hour, (jint) dtVal.tm_min, (jint) dtVal.tm_sec);
		}

		virtual jint SetDateTime (const time_t tVal);

		// Note: this should be the time-zone in seconds (!) - it will be
		//			converted to mili-sec(s) internally for Java compatibility
		virtual jint SetTimeZone (const int tmZoneSec=(-1) /* use local host default */);

		virtual jint SetInfo (const struct tm& dtVal, const int tmZoneSec=(-1) /* use local host default default */);

		// Reminder: tm_mon (Jan=0), tm_year (years since 1900)
		virtual jint GetInfo (struct tm& dtVal, int& tmZoneSec);

#ifdef _WIN32
		virtual jint SetDateTime (const SYSTEMTIME& dtVal)
		{
			return SetDateTime((jint) dtVal.wDay, (jint) dtVal.wMonth, (jint) dtVal.wYear,
									 (jint) dtVal.wHour, (jint) dtVal.wMinute, (jint) dtVal.wSecond);
		}

		virtual jint SetInfo (const SYSTEMTIME& dtVal, const int tmZoneSec=(-1) /* use local host default default */);

		virtual jint GetInfo (SYSTEMTIME& dtVal, int& tmZoneSec);
#endif	/* _WIN32 */

		virtual jint SetInfo (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear,
									 const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond,
									 const int tmZoneSec=(-1) /* use local host default default */);

		virtual ~CJavaCalendarManager ()
		{
		}

	protected:
		virtual jint UpdateCalendarField (const JAVACALENDARFIELDCASE efCase, const jboolean fSetIt, jint& iVal);
};

/*--------------------------------------------------------------------------*/

#if FALSE
// Enumeration class signature
public interface java.util.Enumeration
    /* ACC_SUPER bit NOT set */
{
    public abstract boolean hasMoreElements();
        /*   ()Z   */
    public abstract java.lang.Object nextElement();
        /*   ()Ljava/lang/Object;   */
}
#endif

// handles an Enumeration class object
class CJavaEnumerationManager : public CJavaPureObjectBase {
	private:
		CJavaEnumerationManager (const CJavaEnumerationManager& );
		CJavaEnumerationManager& operator= (const CJavaEnumerationManager& );

	public:
		CJavaEnumerationManager (LPJNIEnv pEnv, jobject objRef, jclass objClass=NULL)
			: CJavaPureObjectBase(pEnv, objRef, objClass)
		{
		}

		CJavaEnumerationManager (LPJNIEnv pEnv, jobject objRef, LPCTSTR lpszClassName)
			: CJavaPureObjectBase(pEnv, objRef, lpszClassName)
		{
		}

		virtual jboolean HasMoreElements ()
		{
			jmethodID	mID=GetMethodID(_T("hasMoreElements"), _T("()Z"));
			if (NULL == mID)
				return JNI_FALSE;

			return CallBooleanMethod(mID);
		}

		virtual jobject NextElement ()
		{
			jmethodID	mID=GetMethodID(_T("nextElement"), _T("()Ljava/lang/Object;"));
			if (NULL == mID)
				return NULL;

			return CallObjectMethod(mID);
		}

		virtual ~CJavaEnumerationManager ()
		{
		}
};

/*--------------------------------------------------------------------------*/

#endif	/* of __cplusplus */

#endif	/* of _JNIAUX_H_ */
