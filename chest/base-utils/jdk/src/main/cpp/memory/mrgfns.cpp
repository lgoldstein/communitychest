#include <_types.h>
#include <util/memory.h>

/*---------------------------------------------------------------------------*/
/*								  merge_bytes/words/dwords
 *								  ------------------------
 *		Merge 2 sorted array into a new (sorted) array. Arrays are assumed to be
 * sorted in ASCENDING order (the "r" version(s) assume descending order)
 *
 * Function returns length of target array (which is the sum of the 2 merged).
 */
/*---------------------------------------------------------------------------*/

size_t merge_dwords (const DWORD a1[], size_t c1,
							const DWORD a2[], size_t c2,
							DWORD			trgt[])
{
	size_t		i1=0, i2=0;
	const DWORD *p1=a1, *p2=a2;
	DWORD			*tp=trgt;
	
	while ((i1 < c1) && (i2 < c2))
	{
		if (*p1 <= *p2)
		{
			*tp = *p1;
			p1++;
			i1++;
		}
		else
		{
			*tp = *p2;
			p2++;
			i2++;
		}
		
		tp++;
	}
	
	/*
	 *		This point is reached if one of the indexes has reached its limit.
	 * This means that the remaining values in the other array can be copied
	 * to the target array as-is.
	 */

	if (i1 < c1)
		for ( ; i1 < c1; i1++, tp++, p1++)
			*tp = *p1;

	if (i2 < c2)
		for ( ; i2 < c2; i2++, tp++, p2++)
			*tp = *p2;

	return(c1+c2);
}

/*---------------------------------------------------------------------------*/

size_t merge_words (const WORD a1[], size_t c1,
						  const WORD a2[], size_t c2,
						  WORD		 trgt[])
{
	size_t	  i1=0, i2=0;
	const WORD *p1=a1, *p2=a2;
	WORD		  *tp=trgt;
	
	while ((i1 < c1) && (i2 < c2))
	{
		if (*p1 <= *p2)
		{
			*tp = *p1;
			p1++;
			i1++;
		}
		else
		{
			*tp = *p2;
			p2++;
			i2++;
		}
		
		tp++;
	}
	
	/*
	 *		This point is reached if one of the indexes has reached its limit.
	 * This means that the remaining values in the other array can be copied
	 * to the target array as-is.
	 */

	if (i1 < c1)
		for ( ; i1 < c1; i1++, tp++, p1++)
			*tp = *p1;

	if (i2 < c2)
		for ( ; i2 < c2; i2++, tp++, p2++)
			*tp = *p2;

	return(c1+c2);
}

/*---------------------------------------------------------------------------*/

size_t merge_bytes (const BYTE a1[], size_t c1,
						  const BYTE a2[], size_t c2,
						  BYTE		 trgt[])
{
	size_t	  i1=0, i2=0;
	const BYTE *p1=a1, *p2=a2;
	BYTE		  *tp=trgt;
	
	while ((i1 < c1) && (i2 < c2))
	{
		if (*p1 <= *p2)
		{
			*tp = *p1;
			p1++;
			i1++;
		}
		else
		{
			*tp = *p2;
			p2++;
			i2++;
		}
		
		tp++;
	}
	
	/*
	 *		This point is reached if one of the indexes has reached its limit.
	 * This means that the remaining values in the other array can be copied
	 * to the target array as-is.
	 */

	if (i1 < c1)
		for ( ; i1 < c1; i1++, tp++, p1++)
			*tp = *p1;

	if (i2 < c2)
		for ( ; i2 < c2; i2++, tp++, p2++)
			*tp = *p2;

	return(c1+c2);
}

/*---------------------------------------------------------------------------*/
