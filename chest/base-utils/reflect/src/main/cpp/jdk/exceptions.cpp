#include <jdk/jniaux.h>

/*--------------------------------------------------------------------------*/

jint ThrowJNIException (JNIEnv *env, LPCTSTR lpszClassName, LPCTSTR lpszMsg /* may be NULL */)
{
	if ((NULL == env) || IsEmptyStr(lpszClassName))
		return EPARAM;

	CJNIEnvRef	envRef(env);
	jclass		excClass=envRef.FindClass(lpszClassName);
	if (NULL == excClass)
		return EIOUNCLASS;

	jthrowable		excObj=NULL;
	if (IsEmptyStr(lpszMsg))
	{
		jmethodID	excCtor=envRef.GetMethodID(excClass, _T("<init>"), _T("()V"));
		if (NULL == excCtor)
			return ENOLOADERMEM;
		if (NULL == (excObj=(jthrowable) envRef.NewObject(excClass, excCtor)))
			return ETRANSMISSION;
	}
	else
	{
		jmethodID	excCtor=envRef.GetMethodID(excClass, _T("<init>"), _T("(Ljava/lang/String;)V"));
		if (NULL == excCtor)
			return ENOLOADERMEM;

		CPureJString	jsMsg(envRef, lpszMsg);
		if (NULL == (excObj=(jthrowable) envRef.NewObject(excClass, excCtor, jsMsg.GetPureJString())))
			return ETRANSMISSION;
	}

	jint	err=envRef.Throw(excObj);
	if (err != EOK)
	{
		envRef.DeleteLocalRef(excObj);
		return err;
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/
