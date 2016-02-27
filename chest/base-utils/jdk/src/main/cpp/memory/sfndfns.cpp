#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/

size_t find_sorted_byte (const BYTE buf[], BYTE value, size_t count)
{
	size_t l=0,h=count,m;

	if (count == 0)
		return(0);
	
	for ( ; ; )
	{
		m = ((l+h) >> 1);
		if (buf[m] == value)
			return(m);
		
		if (buf[m] > value)
			h = m;
		else
			l = m;

		/*
		 *    The end condition differs according to array length parity - the
		 * "l"(ow) and "h"(igh) marks either meet or stop next to each other
		 */

		if (l == (h-1))
		{
			if (buf[l] == value)
				return(l);
			if (buf[l] < value)
				return(h);
			else
				return(l);
		}

		if (l == h)
			return(l);
	}
}

/*---------------------------------------------------------------------------*/

size_t find_sorted_word (const WORD buf[], WORD value, size_t count)
{
	size_t l=0,h=count,m;

	if (count == 0)
		return(0);
	
	for ( ; ; )
	{
		m = ((l+h) >> 1);
		if (buf[m] == value)
			return(m);
		
		if (buf[m] > value)
			h = m;
		else
			l = m;

		/*
		 *    The end condition differs according to array length parity - the
		 * "l"(ow) and "h"(igh) marks either meet or stop next to each other
		 */

		if (l == (h-1))
		{
			if (buf[l] == value)
				return(l);
			if (buf[l] < value)
				return(h);
			else
				return(l);
		}

		if (l == h)
			return(l);
	}
}

/*---------------------------------------------------------------------------*/

size_t find_sorted_dword (const DWORD buf[], DWORD value, size_t count)
{
	size_t l=0,h=count,m;

	if (count == 0)
		return(0);
	
	for ( ; ; )
	{
		m = ((l+h) >> 1);
		if (buf[m] == value)
			return(m);
		
		if (buf[m] > value)
			h = m;
		else
			l = m;

		/*
		 *    The end condition differs according to array length parity - the
		 * "l"(ow) and "h"(igh) marks either meet or stop next to each other
		 */

		if (l == (h-1))
		{
			if (buf[l] == value)
				return(l);
			if (buf[l] < value)
				return(h);
			else
				return(l);
		}

		if (l == h)
			return(l);
	}
}

/*---------------------------------------------------------------------------*/
