#ifndef _COMM_SOCKET_H_
#define _COMM_SOCKET_H_

/*---------------------------------------------------------------------------*/

#include <stdarg.h>
#include <stdio.h>
#include <string.h>

#ifdef WIN32
#	include <winsock.h>

#	ifdef __cplusplus
// class which takes care of initialization/cleanup of WinSock
class CWSAInitializer {
	private:
		WSADATA	m_wsad;
		HRESULT	m_hr;

	public:
		CWSAInitializer (const WORD wsaVer=0)
			: m_hr((HRESULT) (-1))
		{
			if (wsaVer != 0)
				Startup(wsaVer);
			else
				::memset(&m_wsad, 0, (sizeof m_wsad));
		}

		virtual HRESULT Startup (const WORD wsaVer)
		{
			if (m_hr != 0)
				m_hr = ::WSAStartup(wsaVer, &m_wsad);

			return m_hr;
		}

		virtual void Cleanup ()
		{
			if (0 == m_hr)
			{
				::WSACleanup();
				m_hr = (HRESULT) (-1);
			}
		}

		virtual HRESULT GetInitializationError () const
		{
			return m_hr;
		}

		virtual const WSADATA& GetInitializationData () const
		{
			return m_wsad;
		}

		virtual BOOL IsInitialized () const
		{ 
			return (0 == m_hr);
		}

		virtual ~CWSAInitializer () { Cleanup(); }
};	// end of WinSock initializer class

typedef u_long ioctl_value;

#	endif
#else	/* NOT WIN32 */
#	include <stropts.h>
#	include <sys/socket.h>
#	include <sys/time.h>	/* for select */

typedef int SOCKET;
typedef int ioctl_value;

#	ifdef __cplusplus
		int ioctlsocket (SOCKET s, int cmd, ioctl_value* argp)
		{
			return ioctl(s, cmd, argp); 
		}

		int closesocket (SOCKET s)
		{
			return close(s);
		}
#	else
#		define ioctlsocket(s,c,a)	ioctl((s),(c),(a))
#		define closesocket(s)		close(s)
#	endif	/* of __cplusplus */
#endif	/* of WIN32 */

/*---------------------------------------------------------------------------*/

#include <_types.h>

/* value for returning bad socket values */
#ifndef BAD_SOCKET
#	define BAD_SOCKET	((SOCKET) (-1))
#endif

/*---------------------------------------------------------------------------*/

/* creates a socket ready for serving (i.e. ready to 'accept') */
extern EXC_TYPE sock_server_setup (SOCKET *sock, const int port_num);

/*		Creates a (STREAM) socket connected to the specified host on the
 * specified port
 */
extern EXC_TYPE sock_connect (SOCKET		*sock,
										const char	hostname[],
										const int	port_num);

/*---------------------------------------------------------------------------*/

/*		Reads one command from socket. A command is defined as all characters
 * up to the first CRLF (which is not read as part of the line). If only <LF>
 * found, then it is considered an end-of-line as well.
 *
 * Function returns number of characters in the "buf" (up to "bufLen") or (-1)
 * if read error occurred.
 *
 * Note: an internal inactivity mechanism is implemented - if maxSecs != 0 then
 *			routine waits up to the specified number of seconds for input.
 */

extern int sockReadCmd (SOCKET sock, char buf[], const size_t bufLen, const SINT32 maxSecs);

/* writes the supplied buffer up to "buflen" characters.
 *
 * returns number of actual characters written or (-1) if write error occurred.
 */

extern int sockWrite (SOCKET sock, const char buf[], const size_t buflen);

#define MAX_SOCK_CMDF_LINE_LEN	255

/*		This routine provides a general formatting interface for writing a SHORT
 * command/response to a socket.
 *
 * Note: inefficient (uses "sprintf") and limited to up to MAX_SOCK_CMDF
 * characters.
 *
 * Returns number of actual characters written or (-1) if write error.
 */

extern int sockWriteVCmdf (SOCKET sock, const char fmt[], va_list ap);
extern int sockWriteCmdf (SOCKET sock, const char fmt[], ...);

/* if maxSecs == 0 then wait is infinite */
extern EXC_TYPE sockWaitMultiple (SOCKET			socks[],
											 const UINT32	ulNumSocks,
											 const UINT32	ulMaxSecs,
											 UINT32			*pulSockNdx);

extern EXC_TYPE sockWait (SOCKET sock, const UINT32 ulMaxSecs);

