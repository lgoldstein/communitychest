#include <jdk/jniaux.h>

//////////////////////////////////////////////////////////////////////////////

jint Java2NativeTmVal (const jlong jsTime, struct tm& dtVal)
{
	memset(&dtVal, 0, (sizeof dtVal));

	const time_t		tmVal=Java2NativeTimeVal(jsTime);
	const struct tm	*pTM=localtime(&tmVal);
	if (NULL == pTM)
		return EUNKNOWNEXIT;

	dtVal = *pTM;
	return EOK;
}

//////////////////////////////////////////////////////////////////////////////

jint CJavaCalendarManager::UpdateCalendarField (const JAVACALENDARFIELDCASE efCase, const jboolean fSetIt, jint& iVal)
{
	if (IsBadCalendarField(efCase))
		return EFTYPE;

	// Note !!! do not change order unless the enumeration is updated as well
	static const LPCTSTR szCalFieldsNames[]={
		_T("DAY_OF_MONTH"),
		_T("MONTH"),
		_T("YEAR"),
		_T("HOUR_OF_DAY"),
		_T("MINUTE"),
		_T("SECOND"),
		_T("ZONE_OFFSET"),
		NULL
	};
	LPCTSTR	lpszFieldName=szCalFieldsNames[efCase];
	if (IsEmptyStr(lpszFieldName))
		return EEMPTYENTRY;

	jint	iFieldVal=(jint) (-1), exc=GetIntField(lpszFieldName, iFieldVal);
	if (exc != EOK)
		return exc;

	if (fSetIt)
	{
		/*   (II)V   */
		// public final void set(int, int);
		jmethodID	setID=m_envRef.GetMethodID(m_clazz, _T("set"), _T("(II)V"));
		if (NULL == setID)
			return ENOSTART;

		m_envRef.CallVoidMethod(m_objRef, setID, iFieldVal, iVal);
	}
	else
	{
		/*   (I)I   */
		// public final int get(int);
		jmethodID	getID=m_envRef.GetMethodID(m_clazz, _T("get"), _T("(I)I"));
		if (NULL == getID)
			return ENOSTART;

		iVal = m_envRef.CallIntMethod(m_objRef, getID, iFieldVal);
	}

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetDate (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear)
{
	/*   (III)V   */
	// public final void set(int, int, int);
	jmethodID	setID=m_envRef.GetMethodID(m_clazz, _T("set"), _T("(III)V"));
	if (NULL == setID)
		return ENOSTART;

	m_envRef.CallVoidMethod(m_objRef, setID, dYear, dMonth, dDay);
	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetTime (const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond)
{
	jint	exc=EOK;

	if ((exc=SetCalendarField(CAL_HOUR_OF_DAY, tHour)) != EOK)
		return exc;
	if ((exc=SetCalendarField(CAL_MINUTE, tMinute)) != EOK)
		return exc;
	if ((exc=SetCalendarField(CAL_SECOND, tMinute)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetDateTime (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear,
													 const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond)
{
	/*   (IIIII)V   */
	// public final void set(int year, int month, int date, int hour, int minute, int second);
	jmethodID	setID=m_envRef.GetMethodID(m_clazz, _T("set"), _T("(IIIIII)V"));
	if (NULL == setID)
		return ENOSTART;

	m_envRef.CallVoidMethod(m_objRef, setID, dYear, dMonth, dDay, tHour, tMinute, tSecond);
	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetDateTime (const time_t tVal)
{
	const struct tm	*ptVal=::localtime(&tVal);
	if (NULL == ptVal)
		return EUNKNOWNEXIT;
	else
		return SetDateTime(*ptVal);
}

/*--------------------------------------------------------------------------*/

// Note: this should be the time-zone in seconds (!) - it will be
//			converted to mili-sec(s) internally for Java compatibility
jint CJavaCalendarManager::SetTimeZone (const int tmZoneSec /* =(-1) use local host default */)
{
	const int	tmzVal=(((-1) == tmZoneSec) ? _timezone : tmZoneSec);

	/*   (ILjava/lang/String;)V   */
	//	public java.util.SimpleTimeZone(int,java.lang.String);
	jclass	tzClass=m_envRef.FindClass(szJNISimpleTimeZoneObjectClass);
	if (NULL == tzClass)
		return EIOUNCLASS;

	jmethodID	tzCtor=m_envRef.GetMethodID(tzClass, _T("<init>"), _T("(ILjava/lang/String;)V"));
	if (NULL == tzCtor)
		return ENOLOADERMEM;

	CPureJString	jsTMZ(m_envRef, _T("GMT"));
	jobject			tzObj=m_envRef.NewObject(tzClass, tzCtor, (jint) (tmzVal * JAVA_TIMETICKS_PER_SECOND), jsTMZ.GetPureJString());
	if (NULL == tzObj)
		return ETRANSMISSION;
	CJObjectRefGuard	tzg(m_envRef, tzObj);

	/*   (Ljava/util/TimeZone;)V   */
	//	public void setTimeZone(java.util.TimeZone);
	jmethodID	tzSetID=m_envRef.GetMethodID(m_clazz, _T("setTimeZone"), _T("(Ljava/util/TimeZone;)V"));
	if (NULL == tzSetID)
		return ENOLOADERMEM;

	m_envRef.CallVoidMethod(m_objRef, tzSetID, tzObj);
	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetInfo (const struct tm& dtVal, const int tmZoneSec /* =(-1) use local host default default */)
{
	jint	exc=SetDateTime(dtVal);
	if (exc != EOK)
		return exc;

	if ((exc=SetTimeZone(tmZoneSec)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

jint CJavaCalendarManager::SetInfo (const jint dDay, const jint dMonth /* Jan.=1 */, const jint dYear,
												const jint tHour /* 00-23 */, const jint tMinute, const jint tSecond,
												const int tmZoneSec /* =(-1) use local host default default */)
{
	jint	exc=SetDateTime(dDay, dMonth, dYear, tHour, tMinute, tSecond);
	if (exc != EOK)
		return exc;

	if ((exc=SetTimeZone(tmZoneSec)) != EOK)
		return exc;

	return EOK;
}

/*--------------------------------------------------------------------------*/

static jint getIntCalField (CJavaCalendarManager& jcm, const CJavaCalendarManager::JAVACALENDARFIELDCASE efCase, int& iVal)
{
	jint	jsVal=(-1), exc=jcm.GetCalendarField(efCase, jsVal);
	if (exc != EOK)
		return exc;

	iVal = (int) jsVal;
	return EOK;
}

jint CJavaCalendarManager::GetInfo (struct tm& dtVal, int& tmZoneSec)
{
	jint	exc=EOK;

	::memset(&dtVal, 0, (sizeof dtVal));
	tmZoneSec = (-1);

	if ((exc=::getIntCalField(*this, CAL_YEAR, dtVal.tm_year)) != EOK)
		return exc;
	if ((exc=::getIntCalField(*this, CAL_MONTH, dtVal.tm_mon)) != EOK)
		return exc;
	if ((exc=::getIntCalField(*this, CAL_DAY_OF_MONTH, dtVal.tm_mday)) != EOK)
		return exc;
	if ((exc=::getIntCalField(*this, CAL_HOUR_OF_DAY, dtVal.tm_hour)) != EOK)
		return exc;
	if ((exc=::getIntCalField(*this, CAL_MINUTE, dtVal.tm_min)) != EOK)
		return exc;
	if ((exc=::getIntCalField(*this, CAL_SECOND, dtVal.tm_sec)) != EOK)
		return exc;

	// adjust returned values to match TM structure conventions
	dtVal.tm_year -= 1900;
	dtVal.tm_mon--;

	jint	jsTzOffset=(-1);
	if ((exc=GetCalendarField(CAL_ZONE_OFFSET, jsTzOffset)) != EOK)
		return exc;

	tmZoneSec = (int) (jsTzOffset / JAVA_TIMETICKS_PER_SECOND);
	return EOK;
}

/*--------------------------------------------------------------------------*/

#ifdef _WIN32
jint CJavaCalendarManager::SetInfo (const SYSTEMTIME& dtVal, const int tmZoneSec /* (-1) = use local host default default */)
{
	jint	exc=SetDateTime(dtVal);
	if (exc != EOK)
		return exc;

	if ((exc=SetTimeZone(tmZoneSec)) != EOK)
		return exc;

	return EOK;
}

static jint getSysCalField (CJavaCalendarManager& jcm, const CJavaCalendarManager::JAVACALENDARFIELDCASE efCase, WORD& wVal)
{
	jint	jsVal=(-1), exc=jcm.GetCalendarField(efCase, jsVal);
	if (exc != EOK)
		return exc;

	wVal = (WORD) jsVal;
	return EOK;
}

jint CJavaCalendarManager::GetInfo (SYSTEMTIME& dtVal, int& tmZoneSec)
{
	jint	exc=EOK;

	::memset(&dtVal, 0, (sizeof dtVal));
	tmZoneSec = (-1);

	if ((exc=::getSysCalField(*this, CAL_YEAR, dtVal.wYear)) != EOK)
		return exc;
	if ((exc=::getSysCalField(*this, CAL_MONTH, dtVal.wMonth)) != EOK)
		return exc;
	if ((exc=::getSysCalField(*this, CAL_DAY_OF_MONTH, dtVal.wDay)) != EOK)
		return exc;
	if ((exc=::getSysCalField(*this, CAL_HOUR_OF_DAY, dtVal.wHour)) != EOK)
		return exc;
	if ((exc=::getSysCalField(*this, CAL_MINUTE, dtVal.wMinute)) != EOK)
		return exc;
	if ((exc=::getSysCalField(*this, CAL_SECOND, dtVal.wSecond)) != EOK)
		return exc;

	jint	jsTzOffset=(-1);
	if ((exc=GetCalendarField(CAL_ZONE_OFFSET, jsTzOffset)) != EOK)
		return exc;

	tmZoneSec = (int) (jsTzOffset / JAVA_TIMETICKS_PER_SECOND);
	return EOK;
}
#endif	/* _WIN32 */

/*--------------------------------------------------------------------------*/
