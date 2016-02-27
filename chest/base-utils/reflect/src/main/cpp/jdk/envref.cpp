#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

jobject CJNIEnvRef::CallObjectMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jobject result=CallObjectMethodV(obj, methodID, args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jboolean CJNIEnvRef::CallBooleanMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jboolean result=CallBooleanMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jbyte CJNIEnvRef::CallByteMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jbyte	result=CallByteMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jchar CJNIEnvRef::CallCharMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jchar	result=CallCharMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jshort CJNIEnvRef::CallShortMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jshort	result=CallShortMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jint CJNIEnvRef::CallIntMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jint	result=CallIntMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jlong CJNIEnvRef::CallLongMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jlong	result=CallLongMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jfloat CJNIEnvRef::CallFloatMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jfloat	result=CallFloatMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

jdouble CJNIEnvRef::CallDoubleMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	jdouble	result=CallDoubleMethodV(obj,methodID,args);
	va_end(args);

	return result;
}

/*--------------------------------------------------------------------------*/

void CJNIEnvRef::CallVoidMethod (jobject obj, jmethodID methodID, ...)
{
	va_list args;
	va_start(args,methodID);
	CallVoidMethodV(obj, methodID, args);
	va_end(args);
}

/*--------------------------------------------------------------------------*/

jobject CJNIEnvRef::NewObject (jclass clazz, jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jobject	retObj=NewObjectV(clazz, methodID, ap);
	va_end(ap);

	return retObj;
}

//////////////////////////////////////////////////////////////////////////////

jshort CJavaPureObjectBase::CallShortMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jshort	result=m_envRef.CallShortMethodV(m_objRef, methodID, ap);
	va_end(ap);

	return result;
}

/*--------------------------------------------------------------------------*/

jint CJavaPureObjectBase::CallIntMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jint	result=m_envRef.CallIntMethodV(m_objRef, methodID, ap);
	va_end(ap);

	return result;
}

/*--------------------------------------------------------------------------*/

jboolean CJavaPureObjectBase::CallBooleanMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jboolean	result=m_envRef.CallBooleanMethodV(m_objRef, methodID, ap);
	va_end(ap);

	return result;
}

/*--------------------------------------------------------------------------*/

jchar CJavaPureObjectBase::CallCharMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jchar	result=m_envRef.CallCharMethodV(m_objRef, methodID, ap);
	va_end(ap);

	return result;
}

/*--------------------------------------------------------------------------*/

jobject CJavaPureObjectBase::CallObjectMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	jobject	result=m_envRef.CallObjectMethodV(m_objRef, methodID, ap);
	va_end(ap);

	return result;
}

/*--------------------------------------------------------------------------*/

void CJavaPureObjectBase::CallVoidMethod (jmethodID methodID, ...)
{
	va_list	ap;
	va_start(ap, methodID);
	m_envRef.CallObjectMethodV(m_objRef, methodID, ap);
	va_end(ap);
}

/*--------------------------------------------------------------------------*/