extern EXC_TYPE sockClose (SOCKET sock);

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CSockGuard {
	private:
		SOCKET&	m_sock;

	public:
		CSockGuard (SOCKET& sock) : m_sock(sock) { }

		void Close ()
		{
			if (m_sock != BAD_SOCKET)
			{
				sockClose(m_sock);
				m_sock = BAD_SOCKET;
			}
		}

		virtual ~CSockGuard ()
		{
			Close();
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// implemets a sockets set
class CSockSet {
	private:
		UINT32	m_ulSockCount;
		SOCKET	m_Socks[FD_SETSIZE+1];

	public:
		CSockSet (SOCKET sock=BAD_SOCKET) : m_ulSockCount(0)
		{
			if (sock != BAD_SOCKET)
				SetSocket(sock);
		}

		// copy constructor
		CSockSet (const CSockSet& cs)
		{
			SetSet(cs);
		}

		virtual UINT32 GetSize () const { return m_ulSockCount; }
		virtual BOOLEAN IsEmpty () const { return (0 == m_ulSockCount); }

		// Note: only up to available size is added (otherwise EOVERFLOW)
		virtual EXC_TYPE AddSocket (SOCKET sock);
		virtual EXC_TYPE AddSet (const CSockSet& cs);

		// Note: returns EEXIST if not found
		virtual EXC_TYPE DelSocket (SOCKET sock);
		// Note: returns EOK even if none found
		virtual EXC_TYPE DelSet (const CSockSet& cs);

		virtual EXC_TYPE SetSocket (SOCKET sock);
		virtual EXC_TYPE SetSet (const CSockSet& cs);

		virtual void ClearSet () { m_ulSockCount = 0; }

		// operator(s)
		CSockSet& operator= (const CSockSet& cs)
		{
			SetSet(cs);
			return *this;
		}

		CSockSet& operator= (SOCKET sock)
		{
			SetSocket(sock);
			return *this;
		}

		CSockSet& operator+= (const CSockSet& cs)
		{
			AddSet(cs);
			return *this;
		}

		CSockSet& operator+= (SOCKET sock)
		{
			AddSocket(sock);
			return *this;
		}

		CSockSet& operator-= (const CSockSet& cs)
		{
			DelSet(cs);
			return *this;
		}

		CSockSet& operator-= (SOCKET sock)
		{
			DelSocket(sock);
			return *this;
		}

		virtual ~CSockSet () {}

		friend class CSockSetEnum;
};	// end of CSockSet class definition
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CSockSetEnum {
	private:
		UINT32			m_ulCurIdx;
		const CSockSet	*m_pSet;

		// disable copy constructor and assignment operator
		CSockSetEnum (const CSockSetEnum& ce);
		CSockSetEnum& operator= (const CSockSetEnum& ce);

	public:
		CSockSetEnum (const CSockSet *pSet=NULL) : m_ulCurIdx(0), m_pSet(pSet) {}

		virtual SOCKET GetFirstSock ()
		{
			if (m_pSet != NULL)
			{
				m_ulCurIdx = 0;
				if (m_ulCurIdx < m_pSet->m_ulSockCount)
					return m_pSet->m_Socks[m_ulCurIdx];
			}

			return BAD_SOCKET;
		}

		virtual SOCKET GetNextSock ()
		{
			if (m_pSet != NULL)
			{
				if (m_ulCurIdx < (m_pSet->m_ulSockCount-1))
				{
					m_ulCurIdx++;
					return m_pSet->m_Socks[m_ulCurIdx];
				}
			}

			return BAD_SOCKET;
		}

		virtual ~CSockSetEnum () {}
};	// end of CSockSetEnum class definition
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// interface for socket I/O class(es)
class ISockioInterface;

typedef struct {
	unsigned	m_fReadOp	:	1;
	unsigned m_fReadCmd	:	1;
	unsigned m_fReadCRLF	:	1;
} ISOCKIOFLAGS;

// callback for read/write operations
//
// called immediately AFTER read and BEFORE write
//
// Note: it is intended for montioring only - no changes should be made to the data
typedef EXC_TYPE (*ISOCK_IO_CFN)(ISockioInterface&		ISock,
											const ISOCKIOFLAGS&	iFlags,
											const BYTE				buf[],
											const int				bufLen,
											LPVOID					pArg);

class ISockioInterface {
	protected:
		UINT32			m_ulReadCount;
		UINT32			m_ulWriteCount;
		ISOCK_IO_CFN	m_lpfnIcfn;
		LPVOID			m_pIArg;

	public:
		// also default constructor
		ISockioInterface (ISOCK_IO_CFN lpfnIcfn=NULL, LPVOID pIArg=NULL)
			: m_ulReadCount(0)
			, m_ulWriteCount(0)
			, m_lpfnIcfn(lpfnIcfn)
			, m_pIArg(pIArg)
		{
		}

		virtual UINT32 GetReadCount () const
		{
			return m_ulReadCount;
		}

		virtual UINT32 GetWriteCount () const
		{
			return m_ulWriteCount;
		}

		// Note: if already set then error returned
		virtual EXC_TYPE SetIOCfn (ISOCK_IO_CFN lpfnIcfn, LPVOID pIArg);

		/*		Initiates a connection to the specified host on the requested
		 * port, and resets the read/write count.
		 *
		 * Note: caller must close the socket (not done by the destructor)
		 */
		virtual EXC_TYPE Connect (const char hostName[], const int iPort) = 0;

		virtual UINT32 GetConnectionId () const = 0;

		virtual bool IsConnected () const = 0;

		/*
		 *		Reads up to specified buffer size - if data available in buffer then
		 * returns data from it. Otherwise waits up to specified number of seconds
		 * and returns whatever data received by then.
		 *
		 * Note: the fact that returned data len is less than requested does not mean
		 *			in any way that the peer host finished sending a message !!!
		 */
		virtual int Read (char buf[], const size_t bufLen, const SINT32 maxSecs=0) = 0;

		// Note: attempts to fill entire buffer - fails if unsuccessful !!!
		virtual int Fill (char buf[], const size_t bufLen, const SINT32 maxSecs=0);

		/*		Reads one command from socket. A command is defined as all characters
		 * up to the first CRLF (which is not read as part of the line). If only <LF>
		 * found, then it is considered an end-of-line as well.
		 *
		 * Function returns number of characters in the "buf" (up to "bufLen") or (-1)
		 * if read error occurred.
		 *
		 * Note: an internal inactivity mechanism is implemented - if maxSecs != 0 then
		 *			routine waits up to the specified number of seconds for input.
		 */

		virtual int ReadCmd (char				buf[],
									const size_t	bufLen,
									const SINT32	maxSecs=0,
									BOOLEAN			*pfStrippedCRLF=NULL) = 0;

		virtual int Write (const char buf[], const size_t bufLen) = 0;

		virtual int Write (const char buf[])
		{
			if (NULL == buf)
				return (-1);
			else
				return Write(buf, strlen(buf));
		}

		virtual int Writeln (const char buf[], const size_t bufLen) = 0;

		virtual int Writeln (const char buf[])
		{
			if (NULL == buf)
				return (-1);
			else
				return Writeln(buf, strlen(buf));
		}

		virtual int Writeln (void)
		{
			return Write("\r\n", 2);
		}

		virtual int WriteVCmdf (const char fmt[], va_list ap) = 0;

		// Note: calls "WriteVCmdf"
		virtual int WriteCmdf (const char fmt[], ...);

		// closes the socket and releases the associated resources
		virtual EXC_TYPE Close () = 0;

		virtual ~ISockioInterface () { }
};	// end of ISockioInterface class

typedef CAllocStructPtrGuard<ISockioInterface>	CSockioIfGuard;
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CDummySockio : public ISockioInterface {
	public:
		CDummySockio (ISOCK_IO_CFN lpfnIcfn=NULL, LPVOID pIArg=NULL)
			: ISockioInterface(lpfnIcfn, pIArg)
		{
		}

		virtual EXC_TYPE Connect (const char hostName[], const int iPort)
		{
			return (-1);
		}

		virtual UINT32 GetConnectionId () const
		{
			return ((UINT32) BAD_SOCKET);
		}

		virtual bool IsConnected () const
		{
			return false;
		}

		virtual int Read (char buf[], const size_t bufLen, const SINT32 maxSecs=0)
		{
			return (-1);
		}

		// Note: attempts to fill entire buffer - fails if unsuccessful !!!
		virtual int Fill (char buf[], const size_t bufLen, const SINT32 maxSecs=0)
		{
			return (-1);
		}

		virtual int ReadCmd (char				buf[],
									const size_t	bufLen,
									const SINT32	maxSecs=0,
									BOOLEAN			*pfStrippedCRLF=NULL)
		{
			return (-1);
		}

		virtual int Write (const char buf[], const size_t bufLen)
		{
			return (-1);
		}

		virtual int Writeln (const char buf[], const size_t bufLen)
		{
			return (-1);
		}

		virtual int WriteVCmdf (const char fmt[], va_list ap)
		{
			return (-1);
		}

		virtual EXC_TYPE Close ()
		{
			return (-1);
		}

		virtual ~CDummySockio ()
		{
		}
};
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
class CFileSockio : public ISockioInterface {
	protected:
		FILE	*m_fio;

	public:
		CFileSockio (ISOCK_IO_CFN lpfnIcfn=NULL, LPVOID pIArg=NULL)
			: ISockioInterface(lpfnIcfn, pIArg), m_fio(NULL)
		{
		}

		// file is assumed to be open with correct mode (read/write + binary mode)
		virtual EXC_TYPE Attach (FILE *fp);

		virtual FILE *GetFile () const
		{
			return m_fio;
		}

		// Does not close the file
		virtual EXC_TYPE Detach ()
		{
			m_fio = NULL;
			return EOK;
		}

		// host name is taken to mean file name, port: 0 == read, > 0 == write, < 0 == error
		virtual EXC_TYPE Connect (const char hostName[], const int iPort);

		virtual UINT32 GetConnectionId () const
		{
			return ((NULL == m_fio) ? ((UINT32) BAD_SOCKET) : ((UINT32) m_fio));
		}

		virtual bool IsConnected () const
		{
			return (GetFile() != NULL);
		}

		virtual int Read (char buf[], const size_t bufLen, const SINT32 maxSecs=0);

		virtual int ReadCmd (char				buf[],
									const size_t	bufLen,
									const SINT32	maxSecs=0,
									BOOLEAN			*pfStrippedCRLF=NULL);

		virtual int Write (const char buf[], const size_t bufLen);

		virtual int Writeln (const char buf[], const size_t bufLen);

		virtual int WriteVCmdf (const char fmt[], va_list ap);

		virtual EXC_TYPE Close ();

		virtual ~CFileSockio ()
		{
			EXC_TYPE	exc=Close();
		}
};

typedef CAllocStructPtrGuard<CFileSockio>	CFileSockioGuard;
#endif	/* of ifdef __cplusplus */
/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
extern EXC_TYPE CopyNamedFileToSocket (const char			pszFPath[],
													ISockioInterface&	ISock,
													const UINT32		ulBufLen,
													BYTE					*pBuf);	// may be NULL

extern EXC_TYPE CopyFileToSocket (FILE						*fin,
											 ISockioInterface&	ISock,
											 const UINT32			ulBufLen,
											 BYTE						*pBuf);	// may be NULL
#endif /* __cplusplus */

/*---------------------------------------------------------------------------*/

/* approx. an Ethernet frame */
#define DEFAULT_BUFFSOCK_LEN	1436

#ifdef __cplusplus
// clas to implement smart socket buffering on read (!)
class CBuffSock : public ISockioInterface {
	private:
		SOCKET			m_sock;
		UINT32			m_ulMaxLen;	// maximum available buffer
		UINT32			m_ulCurLen;	// currently valid data size
		UINT32			m_ulCurIdx;	// first unread data byte
		BYTE				*m_pBuf;
		BOOLEAN			m_fAutoAlloc;

		// disable copy constructor and assignment operator
		CBuffSock (const CBuffSock& cb);
		CBuffSock& operator= (const CBuffSock& cb);

		// returns number of actually read data bytes (or negative if error)
		// if maxSecs != 0 then awaits maximum specified seconds for data to
		// become available on socket
		int FillBuf (const SINT32 maxSecs);
		int CfnWriteVCmdf (const char fmt[], va_list ap);

	public:
		// also default constructor
		CBuffSock (SOCKET			sock=BAD_SOCKET,
					  const UINT32 ulMaxLen=DEFAULT_BUFFSOCK_LEN,
					  LPBYTE			pBuf=NULL,
					  ISOCK_IO_CFN	lpfnIcfn=NULL,
					  LPVOID			pIArg=NULL);

		CBuffSock (ISOCK_IO_CFN	lpfnIcfn, LPVOID pIArg);

		//	Initiates a connection to the specified host on the requested port.
		virtual EXC_TYPE Connect (const char hostName[], const int iPort);

		virtual EXC_TYPE SetIOBuffer (const UINT32	ulMaxLen=DEFAULT_BUFFSOCK_LEN,
												LPBYTE			pBuf=NULL);

		virtual EXC_TYPE Attach (SOCKET			sock,
										 const UINT32	ulMaxLen=DEFAULT_BUFFSOCK_LEN,
										 LPBYTE			pBuf=NULL);

		// Releases all internal resources (if any)
		//
		// Note: does not close the socket unless required!!!
		virtual EXC_TYPE Detach (const BOOL fCloseSocket=FALSE);

		/*
		 *		Reads up to specified buffer size - if data available in buffer then
		 * returns data from it. Otherwise waits up to specified number of seconds
		 * and returns whatever data received by then.
		 *
		 * Note: the fact that returned data len is less than requested does not mean
		 *			in any way that the peer host finished sending a message !!!
		 */
		virtual int Read (char buf[], const size_t bufLen, const SINT32 maxSecs=0);

		/*		Reads one command from socket. A command is defined as all characters
		 * up to the first CRLF (which is not read as part of the line). If only <LF>
		 * found, then it is considered an end-of-line as well.
		 *
		 * Function returns number of characters in the "buf" (up to "bufLen") or (-1)
		 * if read error occurred.
		 *
		 * Note: an internal inactivity mechanism is implemented - if maxSecs != 0 then
		 *			routine waits up to the specified number of seconds for input.
		 */

		virtual int ReadCmd (char				buf[],
									const size_t	bufLen,
									const SINT32	maxSecs=0,
									BOOLEAN			*pfStrippedCRLF=NULL);

		virtual SOCKET GetSocket () const
		{
			return m_sock;
		}

		virtual UINT32 GetConnectionId () const
		{
			return (UINT32) GetSocket();
		}

		virtual bool IsConnected () const
		{
			return (GetSocket() != BAD_SOCKET);
		}

		virtual int Write (const char buf[], const size_t bufLen);

		virtual int Writeln (const char buf[], const size_t bufLen);

		virtual int WriteVCmdf (const char fmt[], va_list ap);

		// throws away any current data and also exhausts all currently
		// available data from socket.
		virtual EXC_TYPE FlushReadBuffer ();

		// closes the socket and releases the associated resources (calls Detach)
		virtual EXC_TYPE Close ();

		virtual UINT32 GetAvailableData () const
		{
			return ((m_ulCurLen >= m_ulCurIdx) ? (m_ulCurLen - m_ulCurIdx) : 0);
		}

		virtual ~CBuffSock ()
		{ 
			Close();
		}
};	// end of CBuffSock class definition

typedef CAllocStructPtrGuard<CBuffSock>	CBuffSockGuard;

// returns index of first (!) socket with available data
extern EXC_TYPE WaitOnMultipleBuffSocks (const CBuffSock	*socks[],
													  const UINT32		ulNumSocks,
													  const UINT32		ulWaitTimeout,
													  UINT32&			ulSdx);

// assumes last one is NULL
extern EXC_TYPE WaitOnMultipleBuffSocks (const CBuffSock	*socks[],
													  const UINT32		ulWaitTimeout,
													  UINT32&			ulSdx);

#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

#ifdef __cplusplus
// class to implement a one-to-may socket(s) relationship:
//
// reading is done from one specific socket, but writing is performed on all
class CMultiSock : public ISockioInterface {
	private:
		CSockSet		m_WriteSocks;
		CBuffSock	m_ReadSock;

		// disable copy constructor and assignment operator
		CMultiSock (const CMultiSock& ms);
		CMultiSock& operator= (const CMultiSock& ms);

	public:
		CMultiSock () : ISockioInterface()
		{
		}

		virtual EXC_TYPE SetIOCfn (ISOCK_IO_CFN lpfnIcfn, LPVOID pIArg)
		{
			return m_ReadSock.SetIOCfn(lpfnIcfn, pIArg);
		}

		virtual EXC_TYPE SetReadSocket (SOCKET				sock,
												  const UINT32		ulMaxLen=DEFAULT_BUFFSOCK_LEN,
												  const BOOLEAN	fAddToWriteSet=TRUE);

		virtual EXC_TYPE SetReadSocket (const CBuffSock&	bSock,
												  const UINT32			ulMaxLen=DEFAULT_BUFFSOCK_LEN,
												  const BOOLEAN		fAddToWriteSet=TRUE)
		{
			return SetReadSocket(bSock.GetSocket(), ulMaxLen, fAddToWriteSet);
		}

		virtual SOCKET GetReadSocket () const { return m_ReadSock.GetSocket(); }
		virtual const CBuffSock& GetReadCBSock () const { return m_ReadSock; }

		virtual UINT32 GetAvailableData () const { return m_ReadSock.GetAvailableData(); }

		virtual UINT32 GetSize () const { return m_WriteSocks.GetSize(); }
		virtual BOOLEAN IsEmpty () const { return m_WriteSocks.IsEmpty(); }

		// adds socket to writing set
		//
		// Note: returns EEXIST if socket already in set
		virtual EXC_TYPE Attach (SOCKET sock)
		{
			return m_WriteSocks.AddSocket(sock);
		}

		virtual EXC_TYPE Attach (const CBuffSock& bSock)
		{
			return Attach(bSock.GetSocket());
		}

		// remove socket from writing set
		//
		// Note: returns EEXIST if socket not in set
		virtual EXC_TYPE Detach (SOCKET sock)
		{
			return m_WriteSocks.DelSocket(sock);
		}

		virtual EXC_TYPE Detach (const CBuffSock& bSock)
		{
			return Detach(bSock.GetSocket());
		}

		virtual EXC_TYPE Connect (const char	hostName[],
										  const int		iPort)
		{
			return m_ReadSock.Connect(hostName, iPort);
		}

		virtual UINT32 GetReadCount () const
		{
			return m_ReadSock.GetReadCount();
		}

		virtual UINT32 GetWriteCount () const
		{
			return m_ReadSock.GetWriteCount();
		}

		virtual UINT32 GetConnectionId () const
		{
			return m_ReadSock.GetConnectionId();
		}

		virtual bool IsConnected () const
		{
			return m_ReadSock.IsConnected();
		}
		/*
		 *		Reads up to specified buffer size - if data available in buffer then
		 * returns data from it. Otherwise waits up to specified number of seconds
		 * and returns whatever data received by then.
		 *
		 * Note: the fact that returned data len is less than requested does not mean
		 *			in any way that the peer host finished sending a message !!!
		 */
		virtual int Read (char buf[], const size_t bufLen, const SINT32 maxSecs=0)
		{
			return m_ReadSock.Read(buf, bufLen, maxSecs);
		}

		/*		Reads one command from socket. A command is defined as all characters
		 * up to the first CRLF (which is not read as part of the line). If only <LF>
		 * found, then it is considered an end-of-line as well.
		 *
		 * Function returns number of characters in the "buf" (up to "bufLen") or (-1)
		 * if read error occurred.
		 *
		 * Note: an internal inactivity mechanism is implemented - if maxSecs != 0 then
		 *			routine waits up to the specified number of seconds for input.
		 */

		virtual int ReadCmd (char buf[], const size_t bufLen, const SINT32 maxSecs=0, BOOLEAN	*pfStrippedCRLF=NULL)
		{
			return m_ReadSock.ReadCmd(buf, bufLen, maxSecs, pfStrippedCRLF);
		}

		// Note: error is returned only if writing fails on read socket (if written to)
		virtual int Write (const char buf[], const size_t bufLen);

		virtual int WriteVCmdf (const char fmt[], va_list ap);

		virtual int Writeln (const char buf[], const size_t bufLen);

		// closes the (read) socket and releases the associated resources
		virtual EXC_TYPE Close ()
		{
			return m_ReadSock.Close();
		}

		virtual EXC_TYPE CloseAll (const BOOL fCloseReadSock=TRUE);

		// Note: does not close any of the sockets
		virtual ~CMultiSock ()
		{
			m_ReadSock.Detach(FALSE);
		}

}; // end of CMultiSock class definition
#endif	/* of ifdef __cplusplus */

/*---------------------------------------------------------------------------*/

/* callback to write to SOCKET (pFout == socket) */
extern UINT32 sockIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen);

#ifdef __cplusplus
/* callback to write to a ISockioInterface object */
extern UINT32 isioIOWriteCfn (void *pFout, const char pBuf[], const UINT32 ulBufLen);

/* callback to read from a ISockioInterface object */
extern UINT32 isioIOReadCfn (void *pFin, UINT8 pBuf[], const UINT32 ulBufLen);
#endif	/* of __cplusplus */

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _COMM_SOCKET_H_ */
