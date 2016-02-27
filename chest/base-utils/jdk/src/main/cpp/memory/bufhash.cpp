#include <util/memory.h>

/*--------------------------------------------------------------------------*/

/*
 * sdbm - This algorithm was created for sdbm (a reimplementation of ndbm) database library. 
 * it does well in scrambling bits, causing better distribution of the keys and fewer splits. 
 * it also happens to be a good general hashing function with good distribution. the actual function 
 * is hash(i) = hash(i - 1) * 65599 + str[i]; what is included below is the faster version 
 * used in gawk. [there is even a faster, duff-device version] the magic constant 65599 was 
 * picked out of thin air while experimenting with different constants, and turns out to be a prime. 
 * this is one of the algorithms used in berkeley db (see sleepycat) and elsewhere. 
 */
UINT32 sdbm_hash_buffer (const BYTE buf[], const UINT32 ulBufLen)
{
	if ((NULL == buf) || (0 == ulBufLen))
		return 0;

	UINT32 ulHash=0;
	for (UINT32	i=0; i < ulBufLen; ++i)
		ulHash = (ulHash << 6) + (ulHash << 16) - ulHash + buf[i]; /* hash * 65599 + c */
 
	return ulHash;
}

/*--------------------------------------------------------------------------*/

/* 
 * djb2 this algorithm (k=33) was first reported by dan bernstein many years ago in 
 * comp.lang.c. another version of this algorithm (now favored by bernstein) uses xor: 
 * hash(i) = hash(i - 1) * 33 ^ str[i]; 
 * the magic of number 33 (why it works better than many other constants, prime or not) has never been 
 * adequately explained. 
 */
UINT32 djb2_hash_buffer (const BYTE buf[], const UINT32 ulBufLen)
{
	if ((NULL == buf) || (0 == ulBufLen))
		return 0;

	UINT32	ulHash=5381U;
	for (UINT32	i=0; i < ulBufLen; ++i)
		ulHash = (ulHash << 5) + ulHash + buf[i]; /* hash * 33 + c */

	return ulHash;
}

/*--------------------------------------------------------------------------*/
