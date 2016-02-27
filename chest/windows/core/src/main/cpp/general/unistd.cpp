#include <unistd.h>

/*--------------------------------------------------------------------------*/

int ftruncate (int fDes, off_t fLen)
{
	return _chsize(fDes, fLen);
}

/*--------------------------------------------------------------------------*/

int truncate (const char *pszFPath, off_t fLen)
{
	int	fDes=_open(pszFPath, _O_BINARY | _O_RDWR, 0), tRes=(-1);
	if ((-1) == fDes)
		return (-1);

	tRes = ftruncate(fDes, fLen);
	_close(fDes);

	return tRes;
}

/*--------------------------------------------------------------------------*/
