#include <mfc/general.h>

/*--------------------------------------------------------------------------*/

void DateTimePickersToSystemTime (const CDateTimeCtrl& srcDate, const CDateTimeCtrl& srcTime, SYSTEMTIME& sysTime)
{
	srcDate.GetTime(&sysTime);

	SYSTEMTIME	tmVal={ 0 };
	srcTime.GetTime(&tmVal);

	sysTime.wHour = tmVal.wHour;
	sysTime.wMinute = tmVal.wMinute;
	sysTime.wSecond = tmVal.wSecond;
	sysTime.wMilliseconds = tmVal.wMilliseconds;
}

/*--------------------------------------------------------------------------*/
