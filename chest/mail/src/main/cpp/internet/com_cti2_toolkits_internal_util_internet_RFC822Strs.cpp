#include <jdk/util32jni.h>

#include "com_cti2_toolkits_internal_util_internet_RFC822Strs.h"

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    ParseRFC822PropsList
 * Signature: (Ljava/lang/String;Lcom/cti2/toolkits/internal/util/general/CStr2StrMapper;)I
 */
JNIEXPORT jint JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_ParseRFC822PropsList
  (JNIEnv *env, jclass classRef, jstring strInput, jobject str2strMapRef)
{
	CJNIEnvRef	envRef(env);
	CJUTFString	istrRef(envRef, strInput);
	LPCTSTR		lpszInputString=istrRef.GetNativeString();

	CJavaStr2StrMapper	js2s(envRef, str2strMapRef);
	const jint				iCapacity=js2s.GetCapacity();
	if (iCapacity <= 0)
		return EEMPTYENTRY;

	CStr2StrMapper	propsMap;
	EXC_TYPE			exc=propsMap.InitMap((UINT32) iCapacity, js2s.IsCaseSensitive());
	if (exc != EOK)
		return exc;

	if ((exc=ParseRFC822PropsList(lpszInputString, propsMap)) != EOK)
		return exc;

	if ((exc=js2s.Merge(propsMap)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    ValidateRFC822EmailAddr
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_ValidateRFC822EmailAddr
  (JNIEnv *env, jclass classRef, jstring jsAddr)
{
	CJNIEnvRef	envRef(env);
	CJUTFString	istrRef(envRef, jsAddr);
	LPCTSTR		lpszInputString=istrRef.GetNativeString();
	return ValidateRFC822Email(lpszInputString);
}

/*--------------------------------------------------------------------------*/

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    toRFC822EncString
 * Signature: (B)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_toRFC822EncString
  (JNIEnv *env, jclass clazz, jbyte bEncVal)
{
	LPCTSTR	lpszVal=RFC822EncodingCase2Str((RFC822ENCCASE) bEncVal);
	if (IsEmptyStr(lpszVal))
		return NULL;

	CPureJString	jsVal(env, lpszVal, JNI_FALSE);
	return jsVal.GetPureJString();
}

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    toRFC822Encoding
 * Signature: (Ljava/lang/String;)B
 */
JNIEXPORT jbyte JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_toRFC822Encoding
  (JNIEnv *env, jclass clazz, jstring jsEncVal)
{
	CJNIEnvRef	envRef(env);
	CJUTFString	istrRef(envRef, jsEncVal);
	LPCTSTR		lpszInputString=istrRef.GetNativeString();
	return RFC822EncodingStr2Case(lpszInputString);
}

/*--------------------------------------------------------------------------*/

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    BuildRFC822AddrPair
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_BuildRFC822AddrPair
  (JNIEnv *env, jclass clazz, jstring jsName, jstring jsAddr)
{
	CJUTFString			utfName(env, jsName), utfAddr(env, jsAddr);
	LPCTSTR				lpszName=utfName.GetNativeString(), lpszAddr=utfAddr.GetNativeString();
	CIncStrlBuilder	aPair(MAX_ADDRPAIR_EMAIL_LEN, MAX_USER_PART_LEN);
	jint					hr=BuildRFC822AddrPair(lpszName, lpszAddr, aPair);
	if (hr != S_OK)
	{
		hr = ThrowJNIUtil32ErrorsObject(env, hr);
		return NULL;
	}

	LPCTSTR			lpszAddrPair=aPair;
	CPureJString	jsAPair(env, lpszAddrPair, JNI_FALSE);
	return jsAPair.GetPureJString();
}

/*--------------------------------------------------------------------------*/

static void setJNIAddrPairComponent (CJNIEnvRef& envRef, jobjectArray jsComps, jsize jdx, LPCTSTR lpszCompVal, const UINT32 ulCompLen)
{
	if (0 == ulCompLen)
		return;

	const TCHAR		tch=lpszCompVal[ulCompLen];
	*((LPTSTR) (lpszCompVal + ulCompLen)) = _T('\0');
	CPureJString	jsCompVal(envRef, lpszCompVal);
	envRef.SetObjectArrayElement(jsComps, jdx, (jobject) jsCompVal.GetPureJString());
	jsCompVal.Release();
	*((LPTSTR) (lpszCompVal + ulCompLen)) = tch;
}

static jint jniDecodeRFC822AddrPair (JNIEnv *env, jstring jsAPair, jobjectArray& jsComps)
{
	CJUTFString	utfAPair(env, jsAPair);
	LPCTSTR		lpszAPair=utfAPair.GetNativeString(), lpszName=NULL, lpszAddr=NULL;
	UINT32		ulNameLen=0, ulAddrLen=0;
	jint			exc=DecodeRFC822AddrPair(lpszAPair, lpszName, ulNameLen, lpszAddr, ulAddrLen);
	if (exc != EOK)
		return exc;

	CJNIEnvRef	envRef(env);
	if (NULL == (jsComps=envRef.NewStringArray(2)))
		return EMEM;

	setJNIAddrPairComponent(envRef, jsComps, 0, lpszName, ulNameLen);		
	setJNIAddrPairComponent(envRef, jsComps, 1, lpszAddr, ulAddrLen);		

	return EOK;
}

/*
 * Class:     com_cti2_toolkits_internal_util_internet_RFC822Strs
 * Method:    DecodeRFC822AddrPair
 * Signature: (Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_cti2_toolkits_internal_util_internet_RFC822Strs_DecodeRFC822AddrPair
  (JNIEnv *env, jclass clazz, jstring jsAPair)
{
	jobjectArray	jsComps=NULL;
	jint				hr=jniDecodeRFC822AddrPair(env, jsAPair, jsComps);
	if (hr != S_OK)
	{
		hr = ThrowJNIUtil32ErrorsObject(env, hr);
		return NULL;
	}

	return jsComps;
}

/*--------------------------------------------------------------------------*/
