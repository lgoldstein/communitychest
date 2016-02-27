#include <_types.h>
#include <util/string.h>

/* counts until NULL/empty string found (Note: returns 0 if NULL entries array) */
DWORD count_string_entries (const char *entries[])
{
	DWORD	idx=0;

	if (NULL == entries)
		return 0;

	for (idx=0; ; idx++)
		if (IsEmptyStr(entries[idx]))
			return idx;

	return ((DWORD) (-1));	/* should not be reached !!! */
}

/*
 *	Scans the "entries" array for the given "str_entry" until NULL pointer entry
 * or end of entries encountered.
 *
 *	If unsuccessful returns a value which is <= "entries_num"
 */

DWORD find_string_entry (const char		str_entry[],
								 const char		*entries[],
								 const DWORD	entries_num,
								 const BOOLEAN	fCaseSensitive)
{
	DWORD idx=0;

	if ((NULL == str_entry) || (NULL == entries))
		return(entries_num);

	for (idx=0; (idx < entries_num) && (entries[idx] != NULL); idx++)
	{
		const char	*pszEntry=entries[idx];
		const int	nRes=(fCaseSensitive ? strcmp(str_entry, pszEntry) : stricmp(str_entry, pszEntry));

		if (0 == nRes)
			return(idx);
	}

	/*
	 * This point is reached if entry not found or NULL pointer encountered
	 */
	
	return(entries_num);
}
